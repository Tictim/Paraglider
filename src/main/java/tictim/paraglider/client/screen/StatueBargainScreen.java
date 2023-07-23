package tictim.paraglider.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.client.BargainScreenStaminaWheelRenderer;
import tictim.paraglider.client.DisableStaminaRender;
import tictim.paraglider.client.StaminaWheelRenderer;
import tictim.paraglider.contents.recipe.bargain.BargainPreview;
import tictim.paraglider.contents.recipe.bargain.StatueBargain;
import tictim.paraglider.contents.recipe.bargain.StatueBargainContainer;
import tictim.paraglider.contents.recipe.bargain.StatueBargainContainer.ItemDemand;
import tictim.paraglider.network.BargainMsg;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.utils.TooltipFactory;

import javax.annotation.Nullable;
import java.util.List;

import static tictim.paraglider.client.StaminaWheelConstants.WHEEL_RADIUS;

public class StatueBargainScreen extends AbstractContainerScreen<StatueBargainContainer> implements DisableStaminaRender{
	private static final ResourceLocation MERCHANT_GUI_TEXTURE = new ResourceLocation("textures/gui/container/villager2.png");
	private static final long ITEM_CYCLE_TIME = 1000;
	private static final long DIALOG_FADEOUT_START = 1750;
	private static final long DIALOG_FADEOUT_END = 2000;

	private static final int SCROLL_BOX_THING_WIDTH = 97;
	private static final int SCROLL_BOX_THING_HEIGHT = 142;

	private final BargainButton[] buttons = new BargainButton[7];

	private int buttonIndexOffset;
	private boolean isDragging;
	private long createdTime;
	private long currentTickTimestamp;

	@Nullable private Component dialog;
	private long dialogTimestamp;
	private boolean dialogUpdated;

	private StaminaWheelRenderer staminaWheelRenderer;

	public StatueBargainScreen(StatueBargainContainer screenContainer, Inventory inv, Component titleIn){
		super(screenContainer, inv, titleIn);
	}

	@Override protected void init(){
		imageWidth = width;
		imageHeight = height;
		currentTickTimestamp = createdTime = System.currentTimeMillis();
		//noinspection ConstantConditions
		PlayerMovement m = PlayerMovement.of(minecraft.player);
		staminaWheelRenderer = new BargainScreenStaminaWheelRenderer(m==null ? 0 : m.getMaxStamina());
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

	public void setDialog(@Nullable Component dialog){
		this.dialog = dialog;
		this.dialogUpdated = dialog!=null;
	}

	private void renderScroller(GuiGraphics graphics, int left, int top){
		int offScreenBargains = menu.getBargains().size()+1-7;
		int yOffset;
		if(offScreenBargains>1){
			int j = 139-(27+(offScreenBargains-1)*139/offScreenBargains);
			int k = 1+j/offScreenBargains+139/offScreenBargains;
			yOffset = Math.min(113, this.buttonIndexOffset*k);
			if(this.buttonIndexOffset==offScreenBargains-1) yOffset = 113;
		}else yOffset = 0;
		graphics.blit(MERCHANT_GUI_TEXTURE, left+90, top+1+yOffset,0,0, 199, 6, 27, 512, 256);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(graphics);

		long newTimestamp = System.currentTimeMillis();
		if(hasShiftDown())
			this.createdTime += newTimestamp-this.currentTickTimestamp; // For stopping multi item ingredient preview cycling
		this.currentTickTimestamp = newTimestamp;

		super.render(graphics, mouseX, mouseY, partialTicks);

		this.renderTooltip(graphics, mouseX, mouseY);
		lookAtStatue(partialTicks);
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int x, int y) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		graphics.blit(MERCHANT_GUI_TEXTURE, getLeft(), getTop(), 4, 17, SCROLL_BOX_THING_WIDTH, SCROLL_BOX_THING_HEIGHT, 512, 256);

		staminaWheelRenderer.renderStamina(graphics, getLeft()+SCROLL_BOX_THING_WIDTH+5, getTop()-5-WHEEL_RADIUS, 0);

		if(dialog!=null){
			if(dialogUpdated){
				dialogTimestamp = currentTickTimestamp;
				dialogUpdated = false;
			}
			long t = currentTickTimestamp-dialogTimestamp;
			int alpha;
			if(t>=DIALOG_FADEOUT_END) alpha = 0;
			else if(t<=DIALOG_FADEOUT_START) alpha = 0xFF;
			else
				alpha = Mth.clamp((int)((DIALOG_FADEOUT_END-t)*0xFF/(DIALOG_FADEOUT_END-DIALOG_FADEOUT_START)), 0, 0xFF);

			if(alpha>4){ // Don't fucking question me, question FontRenderer#fixAlpha() instead
				graphics.drawCenteredString(font, dialog, width/2, getBottom()+9, alpha<<24|0xFFFFFF);
			}
		}

		List<StatueBargain> bargains = menu.getBargains();
		if(!bargains.isEmpty()){
			this.renderScroller(graphics, getLeft(), getTop());

			graphics.pose().pushPose();
			graphics.pose().translate(0, 0, 100);
			for(BargainButton button : this.buttons)
				button.renderItems(graphics);
			graphics.pose().popPose();

			for(BargainButton button : this.buttons){
				if(button.isHoveredOrFocused()) renderPreview(graphics, button.getActualIndex());
				button.visible = button.index<this.menu.getBargains().size();
			}

			RenderSystem.enableDepthTest();
		}
	}

	@Override protected void renderTooltip(GuiGraphics graphics, int x, int y){
		for(BargainButton button : this.buttons)
			button.renderToolTip(graphics, x, y);
	}

	@Override protected void renderLabels(GuiGraphics graphics, int x, int y){}

	@Override public void renderBackground(GuiGraphics graphics){
		//noinspection ConstantConditions
		if(this.minecraft.level!=null){
			graphics.fillGradient(0, 0, this.width, this.height, 0x70101010, 0xa0101010);
		}else this.renderDirtBackground(graphics);
	}

	private void renderPreview(final GuiGraphics graphics, int bargainIndex){
		ItemDemand[] demands = menu.getDemandPreview(bargainIndex);
		if(demands.length==0) return;

		final int mag = demands.length<=2 ? 8 : demands.length<=8 ? 4 : 2;
		final int textMag = demands.length<=8 ? 2 : 1;
		final int rows = demands.length<=2 ? 2 : demands.length<=8 ? 4 : 8;

		int left = getLeft()+SCROLL_BOX_THING_WIDTH+20, top = getTop();

		for(int i = 0; i<demands.length; i++){
			ItemDemand demand = demands[i];

			int xOff = left+i%rows*(16*mag), yOff = top+(i/rows)*(16*mag);
			PoseStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.pushPose();
			modelViewStack.translate(xOff, yOff, 0);
			modelViewStack.scale(mag, mag, 1);
			RenderSystem.applyModelViewMatrix();
			graphics.renderFakeItem(cycle(demand.getPreviewItems()), 0, 0);
			modelViewStack.popPose();
			RenderSystem.applyModelViewMatrix();

			String s = (demand.getCount()>=demand.getQuantity() ? demand.getCount() : ChatFormatting.RED+""+demand.getCount()+ChatFormatting.RESET)+"/"+demand.getQuantity();

			PoseStack pose = new PoseStack();
			pose.translate(xOff, yOff, 0);
			pose.translate(15*mag+2*textMag, 16*mag-7*textMag, 200);
			pose.scale(textMag, textMag, 1);
			font.drawInBatch(s, -font.width(s), 0, 0xFFFFFFFF, true, pose.last().pose(), graphics.bufferSource(), Font.DisplayMode.NORMAL, 0, 15728880);
			graphics.flush();
		}
	}

	@SuppressWarnings("ConstantConditions")
	private void lookAtStatue(float partialTicks){
		Vec3 lookAt = menu.getLookAt();
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
		int bargainSize = this.menu.getBargains().size();
		if(bargainSize>7){
			this.buttonIndexOffset = Mth.clamp((int)((double)this.buttonIndexOffset-delta), 0, bargainSize-7);
		}
		return true;
	}

	@Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
		if(!this.isDragging) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		int offScreenBargains = this.menu.getBargains().size()-7;
		this.buttonIndexOffset = Mth.clamp((int)((mouseY-getTop()+1-13.5)/(139-27)*offScreenBargains+.5), 0, offScreenBargains);
		return true;
	}

	@Override public boolean mouseClicked(double mouseX, double mouseY, int button){
		this.isDragging = false;
		int left = getLeft(), top = getTop();
		if(this.menu.getBargains().size()>7&&mouseX>left+90&&mouseX<left+90+6&&mouseY>top+1&&mouseY<=top+1+139+1)
			this.isDragging = true;

		return super.mouseClicked(mouseX, mouseY, button);
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
			StatueBargain bargain = getBargain();
			if(bargain!=null) ModNet.NET.sendToServer(new BargainMsg(bargain.getId()));
		}

		public int getIndex(){
			return index;
		}
		public int getActualIndex(){
			return index+buttonIndexOffset;
		}
		@Nullable public StatueBargain getBargain(){
			return menu.getBargain(getActualIndex());
		}

		private int getYImage(boolean isHovered){
			return !menu.canBargain(getActualIndex()) ? 0 : isHovered ? 2 : 1;
		}

		private void renderTradeIndicator(GuiGraphics graphics){
			RenderSystem.enableBlend();
			graphics.blit(MERCHANT_GUI_TEXTURE, getX()+39,
					getY()+5,
					0,
					menu.canBargain(getActualIndex()) ? 15 : 25,
					171,
					10,
					9,
					512,
					256);
		}

		@Override
		protected void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
			Minecraft minecraft = Minecraft.getInstance();
			pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			pGuiGraphics.blitNineSliced(WIDGETS_LOCATION, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 20, 4, 200, 20, 0, 46 + this.getYImage(this.isHoveredOrFocused()) * 20);
			this.renderTradeIndicator(pGuiGraphics);
			pGuiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
			int i = getFGColor();
			this.renderString(pGuiGraphics, minecraft.font, i | Mth.ceil(this.alpha * 255.0F) << 24);
		}

		public void renderItems(final GuiGraphics graphics){
			if(!this.visible) return;
			StatueBargain bargain = getBargain();
			if(bargain==null) return;

			BargainPreview preview = bargain.getPreview();
			int buttonElementTop = getY()+2;

			List<BargainPreview.Demand> demands = preview.demands();
			for(int i = demands.size()-1; i>=0; i--){
				BargainPreview.Demand demand = demands.get(i);
				ItemStack[] stacks = demand.getPreviewItems();
				if(stacks.length==0){
					ParagliderMod.LOGGER.warn("Some of ItemDemandPreview has empty array of items.");
					continue;
				}
				ItemStack stack = cycle(stacks);
				if(stack.isEmpty()){
					ParagliderMod.LOGGER.warn("Some of ItemDemandPreview has empty items.");
					continue;
				}
				int itemX = getX()+determinePosition(i, demands.size(), BUTTON_INPUT_X_OFFSET_START, BUTTON_INPUT_X_OFFSET_END);
				graphics.renderFakeItem(stack, itemX, buttonElementTop);
				if(demand.getQuantity()!=1)
					graphics.renderItemDecorations(font, stack, itemX, buttonElementTop, String.valueOf(demand.getQuantity()));
			}

			List<BargainPreview.Offer> offers = preview.offers();
			for(int i = offers.size()-1; i>=0; i--){
				BargainPreview.Offer offer = offers.get(i);
				ItemStack stack = offer.getPreview();
				if(stack.isEmpty()){
					ParagliderMod.LOGGER.warn("Some of ItemOfferPreview has empty item.");
					continue;
				}
				int itemX = getX()+determinePosition(i, offers.size(), BUTTON_OUTPUT_X_OFFSET_START, BUTTON_OUTPUT_X_OFFSET_END);
				graphics.renderFakeItem(stack, itemX, buttonElementTop);
				if(offer.getQuantity()!=1)
					graphics.renderItemDecorations(font, stack, itemX, buttonElementTop, String.valueOf(offer.getQuantity()));
			}
		}

		public void renderToolTip(final GuiGraphics graphics, int mouseX, int mouseY){
			if(!this.isHovered) return;
			StatueBargain bargain = getBargain();
			if(bargain==null) return;

			BargainPreview preview = bargain.getPreview();

			List<BargainPreview.Demand> demands = preview.demands();
			BargainPreview.Demand closestDemand = null;
			int closestDemandDistance = Integer.MAX_VALUE;
			for(int i = 0; i<demands.size(); i++){
				int itemX = getX()+determinePosition(i, demands.size(), BUTTON_INPUT_X_OFFSET_START, BUTTON_INPUT_X_OFFSET_END);
				if(mouseX>=itemX&&mouseX<itemX+16){
					int dist = Math.abs(itemX+8-mouseX);
					if(closestDemandDistance>dist){
						closestDemandDistance = dist;
						closestDemand = demands.get(i);
					}else break;
				}
			}

			if(closestDemand!=null){
				TooltipFactory tf = closestDemand.getTooltipFactory();
				if(tf!=null) graphics.renderComponentTooltip(font, tf.getTooltip(), mouseX, mouseY);
				else graphics.renderTooltip(font, cycle(closestDemand.getPreviewItems()), mouseX, mouseY);
				return;
			}

			List<BargainPreview.Offer> offers = preview.offers();
			BargainPreview.Offer closestOffer = null;
			int closestOfferDistance = Integer.MAX_VALUE;
			for(int i = 0; i<offers.size(); i++){
				int itemX = getX()+determinePosition(i, offers.size(), BUTTON_OUTPUT_X_OFFSET_START, BUTTON_OUTPUT_X_OFFSET_END);
				if(mouseX>=itemX&&mouseX<itemX+16){
					int dist = Math.abs(itemX+8-mouseX);
					if(closestOfferDistance>dist){
						closestOfferDistance = dist;
						closestOffer = offers.get(i);
					}else break;
				}
			}

			if(closestOffer!=null){
				TooltipFactory tf = closestOffer.getTooltipFactory();
				if(tf!=null) graphics.renderComponentTooltip(font, tf.getTooltip(), mouseX, mouseY);
				else graphics.renderTooltip(font, closestOffer.getPreview(), mouseX, mouseY);
			}
		}

		@Override protected void updateWidgetNarration(NarrationElementOutput o){}
	}

	private ItemStack cycle(ItemStack[] stacks){
		if(stacks.length==0) return ItemStack.EMPTY;
		return stacks[(int)(Math.abs(currentTickTimestamp-createdTime)/ITEM_CYCLE_TIME%stacks.length)];
	}

	private static int determinePosition(int n, int length, int start, int end){
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
