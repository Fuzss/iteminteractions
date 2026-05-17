package fuzs.iteminteractions.common.api.v2.world.inventory.tooltip;

import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public record ItemContentsTooltip(NonNullList<ItemStack> itemList,
                                  int selectedItem,
                                  int gridWidth,
                                  int gridHeight,
                                  int backgroundColor) implements TooltipComponent {

}
