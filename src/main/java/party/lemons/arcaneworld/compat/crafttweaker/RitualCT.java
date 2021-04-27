package party.lemons.arcaneworld.compat.crafttweaker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import party.lemons.arcaneworld.crafting.ritual.Ritual;

import javax.annotation.Nonnull;

public class RitualCT extends Ritual {
    IRitualEffect effect;
    IRitualMatcher matcher;

    public RitualCT(IRitualMatcher matcher, IRitualEffect effect, Ingredient... ingredients) {
        super(ingredients);
        this.effect = effect;
        this.matcher = matcher;
    }

    @Override
    public boolean canCast(@Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        return matcher.test(world, pos, player);
    }

    @Override
    public void onActivate(@Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player, ItemStack... items) {
        effect.activate(world, pos, player, items);
    }
}
