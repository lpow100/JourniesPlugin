package net.minecraft.world.effect;

import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.player.EntityHuman;

class SaturationMobEffect extends InstantMobEffect {

    protected SaturationMobEffect(MobEffectInfo mobeffectinfo, int i) {
        super(mobeffectinfo, i);
    }

    @Override
    public boolean applyEffectTick(WorldServer worldserver, EntityLiving entityliving, int i) {
        if (entityliving instanceof EntityHuman entityhuman) {
            entityhuman.getFoodData().eat(i + 1, 1.0F);
        }

        return true;
    }
}
