package com.vegazsdev.bobobot.core.bot;

/**
 * That class show version info about build.
 */
public record BuildInfo(boolean doStable) {
    public String getVersion() {
        Variables variables = new Variables();

        if (doStable) {
            return variables.VERSION + variables.STABLE;
        } else {
            return variables.VERSION + variables.STAGING;
        }
    }

    private static class Variables {
        public final String VERSION = "v1.5.1-";
        public final String STABLE = "STABLE";
        public final String STAGING = "BETA";
    }
}