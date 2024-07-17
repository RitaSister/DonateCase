package com.jodexindustries.donatecase.api;

import com.jodexindustries.donatecase.api.addon.Addon;
import com.jodexindustries.donatecase.api.data.CaseAction;
import com.jodexindustries.donatecase.api.events.CaseActionRegisteredEvent;
import com.jodexindustries.donatecase.tools.Pair;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

/**
 * Class for managing executable actions. <br>
 * Used in case configuration like this:
 * <pre>{@code
 *       Actions: # GiveType: ONE
 *         - '[command] lp user %player% parent set %group%'
 *         - '[title] &aCongratulations!;&5you won %groupdisplayname%'
 *         - '[broadcast] &a>&c>&e> &c%player% &6won a donate %groupdisplayname% &6from &5Ultra-Case.'
 *       AlternativeActions: # GiveType: any, it doesn't matter; is performed if the group is lower in rank than the player's group in LevelGroups
 *         - "[message] &cI'm sorry %player%, but you have group a stronger group than you won:("
 *       RandomActions: # GiveType: RANDOM
 *         first:
 *           Chance: 50
 *           DisplayName: "something" # displayname for historydata displaying
 *           Actions:
 *             - '[command] say something'
 *             - '[broadcast] &a>&c>&e> &c%player% &6won a donate %groupdisplayname% &6from &5Ultra-Case.'
 *         second:
 *           Chance: 50
 *           Actions:
 *             - '[command] say something'
 *             - '[broadcast] &a>&c>&e> &c%player% &6won a donate %groupdisplayname% &6from &5Ultra-Case.'
 * }</pre>
 *
 * Default actions like: {@code [command], [broadcast], [message], [title], etc.} loading here.
 */
public class ActionManager {
    private static final Map<String, Pair<CaseAction, Addon>> registeredActions = new HashMap<>();
    private final Addon addon;

    public ActionManager(Addon addon) {
        this.addon = addon;
    }

    public void registerAction(String name, CaseAction action) {
        if(isRegistered(name)) {
            registeredActions.put(name, new Pair<>(action, addon));
            String actionAddonName = addon.getName();
            boolean isDefault = actionAddonName.equalsIgnoreCase("DonateCase");
            CaseActionRegisteredEvent event = new CaseActionRegisteredEvent(name, action, actionAddonName, isDefault);
            Bukkit.getPluginManager().callEvent(event);
        } else {
            Case.getInstance().getLogger().warning("CaseAction with name " + name + " already registered!");
        }
    }

    /**
     * Check for action registration
     * @param name action name
     * @return boolean
     */
    public boolean isRegistered(String name) {
        return getRegisteredActions().containsKey(name);
    }

    /**
     * Get all registered animations
     * @return map with registered animations
     */
    public Map<String, Pair<CaseAction, Addon>> getRegisteredActions() {
        return registeredActions;
    }

    /**
     * Get registered action
     * @param action CaseAction name
     * @return CaseAction class instance
     */
    private CaseAction getRegisteredAction(String action) {
        if (isRegistered(action)) {
            return getRegisteredActions().get(action).getFirst();
        }
        return null;
    }
}