package com.jodexindustries.donatecase.gui;

import com.jodexindustries.donatecase.DonateCase;
import com.jodexindustries.donatecase.api.data.MaterialType;
import com.jodexindustries.donatecase.api.data.CaseData;
import com.jodexindustries.donatecase.tools.Tools;
import com.jodexindustries.donatecase.tools.support.CustomHeadSupport;
import com.jodexindustries.donatecase.tools.support.HeadDatabaseSupport;
import com.jodexindustries.donatecase.tools.support.ItemsAdderSupport;
import com.jodexindustries.donatecase.tools.support.PAPISupport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.jodexindustries.donatecase.DonateCase.*;

public class CaseGui {
    private final Inventory inventory;

    public CaseGui(Player p, CaseData caseData) {
        String title = caseData.getCaseTitle();
        String c = caseData.getCaseName();
        YamlConfiguration configCase = casesConfig.getCase(c);
        inventory = Bukkit.createInventory(null, configCase.getInt("case.Gui.Size", 45), Tools.rc(title));
        ConfigurationSection items = configCase.getConfigurationSection("case.Gui.Items");
        Bukkit.getScheduler().runTaskAsynchronously(instance, () -> {
            List<CaseData.HistoryData> globalHistoryData = DonateCase.api.getSortedHistoryData();
            if (items != null) {
            for (String item : items.getKeys(false)) {
                String material = configCase.getString("case.Gui.Items." + item + ".Material", "STONE");
                String displayName = configCase.getString("case.Gui.Items." + item + ".DisplayName", "None");
                int keys;
                String placeholder = Tools.getLocalPlaceholder(displayName);
                if (placeholder.startsWith("keys_")) {
                    String[] parts = placeholder.split("_");
                    String caseName = parts[1];
                    keys = DonateCase.api.getKeys(caseName, p.getName());
                } else {
                    keys = DonateCase.api.getKeys(c, p.getName());
                }
                displayName.replace("%" + placeholder + "%", String.valueOf(keys));
                if(instance.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                    displayName = PAPISupport.setPlaceholders(p, displayName);
                }
                boolean enchanted = configCase.getBoolean("case.Gui.Items." + item + ".Enchanted");
                String itemType = configCase.getString("case.Gui.Items." + item + ".Type", "DEFAULT");
                List<String> lore = Tools.rc(configCase.getStringList("case.Gui.Items." + item + ".Lore"));
                int modelData = configCase.getInt("case.Gui.Items." + item + ".ModelData", -1);
                String[] rgb = null;
                List<String> pLore = new ArrayList<>();
                for(String line : lore) {
                    if(instance.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                        line = PAPISupport.setPlaceholders(p, line);
                    }
                    pLore.add(line);
                }
                lore = Tools.rc(pLore);
                if (material.toUpperCase().startsWith("LEATHER_")) {
                    String rgbString = configCase.getString("case.Gui.Items." + item + ".Rgb");
                    if (rgbString != null) {
                        rgb = rgbString.replace(" ", "").split(",");
                    }
                }
                if (itemType.startsWith("HISTORY")) {
                    String[] typeArgs = itemType.split("-");
                    int index = Integer.parseInt(typeArgs[1]);
                    String caseType = (typeArgs.length >= 3) ? typeArgs[2] : c;
                    boolean isGlobal = caseType.equalsIgnoreCase("GLOBAL");

                    CaseData historyCaseData = isGlobal ? null : DonateCase.api.getCase(caseType);
                    if (historyCaseData == null && !isGlobal) {
                        instance.getLogger().warning("Case " + caseType + " HistoryData is null!");
                        continue;
                    }
                    if (!isGlobal) {
                        historyCaseData = historyCaseData.clone();
                    }

                    CaseData.HistoryData data = null;
                    if (isGlobal) {
                        if(globalHistoryData.size() <= index) continue;
                        data = globalHistoryData.get(index);
                    } else {
                        if(!sql) {
                            data = historyCaseData.getHistoryData()[index];
                        } else {
                            List<CaseData.HistoryData> dbData = DonateCase.api.sortHistoryDataByCase(globalHistoryData,caseType);
                            if(!dbData.isEmpty()) {
                                if(dbData.size() <= index) continue;
                                data = dbData.get(index);
                            }
                        }
                    }
                    if (data == null) continue;
                    if (isGlobal) historyCaseData = DonateCase.api.getCase(data.getCaseType());
                    if(historyCaseData == null) continue;

                    CaseData.Item historyItem = historyCaseData.getItem(data.getItem());
                    if(historyItem == null) continue;
                    material = configCase.getString("case.Gui.Items." + item + ".Material", "HEAD:" + data.getPlayerName());
                    if(material.equalsIgnoreCase("DEFAULT")) {
                        material = historyItem.getMaterial().getId();
                    }
                    CaseData.Item.RandomAction randomAction = historyItem.getRandomAction(data.getAction());
                    String randomActionDisplayname = randomAction != null ? randomAction.getDisplayName() : "";
                    DateFormat formatter = new SimpleDateFormat(customConfig.getConfig().getString("DonatCase.DateFormat", "dd.MM HH:mm:ss"));
                    String dateFormatted = formatter.format(new Date(data.getTime()));
                    String groupDisplayName = data.getItem() != null ? historyItem.getMaterial().getDisplayName() : "open_case_again";
                    String[] template = {"%action%:" + data.getAction(), "%actiondisplayname%:" + randomActionDisplayname, "%casedisplayname%:" + historyCaseData.getCaseDisplayName(), "%casename%:" + data.getCaseType(), "%casetitle%:" + historyCaseData.getCaseTitle(), "%time%:" + dateFormatted, "%group%:" + data.getGroup(), "%player%:" + data.getPlayerName(), "%groupdisplayname%:" + groupDisplayName};
                    displayName = Tools.rt(displayName, template);
                    lore = Tools.rt(lore, template);
                }

                List<String> slots = new ArrayList<>();
                if (configCase.isList("case.Gui.Items." + item + ".Slots")) {
                    List<String> temp = configCase.getStringList("case.Gui.Items." + item + ".Slots");
                    for (String slot : temp) {
                        String[] values = slot.split("-", 2);
                        if (values.length == 2) {
                            for (int i = Integer.parseInt(values[0]); i <= Integer.parseInt(values[1]); i++) {
                                slots.add(String.valueOf(i));
                            }
                        } else {
                            slots.add(slot);
                        }
                    }
                } else {
                    String[] slotArgs = configCase.getString("case.Gui.Items." + item + ".Slots", "0-0").split("-");
                    int range1 = Integer.parseInt(slotArgs[0]);
                    int range2 = range1;
                    if(slotArgs.length >= 2) {
                        range2 = Integer.parseInt(slotArgs[1]);
                    }
                    slots.addAll(IntStream.rangeClosed(range1, range2).mapToObj(String::valueOf).collect(Collectors.toList()));
                }

                ItemStack itemStack = getItem(material, displayName, lore, c, p, enchanted, rgb, modelData);
                for (String slot : slots) {
                    inventory.setItem(Integer.parseInt(slot), itemStack);
                }
            }
        }
        });
        p.openInventory(inventory);
    }
    public Inventory getInventory() {
        return inventory;
    }
    private ItemStack getItem(String material, String displayName, List<String> lore, String c, Player p, boolean enchanted, String[] rgb, int modelData) {
        int keys = DonateCase.api.getKeys(c, p.getName());
        List<String> newLore = new ArrayList<>();
        if(lore != null) {
            for (String string : lore) {
                String placeholder = Tools.getLocalPlaceholder(string);
                if (placeholder.startsWith("keys_")) {
                    String[] parts = placeholder.split("_");
                    String caseName = parts[1];
                    keys = DonateCase.api.getKeys(caseName, p.getName());
                }
                newLore.add(string.replace("%" + placeholder + "%", String.valueOf(keys)));
            }
        }
        MaterialType materialType = Tools.getMaterialType(material);
        Material itemMaterial;
        ItemStack item;
        if(!material.contains(":")) {
            itemMaterial = Material.getMaterial(material);
            if (itemMaterial == null) {
                itemMaterial = Material.STONE;
            }
            item = Tools.createItem(itemMaterial, -1, 1, displayName, Tools.rt(newLore,"%case%:" + c), enchanted, rgb, modelData);
        } else
        if(materialType == MaterialType.HEAD) {
            String[] parts = material.split(":");
            item = Tools.getPlayerHead(parts[1], displayName, Tools.rt(newLore,"%case%:" + c));
        } else
        if(materialType == MaterialType.HDB) {
            String[] parts = material.split(":");
            String id = parts[1];
            item = HeadDatabaseSupport.getSkull(id, displayName, Tools.rt(newLore, "%case%:" + c));
        } else
        if(materialType == MaterialType.CH) {
            String[] parts = material.split(":");
            String category = parts[1];
            String id = parts[2];
            item = CustomHeadSupport.getSkull(category, id, displayName, Tools.rt(newLore, "%case%:" + c));
        } else if (materialType == MaterialType.IA) {
            String[] parts = material.split(":");
            String namespace = parts[1];
            String id = parts[2];
            item = ItemsAdderSupport.getItem(namespace + ":" + id, displayName, Tools.rt(newLore, "%case%:" + c));
        } else if (materialType == MaterialType.BASE64) {
            String[] parts = material.split(":");
            String base64 = parts[1];
            item = Tools.getBASE64Skull(base64, displayName, Tools.rt(newLore,"%case%:" + c));
        } else {
            String[] parts = material.split(":");
            byte data = -1;
            if(parts[1] != null) {
                data = Byte.parseByte(parts[1]);
            }
            itemMaterial = Material.getMaterial(parts[0]);
            if (itemMaterial == null) {
                itemMaterial = Material.STONE;
            }
            item = Tools.createItem(itemMaterial, data, 1, displayName, Tools.rt(newLore,"%case%:" + c), enchanted, null, -1);
        }
        return item;
    }
}
