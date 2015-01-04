/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.Timer;

import java.util.TreeMap;

/**
 * A Lol object is the outermost container for all of the functionality of the
 * game. It implements ApplicationListener (through Game), which provides hooks
 * for GDX to render the game, stop it, resume it, etc.
 *
 * Lol is not responsible for doing anything significant. It keeps track of
 * which screen is currently in use, and forwards (through Game) to that screen.
 * Splash screens, Choosers, Help, and playable Levels each implement Screen, so
 * that they can do the real work.
 */
public abstract class Lol extends Game {

    /**
     * Modes of the game: we can be showing the main splash screen, the help
     * screens, the level chooser, the store, or a playable level
     */
    static final int SPLASH = 0;
    static final int HELP = 1;
    static final int CHOOSER = 2;
    static final int STORE = 3;
    static final int PLAY = 4;
    /**
     * A reference to the game object... Since the interfaces are mostly static,
     * we need an instance of a Lol object that the static methods can call.
     */
    public static Lol sGame;
    /**
     * Store string/integer pairs that get reset whenever we restart the program
     */
    final TreeMap<String, Integer> mSessionFacts = new TreeMap<>();
    /**
     * The current level being shown
     */
    public Level mCurrentLevel;
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

    /*
     * GAME CONFIGURATION VARIABLES
     *
     * These get set in MyGame.java
     */
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
    protected String mStorageKey;
    /**
     * Default font face
     */
    protected String mDefaultFontFace;
    /**
     * Default font size
     */
    protected int mDefaultFontSize;
    /**
     * Red component of default font color
     */
    protected int mDefaultFontRed;
    /**
     * Green component of default font color
     */
    protected int mDefaultFontGreen;
    /**
     * Blue component of default font color
     */
    protected int mDefaultFontBlue;
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
     * The current mode of the program (from among the above choices)
     */
    int mMode;
    /**
     * The mode state is used to represent the current level within a mode
     * (i.e., 3rd help screen, or 5th page of the store). Tracking state
     * separately for each mode makes going between a level and the chooser much
     * easier.
     */
    int mModeStates[] = new int[5];
    /**
     * This variable lets us track whether the user pressed 'back' on an
     * android, or 'escape' on the desktop. We are using polling, so we swallow
     * presses that aren't preceded by a release. In that manner, holding 'back'
     * can't exit all the way out... you must press 'back' repeatedly, once for
     * each screen to revert.
     */
    boolean mKeyDown;
    /**
     * Store all the images, sounds, and fonts for the game
     */
    Media mMedia;

    /*
     * INTERNAL METHODS
     */

    /**
     * The constructor just creates a media object and calls configure, so that
     * all of our globals will be set. Doing it this early lets us access the
     * configuration from within the LWJGL (Desktop) main class. That, in turn,
     * lets us get the screen size correct (see the desktop project's Java
     * file).
     */
    public Lol() {
        configure();
    }

    /**
     * Use this to load the splash screen
     */
    public static void doSplash() {
        // reset state of all screens
        for (int i = 0; i < 5; ++i)
            sGame.mModeStates[i] = 1;
        sGame.mMode = SPLASH;
        sGame.mSplash.display(0);
        sGame.setScreen(sGame.mCurrentLevel);
    }

    /**
     * Use this to load the level-chooser screen. Note that when the chooser is
     * disabled, we jump straight to level 1.
     *
     * @param whichChooser The chooser screen to create
     */
    public static void doChooser(int whichChooser) {
        // if chooser disabled, then we either called this from splash, or from
        // a game level
        if (!sGame.mEnableChooser) {
            if (sGame.mMode == PLAY) {
                doSplash();
            } else {
                doLevel(sGame.mModeStates[PLAY]);
            }
            return;
        }
        // the chooser is not disabled... save the choice of level, configure
        // it, and show it.
        sGame.mMode = CHOOSER;
        sGame.mModeStates[CHOOSER] = whichChooser;
        sGame.mChooser.display(whichChooser);
        sGame.setScreen(sGame.mCurrentLevel);
    }

    /**
     * Use this to load a playable level.
     *
     * @param which The index of the level to load
     */
    public static void doLevel(int which) {
        sGame.mModeStates[PLAY] = which;
        sGame.mMode = PLAY;
        sGame.mLevels.display(which);
        sGame.setScreen(sGame.mCurrentLevel);
    }

    /*
     * APPLICATIONLISTENER (GAME) OVERRIDES
     */

    /**
     * Use this to load a help level.
     *
     * @param which The index of the help level to load
     */
    public static void doHelp(int which) {
        sGame.mModeStates[HELP] = which;
        sGame.mMode = HELP;
        sGame.mHelp.display(which);
        sGame.setScreen(sGame.mCurrentLevel);
    }

    /**
     * Use this to load a screen of the store.
     *
     * @param which The index of the help level to load
     */
    public static void doStore(int which) {
        sGame.mModeStates[STORE] = which;
        sGame.mMode = STORE;
        sGame.mStore.display(which);
        sGame.setScreen(sGame.mCurrentLevel);
    }

    /**
     * Use this to quit the game
     */
    public static void doQuit() {
        sGame.getScreen().dispose();
        Gdx.app.exit();
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Use this to manage the state of Mute
     */
    public static void toggleMute() {
        // volume is either 1 or 0
        if (Facts.getGameFact("volume", 1) == 1) {
            // set volume to 0, set image to 'unmute'
            Facts.putGameFact("volume", 0);
        } else {
            // set volume to 1, set image to 'mute'
            Facts.putGameFact("volume", 1);
        }
        // update all music
        Media.resetMusicVolume();
    }

    /**
     * Use this to determine if the game is muted or not. True corresponds to
     * not muted, false corresponds to muted.
     */
    public static boolean getVolume() {
        return Facts.getGameFact("volume", 1) == 1;
    }

    /**
     * Report whether all levels should be treated as unlocked. This is useful
     * in Chooser, where we might need to prevent some levels from being played.
     */
    public static boolean getUnlockMode() {
        return sGame.mUnlockAllLevels;
    }

    /**
     * Vibrate the phone for a fixed amount of time. Note that this only
     * vibrates the phone if the configuration says that vibration should be
     * permitted.
     *
     * @param millis The amount of time to vibrate
     */
    void vibrate(int millis) {
        if (mEnableVibration)
            Gdx.input.vibrate(millis);
    }

    /**
     * We can call this method from the render loop to poll for back presses
     */
    private void handleKeyDown() {
        // if neither BACK nor ESCAPE is being pressed, do nothing, but
        // recognize future presses
        if (!Gdx.input.isKeyPressed(Keys.BACK) && !Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            mKeyDown = false;
            return;
        }
        // if they key is being held down, ignore it
        if (mKeyDown)
            return;
        // recognize a new back press as being a 'down' press
        mKeyDown = true;
        handleBack();
    }

    /**
     * When the back key is pressed, or when we are simulating the back key
     * being pressed (e.g., a back button), this code runs.
     */
    public void handleBack() {
        // clear all timers, just in case...
        Timer.instance().clear();
        // if we're looking at main menu, then exit
        if (mMode == SPLASH) {
            dispose();
            Gdx.app.exit();
        }
        // if we're looking at the chooser or help, switch to the splash
        // screen
        else if (mMode == CHOOSER || mMode == HELP || mMode == STORE) {
            doSplash();
        }
        // ok, we're looking at a game scene... switch to chooser
        else {
            doChooser(sGame.mModeStates[CHOOSER]);
        }
    }

    /**
     * This is an internal method for initializing a game. User code should
     * never call this.
     */
    @Override
    public void create() {
        // save instance
        sGame = this;

        // set current mode states
        for (int i = 0; i < 5; ++i)
            mModeStates[i] = 1;

        // for handling back presses
        Gdx.input.setCatchBackKey(true);

        // Load Resources
        mMedia = new Media();
        loadResources();

        // configure the volume
        if (Facts.getGameFact("volume", 1) == 1)
            Facts.putGameFact("volume", 1);

        // show the splash screen
        doSplash();
    }

    /**
     * This is an internal method for quitting a game. User code should never
     * call this.
     */
    @Override
    public void dispose() {
        super.dispose();

        // dispose of all fonts, textureregions, etc...
        //
        // It appears that GDX manages all textures for images and fonts, as
        // well as all sounds and music files. That
        // being the case, the only thing we need to be careful about is that we
        // get rid of any references to fonts that
        // might be hanging around
        Media.onDispose();
    }

    /**
     * This is an internal method for drawing game levels. User code should
     * never call this.
     */
    @Override
    public void render() {
        // Check for back press
        handleKeyDown();
        // Draw the current scene
        super.render();
    }

    /**
     * Register any sound or image files to be used by the game
     */
    abstract public void loadResources();

    /**
     * Set up all the global configuration options for the game
     */
    abstract public void configure();
}
