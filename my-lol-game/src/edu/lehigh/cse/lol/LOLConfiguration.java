package edu.lehigh.cse.lol;

/**
 * The LOLConfiguration interface describes the key characteristics of the game screen.
 */
public abstract class LOLConfiguration
{
    /**
     * The width of the screen of your device, in pixels. The actual value here isn't too important, the main point is
     * that the ratio between this and height will determine how LOL scales your game.
     */
    abstract public int getScreenWidth();

    /**
     * The height of the screen of your device, in pixels.
     */
    abstract public int getScreenHeight();

    /**
     * The number of levels in the game
     */
    abstract public int getNumLevels();

    /**
     * The number of help levels in the game
     */
    abstract public int getNumHelpScenes();

    /**
     * Is vibration support enabled?
     */
    abstract public boolean getVibration();

    /**
     * Is the game in "unlock mode"? In unlock mode, every level can be played. When unlock mode is false, you can't
     * play a level if you haven't already finished the preceding level.
     */
    abstract public boolean getUnlockMode();

    /**
     * Is the game in "debug mode"? Debug mode includes drawing the frames-per-second on every level, drawing
     * boxes around every touchable element, and drawing the raw shape for every entity in the game
     */
    abstract public boolean showDebugBoxes();

    /**
     * To store "unlock" information, we need a unique identity for this game. It should probably be based on whatever
     * you rename "com.me.mylolgame"
     */
    abstract public String getStorageKey();
}