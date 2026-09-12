package che.statues20.blockentity;

import che.statues20.Statues20;
import che.statues20.menu.StatueMenu;
import che.statues20.pose.StatuePose;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Server-authoritative state for a finished statue.
 *
 * The original 1.7 mod remembered the source block + metadata.  In 1.20.1 we
 * keep the entire BlockState so stairs/log axes/variants and modded block
 * properties survive sculpting and can be used by the material renderer.
 */
public class StatueBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int HELMET = 0;
    public static final int CHEST = 1;
    public static final int LEGS = 2;
    public static final int BOOTS = 3;
    public static final int MAIN_HAND = 4;
    public static final int OFF_HAND = 5;

    private NonNullList<ItemStack> items = NonNullList.withSize(6, ItemStack.EMPTY);
    private String skinName = "";
    private BlockState sourceState = Blocks.STONE.defaultBlockState();
    private final StatuePose pose = new StatuePose();

    public StatueBlockEntity(BlockPos pos, BlockState state) {
        super(Statues20.STATUE_BE.get(), pos, state);
    }

    public String getSkinName() { return skinName; }
    public StatuePose getPose() { return pose; }
    public BlockState getSourceState() { return sourceState; }
    /** Compatibility helper used by a few client effects. */
    public Block getMaterial() { return sourceState.getBlock(); }

    public void setSkinName(String value) {
        skinName = value == null ? "" : value.trim();
        sync();
    }

    public void setPose(StatuePose value) {
        pose.copyFrom(value);
        sync();
    }

    public void setSourceState(BlockState state) {
        sourceState = state == null ? Blocks.STONE.defaultBlockState() : state;
        sync();
    }

    /** Kept for old callers; a material change intentionally resets properties. */
    public void setMaterial(Block block) {
        setSourceState(block == null ? Blocks.STONE.defaultBlockState() : block.defaultBlockState());
    }

    public CompoundTag savePoseTag() {
        CompoundTag tag = new CompoundTag();
        pose.save(tag);
        return tag;
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("SkinName", skinName);
        tag.put("SourceState", NbtUtils.writeBlockState(sourceState));
        // Backward compatibility with the v0.3 saves.
        tag.putString("Material", BuiltInRegistries.BLOCK.getKey(sourceState.getBlock()).toString());
        CompoundTag poseTag = new CompoundTag();
        pose.save(poseTag);
        tag.put("Pose", poseTag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        skinName = tag.getString("SkinName");

        if (tag.contains("SourceState", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            try {
                sourceState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("SourceState"));
            } catch (Exception ignored) {
                sourceState = Blocks.STONE.defaultBlockState();
            }
        } else if (tag.contains("Material")) {
            var id = net.minecraft.resources.ResourceLocation.tryParse(tag.getString("Material"));
            Block block = id == null ? Blocks.STONE : BuiltInRegistries.BLOCK.get(id);
            sourceState = (block == null ? Blocks.STONE : block).defaultBlockState();
        } else {
            sourceState = Blocks.STONE.defaultBlockState();
        }

        if (tag.contains("Pose", net.minecraft.nbt.Tag.TAG_COMPOUND)) {
            pose.load(tag.getCompound("Pose"));
        }

        items = NonNullList.withSize(6, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) load(packet.getTag());
    }

    @Override public int getContainerSize() { return 6; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) sync();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > 1) stack.setCount(1);
        sync();
    }

    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { items.clear(); sync(); }
    @Override public Component getDisplayName() { return Component.translatable("container.statues20.statue"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new StatueMenu(id, inventory, this);
    }
}
