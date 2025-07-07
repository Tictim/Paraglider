package tictim.paraglider.client.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
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
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public class BargainScreen extends Screen implements DisableStaminaRender {
	private static final ResourceLocation MERCHANT_GUI_TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/villager.png");

	private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller");
	private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller_disabled");
	private static final ResourceLocation TRADE_ARROW_OUT_OF_STOCK_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/trade_arrow_out_of_stock");
	private static final ResourceLocation TRADE_ARROW_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/trade_arrow");

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

	private @NotNull BargainCatalog[] catalogs = {};
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

	private BargainScreenStaminaWheelRenderer staminaWheelRenderer;

	public BargainScreen(int sessionId,
	                     @NotNull List<@NotNull BargainCatalog> catalog,
	                     @Nullable Vec3 lookAt,
	                     @Nullable Component dialog) {
		super(Component.empty());
		this.sessionId = sessionId;
		setCatalog(catalog);
		setLookAt(lookAt);
		setDialog(dialog);
	}

	@SuppressWarnings("unchecked")
	public void setCatalog(@NotNull List<@NotNull BargainCatalog> catalog) {
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
		this.staminaWheelRenderer = new BargainScreenStaminaWheelRenderer();
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

	private void renderScroller(GuiGraphics guiGraphics, int left, int top) {
		int offScreenBargains = this.catalogs.length + 1 - 7;
		if (offScreenBargains > 1) {
			int j = 139 - (27 + (offScreenBargains - 1) * 139 / offScreenBargains);
			int k = 1 + j / offScreenBargains + 139 / offScreenBargains;
			int yOffset = Math.min(113, this.buttonIndexOffset * k);
			if (this.buttonIndexOffset == offScreenBargains - 1) yOffset = 113;

			guiGraphics.blitSprite(SCROLLER_SPRITE, left + 90, top + 1 + yOffset, 6, 27);
		} else {
			guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, left + 90, top + 1, 6, 27);
		}
	}

	@Override public void tick() {
		this.staminaWheelRenderer.tick();
	}

	@Override public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		long newTimestamp = ms();
		if (hasShiftDown())
			this.createdTime += newTimestamp - this.currentTickTimestamp; // For stopping multi item ingredient preview cycling
		this.currentTickTimestamp = newTimestamp;

		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		if (this.catalogs.length > 0) {
			renderScroller(guiGraphics, getLeft(), getTop());

			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 100);
			for (BargainButton button : this.buttons)
				button.renderItems(guiGraphics);
			guiGraphics.pose().popPose();

			for (BargainButton button : this.buttons) {
				if (button.isHovered()) renderPreview(guiGraphics, button.actualIndex());
				button.visible = button.index < this.catalogs.length;
			}
		}

		for (BargainButton button : this.buttons)
			button.renderToolTip(guiGraphics, mouseX, mouseY);

		processLookAt(partialTicks);
	}

	@Override public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		// no background :)

		guiGraphics.blit(MERCHANT_GUI_TEXTURE, getLeft(), getTop(),
				4, 17,
				SCROLL_BOX_THING_WIDTH, SCROLL_BOX_THING_HEIGHT,
				512, 256);

		if (ParagliderUtils.renderStaminaWheel(Objects.requireNonNull(this.minecraft).player)) {
			this.staminaWheelRenderer.render(guiGraphics,
					getLeft() + SCROLL_BOX_THING_WIDTH + 5,
					getTop() - 5 - WHEEL_RADIUS,
					0,
					partialTick,
					ExtraWheelAttachment.LEFT);
		}

		if (this.dialog != null) {
			if (this.dialogUpdated) {
				this.dialogTimestamp = currentTickTimestamp;
				this.dialogUpdated = false;
			}

			int alpha = getDialogAlpha(this.currentTickTimestamp - this.dialogTimestamp);
			if (alpha > 0)
				guiGraphics.drawCenteredString(this.font, this.dialog,
						this.width / 2, getBottom() + 9,
						alpha << 24 | 0xFFFFFF);
		}
	}

	private @Nullable BargainCatalog getBargainCatalog(int bargainIndex) {
		return bargainIndex < 0 || bargainIndex >= this.catalogs.length ? null : this.catalogs[bargainIndex];
	}

	private @NotNull List<List<ItemStack>> demandPreviewItem(int bargainIndex) {
		if (bargainIndex < 0 || bargainIndex >= this.catalogs.length) return List.of();
		BargainCatalog catalog = getBargainCatalog(bargainIndex);
		if (catalog == null) return List.of();

		var ret = this.catalogDemandPreviews[bargainIndex];
		if (ret == null) {
			this.catalogDemandPreviews[bargainIndex] = ret = catalog.demands().stream()
					.map(BargainPreview::display)
					.toList();
		}
		return ret;
	}

	private @NotNull List<List<ItemStack>> offerPreviewItem(int bargainIndex) {
		if (bargainIndex < 0 || bargainIndex >= this.catalogs.length) return List.of();
		BargainCatalog catalog = getBargainCatalog(bargainIndex);
		if (catalog == null) return List.of();

		var ret = this.catalogOfferPreviews[bargainIndex];
		if (ret == null) {
			this.catalogOfferPreviews[bargainIndex] = ret = catalog.offers().stream()
					.map(BargainPreview::display)
					.toList();
		}
		return ret;
	}

	private void renderPreview(GuiGraphics guiGraphics, int bargainIndex) {
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

			PoseStack pose = guiGraphics.pose();
			pose.pushPose();
			pose.translate(xOff, yOff, 0);

			pose.pushPose();
			pose.scale(mag, mag, 1);

			guiGraphics.renderFakeItem(cycle(demandPreviewItem.get(i)), 0, 0);

			pose.popPose();

			int count = catalog.getCount(i);
			String s = (count >= demand.quantity() ? count :
					ChatFormatting.RED + "" + count + ChatFormatting.RESET)
					+ "/" + demand.quantity();

			pose.translate(15 * mag + 2 * textMag, 16 * mag - 7 * textMag, 200);
			pose.scale(textMag, textMag, 1);

			this.font.drawInBatch(s,
					-this.font.width(s),
					0,
					0xFFFFFFFF,
					true,
					pose.last().pose(),
					guiGraphics.bufferSource(),
					Font.DisplayMode.NORMAL,
					0,
					0xf000f0);

			pose.popPose();
		}
	}

	@SuppressWarnings("ConstantConditions")
	private void processLookAt(float partialTicks) {
		if (lookAt == null) return;
		Player player = minecraft.player;
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

	@Override public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!this.isDragging) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		int offScreenBargains = this.catalogs.length - 7;
		this.buttonIndexOffset = Mth.clamp((int)((mouseY - getTop() + 1 - 13.5) / (139 - 27) * offScreenBargains + .5), 0, offScreenBargains);
		return true;
	}

	@Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.isDragging = false;
		int left = getLeft(), top = getTop();
		if (this.catalogs.length > 7 && mouseX > left + 90 && mouseX < left + 90 + 6 && mouseY > top + 1 && mouseY <= top + 1 + 139 + 1)
			this.isDragging = true;

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers) || this.minecraft == null) return true;
		InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);
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

		@Override public void onPress() {
			BargainCatalog catalog = catalog();
			if (catalog != null) ParagliderNetwork.get().bargain(this.screen.sessionId, catalog.bargain());
		}

		public int actualIndex() {
			return this.index + this.screen.buttonIndexOffset;
		}
		public @Nullable BargainCatalog catalog() {
			return this.screen.getBargainCatalog(actualIndex());
		}

		@Override protected void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
			renderTradeArrow(guiGraphics);
		}

		private void renderTradeArrow(GuiGraphics guiGraphics) {
			BargainCatalog catalog = catalog();
			guiGraphics.blitSprite(
					catalog == null || catalog.canBargain() ? TRADE_ARROW_SPRITE : TRADE_ARROW_OUT_OF_STOCK_SPRITE,
					getX() + 39, getY() + 5,
					10, 9);
		}

		@Override public int getFGColor() {
			BargainCatalog catalog = catalog();
			return catalog != null && catalog.canBargain() ? 0xffffff : 0xa0a0a0;
		}

		@Override public void renderString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int color) {}

		private @Nullable ItemStack fallbackIcon;

		private ItemStack fallbackIcon() {
			if (this.fallbackIcon != null) return this.fallbackIcon;
			return this.fallbackIcon = new ItemStack(Items.BARRIER);
		}

		private void renderItems(GuiGraphics guiGraphics) {
			if (!this.visible) return;
			BargainCatalog catalog = catalog();
			if (catalog == null) return;

			var demands = catalog.demands();
			var demandItems = this.screen.demandPreviewItem(actualIndex());
			for (int i = demands.size() - 1; i >= 0; i--) {
				renderPreviewItem(guiGraphics,
						demandItems.get(i),
						demands.get(i).quantity(),
						getX() + determineItemPosition(i, demands.size(), BUTTON_INPUT_X_OFFSET_START, BUTTON_INPUT_X_OFFSET_END),
						getY() + 2);
			}

			var offers = catalog.offers();
			var offerItems = this.screen.offerPreviewItem(actualIndex());
			for (int i = offers.size() - 1; i >= 0; i--) {
				renderPreviewItem(guiGraphics,
						offerItems.get(i),
						offers.get(i).quantity(),
						getX() + determineItemPosition(i, offers.size(), BUTTON_OUTPUT_X_OFFSET_START, BUTTON_OUTPUT_X_OFFSET_END),
						getY() + 2);
			}
		}

		private void renderPreviewItem(GuiGraphics guiGraphics, List<ItemStack> stacks, int quantity, int x, int y) {
			ItemStack stack;
			if (stacks.isEmpty()) {
				stack = fallbackIcon();
			} else {
				stack = this.screen.cycle(stacks);
				if (stack.isEmpty()) stack = fallbackIcon();
			}
			guiGraphics.renderFakeItem(stack, x, y);
			if (quantity != 1) {
				guiGraphics.renderItemDecorations(this.screen.font, stack, x, y, String.valueOf(quantity));
			}
		}

		private void renderToolTip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
			if (!this.isHovered) return;
			BargainCatalog catalog = catalog();
			if (catalog == null) return;

			if (renderTooltip0(guiGraphics, mouseX, mouseY, catalog, true)) return;
			renderTooltip0(guiGraphics, mouseX, mouseY, catalog, false);
		}

		private boolean renderTooltip0(GuiGraphics guiGraphics, int mouseX, int mouseY, BargainCatalog catalog, boolean demand) {
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

			List<@NotNull Component> tooltip = closest.getTooltip();

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
				guiGraphics.renderComponentTooltip(this.screen.font, tooltip, mouseX, mouseY);
			}

			return true;
		}

		@Override protected void updateWidgetNarration(@NotNull NarrationElementOutput o) {}
	}

	private @NotNull ItemStack cycle(@NotNull List<ItemStack> stacks) {
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
