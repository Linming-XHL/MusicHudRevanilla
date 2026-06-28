package indi.etern.musichud.network.payloads.requestResponseCycle;

import indi.etern.musichud.beans.music.MusicResourceInfo;
import indi.etern.musichud.interfaces.CommonRegister;
import indi.etern.musichud.interfaces.RegisterMark;
import indi.etern.musichud.network.INetworkRegister;
import indi.etern.musichud.network.payloads.S2CPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public record GetMusicResourceResponse(MusicResourceInfo musicResourceInfo) implements S2CPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, GetMusicResourceResponse> CODEC =
            StreamCodec.composite(
                    MusicResourceInfo.CODEC,
                    GetMusicResourceResponse::musicResourceInfo,
                    GetMusicResourceResponse::new
            );

    static final Map<Long, Consumer<MusicResourceInfo>> consumerMap = new ConcurrentHashMap<>();
    public static void setReceiver(long id, Consumer<MusicResourceInfo> consumer) {
        Consumer<MusicResourceInfo> previous = consumerMap.put(id, consumer);
        if (previous != null) {
            previous.accept(null);
        }
    }

    @RegisterMark
    public static class RegisterImpl implements CommonRegister {
        public void register() {
            INetworkRegister.getInstance().autoRegisterPayload(
                    GetMusicResourceResponse.class, CODEC,
                    (response, player) -> {
                        long id = response.musicResourceInfo.getId();
                        Consumer<MusicResourceInfo> consumer = consumerMap.remove(id);
                        if (consumer != null) {
                            consumer.accept(response.musicResourceInfo);
                        } else {
                            // Log if no consumer found
                            org.apache.logging.log4j.LogManager.getLogger("MusicHud").warn("No consumer found for music resource response id={}", id);
                        }
                    }
            );
        }
    }
}
