package party.lemons.arcaneworld.compat.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;

import java.util.Arrays;

@ZenClass("mods.arcaneworld.IRitualEffect")
@ZenRegister
public interface IRitualEffect {
    void activate(IWorld world, IBlockPos pos, IPlayer player, IItemStack[] items);

    default void activate(World world, BlockPos pos, EntityPlayer player, ItemStack[] items) {
        this.activate(CraftTweakerMC.getIWorld(world), CraftTweakerMC.getIBlockPos(pos), CraftTweakerMC.getIPlayer(player), transformIngredients(items));
    }

    static IItemStack[] transformIngredients(ItemStack[] stacks) {
        return Arrays.stream(stacks).map(CraftTweakerMC::getIItemStack).toArray(IItemStack[]::new);
    }
}
