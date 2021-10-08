package tictim.paraglider.contents;

import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;

import static tictim.paraglider.ParagliderMod.MODID;

public final class ModAdvancements{
	private ModAdvancements(){}

	public static final ResourceLocation PRAY_TO_THE_GODDESS = new ResourceLocation(MODID, "pray_to_the_goddess");
	public static final ResourceLocation STATUES_BARGAIN = new ResourceLocation(MODID, "statues_bargain");
	public static final ResourceLocation ALL_VESSELS = new ResourceLocation(MODID, "all_vessels");

	public static boolean give(ServerPlayer player, ResourceLocation advancementName, String criterion){
		PlayerAdvancements advancements = player.getAdvancements();
		ServerAdvancementManager advancementManager = player.getLevel().getServer().getAdvancements();
		Advancement advancement = advancementManager.getAdvancement(advancementName);
		return advancement!=null&&advancements.award(advancement, criterion);
	}

	public static boolean has(ServerPlayer player, ResourceLocation advancementName){
		PlayerAdvancements advancements = player.getAdvancements();
		ServerAdvancementManager advancementManager = player.getLevel().getServer().getAdvancements();
		Advancement advancement = advancementManager.getAdvancement(advancementName);
		return advancement!=null&&advancements.getOrStartProgress(advancement).isDone();
	}
}
