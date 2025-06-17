package me.d3mocracy.nectarspirit.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class NectarSpiritRecipe {
    public static void register(JavaPlugin plugin) {
        ItemStack phantomJar = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta phantomJarMeta = phantomJar.getItemMeta();
        phantomJarMeta.setDisplayName("§f§lSpírit §6§lNéctár✨");
        phantomJarMeta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
        phantomJarMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        phantomJar.setItemMeta(phantomJarMeta);

        ShapelessRecipe phantomJarRecipe = new ShapelessRecipe(new NamespacedKey(plugin, "kingdomphantomjar"),
                phantomJar);
        phantomJarRecipe.addIngredient(Material.GLASS_BOTTLE);
        phantomJarRecipe.addIngredient(Material.PHANTOM_MEMBRANE);
        phantomJarRecipe.addIngredient(Material.SOUL_SAND);

        plugin.getServer().addRecipe(phantomJarRecipe);
    }
}
