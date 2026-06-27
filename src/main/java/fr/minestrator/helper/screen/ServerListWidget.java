package fr.minestrator.helper.screen;

import fr.minestrator.helper.api.BoxInfo;
import fr.minestrator.helper.api.ServerInfo;
import fr.minestrator.helper.api.ServerLiveData;
import net.minecraft.client.Minecraft;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} else {
import net.minecraft.client.gui.GuiGraphics;
//?}
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ServerListWidget extends ObjectSelectionList<ServerListWidget.Entry> {

    private final List<ServerEntry> serverEntries = new ArrayList<>();

    public ServerListWidget(Minecraft client, int screenWidth, int screenHeight, int top, int bottom, int itemHeight) {
        //? if >=1.21 {
        super(client, screenWidth, bottom - top, top, itemHeight);
        //?} else
        /*super(client, screenWidth, screenHeight, top, bottom, itemHeight);*/
    }

    public void setBoxes(List<BoxInfo> boxes) {
        this.clearEntries();
        this.serverEntries.clear();

        for (BoxInfo box : boxes) {
            this.addEntry(new BoxHeaderEntry(box));
            for (ServerInfo server : box.getServers()) {
                ServerEntry entry = new ServerEntry(server, box);
                this.addEntry(entry);
                this.serverEntries.add(entry);
            }
        }

        refreshLiveData();
    }

    public void refreshLiveData() {
        for (ServerEntry entry : serverEntries) {
            entry.fetchLiveData();
        }
    }

    @Override
    public void setSelected(Entry entry) {
        if (entry instanceof ServerEntry) {
            super.setSelected(entry);
            if (this.minecraft.screen instanceof HostedServersScreen screen) {
                screen.updateButtonStates();
            }
        }
    }

    public ServerEntry getSelectedServer() {
        Entry entry = this.getSelected();
        if (entry instanceof ServerEntry serverEntry) {
            return serverEntry;
        }
        return null;
    }

    @Override
    public int getRowWidth() {
        return this.width - 20;
    }

    //? if <1.21 {
    /*@Override
    protected int getScrollbarPosition() {
        return this.width - 6;
    }
    *///?}

    public abstract static class Entry extends ObjectSelectionList.Entry<Entry> {
    }

    // Box header entry (not selectable)
    public class BoxHeaderEntry extends Entry {
        private final BoxInfo box;

        public BoxHeaderEntry(BoxInfo box) {
            this.box = box;
        }

        //? if >=26.1 {
        /*@Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderRow(context, getX(), getY(), getWidth(), getHeight());
        }
        *///?} else if >=1.21.11 {
        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderRow(context, getX(), getY(), getWidth(), getHeight());
        }
        //?} else {
        /*@Override
        public void render(GuiGraphics context, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderRow(context, left, top, width, height);
        }
        *///?}

        //? if >=26.1 {
        /*private void renderRow(GuiGraphicsExtractor context, int x, int y, int w, int h) {
        *///?} else {
        private void renderRow(GuiGraphics context, int x, int y, int w, int h) {
        //?}
            context.fill(x, y, x + w, y + h, 0x60404040);

            Minecraft mc = Minecraft.getInstance();

            Gfx.text(context, mc.font, box.getName(), x + 5, y + 4, 0xFFFFAA00, true);

            String offerText = box.getOffer() + " - " + box.getRam() + "GB RAM";
            Gfx.text(context, mc.font, offerText, x + 5, y + 16, 0xFF888888, false);

            String infoText = box.getHashSupport() + " - " + box.getTendDays() + "j";
            int infoWidth = mc.font.width(infoText);
            Gfx.text(context, mc.font, infoText, x + w - infoWidth - 10, y + 10, 0xFF666666, false);
        }

        //? if >=1.21.11 {
        @Override
        public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
            return false;
        }
        //?} else {
        /*@Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return false;
        }
        *///?}

        @Override
        public Component getNarration() {
            return Component.literal(box.getName());
        }
    }

    // Server entry (selectable) - uses ServerEntryData for logic
    public class ServerEntry extends Entry {
        private final ServerEntryData data;

        public ServerEntry(ServerInfo server, BoxInfo box) {
            this.data = new ServerEntryData(server, box);
        }

        public ServerInfo getServerInfo() {
            return data.getServer();
        }

        public BoxInfo getBoxInfo() {
            return data.getBox();
        }

        public ServerLiveData getLiveData() {
            return data.getLiveData();
        }

        public void fetchLiveData() {
            data.fetchLiveData(liveData -> Minecraft.getInstance().execute(() -> {
                // Trigger UI update if needed
            }));
        }

        //? if >=26.1 {
        /*@Override
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderRow(context, getX(), getY(), getWidth());
        }
        *///?} else if >=1.21.11 {
        @Override
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderRow(context, getX(), getY(), getWidth());
        }
        //?} else {
        /*@Override
        public void render(GuiGraphics context, int index, int top, int left, int width, int height,
                           int mouseX, int mouseY, boolean hovering, float partialTick) {
            renderRow(context, left, top, width);
        }
        *///?}

        //? if >=26.1 {
        /*private void renderRow(GuiGraphicsExtractor context, int x, int y, int w) {
        *///?} else {
        private void renderRow(GuiGraphics context, int x, int y, int w) {
        //?}
            int indent = 15;

            Minecraft mc = Minecraft.getInstance();
            ServerInfo server = data.getServer();

            context.fill(x + 3, y + 6, x + 9, y + 12, data.getStateColor());

            Gfx.text(context, mc.font, server.getName(), x + indent, y + 4, data.getNameColor(), false);

            Gfx.text(context, mc.font, server.getConnectionAddress(), x + indent, y + 16, 0xFFAAAAAA, false);

            String versionText = data.getVersionText();
            if (!versionText.isEmpty()) {
                Gfx.text(context, mc.font, versionText, x + indent, y + 28, 0xFF666666, false);
            } else {
                Gfx.text(context, mc.font, server.getHashSupport(), x + indent, y + 28, 0xFF666666, false);
            }

            String statusKey = data.getStatusKey();
            String statusText = statusKey != null ? Component.translatable(statusKey).getString() : "...";
            int statusWidth = mc.font.width(statusText);
            Gfx.text(context, mc.font, statusText, x + w - statusWidth - 10, y + 4, data.getStatusColor(), false);

            String playersText = data.getPlayersText();
            if (!playersText.isEmpty()) {
                int playersWidth = mc.font.width(playersText);
                Gfx.text(context, mc.font, playersText, x + w - playersWidth - 10, y + 16, 0xFFAAAAAA, false);
            }

            if (server.isBedrock()) {
                Gfx.text(context, mc.font, "Bedrock", x + w - 50, y + 28, 0xFF55FFFF, false);
            }
        }

        //? if >=1.21.11 {
        @Override
        public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
            ServerListWidget.this.setSelected(this);
            return true;
        }
        //?} else {
        /*@Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            ServerListWidget.this.setSelected(this);
            return true;
        }
        *///?}

        @Override
        public Component getNarration() {
            return Component.literal(data.getServer().getName());
        }
    }
}
