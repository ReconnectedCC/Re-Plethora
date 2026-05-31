package io.sc3.plethora.gameplay.neural;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.collection.DefaultedList;
import io.sc3.plethora.mixin.SimpleInventoryAccessor;

import static io.sc3.plethora.gameplay.neural.NeuralComputerHandler.DIRTY;
import static io.sc3.plethora.gameplay.neural.NeuralHelpers.INV_SIZE;
import static io.sc3.plethora.gameplay.neural.NeuralHelpers.isItemValid;

public class NeuralInterfaceInventory extends SimpleInventory {
    final ItemStack parent;
    private RegistryWrapper.WrapperLookup registries;

    public NeuralInterfaceInventory(ItemStack parent) {
        super(INV_SIZE);

        this.parent = parent;

        addListener(i -> writeData());
    }

    @Override
    public void onOpen(PlayerEntity player) {
        super.onOpen(player);
        registries = player.getRegistryManager();
        Inventories.readNbt(getData(), getOwnStacks(), registries);
    }

    @Override
    public void onClose(PlayerEntity player) {
        super.onClose(player);
        registries = player.getRegistryManager();
        writeData();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return isItemValid(slot, stack);
    }

    public DefaultedList<ItemStack> getOwnStacks() {
        return ((SimpleInventoryAccessor) this).getHeldStacks();
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);

        NbtCompound nbt = getData();
        nbt.putShort(DIRTY, (short) (nbt.getShort(DIRTY) | (1 << slot)));
        setData(nbt);
    }

    private NbtCompound getData() {
        NbtComponent customData = parent.get(DataComponentTypes.CUSTOM_DATA);
        return customData == null ? new NbtCompound() : customData.copyNbt();
    }

    private void writeData() {
        if (registries == null) return;
        setData(Inventories.writeNbt(getData(), getOwnStacks(), registries));
    }

    private void setData(NbtCompound nbt) {
        if (nbt.isEmpty()) {
            parent.remove(DataComponentTypes.CUSTOM_DATA);
        } else {
            parent.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
    }
}
