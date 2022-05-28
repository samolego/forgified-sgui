package eu.pb4.sgui.api.gui.layered;

import eu.pb4.sgui.api.ClickActionType;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;

class BackendSimpleGui extends SimpleGui {
    public final LayeredGui gui;

    public BackendSimpleGui(MenuType<?> type, ServerPlayer player, boolean manipulatePlayerSlots, LayeredGui gui) {
        super(type, player, manipulatePlayerSlots);
        this.gui = gui;
    }

    @Override
    public boolean onAnyClick(int index, ClickActionType type, ClickType action) {
        return this.gui.onAnyClick(index, type, action);
    }
    @Override
    public boolean onClick(int index, ClickActionType type, ClickType action, GuiElementInterface element) {
        return this.gui.onClick(index, type, action, element);
    }

    @Override
    public void onClose() {
        this.gui.onClose();
    }

    @Override
    public void onTick() {
        this.gui.onTick();
    }

    @Override
    public void onUpdate(boolean firstUpdate) {
        this.gui.onUpdate(firstUpdate);
    }

    @Override
    public void onOpen() {
        this.gui.onOpen();
    }
}
