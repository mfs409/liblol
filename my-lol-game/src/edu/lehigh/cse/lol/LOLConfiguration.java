package edu.lehigh.cse.lol;

// TODO: comment and explain this

public abstract class LOLConfiguration
{
    /*
     * ABSTRACT METHODS, IN LIEU OF XML CONFIG (FOR NOW)
     */
    abstract public int getNumLevels();

    abstract public int getScreenHeight();

    abstract public int getScreenWidth();

    abstract public int getNumHelpScenes();

    abstract public boolean getVibration();

    abstract public boolean getDeveloperUnlock();

    abstract public boolean showDebugBoxes();

    abstract public String getStorageKey();
}
