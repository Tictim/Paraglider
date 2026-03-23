package tictim.paraglider.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.NullMarked;
import tictim.paraglider.ParagliderUtils;
import tictim.paraglider.api.bargain.BargainPreview;
import tictim.paraglider.bargain.BargainCatalog;
import tictim.paraglider.client.render.BargainScreenStaminaWheelRenderer;
import tictim.paraglider.client.settings.ExtraWheelAttachment;
import tictim.paraglider.network.ParagliderNetwork;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static tictim.paraglider.ParagliderUtils.ms;
import static tictim.paraglider.client.render.StaminaWheelConstants.WHEEL_RADIUS;

@NullMarked
public class BargainScreen extends Screen implements DisableStaminaRender {
	private static final Identifier MERCHANT_GUI_TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/villager.png");

	private static final Identifier SCROLLER_SPRITE = Identifier.withDefaultNamespace("container/villager/scroller");
	private static final Identifier SCROLLER_DISABLED_SPRITE = Identifier.withDefaultNamespace("container/villager/scroller_disabled");
	private static final Identifier TRADE_ARROW_OUT_OF_STOCK_SPRITE = Identifier.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
	private static final Identifier TRADE_ARROW_SPRITE = Identifier.withDefaultNamespace("container/villager/trade_arrow");

	private static final long ITEM_CYCLE_TIME = 1000;
	private static final long DIALOG_FADEOUT_START = 1750;
	private static final long DIALOG_FADEOUT_END = 2000;

	private static final int SCROLL_BOX_THING_WIDTH = 97;
	private static final int SCROLL_BOX_THING_HEIGHT = 142;

	public static int getDialogAlpha(long elapsedTime) {
		if (elapsedTime >= DIALOG_FADEOUT_END) return 0;
		if (elapsedTime <= DIALOG_FADEOUT_START) return 0xFF;
		int alpha = Mth.clamp(
				(int)((DIALOG_FADEOUT_END - elapsedTime) * 0xFF / (DIALOG_FADEOUT_END - DIALOG_FADEOUT_START)),
				0, 0xFF);
		// for some reason the string render method doesn't give a jack shit on alpha of 4 or below, I have no idea why
		return alpha > 4 ? alpha : 0;
	}

	public final int sessionId;

	private final BargainButton[] buttons = new BargainButton[7];
	private final BargainScreenStaminaWheelRenderer staminaWheelRenderer = new BargainScreenStaminaWheelRenderer();

	private BargainCatalog[] catalogs = {};
	@SuppressWarnings("unchecked")
	private @Nullable List<List<ItemStack>>[] catalogDemandPreviews = new List[0];
	@SuppressWarnings("unchecked")
	private @Nullable List<List<ItemStack>>[] catalogOfferPreviews = new List[0];

	private int buttonIndexOffset;
	private boolean isDragging;
	private long createdTime;
	private long currentTickTimestamp;

	private @Nullable Vec3 lookAt;

	private @Nullable Component dialog;
	private long dialogTimestamp;
	private boolean dialogUpdated;

	private @Nullable ContextMap contextMap;

	public BargainScreen(int sessionId,
	                     List<BargainCatalog> catalog,
	                     @Nullable Vec3 lookAt,
	                     @Nullable Component dialog) {
		super(Component.empty());
		this.sessionId = sessionId;
		setCatalog(catalog);
		setLookAt(lookAt);
		setDialog(dialog);
	}

	@SuppressWarnings("unchecked")
	public void setCatalog(List<BargainCatalog> catalog) {
		this.catalogs = catalog.toArray(new BargainCatalog[0]);
		this.catalogDemandPreviews = new List[catalog.size()];
		this.catalogOfferPreviews = new List[catalog.size()];

		Arrays.sort(this.catalogs, Comparator.comparing(BargainCatalog::bargain));
	}

	public void setLookAt(@Nullable Vec3 lookAt) {
		this.lookAt = lookAt;
	}

	public void setDialog(@Nullable Component dialog) {
		this.dialog = dialog;
		this.dialogUpdated = dialog != null;
	}

	@Override protected void init() {
		this.currentTickTimestamp = createdTime = ms();
		super.init();

		int y = getTop() + 1;

		for (int i = 0; i < 7; ++i) {
			this.buttons[i] = this.addRenderableWidget(new BargainButton(this, 31, y + 20 * i, i));
		}
	}

	public int getLeft() {
		return 30;
	}
	public int getTop() {
		return (height - SCROLL_BOX_THING_HEIGHT) / 2;
	}
	public int getBottom() {
		return getTop() + SCROLL_BOX_THING_HEIGHT;
	}

	private void extractScroller(GuiGraphicsExtractor graphics, int left, int top) {
		int offScreenBargains = this.catalogs.length + 1 - 7;
		if (offScreenBargains > 1) {
			int j = 139 - (27 + (offScreenBargains - 1) * 139 / offScreenBargains);
			int k = 1 + j / offScreenBargains + 139 / offScreenBargains;
			int yOffset = Math.min(113, this.buttonIndexOffset * k);
			if (this.buttonIndexOffset == offScreenBargains - 1) yOffset = 113;

			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, left + 90, top + 1 + yOffset, 6, 27);
		} else {
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_DISABLED_SPRITE, left + 90, top + 1, 6, 27);
		}
	}

	@Override public void tick() {
		this.staminaWheelRenderer.tick();
	}


	@Override public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		long newTimestamp = ms();
		if (minecraft.hasShiftDown())
			this.createdTime += newTimestamp - this.currentTickTimestamp; // For stopping multi item ingredient preview cycling
		this.currentTickTimestamp = newTimestamp;

		super.extractRenderState(graphics, mouseX, mouseY, a);

		if (ParagliderUtils.renderStaminaWheel(Objects.requireNonNull(this.minecraft).player)) {
			this.staminaWheelRenderer.staminaWheel(graphics,
					getLeft() + SCROLL_BOX_THING_WIDTH + 5,
					getTop() - 5 - WHEEL_RADIUS,
					a,
					ExtraWheelAttachment.LEFT);
		}

		if (this.catalogs.length > 0) {
			extractScroller(graphics, getLeft(), getTop());

			for (BargainButton button : this.buttons)
				button.items(graphics);

			for (BargainButton button : this.buttons) {
				if (button.isHovered()) renderPreview(graphics, button.actualIndex());
				button.visible = button.index < this.catalogs.length;
			}
		}

		for (BargainButton button : this.buttons)
			button.tooltip(graphics, mouseX, mouseY);

		processLookAt(a);
	}

	// disable blur
	@Override protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {}

	@Override protected void extractMenuBackground(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
		// no background :)

		graphics.blit(RenderPipelines.GUI_TEXTURED, MERCHANT_GUI_TEXTURE, getLeft(), getTop(),
				4, 17,
				SCROLL_BOX_THING_WIDTH, SCROLL_BOX_THING_HEIGHT,
				512, 256);

		if (this.dialog != null) {
			if (this.dialogUpdated) {
				this.dialogTimestamp = currentTickTimestamp;
				this.dialogUpdated = false;
			}

			int alpha = getDialogAlpha(this.currentTickTimestamp - this.dialogTimestamp);
			if (alpha > 0)
				graphics.centeredText(this.font, this.dialog,
						this.width / 2, getBottom() + 9,
						alpha << 24 | 0xFFFFFF);
		}
	}

	private @Nullable BargainCatalog getBargainCatalog(int bargainIndex) {
		return bargainIndex < 0 || bargainIndex >= this.catalogs.length ? null : this.catalogs[bargainIndex];
	}

	@SuppressWarnings("DataFlowIssue")
	private ContextMap contextMap() {
		if (this.contextMap == null) {
			this.contextMap = SlotDisplayContext.fromLevel(this.minecraft.level);
		}
		return this.contextMap;
	}

	private List<List<ItemStack>> demandPreviewItem(int bargainIndex) {
		if (bargainIndex < 0 || bargainIndex >= this.catalogs.length) return List.of();
		BargainCatalog catalog = getBargainCatalog(bargainIndex);
		if (catalog == null) return List.of();

		var ret = this.catalogDemandPreviews[bargainIndex];
		if (ret == null) {
			this.catalogDemandPreviews[bargainIndex] = ret = catalog.demands().stream()
					.map(p -> p.display().resolveForStacks(contextMap()))
					.toList();
		}
		return ret;
	}

	private List<List<ItemStack>> offerPreviewItem(int bargainIndex) {
		if (bargainIndex < 0 || bargainIndex >= this.catalogs.length) return List.of();
		BargainCatalog catalog = getBargainCatalog(bargainIndex);
		if (catalog == null) return List.of();

		var ret = this.catalogOfferPreviews[bargainIndex];
		if (ret == null) {
			this.catalogOfferPreviews[bargainIndex] = ret = catalog.offers().stream()
					.map(p -> p.display().resolveForStacks(contextMap()))
					.toList();
		}
		return ret;
	}

	private void renderPreview(GuiGraphicsExtractor graphics, int bargainIndex) {
		var catalog = getBargainCatalog(bargainIndex);
		if (catalog == null) return;

		List<BargainPreview<?>> demands = catalog.demands();
		if (demands.isEmpty()) return;

		List<List<ItemStack>> demandPreviewItem = demandPreviewItem(bargainIndex);
		int mag = demands.size() <= 2 ? 8 : demands.size() <= 8 ? 4 : 2;
		int textMag = demands.size() <= 8 ? 2 : 1;
		int rows = demands.size() <= 2 ? 2 : demands.size() <= 8 ? 4 : 8;

		int left = getLeft() + SCROLL_BOX_THING_WIDTH + 20, top = getTop();

		for (int i = 0; i < demands.size(); i++) {
			BargainPreview<?> demand = demands.get(i);

			int xOff = left + i % rows * (16 * mag);
			int yOff = top + (i / rows) * (16 * mag);

			Matrix3x2fStack pose = graphics.pose();
			pose.pushMatrix();
			pose.translate(xOff, yOff, pose);

			pose.pushMatrix();
			pose.scale(mag, mag, pose);

			graphics.fakeItem(cycle(demandPreviewItem.get(i)), 0, 0);

			pose.popMatrix();

			int count = catalog.getCount(i);
			String s = (count >= demand.quantity() ? count :
					ChatFormatting.RED + "" + count + ChatFormatting.RESET)
					+ "/" + demand.quantity();

			pose.translate(15 * mag + 2 * textMag, 16 * mag - 7 * textMag, pose);
			pose.scale(textMag, textMag, pose);

			graphics.text(this.font,
					s,
					-this.font.width(s),
					0,
					0xFFFFFFFF,
					true
			);

			pose.popMatrix();
		}
	}

	private void processLookAt(float partialTicks) {
		if (lookAt == null) return;
		Player player = minecraft.player;
		if (player == null) return;
		Vec3 eyePosition = player.getEyePosition(partialTicks);

		// stolen from Entity#lookAt
		double lookX = lookAt.x() - eyePosition.x;
		double lookY = lookAt.y() - eyePosition.y;
		double lookZ = lookAt.z() - eyePosition.z;
		double xzLength = Math.sqrt(lookX * lookX + lookZ * lookZ);
		double rotationPitch = Mth.wrapDegrees((float)(-Mth.atan2(lookY, xzLength) * (180 / Math.PI)));
		double rotationYaw = Mth.wrapDegrees((float)(Mth.atan2(lookZ, lookX) * (180 / Math.PI)) - 90);

		double lerpPercentage = partialTicks * 0.3;
		player.setXRot(lerpAngle(lerpPercentage, Mth.wrapDegrees(player.getXRot()), rotationPitch));
		player.setYRot(lerpAngle(lerpPercentage, Mth.wrapDegrees(player.getYRot()), rotationYaw));
		player.setYHeadRot(player.getYRot());
		player.xRotO = player.getXRot();
		player.yRotO = player.getYRot();
		player.yHeadRotO = player.yHeadRot;
		player.yBodyRotO = player.yBodyRot = player.yHeadRot;
	}

	private static float lerpAngle(double percentage, double start, double end) {
		return (float)Mth.lerp(percentage, start < end ? (end - start > 180 ? start + 360 : start) : (start - end > 180 ? start - 360 : start), end);
	}

	@Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
			int bargainSize = this.catalogs.length;
			if (bargainSize > 7) {
				this.buttonIndexOffset = Mth.clamp((int)((double)this.buttonIndexOffset - scrollY), 0, bargainSize - 7);
			}
		}
		return true;
	}

	@Override public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		if (!this.isDragging) return super.mouseDragged(event, dragX, dragY);
		int offScreenBargains = this.catalogs.length - 7;
		this.buttonIndexOffset = Mth.clamp((int)((event.y() - getTop() + 1 - 13.5) / (139 - 27) * offScreenBargains + .5), 0, offScreenBargains);
		return true;
	}

	@Override public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
		this.isDragging = false;
		int left = getLeft(), top = getTop();
		if (this.catalogs.length > 7 && event.x() > left + 90 && event.x() < left + 90 + 6 && event.y() > top + 1 && event.y() <= top + 1 + 139 + 1)
			this.isDragging = true;

		return super.mouseClicked(event, isDoubleClick);
	}

	@Override public boolean keyPressed(KeyEvent event) {
		if (super.keyPressed(event)) return true;
		InputConstants.Key key = InputConstants.getKey(event);
		if (this.minecraft.options.keyInventory.isActiveAndMatches(key)) {
			this.onClose();
			return true;
		} else return false;
	}

	@Override public boolean isPauseScreen() {
		return false;
	}

	@Override public void onClose() {
		ParagliderNetwork.get().bargainEndToServer(this.sessionId);
		super.onClose();
	}

	private static final int BUTTON_INPUT_X_OFFSET_START = 2;
	private static final int BUTTON_INPUT_X_OFFSET_END = 39 - 1 - 16;

	private static final int BUTTON_OUTPUT_X_OFFSET_START = 39 + 10 + 1;
	private static final int BUTTON_OUTPUT_X_OFFSET_END = 88 - 2 - 16;

	private static final class BargainButton extends AbstractButton {
		private final BargainScreen screen;
		private final int index;

		public BargainButton(BargainScreen screen, int x, int y, int index) {
			super(x, y, 89, 20, Component.empty());
			this.screen = screen;
			this.index = index;
			this.visible = false;
		}

		@Override public void onPress(InputWithModifiers input) {
			BargainCatalog catalog = catalog();
			if (catalog != null) ParagliderNetwork.get().bargain(this.screen.sessionId, catalog.bargain());
		}

		public int actualIndex() {
			return this.index + this.screen.buttonIndexOffset;
		}
		public @Nullable BargainCatalog catalog() {
			return this.screen.getBargainCatalog(actualIndex());
		}

		@Override protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
			extractDefaultSprite(graphics);
			tradeArrow(graphics);
		}

		private void tradeArrow(GuiGraphicsExtractor graphics) {
			BargainCatalog catalog = catalog();
			graphics.blitSprite(RenderPipelines.GUI_TEXTURED,
					catalog == null || catalog.canBargain() ? TRADE_ARROW_SPRITE : TRADE_ARROW_OUT_OF_STOCK_SPRITE,
					getX() + 39, getY() + 5,
					10, 9);
		}

		@Override public int getFGColor() {
			BargainCatalog catalog = catalog();
			return catalog != null && catalog.canBargain() ? 0xffffff : 0xa0a0a0;
		}

		private @Nullable ItemStack fallbackIcon;

		private ItemStack fallbackIcon() {
			if (this.fallbackIcon != null) return this.fallbackIcon;
			return this.fallbackIcon = new ItemStack(Items.BARRIER);
		}

		private void items(GuiGraphicsExtractor graphics) {
			if (!this.visible) return;
			BargainCatalog catalog = catalog();
			if (catalog == null) return;

			var demands = catalog.demands();
			var demandItems = this.screen.demandPreviewItem(actualIndex());
			for (int i = demands.size() - 1; i >= 0; i--) {
				previewItem(graphics,
						demandItems.get(i),
						demands.get(i).quantity(),
						getX() + determineItemPosition(i, demands.size(), BUTTON_INPUT_X_OFFSET_START, BUTTON_INPUT_X_OFFSET_END),
						getY() + 2);
			}

			var offers = catalog.offers();
			var offerItems = this.screen.offerPreviewItem(actualIndex());
			for (int i = offers.size() - 1; i >= 0; i--) {
				previewItem(graphics,
						offerItems.get(i),
						offers.get(i).quantity(),
						getX() + determineItemPosition(i, offers.size(), BUTTON_OUTPUT_X_OFFSET_START, BUTTON_OUTPUT_X_OFFSET_END),
						getY() + 2);
			}
		}

		private void previewItem(GuiGraphicsExtractor graphics, List<ItemStack> stacks, int quantity, int x, int y) {
			ItemStack stack;
			if (stacks.isEmpty()) {
				stack = fallbackIcon();
			} else {
				stack = this.screen.cycle(stacks);
				if (stack.isEmpty()) stack = fallbackIcon();
			}
			graphics.fakeItem(stack, x, y);
			if (quantity != 1) {
				graphics.itemDecorations(this.screen.font, stack, x, y, String.valueOf(quantity));
			}
		}

		private void tooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
			if (!this.isHovered) return;
			BargainCatalog catalog = catalog();
			if (catalog == null) return;

			if (tooltipInternal(graphics, mouseX, mouseY, catalog, true)) return;
			tooltipInternal(graphics, mouseX, mouseY, catalog, false);
		}

		private boolean tooltipInternal(GuiGraphicsExtractor graphics, int mouseX, int mouseY, BargainCatalog catalog, boolean demand) {
			List<BargainPreview<?>> list = demand ? catalog.demands() : catalog.offers();
			BargainPreview<?> closest = null;
			int closestIndex = -1;
			int closestDist = Integer.MAX_VALUE;
			for (int i = 0; i < list.size(); i++) {
				int itemX = getX() + determineItemPosition(i, list.size(),
						demand ? BUTTON_INPUT_X_OFFSET_START : BUTTON_OUTPUT_X_OFFSET_START,
						demand ? BUTTON_INPUT_X_OFFSET_END : BUTTON_OUTPUT_X_OFFSET_END);
				if (mouseX >= itemX && mouseX < itemX + 16) {
					int dist = Math.abs(itemX + 8 - mouseX);
					if (closestDist > dist) {
						closestDist = dist;
						closest = list.get(i);
						closestIndex = i;
					} else break;
				}
			}

			if (closest == null) return false;

			List<Component> tooltip = closest.getTooltip();

			if (tooltip == null) {
				List<List<ItemStack>> previewItems = demand ?
						this.screen.demandPreviewItem(actualIndex()) :
						this.screen.offerPreviewItem(actualIndex());

				if (closestIndex < previewItems.size()) {
					List<ItemStack> items = previewItems.get(closestIndex);
					int i = this.screen.cycleIndex(items.size());
					if (i < 0 || i >= items.size()) {
						tooltip = List.of(Component.literal("No Preview"));
					} else {
						tooltip = Screen.getTooltipFromItem(Minecraft.getInstance(), items.get(i));
					}
				}
			}

			if (tooltip != null) {
				graphics.setComponentTooltipForNextFrame(this.screen.font, tooltip, mouseX, mouseY);
			}

			return true;
		}

		@Override protected void updateWidgetNarration(NarrationElementOutput o) {}
	}

	private ItemStack cycle(List<ItemStack> stacks) {
		int i = cycleIndex(stacks.size());
		return i < 0 || i >= stacks.size() ? ItemStack.EMPTY : stacks.get(i);
	}

	private int cycleIndex(int counts) {
		if (counts <= 0) return -1;
		return (int)(Math.abs(this.currentTickTimestamp - this.createdTime) / ITEM_CYCLE_TIME % counts);
	}

	private static int determineItemPosition(int n, int length, int start, int end) {
		if (n >= length) throw new IllegalArgumentException("length");
		if (end < start) throw new IllegalArgumentException("end < start");

		if (length == 1) return (start + end) / 2;

		int span = end - start;
		int spanPerElement = Math.min(16, span / (length - 1));
		if (spanPerElement == 0) return start - (length - 1 - span) / 2 + n;

		int leftover = span % spanPerElement;
		return start + leftover / 2 + spanPerElement * n;
	}
}
