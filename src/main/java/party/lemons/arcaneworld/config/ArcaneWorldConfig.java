package party.lemons.arcaneworld.config;

import net.minecraftforge.common.config.Config;
import party.lemons.arcaneworld.ArcaneWorld;

/**
 * Created by Sam on 19/09/2018.
 */
@Config(modid = ArcaneWorld.MODID, name = ArcaneWorld.NAME)
public class ArcaneWorldConfig
{
    @Config.Name("Ores")
    public static ConfigOres ORES = new ConfigOres();

    @Config.Name("Entities")
    public static ConfigEntity ENTITIES = new ConfigEntity();

    @Config.Name("Dungeon")
    public static ConfigDungeonDimension DUNGEONS = new ConfigDungeonDimension();

    @Config.Name("Rifts")
    public static ConfigRift RIFTS = new ConfigRift();

    public static class ConfigDungeonDimension
    {
        @Config.Name("Dimension ID")
        public int DIM_ID = -546;
    }

    public static class ConfigRift
    {
        @Config.Name("Spawn Chance")
        @Config.RangeInt(min = 0)
        public int SPAWN_CHANCE = 1000;
    }

    public static class ConfigOres
    {
        @Config.Name("Sapphire Generation")
        public ConfigOreGeneration SAPPHIRE_GENERATION = new ConfigOreGeneration(0, 80, 5, 5);

        @Config.Name("Amethyst Generation")
        public ConfigOreGeneration AMETHYST_GENERATION = new ConfigOreGeneration(0, 80, 8, 15);

        @Config.Name("Amethyst Generation (Nether)")
        public ConfigOreGeneration AMETHYST_GENERATION_NETHER = new ConfigOreGeneration(0, 128, 5, 5);
    }

    public static class ConfigEntity
    {
        @Config.Name("Ritual Scroll Drop Chance")
        @Config.RangeInt(min = 0)
        public int SCROLL_CHANCE = 20;

        @Config.Name("Illusioner Spawn")
        public ConfigEntitySpawn ILLUSIONER_SPAWN = new ConfigEntitySpawn(2, 1, 1, true);

        @Config.Name("Vindicator Spawn")
        public ConfigEntitySpawn VINDICATOR_SPAWN = new ConfigEntitySpawn(2, 1, 1, true);

        @Config.Name("Evoker Spawn")
        public ConfigEntitySpawn EVOKER_SPAWN = new ConfigEntitySpawn(2, 1, 1, true);
    }
}
