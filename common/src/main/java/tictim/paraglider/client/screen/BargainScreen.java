package tictim.paraglider.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.bargain.Bargain;
import tictim.paraglider.api.bargain.DemandPreview;
import tictim.paraglider.api.bargain.OfferPreview;
import tictim.paraglider.bargain.BargainCatalog;
import tictim.paraglider.client.render.BargainScreenStaminaWheelRenderer;
import tictim.paraglider.client.render.StaminaWheelRenderer;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static tictim.paraglider.ParagliderUtils.ms;
import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;

public class BargainScreen extends Screen implements DisableStaminaRender{
	private static final ResourceLocation MERCHANT_GUI_TEXTURE = new ResourceLocation("textures/gui/container/villager2.png");
	private static final long ITEM_CYCLE_TIME = 1000;
	private static final long DIALOG_FADEOUT_START = 1750;
	private static final long DIALOG_FADEOUT_END = 2000;

	private static final int SCROLL_BOX_THING_WIDTH = 97;
	private static final int SCROLL_BOX_THING_HEIGHT = 142;

	public static int getDialogAlpha(long elapsedTime){
		if(elapsedTime>=DIALOG_FADEOUT_END) return 0;
		if(elapsedTime<=DIALOG_FADEOUT_START) return 0xFF;
		int alpha = Mth.clamp(
				(int)((DIALOG_FADEOUT_END-elapsedTime)*0xFF/(DIALOG_FADEOUT_END-DIALOG_FADEOUT_START)),
				0, 0xFF);
		// for some reason the string render method doesn't give a jack shit on alpha of 4 or below, I have no idea why
		return alpha>4 ? alpha : 0;
	}

	public final int sessionId;

	private final BargainButton[] buttons = new BargainButton[7];

	private Map<ResourceLocation, BargainCatalog> catalog;
	private List<Pair<BargainCatalog, Bargain>> catalogByOrder;

	private int buttonIndexOffset;
	private boolean isDragging;
	private long createdTime;
	private long currentTickTimestamp;

	@Nullable private Vec3 lookAt;

	@Nullable private Component dialog;
	private long dialogTimestamp;
	private boolean dialogUpdated;

	private StaminaWheelRenderer staminaWheelRenderer;

	public BargainScreen(int sessionId,
	                     @NotNull Map<ResourceLocation, BargainCatalog> catalog,
	                     @Nullable Vec3 lookAt,
	                     @Nullable Component dialog){
		super(Component.empty());
		this.sessionId = sessionId;
		setCatalog(catalog);
		setLookAt(lookAt);
		setDialog(dialog);
	}

	public void setCatalog(@NotNull Map<ResourceLocation, BargainCatalog> catalog){
		this.catalog = catalog;
		this.catalogByOrder = catalog.values().stream()
				.sorted(Comparator.comparing(BargainCatalog::bargain))
				.map(key -> Pair.of(key, getBargain(key.bargain())))
				.toList();
	}

	@Nullable private static Bargain getBargain(ResourceLocation id){
		Minecraft mc = Minecraft.getInstance();
		if(mc.level==null) return null;
		RecipeManager recipeManager = mc.level.getRecipeManager();
		var optionalRecipe = recipeManager.byKey(id);
		return optionalRecipe.isPresent()&&optionalRecipe.get() instanceof Bargain b ? b : null;
	}

	public void setLookAt(@Nullable Vec3 lookAt){
		this.lookAt = lookAt;
	}

	public void setDialog(@Nullable Component dialog){
		this.dialog = dialog;
		this.dialogUpdated = dialog!=null;
	}

	@Override protected void init(){
		this.currentTickTimestamp = createdTime = ms();
		this.staminaWheelRenderer = new BargainScreenStaminaWheelRenderer();
		super.init();

		int y = getTop()+1;

		for(int i = 0; i<7; ++i){
			this.buttons[i] = this.addRenderableWidget(new BargainButton(31, y+20*i, i));
		}
	}

	public int getLeft(){
		return 30;
	}
	public int getTop(){
		return (height-SCROLL_BOX_THING_HEIGHT)/2;
	}
	public int getBottom(){
		return getTop()+SCROLL_BOX_THING_HEIGHT;
	}

	private void renderScroller(GuiGraphics guiGraphics, int left, int top){
		int offScreenBargains = this.catalog.size()+1-7;
		int yOffset;
		if(offScreenBargains>1){
			int j = 139-(27+(offScreenBargains-1)*139/offScreenBargains);
			int k = 1+j/offScreenBargains+139/offScreenBargains;
			yOffset = Math.min(113, this.buttonIndexOffset*k);
			if(this.buttonIndexOffset==offScreenBargains-1) yOffset = 113;
		}else yOffset = 0;
		guiGraphics.blit(MERCHANT_GUI_TEXTURE, left+90, top+1+yOffset, 0, 0, 199, 6, 27, 512, 256);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
		this.renderBackground(guiGraphics);

		long newTimestamp = ms();
		if(hasShiftDown())
			this.createdTime += newTimestamp-this.currentTickTimestamp; // For stopping multi item ingredient preview cycling
		this.currentTickTimestamp = newTimestamp;

		renderBg(guiGraphics);

		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		for(BargainButton button : this.buttons)
			button.renderToolTip(guiGraphics, mouseX, mouseY);

		processLookAt(partialTicks);
	}

	protected void renderBg(GuiGraphics guiGraphics){
		RenderSystem.setShaderColor(1, 1, 1, 1);
		guiGraphics.blit(MERCHANT_GUI_TEXTURE, getLeft(), getTop(), 4, 17, SCROLL_BOX_THING_WIDTH, SCROLL_BOX_THING_HEIGHT, 512, 256);

		staminaWheelRenderer.renderStamina(guiGraphics, getLeft()+SCROLL_BOX_THING_WIDTH+5, getTop()-5-WHEEL_RADIUS, 0);

		if(dialog!=null){
			if(dialogUpdated){
				dialogTimestamp = currentTickTimestamp;
				dialogUpdated = false;
			}
			int alpha = getDialogAlpha(currentTickTimestamp-dialogTimestamp);
			if(alpha>0) guiGraphics.drawCenteredString(font, dialog, width/2, getBottom()+9, alpha<<24|0xFFFFFF);
		}

		if(!this.catalog.isEmpty()){
			this.renderScroller(guiGraphics, getLeft(), getTop());

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 100);
			for(BargainButton button : this.buttons)
				button.renderItems(guiGraphics);
			guiGraphics.pose().popPose();

			for(BargainButton button : this.buttons){
				if(button.isHovered()) renderPreview(guiGraphics, button.actualIndex());
				button.visible = button.index<this.catalog.size();
			}

			RenderSystem.enableDepthTest();
		}
	}

	@Override public void renderBackground(GuiGraphics guiGraphics){
		//noinspection ConstantConditions
		if(this.minecraft.level!=null){
			guiGraphics.fillGradient(0, 0, this.width, this.height, 0x70101010, 0xa0101010);
		}else this.renderDirtBackground(guiGraphics);
	}

	@Nullable private Pair<BargainCatalog, Bargain> getBargain(int bargainIndex){
		return bargainIndex<0||bargainIndex>=this.catalogByOrder.size() ? null : this.catalogByOrder.get(bargainIndex);
	}

	private void renderPreview(GuiGraphics guiGraphics, int bargainIndex){
		var pair = getBargain(bargainIndex);
		if(pair==null) return;
		Bargain bargain = pair.getSecond();
		if(bargain==null){
			BargainCatalog catalog = pair.getFirst();
			int left = getLeft()+SCROLL_BOX_THING_WIDTH+20, top = getTop();
			guiGraphics.drawString(font, catalog.bargain().toString(),
					left, top, 0xFF0000);
			return;
		}
		List<DemandPreview> demands = bargain.previewDemands();
		if(demands.isEmpty()) return;

		BargainCatalog catalog = pair.getFirst();
		int mag = demands.size()<=2 ? 8 : demands.size()<=8 ? 4 : 2;
		int textMag = demands.size()<=8 ? 2 : 1;
		int rows = demands.size()<=2 ? 2 : demands.size()<=8 ? 4 : 8;

		int left = getLeft()+SCROLL_BOX_THING_WIDTH+20, top = getTop();

		for(int i = 0; i<demands.size(); i++){
			DemandPreview demand = demands.get(i);

			int xOff = left+i%rows*(16*mag), yOff = top+(i/rows)*(16*mag);
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			modelViewStack.translate(xOff, yOff, 0);
			modelViewStack.scale(mag, mag, 1);
			RenderSystem.applyModelViewMatrix();
			guiGraphics.renderFakeItem(cycle(demand.preview()), 0, 0);
			modelViewStack.popPose();
			RenderSystem.applyModelViewMatrix();

			int count = catalog.getCount(i);
			String s = (count>=demand.quantity() ? count : ChatFormatting.RED+""+count+ChatFormatting.RESET)
					+"/"+demand.quantity();

			PoseStack pose = new PoseStack();
			pose.translate(xOff, yOff, 0);
			pose.translate(15*mag+2*textMag, 16*mag-7*textMag, 200);
			pose.scale(textMag, textMag, 1);
			font.drawInBatch(s,
					-font.width(s),
					0,
					0xFFFFFFFF,
					true,
					pose.last().pose(),
					guiGraphics.bufferSource(),
					Font.DisplayMode.NORMAL,
					0,
					0xf000f0);
			guiGraphics.flush();
		}
	}

	@SuppressWarnings("ConstantConditions")
	private void processLookAt(float partialTicks){
		if(lookAt==null) return;
		Player player = minecraft.player;
		Vec3 eyePosition = player.getEyePosition(partialTicks);

		// stolen from Entity#lookAt
		double lookX = lookAt.x()-eyePosition.x;
		double lookY = lookAt.y()-eyePosition.y;
		double lookZ = lookAt.z()-eyePosition.z;
		double xzLength = Math.sqrt(lookX*lookX+lookZ*lookZ);
		double rotationPitch = Mth.wrapDegrees((float)(-Mth.atan2(lookY, xzLength)*(180/Math.PI)));
		double rotationYaw = Mth.wrapDegrees((float)(Mth.atan2(lookZ, lookX)*(180/Math.PI))-90);

		double lerpPercentage = partialTicks*0.3;
		player.setXRot(lerpAngle(lerpPercentage, Mth.wrapDegrees(player.getXRot()), rotationPitch));
		player.setYRot(lerpAngle(lerpPercentage, Mth.wrapDegrees(player.getYRot()), rotationYaw));
		player.setYHeadRot(player.getYRot());
		player.xRotO = player.getXRot();
		player.yRotO = player.getYRot();
		player.yHeadRotO = player.yHeadRot;
		player.yBodyRotO = player.yBodyRot = player.yHeadRot;
	}

	private static float lerpAngle(double percentage, double start, double end){
		return (float)Mth.lerp(percentage, start<end ? (end-start>180 ? start+360 : start) : (start-end>180 ? start-360 : start), end);
	}

	@Override public boolean mouseScrolled(double mouseX, double mouseY, double delta){
		int bargainSize = this.catalog.size();
		if(bargainSize>7){
			this.buttonIndexOffset = Mth.clamp((int)((double)this.buttonIndexOffset-delta), 0, bargainSize-7);
		}
		return true;
	}

	@Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
		if(!this.isDragging) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		int offScreenBargains = this.catalog.size()-7;
		this.buttonIndexOffset = Mth.clamp((int)((mouseY-getTop()+1-13.5)/(139-27)*offScreenBargains+.5), 0, offScreenBargains);
		return true;
	}

	@Override public boolean mouseClicked(double mouseX, double mouseY, int button){
		this.isDragging = false;
		int left = getLeft(), top = getTop();
		if(this.catalog.size()>7&&mouseX>left+90&&mouseX<left+90+6&&mouseY>top+1&&mouseY<=top+1+139+1)
			this.isDragging = true;

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if(super.keyPressed(keyCode, scanCode, modifiers)||this.minecraft==null) return true;
		InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
		if(ParagliderUtils.isActiveAndMatches(this.minecraft.options.keyInventory, key)){
			this.onClose();
			return true;
		}else return false;
	}

	@Override public boolean isPauseScreen(){
		return false;
	}

	@Override public void onClose(){
		ParagliderNetwork.get().bargainEndToServer(this.sessionId);
		super.onClose();
	}

	private static final int BUTTON_INPUT_X_OFFSET_START = 2;
	private static final int BUTTON_INPUT_X_OFFSET_END = 39-1-16;

	private static final int BUTTON_OUTPUT_X_OFFSET_START = 39+10+1;
	private static final int BUTTON_OUTPUT_X_OFFSET_END = 88-2-16;

	private final class BargainButton extends AbstractButton{
		private final int index;

		public BargainButton(int x, int y, int index){
			super(x, y, 89, 20, Component.empty());
			this.index = index;
			this.visible = false;
		}

		@Override public void onPress(){
			BargainCatalog catalog = catalog();
			if(catalog!=null) ParagliderNetwork.get().bargain(sessionId, catalog.bargain());
		}

		public int actualIndex(){
			return index+buttonIndexOffset;
		}
		@Nullable public BargainCatalog catalog(){
			var pair = BargainScreen.this.getBargain(actualIndex());
			return pair==null ? null : pair.getFirst();
		}
		@Nullable public Bargain bargain(){
			var pair = BargainScreen.this.getBargain(actualIndex());
			return pair==null ? null : pair.getSecond();
		}

		private void renderTradeIndicator(GuiGraphics guiGraphics){
			BargainCatalog catalog = catalog();
			RenderSystem.enableBlend();
			guiGraphics.blit(MERCHANT_GUI_TEXTURE, getX()+39,
					getY()+5,
					0,
					catalog==null||catalog.canBargain() ? 15 : 25,
					171,
					10,
					9,
					512,
					256);
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
			BargainCatalog catalog = catalog();
			this.active = catalog!=null&&catalog.canBargain();
			super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
			this.active = true; // funny trick to keep "disabled" buttons to receive input events
			this.renderTradeIndicator(guiGraphics);
		}

		@Override public void renderString(GuiGraphics guiGraphics, Font font, int color){}

		@Nullable private ItemStack fallbackIcon;

		private ItemStack fallbackIcon(){
			if(this.fallbackIcon!=null) return this.fallbackIcon;
			return this.fallbackIcon = new ItemStack(Items.BARRIER);
		}

		private void renderItems(GuiGraphics guiGraphics){
			if(!this.visible) return;
			Bargain bargain = bargain();
			if(bargain==null) return;

			var demands = bargain.previewDemands();
			for(int i = demands.size()-1; i>=0; i--){
				DemandPreview demand = demands.get(i);
				List<ItemStack> stacks = demand.preview();
				ItemStack stack;
				if(stacks.isEmpty()){
					stack = fallbackIcon();
				}else{
					stack = cycle(stacks);
					if(stack.isEmpty()) stack = fallbackIcon();
				}
				int itemX = getX()+determineItemPosition(i, demands.size(), BUTTON_INPUT_X_OFFSET_START, BUTTON_INPUT_X_OFFSET_END);
				guiGraphics.renderFakeItem(stack, itemX, getY()+2);
				if(demand.quantity()!=1)
					guiGraphics.renderItemDecorations(font, stack, itemX, getY()+2, String.valueOf(demand.quantity()));
			}

			var offers = bargain.previewOffers();
			for(int i = offers.size()-1; i>=0; i--){
				OfferPreview offer = offers.get(i);
				ItemStack stack = offer.preview();
				if(stack.isEmpty()){
					stack = fallbackIcon();
				}
				int itemX = getX()+determineItemPosition(i, offers.size(), BUTTON_OUTPUT_X_OFFSET_START, BUTTON_OUTPUT_X_OFFSET_END);
				guiGraphics.renderFakeItem(stack, itemX, getY()+2);
				if(offer.quantity()!=1)
					guiGraphics.renderItemDecorations(font, stack, itemX, getY()+2, String.valueOf(offer.quantity()));
			}
		}

		private void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY){
			if(!this.isHovered) return;
			Bargain bargain = bargain();
			if(bargain==null) return;

			var demands = bargain.previewDemands();
			DemandPreview closestDemand = null;
			int closestDemandDistance = Integer.MAX_VALUE;
			for(int i = 0; i<demands.size(); i++){
				int itemX = getX()+determineItemPosition(i, demands.size(), BUTTON_INPUT_X_OFFSET_START, BUTTON_INPUT_X_OFFSET_END);
				if(mouseX>=itemX&&mouseX<itemX+16){
					int dist = Math.abs(itemX+8-mouseX);
					if(closestDemandDistance>dist){
						closestDemandDistance = dist;
						closestDemand = demands.get(i);
					}else break;
				}
			}

			if(closestDemand!=null){
				var preview = closestDemand.preview();
				int i = cycleIndex(preview.size());
				guiGraphics.renderComponentTooltip(font,
						i<0||i>=preview.size() ? List.of(Component.literal("No Preview")) : closestDemand.getTooltip(i),
						mouseX, mouseY);
				return;
			}

			var offers = bargain.previewOffers();
			OfferPreview closestOffer = null;
			int closestOfferDistance = Integer.MAX_VALUE;
			for(int i = 0; i<offers.size(); i++){
				int itemX = getX()+determineItemPosition(i, offers.size(), BUTTON_OUTPUT_X_OFFSET_START, BUTTON_OUTPUT_X_OFFSET_END);
				if(mouseX>=itemX&&mouseX<itemX+16){
					int dist = Math.abs(itemX+8-mouseX);
					if(closestOfferDistance>dist){
						closestOfferDistance = dist;
						closestOffer = offers.get(i);
					}else break;
				}
			}

			if(closestOffer!=null){
				guiGraphics.renderComponentTooltip(font, closestOffer.getTooltip(), mouseX, mouseY);
			}
		}

		@Override protected void updateWidgetNarration(NarrationElementOutput o){}
	}

	@NotNull private ItemStack cycle(@NotNull List<ItemStack> stacks){
		int i = cycleIndex(stacks.size());
		return i<0||i>=stacks.size() ? ItemStack.EMPTY : stacks.get(i);
	}

	private int cycleIndex(int counts){
		if(counts<=0) return -1;
		return (int)(Math.abs(currentTickTimestamp-createdTime)/ITEM_CYCLE_TIME%counts);
	}

	private static int determineItemPosition(int n, int length, int start, int end){
		if(n>=length) throw new IllegalArgumentException("length");
		if(end<start) throw new IllegalArgumentException("end < start");

		if(length==1) return (start+end)/2;

		int span = end-start;
		int spanPerElement = Math.min(16, span/(length-1));
		if(spanPerElement==0) return start-(length-1-span)/2+n;

		int leftover = span%spanPerElement;
		return start+leftover/2+spanPerElement*n;
	}
}
