package net.minecraft.world.entity.monster.breeze;

import net.minecraft.util.MathHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.Vec3D;

public class BreezeUtil {

    private static final double MAX_LINE_OF_SIGHT_TEST_RANGE = 50.0D;

    public BreezeUtil() {}

    public static Vec3D randomPointBehindTarget(EntityLiving entityliving, RandomSource randomsource) {
        int i = 90;
        float f = entityliving.yHeadRot + 180.0F + (float) randomsource.nextGaussian() * 90.0F / 2.0F;
        float f1 = MathHelper.lerp(randomsource.nextFloat(), 4.0F, 8.0F);
        Vec3D vec3d = Vec3D.directionFromRotation(0.0F, f).scale((double) f1);

        return entityliving.position().add(vec3d);
    }

    public static boolean hasLineOfSight(Breeze breeze, Vec3D vec3d) {
        Vec3D vec3d1 = new Vec3D(breeze.getX(), breeze.getY(), breeze.getZ());

        return vec3d.distanceTo(vec3d1) > getMaxLineOfSightTestRange(breeze) ? false : breeze.level().clip(new RayTrace(vec3d1, vec3d, RayTrace.BlockCollisionOption.COLLIDER, RayTrace.FluidCollisionOption.NONE, breeze)).getType() == MovingObjectPosition.EnumMovingObjectType.MISS;
    }

    private static double getMaxLineOfSightTestRange(Breeze breeze) {
        return Math.max(50.0D, breeze.getAttributeValue(GenericAttributes.FOLLOW_RANGE));
    }
}
