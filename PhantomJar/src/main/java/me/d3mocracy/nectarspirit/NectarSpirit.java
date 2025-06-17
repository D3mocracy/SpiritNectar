package me.d3mocracy.nectarspirit;


import org.bukkit.plugin.java.JavaPlugin;
import me.d3mocracy.nectarspirit.listeners.NectarSpiritListener;
import me.d3mocracy.nectarspirit.listeners.NectarSpiritRecipe;
import me.d3mocracy.nectarspirit.listeners.PlayerListener;
import me.d3mocracy.nectarspirit.managers.PluginManager;

public class NectarSpirit extends JavaPlugin {

    @Override
    public void onEnable() {

        // Initialize managers
        PluginManager.getInstance().initialize();

        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        getServer().getPluginManager().registerEvents(new NectarSpiritListener(this), this);

        //Recipes
        NectarSpiritRecipe.register(this);

        saveDefaultConfig();

        getLogger().info("NectarSpirit has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("NectarSpirit has been disabled!");
    }

}