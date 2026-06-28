package indi.etern.musichud.client.ui.screen;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.interfaces.ClientConfig;
import indi.etern.musichud.client.config.Keybinds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public class SettingsTabScreen extends Screen {
    private final MusicHudScreen parent;

    public SettingsTabScreen(MusicHudScreen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearWidgets();
        int centerX = width / 2;
        int y = 10;

        ClientConfig config = ClientConfig.getInstance();

        addRenderableWidget(CycleButton.onOffBuilder(Component.translatable(MusicHud.MOD_ID + ".config.enable"))
                .withInitialValue(config.getEnable())
                .create(centerX - 100, y, 200, 20, Component.empty(), (button, value) -> {
                    config.setEnable(value);
                    config.save();
                    parent.refresh();
                }));
        y += 24;

        addRenderableWidget(CycleButton.onOffBuilder(Component.translatable(MusicHud.MOD_ID + ".config.enableHud"))
                .withInitialValue(config.getEnableHud())
                .create(centerX - 100, y, 200, 20, Component.empty(), (button, value) -> {
                    config.setEnableHud(value);
                    config.save();
                    parent.refresh();
                }));
        y += 24;

        addRenderableWidget(CycleButton.onOffBuilder(Component.translatable(MusicHud.MOD_ID + ".config.enableIsolatedMode"))
                .withInitialValue(config.getEnableIsolatedMode())
                .create(centerX - 100, y, 200, 20, Component.empty(), (button, value) -> {
                    config.setEnableIsolatedMode(value);
                    config.save();
                    parent.refresh();
                }));
        y += 24;

        addRenderableWidget(CycleButton.onOffBuilder(Component.translatable(MusicHud.MOD_ID + ".config.muted"))
                .withInitialValue(config.getMuted())
                .create(centerX - 100, y, 200, 20, Component.empty(), (button, value) -> {
                    config.setMuted(value);
                    config.save();
                    parent.refresh();
                }));
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".config.soundVolume"), button -> {
            EditBox editBox = new EditBox(font, centerX - 100, y, 200, 20, Component.translatable(MusicHud.MOD_ID + ".config.soundVolume"));
            editBox.setValue(String.valueOf(config.getSoundVolume()));
            editBox.setResponder(text -> {
                try {
                    int volume = Integer.parseInt(text);
                    if (volume >= 0 && volume <= 100) {
                        config.forceSetSoundVolume(volume);
                        config.save();
                        parent.refresh();
                    }
                } catch (NumberFormatException ignored) {}
            });
            addRenderableWidget(editBox);
            setFocused(editBox);
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".config.soundVolumeInterval"), button -> {
            EditBox editBox = new EditBox(font, centerX - 100, y, 200, 20, Component.translatable(MusicHud.MOD_ID + ".config.soundVolumeInterval"));
            editBox.setValue(String.valueOf(config.getSoundVolumeInterval()));
            editBox.setResponder(text -> {
                try {
                    int interval = Integer.parseInt(text);
                    if (interval > 0 && interval <= 50) {
                        config.setSoundVolumeInterval(interval);
                        config.save();
                        parent.refresh();
                    }
                } catch (NumberFormatException ignored) {}
            });
            addRenderableWidget(editBox);
            setFocused(editBox);
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(CycleButton.onOffBuilder(Component.translatable(MusicHud.MOD_ID + ".config.enableMarqueeText"))
                .withInitialValue(config.getEnableMarqueeText())
                .create(centerX - 100, y, 200, 20, Component.empty(), (button, value) -> {
                    config.setEnableMarqueeText(value);
                    config.save();
                    parent.refresh();
                }));
        y += 24;

        addRenderableWidget(CycleButton.onOffBuilder(Component.translatable(MusicHud.MOD_ID + ".config.showTranslatedCnLyrics"))
                .withInitialValue(config.getShowTranslatedCnLyrics())
                .create(centerX - 100, y, 200, 20, Component.empty(), (button, value) -> {
                    config.setShowTranslatedCnLyrics(value);
                    config.save();
                    parent.refresh();
                }));
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".config.hudOffsetX"), button -> {
            EditBox editBox = new EditBox(font, centerX - 100, y, 200, 20, Component.translatable(MusicHud.MOD_ID + ".config.hudOffsetX"));
            editBox.setValue(String.valueOf(config.getHudOffsetX()));
            editBox.setResponder(text -> {
                try {
                    int offset = Integer.parseInt(text);
                    config.setHudOffsetX(offset);
                    config.save();
                    parent.refresh();
                } catch (NumberFormatException ignored) {}
            });
            addRenderableWidget(editBox);
            setFocused(editBox);
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".config.hudOffsetY"), button -> {
            EditBox editBox = new EditBox(font, centerX - 100, y, 200, 20, Component.translatable(MusicHud.MOD_ID + ".config.hudOffsetY"));
            editBox.setValue(String.valueOf(config.getHudOffsetY()));
            editBox.setResponder(text -> {
                try {
                    int offset = Integer.parseInt(text);
                    config.setHudOffsetY(offset);
                    config.save();
                    parent.refresh();
                } catch (NumberFormatException ignored) {}
            });
            addRenderableWidget(editBox);
            setFocused(editBox);
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".config.hudWidth"), button -> {
            EditBox editBox = new EditBox(font, centerX - 100, y, 200, 20, Component.translatable(MusicHud.MOD_ID + ".config.hudWidth"));
            editBox.setValue(String.valueOf(config.getHudWidth()));
            editBox.setResponder(text -> {
                try {
                    int width = Integer.parseInt(text);
                    if (width > 0 && width <= 500) {
                        config.setHudWidth(width);
                        config.save();
                        parent.refresh();
                    }
                } catch (NumberFormatException ignored) {}
            });
            addRenderableWidget(editBox);
            setFocused(editBox);
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".config.hudHeight"), button -> {
            EditBox editBox = new EditBox(font, centerX - 100, y, 200, 20, Component.translatable(MusicHud.MOD_ID + ".config.hudHeight"));
            editBox.setValue(String.valueOf(config.getHudHeight()));
            editBox.setResponder(text -> {
                try {
                    int height = Integer.parseInt(text);
                    if (height > 0 && height <= 500) {
                        config.setHudHeight(height);
                        config.save();
                        parent.refresh();
                    }
                } catch (NumberFormatException ignored) {}
            });
            addRenderableWidget(editBox);
            setFocused(editBox);
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".config.hudCornerRadius"), button -> {
            EditBox editBox = new EditBox(font, centerX - 100, y, 200, 20, Component.translatable(MusicHud.MOD_ID + ".config.hudCornerRadius"));
            editBox.setValue(String.valueOf(config.getHudCornerRadius()));
            editBox.setResponder(text -> {
                try {
                    int radius = Integer.parseInt(text);
                    if (radius >= 0 && radius <= 50) {
                        config.setHudCornerRadius(radius);
                        config.save();
                        parent.refresh();
                    }
                } catch (NumberFormatException ignored) {}
            });
            addRenderableWidget(editBox);
            setFocused(editBox);
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}