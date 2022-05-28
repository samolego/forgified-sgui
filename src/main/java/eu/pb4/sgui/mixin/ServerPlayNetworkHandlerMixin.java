package eu.pb4.sgui.mixin;

import eu.pb4.sgui.api.ClickActionType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.HotbarGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.virtual.FakeScreenHandler;
import eu.pb4.sgui.virtual.VirtualScreenHandlerInterface;
import eu.pb4.sgui.virtual.hotbar.HotbarScreenHandler;
import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;


@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Unique
    private AbstractContainerMenu sgui_previousScreen = null;

    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract void sendPacket(Packet<?> packet);

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.AFTER), cancellable = true)
    private void sgui_handleGuiClicks(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandler handler) {
            try {
                var gui = handler.getGui();

                int slot = packet.getSlotNum();
                int button = packet.getButtonNum();
                ClickActionType type = ClickActionType.toClickType(packet.getClickType(), button, slot);
                boolean ignore = gui.onAnyClick(slot, type, packet.getClickType());
                if (ignore && !handler.getGui().getLockPlayerInventory() && (slot >= handler.getGui().getSize() || slot < 0 || handler.getGui().getSlotRedirect(slot) != null)) {
                    if (type == ClickActionType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2)) {
                        GuiHelpers.sendPlayerScreenHandler(this.player);
                    }
                    return;
                }

                boolean allow = gui.click(slot, type, packet.getClickType());
                if (handler.getGui().isOpen()) {
                    if (!allow) {
                        if (slot >= 0 && slot < handler.getGui().getSize()) {
                            this.sendPacket(new ClientboundContainerSetSlotPacket(handler.containerId, handler.incrementStateId(), slot, handler.getSlot(slot).getItem()));
                        }
                        GuiHelpers.sendSlotUpdate(this.player, -1, -1, this.player.containerMenu.getCarried(), handler.getStateId());

                        if (type.numKey) {
                            int x = type.value + handler.slots.size() - 10;
                            GuiHelpers.sendSlotUpdate(player, handler.containerId, x, handler.getSlot(x).getItem(), handler.incrementStateId());
                        } else if (type == ClickActionType.MOUSE_DOUBLE_CLICK || type == ClickActionType.MOUSE_LEFT_SHIFT || type == ClickActionType.MOUSE_RIGHT_SHIFT || (type.isDragging && type.value == 2)) {
                            GuiHelpers.sendPlayerScreenHandler(this.player);
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                ci.cancel();
            }

            ci.cancel();
        }
    }

    @Inject(method = "onClickSlot", at = @At("TAIL"))
    private void sgui_resyncGui(ServerboundContainerClickPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandler) {
            try {
                int slot = packet.getSlotNum();
                int button = packet.getButtonNum();
                ClickActionType type = ClickActionType.toClickType(packet.getClickType(), button, slot);

                if (type == ClickActionType.MOUSE_DOUBLE_CLICK || (type.isDragging && type.value == 2) || type.shift) {
                    GuiHelpers.sendPlayerScreenHandler(this.player);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "onCloseHandledScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;closeScreenHandler()V", shift = At.Shift.BEFORE))
    private void sgui_storeScreenHandler(ServerboundContainerClosePacket packet, CallbackInfo info) {
        if (this.player.containerMenu instanceof VirtualScreenHandlerInterface) {
            this.sgui_previousScreen = this.player.containerMenu;
        }
    }

    @Inject(method = "onCloseHandledScreen", at = @At("TAIL"))
    private void sgui_executeClosing(ServerboundContainerClosePacket packet, CallbackInfo info) {
        try {
            if (this.sgui_previousScreen != null) {
                if (this.sgui_previousScreen instanceof VirtualScreenHandlerInterface screenHandler) {
                    screenHandler.getGui().close(true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.sgui_previousScreen = null;
    }
    

    @Inject(method = "onCraftRequest", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;updateLastActionTime()V", shift = At.Shift.BEFORE), cancellable = true)
    private void sgui_catchRecipeRequests(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandler handler && handler.getGui() instanceof SimpleGui gui) {
            try {
                gui.onCraftRequest(packet.getRecipe(), packet.isShiftDown());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Inject(method = "onUpdateSelectedSlot", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui_catchUpdateSelectedSlot(ServerboundSetCarriedItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler handler) {
            if (!handler.getGui().onSelectedSlotChange(packet.getSlot())) {
                this.sendPacket(new ClientboundSetCarriedItemPacket(handler.getGui().getSelectedSlot()));
            }
            ci.cancel();
        }
    }

    @Inject(method = "onCreativeInventoryAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui_cancelCreativeAction(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof VirtualScreenHandlerInterface) {
            ci.cancel();
        }
    }

    @Inject(method = "onHandSwing", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui_clickHandSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();
            if (!gui.onHandSwing()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerInteractItem", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui_clickWithItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();
            if (screenHandler.slotsOld != null) {
                screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                screenHandler.slotsOld.set(45, ItemStack.EMPTY);
            }
            gui.onClickItem();
            ci.cancel();
        }
    }

    @Inject(method = "onPlayerInteractBlock", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui_clickOnBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();

            if (!gui.onClickBlock(packet.getHitResult())) {
                var pos = packet.getHitResult().getBlockPos();
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }

                this.sendPacket(new ClientboundBlockUpdatePacket(pos, this.player.level.getBlockState(pos)));
                pos = pos.relative(packet.getHitResult().getDirection());
                this.sendPacket(new ClientboundBlockUpdatePacket(pos, this.player.level.getBlockState(pos)));
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerAction", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui_onPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();

            if (!gui.onPlayerAction(packet.getAction(), packet.getDirection())) {
                var pos = packet.getPos();
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }
                this.sendPacket(new ClientboundBlockUpdatePacket(pos, this.player.level.getBlockState(pos)));
                pos = pos.relative(packet.getDirection());
                this.sendPacket(new ClientboundBlockUpdatePacket(pos, this.player.level.getBlockState(pos)));
                ci.cancel();
            }
        }
    }

    @Inject(method = "onPlayerInteractEntity", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/server/world/ServerWorld;)V"), cancellable = true)
    private void sgui_clickOnEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        if (this.player.containerMenu instanceof HotbarScreenHandler screenHandler) {
            var gui = screenHandler.getGui();
            var buf = new FriendlyByteBuf(Unpooled.buffer());
            packet.write(buf);

            int entityId = buf.readVarInt();
            var type = buf.readEnum(HotbarGui.EntityInteraction.class);

            Vec3 interactionPos = null;

            switch (type) {
                case INTERACT -> buf.readVarInt();
                case INTERACT_AT -> {
                    interactionPos = new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat());
                    buf.readVarInt();
                }
            }

            var isSneaking = buf.readBoolean();

            if (!gui.onClickEntity(entityId, type, isSneaking, interactionPos)) {
                if (screenHandler.slotsOld != null) {
                    screenHandler.slotsOld.set(gui.getSelectedSlot() + 36, ItemStack.EMPTY);
                    screenHandler.slotsOld.set(45, ItemStack.EMPTY);
                }
                ci.cancel();
            }
        }
    }
}
