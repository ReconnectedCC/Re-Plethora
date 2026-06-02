package io.sc3.plethora.gameplay.neural;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.shared.computer.core.ComputerFamily;
import dan200.computercraft.shared.computer.core.ServerComputer;
import dan200.computercraft.shared.computer.core.ServerComputerRegistry;
import dan200.computercraft.shared.computer.core.ServerContext;
import dan200.computercraft.shared.util.IDAssigner;
import dev.emi.trinkets.api.SlotReference;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import javax.annotation.Nonnull;

import java.util.Objects;

import static io.sc3.plethora.Plethora.log;

/**
 * Attempt to get computers from items
 */
public class NeuralComputerHandler {
    public static final int WIDTH = 39;
    public static final int HEIGHT = 13;

    private static final String SESSION_ID = "session_id";
    private static final String INSTANCE_ID = "instance_id";
    public static final String COMPUTER_ID = "id";
    public static final String ITEMS = "items";
    public static final String DIRTY = "dirty";
    public static final String MODULE_DATA = "module_data";

    private NeuralComputerHandler() {
    }

    public static NeuralComputer getServer(@Nonnull ItemStack stack, LivingEntity owner, @Nonnull SlotReference slot) {
        NbtCompound nbt = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();

        final ServerComputerRegistry manager = ServerContext.get(Objects.requireNonNull(owner.getServer())).registry();
        final int sessionId = manager.getSessionID();

        NeuralComputer neural = null;
        if (nbt.contains(SESSION_ID) && nbt.containsUuid(INSTANCE_ID)) {
            ServerComputer computer = manager.get(nbt.getInt(SESSION_ID), nbt.getUuid(INSTANCE_ID));
            if (computer instanceof NeuralComputer neuralComputer) {
                neural = neuralComputer;
            } else if(computer != null) {
                log.error("Computer is not NeuralComputer but " + computer);
            }
        }

        if (neural == null) {
            int computerId = nbt.contains(COMPUTER_ID)
                ? nbt.getInt(COMPUTER_ID)
                : ComputerCraftAPI.createUniqueNumberedSaveDir(owner.getServer(), IDAssigner.COMPUTER);

            Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
            String label = customName == null ? null : customName.getString();
            neural = new NeuralComputer((ServerWorld)owner.getEntityWorld(), owner.getBlockPos(), computerId, label);
            neural.readModuleData(nbt.getCompound(MODULE_DATA));

            nbt.putInt(SESSION_ID, sessionId);
            nbt.putUuid(INSTANCE_ID, neural.register());
            nbt.putInt(COMPUTER_ID, computerId);
            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

            neural.turnOn();
            slot.inventory().markDirty();
        }

        return neural;
    }

    public static NeuralComputer tryGetServer(@Nonnull ItemStack stack, LivingEntity player) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);
        if(component == null) return null;
        NbtCompound nbt = component.copyNbt();
        if(!nbt.contains(SESSION_ID) || !nbt.containsUuid(INSTANCE_ID)) return null;

        final ServerComputerRegistry manager = ServerContext.get(Objects.requireNonNull(player.getServer())).registry();
        var computer = manager.get(nbt.getInt(SESSION_ID), nbt.getUuid(INSTANCE_ID));
        if(computer == null) {
            return null;
        } else if (computer instanceof NeuralComputer neuralComputer) {
            return neuralComputer;
        } else {
            log.error("Computer is not NeuralComputer but " + computer);
            return null;
        }
    }
}
