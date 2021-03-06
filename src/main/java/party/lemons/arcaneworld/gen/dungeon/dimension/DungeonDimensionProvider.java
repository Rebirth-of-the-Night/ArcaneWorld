package party.lemons.arcaneworld.gen.dungeon.dimension;

import net.minecraft.client.audio.MusicTicker;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import party.lemons.arcaneworld.ArcaneWorld;
import party.lemons.arcaneworld.proxy.ClientProxy;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Created by Sam on 22/09/2018.
 */
public class DungeonDimensionProvider extends WorldProvider
{

    @Override
    public BiomeProvider getBiomeProvider()
    {
        Set<Biome> biomes = BiomeDictionary.getBiomes(BiomeDictionary.Type.VOID);
        for (Biome biome : biomes)
        {
            if (biome.getRegistryName().toString().equals(ArcaneWorld.MODID + ":arcane_dungeon"))
            {
                return new BiomeProviderSingle(biome);
            }
        }
        return this.biomeProvider;
    }

    public DimensionType getDimensionType()
    {
        return DungeonDimension.TYPE;
    }

    @Override
    public String getSaveFolder()
    {
        return "dungeons";
    }

    public IChunkGenerator createChunkGenerator()
    {
        return new ChunkGeneratorEmpty(world);
    }

    public boolean isSurfaceWorld()
    {
        return false;
    }

    public boolean canRespawnHere()
    {
        return false;
    }

    protected void generateLightBrightnessTable()
    {
        for (int i = 0; i <= 15; ++i)
        {
            float f1 = 1.0F - (float)i / 15.0F;
            this.lightBrightnessTable[i] = 0.12F + ((1.0F - f1) / (f1 * 3.0F + 1.0F) * 1.0F) / 4F;
        }
    }

    public void getLightmapColors(float partialTicks, float sunBrightness, float skyLight, float blockLight, float[] colors)
    {
        colors[0] = colors[0] / (1.5F / (blockLight * 5));
        colors[1] = colors[1] / (1.5F / (blockLight * 5));
      //  colors[2] = colors[2] * (1.1F / (blockLight * 5));
    }

    @Nullable
    @Override
    @SideOnly(Side.CLIENT)
    public MusicTicker.MusicType getMusicType()
    {
        return ClientProxy.DUNGEON_MUSIC_TYPE;
    }
}

