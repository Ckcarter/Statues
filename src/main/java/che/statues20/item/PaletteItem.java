package che.statues20.item;

import che.statues20.block.StatueBlock;
import che.statues20.blockentity.StatueBlockEntity;
import net.minecraft.core.BlockPos;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Original Statues palette: consumes one palette and switches the statue to the painted/full-skin sentinel. */
public class PaletteItem extends Item {
    public PaletteItem(Properties properties) { super(properties.stacksTo(64)); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof StatueBlock)) return InteractionResult.PASS;
        if (state.getValue(StatueBlock.HALF) == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER) pos = pos.below();
        if (!(level.getBlockEntity(pos) instanceof StatueBlockEntity statue)) return InteractionResult.PASS;

        if (!level.isClientSide) {
            BlockState old = statue.getSourceState();
            statue.setSourceState(Blocks.BEDROCK.defaultBlockState());
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), 3);
            level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(old));
            level.levelEvent(2001, pos.above(), net.minecraft.world.level.block.Block.getId(old));
            level.playSound(null, pos, che.statues20.Statues20.PAINT_SOUND.get(), SoundSource.BLOCKS, 1.0F, 0.9F + level.random.nextFloat() * 0.2F);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
