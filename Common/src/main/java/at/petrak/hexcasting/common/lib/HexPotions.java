package at.petrak.hexcasting.common.lib;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static at.petrak.hexcasting.api.HexAPI.modLoc;

public class HexPotions {
    // The potions are created here, i.e. while the POTION registry event is firing. Constructing
    // them during class static initialization wrapped the not-yet-registered mob effects in
    // unbound "direct" holders, which every mod that inspects potion data (Alchemancy, Apotheosis'
    // JEI integration, ...) crashes on.
    public static void register(BiConsumer<Potion, ResourceLocation> r) {
        ENLARGE_GRID = make("enlarge_grid",
            new Potion("enlarge_grid", new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HexMobEffects.ENLARGE_GRID), 3600)));
        ENLARGE_GRID_LONG = make("enlarge_grid_long",
            new Potion("enlarge_grid_long", new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HexMobEffects.ENLARGE_GRID), 9600)));
        ENLARGE_GRID_STRONG = make("enlarge_grid_strong",
            new Potion("enlarge_grid_strong", new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(HexMobEffects.ENLARGE_GRID), 1800, 1)));

        for (var e : POTIONS.entrySet()) {
            r.accept(e.getValue(), e.getKey());
        }
    }

    private static final Map<ResourceLocation, Potion> POTIONS = new LinkedHashMap<>();

    public static Potion ENLARGE_GRID;
    public static Potion ENLARGE_GRID_LONG;
    public static Potion ENLARGE_GRID_STRONG;

    public static void addRecipes() {
        /*
        AccessorPotionBrewing.addMix(Potions.AWKWARD, HexItems.AMETHYST_DUST, ENLARGE_GRID);
        AccessorPotionBrewing.addMix(ENLARGE_GRID, Items.REDSTONE, ENLARGE_GRID_LONG);
        AccessorPotionBrewing.addMix(ENLARGE_GRID, Items.GLOWSTONE_DUST, ENLARGE_GRID_STRONG);
         */
    }

    private static <T extends Potion> T make(String id, T potion) {
        var old = POTIONS.put(modLoc(id), potion);
        if (old != null) {
            throw new IllegalArgumentException("Typo? Duplicate id " + id);
        }
        return potion;
    }
}
