package io.sc3.plethora.gameplay.neural;

import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.network.container.ComputerContainerData;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class NeuralInterfaceScreenFactory implements ExtendedScreenHandlerFactory<ComputerContainerData> {
    public enum TargetType {
        PLAYER, ENTITY
    }

    private final LivingEntity parent;
    private final ItemStack stack;
    private final ServerComputer computer;

    public NeuralInterfaceScreenFactory(LivingEntity parent, ItemStack stack, ServerComputer computer) {
        this.parent = parent;
        this.stack = stack;
        this.computer = computer;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("gui.plethora.neuralInterface.title");
    }

    @Nullable
    @Override
    public NeuralInterfaceScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return NeuralInterfaceScreenHandler.of(syncId, inv, parent, stack, computer);
    }

    @Override
    public ComputerContainerData getScreenOpeningData(ServerPlayerEntity player) {
        return new ComputerContainerData(computer, stack);
    }

    public static NeuralInterfaceScreenHandler fromPacket(int syncId, PlayerInventory inv, ComputerContainerData data) {
        return NeuralInterfaceScreenHandler.of(syncId, inv, data);
    }
}
