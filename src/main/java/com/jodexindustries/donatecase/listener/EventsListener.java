package com.jodexindustries.donatecase.listener;

import com.jodexindustries.donatecase.api.CaseAPI;
import com.jodexindustries.donatecase.api.data.CaseData;
import com.jodexindustries.donatecase.api.events.*;
import com.jodexindustries.donatecase.DonateCase;
import com.jodexindustries.donatecase.tools.Logger;
import com.jodexindustries.donatecase.tools.Tools;
import com.jodexindustries.donatecase.tools.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.logging.Level;

import static com.jodexindustries.donatecase.DonateCase.*;


public class EventsListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Firework && event.getEntity() instanceof Player && event.getDamager().hasMetadata("case")) {
            event.setCancelled(true);
        }
    }

    @EventHandler (priority = EventPriority.HIGH)
    public void onAdminJoined(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (customConfig.getConfig().getBoolean("DonatCase.UpdateChecker")) {
            if (p.hasPermission("donatecase.admin")) {
                new UpdateChecker(DonateCase.instance, 106701).getVersion((version) -> {
                    if (Tools.getPluginVersion(DonateCase.instance.getDescription().getVersion()) < Tools.getPluginVersion(version)) {
                        Tools.msg(p, Tools.rt(DonateCase.customConfig.getLang().getString("UpdateCheck"), "%version:" + version));
                    }

                });
            }
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void InventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        String playerName = p.getName();
        if (CaseAPI.playersGui.containsKey(p.getUniqueId())) {
            String caseType = DonateCase.api.playersGui.get(p.getUniqueId()).getName();
            e.setCancelled(true);
//            if (e.getCurrentItem() == null) return;
            boolean isOpenItem = Tools.getOpenMaterialSlots(caseType).contains(e.getRawSlot());
            Location location = CaseAPI.playersGui.get(p.getUniqueId()).getLocation();
            CaseGuiClickEvent caseGuiClickEvent = new CaseGuiClickEvent(e.getView(), e.getSlotType(), e.getSlot(), e.getClick(), e.getAction(), location, caseType, isOpenItem);
            Bukkit.getServer().getPluginManager().callEvent(caseGuiClickEvent);
            if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY && e.getInventory().getType() == InventoryType.CHEST && isOpenItem) {
                caseType = Tools.getOpenMaterialTypeByMapBySlot(caseType, e.getRawSlot());
                if (DonateCase.api.hasCaseByType(caseType)) {
                    PreOpenCaseEvent event = new PreOpenCaseEvent(p, caseType, location.getBlock());
                    Bukkit.getServer().getPluginManager().callEvent(event);
                    if (!event.isCancelled()) {
                        if (DonateCase.api.getKeys(caseType, playerName) >= 1) {
                            DonateCase.api.removeKeys(caseType, playerName, 1);
                            DonateCase.api.startAnimation(p, location, caseType);
                            OpenCaseEvent openEvent = new OpenCaseEvent(p, caseType, location.getBlock());
                            Bukkit.getServer().getPluginManager().callEvent(openEvent);
                            p.closeInventory();
                        } else {
                            p.closeInventory();
                            Sound sound = null;
                            try {
                                sound = Sound.valueOf(customConfig.getConfig().getString("DonatCase.NoKeyWarningSound"));
                            } catch (IllegalArgumentException ignore) {}
                            if (sound == null) sound = Sound.valueOf("ENTITY_ENDERMEN_TELEPORT");
                            p.playSound(p.getLocation(), sound, 1.0F, 0.4F);
                            String noKey = casesConfig.getCase(caseType).getString("Messages.NoKey");
                            if (noKey == null) noKey = DonateCase.customConfig.getLang().getString("NoKey");
                            Tools.msg(p, noKey);
                        }
                    }
                } else {
                    p.closeInventory();
                    Tools.msg(p, "&cSomething wrong! Contact with server administrator!");
                    DonateCase.instance.getLogger().log(Level.WARNING, "Case with name " + caseType + " not exist!");
                }
            }
        }
    }

    @EventHandler
    public void PlayerInteractEntity(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if(entity instanceof ArmorStand) {
            if (entity.hasMetadata("case")) {
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onAnimationRegistered(AnimationRegisteredEvent e) {
        if(!e.getAnimationName().startsWith("DEFAULT")) {
            Logger.log(ChatColor.GREEN + "Registered new animation with name: " + ChatColor.RED + e.getAnimationName() + ChatColor.GREEN + " from " + ChatColor.RED + e.getAnimationPluginName());
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void PlayerInteract(PlayerInteractEvent e) {
        if(e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player p = e.getPlayer();
            assert e.getClickedBlock() != null;
            Location blockLocation = e.getClickedBlock().getLocation();
            if (DonateCase.api.hasCaseByLocation(blockLocation)) {
                String caseType = DonateCase.api.getCaseTypeByLocation(blockLocation);
                if(caseType == null) return;
                e.setCancelled(true);
                CaseInteractEvent event = new CaseInteractEvent(p, e.getClickedBlock(), caseType);
                Bukkit.getServer().getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    if (!CaseAPI.activeCasesByLocation.containsKey(blockLocation)) {
                        if (DonateCase.api.hasCaseByType(caseType)) {
                            CaseData caseData = DonateCase.api.getCase(caseType);
                            if(caseData == null) return;
                            DonateCase.api.openGui(p, caseData, blockLocation);
                        } else {
                            Tools.msg(p, "&cSomething wrong! Contact with server administrator!");
                            DonateCase.instance.getLogger().log(Level.WARNING, "Case with type: " + caseType + " not found! Check your Cases.yml for broken cases locations.");
                        }
                    } else {
                        Tools.msg(p, DonateCase.customConfig.getLang().getString("HaveOpenCase"));
                    }
                }
            }
        }
    }

    @EventHandler
    public void InventoryClose(InventoryCloseEvent e) {
        Player p = (Player)e.getPlayer();
        if (DonateCase.api.hasCaseByTitle(e.getView().getTitle())) {
            CaseAPI.playersGui.remove(p.getUniqueId());
        }

    }

    @EventHandler
    public void BlockBreak(BlockBreakEvent e) {
        Location loc = e.getBlock().getLocation();
        if (DonateCase.api.hasCaseByLocation(loc)) {
            e.setCancelled(true);
            Tools.msg(e.getPlayer(), DonateCase.customConfig.getLang().getString("DestoryDonatCase"));
        }

    }

}
