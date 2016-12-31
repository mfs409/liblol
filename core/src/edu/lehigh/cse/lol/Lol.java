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
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
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
    /// mConfig stores the configuration state of the game.
    private Config mConfig;

    /// Store all the images, sounds, and fonts for the game
    private Media mMedia;

    /// mStateMachine governs which screen is showing (Chooser, etc) and how to move among them
    private StateMachine mStateMachine = new StateMachine();

    /// Store string/integer pairs that get reset whenever we restart the program
    final TreeMap<String, Integer> mSessionFacts = new TreeMap<>();

    /// The debug renderer, for printing circles and boxes for each actor
    private Box2DDebugRenderer mDebugRender;

    /// The SpriteBatch for drawing all texture regions and fonts
    private SpriteBatch mSpriteBatch;

    /// This variable lets us track whether the user pressed 'back' on an android, or 'escape' on
    // the desktop. We are using polling, so we swallow presses that aren't preceded by a release.
    // In that manner, holding 'back' can't exit all the way out... you must press 'back'
    // repeatedly, once for each screen to revert.
    private boolean mKeyDown;

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

    /**
     * The constructor just creates a media object and calls configureGravity, so that
     * all of our globals will be set. Doing it this early lets us access the
     * configuration from within the LWJGL (Desktop) main class. That, in turn,
     * lets us get the screen size correct (see the desktop project's Java file).
     */
    public Lol(Config config) {
        mConfig = config;
    }


    /**
     * A hack for stopping events when a pause screen is opened
     *
     * @param touchVec The location of the touch that interacted with the pause
     *                 screen.
     */
    void liftAllButtons(Vector3 touchVec) {
        mStateMachine.mLevel.mHud.liftAllButtons(touchVec);
        mStateMachine.mLevel.mWorld.liftAllButtons();
    }

    /**
     * If the level that follows this level has not yet been unlocked, unlock it.
     * <p>
     * NB: we only track one value for locking/unlocking, so this actually unlocks all levels up to
     * and including the level after the current level.
     */
    void unlockNext() {
        if (getGameFact(mConfig, "unlocked", 1) <= mStateMachine.mModeStates[StateMachine.PLAY])
            putGameFact(mConfig, "unlocked", mStateMachine.mModeStates[StateMachine.PLAY] + 1);
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
        mStateMachine.setScreen(l);
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
        mStateMachine.setScreen(l);
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
        mStateMachine.setScreen(l);
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
        mStateMachine.setScreen(l);
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
        mStateMachine.setScreen(l);
    }

    /**
     * Use this to quit the game
     */
    void doQuit() {
        mStateMachine.mLevel.mWorld.stopMusic();
        Gdx.app.exit();
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
     * To properly go gestures, we need to provide the code to run on each
     * type of gesture we care about.
     */
    class LolGestureManager extends GestureDetector.GestureAdapter {
        /**
         * When the screen is tapped, this code forwards the tap to the
         * appropriate handler
         *
         * @param x      X coordinate of the tap
         * @param y      Y coordinate of the tap
         * @param count  1 for single click, 2 for double-click
         * @param button The mouse button that was pressed
         */
        @Override
        public boolean tap(float x, float y, int count, int button) {
            Level level = mStateMachine.mLevel;
            // Give each pop-up scene a chance to go the tap
            if (level.mWinScene.onTap(x, y, Lol.this))
                return true;
            if (level.mLoseScene.onTap(x, y, Lol.this))
                return true;
            if (level.mPreScene.onTap(x, y, Lol.this))
                return true;
            if (level.mPauseScene.onTap(x, y, Lol.this))
                return true;
            // Let the hud go the tap
            if (level.mHud.handleTap(x, y, level.mWorld))
                return true;
            // leave it up to the world
            return level.mWorld.onTap(x, y, count, button);
        }

        /**
         * Handle fling events
         *
         * @param velocityX X velocity of the fling
         * @param velocityY Y velocity of the fling
         * @param button    The mouse button that caused the fling
         */
        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            return mStateMachine.mLevel.mWorld.handleFling(velocityX, velocityY);
        }

        /**
         * Handle pan events
         *
         * @param x      X coordinate of current touch
         * @param y      Y coordinate of current touch
         * @param deltaX change in X
         * @param deltaY change in Y
         */
        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            // check if we panned a control
            if (mStateMachine.mLevel.mHud.handlePan(x, y, deltaX, deltaY, mStateMachine.mLevel.mWorld))
                return true;

            // did we pan the level?
            return mStateMachine.mLevel.mWorld.handlePan(x, y, deltaX, deltaY);
        }

        /**
         * Handle end-of-pan event
         *
         * @param x       X coordinate of the tap
         * @param y       Y coordinate of the tap
         * @param pointer The finger that was used?
         * @param button  The mouse button that was pressed
         */
        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            // check if we panStopped a control
            if (mStateMachine.mLevel.mHud.handlePanStop(x, y, mStateMachine.mLevel.mWorld))
                return true;
            return mStateMachine.mLevel.mWorld.handlePanStop(x, y);
        }

        /**
         * Handle zoom (i.e., pinch)
         *
         * @param initialDistance The distance between fingers when the pinch started
         * @param distance        The current distance between fingers
         */
        @Override
        public boolean zoom(float initialDistance, float distance) {
            return mStateMachine.mLevel.mHud.handleZoom(initialDistance, distance);
        }
    }

    /**
     * Gestures can't cover everything we care about (specifically 'hold this
     * button' sorts of things, for which longpress is not responsive enough),
     * so we need a low-level input adapter, too.
     */
    class LolInputManager extends InputAdapter {
        /**
         * Handle when a downward touch happens
         *
         * @param screenX X coordinate of the tap
         * @param screenY Y coordinate of the tap
         * @param pointer The finger that was used?
         * @param button  The mouse button that was pressed
         */
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            Level level = mStateMachine.mLevel;
            if (level.mHud.handleDown(screenX, screenY, level.mWorld))
                return true;
            return level.mWorld.handleDown(screenX, screenY);
        }

        /**
         * Handle when a touch is released
         *
         * @param screenX X coordinate of the tap
         * @param screenY Y coordinate of the tap
         * @param pointer The finger that was used?
         * @param button  The mouse button that was pressed
         */
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            Level level = mStateMachine.mLevel;
            // check if we down-pressed a control
            if (level.mHud.handleUp(screenX, screenY, level.mWorld))
                return true;

            return level.mWorld.handleUp(screenX, screenY);
        }

        /**
         * Handle dragging
         *
         * @param screenX X coordinate of the drag
         * @param screenY Y coordinate of the drag
         * @param pointer The finger that was used
         */
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            Level level = mStateMachine.mLevel;
            return level.mWorld.handleDrag(screenX, screenY);
        }
    }

    /**
     * This is an internal method for initializing a game. User code should
     * never call this.
     */
    @Override
    public void create() {
        mStateMachine.init();

        mDebugRender = new Box2DDebugRenderer();
        mSpriteBatch = new SpriteBatch();

        // for handling back presses
        Gdx.input.setCatchBackKey(true);

        // Set up listeners for touch events. Gestures are processed before
        // non-gesture touches, and non-gesture touches are only processed when
        // a gesture is not detected.
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(new GestureDetector(new LolGestureManager()));
        mux.addProcessor(new LolInputManager());
        Gdx.input.setInputProcessor(mux);


        // Load Resources
        mMedia = new Media(mConfig);

        // configure the volume
        if (getGameFact(mConfig, "volume", 1) == 1)
            putGameFact(mConfig, "volume", 1);

        // show the splash screen
        Level l = new Level(mConfig, mMedia, this);
        l.doSplash();
    }

    /**
     * This is an internal method for quitting a game. User code should never
     * call this.
     */
    @Override
    public void dispose() {
        if (mStateMachine.mLevel != null)
            mStateMachine.mLevel.mWorld.pauseMusic();

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
     * This code is called every 1/45th of a second to update the game state and
     * re-draw the screen
     */
    @Override
    public void render() {
        // Check for back press
        handleKeyDown();

        // Draw the current scene
        if (mStateMachine.mLevel == null)
            return;

        float delta = Gdx.graphics.getDeltaTime();
        Level level = mStateMachine.mLevel;

        // Make sure the music is playing... Note that we start music before the
        // PreScene shows
        level.mWorld.playMusic();

        // in debug mode, any click will report the coordinates of the click...
        // this is very useful when trying to adjust screen coordinates
        if (mConfig.mShowDebugBoxes) {
            if (Gdx.input.justTouched()) {
                level.mHud.reportTouch(level.mWorld.mTouchVec, mConfig);
                level.mWorld.mCamera.unproject(level.mWorld.mTouchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                Lol.message(mConfig, "World Coordinates", level.mWorld.mTouchVec.x + ", " + level.mWorld.mTouchVec.y);
            }
        }

        // Handle pauses due to pre, pause, or post scenes...
        //
        // Note that these handle their own screen touches...
        //
        // Note that win and lose scenes should come first.
        if (level.mWinScene.render(mSpriteBatch, delta))
            return;
        if (level.mLoseScene.render(mSpriteBatch, delta))
            return;
        if (level.mPreScene.render(mSpriteBatch, delta))
            return;
        if (level.mPauseScene.render(mSpriteBatch, delta))
            return;

        // Let the score object know that we are rendering, so that we can handle any win/lose
        // timers
        level.mScore.onRender(level);

        // handle accelerometer stuff... note that accelerometer is effectively
        // disabled during a popup... we could change that by moving this to the
        // top, but that's probably not going to produce logical behavior
        level.mWorld.handleTilt();

        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it
        // seems like we should be using /delta/ instead of 1/45f
        level.mWorld.mWorld.step(1 / 45f, 8, 3);

        // now handle any events that occurred on account of the world movement
        // or screen touches
        for (LolAction pe : level.mWorld.mOneTimeEvents)
            pe.go();
        level.mWorld.mOneTimeEvents.clear();

        // handle repeat events
        for (LolAction pe : level.mWorld.mRepeatEvents) {
            if (pe.mIsActive)
                pe.go();
        }

        // check for end of game
        if (level.mScore.mEndGameEvent != null)
            level.mScore.mEndGameEvent.go();

        // prepare the main camera... we do it here, so that the parallax code
        // knows where to draw...
        level.mWorld.adjustCamera();
        level.mWorld.mCamera.update();

        // The world is now static for this time step... we can display it!

        // clear the screen
        Gdx.gl.glClearColor(level.mBackground.mColor.r, level.mBackground.mColor.g, level.mBackground.mColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw parallax backgrounds
        level.mBackground.renderLayers(level.mWorld, mSpriteBatch, delta);

        // render the actors
        level.mWorld.render(mSpriteBatch, delta);

        // draw parallax foregrounds
        level.mForeground.renderLayers(level.mWorld, mSpriteBatch, delta);

        // DEBUG: draw outlines of physics actors
        if (mConfig.mShowDebugBoxes)
            mDebugRender.render(level.mWorld.mWorld, level.mWorld.mCamera.combined);

        // draw Controls
        level.mHud.render(mSpriteBatch, delta);
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

    /**
     * StateMachine tracks the current state of the game.  The state consists of the type of level
     * being displayed (Splash, Help, Chooser, Store, or Playable Level), which level number is
     * currently active, and the actual level object.
     */
    static class StateMachine {
        /// Modes of the game, for use by the state machine.  We can be showing the main splash
        /// screen, the help screens, the level chooser, the store, or a playable level
        static final int SPLASH = 0;
        static final int HELP = 1;
        static final int CHOOSER = 2;
        static final int STORE = 3;
        static final int PLAY = 4;

        /// mMode is is for the base state machine.  It tracks the current mode of the program (from
        /// among the above choices)
        int mMode;

        /// mModeStates provides more information about the state of the game.  mMode only lets us
        /// know what state we are in, but mModeStates lets us know which level of that mode is
        /// currently active.  Note that using an array makes it easier for us to use the back
        // button to go from a level to the chooser, or to move to the next level when we win a
        // level
        int mModeStates[] = new int[5];

        void init() {
            // set current mode states
            for (int i = 0; i < 5; ++i)
                mModeStates[i] = 1;
        }

        /// mWorld is the Level object that is active, in accordance with the state machine.
        Level mLevel;

        /**
         * Sets the current screen. {@link Screen#hide()} is called on any old screen, and {@link Screen#show()} is called on the new
         * screen, if any.
         *
         * @param level may be {@code null}
         */
        void setScreen(Level level) {
            if (mLevel != null) {
                mLevel.mWorld.pauseMusic();
            }
            mLevel = level;
        }
    }

    /**
     * When an Actor collides with another Actor, and that collision is intended to
     * cause some custom code to run, we use this interface
     */
    static interface CollisionCallback {
        /**
         * Respond to a collision with a actor. Note that one of the collision
         * actors is not named; it should be clear from the context in which this
         * was constructed.
         *
         * @param actor   The actor involved in the collision
         * @param contact A description of the contact, in case it is useful
         */
        void go(final Actor actor, Contact contact);
    }
}
