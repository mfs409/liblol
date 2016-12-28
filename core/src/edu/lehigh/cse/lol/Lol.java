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
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;
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
    ///
    /// NB: ideally, we wouldn't even bother storing it here, but we construct a Lol object much
    /// earlier than we call 'create' on it, and it's not until we call 'create' that we can use
    /// mConfig to load the images and sounds.  Hence, we're stuck having a copy here, even though
    /// we rarely use it.
    private Config mConfig;

    /**
     * StateMachine tracks the current state of the game.  The state consists of the type of level
     * being displayed (Splash, Help, Chooser, Store, or Playable Level), which level number is
     * currently active, and the actual level object.
     */
    private class StateMachine {
        /// Modes of the game, for use by the state machine.  We can be showing the main splash
        /// screen, the help screens, the level chooser, the store, or a playable level
        static final int SPLASH = 0;
        static final int HELP = 1;
        static final int CHOOSER = 2;
        static final int STORE = 3;
        static final int PLAY = 4;

        /// mMode is is for the base state machine.  It tracks the current mode of the program (from
        /// among the above choices)
        private int mMode;

        /// mModeStates provides more information about the state of the game.  mMode only lets us
        /// know what state we are in, but mModeStates lets us know which level of that mode is
        /// currently active.  Note that using an array makes it easier for us to use the back
        // button to go from a level to the chooser, or to move to the next level when we win a
        // level
        private int mModeStates[] = new int[5];

        void init() {
            // set current mode states
            for (int i = 0; i < 5; ++i)
                mModeStates[i] = 1;
        }

        /// mLevel is the Level object that is active, in accordance with the state machine.
        private Level mLevel;
    }

    /// mStateMachine is the actual state machine used by the game
    private StateMachine mStateMachine = new StateMachine();


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


    /**
     * Sets the current screen. {@link Screen#hide()} is called on any old screen, and {@link Screen#show()} is called on the new
     * screen, if any.
     *
     * @param level may be {@code null}
     */
    private void setScreen(Level level) {
        if (mStateMachine.mLevel != null) {
            mStateMachine.mLevel.mWorld.pauseMusic();
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
        mStateMachine.mLevel.mWorld.stopMusic();
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
     * lets us get the screen size correct (see the desktop project's Java file).
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
     * To properly handle gestures, we need to provide the code to run on each
     * type of gesture we care about.
     */
    class LolGestureManager extends GestureDetector.GestureAdapter {
        /**
         * When the screen is tapped, this code forwards the tap to the
         * appropriate GestureAction
         *
         * @param x      X coordinate of the tap
         * @param y      Y coordinate of the tap
         * @param count  1 for single click, 2 for double-click
         * @param button The mouse button that was pressed
         */
        @Override
        public boolean tap(float x, float y, int count, int button) {
            Level level = mStateMachine.mLevel;
            // if any pop-up scene is showing, forward the tap to the scene and
            // return true, so that the event doesn't get passed to the Scene
            if (level.mWinScene != null && level.mWinScene.mVisible) {
                level.mWinScene.onTap(x, y, level.mHud, Lol.this);
                return true;
            } else if (level.mLoseScene != null && level.mLoseScene.mVisible) {
                level.mLoseScene.onTap(x, y, level.mHud, Lol.this);
                return true;
            } else if (level.mPreScene != null && level.mPreScene.mVisible) {
                level.mPreScene.onTap(x, y, level.mHud, Lol.this);
                return true;
            } else if (level.mPauseScene != null && level.mPauseScene.mVisible) {
                level.mPauseScene.onTap(x, y, level.mHud, Lol.this);
                return true;
            }

            // check if we tapped a control
            if (level.mHud.checkTap(level.mWorld.mTouchVec, x, y, level.mWorld.mGameCam))
                return true;

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
            Level level = mStateMachine.mLevel;
            // we only fling at the whole-level layer
            level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(velocityX, velocityY, 0));
            for (GestureAction ga : level.mWorld.mGestureResponders) {
                if (ga.onFling(level.mWorld.mTouchVec))
                    return true;
            }
            return false;
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
            Level level = mStateMachine.mLevel;
            // check if we panned a control
            level.mHud.mHudCam.unproject(level.mWorld.mTouchVec.set(x, y, 0));
            for (Control c : level.mHud.mPanControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(level.mWorld.mTouchVec.x, level.mWorld.mTouchVec.y)) {
                    level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(x, y, 0));
                    c.mGestureAction.onPan(level.mWorld.mTouchVec, deltaX, deltaY);
                    return true;
                }
            }

            // did we pan the level?
            level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(x, y, 0));
            for (GestureAction ga : level.mWorld.mGestureResponders) {
                if (ga.onPan(level.mWorld.mTouchVec, deltaX, deltaY))
                    return true;
            }
            return false;

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
            Level level = mStateMachine.mLevel;
            // check if we panStopped a control
            level.mHud.mHudCam.unproject(level.mWorld.mTouchVec.set(x, y, 0));
            for (Control c : level.mHud.mPanControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(level.mWorld.mTouchVec.x, level.mWorld.mTouchVec.y)) {
                    level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(x, y, 0));
                    c.mGestureAction.onPanStop(level.mWorld.mTouchVec);
                    return true;
                }
            }

            // handle panstop on level
            level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(x, y, 0));
            for (GestureAction ga : level.mWorld.mGestureResponders)
                if (ga.onPanStop(level.mWorld.mTouchVec))
                    return true;
            return false;

        }

        /**
         * Handle zoom (i.e., pinch)
         *
         * @param initialDistance The distance between fingers when the pinch started
         * @param distance        The current distance between fingers
         */
        @Override
        public boolean zoom(float initialDistance, float distance) {
            Level level = mStateMachine.mLevel;
            for (Control c : level.mHud.mZoomControls) {
                if (c.mIsTouchable && c.mIsActive) {
                    c.mGestureAction.zoom(initialDistance, distance);
                    return true;
                }
            }
            return false;
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
            // check if we down-pressed a control
            level.mHud.mHudCam.unproject(level.mWorld.mTouchVec.set(screenX, screenY, 0));
            for (Control c : level.mHud.mToggleControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(level.mWorld.mTouchVec.x, level.mWorld.mTouchVec.y)) {
                    level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.toggle(false, level.mWorld.mTouchVec);
                    return true;
                }
            }

            // pass to pinch-zoom?
            for (Control c : level.mHud.mZoomControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(level.mWorld.mTouchVec.x, level.mWorld.mTouchVec.y)) {
                    level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.onDown(level.mWorld.mTouchVec);
                    return true;
                }
            }

            // check for actor touch, by looking at gameCam coordinates... on
            // touch, hitActor will change
            level.mWorld.mHitActor = null;
            level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(screenX, screenY, 0));
            level.mWorld.mWorld.QueryAABB(level.mWorld.mTouchCallback, level.mWorld.mTouchVec.x - 0.1f, level.mWorld.mTouchVec.y - 0.1f, level.mWorld.mTouchVec.x + 0.1f,
                    level.mWorld.mTouchVec.y + 0.1f);

            // actors don't respond to DOWN... if it's a down on a
            // actor, we are supposed to remember the most recently
            // touched actor, and that's it
            if (level.mWorld.mHitActor != null) {
                if (level.mWorld.mHitActor.mGestureResponder != null && level.mWorld.mHitActor.mGestureResponder.toggle(false, level.mWorld.mTouchVec))
                    return true;
            }

            // forward to the level's handler
            for (GestureAction ga : level.mWorld.mGestureResponders)
                if (ga.onDown(level.mWorld.mTouchVec))
                    return true;
            return false;
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
            level.mHud.mHudCam.unproject(level.mWorld.mTouchVec.set(screenX, screenY, 0));
            for (Control c : level.mHud.mToggleControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(level.mWorld.mTouchVec.x, level.mWorld.mTouchVec.y)) {
                    level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.toggle(true, level.mWorld.mTouchVec);
                    return true;
                }
            }
            level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(screenX, screenY, 0));
            if (level.mWorld.mHitActor != null) {
                if (level.mWorld.mHitActor.mGestureResponder != null && level.mWorld.mHitActor.mGestureResponder.toggle(true, level.mWorld.mTouchVec)) {
                    level.mWorld.mHitActor = null;
                    return true;
                }
            }
            return false;
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
            if (level.mWorld.mHitActor != null && level.mWorld.mHitActor.mGestureResponder != null) {
                level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(screenX, screenY, 0));
                return level.mWorld.mHitActor.mGestureResponder.onDrag(level.mWorld.mTouchVec);
            }
            for (GestureAction ga : level.mWorld.mGestureResponders)
                if (ga.onDrag(level.mWorld.mTouchVec))
                    return true;
            return false;
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
     * The debug renderer, for printing circles and boxes for each actor
     */
    private Box2DDebugRenderer mDebugRender;

    /**
     * The SpriteBatch for drawing all texture regions and fonts
     */
    private SpriteBatch mSpriteBatch;

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

        // in debug mode, any click will report the coordinates of the click...
        // this is very useful when trying to adjust screen coordinates
        if (mConfig.mShowDebugBoxes) {
            if (Gdx.input.justTouched()) {
                level.mHud.reportTouch(level.mWorld.mTouchVec, mConfig);
                level.mWorld.mGameCam.unproject(level.mWorld.mTouchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                Lol.message(mConfig, "World Coordinates", level.mWorld.mTouchVec.x + ", " + level.mWorld.mTouchVec.y);
            }
        }

        // Make sure the music is playing... Note that we start music before the
        // PreScene shows
        level.mWorld.playMusic();

        // Handle pauses due to pre, pause, or post scenes...
        //
        // Note that these handle their own screen touches...
        //
        // Note that win and lose scenes should come first.
        if (level.mWinScene != null && level.mWinScene.render(mSpriteBatch, level.mHud))
            return;
        if (level.mLoseScene != null && level.mLoseScene.render(mSpriteBatch, level.mHud))
            return;
        if (level.mPreScene != null && level.mPreScene.render(mSpriteBatch, level.mHud))
            return;
        if (level.mPauseScene != null && level.mPauseScene.render(mSpriteBatch, level.mHud))
            return;

        // handle accelerometer stuff... note that accelerometer is effectively
        // disabled during a popup... we could change that by moving this to the
        // top, but that's probably not going to produce logical behavior
        level.mWorld.handleTilt();

        // Check the countdown timers
        if (level.mScore.mLoseCountDownRemaining != -100) {
            level.mScore.mLoseCountDownRemaining -= Gdx.graphics.getDeltaTime();
            if (level.mScore.mLoseCountDownRemaining < 0) {
                if (level.mScore.mLoseCountDownText != "")
                    level.getLoseScene().setDefaultText(level.mScore.mLoseCountDownText);
                level.mScore.endLevel(false);
            }
        }
        if (level.mScore.mWinCountRemaining != -100) {
            level.mScore.mWinCountRemaining -= Gdx.graphics.getDeltaTime();
            if (level.mScore.mWinCountRemaining < 0) {
                if (level.mScore.mWinCountText != "")
                    level.getWinScene().setDefaultText(level.mScore.mWinCountText);
                level.mScore.endLevel(true);
            }
        }
        if (level.mScore.mStopWatchProgress != -100) {
            level.mScore.mStopWatchProgress += Gdx.graphics.getDeltaTime();
        }

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

        // prepare the main camera... we do it here, so that the parallax code
        // knows where to draw...
        level.mWorld.adjustCamera();
        level.mWorld.mGameCam.update();

        // check for end of game
        if (level.mScore.mEndGameEvent != null)
            level.mScore.mEndGameEvent.go();

        // The world is now static for this time step... we can display it!

        // clear the screen
        Gdx.gl.glClearColor(level.mWorld.mBackground.mColor.r, level.mWorld.mBackground.mColor.g, level.mWorld.mBackground.mColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw parallax backgrounds
        level.mWorld.mBackground.renderLayers(level.mWorld, mSpriteBatch, delta);

        // Render the actors in order from z=-2 through z=2
        mSpriteBatch.setProjectionMatrix(level.mWorld.mGameCam.combined);
        mSpriteBatch.begin();
        for (ArrayList<Renderable> a : level.mWorld.mRenderables)
            for (Renderable r : a)
                r.render(mSpriteBatch, delta);
        mSpriteBatch.end();

        // draw parallax foregrounds
        level.mWorld.mForeground.renderLayers(level.mWorld, mSpriteBatch, delta);


        // DEBUG: draw outlines of physics actors
        if (mConfig.mShowDebugBoxes)
            mDebugRender.render(level.mWorld.mWorld, level.mWorld.mGameCam.combined);

        // draw Controls
        level.mHud.render(mConfig, mSpriteBatch);
    }

    /**
     * A hack for stopping events when a pause screen is opened
     *
     * @param touchVec The location of the touch that interacted with the pause
     *                 screen.
     */
    void liftAllButtons(Vector3 touchVec) {
        mStateMachine.mLevel.mHud.liftAllButtons(touchVec);
        for (GestureAction ga : mStateMachine.mLevel.mWorld.mGestureResponders) {
            ga.onPanStop(mStateMachine.mLevel.mWorld.mTouchVec);
            ga.onUp(mStateMachine.mLevel.mWorld.mTouchVec);
        }
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