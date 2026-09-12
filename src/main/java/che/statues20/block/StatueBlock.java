package che.statues20.block;

import che.statues20.blockentity.StatueBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** Two linked blocks, matching the original statue's 0.1..0.9 footprint and 2-block height. */
public class StatueBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    // Original bounds were 0.1 -> 0.9 in X/Z and two blocks tall.
    private static final VoxelShape HALF_SHAPE = Block.box(1.6, 0, 1.6, 14.4, 16, 14.4);

    public StatueBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override public RenderShape getRenderShape(BlockState state) { return RenderShape.INVISIBLE; }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return HALF_SHAPE; }
    @Override public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) { return 0; }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos above = context.getClickedPos().above();
        if (above.getY() >= context.getLevel().getMaxBuildHeight()
                || !context.getLevel().getBlockState(above).canBeReplaced(context)) {
            return null;
        }
        return defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (direction.getAxis() == Direction.Axis.Y
                && ((half == DoubleBlockHalf.LOWER) == (direction == Direction.UP))) {
            return neighbor.is(this) && neighbor.getValue(HALF) != half
                    ? state.setValue(FACING, neighbor.getValue(FACING))
                    : Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    private BlockPos basePos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    private @Nullable StatueBlockEntity statueAt(BlockState state, BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(basePos(state, pos));
        return be instanceof StatueBlockEntity statue ? statue : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockPos base = basePos(state, pos);
            if (level.getBlockEntity(base) instanceof StatueBlockEntity statue) {
                NetworkHooks.openScreen(serverPlayer, statue, buf -> buf.writeBlockPos(base));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new StatueBlockEntity(pos, state) : null;
    }

    /** Preserve the old mod's behavior where luminous source blocks make luminous statues. */
    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        StatueBlockEntity statue = statueAt(state, level, pos);
        if (statue == null) return 0;
        BlockState source = statue.getSourceState();
        try {
            return source.getLightEmission(level, basePos(state, pos));
        } catch (Throwable ignored) {
            return source.getLightEmission();
        }
    }

    /** The old block always advertised itself as a redstone signal source. */
    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    /** Delegate weak redstone output to the block state that was sculpted. */
    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        StatueBlockEntity statue = statueAt(state, level, pos);
        if (statue == null) return 0;
        try {
            return statue.getSourceState().getSignal(level, basePos(state, pos), direction);
        } catch (Throwable ignored) {
            return 0;
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof StatueBlockEntity statue) Containers.dropContents(level, pos, statue);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        DoubleBlockHalf half = state.getValue(HALF);
        BlockPos other = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(other);
        if (otherState.is(this) && otherState.getValue(HALF) != half) {
            level.setBlock(other, Blocks.AIR.defaultBlockState(), 35);
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
