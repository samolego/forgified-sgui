package eu.pb4.sgui.virtual;

import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Some guis don't use screen handlers (Sign or book input)
 * This is mostly utility class to simplify implementation
 */
public class FakeScreenHandler extends AbstractContainerMenu implements VirtualScreenHandlerInterface {

    private final GuiInterface gui;

    public FakeScreenHandler(GuiInterface gui) {
        super(null, -1);
        this.gui = gui;
    }

    @Override
    public GuiInterface getGui() {
        return this.gui;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
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
}
