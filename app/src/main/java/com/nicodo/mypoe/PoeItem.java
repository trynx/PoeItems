package com.nicodo.mypoe;

/**
 * Class which holds the items to save each parameter
 * Important: Must catch IllegalArgumentException
 */
public class PoeItem {

    private String fullName;
    private boolean isIdentified;
    private int level;

    public PoeItem(String fullName, boolean isIdentified, int level) {
        this.fullName = fullName;
        this.isIdentified = isIdentified;
        this.level = level;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isIdentified() {
        return isIdentified;
    }

    public int getLevel() {
        return level;
    }

}
