package tictim.paraglider.config;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.ParagliderUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BlockMatcher{
	private static final BlockMatcher empty = new BlockMatcher(new String[0], null, null);

	// #((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)|((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)(?:\s*#\s*([A-Za-z0-9_.-]+\s*=\s*[A-Za-z0-9_.-]+(?:\s*,\s*[A-Za-z0-9_.-]+\s*=\s*[A-Za-z0-9_.-]+)*))?
	private static final Pattern REGEX = Pattern.compile("^#((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)|((?:[a-z0-9_.-]+:)?[a-z0-9_.-]+)"+
			"(?:\\s*#\\s*([A-Za-z0-9_.-]+\\s*=\\s*[A-Za-z0-9_.-]+(?:\\s*,\\s*[A-Za-z0-9_.-]+\\s*=\\s*[A-Za-z0-9_.-]+)*))?$");

	private static final int GROUP_TAG = 1;
	private static final int GROUP_BLOCK_ID = 2;
	private static final int GROUP_PROPERTIES = 3;

	@NotNull public static BlockMatcher empty(){
		return empty;
	}

	@NotNull public static Result parse(@NotNull Collection<? extends String> inputs){
		return new Result(inputs.toArray(new String[0]));
	}

	@NotNull public static BlockMatcher read(@NotNull FriendlyByteBuf buffer){
		var inputs = new String[buffer.readVarInt()];
		for(int i = 0; i<inputs.length; i++){
			inputs[i] = buffer.readUtf();
		}
		return new Result(inputs).result();
	}

	private final String[] inputs;
	private final Map<Block, Predicate<BlockState>> blockMatches;
	private final Set<TagKey<Block>> tagMatches;

	private BlockMatcher(@NotNull String @NotNull [] inputs,
	                     @Nullable Map<@NotNull Block, Predicate<BlockState>> blockMatches,
	                     @Nullable Set<@NotNull TagKey<Block>> tagMatches){
		this.inputs = inputs;
		this.blockMatches = blockMatches;
		this.tagMatches = tagMatches;
	}

	public boolean test(@NotNull BlockState state){
		if(this.blockMatches!=null){
			Predicate<BlockState> predicate = this.blockMatches.get(state.getBlock());
			if(predicate!=null&&predicate.test(state)) return true;
		}
		if(this.tagMatches!=null){
			for(TagKey<Block> tag : this.tagMatches){
				if(ParagliderUtils.hasTag(state.getBlock(), tag)) return true;
			}
		}
		return false;
	}

	public void write(@NotNull FriendlyByteBuf buffer){
		buffer.writeVarInt(inputs.length);
		for(String input : inputs){
			buffer.writeUtf(input);
		}
	}

	public static final class Result{
		private final BlockMatcher result;
		@Nullable private List<Error> errors;

		private Result(@NotNull String @NotNull [] inputs){
			Map<Block, Predicate<BlockState>> blockMatches = null;
			Set<TagKey<Block>> tagMatches = null;

			Matcher m = REGEX.matcher("");
			for(String input : inputs){
				if(!m.reset(input).matches()){
					addError(input, "Malformed input");
					continue;
				}

				if(m.group(GROUP_TAG)!=null){
					if(tagMatches==null) tagMatches = new ObjectOpenHashSet<>();
					tagMatches.add(TagKey.create(Registries.BLOCK, new ResourceLocation(m.group(GROUP_TAG))));
				}else{
					Block block = ParagliderUtils.getBlock(new ResourceLocation(m.group(GROUP_BLOCK_ID)));
					if(block==Blocks.AIR){
						addError(input, "No block named '"+m.group(GROUP_BLOCK_ID)+"' exists");
						continue;
					}
					Predicate<BlockState> p = parseBlockMatch(input, m, block);
					if(p!=null){
						if(blockMatches==null) blockMatches = new Object2ObjectOpenHashMap<>();
						blockMatches.compute(block, (k, v) -> v==null ? p : v.or(p));
					}
				}
			}

			this.result = new BlockMatcher(inputs, blockMatches, tagMatches);
		}

		@Nullable private Predicate<BlockState> parseBlockMatch(String input, Matcher matcher, Block block){
			String blockState = matcher.group(GROUP_PROPERTIES);
			if(blockState==null) return BlockStatePredicate.ANY;

			Map<String, String> properties = new HashMap<>();
			for(String s : blockState.split(",")){
				int i = s.indexOf('=');
				String key = s.substring(0, i);
				if(properties.containsKey(key)){
					addError(input, "Same property '"+key+"' checked twice");
					return null;
				}else properties.put(key, s.substring(i+1));
			}

			Map<Property<?>, Object> parsedProperties = new IdentityHashMap<>();
			for(var e : properties.entrySet()){
				String key = e.getKey();
				Property<?> property = block.getStateDefinition().getProperty(key);
				if(property==null){
					addError(input, "Property with name '"+key+"' does not exist on block properties");
					return null;
				}else if(parsedProperties.containsKey(property)){
					addError(input, "Same property '"+key+"' checked twice");
					return null;
				}
				Optional<?> o = property.getValue(e.getValue());
				if(o.isEmpty()){
					addError(input, "Property with name '"+key+"' does not contain value '"+e.getValue()+"'");
					return null;
				}
				parsedProperties.put(property, o.get());
			}
			BlockStatePredicate m = BlockStatePredicate.forBlock(block);
			for(var e : parsedProperties.entrySet()){
				Object v = e.getValue();
				m.where(e.getKey(), o -> o!=null&&o.equals(v));
			}
			return m;
		}

		private void addError(String input, String cause){
			if(this.errors==null) this.errors = new ArrayList<>();
			this.errors.add(new Error(input, cause));
		}

		@NotNull public BlockMatcher result(){
			return result;
		}
		@NotNull @Unmodifiable public List<Error> errors(){
			return errors==null ? List.of() : Collections.unmodifiableList(errors);
		}

		public void printErrors(){
			if(errors==null||errors.isEmpty()) return;
			ParagliderMod.LOGGER.warn("Found {} error(s) in wind source configuration:", errors.size());
			for(Error error : errors){
				ParagliderMod.LOGGER.warn("  \"{}\": {}", error.input, error.cause);
			}
		}
	}

	public record Error(String input, String cause){}
}
