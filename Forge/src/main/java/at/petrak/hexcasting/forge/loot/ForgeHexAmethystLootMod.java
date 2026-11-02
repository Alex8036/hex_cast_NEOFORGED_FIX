package at.petrak.hexcasting.forge.loot;

import at.petrak.hexcasting.common.lib.HexItems;
import at.petrak.hexcasting.forge.lib.ForgeHexLootMods;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ForgeHexAmethystLootMod extends LootModifier {
    public static final Supplier<MapCodec<ForgeHexAmethystLootMod>> CODEC =
        Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(
            inst -> codecStart(inst).and(
                Codec.DOUBLE.fieldOf("shardDelta").forGetter(it -> it.shardDelta)
            ).apply(inst, ForgeHexAmethystLootMod::new)
        ));

    public final double shardDelta;

    public ForgeHexAmethystLootMod(LootItemCondition[] conditionsIn, double shardDelta) {
        super(conditionsIn);
        this.shardDelta = shardDelta;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot,
        LootContext context) {
        var level = context.getLevel();
        if (!(level instanceof ServerLevel)) return generatedLoot;
        var serverLevel = (ServerLevel) level;

        var tool = context.getParamOrNull(LootContextParams.TOOL);
        boolean goodTool = tool != null && tool.getItem() instanceof PickaxeItem;
        var reg = serverLevel.registryAccess();
        boolean hasSilkTouch = tool != null && EnchantmentHelper.getTagEnchantmentLevel(
            reg.holderOrThrow(Enchantments.SILK_TOUCH), tool) > 0;
        int fortune = tool != null ? EnchantmentHelper.getTagEnchantmentLevel(
            reg.holderOrThrow(Enchantments.FORTUNE), tool) : 0;
        var random = context.getRandom();

        if (!hasSilkTouch) {
            if (goodTool) {
                int dustCount = 1 + random.nextInt(4) + random.nextInt(fortune + 1);
                generatedLoot.add(new ItemStack(HexItems.AMETHYST_DUST, dustCount));

                float chargedChance = switch (fortune) {
                    case 1 -> 0.35f;
                    case 2 -> 0.5f;
                    case 3 -> 0.75f;
                    default -> 0.25f;
                };
                if (random.nextFloat() < chargedChance) {
                    generatedLoot.add(new ItemStack(HexItems.CHARGED_AMETHYST));
                }
            } else {
                int dustCount = random.nextInt(3);
                if (dustCount > 0) {
                    generatedLoot.add(new ItemStack(HexItems.AMETHYST_DUST, dustCount));
                }
                if (random.nextFloat() < 0.125f) {
                    generatedLoot.add(new ItemStack(HexItems.CHARGED_AMETHYST));
                }
            }
        }

        for (var stack : generatedLoot) {
            if (stack.is(Items.AMETHYST_SHARD)) {
                int reduce = (int) Math.ceil(stack.getCount() * -this.shardDelta);
                stack.shrink(reduce);
            }
        }

        return generatedLoot;
    }

    @Override
    public MapCodec<ForgeHexAmethystLootMod> codec() {
        return ForgeHexLootMods.AMETHYST.get();
    }
}
