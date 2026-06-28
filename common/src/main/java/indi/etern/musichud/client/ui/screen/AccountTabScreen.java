package indi.etern.musichud.client.ui.screen;

import indi.etern.musichud.MusicHud;
import indi.etern.musichud.client.services.LoginService;
import indi.etern.musichud.interfaces.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class AccountTabScreen extends Screen {
    private final MusicHudScreen parent;
    private LoginService loginService;
    private ClientConfig clientConfig;
    private LoginState state = LoginState.MAIN;
    private EditBox inputBox1;
    private EditBox inputBox2;

    public enum LoginState {
        MAIN,
        QR_LOGIN,
        PHONE_CODE_INPUT,
        PHONE_CODE_VERIFY,
        PASSWORD_LOGIN
    }

    public AccountTabScreen(MusicHudScreen parent) {
        super(Component.empty());
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.loginService = LoginService.getInstance();
        this.clientConfig = ClientConfig.getInstance();
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearWidgets();
        int centerX = width / 2;
        int y = 10;

        if (state == LoginState.MAIN) {
            renderMainMenu(centerX, y);
        } else if (state == LoginState.QR_LOGIN) {
            renderQRLogin(centerX, y);
        } else if (state == LoginState.PHONE_CODE_INPUT) {
            renderPhoneCodeInput(centerX, y);
        } else if (state == LoginState.PHONE_CODE_VERIFY) {
            renderPhoneCodeVerify(centerX, y);
        } else if (state == LoginState.PASSWORD_LOGIN) {
            renderPasswordLogin(centerX, y);
        }
    }

    private void renderMainMenu(int centerX, int y) {
        boolean logined = loginService.isLogined();

        if (logined) {
            addRenderableWidget(Button.builder(Component.literal("Logged in as: " + LoginService.getInstance().getProfile()), button -> {}).bounds(centerX - 150, y, 300, 20).build());
            y += 24;
            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.logout"), button -> {
                loginService.logout();
                state = LoginState.MAIN;
                rebuildWidgets();
            }).bounds(centerX - 150, y, 300, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.qrLogin"), button -> {
                state = LoginState.QR_LOGIN;
                loginService.startQRLogin(response -> {
                    Minecraft.getInstance().execute(() -> rebuildWidgets());
                });
                rebuildWidgets();
            }).bounds(centerX - 150, y, 300, 20).build());
            y += 24;

            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.phoneCodeLogin"), button -> {
                state = LoginState.PHONE_CODE_INPUT;
                rebuildWidgets();
            }).bounds(centerX - 150, y, 300, 20).build());
            y += 24;

            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.passwordLogin"), button -> {
                state = LoginState.PASSWORD_LOGIN;
                rebuildWidgets();
            }).bounds(centerX - 150, y, 300, 20).build());
            y += 24;

            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.anonymousLogin"), button -> {
                loginService.loginAsAnonymous();
            }).bounds(centerX - 150, y, 300, 20).build());
        }
    }

    private void renderQRLogin(int centerX, int y) {
        String qrUrl = loginService.getQRCodeUrl();
        if (qrUrl != null) {
            addRenderableWidget(Button.builder(Component.literal("QR Code: " + qrUrl.substring(0, Math.min(40, qrUrl.length())) + "..."), button -> {}).bounds(centerX - 150, y, 300, 20).build());
            y += 24;
            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.checkQRStatus"), button -> {
                loginService.checkQRLoginStatus(response -> {
                    Minecraft.getInstance().execute(() -> {
                        if (response.success()) {
                            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.loginSuccess"));
                            state = LoginState.MAIN;
                        } else {
                            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.qrLoginFailed") + ": " + response.message());
                        }
                        rebuildWidgets();
                    });
                });
            }).bounds(centerX - 150, y, 300, 20).build());
        } else {
            addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".text.loadingQRCode"), button -> {}).bounds(centerX - 150, y, 300, 20).build());
        }
        y += 24;
        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> {
            state = LoginState.MAIN;
            rebuildWidgets();
        }).bounds(centerX - 150, y, 300, 20).build());
    }

    private void renderPhoneCodeInput(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".label.phoneNumber"), button -> {}).bounds(centerX - 150, y, 300, 20).build());
        y += 24;

        inputBox1 = new EditBox(font, centerX - 150, y, 300, 20, Component.translatable(MusicHud.MOD_ID + ".label.phoneNumber"));
        inputBox1.setResponder(text -> {});
        addRenderableWidget(inputBox1);
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.sendCode"), button -> {
            String phone = inputBox1.getValue().trim();
            if (!phone.isEmpty()) {
                loginService.sendPhoneCode(phone, response -> {
                    Minecraft.getInstance().execute(() -> {
                        if (response.success()) {
                            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.codeSent"));
                            state = LoginState.PHONE_CODE_VERIFY;
                        } else {
                            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.sendCodeFailed") + ": " + response.message());
                        }
                        rebuildWidgets();
                    });
                });
            }
        }).bounds(centerX - 150, y, 300, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> {
            state = LoginState.MAIN;
            rebuildWidgets();
        }).bounds(centerX - 150, y, 300, 20).build());
    }

    private void renderPhoneCodeVerify(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".label.verificationCode"), button -> {}).bounds(centerX - 150, y, 300, 20).build());
        y += 24;

        inputBox1 = new EditBox(font, centerX - 150, y, 300, 20, Component.translatable(MusicHud.MOD_ID + ".label.verificationCode"));
        inputBox1.setResponder(text -> {});
        addRenderableWidget(inputBox1);
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.verify"), button -> {
            String code = inputBox1.getValue().trim();
            if (!code.isEmpty()) {
                loginService.verifyPhoneCode(code, response -> {
                    Minecraft.getInstance().execute(() -> {
                        if (response.success()) {
                            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.loginSuccess"));
                            state = LoginState.MAIN;
                        } else {
                            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.verifyFailed") + ": " + response.message());
                        }
                        rebuildWidgets();
                    });
                });
            }
        }).bounds(centerX - 150, y, 300, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> {
            state = LoginState.MAIN;
            rebuildWidgets();
        }).bounds(centerX - 150, y, 300, 20).build());
    }

    private void renderPasswordLogin(int centerX, int y) {
        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".label.phoneNumber"), button -> {}).bounds(centerX - 150, y, 300, 20).build());
        y += 24;

        inputBox1 = new EditBox(font, centerX - 150, y, 300, 20, Component.translatable(MusicHud.MOD_ID + ".label.phoneNumber"));
        inputBox1.setResponder(text -> {});
        addRenderableWidget(inputBox1);
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".label.password"), button -> {}).bounds(centerX - 150, y, 300, 20).build());
        y += 24;

        inputBox2 = new EditBox(font, centerX - 150, y, 300, 20, Component.translatable(MusicHud.MOD_ID + ".label.password"));
        inputBox2.setResponder(text -> {});
        addRenderableWidget(inputBox2);
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable(MusicHud.MOD_ID + ".button.login"), button -> {
            String phone = inputBox1.getValue().trim();
            String password = inputBox2.getValue();
            if (!phone.isEmpty() && !password.isEmpty()) {
                loginService.loginWithPassword(phone, password, response -> {
                    Minecraft.getInstance().execute(() -> {
                        if (response.success()) {
                            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.loginSuccess"));
                            state = LoginState.MAIN;
                        } else {
                            ToastUtil.show(I18n.get(MusicHud.MOD_ID + ".text.loginFailed") + ": " + response.message());
                        }
                        rebuildWidgets();
                    });
                });
            }
        }).bounds(centerX - 150, y, 300, 20).build());
        y += 24;

        addRenderableWidget(Button.builder(Component.translatable("gui.back"), button -> {
            state = LoginState.MAIN;
            rebuildWidgets();
        }).bounds(centerX - 150, y, 300, 20).build());
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