package che.statues20.item;

import che.statues20.Statues20;
import che.statues20.block.StatueBlock;
import che.statues20.blockentity.StatueBlockEntity;
import che.statues20.menu.SculptMenu;
import che.statues20.pose.StatuePose;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;

/**
 * Modern port of ItemMarteau: copy a statue's pose, or sculpt two identical stacked blocks.
 */
public class SculptingHammerItem extends Item {
    public SculptingHammerItem(Properties properties) {
        // The old item had maxDamage=2 and was unstackable, but sculpting did not consume durability.
        super(properties.stacksTo(1).durability(2));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) return InteractionResult.SUCCESS;

        BlockPos clicked = context.getClickedPos();
        BlockState clickedState = player.level().getBlockState(clicked);
        BlockPos statueBase = clickedState.getBlock() instanceof StatueBlock
                && clickedState.getValue(StatueBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER
                ? clicked.below() : clicked;

        if (player.level().getBlockEntity(statueBase) instanceof StatueBlockEntity statue) {
            CompoundTag poseTag = statue.savePoseTag();
            context.getItemInHand().getOrCreateTag().put("CopiedPose", poseTag);
            player.level().playSound(null, clicked, Statues20.COPY_SOUND.get(), SoundSource.BLOCKS, 0.8f, 1.35f);
            player.level().levelEvent(2001, clicked, net.minecraft.world.level.block.Block.getId(statue.getSourceState()));
            player.displayClientMessage(Component.translatable("message.statues20.pose_copied"), true);
            return InteractionResult.CONSUME;
        }

        BlockPos base = findPairBase(player, clicked);
        if (base == null) {
            player.displayClientMessage(Component.translatable("message.statues20.need_pair"), true);
            return InteractionResult.CONSUME;
        }

        StatuePose copied = new StatuePose();
        boolean hasCopied = false;
        ItemStack hammer = context.getItemInHand();
        if (hammer.hasTag() && hammer.getTag().contains("CopiedPose", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            copied.load(hammer.getTag().getCompound("CopiedPose"));
            hasCopied = true;
        }
        final StatuePose copiedFinal = copied.copy();
        final boolean hasCopiedFinal = hasCopied;

        MenuProvider provider = new SimpleMenuProvider(
                (id, inv, p) -> new SculptMenu(id, inv, base, hasCopiedFinal, copiedFinal),
                Component.translatable("gui.statues20.sculpt"));

        NetworkHooks.openScreen(player, provider, buf -> {
            buf.writeBlockPos(base);
            buf.writeBoolean(hasCopiedFinal);
            copiedFinal.write(buf);
        });
        return InteractionResult.CONSUME;
    }

    private BlockPos findPairBase(ServerPlayer player, BlockPos pos) {
        BlockState state = player.level().getBlockState(pos);
        if (!Statues20.canSculpt(state)) return null;

        BlockState above = player.level().getBlockState(pos.above());
        if (state.equals(above) && Statues20.canSculpt(above)) return pos;

        BlockState below = player.level().getBlockState(pos.below());
        if (state.equals(below) && Statues20.canSculpt(below)) return pos.below();

        return null;
    }
}
