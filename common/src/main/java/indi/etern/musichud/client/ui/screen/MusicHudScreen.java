package indi.etern.musichud.client.ui.screen;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.client.ui.hud.HudRendererManager;
import indi.etern.musichud.interfaces.ClientConfig;
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
    private final List<Tab> tabs = new ArrayList<>();

    public MusicHudScreen(@Nullable Screen previous) {
        super(Component.literal("Music HUD"));
        this.previous = previous;
    }

    public static MusicHudScreen createScreen(@Nullable Screen previousScreen) {
        return new MusicHudScreen(previousScreen);
    }

    public static void refresh() {
    }

    @Override
    protected void init() {
        setupTabs();
        rebuildWidgets();
    }

    private void setupTabs() {
        tabs.clear();
        tabs.add(new Tab(I18n.get(MusicHud.MOD_ID + ".gui.tab.home")));
        tabs.add(new Tab(I18n.get(MusicHud.MOD_ID + ".gui.tab.search")));
        tabs.add(new Tab(I18n.get(MusicHud.MOD_ID + ".gui.tab.account")));
        tabs.add(new Tab(I18n.get(MusicHud.MOD_ID + ".gui.tab.settings")));
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
            Tab tab = tabs.get(i);
            int x = startX + i * tabWidth;
            addRenderableWidget(Button.builder(Component.literal(tab.name), button -> {
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

        addRenderableWidget(Button.builder(Component.literal(muted ? "Unmute" : "Mute"), button -> {
            CLIENT_CONFIG.setMuted(!muted);
            CLIENT_CONFIG.save();
            rebuildWidgets();
        }).bounds(centerX - 100, y, 200, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.literal("Volume: " + volume), button -> {}).bounds(centerX - 100, y, 200, 20).build());
    }

    private void addSearchWidgets(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.literal("Search (Coming Soon)"), button -> {}).bounds(centerX - 100, y, 200, 20).build());
    }

    private void addAccountWidgets(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.literal("Login (Coming Soon)"), button -> {}).bounds(centerX - 100, y, 200, 20).build());
    }

    private void addSettingsWidgets(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.literal("Offset X: " + CLIENT_CONFIG.getHudOffsetX()), button -> {}).bounds(centerX - 100, y, 200, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Offset Y: " + CLIENT_CONFIG.getHudOffsetY()), button -> {}).bounds(centerX - 100, y, 200, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Width: " + CLIENT_CONFIG.getHudWidth()), button -> {}).bounds(centerX - 100, y, 200, 20).build());
        y += 24;
        addRenderableWidget(Button.builder(Component.literal("Height: " + CLIENT_CONFIG.getHudHeight()), button -> {}).bounds(centerX - 100, y, 200, 20).build());
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
    }

    @Override
    public void onClose() {
        minecraft.setScreen(previous);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class Tab {
        final String name;
        Tab(String name) {
            this.name = name;
        }
    }
}
