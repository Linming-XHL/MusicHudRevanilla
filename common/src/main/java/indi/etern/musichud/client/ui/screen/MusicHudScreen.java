package indi.etern.musichud.client.ui.screen;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.beans.music.MusicDetail;
import indi.etern.musichud.client.audio.NowPlayingInfo;
import indi.etern.musichud.client.services.LoginService;
import indi.etern.musichud.client.services.MusicService;
import indi.etern.musichud.client.ui.ToastUtil;
import indi.etern.musichud.interfaces.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MusicHudScreen extends Screen {
    private static final ClientConfig CLIENT_CONFIG = ClientConfig.getInstance();
    @Nullable
    private final Screen previous;
    private int selectedTab = 0;
    private final List<String> tabs = new ArrayList<>();

    public MusicHudScreen(@Nullable Screen previous) {
        super(Component.literal("Music HUD"));
        this.previous = previous;
    }

    public static MusicHudScreen createScreen(@Nullable Screen previousScreen) {
        return new MusicHudScreen(previousScreen);
    }

    public static void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.submit(() -> {
            if (minecraft.screen instanceof MusicHudScreen screen) {
                screen.rebuildWidgets();
            }
        });
    }

    @Override
    protected void init() {
        tabs.clear();
        tabs.add(I18n.get(MusicHud.MOD_ID + ".gui.tab.home"));
        tabs.add(I18n.get(MusicHud.MOD_ID + ".gui.tab.search"));
        tabs.add(I18n.get(MusicHud.MOD_ID + ".gui.tab.account"));
        tabs.add(I18n.get(MusicHud.MOD_ID + ".gui.tab.settings"));
        rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        clearWidgets();

        int tabY = 10;
        int tabHeight = 20;
        int tabWidth = 80;
        int startX = (width - tabs.size() * tabWidth) / 2;

        for (int i = 0; i < tabs.size(); i++) {
            int tabIndex = i;
            String tab = tabs.get(i);
            int x = startX + i * tabWidth;
            addRenderableWidget(Button.builder(Component.literal(tab), button -> {
                selectedTab = tabIndex;
                rebuildWidgets();
            }).bounds(x, tabY, tabWidth, tabHeight).build());
        }

        int contentY = tabY + tabHeight + 10;
        addContentWidgets(contentY);

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 50, height - 25, 100, 20).build());
    }

    private void addContentWidgets(int startY) {
        int centerX = width / 2;
        int y = startY;

        if (selectedTab == 0) {
            addHomeWidgets(centerX, y);
        } else if (selectedTab == 1) {
            addSearchWidgets(centerX, y);
        } else if (selectedTab == 2) {
            addAccountWidgets(centerX, y);
        } else if (selectedTab == 3) {
            addSettingsWidgets(centerX, y);
        }
    }

    private void addHomeWidgets(int centerX, int y) {
        boolean enabled = CLIENT_CONFIG.getEnable();
        boolean hudEnabled = CLIENT_CONFIG.getEnableHud();
        boolean muted = CLIENT_CONFIG.getMuted();
        int volume = muted ? 0 : CLIENT_CONFIG.getSoundVolume();

        MusicDetail music = NowPlayingInfo.getInstance().getCurrentlyPlayingMusicDetail();
        boolean hasMusic = music != null && music != MusicDetail.NONE;

        if (hasMusic) {
            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.voteForSkip"), button -> {
                MusicHud.EXECUTOR.execute(() -> MusicService.getInstance().keyBindsVoteSkipCurrent());
            }).bounds(centerX - 100, y, 200, 20).build());
            y += 24;
        }

        addRenderableWidget(Button.builder(Component.literal(enabled ? "Disable" : "Enable"), button -> {
            CLIENT_CONFIG.setEnable(!enabled);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal(hudEnabled ? "Hide HUD" : "Show HUD"), button -> {
            CLIENT_CONFIG.setEnableHud(!hudEnabled);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal(muted ? "Unmute (Volume: 0)" : "Mute (Volume: " + volume + ")"), button -> {
            CLIENT_CONFIG.setMuted(!muted);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Toggle Connection"), button -> {
            MusicHud.EXECUTOR.execute(LoginService.getInstance()::keyBindsToggleConnection);
        }).bounds(centerX - 100, y, 200, 20).build());
    }

    private void addSearchWidgets(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.literal("Search Music (Network Required)"), button -> {
            ToastUtil.show("Search requires server connection");
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Search Artist (Network Required)"), button -> {
            ToastUtil.show("Search requires server connection");
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Search Album (Network Required)"), button -> {
            ToastUtil.show("Search requires server connection");
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Search Playlist (Network Required)"), button -> {
            ToastUtil.show("Search requires server connection");
        }).bounds(centerX - 100, y, 200, 20).build());
    }

    private void addAccountWidgets(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.literal("QR Code Login"), button -> {
            LoginService loginService = LoginService.getInstance();
            loginService.loginToServer(LoginService.ConnectionType.EXTERNAL);
            ToastUtil.show("Connecting to server...");
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Anonymous Login"), button -> {
            LoginService loginService = LoginService.getInstance();
            loginService.loginToServer(LoginService.ConnectionType.EXTERNAL);
            ToastUtil.show("Logging in as anonymous...");
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Logout"), button -> {
            LoginService loginService = LoginService.getInstance();
            loginService.logout();
            ToastUtil.show("Logged out");
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Disconnect"), button -> {
            LoginService loginService = LoginService.getInstance();
            loginService.disconnectToExternalOrIntegratedServer();
            ToastUtil.show("Disconnected");
        }).bounds(centerX - 100, y, 200, 20).build());
    }

    private void addSettingsWidgets(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.literal("HUD Offset X: " + CLIENT_CONFIG.getHudOffsetX()), button -> {
            CLIENT_CONFIG.setHudOffsetX(CLIENT_CONFIG.getHudOffsetX() + 1);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            CLIENT_CONFIG.setHudOffsetX(CLIENT_CONFIG.getHudOffsetX() - 1);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX + 2, y, 98, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("HUD Offset Y: " + CLIENT_CONFIG.getHudOffsetY()), button -> {
            CLIENT_CONFIG.setHudOffsetY(CLIENT_CONFIG.getHudOffsetY() + 1);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            CLIENT_CONFIG.setHudOffsetY(CLIENT_CONFIG.getHudOffsetY() - 1);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX + 2, y, 98, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("HUD Width: " + CLIENT_CONFIG.getHudWidth()), button -> {
            CLIENT_CONFIG.setHudWidth(CLIENT_CONFIG.getHudWidth() + 1);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            CLIENT_CONFIG.setHudWidth(Math.max(50, CLIENT_CONFIG.getHudWidth() - 1));
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX + 2, y, 98, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("HUD Height: " + CLIENT_CONFIG.getHudHeight()), button -> {
            CLIENT_CONFIG.setHudHeight(CLIENT_CONFIG.getHudHeight() + 1);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            CLIENT_CONFIG.setHudHeight(Math.max(20, CLIENT_CONFIG.getHudHeight() - 1));
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX + 2, y, 98, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("HUD Corner Radius: " + CLIENT_CONFIG.getHudCornerRadius()), button -> {
            CLIENT_CONFIG.setHudCornerRadius(CLIENT_CONFIG.getHudCornerRadius() + 1);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            CLIENT_CONFIG.setHudCornerRadius(Math.max(0, CLIENT_CONFIG.getHudCornerRadius() - 1));
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX + 2, y, 98, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Volume Interval: " + CLIENT_CONFIG.getSoundVolumeInterval()), button -> {
            CLIENT_CONFIG.setSoundVolumeInterval(CLIENT_CONFIG.getSoundVolumeInterval() + 1);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 98, 20).build());
        addRenderableWidget(Button.builder(Component.literal("-"), button -> {
            CLIENT_CONFIG.setSoundVolumeInterval(Math.max(1, CLIENT_CONFIG.getSoundVolumeInterval() - 1));
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX + 2, y, 98, 20).build());
        y += 24;

        boolean marquee = CLIENT_CONFIG.getEnableMarqueeText();
        addRenderableWidget(Button.builder(Component.literal("Marquee Text: " + (marquee ? "ON" : "OFF")), button -> {
            CLIENT_CONFIG.setEnableMarqueeText(!marquee);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        boolean translated = CLIENT_CONFIG.getShowTranslatedCnLyrics();
        addRenderableWidget(Button.builder(Component.literal("Show Translated Lyrics: " + (translated ? "ON" : "OFF")), button -> {
            CLIENT_CONFIG.setShowTranslatedCnLyrics(!translated);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        boolean isolated = CLIENT_CONFIG.getEnableIsolatedMode();
        addRenderableWidget(Button.builder(Component.literal("Isolated Mode: " + (isolated ? "ON" : "OFF")), button -> {
            CLIENT_CONFIG.setEnableIsolatedMode(!isolated);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        boolean autoConnect = CLIENT_CONFIG.getEnableAutoConnect();
        addRenderableWidget(Button.builder(Component.literal("Auto Connect: " + (autoConnect ? "ON" : "OFF")), button -> {
            CLIENT_CONFIG.setEnableAutoConnect(!autoConnect);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTick) {
        super.extractBackground(graphics, mouseX, mouseY, deltaTick);
        graphics.fill(0, 0, width, height, 0x99000000);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTick) {
        super.extractRenderState(graphics, mouseX, mouseY, deltaTick);
        int centerX = width / 2;
        int y = 5;
        graphics.text(font, title, centerX - font.width(title) / 2, y, 0xFFFFFFFF, true);

        MusicDetail music = NowPlayingInfo.getInstance().getCurrentlyPlayingMusicDetail();
        String musicName = music == null || music == MusicDetail.NONE
                ? I18n.get(MusicHud.MOD_ID + ".text.idle")
                : music.getName();
        String statusText = "Now Playing: " + musicName;
        graphics.text(font, statusText, centerX - font.width(statusText) / 2, height - 40, 0xFFA0A0A0, false);

        String connectText = "Status: " + MusicHud.getConnectStatus();
        graphics.text(font, connectText, centerX - font.width(connectText) / 2, height - 55, 0xFFA0A0A0, false);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previous);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
