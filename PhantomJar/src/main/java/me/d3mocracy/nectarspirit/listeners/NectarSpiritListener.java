package me.d3mocracy.nectarspirit.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;

public class NectarSpiritListener implements Listener {
    private final Plugin plugin;
    private String pokeballDisplayName = "§f§lSpírit §6§lNéctár✨";

    public NectarSpiritListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPhantomJarUsedInCrafting(PrepareItemCraftEvent event) {
        CraftingInventory inventory = event.getInventory();

        for (ItemStack item : inventory.getMatrix()) {
            if (item != null && isFullPhantomJar(item)) {
                // Cancel crafting by removing the result
                event.getInventory().setResult(new ItemStack(Material.AIR));
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityRightClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();

        if (isPhantomJar(mainHandItem) && event.getHand() == EquipmentSlot.HAND) {
            Entity entity = event.getRightClicked();
            String entityType = entity.getType().toString();

            event.setCancelled(true);
            if (entity instanceof Player) {
                player.sendMessage("§7I don't like him either...");
                return;
            }

            if (entity instanceof LivingEntity) {
                List<String> blacklistNames = plugin.getConfig().getStringList("phantomJar.blacklistEntities");
                Set<EntityType> blacklist = blacklistNames.stream()
                        .map(name -> {
                            try {
                                return EntityType.valueOf(name.toUpperCase());
                            } catch (IllegalArgumentException ex) {
                                plugin.getLogger().warning("Invalid entity type in blacklist: " + name);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                if (blacklist.contains(entity.getType())) {
                    player.sendMessage(ChatColor.RED + "That soul is far too powerful to bottle!");
                    return;
                }

                mainHandItem.setAmount(mainHandItem.getAmount() - 1);
                ItemStack omniBottle = new ItemStack(Material.HONEY_BOTTLE);
                ItemMeta omniMeta = omniBottle.getItemMeta();
                omniMeta.setDisplayName(pokeballDisplayName);
                omniMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                omniMeta.addItemFlags(ItemFlag.values());
                omniMeta.addEnchant(Enchantment.BINDING_CURSE, 1, false);
                omniMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

                ArrayList<String> lore = new ArrayList<String>();
                String rawName = entityType.toLowerCase().replace("_", " ");
                String capitalized = rawName.substring(0, 1).toUpperCase() + rawName.substring(1);
                lore.add("§7Soul Essence: §f" + capitalized);
                lore.add("§8Distilled from the echoes of the void...");
                lore.add("§7Only the brave or foolish would dare drink this.");

                omniMeta.setLore(lore);

                PersistentDataContainer container = omniMeta.getPersistentDataContainer();
                NamespacedKey entityDataKey = new NamespacedKey(plugin, "entityData");
                container.set(entityDataKey, PersistentDataType.STRING, entityType);
                NBTEntity nbtEntity = new NBTEntity((LivingEntity) entity);
                String nbtData = nbtEntity.toString();
                container.set(new NamespacedKey(plugin, "entityNBT"), PersistentDataType.STRING, nbtData);

                omniBottle.setItemMeta(omniMeta);
                player.getWorld().dropItemNaturally(entity.getLocation(), omniBottle);

                playSoulCaptureEffect(player, entity);

                entity.remove();

            } else {
                player.sendMessage(ChatColor.RED + "You can only capture living entities!");
            }
        }
    }

    private void playSoulCaptureEffect(Player player, Entity entity) {
        Location playerLocation = player.getLocation().add(0, 1.5, 0);
        Location entityLocation = entity.getLocation().add(0, 1, 0);

        Vector direction = playerLocation.toVector().subtract(entityLocation.toVector()).normalize();

        for (int i = 0; i < 20; i++) {
            Location particleLocation = entityLocation.clone().add(direction.clone().multiply(i * 0.2));
            player.getWorld().spawnParticle(Particle.SOUL, particleLocation, 1, 0.1, 0.1, 0.1, 0);
            // player.getWorld().spawnParticle(Particle.END_ROD, particleLocation, 1, 0.1,
            // 0.1, 0.1, 0);
            player.spawnParticle(Particle.END_ROD, particleLocation, 0, 0, 0, 0, 0);
            player.getWorld().playSound(particleLocation, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.0f);
        }
    }

    @EventHandler
    private void onConsumePotion(PlayerItemConsumeEvent event) {
        if (isFullPhantomJar(event.getItem())) {
            PersistentDataContainer container = event.getItem().getItemMeta().getPersistentDataContainer();
            NamespacedKey key = new NamespacedKey(plugin, "entityData");
            String entityName = container.get(key, PersistentDataType.STRING).toLowerCase().replace("_", " ");

            List<String> messages = plugin.getConfig().getStringList("phantomJar.consumeMessages");

            if (!messages.isEmpty()) {
                Random rand = new Random();
                String raw = messages.get(rand.nextInt(messages.size()));
                String parsedMessage = raw.replace("{entity}", entityName);
                event.getPlayer().sendMessage("§7" + parsedMessage);
            }

            event.setCancelled(true);
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (isPhantomJar(item)) {
            event.setCancelled(true);
            return;
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isFullPhantomJar(item)
                && event.getClickedBlock() != null) {
            Location clickedBlockLocation = event.getClickedBlock().getLocation();
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                PersistentDataContainer container = meta.getPersistentDataContainer();
                NamespacedKey key = new NamespacedKey(plugin, "entityData");
                NamespacedKey keyNBT = new NamespacedKey(plugin, "entityNBT");

                // Check if the container has the entity data
                if (container.has(key, PersistentDataType.STRING)) {
                    String nbtData = container.get(keyNBT, PersistentDataType.STRING);
                    String entityTypeData = container.get(key, PersistentDataType.STRING);

                    EntityType entityType = EntityType.valueOf(entityTypeData);

                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_HURT, 0.5f, 1.0f);
                    Entity ent = player.getWorld().spawnEntity(clickedBlockLocation.add(0.5, 1, 0.5), entityType);
                    ReadWriteNBT nbt = NBT.parseNBT(nbtData);
                    nbt.removeKey("Pos");

                    NBTEntity nbtEntity = new NBTEntity(ent);
                    nbtEntity.mergeCompound(nbt);

                    item.setAmount(item.getAmount() - 1);
                    player.getInventory().addItem(new ItemStack(Material.GLASS_BOTTLE));
                    // player.sendMessage("Whoosh!");

                } else {
                    player.sendMessage(ChatColor.RED + "No entity data found!"); // This message indicates the issue
                }
            } else {
                player.sendMessage(ChatColor.RED + "Error: Item meta is null!");
            }
        }
    }

    public ItemStack createPockiBall() {
        ItemStack pockiBall = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = pockiBall.getItemMeta();
        meta.setDisplayName(pokeballDisplayName);
        pockiBall.setItemMeta(meta);
        return pockiBall;
    }

    private boolean isPhantomJar(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(pokeballDisplayName)
                && item.getType().equals(Material.GLASS_BOTTLE);
    }

    private boolean isFullPhantomJar(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals(pokeballDisplayName)
                && item.getType().equals(Material.HONEY_BOTTLE);
    }
}
