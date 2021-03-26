package tictim.paraglider.contents;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

import static tictim.paraglider.ParagliderMod.MODID;

public final class ModAdvancements{
	private ModAdvancements(){}

	public static final ResourceLocation STATUES_BARGAIN = new ResourceLocation(MODID, "statues_bargain");

	public static boolean give(ServerPlayerEntity player, ResourceLocation advancementName, String criterion){
		PlayerAdvancements advancements = player.getAdvancements();
		AdvancementManager advancementManager = player.getServerWorld().getServer().getAdvancementManager();
		Advancement advancement = advancementManager.getAdvancement(advancementName);
		return advancement!=null&&advancements.grantCriterion(advancement, criterion);
	}

	public static boolean has(ServerPlayerEntity player, ResourceLocation advancementName){
		PlayerAdvancements advancements = player.getAdvancements();
		AdvancementManager advancementManager = player.getServerWorld().getServer().getAdvancementManager();
		Advancement advancement = advancementManager.getAdvancement(advancementName);
		return advancement!=null&&advancements.getProgress(advancement).isDone();
	}
}
