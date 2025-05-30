package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.npc.EntityVillager;
import net.minecraft.world.entity.npc.VillagerProfession;

// CraftBukkit start
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
// CraftBukkit end

public class BehaviorCareer {

    public BehaviorCareer() {}

    public static BehaviorControl<EntityVillager> create() {
        return BehaviorBuilder.create((behaviorbuilder_b) -> {
            return behaviorbuilder_b.group(behaviorbuilder_b.present(MemoryModuleType.POTENTIAL_JOB_SITE), behaviorbuilder_b.registered(MemoryModuleType.JOB_SITE)).apply(behaviorbuilder_b, (memoryaccessor, memoryaccessor1) -> {
                return (worldserver, entityvillager, i) -> {
                    GlobalPos globalpos = (GlobalPos) behaviorbuilder_b.get(memoryaccessor);

                    if (!globalpos.pos().closerToCenterThan(entityvillager.position(), 2.0D) && !entityvillager.assignProfessionWhenSpawned()) {
                        return false;
                    } else {
                        memoryaccessor.erase();
                        memoryaccessor1.set(globalpos);
                        worldserver.broadcastEntityEvent(entityvillager, (byte) 14);
                        if (!entityvillager.getVillagerData().profession().is(VillagerProfession.NONE)) {
                            return true;
                        } else {
                            MinecraftServer minecraftserver = worldserver.getServer();

                            Optional.ofNullable(minecraftserver.getLevel(globalpos.dimension())).flatMap((worldserver1) -> {
                                return worldserver1.getPoiManager().getType(globalpos.pos());
                            }).flatMap((holder) -> {
                                return BuiltInRegistries.VILLAGER_PROFESSION.listElements().filter((holder_c) -> {
                                    return ((VillagerProfession) holder_c.value()).heldJobSite().test(holder);
                                }).findFirst();
                            }).ifPresent((holder_c) -> {
                                // CraftBukkit start - Fire VillagerCareerChangeEvent where Villager gets employed
                                VillagerCareerChangeEvent event = CraftEventFactory.callVillagerCareerChangeEvent(entityvillager, CraftVillager.CraftProfession.minecraftHolderToBukkit(holder_c), VillagerCareerChangeEvent.ChangeReason.EMPLOYED);
                                if (event.isCancelled()) {
                                    return;
                                }

                                entityvillager.setVillagerData(entityvillager.getVillagerData().withProfession(CraftVillager.CraftProfession.bukkitToMinecraftHolder(event.getProfession())));
                                // CraftBukkit end
                                entityvillager.refreshBrain(worldserver);
                            });
                            return true;
                        }
                    }
                };
            });
        });
    }
}
