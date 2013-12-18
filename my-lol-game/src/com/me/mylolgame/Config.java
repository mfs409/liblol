package com.me.mylolgame;

import edu.lehigh.cse.lol.LOLConfiguration;

/**
 * This file provides configuration information about the game
 */
public class Config implements LOLConfiguration
{
    /**
     * The width of the screen of your device, in pixels. The actual value here isn't too important, the main point is
     * that the ratio between this and height will determine how LOL scales your game.
     */
    @Override
    public int getScreenWidth()
    {
        return 480;
    }

    /**
     * The height of the screen of your device, in pixels.
     */
    @Override
    public int getScreenHeight()
    {
        return 320;
    }

    /**
     * The number of levels in the game
     */
    @Override
    public int getNumLevels()
    {
        return 80;
    }

    /**
     * The number of help levels in the game
     */
    @Override
    public int getNumHelpScenes()
    {
        return 2;
    }

    /**
     * Is vibration support enabled?
     */
    @Override
    public boolean getVibration()
    {
        return false;
    }

    /**
     * Is the game in "unlock mode"? In unlock mode, every level can be played. When unlock mode is false, you can't
     * play a level if you haven't already finished the preceding level.
     */
    @Override
    public boolean getUnlockMode()
    {
        return true;
    }

    /**
     * Is the game in "debug mode"? Debug mode includes drawing the frames-per-second on every level, drawing
     * boxes around every touchable element, and drawing the raw shape for every entity in the game
     */
    @Override
    public boolean showDebugBoxes()
    {
        return true;
    }

    /**
     * To store "unlock" information, we need a unique identity for this game. When you rename "com.me.mylolgame", be
     * sure to update this!
     */
    @Override
    public String getStorageKey()
    {
        return "com.me.mylolgame.prefs";
    }

    /**
     * When getting started, it's useful to use the "easy" versions of some methods, which rely on an implicit default
     * font. You should provide the name of the font through this file, and be sure the font file (with a .ttf
     * extension) is in your assets folder.
     */
    @Override
    public String getDefaultFont()
    {
        return "arial.ttf";
    }

    /**
     * When getting started, you may wish to use default text at the end of each level. This is the default text when
     * winning the level:
     */
    public String getDefaultWinText()
    {
        return "Good Job";
    }

    /**
     * When getting started, you may wish to use default text at the end of each level. This is the default text when
     * losing the level:
     */
    public String getDefaultLoseText()
    {
        return "Try Again";
    }
}
