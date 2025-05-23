package net.minecraft.world.item;

import java.util.List;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagsBlock;
import net.minecraft.tags.TagsItem;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public record ToolMaterial(TagKey<Block> incorrectBlocksForDrops, int durability, float speed, float attackDamageBonus, int enchantmentValue, TagKey<Item> repairItems) {

    public static final ToolMaterial WOOD = new ToolMaterial(TagsBlock.INCORRECT_FOR_WOODEN_TOOL, 59, 2.0F, 0.0F, 15, TagsItem.WOODEN_TOOL_MATERIALS);
    public static final ToolMaterial STONE = new ToolMaterial(TagsBlock.INCORRECT_FOR_STONE_TOOL, 131, 4.0F, 1.0F, 5, TagsItem.STONE_TOOL_MATERIALS);
    public static final ToolMaterial IRON = new ToolMaterial(TagsBlock.INCORRECT_FOR_IRON_TOOL, 250, 6.0F, 2.0F, 14, TagsItem.IRON_TOOL_MATERIALS);
    public static final ToolMaterial DIAMOND = new ToolMaterial(TagsBlock.INCORRECT_FOR_DIAMOND_TOOL, 1561, 8.0F, 3.0F, 10, TagsItem.DIAMOND_TOOL_MATERIALS);
    public static final ToolMaterial GOLD = new ToolMaterial(TagsBlock.INCORRECT_FOR_GOLD_TOOL, 32, 12.0F, 0.0F, 22, TagsItem.GOLD_TOOL_MATERIALS);
    public static final ToolMaterial NETHERITE = new ToolMaterial(TagsBlock.INCORRECT_FOR_NETHERITE_TOOL, 2031, 9.0F, 4.0F, 15, TagsItem.NETHERITE_TOOL_MATERIALS);

    private Item.Info applyCommonProperties(Item.Info item_info) {
        return item_info.durability(this.durability).repairable(this.repairItems).enchantable(this.enchantmentValue);
    }

    public Item.Info applyToolProperties(Item.Info item_info, TagKey<Block> tagkey, float f, float f1, float f2) {
        HolderGetter<Block> holdergetter = BuiltInRegistries.<Block>acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);

        return this.applyCommonProperties(item_info).component(DataComponents.TOOL, new Tool(List.of(Tool.a.deniesDrops(holdergetter.getOrThrow(this.incorrectBlocksForDrops)), Tool.a.minesAndDrops(holdergetter.getOrThrow(tagkey), this.speed)), 1.0F, 1, true)).attributes(this.createToolAttributes(f, f1)).component(DataComponents.WEAPON, new Weapon(2, f2));
    }

    private ItemAttributeModifiers createToolAttributes(float f, float f1) {
        return ItemAttributeModifiers.builder().add(GenericAttributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, (double) (f + this.attackDamageBonus), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(GenericAttributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, (double) f1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }

    public Item.Info applySwordProperties(Item.Info item_info, float f, float f1) {
        HolderGetter<Block> holdergetter = BuiltInRegistries.<Block>acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);

        return this.applyCommonProperties(item_info).component(DataComponents.TOOL, new Tool(List.of(Tool.a.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F), Tool.a.overrideSpeed(holdergetter.getOrThrow(TagsBlock.SWORD_INSTANTLY_MINES), Float.MAX_VALUE), Tool.a.overrideSpeed(holdergetter.getOrThrow(TagsBlock.SWORD_EFFICIENT), 1.5F)), 1.0F, 2, false)).attributes(this.createSwordAttributes(f, f1)).component(DataComponents.WEAPON, new Weapon(1));
    }

    private ItemAttributeModifiers createSwordAttributes(float f, float f1) {
        return ItemAttributeModifiers.builder().add(GenericAttributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, (double) (f + this.attackDamageBonus), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(GenericAttributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, (double) f1, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build();
    }
}
