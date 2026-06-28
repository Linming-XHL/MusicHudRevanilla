package indi.etern.musichud.network.payloads.requestResponseCycle;

import indi.etern.musichud.beans.music.Album;
import indi.etern.musichud.interfaces.CommonRegister;
import indi.etern.musichud.interfaces.RegisterMark;
import indi.etern.musichud.network.Codecs;
import indi.etern.musichud.network.INetworkRegister;
import indi.etern.musichud.network.payloads.S2CPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.function.Consumer;

public record SearchAlbumsResponse(int offset, List<Album> result) implements S2CPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SearchAlbumsResponse> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            SearchAlbumsResponse::offset,
            Codecs.ofList(() -> Album.CODEC),
            SearchAlbumsResponse::result,
            SearchAlbumsResponse::new
    );

    static Consumer<SearchAlbumsResponse> consumer;

    public static void setReceiver(Consumer<SearchAlbumsResponse> receiver) {
        consumer = receiver;
    }

    @RegisterMark
    public static class RegisterImpl implements CommonRegister {
        @Override
        public void register() {
            INetworkRegister.getInstance().autoRegisterPayload(SearchAlbumsResponse.class, CODEC,
                    (message, player) -> {
                        if (consumer != null) {
                            consumer.accept(message);
                        }
                    }
            );
        }
    }
}
