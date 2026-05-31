package io.sc3.plethora.gameplay.neural;

import dan200.computercraft.api.pocket.IPocketAccess;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.upgrades.UpgradeData;
import net.minecraft.component.ComponentChanges;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

/**
 * Proxy IPocketAccess for neural interfaces.
 */
public class NeuralPocketAccess implements IPocketAccess {
  private final NeuralComputer neural;

  public NeuralPocketAccess(NeuralComputer neural) {
        this.neural = neural;
    }

  @Nullable
  @Override
  public ServerWorld getLevel() {
    Entity entity = getEntity();
    if (entity == null) {
      return null;
    }
    return (ServerWorld) entity.getEntityWorld();
  }

  @Nullable
  @Override
  public Vec3d getPosition() {
    Entity entity = getEntity();
    if (entity == null) {
      return null;
    }
    return entity.getPos();
  }

  @Nullable
  @Override
  public Entity getEntity() {
    WeakReference<LivingEntity> ref = neural.getEntity();
    return ref != null ? ref.get() : null;
  }

  @Override
  public int getColour() {
        return -1;
    }

  @Override
  public void setColour(int colour) {}

  @Override
  public int getLight() {
        return -1;
    }

  @Override
  public void setLight(int colour) {}

  @org.jetbrains.annotations.Nullable
  @Override
  public UpgradeData<IPocketUpgrade> getUpgrade() {
    return null;
  }

  @Override
  public void setUpgrade(@org.jetbrains.annotations.Nullable UpgradeData<IPocketUpgrade> upgrade) {}

  @Nonnull
  @Override
  public ComponentChanges getUpgradeData() {
        return ComponentChanges.EMPTY;
    }

  @Override
  public void setUpgradeData(ComponentChanges data) {}

  @Override
  public void invalidatePeripheral() {}
}
