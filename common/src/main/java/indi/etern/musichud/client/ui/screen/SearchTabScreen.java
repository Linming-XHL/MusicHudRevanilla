package indi.etern.musichud.client.ui.screen;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.beans.api.SearchType;
import indi.etern.musichud.beans.music.Album;
import indi.etern.musichud.beans.music.Artist;
import indi.etern.musichud.beans.music.MusicDetail;
import indi.etern.musichud.beans.music.Playlist;
import indi.etern.musichud.client.services.MusicService;
import indi.etern.musichud.interfaces.ClientConfig;
import indi.etern.musichud.network.IClientNetworkService;
import indi.etern.musichud.network.payloads.requestResponseCycle.SearchAlbumsResponse;
import indi.etern.musichud.network.payloads.requestResponseCycle.SearchArtistsResponse;
import indi.etern.musichud.network.payloads.requestResponseCycle.SearchMusicResponse;
import indi.etern.musichud.network.payloads.requestResponseCycle.SearchPlaylistsResponse;
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

public class SearchTabScreen extends Screen {
    private final MusicHudScreen parent;
    private EditBox searchBox;
    private String lastQuery = "";
    private SearchType currentSearchType = SearchType.MUSIC;
    private List<MusicDetail> musicResults = new ArrayList<>();
    private List<Artist> artistResults = new ArrayList<>();
    private List<Album> albumResults = new ArrayList<>();
    private List<Playlist> playlistResults = new ArrayList<>();
    private int resultOffset = 0;
    private boolean isLoading = false;

    public SearchTabScreen(MusicHudScreen parent) {
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

        searchBox = new EditBox(font, centerX - 150, y, 250, 20, Component.translatable(MusicHud.MOD_ID + ".search.placeholder"));
        searchBox.setValue(lastQuery);
        searchBox.setResponder(this::onSearchQueryChanged);
        addRenderableWidget(searchBox);

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.search"), button -> {
            performSearch(lastQuery);
        }).bounds(centerX + 110, y, 60, 20).build());

        y += 24;
        int btnWidth = 60;
        int startX = centerX - (btnWidth * SearchType.values().length) / 2;
        for (int i = 0; i < SearchType.values().length; i++) {
            SearchType type = SearchType.values()[i];
            int btnX = startX + i * btnWidth;
            String label = switch (type) {
                case MUSIC -> I18n.get(MusicHud.MOD_ID + ".search.type.music");
                case ARTIST -> I18n.get(MusicHud.MOD_ID + ".search.type.artist");
                case ALBUM -> I18n.get(MusicHud.MOD_ID + ".search.type.album");
                case PLAYLIST -> I18n.get(MusicHud.MOD_ID + ".search.type.playlist");
            };
            addRenderableWidget(Button.builder(Component.literal(label), button -> {
                currentSearchType = type;
                resultOffset = 0;
                clearResults();
                performSearch(lastQuery);
                rebuildWidgets();
            }).bounds(btnX, y, btnWidth, 20).build());
        }

        y += 30;
        renderResults(y);
    }

    private void onSearchQueryChanged(String query) {
        lastQuery = query;
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            clearResults();
            rebuildWidgets();
            return;
        }

        isLoading = true;
        rebuildWidgets();

        SearchMusicResponse.setReceiver(response -> {
            musicResults = response.result();
            isLoading = false;
            Minecraft.getInstance().execute(this::rebuildWidgets);
        });
        SearchAlbumsResponse.setReceiver(response -> {
            albumResults = response.result();
            isLoading = false;
            Minecraft.getInstance().execute(this::rebuildWidgets);
        });
        SearchArtistsResponse.setReceiver(response -> {
            artistResults = response.result();
            isLoading = false;
            Minecraft.getInstance().execute(this::rebuildWidgets);
        });
        SearchPlaylistsResponse.setReceiver(response -> {
            playlistResults = response.result();
            isLoading = false;
            Minecraft.getInstance().execute(this::rebuildWidgets);
        });

        IClientNetworkService.getInstance().sendToServer(new SearchRequest(query, currentSearchType, resultOffset));
    }

    private void clearResults() {
        musicResults = new ArrayList<>();
        artistResults = new ArrayList<>();
        albumResults = new ArrayList<>();
        playlistResults = new ArrayList<>();
    }

    private void renderResults(int startY) {
        int centerX = width / 2;
        int y = startY;
        int maxResults = 10;

        if (isLoading) {
            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".text.loading"), button -> {})
                    .bounds(centerX - 75, y, 150, 20).build());
            return;
        }

        switch (currentSearchType) {
            case MUSIC -> {
                for (int i = 0; i < Math.min(musicResults.size(), maxResults); i++) {
                    MusicDetail music = musicResults.get(i);
                    String label = music.getName() + " - " + music.getArtists().stream()
                            .map(Artist::getName)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");
                    if (label.length() > 40) label = label.substring(0, 37) + "...";
                    int finalI = i;
                    addRenderableWidget(Button.builder(Component.literal(label), button -> {
                        MusicService.getInstance().sendPushMusicToQueue(musicResults.get(finalI));
                    }).bounds(centerX - 150, y, 300, 20).build());
                    y += 22;
                }
            }
            case ARTIST -> {
                for (int i = 0; i < Math.min(artistResults.size(), maxResults); i++) {
                    Artist artist = artistResults.get(i);
                    addRenderableWidget(Button.builder(Component.literal(artist.getName()), button -> {})
                            .bounds(centerX - 150, y, 300, 20).build());
                    y += 22;
                }
            }
            case ALBUM -> {
                for (int i = 0; i < Math.min(albumResults.size(), maxResults); i++) {
                    Album album = albumResults.get(i);
                    addRenderableWidget(Button.builder(Component.literal(album.getName()), button -> {})
                            .bounds(centerX - 150, y, 300, 20).build());
                    y += 22;
                }
            }
            case PLAYLIST -> {
                for (int i = 0; i < Math.min(playlistResults.size(), maxResults); i++) {
                    Playlist playlist = playlistResults.get(i);
                    addRenderableWidget(Button.builder(Component.literal(playlist.getName()), button -> {})
                            .bounds(centerX - 150, y, 300, 20).build());
                    y += 22;
                }
            }
        }

        boolean hasResults = !musicResults.isEmpty() || !artistResults.isEmpty() ||
                !albumResults.isEmpty() || !playlistResults.isEmpty();
        if (hasResults) {
            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.loadMore"), button -> {
                resultOffset += maxResults;
                performSearch(lastQuery);
            }).bounds(centerX - 75, y, 150, 20).build());
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
