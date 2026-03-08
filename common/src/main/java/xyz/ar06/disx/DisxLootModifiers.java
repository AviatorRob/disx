package xyz.ar06.disx;

import dev.architectury.event.events.common.LootEvent;
import dev.architectury.registry.registries.Registrar;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

public class DisxLootModifiers {
    public static void modifyLootTables(Registrar<Item> itemRegistrar){
        LootContext.Builder contextBuilder = new LootContext.Builder(null);
        LootTable.Builder tableBuilder = new LootTable.Builder();
        LootEvent.MODIFY_LOOT_TABLE.register(((lootDataManager, id, context, builtin) -> {
            if (builtin && (
                    Blocks.OAK_LEAVES.getLootTable().equals(id) ||
                    Blocks.BIRCH_LEAVES.getLootTable().equals(id) ||
                    Blocks.SPRUCE_LEAVES.getLootTable().equals(id) ||
                    Blocks.ACACIA_LEAVES.getLootTable().equals(id) ||
                    Blocks.DARK_OAK_LEAVES.getLootTable().equals(id) ||
                    Blocks.CHERRY_LEAVES.getLootTable().equals(id) ||
                    Blocks.MANGROVE_LEAVES.getLootTable().equals(id) ||
                    Blocks.JUNGLE_LEAVES.getLootTable().equals(id)
                    )){
                LootPool.Builder pool = LootPool.lootPool()
                        .add(LootItem.lootTableItem(
                                itemRegistrar.get(new ResourceLocation("disx","lacquer_drop"))))
                        .when(LootItemRandomChanceCondition.randomChance(0.04f));

                context.addPool(pool);
            }

        }));
    }
}
