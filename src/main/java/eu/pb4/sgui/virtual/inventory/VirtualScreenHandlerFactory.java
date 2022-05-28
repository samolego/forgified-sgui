package eu.pb4.sgui.virtual.inventory;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public record VirtualScreenHandlerFactory(SlotGuiInterface gui) implements MenuProvider {

    @Override
    public Component getDisplayName() {
        Component text = this.gui.getTitle();
        if (text == null) {
            text = new TextComponent("");
        }
        return text;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new VirtualScreenHandler(this.gui.getType(), syncId, this.gui, player);
    }
}
