package eu.pb4.sgui.api.gui;

import eu.pb4.sgui.api.ClickActionType;
import eu.pb4.sgui.api.SlotHolder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.world.inventory.ClickType;
import org.jetbrains.annotations.ApiStatus;

public interface SlotGuiInterface extends SlotHolder, GuiInterface {

    /**
     * Returns the number of slots in the inventory.
     *
     * @return the inventory size
     */
    int getSize();

    boolean getLockPlayerInventory();
    void setLockPlayerInventory(boolean value);

    /**
     * Used internally to receive clicks from the client.
     *
     * @see SlotGuiInterface#onClick(int, ClickActionType, net.minecraft.world.inventory.ClickType, GuiElementInterface)
     * @see SlotGuiInterface#onAnyClick(int, ClickActionType, net.minecraft.world.inventory.ClickType)
     */
    @ApiStatus.Internal
    default boolean click(int index, ClickActionType type, ClickType action) {
        GuiElementInterface element = this.getSlot(index);
        if (element != null) {
            element.getGuiCallback().click(index, type, action, this);
        }
        return this.onClick(index, type, action, element);
    }

    /**
     * Executes when player clicks any slot.
     *
     * @param index  the slot index
     * @param type   the simplified type of click
     * @param action Minecraft's Slot Action Type
     * @return <code>true</code> if to allow manipulation of redirected slots, otherwise <code>false</code>
     */
    default boolean onAnyClick(int index, ClickActionType type, ClickType action) {
        return true;
    }

    /**
     * Executed when player clicks a {@link GuiElementInterface}
     *
     * @param index   slot index
     * @param type    Simplified type of click
     * @param action  Minecraft's Slot Action Type
     * @param element Clicked GuiElement
     * @return Returns false, for automatic handling and syncing or true, if you want to do it manually
     */
    default boolean onClick(int index, ClickActionType type, ClickType action, GuiElementInterface element) {
        return false;
    }
}
