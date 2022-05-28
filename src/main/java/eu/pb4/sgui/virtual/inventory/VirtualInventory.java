package eu.pb4.sgui.virtual.inventory;

import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public record VirtualInventory(SlotGuiInterface gui) implements Container {

    @Override
    public int getContainerSize() {
        return this.gui.getSize();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int index) {
        Slot slot = this.gui.getSlotRedirect(index);
        if (slot != null) {
            return slot.getItem();
        } else {
            GuiElementInterface element = this.gui.getSlot(index);
            if (element == null) {
                return ItemStack.EMPTY;
            }
            return element.getItemStackForDisplay(this.gui);
        }
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        Slot slot = this.gui.getSlotRedirect(index);
        if (slot != null) {
            return slot.container.removeItem(index, count);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        Slot slot = this.gui.getSlotRedirect(index);
        if (slot != null) {
            return slot.container.removeItemNoUpdate(index);
        }
        return ItemStack.EMPTY;
    }


    @Override
    public void setItem(int index, ItemStack stack) {
        Slot slot = this.gui.getSlotRedirect(index);
        if (slot != null) {
            slot.container.setItem(index, stack);
        }
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
