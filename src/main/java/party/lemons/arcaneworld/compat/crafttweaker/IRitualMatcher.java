package party.lemons.arcaneworld.compat.crafttweaker;

import crafttweaker.annotations.ZenRegister;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.api.player.IPlayer;
import crafttweaker.api.world.IBlockPos;
import crafttweaker.api.world.IWorld;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stanhebben.zenscript.annotations.ZenClass;

@ZenClass("mods.arcaneworld.IRitualMatcher")
@ZenRegister
public interface IRitualMatcher {
    boolean test(IWorld world, IBlockPos pos, IPlayer player);

    default boolean test(World world, BlockPos pos, EntityPlayer player) {
        return test(CraftTweakerMC.getIWorld(world), CraftTweakerMC.getIBlockPos(pos), CraftTweakerMC.getIPlayer(player));
    }
}
