package com.vegazsdev.bobobot.core.bot;

public class BuildInfo {

    private final boolean doStable;

    public BuildInfo(boolean doStable) {
        this.doStable = doStable;
    }

    public String getVersion() {
        Variables variables = new Variables();

        if (doStable) {
            return variables.VERSION + variables.STABLE;
        } else {
            return variables.VERSION + variables.STAGING;
        }
    }

    private static class Variables {
        public final String VERSION = "v1.0.1-";
        public final String STABLE = "STABLE";
        public final String STAGING = "BETA";
    }
}