package tictim.paraglider.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.capabilities.PlayerMovement;
import tictim.paraglider.network.BargainMsg;
import tictim.paraglider.network.ModNet;
import tictim.paraglider.recipe.bargain.BargainPreview;
import tictim.paraglider.recipe.bargain.StatueBargain;
import tictim.paraglider.recipe.bargain.StatueBargainContainer;
import tictim.paraglider.recipe.bargain.StatueBargainContainer.ItemDemand;
import tictim.paraglider.utils.TooltipFactory;

import javax.annotation.Nullable;
import java.util.List;

import static tictim.paraglider.client.StaminaWheelConstants.WHEEL_SIZE;

public class StatueBargainScreen extends ContainerScreen<StatueBargainContainer>{
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

	@Nullable private ITextComponent dialog;
	private long dialogTimestamp;
	private boolean dialogUpdated;

	private StaminaWheelRenderer staminaWheelRenderer;

	public StatueBargainScreen(StatueBargainContainer screenContainer, PlayerInventory inv, ITextComponent titleIn){
		super(screenContainer, inv, titleIn);
	}

	@Override protected void init(){
		xSize = width;
		ySize = height;
		currentTickTimestamp = createdTime = System.currentTimeMillis();
		//noinspection ConstantConditions
		PlayerMovement m = PlayerMovement.of(minecraft.player);
		staminaWheelRenderer = new BargainScreenStaminaWheelRenderer(m==null ? 0 : m.getMaxStamina());
		super.init();

		int y = getTop()+1;

		for(int i = 0; i<7; ++i){
			this.buttons[i] = this.addButton(new BargainButton(31, y+20*i, i, b -> {
				if(!(b instanceof BargainButton)) return;
				BargainButton button = (BargainButton)b;
				StatueBargain bargain = button.getBargain();
				if(bargain!=null) ModNet.NET.sendToServer(new BargainMsg(bargain.getId()));
			}));
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

	public void setDialog(@Nullable ITextComponent dialog){
		this.dialog = dialog;
		this.dialogUpdated = dialog!=null;
	}

	private void renderScroller(MatrixStack matrixStack, int left, int top){
		int offScreenBargains = container.getBargains().size()+1-7;
		int yOffset;
		if(offScreenBargains>1){
			int j = 139-(27+(offScreenBargains-1)*139/offScreenBargains);
			int k = 1+j/offScreenBargains+139/offScreenBargains;
			yOffset = Math.min(113, this.buttonIndexOffset*k);
			if(this.buttonIndexOffset==offScreenBargains-1) yOffset = 113;
		}else yOffset = 0;
		blit(matrixStack, left+90, top+1+yOffset, this.getBlitOffset(), 0, 199, 6, 27, 256, 512);
	}

	@Override public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
		this.renderBackground(matrixStack);

		long newTimestamp = System.currentTimeMillis();
		if(hasShiftDown()) this.createdTime += newTimestamp-this.currentTickTimestamp; // For stopping multi item ingredient preview cycling
		this.currentTickTimestamp = newTimestamp;

		super.render(matrixStack, mouseX, mouseY, partialTicks);

		List<StatueBargain> bargains = container.getBargains();
		if(!bargains.isEmpty()){
			//noinspection deprecation
			RenderSystem.pushMatrix();
			//noinspection deprecation
			RenderSystem.enableRescaleNormal();
			//noinspection ConstantConditions
			this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
			this.renderScroller(matrixStack, getLeft(), getTop());

			itemRenderer.zLevel = 100;
			for(BargainButton button : this.buttons)
				button.renderItems(matrixStack);
			itemRenderer.zLevel = 0;

			for(BargainButton button : this.buttons){
				if(button.isHovered()) renderPreview(matrixStack, button.getActualIndex());

				button.renderToolTip(matrixStack, mouseX, mouseY);
				button.visible = button.index<this.container.getBargains().size();
			}

			//noinspection deprecation
			RenderSystem.popMatrix();
			RenderSystem.enableDepthTest();
		}

		if(dialog!=null){
			if(dialogUpdated){
				dialogTimestamp = currentTickTimestamp;
				dialogUpdated = false;
			}
			long t = currentTickTimestamp-dialogTimestamp;
			int alpha;
			if(t>=DIALOG_FADEOUT_END) alpha = 0;
			else if(t<=DIALOG_FADEOUT_START) alpha = 0xFF;
			else alpha = MathHelper.clamp((int)((DIALOG_FADEOUT_END-t)*0xFF/(DIALOG_FADEOUT_END-DIALOG_FADEOUT_START)), 0, 0xFF);

			if(alpha>4){ // Don't fucking question me, question FontRenderer#fixAlpha() instead
				drawCenteredString(matrixStack, font, dialog, width/2, getBottom()+9, alpha<<24|0xFFFFFF);
			}
		}

		staminaWheelRenderer.renderStamina(matrixStack, getLeft()+SCROLL_BOX_THING_WIDTH+5, getTop()-5-WHEEL_SIZE, 0);

		this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
		lookAtStatue(partialTicks);
	}

	private void renderPreview(MatrixStack matrixStack, int bargainIndex){
		ItemDemand[] demands = container.getDemandPreview(bargainIndex);
		if(demands.length==0) return;

		final int mag = demands.length<=2 ? 8 : demands.length<=8 ? 4 : 2;
		final int textMag = demands.length<=8 ? 2 : 1;
		final int rows = demands.length<=2 ? 2 : demands.length<=8 ? 4 : 8;

		matrixStack.push();
		matrixStack.translate(0, 0, 200);

		int left = getLeft()+SCROLL_BOX_THING_WIDTH+20, top = getTop();

		for(int i = 0; i<demands.length; i++){
			ItemDemand demand = demands[i];
			ItemStack stack = cycle(demand.getPreviewItems());

			//noinspection deprecation
			RenderSystem.pushMatrix();

			//noinspection deprecation,IntegerDivisionInFloatingPointContext
			RenderSystem.translated(left+i%rows*(16*mag), top+(i/rows)*(16*mag), 0);

			//noinspection deprecation
			RenderSystem.pushMatrix();
			//noinspection deprecation
			RenderSystem.scaled(mag, mag, 1);
			itemRenderer.renderItemAndEffectIntoGuiWithoutEntity(stack, 0, 0);
			itemRenderer.renderItemOverlayIntoGUI(font, stack, 0, 0, "");
			//noinspection deprecation
			RenderSystem.popMatrix();

			//noinspection deprecation
			RenderSystem.pushMatrix();
			//noinspection deprecation
			RenderSystem.translated(15*mag+2*textMag, 16*mag-7*textMag, 0);
			//noinspection deprecation
			RenderSystem.scaled(textMag, textMag, 1);
			String s = (demand.getCount()>=demand.getQuantity() ? demand.getCount() : TextFormatting.RED+""+demand.getCount()+TextFormatting.RESET)+"/"+demand.getQuantity();
			drawString(matrixStack, font, s, -font.getStringWidth(s), 0, 0xFFFFFFFF);
			//noinspection deprecation
			RenderSystem.popMatrix();

			//noinspection deprecation
			RenderSystem.popMatrix();
		}
		matrixStack.pop();
	}

	private void lookAtStatue(float partialTicks){
		Vector3d lookAt = container.getLookAt();
		if(lookAt==null) return;
		PlayerEntity player = playerInventory.player;
		Vector3d eyePosition = player.getEyePosition(partialTicks);

		// stolen from Entity#lookAt
		double lookX = lookAt.getX()-eyePosition.x;
		double lookY = lookAt.getY()-eyePosition.y;
		double lookZ = lookAt.getZ()-eyePosition.z;
		double xzLength = MathHelper.sqrt(lookX*lookX+lookZ*lookZ);
		double rotationPitch = MathHelper.wrapDegrees((float)(-MathHelper.atan2(lookY, xzLength)*(180/Math.PI)));
		double rotationYaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(lookZ, lookX)*(180/Math.PI))-90);

		double lerpPercentage = partialTicks*0.3;
		player.rotationPitch = lerpAngle(lerpPercentage, MathHelper.wrapDegrees(player.rotationPitch), rotationPitch);
		player.rotationYaw = lerpAngle(lerpPercentage, MathHelper.wrapDegrees(player.rotationYaw), rotationYaw);
		player.setRotationYawHead(player.rotationYaw);
		player.prevRotationPitch = player.rotationPitch;
		player.prevRotationYaw = player.rotationYaw;
		player.prevRotationYawHead = player.rotationYawHead;
		player.prevRenderYawOffset = player.renderYawOffset = player.rotationYawHead;

		/*drawCenteredString(new MatrixStack(),
				font,
				String.format("Look At:[%s %s %s], Pitch: %s, Yaw: %s",
						lookAt.getX(),
						lookAt.getY(),
						lookAt.getZ(),
						rotationPitch,
						rotationYaw),
				width/2,
				getBottom()+18,
				-1);*/
	}

	private static float lerpAngle(double percentage, double start, double end){
		return (float)MathHelper.lerp(percentage, start<end ? (end-start>180 ? start+360 : start) : (start-end>180 ? start-360 : start), end);
	}

	@Override protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y){
		//noinspection deprecation
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		//noinspection ConstantConditions
		this.minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
		blit(matrixStack, getLeft(), getTop(), this.getBlitOffset(), 4, 17, SCROLL_BOX_THING_WIDTH, SCROLL_BOX_THING_HEIGHT, 256, 512);
	}

	@Override protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y){}

	@Override public boolean mouseScrolled(double mouseX, double mouseY, double delta){
		int bargainSize = this.container.getBargains().size();
		if(bargainSize>7){
			this.buttonIndexOffset = MathHelper.clamp((int)((double)this.buttonIndexOffset-delta), 0, bargainSize-7);
		}
		return true;
	}

	@Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
		if(!this.isDragging) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		int offScreenBargains = this.container.getBargains().size()-7;
		this.buttonIndexOffset = MathHelper.clamp((int)((mouseY-getTop()+1-13.5)/(139-27)*offScreenBargains+.5), 0, offScreenBargains);
		return true;
	}

	@Override public boolean mouseClicked(double mouseX, double mouseY, int button){
		this.isDragging = false;
		int left = getLeft(), top = getTop();
		if(this.container.getBargains().size()>7&&mouseX>left+90&&mouseX<left+90+6&&mouseY>top+1&&mouseY<=top+1+139+1)
			this.isDragging = true;

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override public void renderBackground(MatrixStack matrixStack, int vOffset){
		//noinspection ConstantConditions
		if(this.minecraft.world!=null){
			this.fillGradient(matrixStack, 0, 0, this.width, this.height, 0x70101010, 0xa0101010);
			MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent(this, matrixStack));
		}else this.renderDirtBackground(vOffset);
	}

	private static final int BUTTON_INPUT_X_OFFSET_START = 2;
	private static final int BUTTON_INPUT_X_OFFSET_END = 39-1-16;

	private static final int BUTTON_OUTPUT_X_OFFSET_START = 39+10+1;
	private static final int BUTTON_OUTPUT_X_OFFSET_END = 88-2-16;

	private final class BargainButton extends Button{
		private final int index;

		public BargainButton(int x, int y, int index, Button.IPressable pressedAction){
			super(x, y, 89, 20, StringTextComponent.EMPTY, pressedAction);
			this.index = index;
			this.visible = false;
		}

		public int getIndex(){
			return index;
		}
		public int getActualIndex(){
			return index+buttonIndexOffset;
		}
		@Nullable public StatueBargain getBargain(){
			return container.getBargain(getActualIndex());
		}

		@Override protected int getYImage(boolean isHovered){
			return !container.canBargain(getActualIndex()) ? 0 : isHovered ? 2 : 1;
		}

		@Override public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
			super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
		}

		public void renderItems(MatrixStack matrixStack){
			if(!this.visible) return;
			StatueBargain bargain = getBargain();
			if(bargain==null) return;

			BargainPreview preview = bargain.getPreview();
			int buttonElementTop = y+2;

			List<BargainPreview.Demand> demands = preview.getDemands();
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
				int itemX = x+determinePosition(i, demands.size(), BUTTON_INPUT_X_OFFSET_START, BUTTON_INPUT_X_OFFSET_END);
				itemRenderer.renderItemAndEffectIntoGuiWithoutEntity(stack, itemX, buttonElementTop);
				if(demand.getQuantity()!=1)
					itemRenderer.renderItemOverlayIntoGUI(font, stack, itemX, buttonElementTop, String.valueOf(demand.getQuantity()));
			}

			renderButtonArrows(matrixStack);

			List<BargainPreview.Offer> offers = preview.getOffers();
			for(int i = offers.size()-1; i>=0; i--){
				BargainPreview.Offer offer = offers.get(i);
				ItemStack stack = offer.getPreview();
				if(stack.isEmpty()){
					ParagliderMod.LOGGER.warn("Some of ItemOfferPreview has empty item.");
					continue;
				}
				int itemX = x+determinePosition(i, offers.size(), BUTTON_OUTPUT_X_OFFSET_START, BUTTON_OUTPUT_X_OFFSET_END);
				itemRenderer.renderItemAndEffectIntoGuiWithoutEntity(stack, itemX, buttonElementTop);
				if(offer.getQuantity()!=1)
					itemRenderer.renderItemOverlayIntoGUI(font, stack, itemX, buttonElementTop, String.valueOf(offer.getQuantity()));
			}
		}

		private void renderButtonArrows(MatrixStack matrixStack){
			RenderSystem.enableBlend();
			//noinspection ConstantConditions
			minecraft.getTextureManager().bindTexture(MERCHANT_GUI_TEXTURE);
			blit(matrixStack,
					x+39,
					y+5,
					this.getBlitOffset(),
					container.canBargain(getActualIndex()) ? 15 : 25,
					171,
					10,
					9,
					256,
					512);
		}

		@Override public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY){
			if(!this.isHovered) return;
			StatueBargain bargain = getBargain();
			if(bargain==null) return;

			BargainPreview preview = bargain.getPreview();

			List<BargainPreview.Demand> demands = preview.getDemands();
			BargainPreview.Demand closestDemand = null;
			int closestDemandDistance = Integer.MAX_VALUE;
			for(int i = 0; i<demands.size(); i++){
				int itemX = x+determinePosition(i, demands.size(), BUTTON_INPUT_X_OFFSET_START, BUTTON_INPUT_X_OFFSET_END);
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
				if(tf!=null) func_243308_b(matrixStack, tf.getTooltip(), mouseX, mouseY);
				else renderTooltip(matrixStack, cycle(closestDemand.getPreviewItems()), mouseX, mouseY);
				return;
			}

			List<BargainPreview.Offer> offers = preview.getOffers();
			BargainPreview.Offer closestOffer = null;
			int closestOfferDistance = Integer.MAX_VALUE;
			for(int i = 0; i<offers.size(); i++){
				int itemX = x+determinePosition(i, offers.size(), BUTTON_OUTPUT_X_OFFSET_START, BUTTON_OUTPUT_X_OFFSET_END);
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
				if(tf!=null) func_243308_b(matrixStack, tf.getTooltip(), mouseX, mouseY);
				else renderTooltip(matrixStack, closestOffer.getPreview(), mouseX, mouseY);
			}
		}
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
