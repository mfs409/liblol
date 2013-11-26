package edu.lehigh.cse.ale;

// TODO: comment and explain this

public abstract class ALEConfiguration
{
    /*
     * ABSTRACT METHODS, IN LIEU OF XML CONFIG (FOR NOW)
     */
    abstract public int getNumLevels();

    abstract public int getScreenHeight();

    abstract public int getScreenWidth();

    abstract public int getNumHelpScenes();

    abstract public String getSplashBackground();

    abstract public String getSplashMusic();

    abstract public boolean getVibration();

    abstract public boolean getDeveloperUnlock();

    abstract public boolean showDebugBoxes();

}
