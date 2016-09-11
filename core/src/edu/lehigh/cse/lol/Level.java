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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import java.util.ArrayList;
import java.util.TreeMap;

import edu.lehigh.cse.lol.internals.GestureAction;
import edu.lehigh.cse.lol.internals.LolAction;
import edu.lehigh.cse.lol.internals.ParallaxCamera;
import edu.lehigh.cse.lol.internals.Renderable;

/**
 * A Level is an interactive portion of the game. Levels can be infinite, or
 * they can have an end goal. Level has two components. One is the part that is
 * visible to the game designer, which involves some limited control over the
 * camera and music, and the ability to request that custom code run after a
 * fixed amount of time. These timers can also be attached to a specific enemy,
 * if desired. Internally, Level is responsible for managing a set of cameras
 * used to display everything that appears on the screen. It is also responsible
 * for keeping track of everything on the screen (Actors, Controls, and
 * Displays), so we can draw the game correctly.
 *
 * Note that everything in Lol is a level... the splash screen, the choosers,
 * the help, and the game levels themselves.
 */
public class Level extends ScreenAdapter {

    /**
     * Store string/integer pairs that get reset at the end of every level
     */
    final TreeMap<String, Integer> mLevelFacts;
    /**
     * Store Actors, so that we can get to them in callbacks
     */
    final TreeMap<String, Actor> mLevelActors;
    /**
     * All the things that can be rendered, in 5 planes. We draw them as planes
     * -2, -1, 0, 1, 2
     */
    private final ArrayList<ArrayList<Renderable>> mRenderables = new ArrayList<>(5);
    /**
     * The debug renderer, for printing circles and boxes for each actor
     */
    private final Box2DDebugRenderer mDebugRender = new Box2DDebugRenderer();
    /**
     * The SpriteBatch for drawing all texture regions and fonts
     */
    private final SpriteBatch mSpriteBatch = new SpriteBatch();
    /**
     * The debug shape renderer, for putting boxes around Controls and Displays
     */
    private final ShapeRenderer mShapeRender = new ShapeRenderer();
    /**
     * We use this to avoid garbage collection when converting screen touches to
     * camera coordinates
     */
    private final Vector3 mTouchVec = new Vector3();
    /**
     * This camera is for drawing controls that sit above the world
     */
    public OrthographicCamera mHudCam;
    /**
     * A reference to the score object, for tracking winning and losing
     */
    Score mScore = new Score();
    /**
     * A reference to the tilt object, for managing how tilts are handled
     */
    Tilt mTilt = new Tilt();
    /**
     * The physics world in which all actors interact
     */
    World mWorld;
    /**
     * The set of Parallax backgrounds
     */
    Background mBackground = new Background();
    /**
     * The set of Parallax foregrounds
     */
    Foreground mForeground = new Foreground();
    /**
     * The scene to show when the level is created (if any)
     */
    PreScene mPreScene;
    /**
     * The scene to show when the level is won
     */
    WinScene mWinScene = new WinScene();
    /**
     * The scene to show when the level is lost
     */
    LoseScene mLoseScene = new LoseScene();
    /**
     * The scene to show when the level is paused (if any)
     */
    PauseScene mPauseScene;
    /**
     * Input Controls
     */
    ArrayList<Control> mControls = new ArrayList<>();
    /**
     * Output Displays
     */
    ArrayList<Display> mDisplays = new ArrayList<>();
    /**
     * Controls that have a tap event
     */
    ArrayList<Control> mTapControls = new ArrayList<>();
    /**
     * Controls that have a pan event
     */
    ArrayList<Control> mPanControls = new ArrayList<>();
    /**
     * Controls that have a pinch zoom event
     */
    ArrayList<Control> mZoomControls = new ArrayList<>();
    /**
     * Toggle Controls
     */
    ArrayList<Control> mToggleControls = new ArrayList<>();
    /**
     * Events that get processed on the next render, then discarded
     */
    ArrayList<LolAction> mOneTimeEvents = new ArrayList<>();
    /**
     * When the level is won or lost, this is where we store the event that
     * needs to run
     */
    LolAction mEndGameEvent;
    /**
     * Events that get processed on every render
     */
    ArrayList<LolAction> mRepeatEvents = new ArrayList<>();
    /**
     * This camera is for drawing actors that exist in the physics world
     */
    OrthographicCamera mGameCam;
    /**
     * This camera is for drawing parallax backgrounds that go in front of or behind the world
     */
    ParallaxCamera mBgCam;

    /**
     * This is the Actor that the camera chases
     */
    Actor mChaseActor;

    /**
     * The maximum x value of the camera
     */
    int mCamBoundX;

    /**
     * The maximum y value of the camera
     */
    int mCamBoundY;
    /**
     * When there is a touch of an actor in the physics world, this is how we
     * find it
     */
    Actor mHitActor = null;
    /**
     * actors may need to set callbacks to run on a screen touch. If so, they
     * can use this.
     */
    ArrayList<GestureAction> mGestureResponders = new ArrayList<>();
    /**
     * In levels with a projectile pool, the pool is accessed from here
     */
    ProjectilePool mProjectilePool;
    /**
     * Code to run when a level is won
     */
    LolCallback mWinCallback;
    /**
     * Code to run when a level is lost
     */
    LolCallback mLoseCallback;
    /**
     * The music, if any
     */
    private Music mMusic;
    /**
     * Whether the music is playing or not
     */
    private boolean mMusicPlaying;
    /**
     * This callback is used to get a touched actor from the physics world
     */
    private QueryCallback mTouchCallback;

    /**
     * Construct a level. This is mostly using defaults, so the main work is in
     * camera setup
     *
     * @param width  The width of the level, in meters
     * @param height The height of the level, in meters
     */
    Level(int width, int height) {
        // clear any timers
        Timer.instance().clear();

        // Set up listeners for touch events. Gestures are processed before
        // non-gesture touches, and non-gesture touches are only processed when
        // a gesture is not detected.
        InputMultiplexer mux = new InputMultiplexer();
        mux.addProcessor(new GestureDetector(new LolGestureManager()));
        mux.addProcessor(new LolInputManager());
        Gdx.input.setInputProcessor(mux);

        // reset the per-level object store
        mLevelFacts = new TreeMap<>();
        mLevelActors = new TreeMap<>();

        // save the camera bounds
        mCamBoundX = width;
        mCamBoundY = height;

        // warn on strange dimensions
        if (width < Lol.sGame.mWidth / Physics.PIXEL_METER_RATIO)
            Util.message("Warning", "Your game width is less than 1/10 of the screen width");
        if (height < Lol.sGame.mHeight / Physics.PIXEL_METER_RATIO)
            Util.message("Warning", "Your game height is less than 1/10 of the screen height");

        // set up the game camera, with 0,0 in the bottom left
        mGameCam = new OrthographicCamera(Lol.sGame.mWidth / Physics.PIXEL_METER_RATIO, Lol.sGame.mHeight
                / Physics.PIXEL_METER_RATIO);
        mGameCam.position.set(Lol.sGame.mWidth / Physics.PIXEL_METER_RATIO / 2, Lol.sGame.mHeight
                / Physics.PIXEL_METER_RATIO / 2, 0);
        mGameCam.zoom = 1;

        // set up the heads-up display camera
        int camWidth = Lol.sGame.mWidth;
        int camHeight = Lol.sGame.mHeight;
        mHudCam = new OrthographicCamera(camWidth, camHeight);
        mHudCam.position.set(camWidth / 2, camHeight / 2, 0);

        // the background camera is like the hudcam
        mBgCam = new ParallaxCamera(camWidth, camHeight);
        mBgCam.position.set(camWidth / 2, camHeight / 2, 0);
        mBgCam.zoom = 1;

        // set up the renderables
        for (int i = 0; i < 5; ++i)
            mRenderables.add(new ArrayList<Renderable>());

        // set up the callback for finding out who in the physics world was
        // touched
        mTouchCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                // if the hit point is inside the fixture of the body we report
                // it
                if (fixture.testPoint(mTouchVec.x, mTouchVec.y)) {
                    Actor hs = (Actor) fixture.getBody().getUserData();
                    if (hs.mVisible) {
                        mHitActor = hs;
                        return false;
                    }
                }
                return true;
            }
        };
    }

    /**
     * Create a new empty level, and configure its camera
     *
     * @param width  width of the camera
     * @param height height of the camera
     */
    public static void configure(int width, int height) {
        Lol.sGame.mCurrentLevel = new Level(width, height);

        // When debug mode is on, print the frames per second. This is icky, but
        // we need the singleton to be set before we call this, so we don't
        // actually do it in the constructor...
        if (Lol.sGame.mShowDebugBoxes)
            Display.addFPS(800, 15, Lol.sGame.mDefaultFontFace, Lol.sGame.mDefaultFontRed, Lol.sGame.mDefaultFontGreen,
                    Lol.sGame.mDefaultFontBlue, 12);

    }

    /**
     * Identify the actor that the camera should try to keep on screen at all
     * times
     *
     * @param actor The actor the camera should chase
     */
    public static void setCameraChase(Actor actor) {
        Lol.sGame.mCurrentLevel.mChaseActor = actor;
    }

    /**
     * Set the background music for this level
     *
     * @param musicName Name of the Music file to play
     */
    public static void setMusic(String musicName) {
        Lol.sGame.mCurrentLevel.mMusic = Media.getMusic(musicName);
    }

    /**
     * Specify that you want some code to run after a fixed amount of time
     * passes.
     *
     * @param howLong  How long to wait before the timer code runs
     * @param callback The code to run
     */
    public static void setTimerCallback(float howLong, final LolCallback callback) {
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (!Lol.sGame.mCurrentLevel.mScore.mGameOver)
                    callback.onEvent();
            }
        }, howLong);
    }

    /**
     * Specify that you want some code to run repeatedly
     *
     * @param howLong  How long to wait before the timer code runs for the first time
     * @param interval The time between subsequent executions of the code
     * @param callback The code to run
     */
    public static void setTimerCallback(float howLong, float interval, final LolCallback callback) {
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (!Lol.sGame.mCurrentLevel.mScore.mGameOver)
                    callback.onEvent();
            }
        }, howLong, interval);
    }

    /**
     * Turn on scribble mode, so that scene touch events draw circular objects
     *
     * Note: this code should be thought of as serving to demonstrate, only. If
     * you really wanted to do anything clever with scribbling, you'd certainly
     * want to change this code.
     *
     * @param imgName          The name of the image to use for scribbling
     * @param width            Width of the individual components of the scribble
     * @param height           Height of the individual components of the scribble
     * @param interval         Time (in milliseconds) that must transpire between scribble
     *                         events... use this to avoid outrageously high rates of
     *                         scribbling
     * @param onCreateCallback A callback to run in order to modify the scribble behavior.
     *                         The obstacle that is drawn in the scribble will be the
     *                         "AttachedActor" of the callback.
     */
    public static void setScribbleMode(final String imgName, final float width,
                                       final float height, final int interval, final LolCallback onCreateCallback) {
        // we set a callback on the Level, so that any touch to the level (down,
        // drag, up) will affect our scribbling
        Lol.sGame.mCurrentLevel.mGestureResponders.add(new GestureAction() {
            /**
             * The time of the last touch event... we use this to prevent high
             * rates of scribble
             */
            long mLastTime;

            /**
             * On a down press, draw a new obstacle if enough time has
             * transpired
             *
             * @param touchLoc
             * The location of the touch
             * @param deltaX
             *            The change in X since last pan
             * @param deltaY
             *            The change in Y since last pan
             */
            @Override
            public boolean onPan(final Vector3 touchLoc, float deltaX, float deltaY) {
                // check if enough milliseconds have passed
                long now = System.currentTimeMillis();
                if (now < mLastTime + interval) {
                    return true;
                }
                mLastTime = now;

                // make a circular obstacle
                final Obstacle o = Obstacle.makeAsCircle(touchLoc.x - width / 2, touchLoc.y - height / 2, width,
                        height, imgName);
                if (onCreateCallback != null) {
                    onCreateCallback.mAttachedActor = o;
                    onCreateCallback.onEvent();
                }

                return true;
            }
        });
    }

    /**
     * Manually set the zoom level of the game
     *
     * @param zoom The amount of zoom (1 is no zoom, &gt;1 zooms out)
     */
    public static void setZoom(float zoom) {
        Lol.sGame.mCurrentLevel.mGameCam.zoom = zoom;
        Lol.sGame.mCurrentLevel.mBgCam.zoom = zoom;
    }

    /**
     * Register a callback so that custom code will run when the level is won
     *
     * @param callback The code to run
     */
    public static void setWinCallback(LolCallback callback) {
        Lol.sGame.mCurrentLevel.mWinCallback = callback;
    }

    /**
     * Register a callback so that custom code will run when the level is lost
     *
     * @param callback The code to run
     */
    public static void setLoseCallback(LolCallback callback) {
        Lol.sGame.mCurrentLevel.mLoseCallback = callback;
    }

    /*
     * SCREEN (SCREENADAPTER) OVERRIDES
     */

    /**
     * If the level has music attached to it, this starts playing it
     */
    void playMusic() {
        if (!mMusicPlaying && mMusic != null) {
            mMusicPlaying = true;
            mMusic.play();
        }
    }

    /**
     * If the level has music attached to it, this pauses it
     */
    void pauseMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.pause();
        }
    }

    /**
     * If the level has music attached to it, this stops it
     */
    void stopMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.stop();
        }
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * If the camera is supposed to follow an actor, this code will handle
     * updating the camera position
     */
    private void adjustCamera() {
        if (mChaseActor == null)
            return;
        // figure out the actor's position
        float x = mChaseActor.mBody.getWorldCenter().x + mChaseActor.mCameraOffset.x;
        float y = mChaseActor.mBody.getWorldCenter().y + mChaseActor.mCameraOffset.y;

        // if x or y is too close to MAX,MAX, stick with max acceptable values
        if (x > mCamBoundX - Lol.sGame.mWidth * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
            x = mCamBoundX - Lol.sGame.mWidth * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;
        if (y > mCamBoundY - Lol.sGame.mHeight * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
            y = mCamBoundY - Lol.sGame.mHeight * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;

        // if x or y is too close to 0,0, stick with minimum acceptable values
        //
        // NB: we do MAX before MIN, so that if we're zoomed out, we show extra
        // space at the top instead of the bottom
        if (x < Lol.sGame.mWidth * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
            x = Lol.sGame.mWidth * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;
        if (y < Lol.sGame.mHeight * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
            y = Lol.sGame.mHeight * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;

        // update the camera position
        mGameCam.position.set(x, y, 0);
    }

    /**
     * Add an actor to the level, putting it into the appropriate z plane
     *
     * @param actor  The actor to add
     * @param zIndex The z plane. valid values are -2, -1, 0, 1, and 2. 0 is the
     *               default.
     */
    void addActor(Renderable actor, int zIndex) {
        assert zIndex >= -2;
        assert zIndex <= 2;
        mRenderables.get(zIndex + 2).add(actor);
    }

    /**
     * Remove an actor from its z plane
     *
     * @param actor  The actor to remove
     * @param zIndex The z plane where it is expected to be
     */
    void removeActor(Renderable actor, int zIndex) {
        assert zIndex >= -2;
        assert zIndex <= 2;
        mRenderables.get(zIndex + 2).remove(actor);
    }

    /**
     * A hack for stopping events when a pause screen is opened
     *
     * @param touchVec The location of the touch that interacted with the pause
     *                 screen.
     */
    public void liftAllButtons(Vector3 touchVec) {
        for (Control c : mToggleControls) {
            if (c.mIsActive && c.mIsTouchable) {
                c.mGestureAction.toggle(true, touchVec);
            }
        }
        for (GestureAction ga : mGestureResponders) {
            ga.onPanStop(mTouchVec);
            ga.onUp(mTouchVec);
        }
    }

    /**
     * This code is called every 1/45th of a second to update the game state and
     * re-draw the screen
     *
     * @param delta The time since the last render
     */
    @Override
    public void render(float delta) {
        // in debug mode, any click will report the coordinates of the click...
        // this is very useful when trying to adjust screen coordinates
        if (Lol.sGame.mShowDebugBoxes) {
            if (Gdx.input.justTouched()) {
                mHudCam.unproject(mTouchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                Util.message("Screen Coordinates", mTouchVec.x + ", " + mTouchVec.y);
                mGameCam.unproject(mTouchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                Util.message("World Coordinates", mTouchVec.x + ", " + mTouchVec.y);

            }
        }

        // Make sure the music is playing... Note that we start music before the
        // PreScene shows
        playMusic();

        // Handle pauses due to pre, pause, or post scenes...
        //
        // Note that these handle their own screen touches...
        //
        // Note that win and lose scenes should come first.
        if (mWinScene != null && mWinScene.render(mSpriteBatch))
            return;
        if (mLoseScene != null && mLoseScene.render(mSpriteBatch))
            return;
        if (mPreScene != null && mPreScene.render(mSpriteBatch))
            return;
        if (mPauseScene != null && mPauseScene.render(mSpriteBatch))
            return;

        // handle accelerometer stuff... note that accelerometer is effectively
        // disabled during a popup... we could change that by moving this to the
        // top, but that's probably not going to produce logical behavior
        Lol.sGame.mCurrentLevel.mTilt.handleTilt();

        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it
        // seems like we should be using /delta/ instead of 1/45f
        mWorld.step(1 / 45f, 8, 3);

        // now handle any events that occurred on account of the world movement
        // or screen touches
        for (LolAction pe : mOneTimeEvents)
            pe.go();
        mOneTimeEvents.clear();

        // handle repeat events
        for (LolAction pe : mRepeatEvents)
            pe.go();

        // check for end of game
        if (mEndGameEvent != null)
            mEndGameEvent.go();

        // The world is now static for this time step... we can display it!

        // clear the screen
        Gdx.gl.glClearColor(mBackground.mColor.r, mBackground.mColor.g, mBackground.mColor.b, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // prepare the main camera... we do it here, so that the parallax code
        // knows where to draw...
        adjustCamera();
        mGameCam.update();

        // draw parallax backgrounds
        mBackground.renderLayers(mSpriteBatch, delta);

        // Render the actors in order from z=-2 through z=2
        mSpriteBatch.setProjectionMatrix(mGameCam.combined);
        mSpriteBatch.begin();
        for (ArrayList<Renderable> a : mRenderables)
            for (Renderable r : a)
                r.render(mSpriteBatch, delta);
        mSpriteBatch.end();

        // draw parallax foregrounds
        mForeground.renderLayers(mSpriteBatch, delta);

        
        // DEBUG: draw outlines of physics actors
        if (Lol.sGame.mShowDebugBoxes)
            mDebugRender.render(mWorld, mGameCam.combined);

        // draw Controls
        mHudCam.update();
        mSpriteBatch.setProjectionMatrix(mHudCam.combined);
        mSpriteBatch.begin();
        for (Control c : mControls)
            if (c.mIsActive)
                c.render(mSpriteBatch);
        for (Display d : mDisplays)
            d.render(mSpriteBatch);
        mSpriteBatch.end();

        // DEBUG: render Controls' outlines
        if (Lol.sGame.mShowDebugBoxes) {
            mShapeRender.setProjectionMatrix(mHudCam.combined);
            mShapeRender.begin(ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            for (Control pe : mControls)
                if (pe.mRange != null)
                    mShapeRender.rect(pe.mRange.x, pe.mRange.y, pe.mRange.width, pe.mRange.height);
            mShapeRender.end();
        }
    }

    /**
     * Whenever we hide the level, be sure to turn off the music
     */
    @Override
    public void hide() {
        pauseMusic();
    }

    /**
     * Whenever we dispose of the level, be sure to turn off the music
     */
    @Override
    public void dispose() {
        stopMusic();
    }

    /**
     * To properly handle gestures, we need to provide the code to run on each
     * type of gesture we care about.
     */
    class LolGestureManager extends GestureAdapter {
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
            // if any pop-up scene is showing, forward the tap to the scene and
            // return true, so that the event doesn't get passed to the Scene
            if (mWinScene != null && mWinScene.mVisible) {
                mWinScene.onTap(x, y);
                return true;
            } else if (mLoseScene != null && mLoseScene.mVisible) {
                mLoseScene.onTap(x, y);
                return true;
            } else if (mPreScene != null && mPreScene.mVisible) {
                mPreScene.onTap(x, y);
                return true;
            } else if (mPauseScene != null && mPauseScene.mVisible) {
                mPauseScene.onTap(x, y);
                return true;
            }

            // check if we tapped a control
            mHudCam.unproject(mTouchVec.set(x, y, 0));
            for (Control c : mTapControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(x, y, 0));
                    c.mGestureAction.onTap(mTouchVec);
                    return true;
                }
            }

            // check if we tapped an actor
            mHitActor = null;
            mGameCam.unproject(mTouchVec.set(x, y, 0));
            mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                    mTouchVec.y + 0.1f);
            if (mHitActor != null && mHitActor.onTap(mTouchVec))
                return true;

            // is this a raw screen tap?
            for (GestureAction ga : mGestureResponders)
                if (ga.onTap(mTouchVec))
                    return true;
            return false;
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
            // we only fling at the whole-level layer
            mGameCam.unproject(mTouchVec.set(velocityX, velocityY, 0));
            for (GestureAction ga : Lol.sGame.mCurrentLevel.mGestureResponders) {
                if (ga.onFling(mTouchVec))
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
            // check if we panned a control
            mHudCam.unproject(mTouchVec.set(x, y, 0));
            for (Control c : mPanControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(x, y, 0));
                    c.mGestureAction.onPan(mTouchVec, deltaX, deltaY);
                    return true;
                }
            }

            // did we pan the level?
            mGameCam.unproject(mTouchVec.set(x, y, 0));
            for (GestureAction ga : Lol.sGame.mCurrentLevel.mGestureResponders) {
                if (ga.onPan(mTouchVec, deltaX, deltaY))
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
            // check if we panStopped a control
            mHudCam.unproject(mTouchVec.set(x, y, 0));
            for (Control c : mPanControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(x, y, 0));
                    c.mGestureAction.onPanStop(mTouchVec);
                    return true;
                }
            }

            // handle panstop on level
            mGameCam.unproject(mTouchVec.set(x, y, 0));
            for (GestureAction ga : Lol.sGame.mCurrentLevel.mGestureResponders)
                if (ga.onPanStop(mTouchVec))
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
            for (Control c : mZoomControls) {
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
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            // check if we down-pressed a control
            mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
            for (Control c : mToggleControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.toggle(false, mTouchVec);
                    return true;
                }
            }

            // pass to pinch-zoom?
            for (Control c : mZoomControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.onDown(mTouchVec);
                    return true;
                }
            }

            // check for actor touch, by looking at gameCam coordinates... on
            // touch, hitActor will change
            mHitActor = null;
            mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
            mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                    mTouchVec.y + 0.1f);

            // actors don't respond to DOWN... if it's a down on a
            // actor, we are supposed to remember the most recently
            // touched actor, and that's it
            if (mHitActor != null)
                return true;

            // forward to the level's handler
            for (GestureAction ga : mGestureResponders)
                if (ga.onDown(mTouchVec))
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
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            // check if we down-pressed a control
            mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
            for (Control c : mToggleControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.toggle(true, mTouchVec);
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
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (mHitActor != null && mHitActor.mGestureResponder != null) {
                mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                return mHitActor.mGestureResponder.onDrag(mTouchVec);
            }
            for (GestureAction ga : mGestureResponders)
                if (ga.onDrag(mTouchVec))
                    return true;
            return false;
        }
    }
}
