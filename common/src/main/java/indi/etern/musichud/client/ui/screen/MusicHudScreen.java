package indi.etern.musichud.client.ui.screen;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.beans.api.SearchType;
import indi.etern.musichud.beans.music.MusicDetail;
import indi.etern.musichud.client.audio.NowPlayingInfo;
import indi.etern.musichud.client.services.LoginService;
import indi.etern.musichud.client.services.MusicService;
import indi.etern.musichud.client.ui.ToastUtil;
import indi.etern.musichud.interfaces.ClientConfig;
import indi.etern.musichud.network.IClientNetworkService;
import indi.etern.musichud.network.payloads.requestResponseCycle.SearchMusicResponse;
import indi.etern.musichud.network.payloads.requestResponseCycle.SearchRequest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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
    private EditBox searchBox;
    private String searchQuery = "";
    private List<MusicDetail> searchResults = new ArrayList<>();
    private boolean hasSearched = false;

    public MusicHudScreen(@Nullable Screen previous) {
        super(Component.translatable(MusicHud.MOD_ID + ".gui.title"));
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

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.button." + (enabled ? "disable" : "enable")), button -> {
            CLIENT_CONFIG.setEnable(!enabled);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.button." + (hudEnabled ? "hideHud" : "showHud")), button -> {
            CLIENT_CONFIG.setEnableHud(!hudEnabled);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        String muteKey = muted ? MusicHud.MOD_ID + ".gui.button.unmute" : MusicHud.MOD_ID + ".gui.button.mute";
        addRenderableWidget(Button.builder(Component.translatable(muteKey, volume), button -> {
            CLIENT_CONFIG.setMuted(!muted);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.button.toggleConnection"), button -> {
            MusicHud.EXECUTOR.execute(LoginService.getInstance()::keyBindsToggleConnection);
        }).bounds(centerX - 100, y, 200, 20).build());
    }

    private void addSearchWidgets(int centerX, int y) {
        searchBox = new EditBox(font, centerX - 100, y, 200, 20, Component.translatable(MusicHud.MOD_ID + ".field.hint.searchMusic"));
        searchBox.setValue(searchQuery);
        searchBox.setResponder(value -> searchQuery = value);
        addRenderableWidget(searchBox);
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.searchMusic"), button -> {
            performSearch();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        if (hasSearched) {
            if (searchResults.isEmpty()) {
                graphics.text(font, I18n.get(MusicHud.MOD_ID + ".text.searchNoMoreResult"), centerX - 50, y, 0xFFA0A0A0, false);
                y += 20;
            } else {
                for (int i = 0; i < Math.min(searchResults.size(), 5); i++) {
                    MusicDetail detail = searchResults.get(i);
                    String name = detail.getName();
                    if (name.length() > 25) name = name.substring(0, 22) + "...";
                    int index = i;
                    addRenderableWidget(Button.builder(Component.literal(name), button -> {
                        MusicService.getInstance().sendPushMusicToQueue(searchResults.get(index));
                        ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.pushedMusicToPlaylist"));
                    }).bounds(centerX - 100, y, 200, 20).build());
                    y += 22;
                }
            }
        }
    }

    private void performSearch() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return;
        }

        SearchMusicResponse.setReceiver(response -> {
            searchResults = response.result();
            hasSearched = true;
            Minecraft.getInstance().execute(this::rebuildWidgets);
        });

        IClientNetworkService.getInstance().sendToServer(new SearchRequest(searchQuery, SearchType.MUSIC, 0));
        ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".gui.text.searching"));
    }

    private void addAccountWidgets(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.button.qrLogin"), button -> {
            LoginService loginService = LoginService.getInstance();
            loginService.loginToServer(LoginService.ConnectionType.EXTERNAL);
            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".gui.text.connecting"));
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.button.anonymousLogin"), button -> {
            LoginService loginService = LoginService.getInstance();
            loginService.loginToServer(LoginService.ConnectionType.EXTERNAL);
            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".gui.text.loggingIn"));
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.button.logout"), button -> {
            LoginService loginService = LoginService.getInstance();
            loginService.logout();
            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".gui.text.loggedOut"));
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.button.disconnect"), button -> {
            LoginService loginService = LoginService.getInstance();
            loginService.disconnectToExternalOrIntegratedServer();
            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".gui.text.disconnected"));
        }).bounds(centerX - 100, y, 200, 20).build());
    }

    private void addSettingsWidgets(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.hudOffsetX", CLIENT_CONFIG.getHudOffsetX()), button -> {
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

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.hudOffsetY", CLIENT_CONFIG.getHudOffsetY()), button -> {
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

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.hudWidth", CLIENT_CONFIG.getHudWidth()), button -> {
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

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.hudHeight", CLIENT_CONFIG.getHudHeight()), button -> {
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

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.hudCornerRadius", CLIENT_CONFIG.getHudCornerRadius()), button -> {
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

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.volumeInterval", CLIENT_CONFIG.getSoundVolumeInterval()), button -> {
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
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.marqueeText", I18n.get(MusicHud.MOD_ID + ".gui.value." + (marquee ? "on" : "off"))), button -> {
            CLIENT_CONFIG.setEnableMarqueeText(!marquee);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        boolean translated = CLIENT_CONFIG.getShowTranslatedCnLyrics();
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.showTranslatedLyrics", I18n.get(MusicHud.MOD_ID + ".gui.value." + (translated ? "on" : "off"))), button -> {
            CLIENT_CONFIG.setShowTranslatedCnLyrics(!translated);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        boolean isolated = CLIENT_CONFIG.getEnableIsolatedMode();
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.isolatedMode", I18n.get(MusicHud.MOD_ID + ".gui.value." + (isolated ? "on" : "off"))), button -> {
            CLIENT_CONFIG.setEnableIsolatedMode(!isolated);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        boolean autoConnect = CLIENT_CONFIG.getEnableAutoConnect();
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".gui.label.autoConnect", I18n.get(MusicHud.MOD_ID + ".gui.value." + (autoConnect ? "on" : "off"))), button -> {
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
        String statusText = I18n.get(MusicHud.MOD_ID + ".gui.text.nowPlaying") + ": " + musicName;
        graphics.text(font, statusText, centerX - font.width(statusText) / 2, height - 40, 0xFFA0A0A0, false);

        String connectText = I18n.get(MusicHud.MOD_ID + ".gui.text.status") + ": " + MusicHud.getConnectStatus();
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
