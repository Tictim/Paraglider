package tictim.paraglider.client;

import com.ibm.icu.impl.Pair;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.StringTextComponent;
import tictim.paraglider.ParagliderMod;
import tictim.paraglider.client.dialog.ChoiceButtonListWidget;
import tictim.paraglider.client.dialog.ChoiceButtonWidget;
import tictim.paraglider.client.dialog.NextArrowWidget;
import tictim.paraglider.client.dialog.TextWidget;
import tictim.paraglider.client.dialog.Transition;
import tictim.paraglider.dialog.DialogContainer;
import tictim.paraglider.dialog.Scenario;
import tictim.paraglider.dialog.script.Choice;
import tictim.paraglider.dialog.script.Error;
import tictim.paraglider.dialog.script.GoTo;
import tictim.paraglider.dialog.script.IfAction;
import tictim.paraglider.dialog.script.Script;
import tictim.paraglider.dialog.script.ScriptVisitor;
import tictim.paraglider.dialog.script.Text;
import tictim.paraglider.network.DialogActionRequestMsg;
import tictim.paraglider.network.ModNet;

import javax.annotation.Nullable;
import java.util.Objects;

// TODO Item counter at top right corner
public class DialogScreen extends ContainerScreen<DialogContainer>{
	private static final String ERROR_MESSAGE = "This is error. Please report to Paraglider via GitHub issue page.";

	private final ScriptInterpreter interpreter;

	@Nullable private String error;

	private final TextWidget text = new TextWidget(this);
	private final NextArrowWidget nextArrow = new NextArrowWidget(this);
	private final ChoiceButtonListWidget choiceButtonList = new ChoiceButtonListWidget(this);

	private final boolean hideGui;
	@Nullable private IfAction waitingAction;

	public DialogScreen(DialogContainer screenContainer){
		super(screenContainer, screenContainer.getPlayerInventory(), StringTextComponent.EMPTY);
		this.hideGui = Minecraft.getInstance().gameSettings.hideGUI;
		Minecraft.getInstance().gameSettings.hideGUI = true;
		Scenario scenario = container.getDialog().getScenario();
		interpreter = new ScriptInterpreter(scenario);
		if(scenario==null) return;
		ParagliderMod.LOGGER.debug("Opening DialogScreen with ContainerType "+container.getType().getRegistryName());
		interpreter.run();
	}

	@Override protected void init(){
		xSize = width;
		ySize = height;
		super.init();
	}

	@Override public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks){
		super.render(matrixStack, mouseX, mouseY, partialTicks);

		Vector3f lookAt = container.getLookAt();
		if(lookAt!=null){
			PlayerEntity player = playerInventory.player;
			Vector3d eyePosition = player.getEyePosition(partialTicks);

			// stolen from Entity#lookAt
			double lookX = lookAt.getX()-eyePosition.x;
			double lookY = lookAt.getY()-eyePosition.y;
			double lookZ = lookAt.getZ()-eyePosition.z;
			double xzLength = MathHelper.sqrt(lookX*lookX+lookZ*lookZ);
			double rotationPitch = MathHelper.wrapDegrees((float)(-(MathHelper.atan2(lookY, xzLength)*(double)(180F/(float)Math.PI))));
			double rotationYaw = MathHelper.wrapDegrees((float)(MathHelper.atan2(lookZ, lookX)*(double)(180F/(float)Math.PI))-90.0F);

			double lerpPercentage = partialTicks*0.4;
			player.rotationPitch = lerpAngle(lerpPercentage, MathHelper.wrapDegrees(player.rotationPitch), rotationPitch);
			player.rotationYaw = lerpAngle(lerpPercentage, MathHelper.wrapDegrees(player.rotationYaw), rotationYaw);
			player.setRotationYawHead(player.rotationYaw);
			player.prevRotationPitch = player.rotationPitch;
			player.prevRotationYaw = player.rotationYaw;
			player.prevRotationYawHead = player.rotationYawHead;
			player.renderYawOffset = player.rotationYawHead;
			player.prevRenderYawOffset = player.renderYawOffset;
		}
	}

	private static float lerpAngle(double percentage, double start, double end){
		if(start>end){
			return (float)MathHelper.lerp(percentage, start, start-end>180 ? end+180 : end);
		}else{ // start<end
			return (float)MathHelper.lerp(percentage, start, end-start>180 ? end-180 : end);
		}
	}

	@Override public boolean mouseClicked(double mouseX, double mouseY, int button){
		super.mouseClicked(mouseX, mouseY, button);
		if(error!=null||interpreter.scenario==null) return false;

		if(nextArrow.isNextAvailable()){
			this.nextArrow.boop();
			interpreter.run();
			return true;
		}else if(choiceButtonList.acceptsInput()){
			Pair<Transition, ChoiceButtonWidget> clickButton = choiceButtonList.clickButton(mouseX, mouseY);
			if(clickButton!=null){
				clickButton.first.then(() -> {
					choiceButtonList.clear();
					interpreter.jump(clickButton.second.getCase().getThen());
					interpreter.run();
				});
			}
		}
		return true;
	}

	@Override protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y){

	}

	@Override protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y){
		if(interpreter.scenario==null){
			AbstractGui.drawCenteredString(matrixStack,
					font,
					"Dialog "+container.getDialog().getName()+" has no scenario attached, probably caused by error while loading resource file.",
					xSize/2,
					ySize/2,
					16733525);
			return;
		}
		if(error!=null){
			AbstractGui.drawCenteredString(matrixStack, font, error, xSize/2, ySize/2-font.FONT_HEIGHT, 16733525); // Color of TextFormatting.RED
			AbstractGui.drawCenteredString(matrixStack, font, ERROR_MESSAGE, xSize/2, ySize/2, 16733525);
			AbstractGui.drawCenteredString(matrixStack, font, Objects.toString(container.getType().getRegistryName()), xSize/2, ySize/2+font.FONT_HEIGHT, 16733525);
			return;
		}

		text.render(matrixStack);
		nextArrow.render(matrixStack);
		choiceButtonList.render(matrixStack, x, y);
	}

	@Nullable public String getError(){
		return error;
	}
	public void setError(@Nullable String error){
		this.error = error;
	}

	private void nextDialog(String text, @Nullable Choice choice){
		this.text.fadeOut().then(() -> {
			this.nextArrow.hide();
			this.text.show(text).then(() -> {
				if(choice!=null){
					this.choiceButtonList.createButtons(choice);
				}else{
					this.nextArrow.show();
				}
			});
		});
	}

	private void endDialog(){
		this.text.fadeOut().then(this::closeScreen);
	}

	private void requestActionAndWait(IfAction action){
		this.text.fadeOut().then(() -> {
			this.nextArrow.hide();
			this.waitingAction = action;
			ModNet.NET.sendToServer(new DialogActionRequestMsg(action.getAction()));
		});
	}

	public void processResponse(String id, boolean result){
		if(waitingAction!=null){
			if(!id.equals(waitingAction.getAction())){
				ParagliderMod.LOGGER.warn("Ignoring response {} while waiting for {}", id, waitingAction.getAction());
				return;
			}
			ParagliderMod.LOGGER.warn("Processing response {} for {}", result, id);
			IfAction waitingAction = this.waitingAction;
			this.waitingAction = null;

			if(!result&&waitingAction.doesElseThenExists())
				interpreter.jump(waitingAction.getElseThen());
			interpreter.run();
		}else ParagliderMod.LOGGER.warn("Ignoring response {}", id);
	}

	@Override public void onClose(){
		super.onClose();
		Minecraft.getInstance().gameSettings.hideGUI = hideGui;
	}

	private static final int CONNECTED_JUMPS_THRESHOLD = 15;

	public final class ScriptInterpreter implements ScriptVisitor{
		@Nullable public final Scenario scenario;

		public Script currentScript;
		public int ip;

		private boolean jumped;

		/**
		 * Counter for connected GOTOs
		 */
		private int connectedJumps;

		public ScriptInterpreter(@Nullable Scenario scenario){
			this.scenario = scenario;
		}

		public void run(){
			if(scenario==null) return;
			if(scenario.size()<=ip||ip<0){
				setError("Instruction Pointer out of range ("+ip+")");
				return;
			}
			do{
				int ip = this.ip++;
				jumped = false;
				currentScript = scenario.getScript(ip);
				ParagliderMod.LOGGER.debug("Visiting "+ip+": "+currentScript);
				currentScript.visit(this);
			}while(jumped);
		}

		public void jump(int ip){
			if(++connectedJumps>=CONNECTED_JUMPS_THRESHOLD){
				setError("Possible infinite loop detected");
				return;
			}
			this.ip = ip;
			jumped = true;
		}

		@Override public void visitText(Text text){
			connectedJumps = 0;
			nextDialog(text.getText().getString(), null);
		}
		@Override public void visitChoice(Choice choice){
			connectedJumps = 0;
			nextDialog(choice.getText().getString(), choice);
		}
		@Override public void visitGoTo(GoTo goTo){
			jump(goTo.getPos());
		}
		@Override public void visitIfAction(IfAction ifAction){
			requestActionAndWait(ifAction);
		}
		@Override public void visitEnd(){
			endDialog();
		}
		@Override public void visitError(Error error){
			setError(error.getMessage());
		}
	}
}
