package eu.pb4.sgui.mixin;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {
    @Accessor("textColor")
    void setTextColorNoUpdate(DyeColor color);
}
