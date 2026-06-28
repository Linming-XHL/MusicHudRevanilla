package indi.etern.musichud.client.ui.screen;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.client.services.LoginService;
import indi.etern.musichud.client.services.MusicService;
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
    private Screen currentTabScreen;
    private List<Tab> tabs = new ArrayList<>();

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
        setupTabs();
        rebuildWidgets();
    }

    private void setupTabs() {
        tabs.clear();
        tabs.add(new Tab(I18n.get(MusicHud.MOD_ID + ".gui.tab.home"), this::createHomeScreen));
        tabs.add(new Tab(I18n.get(MusicHud.MOD_ID + ".gui.tab.search"), this::createSearchScreen));
        tabs.add(new Tab(I18n.get(MusicHud.MOD_ID + ".gui.tab.account"), this::createAccountScreen));
        tabs.add(new Tab(I18n.get(MusicHud.MOD_ID + ".gui.tab.settings"), this::createSettingsScreen));
    }

    private Screen createHomeScreen() {
        return new HomeTabScreen(this);
    }

    private Screen createSearchScreen() {
        return new SearchTabScreen(this);
    }

    private Screen createAccountScreen() {
        return new AccountTabScreen(this);
    }

    private Screen createSettingsScreen() {
        return new SettingsTabScreen(this);
    }

    private void switchTab(int index) {
        if (index < 0 || index >= tabs.size()) return;
        selectedTab = index;
        currentTabScreen = tabs.get(index).factory.apply(this);
        if (currentTabScreen != null) {
            currentTabScreen.init(this.minecraft, this.width, this.height);
        }
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
            Tab tab = tabs.get(i);
            int x = startX + i * tabWidth;
            int tabIndex = i;
            addRenderableWidget(Button.builder(Component.literal(tab.name), button -> switchTab(tabIndex))
                    .bounds(x, tabY, tabWidth, tabHeight)
                    .build());
        }

        if (currentTabScreen != null) {
            this.children.addAll(currentTabScreen.children());
        }

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(width / 2 - 50, height - 25, 100, 20).build());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, width, height, 0x99000000);

        if (currentTabScreen != null) {
            currentTabScreen.render(graphics, mouseX, mouseY, partialTick);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (currentTabScreen != null) {
            currentTabScreen.onClose();
        }
        this.minecraft.setScreen(previous);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class Tab {
        final String name;
        final java.util.function.Function<MusicHudScreen, Screen> factory;

        Tab(String name, java.util.function.Function<MusicHudScreen, Screen> factory) {
            this.name = name;
            this.factory = factory;
        }
    }
}
