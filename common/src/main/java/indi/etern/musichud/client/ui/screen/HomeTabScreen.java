package indi.etern.musichud.client.ui.screen;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.beans.music.MusicDetail;
import indi.etern.musichud.client.audio.NowPlayingInfo;
import indi.etern.musichud.client.services.MusicService;
import indi.etern.musichud.interfaces.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class HomeTabScreen extends Screen {
    private final MusicHudScreen parent;

    public HomeTabScreen(MusicHudScreen parent) {
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
        int startY = 10;

        MusicDetail music = NowPlayingInfo.getInstance().getCurrentlyPlayingMusicDetail();
        boolean hasMusic = music != null && music != MusicDetail.NONE;

        if (hasMusic) {
            addRenderableWidget(Button.builder(Component.literal(I18n.get(MusicHud.MOD_ID + ".button.voteForSkip")), button ->
                    MusicHud.EXECUTOR.execute(() -> MusicService.getInstance().keyBindsVoteSkipCurrent())
            ).bounds(centerX - 100, startY, 200, 20).build());
            startY += 24;
        }

        ClientConfig config = ClientConfig.getInstance();
        addRenderableWidget(Button.builder(Component.literal(config.getEnableHud() ? "Hide HUD" : "Show HUD"), button -> {
            MusicHud.EXECUTOR.execute(() -> {
                config.setEnableHud(!config.getEnableHud());
                config.save();
                parent.refresh();
            });
        }).bounds(centerX - 100, startY, 200, 20).build());
        startY += 24;

        addRenderableWidget(Button.builder(Component.literal(config.getMuted() ? "Unmute" : "Mute"), button -> {
            MusicHud.EXECUTOR.execute(() -> {
                config.setMuted(!config.getMuted());
                config.save();
                parent.refresh();
            });
        }).bounds(centerX - 100, startY, 200, 20).build());
        startY += 24;

        addRenderableWidget(Button.builder(Component.literal("Toggle Connection"), button ->
                MusicHud.EXECUTOR.execute(LoginService.getInstance()::keyBindsToggleConnection)
        ).bounds(centerX - 100, startY, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int centerX = width / 2;
        int y = 10;

        graphics.drawString(font, title, centerX - font.width(title) / 2, y, 0xFFFFFFFF, true);
        y += 20;

        MusicDetail music = NowPlayingInfo.getInstance().getCurrentlyPlayingMusicDetail();
        String musicName = music == null || music == MusicDetail.NONE
                ? I18n.get(MusicHud.MOD_ID + ".text.idle")
                : music.getName();
        drawCenteredString(graphics, I18n.get(MusicHud.MOD_ID + ".text.currentMusic") + ": " + musicName, centerX, y, 0xFFE0E0E0);
        y += 12;
        drawCenteredString(graphics, "Status: " + MusicHud.getConnectStatus(), centerX, y, 0xFFA0A0A0);
        y += 12;
        drawCenteredString(graphics, "Volume: " + (ClientConfig.getInstance().getMuted() ? 0 : ClientConfig.getInstance().getSoundVolume()), centerX, y, 0xFFA0A0A0);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}