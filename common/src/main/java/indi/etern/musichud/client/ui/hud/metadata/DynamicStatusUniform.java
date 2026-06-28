package indi.etern.musichud.client.ui.hud.metadata;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import indi.etern.musichud.MusicHud;
import indi.etern.musichud.client.audio.NowPlayingInfo;
import indi.etern.musichud.client.ui.hud.pipelines.HudUniform;
import indi.etern.musichud.client.ui.utils.Transitionable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
public class DynamicStatusUniform implements HudUniform {
    private static volatile DynamicStatusUniform instance;
    @Getter
    @Setter
    Transitionable<?> transitionable;
    public static final int UBO_SIZE = new Std140SizeCalculator().putVec4().align(16).get();
    private static final NowPlayingInfo NOW_PLAYING_INFO = NowPlayingInfo.getInstance();

    public static DynamicStatusUniform getInstance() {
        if (instance == null) {
            synchronized (DynamicStatusUniform.class) {
                if (instance == null) {
                    instance = new DynamicStatusUniform();
                }
            }
        }
        return instance;
    }
    private DynamicStatusUniform() {}

    @Override
    public String getUBOName() {
        return "MHDynamicStatus";
    }

    @Override
    public int getUBOSize() {
        return UBO_SIZE;
    }

    @Override
    public boolean shouldUseBuffer(HudUniform lastBuffered) {
        if (lastBuffered instanceof DynamicStatusUniform other) {
            // Only update if the values have changed significantly
            float thisTime = (float) MusicHud.getRunningMillis() / 1000;
            float otherTime = other.getLastTime();
            float thisProgress = NOW_PLAYING_INFO.getProgressRate();
            float otherProgress = other.getLastProgress();
            return Math.abs(thisTime - otherTime) < 0.05f && Math.abs(thisProgress - otherProgress) < 0.001f;
        }
        return false;
    }

    private float lastTime = 0;
    private float lastProgress = 0;

    public float getLastTime() { return lastTime; }
    public float getLastProgress() { return lastProgress; }

    @Override
    public void write(Std140Builder builder) {
        lastTime = (float) MusicHud.getRunningMillis() / 1000;
        lastProgress = NOW_PLAYING_INFO.getProgressRate();
        builder.putVec4(
                lastTime,
                lastProgress,
                transitionable == null ? 0 : transitionable.getProgress(),
                0
        );
    }
}
