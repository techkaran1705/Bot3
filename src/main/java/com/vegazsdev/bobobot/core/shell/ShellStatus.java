package com.vegazsdev.bobobot.core.shell;

public class ShellStatus {
    private boolean isRunning;
    private boolean canRun;

    public ShellStatus() {
        isRunning = true;
        canRun = false;
    }

    public void unlockStatus() {
        isRunning = false;
        canRun = true;
    }

    public void lockStatus() {
        isRunning = true;
        canRun = false;
    }

    public boolean canRun() {
        return canRun;
    }

    public boolean isRunning() {
        return isRunning;
    }
}