/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;

/**
 * The Lol object is the outermost container for all of the functionality of the game.
 * <p>
 * The Lol object implements ApplicationListener, which provides hooks for rendering the game,
 * stopping it, resuming it, and handling any Android lifecycle events.
 * <p>
 * In addition to ApplicationListener duties, the Lol object is responsible for providing an
 * abstracted interface to some of the hardware (e.g., the back button and persistent storage),
 * loading resources, and forwarding key/touch inputs to the appropriate handlers.
 */
public class Lol implements ApplicationListener {
    /// mConfig stores the configuration state of the game.
    final Config mConfig;
    /// mMedia stores all the images, sounds, and fonts for the game
    Media mMedia;

    /// The Manager object handles scores, screen management, and transitions among screens
    LolManager mManager;

    /// The SpriteBatch for drawing all texture regions and fonts
    private SpriteBatch mSpriteBatch;
    /// The debug renderer, for printing circles and boxes for each actor
    private Box2DDebugRenderer mDebugRender;

    /// This variable lets us track whether the user pressed 'back' on an android, or 'escape' on
    /// the desktop. We are using polling, so we swallow presses that aren't preceded by a release.
    /// In that manner, holding 'back' can't exit all the way out... you must press 'back'
    /// repeatedly, once for each screen to revert.
    private boolean mKeyDown;

    /**
     * Look up a fact that was stored for the current game session. If no such fact exists,
     * defaultVal will be returned.
     *
     * @param config     The game-wide configuration
     * @param factName   The name used to store the fact
     * @param defaultVal The value to return if the fact does not exist
     * @return The integer value corresponding to the last value stored
     */
    static int getGameFact(Config config, String factName, int defaultVal) {
        Preferences prefs = Gdx.app.getPreferences(config.mStorageKey);
        return prefs.getInteger(factName, defaultVal);
    }

    /**
     * Save a fact about the current game session. If the factName has already been used for this
     * game session, the new value will overwrite the old.
     *
     * @param config    The game-wide configuration
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    static void putGameFact(Config config, String factName, int factValue) {
        Preferences prefs = Gdx.app.getPreferences(config.mStorageKey);
        prefs.putInteger(factName, factValue);
        prefs.flush();
    }

    /**
     * Vibrate the phone for a fixed amount of time. Note that this only vibrates the phone if the
     * configuration says that vibration should be permitted.
     *
     * @param config The game-wide configuration
     * @param millis The amount of time to vibrate
     */
    static void vibrate(Config config, int millis) {
        if (config.mEnableVibration)
            Gdx.input.vibrate(millis);
    }

    /**
     * Instead of using Gdx.app.log directly, and potentially writing a lot of debug info in a
     * production setting, we use this to only dump to the log when debug mode is on
     *
     * @param config The game-wide configuration
     * @param tag    The message tag
     * @param text   The message text
     */
    static void message(Config config, String tag, String text) {
        if (config.mShowDebugBoxes)
            Gdx.app.log(tag, text);
    }

    /**
     * The constructor just creates a media object and calls configureGravity, so that all of our
     * globals will be set. Doing it this early lets us access the configuration from within the
     * LWJGL (Desktop) main class. That, in turn, lets us get the screen size correct (see the
     * desktop project's Java file).
     *
     * @param config The game-wide configuration
     */
    public Lol(Config config) {
        mConfig = config;
    }

    /**
     * A hack for stopping events when a pause screen is opened
     *
     * @param touchX The x coordinate of the touch that is being lifted
     * @param touchY The y coordinate of the touch that is being lifted
     */
    void liftAllButtons(float touchX, float touchY) {
        mManager.mHud.liftAllButtons(touchX, touchY);
        mManager.mWorld.liftAllButtons(touchX, touchY);
    }

    /**
     * We can call this method from the render loop to poll for back presses
     */
    private void handleKeyDown() {
        // if neither BACK nor ESCAPE is being pressed, do nothing, but recognize future presses
        if (!Gdx.input.isKeyPressed(Keys.BACK) && !Gdx.input.isKeyPressed(Keys.ESCAPE)) {
            mKeyDown = false;
            return;
        }
        // if they key is being held down, ignore it
        if (mKeyDown)
            return;
        // recognize a new back press as being a 'down' press
        mKeyDown = true;
        mManager.handleBack();
    }

    /**
     * To properly go gestures, we need to provide the code to run on each type of gesture we care
     * about.
     */
    private class LolGestureManager extends GestureDetector.GestureAdapter {
        /**
         * When the screen is tapped, this code forwards the tap to the appropriate handler
         *
         * @param x      X coordinate of the tap
         * @param y      Y coordinate of the tap
         * @param count  1 for single click, 2 for double-click
         * @param button The mouse button that was pressed
         */
        @Override
        public boolean tap(float x, float y, int count, int button) {
            // Give each pop-up scene a chance to go the tap
            if (mManager.mWinScene.onTap(x, y, Lol.this))
                return true;
            if (mManager.mLoseScene.onTap(x, y, Lol.this))
                return true;
            if (mManager.mPreScene.onTap(x, y, Lol.this))
                return true;
            if (mManager.mPauseScene.onTap(x, y, Lol.this))
                return true;
            // Let the hud go the tap
            if (mManager.mHud.handleTap(x, y, mManager.mWorld.mCamera))
                return true;
            // leave it up to the world
            return mManager.mWorld.onTap(x, y);
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
            return mManager.mWorld.handleFling(velocityX, velocityY);
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
            if (mManager.mHud.handlePan(x, y, deltaX, deltaY, mManager.mWorld.mCamera))
                return true;

            // did we pan the level?
            return mManager.mWorld.handlePan(x, y, deltaX, deltaY);
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
            return mManager.mHud.handlePanStop(x, y, mManager.mWorld.mCamera) ||
                    mManager.mWorld.handlePanStop(x, y);
        }

        /**
         * Handle zoom (i.e., pinch)
         *
         * @param initialDistance The distance between fingers when the pinch started
         * @param distance        The current distance between fingers
         */
        @Override
        public boolean zoom(float initialDistance, float distance) {
            return mManager.mHud.handleZoom(initialDistance, distance);
        }
    }

    /**
     * Gestures can't cover everything we care about (specifically 'hold this button', for which
     * long-press is not responsive enough), so we need a low-level input adapter, too.
     */
    private class LolInputManager extends InputAdapter {
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
            return mManager.mHud.handleDown(screenX, screenY, mManager.mWorld.mCamera)
                    || mManager.mWorld.handleDown(screenX, screenY);
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
            // check if we down-pressed a control
            return mManager.mHud.handleUp(screenX, screenY, mManager.mWorld.mCamera) ||
                    mManager.mWorld.handleUp(screenX, screenY);

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
            return mManager.mWorld.handleDrag(screenX, screenY);
        }
    }

    /**
     * App creation lifecycle event.
     * <p>
     * The lifecycle of LibGDX games splits app startup into two parts.  First, an
     * <code>ApplicationListener</code> is constructed.  However, it is constructed *very* early,
     * and can't even do all of the things one might expect.  For example, it doesn't have an OpenGL
     * context yet, so it can't load its assets from disk.  In the second stage, the
     * <code>create</code> method, we can finish constructing the application, knowing that it has
     * access to the full resources of the device.
     * <p>
     * NB: This is an internal method for initializing a game. User code should never call this.
     */
    @Override
    public void create() {
        // We want to intercept 'back' button presses, so that we can poll for them in
        // <code>render</code> and react accordingly
        Gdx.input.setCatchBackKey(true);

        // The config object has already been set, so we can load all assets
        mMedia = new Media(mConfig);

        // Configure the objects we need in order to render
        mDebugRender = new Box2DDebugRenderer();
        mSpriteBatch = new SpriteBatch();

        // Configure the input handlers.  We process gestures first, and if no gesture occurs, then
        // we look for a non-gesture touch event
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(new GestureDetector(new LolGestureManager()));
        mux.addProcessor(new LolInputManager());
        Gdx.input.setInputProcessor(mux);

        // configure the volume
        if (getGameFact(mConfig, "volume", 1) == 1)
            putGameFact(mConfig, "volume", 1);


        // Create the level manager, and instruct it to transition to the Splash screen
        mManager = new LolManager(mConfig, mMedia, this);
        mManager.doSplash();
    }

    /**
     * App dispose lifecycle event
     * <p>
     * When the app is disposed (on terminate or suspend to background), this lets us turn off music
     * and release any resources that won't survive.
     * <p>
     * NB: This is an internal method. User code should never call this.
     */
    @Override
    public void dispose() {
        if (mManager != null)
            mManager.mWorld.pauseMusic();

        // dispose of all fonts, TextureRegions, etc...
        //
        // It appears that GDX manages all textures for images and fonts, as well as all sounds and
        // music files. That being the case, the only thing we need to be careful about is that we
        // getLoseScene rid of any references to fonts that might be hanging around
        mMedia.onDispose();
    }

    /**
     * This code is called every 1/45th of a second to update the game state and re-draw the screen
     * <p>
     * NB: This is an internal method. User code should never call this.
     */
    @Override
    public void render() {
        // Draw the current scene
        if (mManager == null)
            return;

        float delta = Gdx.graphics.getDeltaTime();

        // Check for back press
        handleKeyDown();

        // Make sure the music is playing... Note that we start music before the PreScene shows
        mManager.mWorld.playMusic();

        // Handle pauses due to pre, pause, or post scenes.  Note that these handle their own screen
        // touches, and that win and lose scenes should come first.
        if (mManager.mWinScene.render(mSpriteBatch, delta))
            return;
        if (mManager.mLoseScene.render(mSpriteBatch, delta))
            return;
        if (mManager.mPreScene.render(mSpriteBatch, delta))
            return;
        if (mManager.mPauseScene.render(mSpriteBatch, delta))
            return;

        // in debug mode, any click will report the coordinates of the click.  this is very useful
        // when trying to adjust screen coordinates
        if (mConfig.mShowDebugBoxes) {
            if (Gdx.input.justTouched()) {
                float x = Gdx.input.getX();
                float y = Gdx.input.getY();
                mManager.mHud.reportTouch(x, y, "Hud ");
                mManager.mWorld.reportTouch(x, y, "World ");
            }
        }

        // Update the win/lose timers
        mManager.updateTimeCounts();

        // handle accelerometer stuff... note that accelerometer is effectively disabled during a
        // popup... we could change that by moving this to the top, but that's probably not going to
        // produce logical behavior
        mManager.mWorld.handleTilt();

        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it seems like we should be
        //     using /delta/ instead of 1/45f
        mManager.mWorld.mWorld.step(1 / 45f, 8, 3);

        // now handle any events that occurred on account of the world movement or screen touches
        for (LolAction pe : mManager.mWorld.mOneTimeEvents)
            pe.go();
        mManager.mWorld.mOneTimeEvents.clear();

        // handle repeat events
        for (LolAction pe : mManager.mWorld.mRepeatEvents) {
            if (pe.mIsActive)
                pe.go();
        }

        // check for end of game
        if (mManager.mEndGameEvent != null)
            mManager.mEndGameEvent.go();

        // prepare the main camera... we do it here, so that the parallax code knows where to
        // draw...
        mManager.mWorld.adjustCamera();
        mManager.mWorld.mCamera.update();

        // The world is now static for this time step... we can display it!

        // clear the screen
        Gdx.gl.glClearColor(mManager.mBackground.mColor.r, mManager.mBackground.mColor.g, mManager.mBackground.mColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // draw parallax backgrounds
        mManager.mBackground.renderLayers(mManager.mWorld.mCamera.position.x,
                mManager.mWorld.mCamera.position.y, mSpriteBatch, delta);

        // render the actors
        mManager.mWorld.render(mSpriteBatch, delta);

        // draw parallax foregrounds
        mManager.mForeground.renderLayers(mManager.mWorld.mCamera.position.x,
                mManager.mWorld.mCamera.position.y, mSpriteBatch, delta);

        // DEBUG: draw outlines of physics actors
        //
        // TODO: pass the debug renderer to the Scenes?
        if (mConfig.mShowDebugBoxes)
            mDebugRender.render(mManager.mWorld.mWorld, mManager.mWorld.mCamera.combined);

        // draw Controls
        mManager.mHud.render(mSpriteBatch, delta);
        if (mConfig.mShowDebugBoxes)
            mDebugRender.render(mManager.mHud.mWorld, mManager.mHud.mCamera.combined);
    }

    /**
     * App lifecycle Pause event.  Note that we don't have to do anything special on a pause
     *
     * NB: This is an internal method. User code should never call this.
     */
    @Override
    public void pause() {
    }

    /**
     * App lifecycle Resume event.  Note that we don't have to do anything special on a resume
     *
     * NB: This is an internal method. User code should never call this.
     */
    @Override
    public void resume() {
    }

    /**
     * App lifecycle Resize event.  Note that we don't have to do anything special on a resize
     *
     * NB: This is an internal method. User code should never call this.
     */
    @Override
    public void resize(int width, int height) {
    }
}
