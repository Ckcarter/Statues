package che.statues20.block;

import che.statues20.Statues20;
import che.statues20.blockentity.ShowcaseBlockEntity;
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
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/** Three-block-wide linked showcase, mirroring the original center + two side blocks. */
public class ShowcaseBlock extends BaseEntityBlock {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final IntegerProperty PART=IntegerProperty.create("part",0,2); // 0 center, 1 left, 2 right
 private static final VoxelShape SHAPE=Block.box(0,0,0,16,24,16);
 public ShowcaseBlock(Properties p){super(p);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,0));}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState>b){b.add(FACING,PART);}
 @Override public RenderShape getRenderShape(BlockState s){return RenderShape.INVISIBLE;}
 @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return SHAPE;}
 private Direction left(BlockState s){return s.getValue(FACING).getCounterClockWise();} private Direction right(BlockState s){return s.getValue(FACING).getClockWise();}
 @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext c){Direction f=c.getHorizontalDirection().getOpposite();BlockPos p=c.getClickedPos();BlockState temp=defaultBlockState().setValue(FACING,f);if(!c.getLevel().getBlockState(p.relative(left(temp))).canBeReplaced(c)||!c.getLevel().getBlockState(p.relative(right(temp))).canBeReplaced(c))return null;return temp;}
 @Override public void setPlacedBy(Level l,BlockPos p,BlockState s,@Nullable LivingEntity e,ItemStack st){super.setPlacedBy(l,p,s,e,st);if(s.getValue(PART)==0){l.setBlock(p.relative(left(s)),s.setValue(PART,1),3);l.setBlock(p.relative(right(s)),s.setValue(PART,2),3);}}
 private BlockPos center(BlockState s,BlockPos p){int part=s.getValue(PART);return part==1?p.relative(s.getValue(FACING).getClockWise()):part==2?p.relative(s.getValue(FACING).getCounterClockWise()):p;}
 @Override public InteractionResult use(BlockState s,Level l,BlockPos p,Player pl,InteractionHand h,BlockHitResult hit){if(!l.isClientSide&&pl instanceof ServerPlayer sp){BlockPos c=center(s,p);if(l.getBlockEntity(c) instanceof ShowcaseBlockEntity be)NetworkHooks.openScreen(sp,be,b->b.writeBlockPos(c));}return InteractionResult.sidedSuccess(l.isClientSide);}
 @Nullable @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return s.getValue(PART)==0?new ShowcaseBlockEntity(p,s):null;}
 @Nullable @Override public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(Level l,BlockState s,net.minecraft.world.level.block.entity.BlockEntityType<T> type){return createTickerHelper(type,Statues20.SHOWCASE_BE.get(),ShowcaseBlockEntity::tick);}

 @Override public void playerWillDestroy(Level l,BlockPos p,BlockState s,Player pl){if(!l.isClientSide){BlockPos c=center(s,p);BlockState cs=l.getBlockState(c);if(cs.is(this)){if(l.getBlockEntity(c) instanceof ShowcaseBlockEntity be)Containers.dropContents(l,c,be);if(!pl.getAbilities().instabuild)popResource(l,c,new ItemStack(Statues20.SHOWCASE_ITEM.get()));Direction le=left(cs),ri=right(cs);for(BlockPos q:new BlockPos[]{c.relative(le),c.relative(ri)})if(l.getBlockState(q).is(this))l.setBlock(q,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),35);if(!c.equals(p))l.setBlock(c,net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),35);}}super.playerWillDestroy(l,p,s,pl);}
}
