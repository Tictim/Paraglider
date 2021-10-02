package tictim.paraglider.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent;
import net.minecraftforge.fml.client.gui.GuiUtils;
import org.lwjgl.opengl.GL11;
import tictim.paraglider.ModCfg;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.client.DisableStaminaRender;
import tictim.paraglider.client.SettingsWidgetStaminaWheelRenderer;
import tictim.paraglider.event.ParagliderClientEventHandler;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.Arrays;

import static net.minecraftforge.common.MinecraftForge.EVENT_BUS;
import static tictim.paraglider.client.StaminaWheelConstants.IDLE;
import static tictim.paraglider.client.StaminaWheelConstants.WHEEL_RADIUS;

public class StaminaWheelSettingScreen extends Screen implements DisableStaminaRender{
	private static final DecimalFormat PERCENTAGE = new DecimalFormat("#.#%");

	@Nullable private final ParagliderSettingScreen parent;
	private StaminaWheel staminaWheel;
	private Button saveButton;
	private Button cancelButton;

	private TextComponent[] fuckingText;

	private double initialStaminaWheelX = ModCfg.staminaWheelX();
	private double initialStaminaWheelY = ModCfg.staminaWheelY();

	protected StaminaWheelSettingScreen(@Nullable ParagliderSettingScreen parent){
		super(StringTextComponent.EMPTY);
		this.parent = parent;
	}

	@Override protected void init(){
		this.staminaWheel = addButton(new StaminaWheel(initialStaminaWheelX, initialStaminaWheelY));
		this.saveButton = addButton(new Button(0, 0, 48, 20, new TranslationTextComponent("adjustStamina.save"), button -> {
			ParagliderMod.LOGGER.debug("Save?");
			ModCfg.setStaminaWheel(staminaWheel.getStaminaWheelX(), staminaWheel.getStaminaWheelY());
			if(parent!=null) parent.saveSettings();
			closeScreen();
		}));
		this.cancelButton = addButton(new Button(0, 0, 48, 20, new TranslationTextComponent("adjustStamina.cancel"), button -> closeScreen()));
		//noinspection ConstantConditions
		this.fuckingText = new TextComponent[]{
				new TranslationTextComponent("adjustStamina.guide.0"),
				new TranslationTextComponent("adjustStamina.guide.1"),
				new TranslationTextComponent("adjustStamina.guide.2",
						this.minecraft.gameSettings.keyBindInventory.func_238171_j_(),
						ParagliderClientEventHandler.paragliderSettingsKey().func_238171_j_())
		};
	}

	@Override public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
		int textWidth = Arrays.stream(fuckingText).mapToInt(e -> font.getStringPropertyWidth(e)).max().orElse(0)+6+48;
		int textHeight = Math.max(fuckingText.length*font.FONT_HEIGHT, 40+2)+4;
		int textX = staminaWheel.x>=this.width/2 ? 0 : this.width-textWidth;
		int textY = staminaWheel.y>=this.height/2 ? 0 : this.height-textHeight;

		this.saveButton.x = textX+textWidth-this.saveButton.getWidth()-2;
		this.saveButton.y = textY+textHeight-this.saveButton.getHeightRealms()-2; // height realms? what?
		this.cancelButton.x = textX+textWidth-this.cancelButton.getWidth()-2;
		this.cancelButton.y = textY+textHeight-this.saveButton.getHeightRealms()-this.cancelButton.getHeightRealms()-4;

		renderBackground(matrixStack);
		GuiUtils.drawGradientRect(matrixStack.getLast().getMatrix(), 0, textX, textY, textX+textWidth, textY+textHeight, 0x80000000, 0x80000000);
		super.render(matrixStack, mouseX, mouseY, partialTicks);

		int y = textY+2;
		for(TextComponent t : fuckingText){
			font.func_243248_b(matrixStack, t, textX+2, y, 0xFF00DF53);
			y += font.FONT_HEIGHT;
		}
	}

	@Override public void renderBackground(MatrixStack matrixStack, int vOffset){
		//noinspection ConstantConditions
		if(this.minecraft.world!=null){
			this.fillGradient(matrixStack, 0, 0, this.width, this.height, 0x10101010, 0x30101010);
			EVENT_BUS.post(new BackgroundDrawnEvent(this, matrixStack));
		}else this.renderDirtBackground(vOffset);
	}

	@SuppressWarnings("ConstantConditions")
	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers){
		if(super.keyPressed(keyCode, scanCode, modifiers)) return true;
		InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
		if(this.minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey)||
				ParagliderClientEventHandler.paragliderSettingsKey().getKey().equals(mouseKey)){
			this.closeScreen();
			return true;
		}else for(int i = 0; i<3; i++){
			if(!this.minecraft.gameSettings.keyBindsHotbar[i].matchesKey(keyCode, scanCode)) continue;
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
	@Override public void closeScreen(){
		this.minecraft.displayGuiScreen(parent);
	}

	private int screenWidth(){
		return this.width;
	}
	private int screenHeight(){
		return this.height;
	}

	public class StaminaWheel extends Widget{
		private final SettingsWidgetStaminaWheelRenderer wheel = new SettingsWidgetStaminaWheelRenderer();

		private boolean dragging;
		private int dragStartX, dragStartY;
		private double dragDeltaX, dragDeltaY;

		public StaminaWheel(double x, double y){
			super((int)Math.round(x*(double)screenWidth())-WHEEL_RADIUS,
					(int)Math.round(y*(double)screenHeight())-WHEEL_RADIUS,
					WHEEL_RADIUS*2, WHEEL_RADIUS*2, StringTextComponent.EMPTY);
		}

		public double getStaminaWheelX(){
			return (x+WHEEL_RADIUS)/(double)screenWidth();
		}
		public double getStaminaWheelY(){
			return (y+WHEEL_RADIUS)/(double)screenHeight();
		}

		@Override public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
			this.x = MathHelper.clamp(this.x, 1, screenWidth()-2-WHEEL_RADIUS*2);
			this.y = MathHelper.clamp(this.y, 1, screenHeight()-2-WHEEL_RADIUS*2);
			if(this.visible)
				this.wheel.renderStamina(matrixStack, this.x+WHEEL_RADIUS, this.y+WHEEL_RADIUS, 0);

			RenderSystem.disableTexture();

			Tessellator t = Tessellator.getInstance();
			BufferBuilder b = t.getBuffer();
			b.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
			b.pos(this.x, this.y, 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			b.pos(this.x+this.width, this.y, 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			b.pos(this.x+this.width, this.y+this.height, 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			b.pos(this.x, this.y+this.height, 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			b.pos(this.x, this.y, 0).color(IDLE.red, IDLE.green, IDLE.blue, 1).endVertex();
			t.draw();
			RenderSystem.enableTexture();

			String s = (this.x)+", "+(this.y)+
					" ("+PERCENTAGE.format(this.getStaminaWheelX())+" :: "+PERCENTAGE.format(this.getStaminaWheelY())+")";
			int sw = font.getStringWidth(s);

			int textX = Math.min(this.x, screenWidth()-sw-3);
			int textY = this.y>=screenHeight()/2 ? this.y-1-font.FONT_HEIGHT : this.y+this.height+1;
			font.drawString(matrixStack, s, textX, textY, 0xFF00DF53);
		}

		@Override public boolean mouseClicked(double mouseX, double mouseY, int button){
			if(active&&visible&&dragging&&button==1&&this.clicked(mouseX, mouseY)){
				ParagliderMod.LOGGER.debug("Drag Cancelled");
				this.dragging = false;
				this.x = this.dragStartX;
				this.y = this.dragStartY;
				return true;
			}else return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override public void onClick(double mouseX, double mouseY){
			ParagliderMod.LOGGER.debug("Drag Started");
			this.dragStartX = this.x;
			this.dragStartY = this.y;
			this.dragDeltaX = this.dragDeltaY = 0;
			this.dragging = true;
		}
		@Override protected void onDrag(double mouseX, double mouseY, double dragX, double dragY){
			this.dragDeltaX += dragX;
			this.dragDeltaY += dragY;
			this.x = (int)Math.round(this.dragStartX+this.dragDeltaX+.5);
			this.y = (int)Math.round(this.dragStartY+this.dragDeltaY+.5);
		}

		@Override public void onRelease(double mouseX, double mouseY){
			if(this.dragging){
				ParagliderMod.LOGGER.debug("Drag Ended");
				this.x = (int)Math.round(this.dragStartX+this.dragDeltaX+.5);
				this.y = (int)Math.round(this.dragStartY+this.dragDeltaY+.5);
				this.dragging = false;
			}
		}

		@Override public void playDownSound(SoundHandler handler){}
	}
}
