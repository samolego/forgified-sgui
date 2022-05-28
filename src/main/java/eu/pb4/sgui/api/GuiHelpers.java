package eu.pb4.sgui.api;

import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public final class GuiHelpers {
    public static void sendSlotUpdate(ServerPlayer player, int syncId, int slot, ItemStack stack, int revision) {
        player.connection.send(new ClientboundContainerSetSlotPacket(syncId, revision, slot, stack));
    }

    public static void sendSlotUpdate(ServerPlayer player, int syncId, int slot, ItemStack stack) {
        sendSlotUpdate(player, syncId, slot, stack, 0);
    }

    public static void sendPlayerScreenHandler(ServerPlayer player) {
        player.connection.send(new ClientboundContainerSetContentPacket(player.containerMenu.containerId, player.containerMenu.incrementStateId(), player.containerMenu.getItems(), player.containerMenu.getCarried()));
    }

    public static void sendPlayerInventory(ServerPlayer player) {
        player.connection.send(new ClientboundContainerSetContentPacket(player.inventoryMenu.containerId, player.inventoryMenu.incrementStateId(), player.inventoryMenu.getItems(), player.inventoryMenu.getCarried()));
    }

    public static int posToIndex(int x, int y, int height, int width) {
        return x + y * width;
    }

    public static int getHeight(MenuType<?> type) {
        if (MenuType.GENERIC_9x6.equals(type)) {
            return 6;
        } else if (MenuType.GENERIC_9x5.equals(type) || MenuType.CRAFTING.equals(type)) {
            return 5;
        } else if (MenuType.GENERIC_9x4.equals(type)) {
            return 4;
        } else if (MenuType.GENERIC_9x2.equals(type) || MenuType.ENCHANTMENT.equals(type) || MenuType.STONECUTTER.equals(type)) {
            return 2;
        } else if (MenuType.GENERIC_9x1.equals(type) || MenuType.BEACON.equals(type) || MenuType.HOPPER.equals(type) || MenuType.BREWING_STAND.equals(type)) {
            return 1;
        }

        return 3;
    }

    public static int getWidth(MenuType<?> type) {
        if (MenuType.CRAFTING.equals(type)) {
            return 2;
        } else if (MenuType.GENERIC_3x3.equals(type)) {
            return 3;
        } else if (MenuType.HOPPER.equals(type) || MenuType.BREWING_STAND.equals(type)) {
            return 5;
        } else if (MenuType.ENCHANTMENT.equals(type) || MenuType.STONECUTTER.equals(type) || MenuType.BEACON.equals(type) || MenuType.BLAST_FURNACE.equals(type) || MenuType.FURNACE.equals(type) || MenuType.SMOKER.equals(type) || MenuType.ANVIL.equals(type) || MenuType.SMITHING.equals(type) || MenuType.GRINDSTONE.equals(type) || MenuType.MERCHANT.equals(type) || MenuType.CARTOGRAPHY_TABLE.equals(type) || MenuType.LOOM.equals(type)) {
            return 1;
        }

        return 9;
    }

    private GuiHelpers() {
    }
}
