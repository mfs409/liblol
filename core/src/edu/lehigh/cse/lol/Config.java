package edu.lehigh.cse.lol;

/**
 * Config stores game-specific configuration of LOL.  It consists primarily of default values.
 */
public class Config {
    /**
     * This ratio means that every 20 pixels on the screen will correspond to a
     * meter. Note that 'pixels' are defined in terms of what a programmer's
     * configuration() says, not the actual screen size, because the
     * configuration gets scaled to screen dimensions. The default is 960x640.
     */
    public float PIXEL_METER_RATIO = 20;

    /**
     * The default screen width (note: it will be stretched appropriately on a
     * phone)
     */
    public int mWidth;

    /**
     * The default screen height (note: it will be stretched appropriately on a
     * phone)
     */
    public int mHeight;

    /**
     * This is a debug feature, to help see the physics behind every Actor
     */
    public boolean mShowDebugBoxes;

    /**
     * Title of the game (for desktop mode)
     */
    public String mGameTitle;

    /**
     * The total number of levels. This is only useful for knowing what to do
     * when the last level is completed.
     */
    protected int mNumLevels;

    /**
     * Should the phone vibrate on certain events?
     */
    protected boolean mEnableVibration;

    /**
     * Should all levels be unlocked?
     */
    protected boolean mUnlockAllLevels;

    /**
     * A per-game string, to use for storing information on an Android device
     */
    public String mStorageKey;

    /**
     * Default font face
     *
     * TODO: can we get rid of this?
     */
    protected String mDefaultFontFace;

    /**
     * Default font size
     */
    protected int mDefaultFontSize;

    /**
     * Default font color, as #RRGGBB value
     */
    protected String mDefaultFontColor;

    /**
     * Default text to display when a level is won
     */
    protected String mDefaultWinText;

    /**
     * Default text to display when a level is lost
     */
    protected String mDefaultLoseText;

    /**
     * Should the level chooser be activated?
     */
    protected boolean mEnableChooser;

    /**
     * The levels of the game are drawn by this object
     */
    protected ScreenManager mLevels;

    /**
     * The chooser is drawn by this object
     */
    protected ScreenManager mChooser;

    /**
     * The help screens are drawn by this object
     */
    protected ScreenManager mHelp;

    /**
     * The splash screen is drawn by this object
     */
    protected ScreenManager mSplash;

    /**
     * The store is drawn by this object
     */
    protected ScreenManager mStore;

    /**
     * The list of image files that will be used by the game
     */
    protected String[] mImageNames;

    /**
     * The list of audio files that will be used as sound effects by the game
     */
    protected String[] mSoundNames;

    /**
     * The list of audio files that will be used as (looping) background music by the game
     */
    protected String[] mMusicNames;
}
