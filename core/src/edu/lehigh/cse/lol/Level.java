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

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
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

    private void setCamera(int width, int height) {
        mCamBoundX = width;
        mCamBoundY = height;

        // warn on strange dimensions
        if (width < Lol.sGame.mWidth / Level.PIXEL_METER_RATIO)
            Util.message("Warning", "Your game width is less than 1/10 of the screen width");
        if (height < Lol.sGame.mHeight / Level.PIXEL_METER_RATIO)
            Util.message("Warning", "Your game height is less than 1/10 of the screen height");
    }

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
        setCamera(width, height);

        // set up the game camera, with 0,0 in the bottom left
        mGameCam = new OrthographicCamera(Lol.sGame.mWidth / Level.PIXEL_METER_RATIO, Lol.sGame.mHeight
                / Level.PIXEL_METER_RATIO);
        mGameCam.position.set(Lol.sGame.mWidth / Level.PIXEL_METER_RATIO / 2, Lol.sGame.mHeight
                / Level.PIXEL_METER_RATIO / 2, 0);
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
     * Create a new empty level, and configureGravity its camera
     *
     * @param width  width of the camera
     * @param height height of the camera
     */
    public static void configure(int width, int height) {
        Lol.sGame.mCurrentLevel = new Level(width, height);
        Lol.sGame.mCurrentLevel.setCamera(width, height);

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
        if (x > mCamBoundX - Lol.sGame.mWidth * mGameCam.zoom / Level.PIXEL_METER_RATIO / 2)
            x = mCamBoundX - Lol.sGame.mWidth * mGameCam.zoom / Level.PIXEL_METER_RATIO / 2;
        if (y > mCamBoundY - Lol.sGame.mHeight * mGameCam.zoom / Level.PIXEL_METER_RATIO / 2)
            y = mCamBoundY - Lol.sGame.mHeight * mGameCam.zoom / Level.PIXEL_METER_RATIO / 2;

        // if x or y is too close to 0,0, stick with minimum acceptable values
        //
        // NB: we do MAX before MIN, so that if we're zoomed out, we show extra
        // space at the top instead of the bottom
        if (x < Lol.sGame.mWidth * mGameCam.zoom / Level.PIXEL_METER_RATIO / 2)
            x = Lol.sGame.mWidth * mGameCam.zoom / Level.PIXEL_METER_RATIO / 2;
        if (y < Lol.sGame.mHeight * mGameCam.zoom / Level.PIXEL_METER_RATIO / 2)
            y = Lol.sGame.mHeight * mGameCam.zoom / Level.PIXEL_METER_RATIO / 2;

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















    /**
     * Manually increment the number of goodies of type 1 that have been
     * collected.
     */
    public static void incrementGoodiesCollected1() {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[0]++;
    }

    /**
     * Manually increment the number of goodies of type 2 that have been
     * collected.
     */
    public static void incrementGoodiesCollected2() {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[1]++;
    }

    /**
     * Manually increment the number of goodies of type 3 that have been
     * collected.
     */
    public static void incrementGoodiesCollected3() {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[2]++;
    }

    /**
     * Manually increment the number of goodies of type 4 that have been
     * collected.
     */
    public static void incrementGoodiesCollected4() {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[3]++;
    }

    /**
     * Getter for number of goodies of type 1 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected1() {
        return Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[0];
    }

    /**
     * Manually set the number of goodies of type 1 that have been collected.
     *
     * @param value The new value
     */
    public static void setGoodiesCollected1(int value) {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[0] = value;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Getter for number of goodies of type 2 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected2() {
        return Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[1];
    }

    /**
     * Manually set the number of goodies of type 2 that have been collected.
     *
     * @param value The new value
     */
    public static void setGoodiesCollected2(int value) {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[1] = value;
    }

    /**
     * Getter for number of goodies of type 3 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected3() {
        return Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[2];
    }

    /**
     * Manually set the number of goodies of type 3 that have been collected.
     *
     * @param value The new value
     */
    public static void setGoodiesCollected3(int value) {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[2] = value;
    }

    /**
     * Getter for number of goodies of type 4 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public static int getGoodiesCollected4() {
        return Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[3];
    }

    /**
     * Manually set the number of goodies of type 4 that have been collected.
     *
     * @param value The new value
     */
    public static void setGoodiesCollected4(int value) {
        Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[3] = value;
    }

    /**
     * Indicate that the level is won by defeating all the enemies. This version
     * is useful if the number of enemies isn't known, or if the goal is to
     * defeat all enemies before more are are created.
     */
    static public void setVictoryEnemyCount() {
        Lol.sGame.mCurrentLevel.mScore.mVictoryType = VictoryType.ENEMYCOUNT;
        Lol.sGame.mCurrentLevel.mScore.mVictoryEnemyCount = -1;
    }

    /**
     * Indicate that the level is won by defeating a certain number of enemies
     *
     * @param howMany The number of enemies that must be defeated to win the level
     */
    static public void setVictoryEnemyCount(int howMany) {
        Lol.sGame.mCurrentLevel.mScore.mVictoryType = VictoryType.ENEMYCOUNT;
        Lol.sGame.mCurrentLevel.mScore.mVictoryEnemyCount = howMany;
    }

    /**
     * Indicate that the level is won by collecting enough goodies
     *
     * @param v1 Number of type-1 goodies that must be collected to win the
     *           level
     * @param v2 Number of type-2 goodies that must be collected to win the
     *           level
     * @param v3 Number of type-3 goodies that must be collected to win the
     *           level
     * @param v4 Number of type-4 goodies that must be collected to win the
     *           level
     */
    static public void setVictoryGoodies(int v1, int v2, int v3, int v4) {
        Lol.sGame.mCurrentLevel.mScore.mVictoryType = VictoryType.GOODIECOUNT;
        Lol.sGame.mCurrentLevel.mScore.mVictoryGoodieCount[0] = v1;
        Lol.sGame.mCurrentLevel.mScore.mVictoryGoodieCount[1] = v2;
        Lol.sGame.mCurrentLevel.mScore.mVictoryGoodieCount[2] = v3;
        Lol.sGame.mCurrentLevel.mScore.mVictoryGoodieCount[3] = v4;
    }

    /**
     * Indicate that the level is won by having a certain number of heroes reach
     * destinations
     *
     * @param howMany Number of heroes that must reach destinations
     */
    static public void setVictoryDestination(int howMany) {
        Lol.sGame.mCurrentLevel.mScore.mVictoryType = VictoryType.DESTINATION;
        Lol.sGame.mCurrentLevel.mScore.mVictoryHeroCount = howMany;
    }

    /**
     * Change the amount of time left in a countdown timer
     *
     * @param delta The amount of time to add before the timer expires
     */
    public static void updateTimerExpiration(float delta) {
        Lol.sGame.mCurrentLevel.mScore.mCountDownRemaining += delta;
    }

    /**
     * Report the total distance the hero has traveled
     */
    public static int getDistance() {
        return Lol.sGame.mCurrentLevel.mScore.mDistance;
    }

    /**
     * Report the stopwatch value
     */
    public static int getStopwatch() {
        return (int) Lol.sGame.mCurrentLevel.mScore.mStopWatchProgress;
    }

    /**
     * Report the number of enemies that have been defeated
     */
    public static int getEnemiesDefeated() {
        return Lol.sGame.mCurrentLevel.mScore.mEnemiesDefeated;
    }

    /**
     * Force the level to end in victory
     *
     * This is useful in callbacks, where we might want to immediately end the
     * game
     */
    public static void winLevel() {
        Lol.sGame.mCurrentLevel.mScore.endLevel(true);
    }

    /**
     * Force the level to end in defeat
     *
     * This is useful in callbacks, where we might want to immediately end the
     * game
     */
    public static void loseLevel() {
        Lol.sGame.mCurrentLevel.mScore.endLevel(false);
    }



    /**
     * Score encapsulates the data used by a Level to track the player's progress.
     * There are four things tracked: the number of heroes created and destroyed,
     * the number of enemies created and destroyed, the number of heroes at
     * destinations, and the number of (each type of) goodie that has been
     * collected. Apart from storing the counts, this class provides a public
     * interface for manipulating the goodie counts, and a set of internal
     * convenience methods for updating values and checking for win/lose. It also
     * manages the mode of the level (i.e., what must be done to finish the level...
     * collecting goodies, reaching a destination, etc).
     */
     class Score {
        /**
         * This is the number of goodies that must be collected, if we're in
         * GOODIECOUNT mode
         */
        final int[] mVictoryGoodieCount = new int[4];
        /**
         * Track the number of heroes that have been created
         */
        int mHeroesCreated = 0;
        /**
         * Count of the goodies that have been collected in this level
         */
        int[] mGoodiesCollected = new int[]{0, 0, 0, 0};
        /**
         * Count the number of enemies that have been created
         */
        int mEnemiesCreated = 0;
        /**
         * Count the enemies that have been defeated
         */
        int mEnemiesDefeated = 0;
        /**
         * Track if the level has been lost (true) or the game is still being played
         * (false)
         */
        boolean mGameOver;
        /**
         * In levels that have a lose-on-timer feature, we store the timer here, so
         * that we can extend the time left to complete a game
         */
        float mCountDownRemaining;
        /**
         * This is the same as CountDownRemaining, but for levels where the hero
         * wins by lasting until time runs out.
         */
        float mWinCountRemaining;
        /**
         * This is a stopwatch, for levels where we count how long the game has been
         * running
         */
        float mStopWatchProgress;
        /**
         * This is how far the hero has traveled
         */
        int mDistance;
        /**
         * Track the number of heroes that have been removed/defeated
         */
        int mHeroesDefeated = 0;
        /**
         * Number of heroes who have arrived at any destination yet
         */
        int mDestinationArrivals = 0;
        /**
         * Describes how a level is won.
         */
        VictoryType mVictoryType = VictoryType.DESTINATION;
        /**
         * This is the number of heroes who must reach destinations, if we're in
         * DESTINATION mode
         */
        int mVictoryHeroCount;
        /**
         * This is the number of enemies that must be defeated, if we're in
         * ENEMYCOUNT mode. -1 means "all of them"
         */
        int mVictoryEnemyCount;


        /**
         * Use this to inform the level that a hero has been defeated
         *
         * @param e The enemy who defeated the hero
         */
        void defeatHero(Enemy e) {
            mHeroesDefeated++;
            if (mHeroesDefeated == mHeroesCreated) {
                // possibly change the end-of-level text
                if (!e.mOnDefeatHeroText.equals(""))
                    LoseScene.get().setDefaultText(e.mOnDefeatHeroText);
                endLevel(false);
            }
        }

        /**
         * Use this to inform the level that a goodie has been collected by a hero
         *
         * @param g The goodie that was collected
         */
        void onGoodieCollected(Goodie g) {
            // Update goodie counts
            for (int i = 0; i < 4; ++i)
                mGoodiesCollected[i] += g.mScore[i];

            // possibly win the level, but only if we win on goodie count and all
            // four counts are high enough
            if (mVictoryType != VictoryType.GOODIECOUNT)
                return;
            boolean match = true;
            for (int i = 0; i < 4; ++i)
                match &= mVictoryGoodieCount[i] <= mGoodiesCollected[i];
            if (match)
                endLevel(true);
        }

        /**
         * Use this to inform the level that a hero has reached a destination
         */
        void onDestinationArrive() {
            // check if the level is complete
            mDestinationArrivals++;
            if ((mVictoryType == VictoryType.DESTINATION) && (mDestinationArrivals >= mVictoryHeroCount))
                endLevel(true);
        }

        /**
         * Internal method for handling whenever an enemy is defeated
         */
        void onDefeatEnemy() {
            // update the count of defeated enemies
            mEnemiesDefeated++;

            // if we win by defeating enemies, see if we've defeated enough of them:
            boolean win = false;
            if (mVictoryType == VictoryType.ENEMYCOUNT) {
                // -1 means "defeat all enemies"
                if (mVictoryEnemyCount == -1)
                    win = mEnemiesDefeated == mEnemiesCreated;
                else
                    win = mEnemiesDefeated >= mVictoryEnemyCount;
            }
            if (win)
                endLevel(true);
        }

        /**
         * When a level ends, we run this code to shut it down, print a message, and
         * then let the user resume play
         *
         * @param win true if the level was won, false otherwise
         */
        void endLevel(final boolean win) {
            if (Lol.sGame.mCurrentLevel.mEndGameEvent == null)
                Lol.sGame.mCurrentLevel.mEndGameEvent = new LolAction() {
                    @Override
                    public void go() {
                        // Safeguard: only call this method once per level
                        if (mGameOver)
                            return;
                        mGameOver = true;

                        // Run the level-complete callback
                        if (win && Lol.sGame.mCurrentLevel.mWinCallback != null)
                            Lol.sGame.mCurrentLevel.mWinCallback.onEvent();
                        else if (!win && Lol.sGame.mCurrentLevel.mLoseCallback != null)
                            Lol.sGame.mCurrentLevel.mLoseCallback.onEvent();

                        // if we won, unlock the next level
                        if (win && Facts.getGameFact("unlocked", 1) <= Lol.sGame.mModeStates[Lol.PLAY])
                            Facts.putGameFact("unlocked", Lol.sGame.mModeStates[Lol.PLAY] + 1);

                        // drop everything from the hud
                        Lol.sGame.mCurrentLevel.mControls.clear();
                        Lol.sGame.mCurrentLevel.mDisplays.clear();

                        // clear any pending timers
                        Timer.instance().clear();

                        // display the PostScene, which provides a pause before we
                        // retry/start the next level
                        if (win)
                            Lol.sGame.mCurrentLevel.mWinScene.show();
                        else
                            Lol.sGame.mCurrentLevel.mLoseScene.show();
                    }
                };
        }

    }

    /**
     * These are the ways you can complete a level: you can reach the
     * destination, you can collect enough stuff, or you can reach a certain
     * number of enemies defeated Technically, there's also 'survive for x
     * seconds', but that doesn't need special support
     */
    enum VictoryType {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    }

    /**
     * Change the gravity in a running level
     * @param newXGravity The new X gravity
     * @param newYGravity The new Y gravity
     */
    public static void resetGravity(float newXGravity, float newYGravity) {
        Lol.sGame.mCurrentLevel.mWorld.setGravity(new Vector2(newXGravity, newYGravity));
    }

    /**
     * Configure physics for the current level
     *
     * @param defaultXGravity The default force moving actors to the left (negative) or
     *                        right (positive)... Usually zero
     * @param defaultYGravity The default force pushing actors down (negative) or up
     *                        (positive)... Usually zero or -10
     */
    public static void configureGravity(float defaultXGravity, float defaultYGravity) {
        // create a world with gravity
        Lol.sGame.mCurrentLevel.mWorld = new World(new Vector2(defaultXGravity, defaultYGravity), true);

        // set up the collision handlers
        Lol.sGame.mCurrentLevel.mWorld.setContactListener(new ContactListener() {
            /**
             * When two bodies start to collide, we can use this to forward to
             * our onCollide methods
             */
            @Override
            public void beginContact(final Contact contact) {
                // Get the bodies, make sure both are actors
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();
                if (!(a instanceof Actor) || !(b instanceof Actor))
                    return;

                // the order is Hero, Enemy, Goodie, Projectile, Obstacle, Destination
                //
                // Of those, Hero, Enemy, and Projectile are the only ones with
                // a non-empty onCollide
                final Actor c0;
                final Actor c1;
                if (a instanceof Hero) {
                    c0 = (Actor) a;
                    c1 = (Actor) b;
                } else if (b instanceof Hero) {
                    c0 = (Actor) b;
                    c1 = (Actor) a;
                } else if (a instanceof Enemy) {
                    c0 = (Actor) a;
                    c1 = (Actor) b;
                } else if (b instanceof Enemy) {
                    c0 = (Actor) b;
                    c1 = (Actor) a;
                } else if (a instanceof Projectile) {
                    c0 = (Actor) a;
                    c1 = (Actor) b;
                } else if (b instanceof Projectile) {
                    c0 = (Actor) b;
                    c1 = (Actor) a;
                } else {
                    return;
                }

                // Schedule an event to run as soon as the physics world
                // finishes its step.
                //
                // NB: this is called from render, while world is updating...
                // you can't modify the world or its actors until the update
                // finishes, so we have to schedule collision-based updates to
                // run after the world update.
                Lol.sGame.mCurrentLevel.mOneTimeEvents.add(new LolAction() {
                    @Override
                    public void go() {
                        c0.onCollide(c1, contact);
                    }
                });
            }

            /**
             * We ignore endcontact
             */
            @Override
            public void endContact(Contact contact) {
            }

            /**
             * Presolve is a hook for disabling certain collisions. We use it
             * for collision immunity, sticky obstacles, and one-way walls
             */
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                // get the bodies, make sure both are actors
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();
                if (!(a instanceof Actor) || !(b instanceof Actor))
                    return;
                Actor gfoA = (Actor) a;
                Actor gfoB = (Actor) b;

                // handle sticky obstacles... only do something if at least one
                // actor is a sticky actor
                if (gfoA.mIsSticky[0] || gfoA.mIsSticky[1] || gfoA.mIsSticky[2] || gfoA.mIsSticky[3]) {
                    handleSticky(gfoA, gfoB, contact);
                    return;
                } else if (gfoB.mIsSticky[0] || gfoB.mIsSticky[1] || gfoB.mIsSticky[2] || gfoB.mIsSticky[3]) {
                    handleSticky(gfoB, gfoA, contact);
                    return;
                }

                // if the actors have the same passthrough ID, and it's
                // not zero, then disable the contact
                if (gfoA.mPassThroughId != 0 && gfoA.mPassThroughId == gfoB.mPassThroughId) {
                    contact.setEnabled(false);
                    return;
                }

                // is either one-sided? If not, we're done
                Actor onesided = null;
                Actor other;
                if (gfoA.mIsOneSided > -1) {
                    onesided = gfoA;
                    other = gfoB;
                } else if (gfoB.mIsOneSided > -1) {
                    onesided = gfoB;
                    other = gfoA;
                } else {
                    return;
                }

                // if we're here, see if we should be disabling a one-sided
                // obstacle collision
                WorldManifold worldManiFold = contact.getWorldManifold();
                int numPoints = worldManiFold.getNumberOfContactPoints();
                for (int i = 0; i < numPoints; i++) {
                    Vector2 vector2 = other.mBody.getLinearVelocityFromWorldPoint(worldManiFold.getPoints()[i]);
                    // disable based on the value of isOneSided and the vector
                    // between the actors
                    if (onesided.mIsOneSided == 0 && vector2.y < 0)
                        contact.setEnabled(false);
                    else if (onesided.mIsOneSided == 2 && vector2.y > 0)
                        contact.setEnabled(false);
                    else if (onesided.mIsOneSided == 1 && vector2.x > 0)
                        contact.setEnabled(false);
                    else if (onesided.mIsOneSided == 3 && vector2.x < 0)
                        contact.setEnabled(false);
                }
            }

            /**
             * We ignore postsolve
             */
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }

    /**
     * This ratio means that every 20 pixels on the screen will correspond to a
     * meter. Note that 'pixels' are defined in terms of what a programmer's
     * configuration() says, not the actual screen size, because the
     * configuration gets scaled to screen dimensions. The default is 960x640.
     */
    static final float PIXEL_METER_RATIO = 20;

    /**
     * When a hero collides with a "sticky" obstacle, this is the code we run to
     * figure out what to do
     *
     * @param sticky  The sticky actor... it should always be an obstacle for now
     * @param other   The other actor... it should always be a hero for now
     * @param contact A description of the contact event
     */
    static void handleSticky(final Actor sticky, final Actor other, Contact contact) {
        // don't create a joint if we've already got one
        if (other.mDJoint != null)
            return;
        // don't create a joint if we're supposed to wait
        if (System.currentTimeMillis() < other.mStickyDelay)
            return;
        // handle sticky obstacles... only do something if we're hitting the
        // obstacle from the correct direction
        if ((sticky.mIsSticky[0] && other.getYPosition() >= sticky.getYPosition() + sticky.mSize.y)
                || (sticky.mIsSticky[1] && other.getXPosition() + other.mSize.x <= sticky.getXPosition())
                || (sticky.mIsSticky[3] && other.getXPosition() >= sticky.getXPosition() + sticky.mSize.x)
                || (sticky.mIsSticky[2] && other.getYPosition() + other.mSize.y <= sticky.getYPosition())) {
            // create distance and weld joints... somehow, the combination is
            // needed to get this to work. Note that this function runs during
            // the box2d step, so we need to make the joint in a callback that
            // runs later
            final Vector2 v = contact.getWorldManifold().getPoints()[0];
            Lol.sGame.mCurrentLevel.mOneTimeEvents.add(new LolAction() {
                @Override
                public void go() {
                    other.mBody.setLinearVelocity(0, 0);
                    DistanceJointDef d = new DistanceJointDef();
                    d.initialize(sticky.mBody, other.mBody, v, v);
                    d.collideConnected = true;
                    other.mDJoint = (DistanceJoint) Lol.sGame.mCurrentLevel.mWorld.createJoint(d);
                    WeldJointDef w = new WeldJointDef();
                    w.initialize(sticky.mBody, other.mBody, v);
                    w.collideConnected = true;
                    other.mWJoint = (WeldJoint) Lol.sGame.mCurrentLevel.mWorld.createJoint(w);
                }
            });
        }
    }

    /**
     * Turn on accelerometer support so that tilt can control actors in this
     * level
     *
     * @param xGravityMax Max X force that the accelerometer can produce
     * @param yGravityMax Max Y force that the accelerometer can produce
     */
    public static void enableTilt(float xGravityMax, float yGravityMax) {
        Lol.sGame.mCurrentLevel.mTilt.mGravityMax = new Vector2(xGravityMax, yGravityMax);
    }

    /**
     * Turn off accelerometer support so that tilt stops controlling actors in this
     * level
     */
    public static void disableTilt() {
        Lol.sGame.mCurrentLevel.mTilt.mGravityMax = null;
    }

    /**
     * This method lets us change the behavior of tilt, so that instead of
     * applying a force, we directly set the velocity of objects using the
     * accelerometer data.
     *
     * @param toggle This should usually be false. Setting it to true means that
     *               tilt does not cause forces upon objects, but instead the tilt
     *               of the phone directly sets velocities
     */
    public static void setTiltAsVelocity(boolean toggle) {
        Lol.sGame.mCurrentLevel.mTilt.mTiltVelocityOverride = toggle;
    }

    /**
     * Use this to make the accelerometer more or less responsive, by
     * multiplying accelerometer values by a constant.
     *
     * @param multiplier The constant that should be multiplied by the accelerometer
     *                   data. This can be a fraction, like 0.5f, to make the
     *                   accelerometer less sensitive
     */
    public static void setGravityMultiplier(float multiplier) {
        Lol.sGame.mCurrentLevel.mTilt.mMultiplier = multiplier;
    }

    /**
     * Tilt provides a mechanism for moving actors on the screen. To use tilt, you
     * must enableTilt it for a level, and also indicate that some actors move via
     * tilting. Tilt has two flavors: tilt can cause gravitational effects, where a
     * sustained tilt causes acceleration (this is the default), or it can cause
     * actors to move with a fixed velocity. Be careful when using tilt. Different
     * phones' accelerometers vary in terms of sensitivity. It is possible to set
     * multipliers and/or caps on the effect of Tilt, but these may not suffice to
     * make your game playable and enjoyable.
     */
    class Tilt {
        /**
         * List of actors that change behavior based on tilt
         */
        ArrayList<Actor> mAccelActors = new ArrayList<>();
        /**
         * Magnitude of the maximum gravity the accelerometer can create
         */
        Vector2 mGravityMax;
        /**
         * Track if we have an override for gravity to be translated into velocity
         */
        boolean mTiltVelocityOverride;
        /**
         * A multiplier to make gravity change faster or slower than the
         * accelerometer default
         */
        float mMultiplier = 1;


        /**
         * The main render loop calls this to determine what to do when there is a
         * phone tilt
         */
        void handleTilt() {
            if (mGravityMax == null)
                return;

            // these temps are for storing the accelerometer forces we measure
            float xGravity = 0;
            float yGravity = 0;

            // if we're on a phone, read from the accelerometer device, taking into
            // account the rotation of the device
            Application.ApplicationType appType = Gdx.app.getType();
            if (appType == Application.ApplicationType.Android || appType == Application.ApplicationType.iOS) {
                float rot = Gdx.input.getRotation();
                if (rot == 0) {
                    xGravity = -Gdx.input.getAccelerometerX();
                    yGravity = -Gdx.input.getAccelerometerY();
                } else if (rot == 90) {
                    xGravity = Gdx.input.getAccelerometerY();
                    yGravity = -Gdx.input.getAccelerometerX();
                } else if (rot == 180) {
                    xGravity = Gdx.input.getAccelerometerX();
                    yGravity = Gdx.input.getAccelerometerY();
                } else if (rot == 270) {
                    xGravity = -Gdx.input.getAccelerometerY();
                    yGravity = Gdx.input.getAccelerometerX();
                }
            }
            // if we're on a computer, we simulate tilt with the arrow keys
            else {
                if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT))
                    xGravity = -15f;
                else if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT))
                    xGravity = 15f;
                else if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP))
                    yGravity = 15f;
                else if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN))
                    yGravity = -15f;
            }

            // Apply the gravity multiplier
            xGravity *= mMultiplier;
            yGravity *= mMultiplier;

            // ensure x is within the -GravityMax.x : GravityMax.x range
            xGravity = (xGravity > Level.PIXEL_METER_RATIO * mGravityMax.x) ? Level.PIXEL_METER_RATIO * mGravityMax.x
                    : xGravity;
            xGravity = (xGravity < Level.PIXEL_METER_RATIO * -mGravityMax.x) ? Level.PIXEL_METER_RATIO * -mGravityMax.x
                    : xGravity;

            // ensure y is within the -GravityMax.y : GravityMax.y range
            yGravity = (yGravity > Level.PIXEL_METER_RATIO * mGravityMax.y) ? Level.PIXEL_METER_RATIO * mGravityMax.y
                    : yGravity;
            yGravity = (yGravity < Level.PIXEL_METER_RATIO * -mGravityMax.y) ? Level.PIXEL_METER_RATIO * -mGravityMax.y
                    : yGravity;

            // If we're in 'velocity' mode, apply the accelerometer reading to each
            // actor as a fixed velocity
            if (mTiltVelocityOverride) {
                // if X is clipped to zero, set each actor's Y velocity, leave X
                // unchanged
                if (mGravityMax.x == 0) {
                    for (Actor gfo : mAccelActors)
                        if (gfo.mBody.isActive())
                            gfo.updateVelocity(gfo.mBody.getLinearVelocity().x, yGravity);
                }
                // if Y is clipped to zero, set each actor's X velocity, leave Y
                // unchanged
                else if (mGravityMax.y == 0) {
                    for (Actor gfo : mAccelActors)
                        if (gfo.mBody.isActive())
                            gfo.updateVelocity(xGravity, gfo.mBody.getLinearVelocity().y);
                }
                // otherwise we set X and Y velocity
                else {
                    for (Actor gfo : mAccelActors)
                        if (gfo.mBody.isActive())
                            gfo.updateVelocity(xGravity, yGravity);
                }
            }
            // when not in velocity mode, apply the accelerometer reading to each
            // actor as a force
            else {
                for (Actor gfo : mAccelActors)
                    if (gfo.mBody.isActive())
                        gfo.mBody.applyForceToCenter(xGravity, yGravity, true);
            }
        }
    }

}
