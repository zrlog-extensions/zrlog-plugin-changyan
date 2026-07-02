package com.zrlog.plugin.changyan.controller;

import com.zrlog.plugin.message.Plugin;

public class ChangyanPageData {

    private boolean dark;
    private String colorPrimary;
    private Plugin plugin;
    private ChangyanConfig config;

    public boolean isDark() {
        return dark;
    }

    public void setDark(boolean dark) {
        this.dark = dark;
    }

    public String getColorPrimary() {
        return colorPrimary;
    }

    public void setColorPrimary(String colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public ChangyanConfig getConfig() {
        return config;
    }

    public void setConfig(ChangyanConfig config) {
        this.config = config;
    }
}
