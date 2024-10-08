package fuzs.iteminteractions.api.v1.client.tooltip;

import fuzs.iteminteractions.api.v1.tooltip.BundleContentsTooltip;

public class ClientBundleContentsTooltip extends AbstractClientItemContentsTooltip {
    private final boolean isBundleFull;

    public ClientBundleContentsTooltip(BundleContentsTooltip tooltip) {
        super(tooltip.items(), tooltip.backgroundColor());
        this.isBundleFull = tooltip.isBundleFull();
    }

    @Override
    protected int getGridSizeX() {
        return Math.max(2, (int) Math.ceil(Math.sqrt((double) this.items.size() + 1.0D)));
    }

    @Override
    protected int getGridSizeY() {
        return (int) Math.ceil(((double) this.items.size() + 1.0D) / (double) this.getGridSizeX());
    }

    @Override
    protected boolean isSlotBlocked(int itemIndex) {
        // container is larger by one to allow for adding items, we need to subtract that additional slot again when checking if it is full
        return itemIndex >= this.items.size() - 1 && this.isBundleFull;
    }
}
