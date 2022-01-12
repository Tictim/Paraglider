package tictim.paraglider.event;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.BiomeCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.ClientPlayerMovement;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.capabilities.RemotePlayerMovement;
import tictim.paraglider.capabilities.ServerPlayerMovement;
import tictim.paraglider.contents.ModStructures;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.network.SyncParaglidingMsg;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = ParagliderMod.MODID)
public final class ParagliderEventHandler{
	private ParagliderEventHandler(){}

	// PlayerEntity#livingTick(), default value of jumpMovementFactor while sprinting
	private static final double DEFAULT_PARAGLIDING_SPEED = 0.02+0.005999999865889549;

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event){
		if(!(event.getWorld() instanceof ServerLevel level)) return;

		ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();

		if(chunkGenerator instanceof FlatLevelSource&&level.dimension().equals(Level.OVERWORLD)) return;

		ImmutableMap.Builder<StructureFeature<?>, ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> b = ImmutableMap.builder();

		Map<StructureFeature<?>, HashMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>>> map = new HashMap<>();
		map.put(ModStructures.TARREY_TOWN_GODDESS_STATUE, HashMultimap.create());
		map.put(ModStructures.UNDERGROUND_HORNED_STATUE, HashMultimap.create());
		map.put(ModStructures.NETHER_HORNED_STATUE, HashMultimap.create());

		for(Entry<ResourceKey<Biome>, Biome> e : level.registryAccess().ownedRegistryOrThrow(Registry.BIOME_REGISTRY).entrySet()){
			Biome biome = e.getValue();
			BiomeCategory category = biome.getBiomeCategory();
			ResourceKey<Biome> biomeKey = e.getKey();
			switch(category){
				case EXTREME_HILLS:
				case MESA:
				case PLAINS:
				case SAVANNA:
					map.get(ModStructures.TARREY_TOWN_GODDESS_STATUE).put(ModStructures.TARREY_TOWN_GODDESS_STATUE_CONFIGURED, biomeKey);
				case NONE:
				case TAIGA:
				case FOREST:
				case JUNGLE:
				case ICY:
				case BEACH:
				case DESERT:
				case RIVER:
				case MUSHROOM:
					map.get(ModStructures.UNDERGROUND_HORNED_STATUE).put(ModStructures.UNDERGROUND_HORNED_STATUE_CONFIGURED, biomeKey);
					break;
				case NETHER:
					if(!biomeKey.equals(Biomes.BASALT_DELTAS)&&!biomeKey.equals(Biomes.CRIMSON_FOREST))
						map.get(ModStructures.NETHER_HORNED_STATUE).put(ModStructures.NETHER_HORNED_STATUE_CONFIGURED, biomeKey);
					break;
				// case THEEND: case OCEAN: case SWAMP: // no-op
			}
		}

		map.forEach((k, v) -> b.put(k, ImmutableMultimap.copyOf(v)));
		chunkGenerator.getSettings().configuredStructures.forEach((k, v) -> {
			if(!map.containsKey(k))
				b.put(k, v);
		});

		chunkGenerator.getSettings().configuredStructures = b.build();

		Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(chunkGenerator.getSettings().structureConfig());
		if(ModCfg.enableStructures()){
			tempMap.put(ModStructures.UNDERGROUND_HORNED_STATUE, ModStructures.UNDERGROUND_HORNED_STATUE_SEPARATION_SETTINGS);
			tempMap.put(ModStructures.NETHER_HORNED_STATUE, ModStructures.NETHER_HORNED_STATUE_SEPARATION_SETTINGS);
			tempMap.put(ModStructures.TARREY_TOWN_GODDESS_STATUE, ModStructures.TARREY_TOWN_GODDESS_STATUE_SEPARATION_SETTINGS);
		}else{
			tempMap.remove(ModStructures.UNDERGROUND_HORNED_STATUE);
			tempMap.remove(ModStructures.NETHER_HORNED_STATUE);
			tempMap.remove(ModStructures.TARREY_TOWN_GODDESS_STATUE);
		}
		chunkGenerator.getSettings().structureConfig = tempMap;
	}

	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent event){
		if(event.isCancelable()&&event.getHand()==InteractionHand.OFF_HAND){
			ServerPlayerMovement m = ServerPlayerMovement.of(event.getPlayer());
			if(m!=null&&m.isParagliding()) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event){
		Player original = event.getOriginal();
		original.reviveCaps();
		PlayerMovement m1 = PlayerMovement.of(original);
		PlayerMovement m2 = PlayerMovement.of(event.getPlayer());
		if(m1!=null&&m2!=null) m1.copyTo(m2);
		original.invalidateCaps();
	}

	@SubscribeEvent
	public static void onPlayerUseItem(LivingEntityUseItemEvent.Tick event){
		if(event.getEntityLiving().getUsedItemHand()==InteractionHand.OFF_HAND&&event.getEntityLiving() instanceof Player){
			ServerPlayerMovement m = ServerPlayerMovement.of(event.getEntityLiving());
			if(m!=null&&m.isParagliding()) event.getEntityLiving().stopUsingItem();
		}
	}

	private static final ResourceLocation MOVEMENT_HANDLER_KEY = new ResourceLocation(MODID, "paragliding_movement_handler");

	@SubscribeEvent
	public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event){
		if(event.getObject() instanceof Player p){
			PlayerMovement m = p instanceof ServerPlayer ? new ServerPlayerMovement((ServerPlayer)p) :
					DistExecutor.unsafeRunForDist(
							() -> () -> Client.createPlayerMovement(p),
							() -> () -> new RemotePlayerMovement(p));

			event.addCapability(MOVEMENT_HANDLER_KEY, m);
		}
	}

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event){
		PlayerMovement h = PlayerMovement.of(event.player);
		if(h!=null){
			if(event.phase==TickEvent.Phase.END){
				h.update();
			}else{
				if(h.isParagliding()){
					double v = ModCfg.paraglidingSpeed();
					event.player.flyingSpeed = (float)(DEFAULT_PARAGLIDING_SPEED*v);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent event){
		ServerPlayerMovement h = ServerPlayerMovement.of(event.getPlayer());
		if(h!=null){
			h.movementNeedsSync = true;
			h.paraglidingNeedsSync = true;
			h.vesselNeedsSync = true;
		}
	}

	@SubscribeEvent
	public static void onStartTracking(PlayerEvent.StartTracking event){
		Player player = event.getPlayer();
		if(player instanceof ServerPlayer sp){
			PlayerMovement h = PlayerMovement.of(event.getTarget());
			if(h!=null){
				SyncParaglidingMsg msg = new SyncParaglidingMsg(h);
				if(ModCfg.traceParaglidingPacket()) ParagliderMod.LOGGER.debug("Sending packet {} from player {} to player {}", msg, h.player, player);
				ModNet.NET.send(PacketDistributor.PLAYER.with(() -> sp), msg);
			}
		}
	}

	private static final class Client{
		public static PlayerMovement createPlayerMovement(Player player){
			return player instanceof LocalPlayer ? new ClientPlayerMovement(player) : new RemotePlayerMovement(player);
		}
	}
}
