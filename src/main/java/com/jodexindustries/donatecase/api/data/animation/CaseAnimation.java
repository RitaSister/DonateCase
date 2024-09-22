package com.jodexindustries.donatecase.api.data.animation;

import com.jodexindustries.donatecase.api.addon.Addon;
import com.jodexindustries.donatecase.api.data.Animation;
import com.jodexindustries.donatecase.api.data.JavaAnimation;
import org.jetbrains.annotations.Nullable;

/**
 * Class for custom animation storage
 */
public class CaseAnimation {
    private final Addon addon;
    private final String name;

    private Class<? extends JavaAnimation> animation;
    private Animation oldAnimation;
    private String description;
    private boolean requireSettings;

    public CaseAnimation(String name, Addon addon) {
        this.addon = addon;
        this.name = name;
    }

    /**
     * Gets animation class
     *
     * @return animation class
     */
    @Nullable
    public Class<? extends JavaAnimation> getAnimation() {
        return animation;
    }

    /**
     * Gets addon which registered this animation
     *
     * @return addon animation
     */
    public Addon getAddon() {
        return addon;
    }

    /**
     * Gets animation name
     *
     * @return animation name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets animation description
     *
     * @return animation description
     */
    public String getDescription() {
        return description;
    }

    @Nullable
    public Animation getOldAnimation() {
        return oldAnimation;
    }

    /**
     *
     * @since 2.2.6.2
     * @return Require settings
     */
    public boolean isRequireSettings() {
        return requireSettings;
    }

    public void setAnimation(Class<? extends JavaAnimation> animation) {
        if(this.oldAnimation == null) {
            this.animation = animation;
        } else {
            addon.getLogger().warning("CaseAnimation already has oldAnimation (Animation). Can't add JavaAnimation");
        }
    }

    public void setOldAnimation(Animation oldAnimation) {
        if(this.animation == null) {
            this.oldAnimation = oldAnimation;
            this.requireSettings = false;
        } else {
            addon.getLogger().warning("CaseAnimation already has JavaAnimation. Can't add oldAnimation (Animation)");
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRequireSettings(boolean requireSettings) {
        this.requireSettings = requireSettings;
    }

    /**
     * @since 2.2.6.2
     */
    public static class Builder {
        private final Addon addon;
        private final String name;

        private String description;
        private boolean requireSettings;
        private Class<? extends JavaAnimation> animation;
        private Animation oldAnimation;

        public Builder(String name, Addon addon) {
            this.addon = addon;
            this.name = name;
        }

        public Builder animation(Class<? extends JavaAnimation> animation) {
            this.animation = animation;
            return this;
        }

        @Deprecated
        public Builder oldAnimation(Animation oldAnimation) {
            this.oldAnimation = oldAnimation;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder requireSettings(boolean requireSettings) {
            this.requireSettings = requireSettings;
            return this;
        }

        public CaseAnimation build() {
            CaseAnimation caseAnimation = new CaseAnimation(name, addon);
            caseAnimation.setAnimation(animation);
            caseAnimation.setOldAnimation(oldAnimation);
            caseAnimation.setDescription(description);
            caseAnimation.setRequireSettings(requireSettings);
            return caseAnimation;
        }
    }

}
