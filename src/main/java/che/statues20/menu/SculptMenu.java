package che.statues20.menu;

import che.statues20.Statues20;
import che.statues20.pose.StatuePose;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/** Stateless menu used only to securely anchor the client sculpt screen to a world position. */
public class SculptMenu extends AbstractContainerMenu {
    public final BlockPos sourcePos;
    public final boolean hasCopiedPose;
    public final StatuePose copiedPose;

    public SculptMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, buf.readBlockPos(), buf.readBoolean(), StatuePose.read(buf));
    }

    public SculptMenu(int id, Inventory inv, BlockPos pos, boolean hasCopiedPose, StatuePose copiedPose) {
        super(Statues20.SCULPT_MENU.get(), id);
        this.sourcePos = pos;
        this.hasCopiedPose = hasCopiedPose;
        this.copiedPose = copiedPose == null ? new StatuePose() : copiedPose.copy();
    }

    @Override public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(sourcePos.getX() + .5, sourcePos.getY() + .5, sourcePos.getZ() + .5) <= 64;
    }
}
