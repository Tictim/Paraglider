package tictim.paraglider.event;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
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

import static tictim.paraglider.ParagliderMod.MODID;

@Mod.EventBusSubscriber(modid = ParagliderMod.MODID)
public final class ParagliderEventHandler{
	private ParagliderEventHandler(){}

	// PlayerEntity#livingTick(), default value of jumpMovementFactor while sprinting
	private static final double DEFAULT_PARAGLIDING_SPEED = 0.02+0.005999999865889549;

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void loadBiome(BiomeLoadingEvent event){
		switch(event.getCategory()){
			case EXTREME_HILLS:
			case MESA:
			case PLAINS:
			case SAVANNA:
				event.getGeneration().getStructures().add(() -> ModStructures.TARREY_TOWN_GODDESS_STATUE_CONFIGURED);
			case NONE:
			case TAIGA:
			case FOREST:
			case JUNGLE:
			case ICY:
			case BEACH:
			case DESERT:
			case RIVER:
			case MUSHROOM:
				event.getGeneration().getStructures().add(() -> ModStructures.UNDERGROUND_HORNED_STATUE_CONFIGURED);
				break;
			case NETHER:{
				ResourceLocation name = event.getName();
				if(name==null||(!name.equals(Biomes.BASALT_DELTAS.getLocation())&&!name.equals(Biomes.CRIMSON_FOREST.getLocation())))
					event.getGeneration().getStructures().add(() -> ModStructures.NETHER_HORNED_STATUE_CONFIGURED);
				break;
			}
			// case THEEND: case OCEAN: case SWAMP: // no-op
		}
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event){
		if(!(event.getWorld() instanceof ServerWorld)) return;
		ServerWorld world = (ServerWorld)event.getWorld();

		if(world.getChunkProvider().getChunkGenerator() instanceof FlatChunkGenerator&&
				world.getDimensionKey().equals(World.OVERWORLD)) return;

		Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(world.getChunkProvider().generator.func_235957_b_().func_236195_a_());
		if(ModCfg.enableStructures()){
			tempMap.put(ModStructures.UNDERGROUND_HORNED_STATUE, ModStructures.UNDERGROUND_HORNED_STATUE_SEPARATION_SETTINGS);
			tempMap.put(ModStructures.NETHER_HORNED_STATUE, ModStructures.NETHER_HORNED_STATUE_SEPARATION_SETTINGS);
			tempMap.put(ModStructures.TARREY_TOWN_GODDESS_STATUE, ModStructures.TARREY_TOWN_GODDESS_STATUE_SEPARATION_SETTINGS);
		}else{
			tempMap.remove(ModStructures.UNDERGROUND_HORNED_STATUE);
			tempMap.remove(ModStructures.NETHER_HORNED_STATUE);
			tempMap.remove(ModStructures.TARREY_TOWN_GODDESS_STATUE);
		}
		world.getChunkProvider().generator.func_235957_b_().field_236193_d_ = tempMap;
	}

	@SubscribeEvent
	public static void onPlayerInteract(PlayerInteractEvent event){
		if(event.isCancelable()){
			PlayerMovement m = PlayerMovement.of(event.getPlayer());
			if(m!=null&&m.isParagliding()) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPlayerStartUseItem(LivingEntityUseItemEvent.Start event){
		PlayerMovement m = PlayerMovement.of(event.getEntityLiving());
		if(m!=null&&m.isParagliding()) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onPlayerTickUseItem(LivingEntityUseItemEvent.Tick event){
		PlayerMovement m = PlayerMovement.of(event.getEntityLiving());
		if(m!=null&&m.isParagliding()) event.getEntityLiving().resetActiveHand();
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event){
		PlayerMovement m1 = PlayerMovement.of(event.getOriginal());
		PlayerMovement m2 = PlayerMovement.of(event.getPlayer());
		if(m1!=null&&m2!=null) m1.copyTo(m2);
	}

	private static final ResourceLocation MOVEMENT_HANDLER_KEY = new ResourceLocation(MODID, "paragliding_movement_handler");

	@SubscribeEvent
	public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event){
		Entity o = event.getObject();
		if(o instanceof PlayerEntity){
			PlayerEntity p = (PlayerEntity)o;
			PlayerMovement m = p instanceof ServerPlayerEntity ? new ServerPlayerMovement((ServerPlayerEntity)p) :
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
					event.player.jumpMovementFactor = (float)(DEFAULT_PARAGLIDING_SPEED*v);
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
		PlayerEntity player = event.getPlayer();
		if(player instanceof ServerPlayerEntity){
			PlayerMovement h = PlayerMovement.of(event.getTarget());
			if(h!=null){
				SyncParaglidingMsg msg = new SyncParaglidingMsg(h);
				if(ModCfg.traceParaglidingPacket()) ParagliderMod.LOGGER.debug("Sending packet {} from player {} to player {}", msg, h.player, player);
				ModNet.NET.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity)player), msg);
			}
		}
	}

	private static final class Client{
		public static PlayerMovement createPlayerMovement(PlayerEntity player){
			return player instanceof ClientPlayerEntity ? new ClientPlayerMovement(player) : new RemotePlayerMovement(player);
		}
	}
}
