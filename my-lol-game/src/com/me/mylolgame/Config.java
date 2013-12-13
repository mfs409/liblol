package com.me.mylolgame;

import edu.lehigh.cse.lol.LOLConfiguration;

// TODO: comment/explain

public class Config extends LOLConfiguration
{
    @Override
    public int getNumLevels()
    {
        return 80;
    }

    @Override
    public int getScreenWidth()
    {
        return 480;
    }

    @Override
    public int getScreenHeight()
    {
        return 320;
    }

    @Override
    public int getNumHelpScenes()
    {
        return 2;
    }

    @Override
    public String getSplashBackground()
    {
        return "splash.png";
    }

    @Override
    public String getSplashMusic()
    {
        return "tune.ogg";
    }

    @Override
    public boolean getVibration()
    {
        return false;
    }

    @Override
    public boolean getDeveloperUnlock()
    {
        return true;
    }

    @Override
    public boolean showDebugBoxes()
    {
        return true;
    }

    @Override
    public String getStorageKey()
    {
        return "com.me.mylolgame.prefs";
    }
}
