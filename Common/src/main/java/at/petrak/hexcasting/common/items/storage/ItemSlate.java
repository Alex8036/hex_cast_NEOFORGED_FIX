package at.petrak.hexcasting.common.items.storage;

import at.petrak.hexcasting.annotations.SoftImplement;
import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.item.IotaHolderItem;
import at.petrak.hexcasting.client.gui.PatternTooltipComponent;
import at.petrak.hexcasting.api.utils.NBTHelper;
import at.petrak.hexcasting.common.blocks.circles.BlockEntitySlate;
import at.petrak.hexcasting.common.lib.HexBlockEntities;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import at.petrak.hexcasting.common.misc.PatternTooltip;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class ItemSlate extends BlockItem implements IotaHolderItem {
    public static final ResourceLocation WRITTEN_PRED = modLoc("written");

    public ItemSlate(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public Component getName(ItemStack pStack) {
        var key = "block." + HexAPI.MOD_ID + ".slate." + (hasPattern(pStack) ? "written" : "blank");
        return Component.translatable(key);
    }

    /**
     * The slab block entity data used to live under the legacy {@code BlockEntityTag} item NBT
     * key, which vanilla no longer honors; on Minecraft 1.21 it must be carried by the
     * {@link DataComponents#BLOCK_ENTITY_DATA} component so placement copies the pattern into the
     * placed {@link BlockEntitySlate}.
     */
    public static @Nullable CompoundTag getBlockEntityData(ItemStack stack) {
        var customData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (customData != null) {
            return customData.copyTag();
        }
        // Old saves stored the block entity tag under the legacy "BlockEntityTag" key of the
        // vanilla custom_data component; migrate it into the real component on first read.
        var legacy = NBTHelper.getCompound(stack, "BlockEntityTag");
        if (legacy != null && !legacy.isEmpty()) {
            setBlockEntityData(stack, legacy);
            NBTHelper.remove(stack, "BlockEntityTag");
            return legacy;
        }
        return null;
    }

    public static void setBlockEntityData(@Nullable ItemStack stack, @Nullable CompoundTag bet) {
        if (stack != null) {
            if (bet == null || bet.isEmpty()) {
                stack.set(DataComponents.BLOCK_ENTITY_DATA, null);
            } else {
                // BlockItem.setBlockEntityData stamps the block entity type id into the
                // component (the `minecraft:block_entity_data` component format requires it)
                BlockItem.setBlockEntityData(stack, HexBlockEntities.SLATE_TILE, bet);
            }
        }
    }

    public static boolean hasPattern(ItemStack stack) {
        var bet = getBlockEntityData(stack);
        if (bet != null) {
            return bet.contains(BlockEntitySlate.TAG_PATTERN, Tag.TAG_COMPOUND) &&
                !bet.getCompound(BlockEntitySlate.TAG_PATTERN).isEmpty();
        }
        return false;
    }

    @SoftImplement("IForgeItem")
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (!hasPattern(stack)) {
            setBlockEntityData(stack, null);
        }
        return false;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (!hasPattern(pStack)) {
            setBlockEntityData(pStack, null);
        }
    }

    @Override
    public @Nullable
    CompoundTag readIotaTag(ItemStack stack) {
        var bet = getBlockEntityData(stack);

        if (bet == null || !bet.contains(BlockEntitySlate.TAG_PATTERN, Tag.TAG_COMPOUND)) {
            return null;
        }

        var patTag = bet.getCompound(BlockEntitySlate.TAG_PATTERN);
        if (patTag.isEmpty()) {
            return null;
        }
        var out = new CompoundTag();
        out.putString(HexIotaTypes.KEY_TYPE, "hexcasting:pattern");
        out.put(HexIotaTypes.KEY_DATA, patTag);
        return out;
    }

    @Override
    public boolean writeable(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canWrite(ItemStack stack, Iota datum) {
        return datum instanceof PatternIota || datum == null;
    }

    @Override
    public void writeDatum(ItemStack stack, Iota datum) {
        if (this.canWrite(stack, datum)) {
            if (datum == null) {
                var beTag = getBlockEntityData(stack);
                if (beTag != null) {
                    beTag.remove(BlockEntitySlate.TAG_PATTERN);
                    if (beTag.isEmpty() || beTag.size() == 1 && beTag.contains("id")) {
                        setBlockEntityData(stack, null);
                    } else {
                        setBlockEntityData(stack, beTag);
                    }
                }
            } else if (datum instanceof PatternIota pat) {
                var beTag = getBlockEntityData(stack);
                if (beTag == null) {
                    beTag = new CompoundTag();
                }
                beTag.put(BlockEntitySlate.TAG_PATTERN, pat.getPattern().serializeToNBT());
                setBlockEntityData(stack, beTag);
            }
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        var bet = getBlockEntityData(stack);

        if (bet != null && bet.contains(BlockEntitySlate.TAG_PATTERN, Tag.TAG_COMPOUND)) {
            var patTag = bet.getCompound(BlockEntitySlate.TAG_PATTERN);
            if (!patTag.isEmpty()) {
                var pattern = HexPattern.fromNBT(patTag);
                return Optional.of(new PatternTooltip(pattern, PatternTooltipComponent.SLATE_BG));
            }
        }
        return Optional.empty();
    }
}
