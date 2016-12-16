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
 * A Lol object is the outermost container for all of the functionality of the
 * game. It implements ApplicationListener (through Game), which provides hooks
 * for GDX to render the game, stop it, resume it, etc.
 * <p/>
 * Lol is not responsible for doing anything significant. It keeps track of
 * which screen is currently in use, and forwards (through Game) to that screen.
 * Splash screens, Choosers, Help, and playable Levels each implement Screen, so
 * that they can do the real work.
 */
public class Lol implements ApplicationListener {

    Config mConfig;

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
     * Store string/integer pairs that get reset whenever we restart the program
     */
    final TreeMap<String, Integer> mSessionFacts = new TreeMap<>();

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
        if (mMode == SPLASH) {
            dispose();
            Gdx.app.exit();
        }
        // if we're looking at the chooser or help, switch to the splash
        // screen
        else if (mMode == CHOOSER || mMode == HELP || mMode == STORE) {
            ((Level) getScreen()).doSplash();
        }
        // ok, we're looking at a game scene... switch to chooser
        else {
            ((Level) getScreen()).doChooser(mModeStates[CHOOSER]);
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
            mModeStates[i] = 1;

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
        if (mScreen != null)
            mScreen.hide();

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
        if (mScreen != null)
            mScreen.render(Gdx.graphics.getDeltaTime());
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

    protected Level mScreen;

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {
    }

    /**
     * Sets the current screen. {@link Screen#hide()} is called on any old screen, and {@link Screen#show()} is called on the new
     * screen, if any.
     *
     * @param level may be {@code null}
     */
    void setScreen(Level level) {
        if (this.mScreen != null) this.mScreen.hide();
        this.mScreen = level;
    }

    /**
     * @return the currently active {@link Screen}.
     */
    Level getScreen() {
        return mScreen;
    }
}