package org.mtr.widget;

import org.mtr.libraries.gg.essential.elementa.components.UIContainer;
import org.mtr.libraries.gg.essential.elementa.components.UIWrappedText;
import org.mtr.libraries.gg.essential.elementa.constraints.FillConstraint;
import org.mtr.libraries.gg.essential.elementa.constraints.PixelConstraint;
import org.mtr.libraries.gg.essential.elementa.constraints.SiblingConstraint;
import org.mtr.libraries.gg.essential.elementa.constraints.SubtractiveConstraint;
import lombok.Getter;
import org.mtr.tool.GuiHelper;
import org.mtr.tool.ReleasedDynamicTextureRegistry;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class CheckboxComponent extends UIContainer {

	private boolean disabled = false;
	@Getter
	private boolean checked = false;

	private final DirectImageComponent directImageComponent = (DirectImageComponent) new DirectImageComponent(
		ReleasedDynamicTextureRegistry.CHECKBOX_UNCHECKED_TEXTURE.get(),
		ReleasedDynamicTextureRegistry.CHECKBOX_CHECKED_TEXTURE.get(),
		ReleasedDynamicTextureRegistry.CHECKBOX_UNCHECKED_HIGHLIGHTED_TEXTURE.get(),
		ReleasedDynamicTextureRegistry.CHECKBOX_CHECKED_HIGHLIGHTED_TEXTURE.get()
	)
		.setChildOf(this)
		.setWidth(new PixelConstraint(CHECKBOX_SIZE))
		.setHeight(new PixelConstraint(CHECKBOX_SIZE));

	private final UIWrappedText wrappedTextLabel = (UIWrappedText) new UIWrappedText("", false, null, false)
		.setChildOf(this)
		.setX(new SiblingConstraint(TEXT_PADDING))
		.setY(new PixelConstraint(TEXT_PADDING))
		.setWidth(new SubtractiveConstraint(new FillConstraint(), new PixelConstraint(TEXT_PADDING)))
		.setColor(new Color(GuiHelper.MINECRAFT_GUI_TITLE_TEXT_COLOR));

	private final List<Runnable> clickListeners = new ArrayList<>();

	private static final int CHECKBOX_SIZE = 20;
	private static final int TEXT_PADDING = (CHECKBOX_SIZE - GuiHelper.MINECRAFT_FONT_SIZE) / 2;

	public CheckboxComponent() {
		setHeight(new PixelConstraint(CHECKBOX_SIZE));
		onMouseEnterRunnable(() -> setHighlighted(true));
		onMouseLeaveRunnable(() -> setHighlighted(false));
		setHighlighted(false);
		// Single click consumer: toggle the state first, then notify listeners (so isChecked()
		// already reflects the new value when the listener runs).
		onMouseClickConsumer(clickEvent -> {
			if (!disabled) {
				checked = !checked;
				setHighlighted(true);
				clickListeners.forEach(Runnable::run);
			}
		});
	}

	public void onClick(Runnable runnable) {
		clickListeners.add(runnable);
	}

	public void setText(String text) {
		wrappedTextLabel.setText(text);
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
		setHighlighted(false);
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
		setHighlighted(false);
	}

	private void setHighlighted(boolean highlighted) {
		directImageComponent.setActiveTexture((highlighted ? 2 : 0) + (checked ? 1 : 0));
	}
}
