package io.sc3.plethora.gameplay.modules.scanner;

import dan200.computercraft.api.lua.IArguments;
import dan200.computercraft.api.lua.LuaException;
import io.sc3.plethora.api.IWorldLocation;
import io.sc3.plethora.api.WorldLocation;
import io.sc3.plethora.api.method.FutureMethodResult;
import io.sc3.plethora.api.method.IContext;
import io.sc3.plethora.api.method.IUnbakedContext;
import io.sc3.plethora.api.module.IModuleContainer;
import io.sc3.plethora.api.module.SubtargetedModuleMethod;
import io.sc3.plethora.api.reference.BlockReference;
import io.sc3.plethora.gameplay.modules.RangeInfo;
import io.sc3.plethora.integration.vanilla.meta.block.BlockStateMeta;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.*;

import static io.sc3.plethora.api.method.ArgumentExt.assertIntBetween;
import static io.sc3.plethora.api.method.ContextKeys.ORIGIN;
import static io.sc3.plethora.core.ContextHelpers.fromContext;
import static io.sc3.plethora.gameplay.registry.PlethoraModules.SCANNER_M;
import static io.sc3.plethora.gameplay.registry.PlethoraModules.SCANNER_S;

public class ScannerMethods {
    public static final SubtargetedModuleMethod<IWorldLocation> SCAN = SubtargetedModuleMethod.of(
        "scan", SCANNER_M, IWorldLocation.class,
        "function():table -- Scan all blocks in the vicinity",
        ScannerMethods::scan
    );
    private static FutureMethodResult scan(@Nonnull IUnbakedContext<IModuleContainer> unbaked,
                                           @Nonnull IArguments args) throws LuaException {
        ScannerMethodContext ctx = getContext(unbaked);
        World world = ctx.loc.getWorld();
        BlockPos pos = ctx.loc.getPos();
        final int x = pos.getX(), y = pos.getY(), z = pos.getZ();

        return ctx.context.getCostHandler().await(ctx.range.getBulkCost(), () ->
            FutureMethodResult.result(scan(world, x, y, z, ctx.range.getRange())));
    }

    private static List<Map<String, ?>> scan(World world, int x, int y, int z, int radius) {
        List<Map<String, ?>> result = new ArrayList<>();
        for (int oX = x - radius; oX <= x + radius; oX++) {
            for (int oY = y - radius; oY <= y + radius; oY++) {
                for (int oZ = z - radius; oZ <= z + radius; oZ++) {
                    BlockPos subPos = BlockPos.ofFloored(oX, oY, oZ);
                    BlockState block = world.getBlockState(subPos);

                    HashMap<String, Object> data = new HashMap<>(6);
                    data.put("x", oX - x);
                    data.put("y", oY - y);
                    data.put("z", oZ - z);

                    Identifier name = Registries.BLOCK.getId(block.getBlock());
                    data.put("name", name.toString());

                    BlockStateMeta.fillBasicMeta(data, block);

                    result.add(data);
                }
            }
        }

        return result;
    }

    public static final SubtargetedModuleMethod<IWorldLocation> GET_BLOCK_META = SubtargetedModuleMethod.of(
        "getBlockMeta", SCANNER_M, IWorldLocation.class,
        "function(x:integer, y:integer, z:integer):table|nil -- -- Get metadata about a nearby block",
        ScannerMethods::getBlockMeta
    );
    private static FutureMethodResult getBlockMeta(@Nonnull IUnbakedContext<IModuleContainer> unbaked,
                                                   @Nonnull IArguments args) throws LuaException {
        ScannerMethodContext ctx = getContext(unbaked);
        int radius = ctx.range.getRange();

        int x = assertIntBetween(args, 0, -radius, radius, "X coordinate out of bounds (%s)");
        int y = assertIntBetween(args, 1, -radius, radius, "Y coordinate out of bounds (%s)");
        int z = assertIntBetween(args, 2, -radius, radius, "Z coordinate out of bounds (%s)");

        return FutureMethodResult.result(ctx.context
            .makeChild(new BlockReference(new WorldLocation(ctx.loc.getWorld(), ctx.loc.getPos().add(x, y, z))))
            .getMeta());
    }
  private static double rayLengthForCube(Vec3d dir, int range) {
    final double EPS = 1.0e-8;

    double t = Double.POSITIVE_INFINITY;

    if (Math.abs(dir.x) > EPS) {
      t = Math.min(t, range / Math.abs(dir.x));
    }
    if (Math.abs(dir.y) > EPS) {
      t = Math.min(t, range / Math.abs(dir.y));
    }
    if (Math.abs(dir.z) > EPS) {
      t = Math.min(t, range / Math.abs(dir.z));
    }

    return t;
  }
  private static List<BlockPos> raycastAllBlocks(
    World world,
    Vec3d origin,
    BlockPos originPos,
    Vec3d direction,
    double maxDistance
  ) {
    List<BlockPos> hits = new ArrayList<>();

    Vec3d dir = direction.normalize();

    int x = originPos.getX();
    int y = originPos.getY();
    int z = originPos.getZ();

    int stepX = dir.x > 0 ? 1 : dir.x < 0 ? -1 : 0;
    int stepY = dir.y > 0 ? 1 : dir.y < 0 ? -1 : 0;
    int stepZ = dir.z > 0 ? 1 : dir.z < 0 ? -1 : 0;

    double tDeltaX = stepX == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.x);
    double tDeltaY = stepY == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.y);
    double tDeltaZ = stepZ == 0 ? Double.POSITIVE_INFINITY : Math.abs(1.0 / dir.z);

    double xDist = stepX > 0
      ? (x + 1 - origin.x)
      : (origin.x - x);
    double yDist = stepY > 0
      ? (y + 1 - origin.y)
      : (origin.y - y);
    double zDist = stepZ > 0
      ? (z + 1 - origin.z)
      : (origin.z - z);

    double tMaxX = stepX == 0 ? Double.POSITIVE_INFINITY : xDist / Math.abs(dir.x);
    double tMaxY = stepY == 0 ? Double.POSITIVE_INFINITY : yDist / Math.abs(dir.y);
    double tMaxZ = stepZ == 0 ? Double.POSITIVE_INFINITY : zDist / Math.abs(dir.z);

    double t = 0.0;

    while (t <= maxDistance) {
      BlockPos pos = new BlockPos(x, y, z);

      if (!pos.equals(originPos)) {
        BlockState state = world.getBlockState(pos);
        if (!state.isAir()) {
          hits.add(pos);
        }
      }

      if (tMaxX < tMaxY) {
        if (tMaxX < tMaxZ) {
          x += stepX;
          t = tMaxX;
          tMaxX += tDeltaX;
        } else {
          z += stepZ;
          t = tMaxZ;
          tMaxZ += tDeltaZ;
        }
      } else {
        if (tMaxY < tMaxZ) {
          y += stepY;
          t = tMaxY;
          tMaxY += tDeltaY;
        } else {
          z += stepZ;
          t = tMaxZ;
          tMaxZ += tDeltaZ;
        }
      }
    }

    return hits;
  }





  public static final SubtargetedModuleMethod<IWorldLocation> RAYCAST = SubtargetedModuleMethod.of(
    "raycast", SCANNER_M, IWorldLocation.class,
    "function(yaw:number, pitch:number):table|nil -- Raycast in a direction and return all blocks in its path",
    ScannerMethods::raycast
  );
  private static FutureMethodResult raycast(
    @Nonnull IUnbakedContext<IModuleContainer> unbaked,
    @Nonnull IArguments args
  ) throws LuaException {
    ScannerMethodContext ctx = getContext(unbaked);

    // --- Argument validation ---
    if (args.count() < 2) {
      throw new LuaException("Expected yaw and pitch");
    }

    double yaw = args.getDouble(0);
    double pitch = args.getDouble(1);
    Double userDistance = null;
    if (args.count() >= 3 ) {
      userDistance = args.getDouble(2);
      if (userDistance < 0) {
        throw new LuaException("Distance must be non-negative");
      }
    }




    if (yaw < -180 || yaw > 180 ) {
      throw new LuaException("Yaw must be between -180 and 180 degrees");
    }
    if (pitch < -90.0 || pitch > 90.0) {
      throw new LuaException("Pitch must be between -90 and 90 degrees");
    }


    World world = ctx.loc.getWorld();
    BlockPos originPos = ctx.loc.getPos();

    int range = ctx.range.getRange();

    double yawRad = Math.toRadians(yaw);
    double pitchRad = Math.toRadians(pitch);

    Vec3d direction = new Vec3d(
      -Math.sin(yawRad) * Math.cos(pitchRad),
      -Math.sin(pitchRad),
      Math.cos(yawRad) * Math.cos(pitchRad)
    ).normalize();
    double rayLength = rayLengthForCube(direction, range);
    if (userDistance != null) {
      if (userDistance > rayLength) {
        throw new LuaException("Distance exceeds scanner range");
      }
      rayLength = userDistance;
    }
    Vec3d origin = Vec3d.ofCenter(originPos);






    List<BlockPos> hits = raycastAllBlocks(
      world,
      origin,
      originPos,
      direction,
      rayLength
    );

    if (hits.isEmpty()) {
      return FutureMethodResult.result((Object) null);
    }

    List<Map<String, Object>> result = new ArrayList<>();
    for (BlockPos hitPos : hits) {
      int dx = hitPos.getX() - originPos.getX();
      int dy = hitPos.getY() - originPos.getY();
      int dz = hitPos.getZ() - originPos.getZ();

      if (Math.abs(dx) > range || Math.abs(dy) > range || Math.abs(dz) > range) {
        continue;
      }

      BlockState state = world.getBlockState(hitPos);
      Identifier name = Registries.BLOCK.getId(state.getBlock());

      Map<String, Object> entry = new HashMap<>(6);
      entry.put("x", dx);
      entry.put("y", dy);
      entry.put("z", dz);
      entry.put("name", name.toString());

      result.add(entry);
    }

    return FutureMethodResult.result(result);
  }


  private record ScannerMethodContext(IContext<IModuleContainer> context, IWorldLocation loc, RangeInfo range) {}
    private static ScannerMethodContext getContext(@Nonnull IUnbakedContext<IModuleContainer> unbaked) throws LuaException {
        IContext<IModuleContainer> ctx = unbaked.bake();
        IWorldLocation location = fromContext(ctx, IWorldLocation.class, ORIGIN);
        RangeInfo range = fromContext(ctx, RangeInfo.class, SCANNER_S);
        return new ScannerMethodContext(ctx, location, range);
    }
}
