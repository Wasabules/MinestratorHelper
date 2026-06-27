package fr.minestrator.helper.screen;

//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {
    private final Screen parent;
    private final ConfigLogic logic;
    private EditBox tokenField;
    private Button testButton;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("minestratorhelper.config.title"));
        this.parent = parent;
        this.logic = new ConfigLogic();
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = this.height / 4;

        this.tokenField = new EditBox(
                this.font,
                centerX - 150, startY + 20,
                300, 20,
                Component.translatable("minestratorhelper.config.token")
        );
        this.tokenField.setMaxLength(512);
        this.tokenField.setValue(logic.getCurrentToken());
        this.addRenderableWidget(this.tokenField);

        this.testButton = Button.builder(
                Component.translatable("minestratorhelper.config.test"),
                button -> testConnection()
        ).bounds(centerX - 100, startY + 55, 200, 20).build();
        this.addRenderableWidget(this.testButton);

        this.addRenderableWidget(Button.builder(
                Component.translatable("minestratorhelper.config.save"),
                button -> {
                    logic.saveToken(this.tokenField.getValue());
                    this.minecraft.setScreen(this.parent);
                }
        ).bounds(centerX - 100, startY + 90, 200, 20).build());

        this.addRenderableWidget(Button.builder(
                Component.translatable("minestratorhelper.config.cancel"),
                button -> this.minecraft.setScreen(this.parent)
        ).bounds(centerX - 100, startY + 115, 200, 20).build());
    }

    private void testConnection() {
        this.testButton.active = false;
        logic.testConnection(
                this.tokenField.getValue(),
                user -> this.minecraft.execute(() -> this.testButton.active = true),
                error -> this.minecraft.execute(() -> this.testButton.active = true),
                null
        );
    }

    //? if >=26.1 {
    /*@Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    *///?} else {
    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
    //?}
        //? if <1.21
        /*this.renderBackground(context);*/
        //? if >=26.1 {
        /*super.extractRenderState(context, mouseX, mouseY, delta);
        *///?} else {
        super.render(context, mouseX, mouseY, delta);
        //?}

        int centerX = this.width / 2;
        int startY = this.height / 4;

        String titleStr = this.title.getString();
        Gfx.text(context, this.font, titleStr,
                centerX - this.font.width(titleStr) / 2, 20, 0xFFFFFFFF, true);

        String apiStr = "API: " + logic.getApiBaseUrl();
        Gfx.text(context, this.font, apiStr,
                centerX - this.font.width(apiStr) / 2, startY - 10, 0xFF808080, true);

        String tokenLabel = Component.translatable("minestratorhelper.config.token").getString();
        Gfx.text(context, this.font, tokenLabel, centerX - 150, startY + 8, 0xFFA0A0A0, true);

        String statusMessage = logic.getStatusMessage();
        if (statusMessage != null) {
            String statusArg = logic.getStatusArg();
            String displayMessage = statusArg != null
                    ? Component.translatable(statusMessage, statusArg).getString()
                    : Component.translatable(statusMessage).getString();
            Gfx.text(context, this.font, displayMessage,
                    centerX - this.font.width(displayMessage) / 2, startY + 145,
                    logic.isStatusSuccess() ? 0xFF55FF55 : 0xFFFF5555, true);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
