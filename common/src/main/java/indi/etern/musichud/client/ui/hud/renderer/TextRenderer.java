package indi.etern.musichud.client.ui.hud.renderer;

import indi.etern.musichud.client.ui.hud.metadata.Layout;
import indi.etern.musichud.client.ui.utils.Easing;
import indi.etern.musichud.interfaces.ClientConfig;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class TextRenderer implements HudRenderer {
    private static final ClientConfig clientConfig = ClientConfig.getInstance();
    private static final Map<String, Float> WIDTH_CACHE = new ConcurrentHashMap<>(64);
    private int vanillaLineHeight = -1;
    private TextStyle currentTextData;
    private Layout layout;
    private int baseColor;
    private Position position;
    private int marqueeIntervalMillis = 5000;
    private Easing marqueeStartAndEndUpSpeedEasing = Easing.EASE_IN_OUT_SINE;
    private float marqueeSpaceWeight = 0.4f;
    private TextStyle nextTextData;
    private float transitionProgress = 1.0f;
    private boolean isTransitioning = false;
    private float transitionSpeed = 4.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private float marqueeDuration = 10000;

    public void configure(Layout layout, int baseColor, Position position) {
        this.layout = layout;
        this.baseColor = baseColor;
        this.position = position;
    }

    public void setText(String text) {
        if (text == null) {
            text = "";
        }

        if (currentTextData == null) {
            currentTextData = new TextStyle(text, baseColor);
            transitionProgress = 1.0f;
            isTransitioning = false;
            nextTextData = null;
        } else if (text.equals(currentTextData.text)) {
            if (isTransitioning) {
                currentTextData.text = text;
                transitionProgress = 1.0f;
                isTransitioning = false;
                nextTextData = null;
            }
        } else {
            if (isTransitioning) {
                if (nextTextData == null || !text.equals(nextTextData.text)) {
                    if (nextTextData != null) {
                        currentTextData = nextTextData;
                    }
                    nextTextData = new TextStyle(text, baseColor);
                    transitionProgress = 0.0f;
                    lastUpdateTime = System.currentTimeMillis();
                }
            } else {
                nextTextData = new TextStyle(text, baseColor);
                transitionProgress = 0.0f;
                isTransitioning = true;
                lastUpdateTime = System.currentTimeMillis();
            }
        }
    }

    private void updateTransition() {
        if (!isTransitioning) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;

        transitionProgress += deltaTime * transitionSpeed;

        if (transitionProgress >= 1.0f) {
            transitionProgress = 1.0f;
            if (nextTextData != null) {
                currentTextData = nextTextData;
            }
            nextTextData = null;
            isTransitioning = false;
        }
    }

    public void render(HudRenderContext context) {
        updateTransition();

        if (currentTextData == null || layout.getHeight() <= 0 || layout.getWidth() <= 0) {
            return;
        }

        if (vanillaLineHeight < 0) {
            vanillaLineHeight = Minecraft.getInstance().font.lineHeight;
        }

        float scale = layout.getHeight() / vanillaLineHeight;

        Layout.AbsolutePosition absolutePosition = layout.calcAbsolutePosition(context);

        if (!isTransitioning || nextTextData == null) {
            renderText(context, currentTextData, absolutePosition, scale, 1.0f);
        } else {
            float oldAlpha = 1.0f - transitionProgress;
            if (oldAlpha > 0) {
                renderText(context, currentTextData, absolutePosition, scale, oldAlpha);
            }

            float newAlpha = transitionProgress;
            if (newAlpha > 0) {
                renderText(context, nextTextData, absolutePosition, scale, newAlpha);
            }
        }
    }

    private void renderText(HudRenderContext context, TextStyle textData, Layout.AbsolutePosition absolutePosition,
                            float scale, float alpha) {
        String text = textData.text;
        if (text == null || text.isEmpty()) return;

        int color = getColorWithAlpha(textData.baseColor, alpha);

        float measuredWidth = textData.getCachedWidth();
        float textRenderWidth = scale * measuredWidth;
        float layoutWidth = layout.getWidth();
        float x = position.computeX(absolutePosition.x(), text, Math.min(textRenderWidth, layoutWidth));
        float y = absolutePosition.y();
        boolean overflow = textRenderWidth > layoutWidth;

        float x1 = x;
        float marqueeWidth = 0;
        float marqueeOffset = 0;
        boolean enableMarqueeText = clientConfig.getEnableMarqueeText();
        if (enableMarqueeText && position == Position.LEFT) {
            marqueeWidth = textRenderWidth + layoutWidth * marqueeSpaceWeight;
            long elapsedTime = System.currentTimeMillis() - lastUpdateTime;
            float marqueeElapsedTime = Math.max(0, elapsedTime % (marqueeDuration + marqueeIntervalMillis) - marqueeIntervalMillis);
            float marqueeProgress = marqueeStartAndEndUpSpeedEasing.getInterpolation(marqueeElapsedTime / marqueeDuration);
            marqueeOffset = overflow ? marqueeProgress * marqueeWidth : 0;
            x1 -= marqueeOffset;
        } else {
            float maxWidth = layout.getWidth() / scale;
            text = trimToWidth(text, maxWidth);
            if (text.isEmpty()) return;
        }
        context.pushScissor((int) x, (int) y, (int) (x + layoutWidth), (int) (y + layout.getHeight() + 1));
        HudRenderContext.Transforming transform = context.transform();
        String finalText = text;
        transform.translate(x1, y)
                .subTransform(transforming -> {
                    transforming.scale(scale)
                            .then(transforming1 -> {
                                context.drawString(Minecraft.getInstance().font, finalText, 0, 0, color, false);
                            });
                });
        if (overflow && enableMarqueeText) {
            if (marqueeWidth - marqueeOffset < layoutWidth) {
                transform.translate(marqueeWidth, 0)
                        .subTransform(transforming -> {
                            transforming.scale(scale)
                                    .then(transforming1 -> {
                                        context.drawString(Minecraft.getInstance().font, finalText, 0, 0, color, false);
                                    });
                        });
            }
        }
        transform.end();
        context.popScissor();
    }

    private int getColorWithAlpha(int baseColor, float alpha) {
        float a = ((baseColor >> 24) & 0xff) / 255.0f;
        int alphaValue = (int) (a * alpha * 255);
        alphaValue = Math.clamp(alphaValue, 0, 255);
        return (alphaValue << 24) | (baseColor & 0x00FFFFFF);
    }

    public float calcDisplayWidth() {
        if (currentTextData == null || currentTextData.text == null || currentTextData.text.isEmpty()) {
            return 0f;
        } else {
            return Math.min(layout.getWidth(), currentTextData.getCachedWidth() * (layout.getHeight() / vanillaLineHeight));
        }
    }

    private String trimToWidth(String text, float maxWidth) {
        return trimWithVanilla(text, maxWidth);
    }

    private String trimWithVanilla(String text, float maxWidth) {
        float width = 0;
        int index = 0;
        final int len = text.length();
        while (index < len) {
            int codePoint = text.codePointAt(index);
            int cpLen = Character.charCount(codePoint);
            String cpStr = new String(new int[]{codePoint}, 0, 1);
            int w = getCachedCharWidth(cpStr);
            if (width + w > maxWidth) {
                break;
            }
            width += w;
            index += cpLen;
        }

        String trimmed = text.substring(0, index);
        if (index < len) {
            trimmed = addEllipsis(trimmed);
        }
        return trimmed;
    }

    private String addEllipsis(String base) {
        if (base.length() <= 3) {
            return "";
        }
        int cut = Math.max(0, base.length() - 3);
        return base.substring(0, cut) + "...";
    }

    private float measureWidth(String text) {
        return WIDTH_CACHE.computeIfAbsent(text, t -> (float) Minecraft.getInstance().font.width(t));
    }

    private int getCachedCharWidth(String charStr) {
        return WIDTH_CACHE.computeIfAbsent(charStr, t -> (float) Minecraft.getInstance().font.width(t)).intValue();
    }

    public enum Position {
        LEFT {
            @Override
            float computeX(float startX, String text, float scaledMeasuredWidth) {
                return startX;
            }
        }, CENTER {
            @Override
            float computeX(float startX, String text, float scaledMeasuredWidth) {
                return startX - 0.5f * scaledMeasuredWidth;
            }
        }, RIGHT {
            @Override
            float computeX(float startX, String text, float scaledMeasuredWidth) {
                return startX - scaledMeasuredWidth;
            }
        };

        abstract float computeX(float startX, String text, float scaledMeasuredWidth);
    }

    public static class TextStyle {
        public final int baseColor;
        public String text;
        private float cachedWidth = -1;

        public TextStyle(String text, int baseColor) {
            this.text = text;
            this.baseColor = baseColor;
        }

        public float getCachedWidth() {
            if (cachedWidth < 0 && text != null && !text.isEmpty()) {
                cachedWidth = WIDTH_CACHE.computeIfAbsent(text, t -> (float) Minecraft.getInstance().font.width(t));
            }
            return cachedWidth;
        }
    }
}
