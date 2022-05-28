package eu.pb4.sgui.virtual.inventory;

import eu.pb4.sgui.api.gui.SlotGuiInterface;
import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class VirtualScreenHandler extends AbstractContainerMenu implements VirtualScreenHandlerInterface {
    private final SlotGuiInterface gui;
    public final VirtualInventory inventory;

    public VirtualScreenHandler(@Nullable MenuType<?> type, int syncId, SlotGuiInterface gui, Player player) {
        super(type, syncId);
        this.gui = gui;

        this.inventory = new VirtualInventory(gui);
        setupSlots(player);
    }

    protected void setupSlots(Player player) {
        int n;
        int m;

        for (n = 0; n < this.gui.getVirtualSize(); ++n) {
            Slot slot = this.gui.getSlotRedirect(n);
            if (slot != null) {
                this.addSlot(slot);
            } else {
                this.addSlot(new VirtualSlot(inventory, n, 0, 0));
            }
        }

        if (gui.isIncludingPlayer()) {
            int size = this.gui.getHeight() * this.gui.getWidth();
            for (n = 0; n < 4; ++n) {
                for (m = 0; m < 9; ++m) {
                    this.addSlot(new VirtualSlot(
                            inventory, m + n * 9 + size, 0, 0));
                }
            }
        } else {
            Inventory playerInventory = player.getInventory();
            for (n = 0; n < 3; ++n) {
                for (m = 0; m < 9; ++m) {
                    this.addSlot(new Slot(playerInventory, m + n * 9 + 9, 0, 0));
                }
            }

            for (n = 0; n < 9; ++n) {
                this.addSlot(new Slot(playerInventory, n, 0, 0));
            }
        }
    }

    @Override
    public SlotGuiInterface getGui() {
        return this.gui;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void setItem(int slot, int i, ItemStack stack) {
        if (this.gui.getSize() <= slot) {
            this.getSlot(slot).set(stack);
        } else {
            this.getSlot(slot).set(ItemStack.EMPTY);
        }
    }

    @Override
    public void broadcastChanges() {
        try {
            this.gui.onTick();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem() && !(slot instanceof VirtualSlot)) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (index < this.gui.getSize()) {
                if (!this.moveItemStackTo(itemStack2, this.gui.getSize(), player.getInventory().items.size() + this.gui.getSize(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, this.gui.getSize(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        } else if (slot instanceof VirtualSlot) {
            return slot.getItem();
        }

        return itemStack;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return !(slot instanceof VirtualSlot) && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public Slot addSlot(Slot slot) {
        return super.addSlot(slot);
    }

    public void setSlot(int index, Slot slot) {
        this.slots.set(index, slot);
    }

    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        boolean bl = false;
        int i = startIndex;
        if (fromLast) {
            i = endIndex - 1;
        }

        Slot slot2;
        ItemStack itemStack;
        if (stack.isStackable()) {
            while(!stack.isEmpty()) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot2 = this.slots.get(i);

                itemStack = slot2.getItem();

                if (!(slot2 instanceof VirtualSlot) && stack != itemStack && !itemStack.isEmpty() && ItemStack.isSameItemSameTags(stack, itemStack)) {
                    int j = itemStack.getCount() + stack.getCount();
                    if (j <= stack.getMaxStackSize()) {
                        stack.setCount(0);
                        itemStack.setCount(j);
                        slot2.setChanged();
                        bl = true;
                    } else if (itemStack.getCount() < stack.getMaxStackSize()) {
                        stack.shrink(stack.getMaxStackSize() - itemStack.getCount());
                        itemStack.setCount(stack.getMaxStackSize());
                        slot2.setChanged();
                        bl = true;
                    }
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        if (!stack.isEmpty()) {
            if (fromLast) {
                i = endIndex - 1;
            } else {
                i = startIndex;
            }

            while(true) {
                if (fromLast) {
                    if (i < startIndex) {
                        break;
                    }
                } else if (i >= endIndex) {
                    break;
                }

                slot2 = this.slots.get(i);
                itemStack = slot2.getItem();
                if (itemStack.isEmpty() && slot2.mayPlace(stack)) {
                    if (stack.getCount() > slot2.getMaxStackSize()) {
                        slot2.set(stack.split(slot2.getMaxStackSize()));
                    } else {
                        slot2.set(stack.split(stack.getCount()));
                    }

                    slot2.setChanged();
                    bl = true;
                    break;
                }

                if (fromLast) {
                    --i;
                } else {
                    ++i;
                }
            }
        }

        return bl;
    }
}
