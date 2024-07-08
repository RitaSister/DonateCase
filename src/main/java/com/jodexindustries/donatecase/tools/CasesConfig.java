package com.jodexindustries.donatecase.tools;

import com.jodexindustries.donatecase.api.Case;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Class for loading cases configurations
 */
public class CasesConfig {
    private final Map<String, Pair<File, YamlConfiguration>> cases = new HashMap<>();

    /**
     * Default initialization constructor
     */
    public CasesConfig() {
        for (File file : getCasesInFolder()) {
            if (isYamlFile(file)) {
                String name = getFileNameWithoutExtension(file);
                YamlConfiguration caseConfig = YamlConfiguration.loadConfiguration(file);
                if (caseConfig.getConfigurationSection("case") != null) {
                    if (!processItems(caseConfig, name, file)) {
                        Case.getInstance().getLogger().warning("Case " + name + " has a broken case.Items section, skipped.");
                    }
                    cases.put(name, new Pair<>(file, caseConfig));
                } else {
                    Case.getInstance().getLogger().warning("Case " + name + " has a broken case section, skipped.");
                }
            }
        }
    }

    private boolean isYamlFile(File file) {
        return file.getName().endsWith(".yml");
    }

    private String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.length() - 4); // remove ".yml"
    }

    private boolean processItems(YamlConfiguration caseConfig, String caseName, File file) {
        ConfigurationSection itemsSection = caseConfig.getConfigurationSection("case.Items");
        if (itemsSection == null) {
            return false;
        }

        boolean isOld = false;
        for (String item : itemsSection.getKeys(false)) {
            List<String> actions = new ArrayList<>();
            String giveCommand = caseConfig.getString("case.Items." + item + ".GiveCommand");
            List<String> giveCommands = caseConfig.getStringList("case.Items." + item + ".Commands");
            ConfigurationSection giveSection = caseConfig.getConfigurationSection("case.Items." + item + ".GiveCommands");

            if (giveCommand != null || !giveCommands.isEmpty() || giveSection != null) {
                if (!isOld) {
                    isOld = true;
                    Case.getInstance().getLogger().warning("Case " + caseName + " outdated! Converting...");
                }
                actions.addAll(collectActions(caseConfig, item, giveCommand, giveCommands));
                actions.addAll(collectRandomActions(caseConfig, item, giveSection));

                caseConfig.set("case.Items." + item + ".Actions", actions);
                clearOldConfig(caseConfig, item);
                saveConfig(caseConfig, file);
            }
        }
        return true;
    }

    private List<String> collectActions(YamlConfiguration caseConfig, String item, String giveCommand, List<String> giveCommands) {
        List<String> actions = new ArrayList<>();
        if (giveCommand != null) {
            actions.add("[command] " + giveCommand);
        }
        for (String command : giveCommands) {
            actions.add("[command] " + command);
        }
        String title = caseConfig.getString("case.Items." + item + ".Title");
        String subTitle = caseConfig.getString("case.Items." + item + ".SubTitle");
        List<String> broadcast = caseConfig.getStringList("case.Items." + item + ".Broadcast");
        actions.add("[title] " + title + ";" + subTitle);
        for (String line : broadcast) {
            actions.add("[broadcast] " + line);
        }
        return actions;
    }

    private List<String> collectRandomActions(YamlConfiguration caseConfig, String item, ConfigurationSection giveSection) {
        List<String> randomActions = new ArrayList<>();
        if (giveSection != null) {
            for (String choice : giveSection.getKeys(false)) {
                int chance = caseConfig.getInt("case.Items." + item + ".GiveCommands." + choice + ".Chance");
                List<String> choiceActions = new ArrayList<>();
                List<String> choiceCommands = caseConfig.getStringList("case.Items." + item + ".GiveCommands." + choice + ".Commands");
                for (String choiceCommand : choiceCommands) {
                    choiceActions.add("[command] " + choiceCommand);
                }
                List<String> choiceBroadcasts = caseConfig.getStringList("case.Items." + item + ".GiveCommands." + choice + ".Broadcast");
                for (String choiceBroadcast : choiceBroadcasts) {
                    choiceActions.add("[broadcast] " + choiceBroadcast);
                }
                caseConfig.set("case.Items." + item + ".RandomActions." + choice + ".Chance", chance);
                caseConfig.set("case.Items." + item + ".RandomActions." + choice + ".Actions", choiceActions);
            }
        }
        return randomActions;
    }

    private void clearOldConfig(YamlConfiguration caseConfig, String item) {
        caseConfig.set("case.Items." + item + ".Title", null);
        caseConfig.set("case.Items." + item + ".SubTitle", null);
        caseConfig.set("case.Items." + item + ".Commands", null);
        caseConfig.set("case.Items." + item + ".GiveCommand", null);
        caseConfig.set("case.Items." + item + ".Broadcast", null);
        caseConfig.set("case.Items." + item + ".GiveCommands", null);
    }

    private void saveConfig(YamlConfiguration caseConfig, File file) {
        try {
            caseConfig.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<File> getCasesInFolder() {
        List<File> files = new ArrayList<>();
        File directory = new File(Case.getInstance().getDataFolder(), "cases");
        Collections.addAll(files, Objects.requireNonNull(directory.listFiles()));
        return files;
    }

    /**
     * Get all cases configurations
     *
     * @return map of configuration
     */
    public Map<String, Pair<File, YamlConfiguration>> getCases() {
        return cases;
    }

    /**
     * Get case configuration
     *
     * @param name Case type (file name without .yml)
     * @return case configuration
     */
     public Pair<File, YamlConfiguration> getCase(String name) {
        return cases.get(name);
     }
}
