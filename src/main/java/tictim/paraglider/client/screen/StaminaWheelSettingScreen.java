package tictim.paraglider.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.client.DisableStaminaRender;
import tictim.paraglider.client.SettingsWidgetStaminaWheelRenderer;
import tictim.paraglider.event.ParagliderClientEventHandler;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Arrays;

import static tictim.paraglider.client.StaminaWheelConstants.IDLE;
import static tictim.paraglider.client.StaminaWheelConstants.WHEEL_RADIUS;

public class StaminaWheelSettingScreen extends Screen implements DisableStaminaRender{
	private static final DecimalFormat PERCENTAGE = new DecimalFormat("#.#%");

	@Nullable private final ParagliderSettingScreen parent;
	private StaminaWheel staminaWheel;
	private Button saveButton;
	private Button cancelButton;

	private Component[] fuckingText;

	private double initialStaminaWheelX = ModCfg.staminaWheelX();
	private double initialStaminaWheelY = ModCfg.staminaWheelY();

	protected StaminaWheelSettingScreen(@Nullable ParagliderSettingScreen parent){
		super(Component.empty());
		this.parent = parent;
	}

	@Override protected void init(){
		this.staminaWheel = addRenderableWidget(new StaminaWheel(initialStaminaWheelX, initialStaminaWheelY));
		this.saveButton = addRenderableWidget(Button.builder(Component.translatable("adjustStamina.save"), button -> {
					ParagliderMod.LOGGER.debug("Save?");
					ModCfg.setStaminaWheel(staminaWheel.getStaminaWheelX(), staminaWheel.getStaminaWheelY());
					if(parent!=null) parent.saveSettings();
					onClose();
				})
				.bounds(0, 0, 48, 20)
				.build());
		this.cancelButton = addRenderableWidget(Button.builder(Component.translatable("adjustStamina.cancel"), button -> onClose())
				.bounds(0, 0, 48, 20)
				.build());
		//noinspection ConstantConditions
		this.fuckingText = new Component[]{
				Component.translatable("adjustStamina.guide.0"),
				Component.translatable("adjustStamina.guide.1"),
				Component.translatable("adjustStamina.guide.2",
						this.minecraft.options.keyInventory.getTranslatedKeyMessage(),
						ParagliderClientEventHandler.paragliderSettingsKey().getTranslatedKeyMessage())
		};
	}

	@Override public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
		int textWidth = Arrays.stream(fuckingText).mapToInt(e -> font.width(e)).max().orElse(0)+6+48;
		int textHeight = Math.max(fuckingText.length*font.lineHeight, 40+2)+4;
		int textX = staminaWheel.getX()>=this.width/2 ? 0 : this.width-textWidth;
		int textY = staminaWheel.getY()>=this.height/2 ? 0 : this.height-textHeight;

		this.saveButton.setX(textX+textWidth-this.saveButton.getWidth()-2);
		this.saveButton.setY(textY+textHeight-this.saveButton.getHeight()-2); // height realms? what?
		this.cancelButton.setX(textX+textWidth-this.cancelButton.getWidth()-2);
		this.cancelButton.setY(textY+textHeight-this.saveButton.getHeight()-this.cancelButton.getHeight()-4);

		renderBackground(matrixStack);
		fillGradient(matrixStack, textX, textY, textX+textWidth, textY+textHeight, 0x80000000, 0x80000000);
		super.render(matrixStack, mouseX, mouseY, partialTicks);

		int y = textY+2;
		for(Component t : fuckingText){
			font.draw(matrixStack, t, textX+2, y, 0xFF00DF53);
			y += font.lineHeight;
		}
	}

	@Override public void renderBackground(PoseStack pose, int vOffset){
		//noinspection ConstantConditions
		if(this.minecraft.level!=null){
			this.fillGradient(pose, 0, 0, this.width, this.height, 0x10101010, 0x30101010);
			// MinecraftForge.EVENT_BUS.post(new ScreenEvent.BackgroundRendered(this, pose));
		}else this.renderDirtBackground(vOffset);
	}

	@SuppressWarnings("ConstantConditions")
	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if(super.keyPressed(keyCode, scanCode, modifiers)) return true;
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if(this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)||
				ParagliderClientEventHandler.paragliderSettingsKey().getKey().equals(mouseKey)){
			this.onClose();
			return true;
		}else for(int i = 0; i<3; i++){
			if(!this.minecraft.options.keyHotbarSlots[i].matches(keyCode, scanCode)) continue;
			this.staminaWheel.wheel.setWheels(i+1);
			return true;
		}
		return false;
	}

	@Override public void resize(Minecraft minecraft, int width, int height){
		this.initialStaminaWheelX = this.staminaWheel.getStaminaWheelX();
		this.initialStaminaWheelY = this.staminaWheel.getStaminaWheelY();
		super.resize(minecraft, width, height);
	}

	@SuppressWarnings("ConstantConditions")
	@Override public void onClose(){
		this.minecraft.setScreen(parent);
	}

	private int screenWidth(){
		return this.width;
	}
	private int screenHeight(){
		return this.height;
	}

	public class StaminaWheel extends AbstractWidget{
		private final SettingsWidgetStaminaWheelRenderer wheel = new SettingsWidgetStaminaWheelRenderer();

		private boolean dragging;
		private int dragStartX, dragStartY;
		private double dragDeltaX, dragDeltaY;

		public StaminaWheel(double x, double y){
			super((int)Math.round(x*(double)screenWidth())-WHEEL_RADIUS,
					(int)Math.round(y*(double)screenHeight())-WHEEL_RADIUS,
					WHEEL_RADIUS*2, WHEEL_RADIUS*2, Component.empty());
		}

		public double getStaminaWheelX(){
			return (getX()+WHEEL_RADIUS)/(double)screenWidth();
		}
		public double getStaminaWheelY(){
			return (getY()+WHEEL_RADIUS)/(double)screenHeight();
		}

		@Override public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){
			this.setX(Mth.clamp(this.getX(), 1, screenWidth()-2-WHEEL_RADIUS*2));
			this.setY(Mth.clamp(this.getY(), 1, screenHeight()-2-WHEEL_RADIUS*2));
			if(this.visible)
				this.wheel.renderStamina(matrixStack, this.getX()+WHEEL_RADIUS, this.getY()+WHEEL_RADIUS, 0);

			RenderSystem.disableTexture();

			Tesselator t = Tesselator.getInstance();
			BufferBuilder b = t.getBuilder();
			b.begin(VertexFormat.Mode.LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
			b.vertex(this.getX(), this.getY(), 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			b.vertex(this.getX()+this.width, this.getY(), 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			b.vertex(this.getX()+this.width, this.getY()+this.height, 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			b.vertex(this.getX(), this.getY()+this.height, 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			b.vertex(this.getX(), this.getY(), 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			t.end();
			RenderSystem.enableTexture();

			String s = (this.getX())+", "+(this.getY())+
					" ("+PERCENTAGE.format(this.getStaminaWheelX())+" :: "+PERCENTAGE.format(this.getStaminaWheelY())+")";
			int sw = font.width(s);

			int textX = Math.min(this.getX(), screenWidth()-sw-3);
			int textY = this.getY()>=screenHeight()/2 ? this.getY()-1-font.lineHeight : this.getY()+this.height+1;
			font.draw(matrixStack, s, textX, textY, 0xFF00DF53);
		}

		@Override public boolean mouseClicked(double mouseX, double mouseY, int button){
			if(active&&visible&&dragging&&button==1&&this.clicked(mouseX, mouseY)){
				ParagliderMod.LOGGER.debug("Drag Cancelled");
				this.dragging = false;
				this.setX(this.dragStartX);
				this.setY(this.dragStartY);
				return true;
			}else return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override public void onClick(double mouseX, double mouseY){
			ParagliderMod.LOGGER.debug("Drag Started");
			this.dragStartX = this.getX();
			this.dragStartY = this.getY();
			this.dragDeltaX = this.dragDeltaY = 0;
			this.dragging = true;
		}
		@Override protected void onDrag(double mouseX, double mouseY, double dragX, double dragY){
			this.dragDeltaX += dragX;
			this.dragDeltaY += dragY;
			this.setX((int)Math.round(this.dragStartX+this.dragDeltaX+.5));
			this.setY((int)Math.round(this.dragStartY+this.dragDeltaY+.5));
		}

		@Override public void onRelease(double mouseX, double mouseY){
			if(this.dragging){
				ParagliderMod.LOGGER.debug("Drag Ended");
				this.setX((int)Math.round(this.dragStartX+this.dragDeltaX+.5));
				this.setY((int)Math.round(this.dragStartY+this.dragDeltaY+.5));
				this.dragging = false;
			}
		}

		@Override public void playDownSound(SoundManager handler){}
		@Override protected void updateWidgetNarration(NarrationElementOutput o){}
	}
}
