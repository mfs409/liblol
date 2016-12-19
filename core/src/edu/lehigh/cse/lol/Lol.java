/**
 * This is free and unencumbered software released into the public domain.
 * <p/>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p/>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Timer;

import java.util.TreeMap;

/**
 * The Lol object is the outermost container for all of the functionality of the
 * game. It implements ApplicationListener, which provides hooks for rendering the
 * game, stopping it, resuming it, etc.
 * <p/>
 * Apart from ApplicationListener duties, the Lol object is responsible for providing an abstracted
 * interface to some of the hardware (e.g., the back button), loading resources, and managing a
 * state machine that monitors which type of level is currently being displayed.
 */
public class Lol implements ApplicationListener {
    // mConfig stores the configuration state of the game.
    //
    // NB: ideally, we wouldn't even bother storing it here, but we construct a Lol object much
    // earlier than we call 'create' on it, and it's not until we call 'create' that we can use
    // mConfig to load the images and sounds.  Hence, we're stuck having a copy here, even though
    // we rarely use it.
    private Config mConfig;

    /**
     * StateMachine tracks the current state of the game.  The state consists of the type of level
     * being displayed (Splash, Help, Chooser, Store, or Playable Level), which level number is
     * currently active, and the actual level object.
     */
    private class StateMachine {
        // Modes of the game, for use by the state machine.  We can be showing the main splash screen,
        // the help screens, the level chooser, the store, or a playable level
        static final int SPLASH = 0;
        static final int HELP = 1;
        static final int CHOOSER = 2;
        static final int STORE = 3;
        static final int PLAY = 4;

        // mMode is is for the base state machine.  It tracks the current mode of the program (from
        // among the above choices)
        int mMode;

        // mModeStates provides more information about the state of the game.  mMode only lets us know
        // what state we are in, but mModeStates lets us know which level of that mode is currently
        // active.  Note that using an array makes it easier for us to use the back button to go from
        // a level to the chooser, or to move to the next level when we win a level
        int mModeStates[] = new int[5];

        // mLevel is the Level object that is active, corresponding to the mMode and mModeState fields.
        // It is the third and final field that comprises the state machine
        Level mLevel;
    }

    // mStateMachine is the actual state machine used by the game
    private StateMachine mStateMachine = new StateMachine();

    /**
     * If the level that follows this level has not yet been unlocked, unlock it.
     *
     * NB: we only track one value for locking/unlocking, so this actually unlocks all levels up to
     *     and including the level after the current level.
     */
     void unlockNext() {
        if (getGameFact(mConfig, "unlocked", 1) <= mStateMachine.mModeStates[StateMachine.PLAY])
            putGameFact(mConfig, "unlocked", mStateMachine.mModeStates[StateMachine.PLAY] + 1);
    }








    /**
     * Sets the current screen. {@link Screen#hide()} is called on any old screen, and {@link Screen#show()} is called on the new
     * screen, if any.
     *
     * @param level may be {@code null}
     */
    private void setScreen(Level level) {
        if (mStateMachine.mLevel != null) {
            mStateMachine.mLevel.pauseMusic();
        }
        mStateMachine.mLevel = level;
    }



    void advanceLevel() {
        if (mStateMachine.mModeStates[StateMachine.PLAY] == mConfig.mNumLevels) {
            mStateMachine.mLevel.doChooser(1);
        } else {
            mStateMachine.mModeStates[StateMachine.PLAY]++;
            mStateMachine.mLevel.doLevel(mStateMachine.mModeStates[StateMachine.PLAY]);
        }
    }

    void repeatLevel() {
        mStateMachine.mLevel.doLevel(mStateMachine.mModeStates[StateMachine.PLAY]);
    }
    /**
     * Use this to load the splash screen
     */
     void doSplash() {
        // reset state of all screens
        for (int i = 0; i < 5; ++i)
            mStateMachine.mModeStates[i] = 1;
        mStateMachine.mMode = StateMachine.SPLASH;
        Level l = new Level(mConfig, mMedia, this);
        mConfig.mSplash.display(1, l);
        setScreen(l);
    }

    /**
     * Use this to load the level-chooser screen. Note that when the chooser is
     * disabled, we jump straight to level 1.
     *
     * @param whichChooser The chooser screen to create
     */
     void doChooser(int whichChooser) {
        // if chooser disabled, then we either called this from splash, or from
        // a game level
        if (!mConfig.mEnableChooser) {
            if (mStateMachine.mMode == StateMachine.PLAY) {
                doSplash();
            } else {
                doLevel(mStateMachine.mModeStates[StateMachine.PLAY]);
            }
            return;
        }
        // the chooser is not disabled... save the choice of level, configureGravity
        // it, and show it.
        mStateMachine.mMode = StateMachine.CHOOSER;
        mStateMachine.mModeStates[StateMachine.CHOOSER] = whichChooser;
        Level l = new Level(mConfig, mMedia, this);
        mConfig.mChooser.display(whichChooser, l);
        setScreen(l);
    }

    /**
     * Use this to load a playable level.
     *
     * @param which The index of the level to load
     */
     void doLevel(int which) {
        mStateMachine.mModeStates[StateMachine.PLAY] = which;
        mStateMachine.mMode = StateMachine.PLAY;
        Level l = new Level(mConfig, mMedia, this);
        mConfig.mLevels.display(which, l);
        setScreen(l);
    }

    /**
     * Use this to load a help level.
     *
     * @param which The index of the help level to load
     */
     void doHelp(int which) {
        mStateMachine.mModeStates[StateMachine.HELP] = which;
        mStateMachine.mMode = StateMachine.HELP;
        Level l = new Level(mConfig, mMedia, this);
        mConfig.mHelp.display(which, l);
        setScreen(l);
    }

    /**
     * Use this to load a screen of the store.
     *
     * @param which The index of the help level to load
     */
     void doStore(int which) {
        mStateMachine.mModeStates[StateMachine.STORE] = which;
        mStateMachine.mMode = StateMachine.STORE;
        Level l = new Level(mConfig, mMedia, this);
        mConfig.mStore.display(which, l);
        setScreen(l);
    }

    /**
     * Use this to quit the game
     */
     void doQuit() {
        mStateMachine.mLevel.stopMusic();
        Gdx.app.exit();
    }














    // Store string/integer pairs that get reset whenever we restart the program
    final TreeMap<String, Integer> mSessionFacts = new TreeMap<>();



    /**
     * This variable lets us track whether the user pressed 'back' on an
     * android, or 'escape' on the desktop. We are using polling, so we swallow
     * presses that aren't preceded by a release. In that manner, holding 'back'
     * can't exit all the way out... you must press 'back' repeatedly, once for
     * each screen to revert.
     */
    private boolean mKeyDown;

    /**
     * Store all the images, sounds, and fonts for the game
     */
    private Media mMedia;

    /**
     * The constructor just creates a media object and calls configureGravity, so that
     * all of our globals will be set. Doing it this early lets us access the
     * configuration from within the LWJGL (Desktop) main class. That, in turn,
     * lets us getLoseScene the screen size correct (see the desktop project's Java
     * file).
     */
    public Lol(Config config) {
        mConfig = config;
    }

    /**
     * Vibrate the phone for a fixed amount of time. Note that this only
     * vibrates the phone if the configuration says that vibration should be
     * permitted.
     *
     * @param millis The amount of time to vibrate
     */
    static void vibrate(Config config, int millis) {
        if (config.mEnableVibration)
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
    void handleBack() {
        // clear all timers, just in case...
        Timer.instance().clear();
        // if we're looking at main menu, then exit
        if (mStateMachine.mMode == StateMachine.SPLASH) {
            dispose();
            Gdx.app.exit();
        }
        // if we're looking at the chooser or help, switch to the splash
        // screen
        else if (mStateMachine.mMode == StateMachine.CHOOSER || mStateMachine.mMode == StateMachine.HELP || mStateMachine.mMode == StateMachine.STORE) {
            mStateMachine.mLevel.doSplash();
        }
        // ok, we're looking at a game scene... switch to chooser
        else {
            mStateMachine.mLevel.doChooser(mStateMachine.mModeStates[StateMachine.CHOOSER]);
        }
    }

    /**
     * This is an internal method for initializing a game. User code should
     * never call this.
     */
    @Override
    public void create() {
        // set current mode states
        for (int i = 0; i < 5; ++i)
            mStateMachine.mModeStates[i] = 1;

        // for handling back presses
        Gdx.input.setCatchBackKey(true);

        // Load Resources
        mMedia = new Media(mConfig);

        // configure the volume
        Level l = new Level(mConfig, mMedia, this);
        if (getGameFact(mConfig, "volume", 1) == 1)
            putGameFact(mConfig, "volume", 1);

        // show the splash screen
        l.doSplash();
    }

    /**
     * This is an internal method for quitting a game. User code should never
     * call this.
     */
    @Override
    public void dispose() {
        if (mStateMachine.mLevel != null)
            mStateMachine.mLevel.pauseMusic();

        // dispose of all fonts, textureregions, etc...
        //
        // It appears that GDX manages all textures for images and fonts, as
        // well as all sounds and music files. That
        // being the case, the only thing we need to be careful about is that we
        // getLoseScene rid of any references to fonts that
        // might be hanging around
        mMedia.onDispose();
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
        if (mStateMachine.mLevel != null)
            mStateMachine.mLevel.render(Gdx.graphics.getDeltaTime());
    }

    /**
     * Look up a fact that was stored for the current game session. If no such
     * fact exists, defaultVal will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The value to return if the fact does not exist
     * @return The integer value corresponding to the last value stored
     */
    static int getGameFact(Config config, String factName, int defaultVal) {
        Preferences prefs = Gdx.app.getPreferences(config.mStorageKey);
        return prefs.getInteger(factName, defaultVal);
    }

    /**
     * Save a fact about the current game session. If the factName has already
     * been used for this game session, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    static void putGameFact(Config config, String factName, int factValue) {
        Preferences prefs = Gdx.app.getPreferences(config.mStorageKey);
        prefs.putInteger(factName, factValue);
        prefs.flush();
    }

    /**
     * Instead of using Gdx.app.log directly, and potentially writing a lot of
     * debug info in a production setting, we use this to only dump to the log
     * when debug mode is on
     *
     * @param tag  The message tag
     * @param text The message text
     */
    static void message(Config config, String tag, String text) {
        if (config.mShowDebugBoxes)
            Gdx.app.log(tag, text);
    }


    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {
    }

}