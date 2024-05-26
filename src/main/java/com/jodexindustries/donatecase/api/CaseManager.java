package com.jodexindustries.donatecase.api;

import com.jodexindustries.donatecase.api.addon.Addon;
import com.jodexindustries.donatecase.api.addon.external.ExternalJavaAddon;
import com.jodexindustries.donatecase.api.holograms.HologramManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import static com.jodexindustries.donatecase.DonateCase.*;


/**
 * The main class for API interaction with DonateCase, this is where most of the functions are located.
 */
public class CaseManager {
    private final AddonManager addonManager;
    private final AnimationManager animationManager;
    private final SubCommandManager subCommandManager;
    private final Addon addon;
    public CaseManager(Addon addon) {
        this.addon = addon;
        this.addonManager = new AddonManager();
        this.subCommandManager = new SubCommandManager(addon);
        this.animationManager = new AnimationManager(addon);
    }
    public CaseManager(Plugin plugin) {
        this.addon = new ExternalJavaAddon(plugin);
        this.addonManager = new AddonManager();
        this.subCommandManager = new SubCommandManager(addon);
        this.animationManager = new AnimationManager(addon);
    }

    /**
     * Get plugin instance
     * @return DonateCase instance
     */
    public JavaPlugin getInstance() {
        return instance;
    }

    /**
     * Get addon manager for addons manipulate
     * @return AddonManager instance
     */
    public AddonManager getAddonManager() {
        return this.addonManager;
    }

    /**
     * Get animation manager for animations manipulate
     * @return AnimationManager instance
     */
    public AnimationManager getAnimationManager() {
        return this.animationManager;
    }

    /**
     * Get sub command manager
     * @return SubCommandManager instance
     */
    public SubCommandManager getSubCommandManager() {
        return this.subCommandManager;
    }

    /**
     * Get hologram manager
     * @return HologramManager instance
     */
    public static HologramManager getHologramManager() {
        return hologramManager;
    }

    /**
     * Get addon object
     * Can be cast to Addon from DonateCase if it's really loaded addon by DonateCase
     * @return Addon object
     */
    public Addon getAddon() {
        return addon;
    }
}
