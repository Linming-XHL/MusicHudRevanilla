package indi.etern.musichud.client.ui.hud.metadata;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.client.ui.hud.renderer.HudRenderContext;
import lombok.Getter;
import net.minecraft.client.resources.language.I18n;

@Getter
public enum VerticalAlign {
    TOP(MusicHud.MOD_ID + ".config.layout.verticalAlign.TOP", 48) {
        @Override
        float calcY(float y, HudRenderContext hudRenderContext, Layout hudLayout) {
            return y;
        }
    }, CENTER(MusicHud.MOD_ID + ".config.layout.verticalAlign.CENTER", 17) {
        @Override
        float calcY(float y, HudRenderContext hudRenderContext, Layout hudLayout) {
            return (float) hudRenderContext.guiHeight() / 2 + y - hudLayout.getHeight() / 2;
        }
    }, BOTTOM(MusicHud.MOD_ID + ".config.layout.verticalAlign.BOTTOM", 80) {
        @Override
        float calcY(float y, HudRenderContext hudRenderContext, Layout hudLayout) {
            return hudRenderContext.guiHeight() - hudLayout.getHeight() - y;
        }
    };

    private final String displayNameKey;
    private final int gravity;

    VerticalAlign(String displayNameKey, int gravity) {
        this.displayNameKey = displayNameKey;
        this.gravity = gravity;
    }


    abstract float calcY(float y, HudRenderContext hudRenderContext, Layout hudLayout);

    @Override
    public String toString() {
        return I18n.get(displayNameKey);
    }
}
