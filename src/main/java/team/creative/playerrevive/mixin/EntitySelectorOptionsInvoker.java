package team.creative.playerrevive.mixin;

import java.util.function.Predicate;
import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntitySelectorOptions.class)
public interface EntitySelectorOptionsInvoker {
    @Invoker("putOption")
    static void registerOption(String name, EntitySelectorOptions.SelectorHandler handler,
            Predicate<EntitySelectorReader> condition, Text description) {
        throw new AssertionError("Mixin invoker was not applied");
    }
}
