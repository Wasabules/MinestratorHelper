package fr.minestrator.helper.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConsoleCommandScreen extends Screen {
    private final ConsoleCommandLogic logic;
    private EditBox commandField;
    private Button sendButton;

    public ConsoleCommandScreen() {
        super(Component.translatable("minestratorhelper.console.title"));
        this.logic = new ConsoleCommandLogic();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.commandField = new EditBox(
                this.font,
                centerX - 150,
                centerY - 10,
                250,
                20,
                Component.translatable("minestratorhelper.console.placeholder")
        );
        this.commandField.setMaxLength(256);
        this.commandField.setFocused(true);
        this.addRenderableWidget(this.commandField);

        this.sendButton = Button.builder(
                Component.translatable("minestratorhelper.console.send"),
                button -> sendCommand()
        ).bounds(centerX + 105, centerY - 10, 60, 20).build();
        this.addRenderableWidget(this.sendButton);

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> this.onClose()
        ).bounds(centerX - 40, centerY + 20, 80, 20).build());

        this.setInitialFocus(this.commandField);
    }

    private void sendCommand() {
        this.sendButton.active = false;
        this.sendButton.setMessage(Component.translatable("minestratorhelper.console.sending"));

        logic.sendCommand(
                this.commandField.getValue(),
                () -> this.minecraft.execute(() -> this.commandField.setValue("")),
                error -> {},
                () -> this.minecraft.execute(() -> {
                    this.sendButton.active = true;
                    this.sendButton.setMessage(Component.translatable("minestratorhelper.console.send"));
                })
        );
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        context.fill(centerX - 170, centerY - 50, centerX + 180, centerY + 55, 0xCC000000);

        int borderColor = 0xFF555555;
        context.fill(centerX - 170, centerY - 50, centerX + 180, centerY - 49, borderColor);
        context.fill(centerX - 170, centerY + 54, centerX + 180, centerY + 55, borderColor);
        context.fill(centerX - 170, centerY - 50, centerX - 169, centerY + 55, borderColor);
        context.fill(centerX + 179, centerY - 50, centerX + 180, centerY + 55, borderColor);

        String titleStr = this.title.getString();
        context.drawString(this.font, titleStr,
                centerX - this.font.width(titleStr) / 2, centerY - 40, 0xFFFFFF, true);

        String serverName = logic.getServerName();
        if (serverName != null) {
            String serverText = "Serveur: " + serverName;
            context.drawString(this.font, serverText,
                    centerX - this.font.width(serverText) / 2, centerY - 28, 0xAAAAAA, false);
        }

        if (logic.shouldShowStatus()) {
            String statusMessage = Component.translatable(logic.getStatusMessage()).getString();
            context.drawString(this.font, statusMessage,
                    centerX - this.font.width(statusMessage) / 2, centerY + 45,
                    logic.isStatusSuccess() ? 0x55FF55 : 0xFF5555, false);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(null);
    }
}
