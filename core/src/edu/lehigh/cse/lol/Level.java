package edu.lehigh.cse.lol;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
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

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

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
 * <p>
 * Note that everything in Lol is a level... the splash screen, the choosers,
 * the help, and the game levels themselves.
 */
public class Level {
    /// A reference to the game-wide configuration variables
    Config mConfig;

    /// A reference to the object that stores all of the sounds and images we use in the game
    Media mMedia;

    /// A reference to the game object, so we can access session facts and the state machine
    Lol mGame;

    /// A map for storing the level facts for the current level
    private final TreeMap<String, Integer> mLevelFacts;

    /// A map for storing the actors in the current level
    private final TreeMap<String, Actor> mLevelActors;

    /// Anything in the world that can be rendered, in 5 planes [-2, -1, 0, 1, 2]
    private final ArrayList<ArrayList<Renderable>> mRenderables = new ArrayList<>(5);


    /// A heads-up display, for writing Display and Control objects
    ///
    /// TODO: make private
    Hud mHud;

    /**
     * We use this to avoid garbage collection when converting screen touches to
     * camera coordinates
     */
    private final Vector3 mTouchVec = new Vector3();
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
    WinScene mWinScene;
    /**
     * The scene to show when the level is lost
     */
    LoseScene mLoseScene;
    /**
     * The scene to show when the level is paused (if any)
     */
    PauseScene mPauseScene;
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
        if (width < mConfig.mWidth / mConfig.PIXEL_METER_RATIO)
            Lol.message(mConfig, "Warning", "Your game width is less than 1/10 of the screen width");
        if (height < mConfig.mHeight / mConfig.PIXEL_METER_RATIO)
            Lol.message(mConfig, "Warning", "Your game height is less than 1/10 of the screen height");
    }

    /**
     * Construct a level. This is mostly using defaults, so the main work is in
     * camera setup
     */
    Level(Config config, Media media, Lol game) {
        mConfig = config;
        mMedia = media;
        mGame = game;

        mWinScene = new WinScene(this);
        mLoseScene = new LoseScene(this);

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
        setCamera((int) (mConfig.mWidth / mConfig.PIXEL_METER_RATIO), (int) (mConfig.mHeight / mConfig.PIXEL_METER_RATIO));

        // set up the game camera, with 0,0 in the bottom left
        mGameCam = new OrthographicCamera(mConfig.mWidth / mConfig.PIXEL_METER_RATIO, mConfig.mHeight
                / mConfig.PIXEL_METER_RATIO);
        mGameCam.position.set(mConfig.mWidth / mConfig.PIXEL_METER_RATIO / 2, mConfig.mHeight
                / mConfig.PIXEL_METER_RATIO / 2, 0);
        mGameCam.zoom = 1;

        // set up the heads-up display camera
        int camWidth = mConfig.mWidth;
        int camHeight = mConfig.mHeight;
        mHud = new Hud(camWidth, camHeight);

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
     * Configure the camera bounds for a level
     * <p>
     * TODO: set upper and lower bounds, instead of assuming a lower bound of (0, 0)
     *
     * @param width  width of the camera
     * @param height height of the camera
     */
    public void configureCamera(int width, int height) {
        setCamera(width, height);

        // TODO: we can move this once we get rid of the static sGame.mCurrentLevel field
        // When debug mode is on, print the frames per second. This is icky, but
        // we need the singleton to be set before we call this, so we don't
        // actually do it in the constructor...
        if (mConfig.mShowDebugBoxes)
            addDisplay(800, 15, mConfig.mDefaultFontFace, mConfig.mDefaultFontColor, 12, "fps: ", "", DisplayFPS);

    }

    /**
     * Identify the actor that the camera should try to keep on screen at all
     * times
     *
     * @param actor The actor the camera should chase
     */
    public void setCameraChase(Actor actor) {
        mChaseActor = actor;
    }

    /**
     * Set the background music for this level
     *
     * @param musicName Name of the Music file to play
     */
    public void setMusic(String musicName) {
        mMusic = mMedia.getMusic(musicName);
    }

    /**
     * Specify that you want some code to run after a fixed amount of time
     * passes.
     *
     * @param howLong  How long to wait before the timer code runs
     * @param callback The code to run
     */
    public void setTimerCallback(float howLong, final LolCallback callback) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (!mScore.mGameOver)
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
    public void setTimerCallback(float howLong, float interval, final LolCallback callback) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                if (!mScore.mGameOver)
                    callback.onEvent();
            }
        }, howLong, interval);
    }

    /**
     * Turn on scribble mode, so that scene touch events draw circular objects
     * <p>
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
    public void setScribbleMode(final String imgName, final float width,
                                final float height, final int interval, final LolCallback onCreateCallback) {
        // we set a callback on the Level, so that any touch to the level (down,
        // drag, up) will affect our scribbling
        mGestureResponders.add(new GestureAction() {
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
                final Obstacle o = makeObstacleAsCircle(touchLoc.x - width / 2, touchLoc.y - height / 2, width,
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
    public void setZoom(float zoom) {
        mGameCam.zoom = zoom;
        mBgCam.zoom = zoom;
    }

    /**
     * Register a callback so that custom code will run when the level is won
     *
     * @param callback The code to run
     */
    public void setWinCallback(LolCallback callback) {
        mWinCallback = callback;
    }

    /**
     * Register a callback so that custom code will run when the level is lost
     *
     * @param callback The code to run
     */
    public void setLoseCallback(LolCallback callback) {
        mLoseCallback = callback;
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
        if (x > mCamBoundX - mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
            x = mCamBoundX - mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;
        if (y > mCamBoundY - mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
            y = mCamBoundY - mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;

        // if x or y is too close to 0,0, stick with minimum acceptable values
        //
        // NB: we do MAX before MIN, so that if we're zoomed out, we show extra
        // space at the top instead of the bottom
        if (x < mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
            x = mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;
        if (y < mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
            y = mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;

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
        mHud.liftAllButtons(touchVec);
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
    public void render(float delta, Box2DDebugRenderer debugRender, SpriteBatch sb) {
        // in debug mode, any click will report the coordinates of the click...
        // this is very useful when trying to adjust screen coordinates
        if (mConfig.mShowDebugBoxes) {
            if (Gdx.input.justTouched()) {
                mHud.reportTouch(mTouchVec, mConfig);
                mGameCam.unproject(mTouchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
                Lol.message(mConfig, "World Coordinates", mTouchVec.x + ", " + mTouchVec.y);

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
        if (mWinScene != null && mWinScene.render(sb))
            return;
        if (mLoseScene != null && mLoseScene.render(sb))
            return;
        if (mPreScene != null && mPreScene.render(sb))
            return;
        if (mPauseScene != null && mPauseScene.render(sb))
            return;

        // handle accelerometer stuff... note that accelerometer is effectively
        // disabled during a popup... we could change that by moving this to the
        // top, but that's probably not going to produce logical behavior
        mTilt.handleTilt();

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
        for (LolAction pe : mRepeatEvents) {
            if (pe.mIsActive)
                pe.go();
        }

        // Check the countdown timers
        if (mScore.mLoseCountDownRemaining != -100) {
            mScore.mLoseCountDownRemaining -= Gdx.graphics.getDeltaTime();
            if (mScore.mLoseCountDownRemaining < 0) {
                if (mScore.mLoseCountDownText != "")
                    getLoseScene().setDefaultText(mScore.mLoseCountDownText);
                mScore.endLevel(false);
            }
        }
        if (mScore.mWinCountRemaining != -100) {
            mScore.mWinCountRemaining -= Gdx.graphics.getDeltaTime();
            if (mScore.mWinCountRemaining < 0) {
                if (mScore.mWinCountText != "")
                    getWinScene().setDefaultWinText(mScore.mWinCountText);
                mScore.endLevel(true);
            }
        }
        if (mScore.mStopWatchProgress != -100) {
            mScore.mStopWatchProgress += Gdx.graphics.getDeltaTime();
        }

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
        mBackground.renderLayers(this, sb, delta);

        // Render the actors in order from z=-2 through z=2
        sb.setProjectionMatrix(mGameCam.combined);
        sb.begin();
        for (ArrayList<Renderable> a : mRenderables)
            for (Renderable r : a)
                r.render(sb, delta);
        sb.end();

        // draw parallax foregrounds
        mForeground.renderLayers(this, sb, delta);


        // DEBUG: draw outlines of physics actors
        if (mConfig.mShowDebugBoxes)
            debugRender.render(mWorld, mGameCam.combined);

        // draw Controls
        mHud.render(mConfig, sb);
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
            if (mHud.checkTap(mTouchVec, x, y, mGameCam))
                return true;

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
            for (GestureAction ga : mGestureResponders) {
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
            mHud.mHudCam.unproject(mTouchVec.set(x, y, 0));
            for (Control c : mHud.mPanControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(x, y, 0));
                    c.mGestureAction.onPan(mTouchVec, deltaX, deltaY);
                    return true;
                }
            }

            // did we pan the level?
            mGameCam.unproject(mTouchVec.set(x, y, 0));
            for (GestureAction ga : mGestureResponders) {
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
            mHud.mHudCam.unproject(mTouchVec.set(x, y, 0));
            for (Control c : mHud.mPanControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(x, y, 0));
                    c.mGestureAction.onPanStop(mTouchVec);
                    return true;
                }
            }

            // handle panstop on level
            mGameCam.unproject(mTouchVec.set(x, y, 0));
            for (GestureAction ga : mGestureResponders)
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
            for (Control c : mHud.mZoomControls) {
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
            mHud.mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
            for (Control c : mHud.mToggleControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.toggle(false, mTouchVec);
                    return true;
                }
            }

            // pass to pinch-zoom?
            for (Control c : mHud.mZoomControls) {
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
            mHud.mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
            for (Control c : mHud.mToggleControls) {
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
    public void incrementGoodiesCollected1() {
        mScore.mGoodiesCollected[0]++;
    }

    /**
     * Manually increment the number of goodies of type 2 that have been
     * collected.
     */
    public void incrementGoodiesCollected2() {
        mScore.mGoodiesCollected[1]++;
    }

    /**
     * Manually increment the number of goodies of type 3 that have been
     * collected.
     */
    public void incrementGoodiesCollected3() {
        mScore.mGoodiesCollected[2]++;
    }

    /**
     * Manually increment the number of goodies of type 4 that have been
     * collected.
     */
    public void incrementGoodiesCollected4() {
        mScore.mGoodiesCollected[3]++;
    }

    /**
     * Getter for number of goodies of type 1 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public int getGoodiesCollected1() {
        return mScore.mGoodiesCollected[0];
    }

    /**
     * Manually set the number of goodies of type 1 that have been collected.
     *
     * @param value The new value
     */
    public void setGoodiesCollected1(int value) {
        mScore.mGoodiesCollected[0] = value;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Getter for number of goodies of type 2 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public int getGoodiesCollected2() {
        return mScore.mGoodiesCollected[1];
    }

    /**
     * Manually set the number of goodies of type 2 that have been collected.
     *
     * @param value The new value
     */
    public void setGoodiesCollected2(int value) {
        mScore.mGoodiesCollected[1] = value;
    }

    /**
     * Getter for number of goodies of type 3 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public int getGoodiesCollected3() {
        return mScore.mGoodiesCollected[2];
    }

    /**
     * Manually set the number of goodies of type 3 that have been collected.
     *
     * @param value The new value
     */
    public void setGoodiesCollected3(int value) {
        mScore.mGoodiesCollected[2] = value;
    }

    /**
     * Getter for number of goodies of type 4 that have been collected.
     *
     * @return The number of goodies collected.
     */
    public int getGoodiesCollected4() {
        return mScore.mGoodiesCollected[3];
    }

    /**
     * Manually set the number of goodies of type 4 that have been collected.
     *
     * @param value The new value
     */
    public void setGoodiesCollected4(int value) {
        mScore.mGoodiesCollected[3] = value;
    }

    /**
     * Indicate that the level is won by defeating all the enemies. This version
     * is useful if the number of enemies isn't known, or if the goal is to
     * defeat all enemies before more are are created.
     */
    public void setVictoryEnemyCount() {
        mScore.mVictoryType = VictoryType.ENEMYCOUNT;
        mScore.mVictoryEnemyCount = -1;
    }

    /**
     * Indicate that the level is won by defeating a certain number of enemies
     *
     * @param howMany The number of enemies that must be defeated to win the level
     */
    public void setVictoryEnemyCount(int howMany) {
        mScore.mVictoryType = VictoryType.ENEMYCOUNT;
        mScore.mVictoryEnemyCount = howMany;
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
    public void setVictoryGoodies(int v1, int v2, int v3, int v4) {
        mScore.mVictoryType = VictoryType.GOODIECOUNT;
        mScore.mVictoryGoodieCount[0] = v1;
        mScore.mVictoryGoodieCount[1] = v2;
        mScore.mVictoryGoodieCount[2] = v3;
        mScore.mVictoryGoodieCount[3] = v4;
    }

    /**
     * Indicate that the level is won by having a certain number of heroes reach
     * destinations
     *
     * @param howMany Number of heroes that must reach destinations
     */
    public void setVictoryDestination(int howMany) {
        mScore.mVictoryType = VictoryType.DESTINATION;
        mScore.mVictoryHeroCount = howMany;
    }

    /**
     * Change the amount of time left in a countdown timer
     *
     * @param delta The amount of time to add before the timer expires
     */
    public void updateTimerExpiration(float delta) {
        mScore.mLoseCountDownRemaining += delta;
    }

    /**
     * Report the total distance the hero has traveled
     */
    public int getDistance() {
        return mScore.mDistance;
    }

    /**
     * Report the stopwatch value
     */
    public int getStopwatch() {
        // Inactive stopwatch should return 0
        if (mScore.mStopWatchProgress == -100)
            return 0;
        return (int) mScore.mStopWatchProgress;
    }

    /**
     * Report the number of enemies that have been defeated
     */
    public int getEnemiesDefeated() {
        return mScore.mEnemiesDefeated;
    }

    /**
     * Force the level to end in victory
     * <p>
     * This is useful in callbacks, where we might want to immediately end the
     * game
     */
    public void winLevel() {
        mScore.endLevel(true);
    }

    /**
     * Force the level to end in defeat
     * <p>
     * This is useful in callbacks, where we might want to immediately end the
     * game
     */
    public void loseLevel() {
        mScore.endLevel(false);
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
         * <p>
         * NB: -1 indicates the timer is not active
         */
        float mLoseCountDownRemaining = -100;

        /**
         * Text to display when a Lose Countdown completes
         */
        String mLoseCountDownText = "";
        /**
         * This is the same as CountDownRemaining, but for levels where the hero
         * wins by lasting until time runs out.
         */
        float mWinCountRemaining = -100;
        /**
         * Text to ddisplay when a Win Countdown completes
         */
        String mWinCountText = "";
        /**
         * This is a stopwatch, for levels where we count how long the game has been
         * running
         */
        float mStopWatchProgress = -100;
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
                    getLoseScene().setDefaultText(e.mOnDefeatHeroText);
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
            if (mEndGameEvent == null)
                mEndGameEvent = new LolAction() {
                    @Override
                    public void go() {
                        // Safeguard: only call this method once per level
                        if (mGameOver)
                            return;
                        mGameOver = true;

                        // Run the level-complete callback
                        if (win && mWinCallback != null)
                            mWinCallback.onEvent();
                        else if (!win && mLoseCallback != null)
                            mLoseCallback.onEvent();

                        // if we won, unlock the next level
                        if (win)
                            mGame.unlockNext();

                        // drop everything from the hud
                        mHud.reset();

                        // clear any pending timers
                        Timer.instance().clear();

                        // display the PostScene, which provides a pause before we
                        // retry/start the next level
                        if (win)
                            mWinScene.show();
                        else
                            mLoseScene.show();
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
     *
     * @param newXGravity The new X gravity
     * @param newYGravity The new Y gravity
     */
    public void resetGravity(float newXGravity, float newYGravity) {
        mWorld.setGravity(new Vector2(newXGravity, newYGravity));
    }

    /**
     * Configure physics for the current level
     * <p>
     * TODO: once we remove the static Level, we can make this happen with (0,0) in the ctor
     *
     * @param defaultXGravity The default force moving actors to the left (negative) or
     *                        right (positive)... Usually zero
     * @param defaultYGravity The default force pushing actors down (negative) or up
     *                        (positive)... Usually zero or -10
     */
    public void configureGravity(float defaultXGravity, float defaultYGravity) {
        // create a world with gravity
        mWorld = new World(new Vector2(defaultXGravity, defaultYGravity), true);

        // set up the collision handlers
        mWorld.setContactListener(new ContactListener() {
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
                mOneTimeEvents.add(new LolAction() {
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
     * When a hero collides with a "sticky" obstacle, this is the code we run to
     * figure out what to do
     *
     * @param sticky  The sticky actor... it should always be an obstacle for now
     * @param other   The other actor... it should always be a hero for now
     * @param contact A description of the contact event
     */
    void handleSticky(final Actor sticky, final Actor other, Contact contact) {
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
            mOneTimeEvents.add(new LolAction() {
                @Override
                public void go() {
                    other.mBody.setLinearVelocity(0, 0);
                    DistanceJointDef d = new DistanceJointDef();
                    d.initialize(sticky.mBody, other.mBody, v, v);
                    d.collideConnected = true;
                    other.mDJoint = (DistanceJoint) mWorld.createJoint(d);
                    WeldJointDef w = new WeldJointDef();
                    w.initialize(sticky.mBody, other.mBody, v);
                    w.collideConnected = true;
                    other.mWJoint = (WeldJoint) mWorld.createJoint(w);
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
    public void enableTilt(float xGravityMax, float yGravityMax) {
        mTilt.mGravityMax = new Vector2(xGravityMax, yGravityMax);
    }

    /**
     * Turn off accelerometer support so that tilt stops controlling actors in this
     * level
     */
    public void disableTilt() {
        mTilt.mGravityMax = null;
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
    public void setTiltAsVelocity(boolean toggle) {
        mTilt.mTiltVelocityOverride = toggle;
    }

    /**
     * Use this to make the accelerometer more or less responsive, by
     * multiplying accelerometer values by a constant.
     *
     * @param multiplier The constant that should be multiplied by the accelerometer
     *                   data. This can be a fraction, like 0.5f, to make the
     *                   accelerometer less sensitive
     */
    public void setGravityMultiplier(float multiplier) {
        mTilt.mMultiplier = multiplier;
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
            xGravity = (xGravity > mConfig.PIXEL_METER_RATIO * mGravityMax.x) ? mConfig.PIXEL_METER_RATIO * mGravityMax.x
                    : xGravity;
            xGravity = (xGravity < mConfig.PIXEL_METER_RATIO * -mGravityMax.x) ? mConfig.PIXEL_METER_RATIO * -mGravityMax.x
                    : xGravity;

            // ensure y is within the -GravityMax.y : GravityMax.y range
            yGravity = (yGravity > mConfig.PIXEL_METER_RATIO * mGravityMax.y) ? mConfig.PIXEL_METER_RATIO * mGravityMax.y
                    : yGravity;
            yGravity = (yGravity < mConfig.PIXEL_METER_RATIO * -mGravityMax.y) ? mConfig.PIXEL_METER_RATIO * -mGravityMax.y
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

    /**
     * TextProducer creates text programatically... it is the foundation for displaying text that
     * changes
     */
    public interface TextProducer {
        String makeText();
    }

    /**
     * Generate text indicating the current FPS
     */
    public final TextProducer DisplayFPS = new TextProducer() {
        @Override
        public String makeText() {
            return "" + Gdx.graphics.getFramesPerSecond();
        }
    };

    /**
     * Generate text indicating the current count of Type 1 Goodies
     */
    public final TextProducer DisplayGoodies1 = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mScore.mGoodiesCollected[0];
        }
    };

    /**
     * Generate text indicating the current count of Type 2 Goodies
     */
    public final TextProducer DisplayGoodies2 = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mScore.mGoodiesCollected[1];
        }
    };

    /**
     * Generate text indicating the current count of Type 3 Goodies
     */
    public final TextProducer DisplayGoodies3 = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mScore.mGoodiesCollected[2];
        }
    };

    /**
     * Generate text indicating the current count of Type 4 Goodies
     */
    public final TextProducer DisplayGoodies4 = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mScore.mGoodiesCollected[3];
        }
    };

    /**
     * Generate text indicating the time until the level is lost
     */
    public final TextProducer DisplayLoseCountdown = new TextProducer() {
        @Override
        public String makeText() {
            return "" + (int) mScore.mLoseCountDownRemaining;
        }
    };

    /**
     * Generate text indicating the time until the level is won
     */
    public final TextProducer DisplayWinCountdown = new TextProducer() {
        @Override
        public String makeText() {
            return "" + (int) mScore.mWinCountRemaining;
        }
    };

    /**
     * Generate text indicating the number of defeated enemies
     */
    public final TextProducer DisplayEnemiesDefeated = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mScore.mEnemiesDefeated;
        }
    };

    /**
     * Generate text indicating the value of the stopwatch
     */
    public final TextProducer DisplayStopwatch = new TextProducer() {
        @Override
        public String makeText() {
            return "" + (int) mScore.mStopWatchProgress;
        }
    };

    /**
     * Generate text indicating the remaining projectiles
     */
    public final TextProducer DisplayRemainingProjectiles = new TextProducer() {
        @Override
        public String makeText() {
            return "" + mProjectilePool.mProjectilesRemaining;
        }
    };

    /**
     * Generate text indicating the strength of a hero
     *
     * @param h The hero whose strength is to be displayed
     * @return A TextProducer, which can be passed to addDisplay
     */
    public TextProducer DisplayStrength(final Hero h) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return "" + h.getStrength();
            }
        };
    }

    /**
     * Generate text indicating the value of a Level fact
     *
     * @param key The key to use to get the Level fact
     * @return A TextProducer, which can be passed to addDisplay
     */
    public TextProducer DisplayLevelFact(final String key) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return "" + getLevelFact(key, -1);
            }
        };
    }

    /**
     * Generate text indicating the value of a Session fact
     *
     * @param key The key to use to get the Session fact
     * @return A TextProducer, which can be passed to addDisplay
     */
    public TextProducer DisplaySessionFact(final String key) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return "" + getSessionFact(key, -1);
            }
        };
    }

    /**
     * Generate text indicating the value of a Game fact
     *
     * @param key The key to use to get the Game fact
     * @return A TextProducer, which can be passed to addDisplay
     */
    public TextProducer DisplayGameFact(final String key) {
        return new TextProducer() {
            @Override
            public String makeText() {
                return "" + getGameFact(key, -1);
            }
        };
    }

    /**
     * Generate text indicating the distance that an actor has travelled.
     * <p>
     * Note: This distance will also become the Distance Score for the level.
     *
     * @param actor The actor whose distance is being monitored
     * @return A TextProducer, which can be passed to addDisplay
     */
    public TextProducer DisplayDistance(final Actor actor) {
        return new TextProducer() {
            @Override
            public String makeText() {
                mScore.mDistance = (int) actor.getXPosition();
                return "" + mScore.mDistance;
            }
        };
    }

    /**
     * Place some text on the screen.  The text will be generated by tp, which is called on every screen render
     *
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param fontName The name of the font to use
     * @param size     The font size
     * @param tp       The TextProducer
     * @return The display, so that it can be controlled further if needed
     */
    public Display addDisplay(final int x, final int y, String fontName, final String fontColor, int size, final String prefix, final String suffix, final TextProducer tp) {
        Display d = new Display(this, fontColor, fontName, size) {
            @Override
            void render(SpriteBatch sb) {
                mFont.setColor(mColor);
                String txt = prefix + tp.makeText() + suffix;
                drawTextTransposed(x, y, txt, mFont, sb);
            }
        };
        mHud.mDisplays.add(d);
        return d;
    }

    /**
     * Indicate that the level will end in defeat if it is not completed in a given amount of time.
     *
     * @param timeout The amount of time until the level will end in defeat
     * @param text    The text to display when the level ends in defeat
     *                TODO: make second parameter a callback?
     */
    public void setLoseCountdown(float timeout, String text) {
        // Once the Lose CountDown is not -100, it will start counting down
        this.mScore.mLoseCountDownRemaining = timeout;
        this.mScore.mLoseCountDownText = text;
    }

    /**
     * Indicate that the level will end in victory if the hero survives for a given amount of time
     *
     * @param timeout The amount of time until the level will end in victory
     * @param text    The text to display when the level ends in victory
     */
    public void setWinCountdown(float timeout, String text) {
        // Once the Win CountDown is not -100, it will start counting down
        this.mScore.mWinCountRemaining = timeout;
        this.mScore.mWinCountText = text;
    }

    /**
     * Set the current value of the stopwatch.  Use -100 to disable the stopwatch, otherwise it will start counting immediately.
     * @param newVal
     */
    public void setStopwatch(float newVal) {
        this.mScore.mStopWatchProgress = newVal;
    }

    /**
     * Add a button that pauses the game (via a single tap) by causing a
     * PauseScene to be displayed. Note that you must configureGravity a PauseScene, or
     * pressing this button will cause your game to crash.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     */
    public Control addTapControl(int x, int y, int width, int height, String imgName, final TouchEventHandler action) {
        Control c = new Control(this, imgName, x, y, width, height);
        // TODO: can we refactor GestureAction now that we use TouchEventHandlers?
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean onTap(Vector3 vv) {
                action.go(vv);
                return true;
            }
        };
        action.mAttachedControl = c;
        mHud.mControls.add(c);
        mHud.mTapControls.add(c);
        return c;
    }

    /**
     * An action to pause the game.  This action can be used as the action taken on a Control tap.
     */
    public TouchEventHandler PauseAction = new TouchEventHandler() {
        @Override
        public void go(Vector3 touchLocation) {
            getPauseScene().show();
        }
    };

    /**
     * Create an action that makes a hero jump.  This action can be used as the action taken on a Control tap.
     *
     * @param hero The hero who we want to jump
     * @return The action object
     */
    public TouchEventHandler JumpAction(final Hero hero) {
        return new TouchEventHandler() {
            @Override
            public void go(Vector3 touchLocation) {
                hero.jump();
            }
        };
    }

    /**
     * Create an action that makes a hero throw a projectile
     *
     * @param hero      The hero who should throw the projectile
     * @param offsetX   specifies the x distance between the bottom left of the
     *                  projectile and the bottom left of the hero throwing the
     *                  projectile
     * @param offsetY   specifies the y distance between the bottom left of the
     *                  projectile and the bottom left of the hero throwing the
     *                  projectile
     * @param velocityX The X velocity of the projectile when it is thrown
     * @param velocityY The Y velocity of the projectile when it is thrown
     */
    public TouchEventHandler ThrowFixedAction(final Hero hero, final float offsetX, final float offsetY, final float velocityX, final float velocityY) {
        return new TouchEventHandler() {
            public void go(Vector3 touchLocation) {
                mProjectilePool.throwFixed(hero, offsetX, offsetY, velocityX, velocityY);
            }
        };
    }

    /**
     * Create an action that makes a hero throw a projectile in a direction that relates to how the screen was touched
     *
     * @param hero    The hero who should throw the projectile
     * @param offsetX specifies the x distance between the bottom left of the
     *                projectile and the bottom left of the hero throwing the
     *                projectile
     * @param offsetY specifies the y distance between the bottom left of the
     *                projectile and the bottom left of the hero throwing the
     *                projectile
     */
    public TouchEventHandler ThrowDirectionalAction(final Hero hero, final float offsetX, final float offsetY) {
        return new TouchEventHandler() {
            public void go(Vector3 touchLocation) {
                mProjectilePool.throwAt(hero.mBody.getPosition().x, hero.mBody.getPosition().y,
                        touchLocation.x, touchLocation.y, hero, offsetX, offsetY);
            }
        };
    }

    /**
     * Create an action that makes the screen zoom out
     *
     * @param maxZoom The maximum zoom factor to allow
     */
    public TouchEventHandler ZoomOutAction(final float maxZoom) {
        return new TouchEventHandler() {
            public void go(Vector3 eventPosition) {
                float curzoom = mGameCam.zoom;
                if (curzoom < maxZoom) {
                    mGameCam.zoom *= 2;
                    mBgCam.zoom *= 2;
                }
            }
        };
    }

    /**
     * Create an action that makes the screen zoom in
     *
     * @param minZoom The minimum zoom factor to allow
     */
    public TouchEventHandler ZoomInAction(final float minZoom) {
        return new TouchEventHandler() {
            public void go(Vector3 eventPosition) {
                float curzoom = mGameCam.zoom;
                if (curzoom > minZoom) {
                    mGameCam.zoom /= 2;
                    mBgCam.zoom /= 2;
                }
            }
        };
    }

    /**
     * Add a button that has one behavior while it is being pressed, and another when it is released
     *
     * @param x               The X coordinate of the bottom left corner
     * @param y               The Y coordinate of the bottom left corner
     * @param width           The width of the image
     * @param height          The height of the image
     * @param imgName         The name of the image to display.  Use "" for an invisible button
     * @param whileDownAction The action to execute, repeatedly, whenever the button is pressed
     * @param onUpAction      The action to execute once any time the button is released
     * @return The control, so we can do more with it as needed.
     */
    public Control addToggleButton(int x, int y, int width, int height, String imgName, final LolAction whileDownAction, final LolAction onUpAction) {
        // make the control
        final Control c = new Control(this, imgName, x, y, width, height);
        // initially the down action is not active
        whileDownAction.mIsActive = false;
        // set up the toggle behavior
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                if (isUp) {
                    whileDownAction.mIsActive = false;
                    onUpAction.go();
                } else {
                    whileDownAction.mIsActive = true;
                }
                return true;
            }
        };
        // Put the control and events in the appropriate lists
        mHud.mControls.add(c);
        mHud.mToggleControls.add(c);
        mRepeatEvents.add(whileDownAction);
        return c;
    }

    /**
     * Create an action for moving an actor in the X direction.  This action can be used by a Control.
     *
     * @param actor The actor to move
     * @param xRate The rate at which the actor should move in the X direction (negative values are allowed)
     * @return The action
     */
    public LolAction makeXMotionAction(final Actor actor, final float xRate) {
        return new LolAction() {
            @Override
            void go() {
                Vector2 v = actor.mBody.getLinearVelocity();
                v.x = xRate;
                actor.updateVelocity(v.x, v.y);
            }
        };
    }

    /**
     * Create an action for moving an actor in the Y direction.  This action can be used by a Control.
     *
     * @param actor The actor to move
     * @param yRate The rate at which the actor should move in the Y direction (negative values are allowed)
     * @return The action
     */
    public LolAction makeYMotionAction(final Actor actor, final float yRate) {
        return new LolAction() {
            @Override
            void go() {
                Vector2 v = actor.mBody.getLinearVelocity();
                v.y = yRate;
                actor.updateVelocity(v.x, v.y);
            }
        };
    }

    /**
     * Create an action for moving an actor in the X and Y directions.  This action can be used by a Control.
     *
     * @param actor The actor to move
     * @param xRate The rate at which the actor should move in the X direction (negative values are allowed)
     * @param yRate The rate at which the actor should move in the Y direction (negative values are allowed)
     * @return The action
     */
    public LolAction makeXYMotionAction(final Actor actor, final float xRate, final float yRate) {
        return new LolAction() {
            @Override
            void go() {
                actor.updateVelocity(xRate, yRate);
            }
        };
    }

    /**
     * Add a button that moves an actor downward while the button is being held
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rate    Rate at which the actor moves
     * @param actor   The actor to move downward
     */
    public Control addDownButton(int x, int y, int width, int height, String imgName, float rate, Actor actor) {
        return addToggleButton(x, y, width, height, imgName, makeYMotionAction(actor, -rate), makeYMotionAction(actor, 0));
    }

    /**
     * Add a button that moves an actor upward while the button is being held
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rate    Rate at which the actor moves
     * @param actor   The actor to move upward
     */
    public Control addUpButton(int x, int y, int width, int height, String imgName, float rate, Actor actor) {
        return addToggleButton(x, y, width, height, imgName, makeYMotionAction(actor, rate), makeYMotionAction(actor, 0));
    }

    /**
     * Add a button that moves the given actor at one speed when it is
     * depressed, and at another otherwise
     *
     * @param x         The X coordinate of the bottom left corner (in pixels)
     * @param y         The Y coordinate of the bottom left corner (in pixels)
     * @param width     The width of the image
     * @param height    The height of the image
     * @param imgName   The name of the image to display. Use "" for an invisible
     *                  button
     * @param rateDownX Rate (X) at which the actor moves when the button is pressed
     * @param rateDownY Rate (Y) at which the actor moves when the button is pressed
     * @param rateUpX   Rate (X) at which the actor moves when the button is not
     *                  pressed
     * @param rateUpY   Rate (Y) at which the actor moves when the button is not
     *                  pressed
     * @param actor     The actor that the button controls
     */
    public Control addTurboButton(int x, int y, int width, int height, String imgName, final int rateDownX,
                                  final int rateDownY, final int rateUpX, final int rateUpY, final Actor actor) {
        return addToggleButton(x, y, width, height, imgName, makeXYMotionAction(actor, rateDownX, rateDownY), makeXYMotionAction(actor, rateUpX, rateUpY));
    }

    /**
     * Add a button that moves the given actor at one speed, but doesn't
     * immediately stop the actor when the button is released
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rateX   Rate (X) at which the actor moves when the button is pressed
     * @param rateY   Rate (Y) at which the actor moves when the button is pressed
     * @param actor   The actor that the button controls
     */
    public Control addDampenedMotionButton(int x, int y, int width, int height, String imgName,
                                           final float rateX, final float rateY, final float dampening, final Actor actor) {
        final Control c = new Control(this, imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 vv) {
                mHolding = !isUp;
                return true;
            }
        };
        mHud.mControls.add(c);
        mHud.mToggleControls.add(c);
        mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    Vector2 v = actor.mBody.getLinearVelocity();
                    v.x = rateX;
                    v.y = rateY;
                    actor.mBody.setLinearDamping(0);
                    actor.updateVelocity(v.x, v.y);
                } else {
                    actor.mBody.setLinearDamping(dampening);
                }
            }
        });
        return c;
    }

    /**
     * Add a button that puts a hero into crawl mode when depressed, and regular
     * mode when released
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param h       The hero to control
     */
    public Control addCrawlButton(int x, int y, int width, int height, String imgName, final Hero h) {
        Control c = new Control(this, imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean upPress, Vector3 touchVec) {
                if (upPress)
                    h.crawlOff();
                else
                    h.crawlOn();
                return true;
            }
        };
        mHud.mControls.add(c);
        mHud.mToggleControls.add(c);
        return c;
    }

    /**
     * Add a button that rotates the hero
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param rate    Amount of rotation to apply to the hero on each press
     */
    public Control addRotateButton(int x, int y, int width, int height, String imgName, final float rate,
                                   final Hero h) {
        final Control c = new Control(this, imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                mHolding = !isUp;
                return true;
            }
        };
        mHud.mControls.add(c);
        mHud.mToggleControls.add(c);
        mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (c.mGestureAction.mHolding)
                    h.increaseRotation(rate);
            }
        });
        return c;
    }

    /**
     * Add a button to make the hero throw a projectile
     *
     * @param x          The X coordinate of the bottom left corner (in pixels)
     * @param y          The Y coordinate of the bottom left corner (in pixels)
     * @param width      The width of the image
     * @param height     The height of the image
     * @param imgName    The name of the image to display. Use "" for an invisible
     *                   button
     * @param h          The hero who should throw the projectile
     * @param milliDelay A delay between throws, so that holding doesn't lead to too
     *                   many throws at once
     * @param offsetX    specifies the x distance between the bottom left of the
     *                   projectile and the bottom left of the hero throwing the
     *                   projectile
     * @param offsetY    specifies the y distance between the bottom left of the
     *                   projectile and the bottom left of the hero throwing the
     *                   projectile
     * @param velocityX  The X velocity of the projectile when it is thrown
     * @param velocityY  The Y velocity of the projectile when it is thrown
     */
    public Control addThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
                                  final int milliDelay, final float offsetX, final float offsetY, final float velocityX, final float velocityY) {
        final Control c = new Control(this, imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                mHolding = !isUp;
                return true;
            }
        };
        mHud.mControls.add(c);
        mHud.mToggleControls.add(c);
        mRepeatEvents.add(new LolAction() {
            long mLastThrow;

            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    long now = System.currentTimeMillis();
                    if (mLastThrow + milliDelay < now) {
                        mLastThrow = now;
                        mProjectilePool.throwFixed(h, offsetX, offsetY, velocityX, velocityY);
                    }
                }
            }
        });
        return c;
    }


    /**
     * The default behavior for throwing is to throw in a straight line. If we
     * instead desire that the projectiles have some sort of aiming to them, we
     * need to use this method, which throws toward where the screen was pressed
     * <p>
     * Note: you probably want to use an invisible button that covers the
     * screen...
     *
     * @param x          The X coordinate of the bottom left corner (in pixels)
     * @param y          The Y coordinate of the bottom left corner (in pixels)
     * @param width      The width of the image
     * @param height     The height of the image
     * @param imgName    The name of the image to display. Use "" for an invisible
     *                   button
     * @param h          The hero who should throw the projectile
     * @param milliDelay A delay between throws, so that holding doesn't lead to too
     *                   many throws at once
     * @param offsetX    specifies the x distance between the bottom left of the
     *                   projectile and the bottom left of the hero throwing the
     *                   projectile
     * @param offsetY    specifies the y distance between the bottom left of the
     *                   projectile and the bottom left of the hero throwing the
     *                   projectile
     */
    public Control addDirectionalThrowButton(int x, int y, int width, int height, String imgName, final Hero h,
                                             final long milliDelay, final float offsetX, final float offsetY) {
        final Control c = new Control(this, imgName, x, y, width, height);
        final Vector3 v = new Vector3();
        c.mGestureAction = new GestureAction() {
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                if (isUp) {
                    mHolding = false;
                } else {
                    mHolding = true;
                    v.x = touchVec.x;
                    v.y = touchVec.y;
                    v.z = touchVec.z;
                }
                return true;
            }

            @Override
            public boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
                v.x = touchVec.x;
                v.y = touchVec.y;
                v.z = touchVec.z;
                return true;
            }
        };
        mHud.mControls.add(c);
        // on toggle, we start or stop throwing; on pan, we change throw
        // direction
        mHud.mToggleControls.add(c);
        mHud.mPanControls.add(c);
        mRepeatEvents.add(new LolAction() {
            long mLastThrow;

            @Override
            public void go() {
                if (c.mGestureAction.mHolding) {
                    long now = System.currentTimeMillis();
                    if (mLastThrow + milliDelay < now) {
                        mLastThrow = now;
                        mProjectilePool.throwAt(h.mBody.getPosition().x,
                                h.mBody.getPosition().y, v.x, v.y, h, offsetX, offsetY);
                    }
                }
            }
        });
        return c;
    }


    /**
     * Allow panning to view more of the screen than is currently visible
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     */
    public Control addPanControl(int x, int y, int width, int height, String imgName) {
        Control c = new Control(this, imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            /**
             * Use this to restore the chase actor when the Pan stops
             */
            Actor oldChaseactor;

            /**
             * Handle a pan stop event by restoring the chase actor, if there
             * was one
             *
             * @param touchVec
             *            The x/y/z coordinates of the touch
             */
            @Override
            public boolean onPanStop(Vector3 touchVec) {
                setCameraChase(oldChaseactor);
                oldChaseactor = null;
                return true;
            }

            /**
             * Run this when the screen is panned
             *
             * @param touchVec
             *            The x/y/z world coordinates of the touch
             *
             * @param deltaX
             *            the change in X, in screen coordinates
             *
             * @param deltaY
             *            the change in Y, in screen coordinates
             */
            @Override
            public boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
                if (mChaseActor != null) {
                    oldChaseactor = mChaseActor;
                    mChaseActor = null;
                }
                float x = mGameCam.position.x - deltaX * .1f
                        * mGameCam.zoom;
                float y = mGameCam.position.y + deltaY * .1f
                        * mGameCam.zoom;
                // if x or y is too close to MAX,MAX, stick with max acceptable
                // values
                if (x > mCamBoundX - mConfig.mWidth * mGameCam.zoom
                        / mConfig.PIXEL_METER_RATIO / 2)
                    x = mCamBoundX - mConfig.mWidth * mGameCam.zoom
                            / mConfig.PIXEL_METER_RATIO / 2;
                if (y > mCamBoundY - mConfig.mHeight * mGameCam.zoom
                        / mConfig.PIXEL_METER_RATIO / 2)
                    y = mCamBoundY - mConfig.mHeight * mGameCam.zoom
                            / mConfig.PIXEL_METER_RATIO / 2;

                // if x or y is too close to 0,0, stick with minimum acceptable
                // values
                //
                // NB: we do MAX before MIN, so that if we're zoomed out, we
                // show extra space at the top instead of the bottom
                if (x < mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
                    x = mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;
                if (y < mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
                    y = mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;

                // update the camera position
                mGameCam.position.set(x, y, 0);
                return true;
            }
        };
        mHud.mPanControls.add(c);
        return c;
    }

    /**
     * Allow pinch-to-zoom
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param maxZoom The maximum zoom (out) factor. 8 is usually a good choice.
     * @param minZoom The minimum zoom (int) factor. .25f is usually a good choice.
     */
    public Control addPinchZoomControl(int x, int y, int width, int height, String imgName, final float maxZoom,
                                       final float minZoom) {
        Control c = new Control(this, imgName, x, y, width, height);
        c.mGestureAction = new GestureAction() {
            float lastZoom = 1;

            /**
             * Handle a down press (hopefully to turn it into a hold/release)
             *
             * @param touchVec
             *            The x/y/z coordinates of the touch
             */
            public boolean onDown(Vector3 touchVec) {
                lastZoom = mGameCam.zoom;
                return true;
            }

            /**
             * Handle a zoom-via-pinch event
             *
             * @param initialDistance
             *            The distance between fingers when the pinch started
             * @param distance
             *            The current distance between fingers
             */
            @Override
            public boolean zoom(float initialDistance, float distance) {
                float ratio = initialDistance / distance;
                float newZoom = lastZoom * ratio;
                if (newZoom > minZoom && newZoom < maxZoom)
                    mGameCam.zoom = newZoom;
                return false;
            }
        };
        mHud.mZoomControls.add(c);
        return c;
    }

    /**
     * Add an image to the heads-up display that changes its clipping rate to
     * seem to grow vertically, without stretching. Touching the image causes
     * its scale (0-100) to be sent to a ControlPressactor event
     *
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param width    The width of the image
     * @param height   The height of the image
     * @param imgName  The name of the image to display. Use "" for an invisible
     *                 button
     * @param callback The code to run when the bar is pressed
     */
    public Control addVerticalBar(final int x, final int y, final int width, final int height, String imgName,
                                  final LolCallback callback) {
        final Control c = new Control(this, imgName, x, y, width, height) {
            /**
             * Track if the bar is growing (true) or shrinking (false)
             */
            boolean mGrow = true;

            /**
             * The raw width of the image
             */
            int mTrueWidth;

            /**
             * The raw height of the image
             */
            int mTrueHeight;

            /**
             * The x position of the image's bottom left corner
             */
            int mTrueX;

            /**
             * This control requires run-time configuration... we track if it's
             * been done via this flag
             */
            boolean mConfigured = false;

            /**
             * This is the render method when we've got a valid TR. We're going
             * to play with how we draw, so that we can clip and stretch the
             * image
             *
             * @param sb
             *            The SpriteBatch to use to draw the image
             */
            @Override
            void render(SpriteBatch sb) {
                // one-time configuration
                if (!mConfigured) {
                    mTrueHeight = mImage.getRegionHeight();
                    mTrueWidth = mImage.getRegionWidth();
                    mTrueX = mImage.getRegionX();
                    mConfigured = true;
                }

                if (!mIsActive)
                    return;

                // draw it
                sb.draw(mImage.getTexture(), x, y, width / 2, height / 2, width,
                        (height * (int) callback.mFloatVal) / 100, 1, 1, 0, mTrueX, 0, mTrueWidth,
                        (mTrueHeight * (int) callback.mFloatVal) / 100, false, true);

                // don't keep showing anything if we've already received a
                // touch...
                if (!mIsTouchable)
                    return;

                // update size
                if (callback.mFloatVal == 100)
                    mGrow = false;
                if (callback.mFloatVal == 0)
                    mGrow = true;
                callback.mFloatVal = callback.mFloatVal + (mGrow ? 1 : -1);
            }
        };
        c.mGestureAction = new GestureAction() {
            /**
             * This is a touchable control...
             */
            @Override
            public boolean onTap(Vector3 v) {
                if (!c.mIsActive || !c.mIsTouchable)
                    return false;
                callback.onEvent();
                return true;
            }
        };
        // add to hud
        mHud.mControls.add(c);
        mHud.mTapControls.add(c);
        return c;
    }

    /**
     * Add a rotating button that generates a ControlPress actor event and passes
     * the rotation to the handler.
     *
     * @param x        The X coordinate of the bottom left corner (in pixels)
     * @param y        The Y coordinate of the bottom left corner (in pixels)
     * @param width    The width of the image
     * @param height   The height of the image
     * @param imgName  The name of the image to display. Use "" for an invisible
     *                 button
     * @param delta    Amount of rotation to add during each fraction of a second
     * @param callback The code to run when the rotator is pressed
     */
    public Control addRotator(final int x, final int y, final int width, final int height, String imgName,
                              final float delta, final LolCallback callback) {
        final Control c = new Control(this, imgName, x, y, width, height) {
            /**
             * This is the render method when we've got a valid TR. We're going
             * to play with how we draw, so that we can clip and stretch the
             * image
             *
             * @param sb
             *            The SpriteBatch to use to draw the image
             */
            @Override
            void render(SpriteBatch sb) {
                if (!mIsActive)
                    return;
                // draw it
                sb.draw(mImage, mRange.x, mRange.y, mRange.width / 2, 0, mRange.width, mRange.height, 1, 1,
                        callback.mFloatVal);

                // don't keep rotating if we've got a touch...
                if (!mIsTouchable)
                    return;

                // update rotation
                callback.mFloatVal += delta;
                if (callback.mFloatVal == 360)
                    callback.mFloatVal = 0;
            }
        };
        c.mGestureAction = new GestureAction() {
            /**
             * This is a touchable control...
             */
            @Override
            public boolean onTap(Vector3 v) {
                if (!c.mIsActive)
                    return false;
                callback.onEvent();
                return true;
            }
        };
        mHud.mControls.add(c);
        mHud.mTapControls.add(c);
        return c;
    }

    /**
     * Add an image to the heads-up display. Touching the image has no effect
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     */
    public Control addImage(int x, int y, int width, int height, String imgName) {
        Control c = new Control(this, imgName, x, y, width, height);
        c.mIsTouchable = false;
        mHud.mControls.add(c);
        return c;
    }

    /**
     * Add a control with callbacks for down, up, and pan
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param width   The width of the image
     * @param height  The height of the image
     * @param imgName The name of the image to display. Use "" for an invisible
     *                button
     * @param upCB    The callback to run when the Control is released
     * @param dnCB    The callback to run when the Control is pressed
     * @param mvCB    The callback to run when there is a finger move (pan) on the
     *                Control
     */
    public Control addPanCallbackControl(int x, int y, int width, int height, String imgName, final LolCallback upCB, final LolCallback dnCB, final LolCallback mvCB) {
        final Control c = new Control(this, imgName, x, y, width, height);
        // Pan only consists of pan-stop and pan events. That means we can't
        // capture a down-press or up-press that isn't also involved in a move.
        // To overcome this limitation, we'll make this BOTH a pan control and a
        // toggle control
        c.mGestureAction = new GestureAction() {
            /**
             * Toggle action: either call the "up" callback or the "down" callback
             */
            @Override
            public boolean toggle(boolean isUp, Vector3 touchVec) {
                // up event
                if (isUp) {
                    upCB.mUpLocation = touchVec.cpy();
                    upCB.onEvent();
                    mHolding = false;
                }
                // down event
                else {
                    mHolding = true;
                    dnCB.mDownLocation = touchVec.cpy();
                    dnCB.onEvent();
                }
                // toggle state
                mHolding = !isUp;
                return true;
            }

            /**
             * Finger move action: call the "pan" callback
             */
            @Override
            public boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
                // force a down event, if we didn't get one
                if (!mHolding) {
                    toggle(false, touchVec);
                    return true;
                }
                // pan event
                mvCB.mMoveLocation = touchVec.cpy();
                mvCB.onEvent();
                return true;
            }

            /**
             * Pan stop doesn't always trigger an up, so force one if necessary
             */
            @Override
            public boolean onPanStop(Vector3 touchVec) {
                // force an up event?
                if (mHolding) {
                    toggle(true, touchVec);
                    return true;
                }
                return false;
            }
        };
        mHud.mControls.add(c);
        mHud.mPanControls.add(c);
        mHud.mToggleControls.add(c);
        return c;
    }

    /**
     * Look up a fact that was stored for the current level. If no such fact
     * exists, defaultVal will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The default value to use if the fact cannot be found
     * @return The integer value corresponding to the last value stored
     */
    public int getLevelFact(String factName, int defaultVal) {
        Integer i = mLevelFacts.get(factName);
        if (i == null) {
            Lol.message(mConfig, "ERROR", "Error retreiving level fact '" + factName + "'");
            return defaultVal;
        }
        return i;
    }

    /**
     * Save a fact about the current level. If the factName has already been
     * used for this level, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public void putLevelFact(String factName, int factValue) {
        mLevelFacts.put(factName, factValue);
    }

    /**
     * Look up a fact that was stored for the current game session. If no such
     * fact exists, -1 will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The default value to use if the fact cannot be found
     * @return The integer value corresponding to the last value stored
     */
    public int getSessionFact(String factName, int defaultVal) {
        Integer i = mGame.mSessionFacts.get(factName);
        if (i == null) {
            Lol.message(mConfig, "ERROR", "Error retreiving level fact '" + factName + "'");
            return defaultVal;
        }
        return i;
    }

    /**
     * Save a fact about the current game session. If the factName has already
     * been used for this game session, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public void putSessionFact(String factName, int factValue) {
        mGame.mSessionFacts.put(factName, factValue);
    }

    /**
     * Look up a fact that was stored for the current game session. If no such
     * fact exists, defaultVal will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The value to return if the fact does not exist
     * @return The integer value corresponding to the last value stored
     */
    public int getGameFact(String factName, int defaultVal) {
        return Lol.getGameFact(mConfig, factName, defaultVal);
    }

    /**
     * Save a fact about the current game session. If the factName has already
     * been used for this game session, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
    public void putGameFact(String factName, int factValue) {
        Lol.putGameFact(mConfig, factName, factValue);
    }

    /**
     * Look up an Actor that was stored for the current level. If no such Actor
     * exists, null will be returned.
     *
     * @param actorName The name used to store the Actor
     * @return The last Actor stored with this name
     */
    public Actor getLevelActor(String actorName) {
        Actor actor = mLevelActors.get(actorName);
        if (actor == null) {
            Lol.message(mConfig, "ERROR", "Error retreiving level fact '" + actorName + "'");
            return null;
        }
        return actor;
    }

    /**
     * Save a Actor from the current level. If the actorName has already been
     * used for this level, the new value will overwrite the old.
     *
     * @param actorName The name for the Actor being saved
     * @param actor     The Actor that is the fact being saved
     */
    public void putLevelActor(String actorName, Actor actor) {
        mLevelActors.put(actorName, actor);
    }

    /**
     * Set the background color for the current level
     *
     * @param color The color, formated as #RRGGBB
     */
    public void setBackgroundColor(String color) {
        mBackground.mColor = Color.valueOf(color);
    }

    /**
     * Add a picture that may repeat in the X dimension
     *
     * @param xSpeed  Speed that the picture seems to move in the X direction. "1"
     *                is the same speed as the hero; "0" is not at all; ".5f" is at
     *                half the hero's speed
     * @param ySpeed  Speed that the picture seems to move in the Y direction. "1"
     *                is the same speed as the hero; "0" is not at all; ".5f" is at
     *                half the hero's speed
     * @param imgName The name of the image file to use as the background
     * @param yOffset The default is to draw the image at y=0. This field allows the
     *                picture to be moved up or down.
     * @param width   The width of the image being used as a background layer
     * @param height  The height of the image being used as a background layer
     */
    public void addHorizontalBackgroundLayer(float xSpeed, float ySpeed,
                                             String imgName, float yOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed,
                mMedia.getImage(imgName), 0, yOffset
                * mConfig.PIXEL_METER_RATIO, width, height);
        pl.mXRepeat = xSpeed != 0;
        mBackground.mLayers.add(pl);
    }

    /**
     * Add a picture that may repeat in the X dimension, and which moves
     * automatically
     *
     * @param xSpeed  Speed, in pixels per second
     * @param imgName The name of the image file to use as the background
     * @param yOffset The default is to draw the image at y=0. This field allows the
     *                picture to be moved up or down.
     * @param width   The width of the image being used as a background layer
     * @param height  The height of the image being used as a background layer
     */
    public void addHorizontalAutoBackgroundLayer(float xSpeed, String imgName,
                                                 float yOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, 0,
                mMedia.getImage(imgName), 0, yOffset
                * mConfig.PIXEL_METER_RATIO, width, height);
        pl.mAutoX = true;
        pl.mXRepeat = xSpeed != 0;
        mBackground.mLayers.add(pl);
    }

    /**
     * Add a picture that may repeat in the Y dimension
     *
     * @param xSpeed  Speed that the picture seems to move in the X direction. "1"
     *                is the same speed as the hero; "0" is not at all; ".5f" is at
     *                half the hero's speed
     * @param ySpeed  Speed that the picture seems to move in the Y direction. "1"
     *                is the same speed as the hero; "0" is not at all; ".5f" is at
     *                half the hero's speed
     * @param imgName The name of the image file to use as the background
     * @param xOffset The default is to draw the image at x=0. This field allows the
     *                picture to be moved left or right.
     * @param width   The width of the image being used as a background layer
     * @param height  The height of the image being used as a background layer
     */
    public void addVerticalBackgroundLayer(float xSpeed, float ySpeed,
                                           String imgName, float xOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed,
                mMedia.getImage(imgName),
                xOffset * mConfig.PIXEL_METER_RATIO, 0, width, height);
        pl.mYRepeat = ySpeed != 0;
        mBackground.mLayers.add(pl);
    }

    /**
     * Create a particle effect system
     *
     * @param filename The file holding the particle definition
     * @param zIndex   The z index of the particle system.
     * @param x The x coordinate of the starting point of the particle system
     * @param y The y coordinate of the starting point of the particle system
     */
    public Effect makeParticleSystem(String filename, int zIndex, float x, float y) {
        Effect e = new Effect();

        // create the particle effect system.
        ParticleEffect pe = new ParticleEffect();
        pe.load(Gdx.files.internal(filename), Gdx.files.internal(""));
        e.mParticleEffect = pe;

        // update the effect's coordinates to reflect world coordinates
        pe.getEmitters().first().setPosition(x, y);

        // NB: we pretend effects are Actors, so that we can have them in front of or behind Actors
        addActor(e, zIndex);

        // start emitting particles
        pe.start();
        return e;
    }

    /**
     * Add a picture that may repeat in the X dimension
     *
     * @param xSpeed  Speed that the picture seems to move in the X direction. "1"
     *                is the same speed as the hero; "0" is not at all; ".5f" is at
     *                half the hero's speed
     * @param ySpeed  Speed that the picture seems to move in the Y direction. "1"
     *                is the same speed as the hero; "0" is not at all; ".5f" is at
     *                half the hero's speed
     * @param imgName The name of the image file to use as the foreground
     * @param yOffset The default is to draw the image at y=0. This field allows the
     *                picture to be moved up or down.
     * @param width   The width of the image being used as a foreground layer
     * @param height  The height of the image being used as a foreground layer
     */
    public void addHorizontalForegroundLayer(float xSpeed, float ySpeed,
                                             String imgName, float yOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed,
                mMedia.getImage(imgName), 0, yOffset
                * mConfig.PIXEL_METER_RATIO, width, height);
        pl.mXRepeat = xSpeed != 0;
        mForeground.mLayers.add(pl);
    }

    /**
     * Add a picture that may repeat in the X dimension, and which moves
     * automatically
     *
     * @param xSpeed  Speed, in pixels per second
     * @param imgName The name of the image file to use as the foreground
     * @param yOffset The default is to draw the image at y=0. This field allows the
     *                picture to be moved up or down.
     * @param width   The width of the image being used as a foreground layer
     * @param height  The height of the image being used as a foreground layer
     */
    public void addHorizontalAutoForegroundLayer(float xSpeed, String imgName,
                                                 float yOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, 0,
                mMedia.getImage(imgName), 0, yOffset
                * mConfig.PIXEL_METER_RATIO, width, height);
        pl.mAutoX = true;
        pl.mXRepeat = xSpeed != 0;
        mForeground.mLayers.add(pl);
    }

    /**
     * Add a picture that may repeat in the Y dimension
     *
     * @param xSpeed  Speed that the picture seems to move in the Y direction. "1"
     *                is the same speed as the hero; "0" is not at all; ".5f" is at
     *                half the hero's speed
     * @param ySpeed  Speed that the picture seems to move in the Y direction. "1"
     *                is the same speed as the hero; "0" is not at all; ".5f" is at
     *                half the hero's speed
     * @param imgName The name of the image file to use as the foreground
     * @param xOffset The default is to draw the image at x=0. This field allows the
     *                picture to be moved left or right.
     * @param width   The width of the image being used as a foreground layer
     * @param height  The height of the image being used as a foreground layer
     */
    public void addVerticalForegroundLayer(float xSpeed, float ySpeed,
                                           String imgName, float xOffset, float width, float height) {
        ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed,
                mMedia.getImage(imgName),
                xOffset * mConfig.PIXEL_METER_RATIO, 0, width, height);
        pl.mYRepeat = ySpeed != 0;
        mForeground.mLayers.add(pl);
    }

    /**
     * Get the LoseScene that is configured for the current level, or create a
     * blank one if none exists.
     *
     * @return The current LoseScene
     */
    public LoseScene getLoseScene() {
        LoseScene scene = mLoseScene;
        if (scene != null)
            return scene;
        scene = new LoseScene(this);
        mLoseScene = scene;
        return scene;
    }

    /**
     * Get the PreScene that is configured for the current level, or create a
     * blank one if none exists.
     *
     * @return The current PreScene
     */
    public PreScene getPreScene() {
        PreScene scene = mPreScene;
        if (scene != null)
            return scene;
        scene = new PreScene(this);
        // immediately make the scene visible
        scene.mVisible = true;
        mPreScene = scene;
        // NB: disable the timer so the game doesn't start playing in the
        // background
        scene.suspendClock();
        return scene;
    }

    /**
     * Get the PauseScene that is configured for the current level, or create a
     * blank one if none exists.
     *
     * @return The current PauseScene
     */
    public PauseScene getPauseScene() {
        PauseScene scene = mPauseScene;
        if (scene != null)
            return scene;
        scene = new PauseScene(this);
        mPauseScene = scene;
        return scene;
    }

    /**
     * Get the WinScene that is configured for the current level, or create a
     * blank one if none exists.
     *
     * @return The current WinScene
     */
    public WinScene getWinScene() {
        WinScene scene = mWinScene;
        if (scene != null)
            return scene;
        scene = new WinScene(this);
        mWinScene = scene;
        return scene;
    }

    /**
     * Make an enemy that has an underlying rectangular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the enemy
     * @param height  The height of the enemy
     * @param imgName The name of the image to display
     * @return The enemy, so that it can be modified further
     */
    public Enemy makeEnemyAsBox(float x, float y, float width, float height, String imgName) {
        Enemy e = new Enemy(this, width, height, imgName);
        e.setBoxPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y);
        addActor(e, 0);
        return e;
    }

    /**
     * Draw an enemy with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this
     *                polygon, listed as x0,y0,x1,y1,x2,y2,...
     * @return The enemy, so that it can be further modified
     */
    public Enemy makeEnemyAsPolygon(float x, float y, float width, float height, String imgName, float... verts) {
        Enemy e = new Enemy(this, width, height, imgName);
        e.setPolygonPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, verts);
        addActor(e, 0);
        return e;
    }

    /**
     * Make an enemy that has an underlying circular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the enemy
     * @param height  The height of the enemy
     * @param imgName The name of the image to display
     * @return The enemy, so that it can be modified further
     */
    public Enemy makeEnemyAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Enemy e = new Enemy(this, radius, radius, imgName);
        e.setCirclePhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, radius / 2);
        addActor(e, 0);
        return e;
    }

    /**
     * Make a destination that has an underlying rectangular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the destination
     * @param height  The height of the destination
     * @param imgName The name of the image to display
     * @return The destination, so that it can be modified further
     */
    public Destination makeDestinationAsBox(float x, float y, float width, float height, String imgName) {
        Destination d = new Destination(this, width, height, imgName);
        d.setBoxPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y);
        d.setCollisionsEnabled(false);
        addActor(d, 0);
        return d;
    }

    /**
     * Draw a destination with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this
     *                polygon, listed as x0,y0,x1,y1,x2,y2,...
     * @return The destination, so that it can be further modified
     */
    public Destination makeDestinationAsPolygon(float x, float y, float width, float height, String imgName, float... verts) {
        Destination d = new Destination(this, width, height, imgName);
        d.setPolygonPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, verts);
        d.setCollisionsEnabled(false);
        addActor(d, 0);
        return d;
    }

    /**
     * Make a destination that has an underlying circular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the destination
     * @param height  The height of the destination
     * @param imgName The name of the image to display
     * @return The destination, so that it can be modified further
     */
    public Destination makeDestinationAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Destination d = new Destination(this, radius, radius, imgName);
        d.setCirclePhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, radius / 2);
        d.setCollisionsEnabled(false);
        addActor(d, 0);
        return d;
    }

    /**
     * Draw an obstacle with an underlying box shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @return The obstacle, so that it can be further modified
     */
    public Obstacle makeObstacleAsBox(float x, float y, float width, float height, String imgName) {
        Obstacle o = new Obstacle(this, width, height, imgName);
        o.setBoxPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y);
        addActor(o, 0);
        return o;
    }

    /**
     * Draw an obstacle with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this
     *                polygon, listed as x0,y0,x1,y1,x2,y2,...
     * @return The obstacle, so that it can be further modified
     */
    public Obstacle makeObstacleAsPolygon(float x, float y, float width, float height, String imgName, float... verts) {
        Obstacle o = new Obstacle(this, width, height, imgName);
        o.setPolygonPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, verts);
        addActor(o, 0);
        return o;
    }

    /**
     * Draw an obstacle with an underlying circle shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @return The obstacle, so that it can be further modified
     */
    public Obstacle makeObstacleAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Obstacle o = new Obstacle(this, width, height, imgName);
        o.setCirclePhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, radius / 2);
        addActor(o, 0);
        return o;
    }

    /**
     * Draw a goodie with an underlying box shape, and a default score of
     * [1,0,0,0]
     *
     * @param x       X coordinate of bottom left corner
     * @param y       Y coordinate of bottom left corner
     * @param width   Width of the image
     * @param height  Height of the image
     * @param imgName Name of image file to use
     * @return The goodie, so that it can be further modified
     */
    public Goodie makeGoodieAsBox(float x, float y, float width, float height, String imgName) {
        Goodie g = new Goodie(this, width, height, imgName);
        g.setBoxPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y);
        g.setCollisionsEnabled(false);
        addActor(g, 0);
        return g;
    }

    /**
     * Draw a goodie with an underlying circle shape, and a default score of
     * [1,0,0,0]
     *
     * @param x       X coordinate of bottom left corner
     * @param y       Y coordinate of bottom left corner
     * @param width   Width of the image
     * @param height  Height of the image
     * @param imgName Name of image file to use
     * @return The goodie, so that it can be further modified
     */
    public Goodie makeGoodieAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Goodie g = new Goodie(this, width, height, imgName);
        g.setCirclePhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, radius / 2);
        g.setCollisionsEnabled(false);
        addActor(g, 0);
        return g;
    }

    /**
     * Draw a goodie with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this
     *                polygon, listed as x0,y0,x1,y1,x2,y2,...
     * @return The goodie, so that it can be further modified
     */
    public Goodie makeGoodieAsPolygon(float x, float y, float width, float height, String imgName, float... verts) {
        Goodie g = new Goodie(this, width, height, imgName);
        g.setPolygonPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, verts);
        g.setCollisionsEnabled(false);
        addActor(g, 0);
        return g;
    }

    /**
     * Make a Hero with an underlying rectangular shape
     *
     * @param x       X coordinate of the hero
     * @param y       Y coordinate of the hero
     * @param width   width of the hero
     * @param height  height of the hero
     * @param imgName File name of the default image to display
     * @return The hero that was created
     */
    public Hero makeHeroAsBox(float x, float y, float width, float height, String imgName) {
        Hero h = new Hero(this, width, height, imgName);
        h.setBoxPhysics(0, 0, 0, BodyDef.BodyType.DynamicBody, false, x, y);
        addActor(h, 0);
        return h;
    }

    /**
     * Make a Hero with an underlying circular shape
     *
     * @param x       X coordinate of the hero
     * @param y       Y coordinate of the hero
     * @param width   width of the hero
     * @param height  height of the hero
     * @param imgName File name of the default image to display
     * @return The hero that was created
     */
    public Hero makeHeroAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Hero h = new Hero(this, width, height, imgName);
        h.setCirclePhysics(0, 0, 0, BodyDef.BodyType.DynamicBody, false, x, y, radius / 2);
        addActor(h, 0);
        return h;
    }

    /**
     * Draw a hero with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this
     *                polygon, listed as x0,y0,x1,y1,x2,y2,...
     * @return The hero, so that it can be further modified
     */
    public Hero makeHeroAsPolygon(float x, float y, float width, float height, String imgName, float... verts) {
        Hero h = new Hero(this, width, height, imgName);
        h.setPolygonPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y, verts);
        addActor(h, 0);
        return h;
    }

    /**
     * Specify a limit on how far away from the Hero a projectile can go.
     * Without this, projectiles could keep on traveling forever.
     *
     * @param distance Maximum distance from the hero that a projectile can travel
     */
    public void setProjectileRange(float distance) {
        for (Projectile p : mProjectilePool.mPool)
            p.mRange = distance;
    }

    /**
     * Indicate that projectiles should feel the effects of gravity. Otherwise,
     * they will be (more or less) immune to gravitational forces.
     */
    public void setProjectileGravityOn() {
        for (Projectile p : mProjectilePool.mPool)
            p.mBody.setGravityScale(1);
    }

    /**
     * Specify the image file from which to randomly choose projectile images
     *
     * @param imgName The file to use when picking images
     *                <p>
     *                TODO: this is broken now that we removed Animatable images
     */
    public void setProjectileImageSource(String imgName) {
        for (Projectile p : mProjectilePool.mPool)
            p.mAnimator.updateImage(this, imgName);
        mProjectilePool.mRandomizeImages = true;
    }

    /**
     * The "directional projectile" mechanism might lead to the projectiles
     * moving too fast. This will cause the speed to be multiplied by a factor
     *
     * @param factor The value to multiply against the projectile speed.
     */
    public void setProjectileVectorDampeningFactor(float factor) {
        mProjectilePool.mDirectionalDamp = factor;
    }

    /**
     * Indicate that all projectiles should participate in collisions, rather
     * than disappearing when they collide with other actors
     */
    public void enableCollisionsForProjectiles() {
        mProjectilePool.mSensorProjectiles = false;
    }

    /**
     * Indicate that projectiles thrown with the "directional" mechanism should
     * have a fixed velocity
     *
     * @param velocity The magnitude of the velocity for projectiles
     */
    public void setFixedVectorThrowVelocityForProjectiles(float velocity) {
        mProjectilePool.mEnableFixedVectorVelocity = true;
        mProjectilePool.mFixedVectorVelocity = velocity;
    }

    /**
     * Indicate that projectiles thrown via the "directional" mechanism should
     * be rotated to face in their direction or movement
     */
    public void setRotateVectorThrowForProjectiles() {
        mProjectilePool.mRotateVectorThrow = true;
    }

    /**
     * Indicate that when two projectiles collide, they should both remain on
     * screen
     */
    public void setCollisionOkForProjectiles() {
        for (Projectile p : mProjectilePool.mPool)
            p.mDisappearOnCollide = false;
    }

    /**
     * Describe the behavior of projectiles in a scene. You must call this if
     * you intend to use projectiles in your scene.
     *
     * @param size     number of projectiles that can be thrown at once
     * @param width    width of a projectile
     * @param height   height of a projectile
     * @param imgName  image to use for projectiles
     * @param strength specifies the amount of damage that a projectile does to an
     *                 enemy
     * @param zIndex   The z plane on which the projectiles should be drawn
     * @param isCircle Should projectiles have an underlying circle or box shape?
     */
    public void configureProjectiles(int size, float width, float height, String imgName, int strength, int zIndex,
                                     boolean isCircle) {
        mProjectilePool = new ProjectilePool(this, size, width, height, imgName, strength, zIndex,
                isCircle);
    }

    /**
     * Set a limit on the total number of projectiles that can be thrown
     *
     * @param number How many projectiles are available
     */
    public void setNumberOfProjectiles(int number) {
        mProjectilePool.mProjectilesRemaining = number;
    }

    /**
     * Specify a sound to play when the projectile is thrown
     *
     * @param soundName Name of the sound file to play
     */
    public void setThrowSound(String soundName) {
        mProjectilePool.mThrowSound = mMedia.getSound(soundName);
    }

    /**
     * Specify the sound to play when a projectile disappears
     *
     * @param soundName the name of the sound file to play
     */
    public void setProjectileDisappearSound(String soundName) {
        mProjectilePool.mProjectileDisappearSound = mMedia.getSound(soundName);
    }

    /**
     * Specify how projectiles should be animated
     *
     * @param a The animation object to use for each projectile that is thrown
     */
    public void setProjectileAnimation(Animation a) {
        for (Projectile p : mProjectilePool.mPool)
            p.setDefaultAnimation(a);
    }

    /**
     * Draw a box on the scene Note: the box is actually four narrow rectangles
     *
     * @param x0         X coordinate of top left corner
     * @param y0         Y coordinate of top left corner
     * @param x1         X coordinate of bottom right corner
     * @param y1         Y coordinate of bottom right corner
     * @param imgName    name of the image file to use when drawing the rectangles
     * @param density    Density of the rectangle. When in doubt, use 1
     * @param elasticity Elasticity of the rectangle. When in doubt, use 0
     * @param friction   Friction of the rectangle. When in doubt, use 1
     */
    public void drawBoundingBox(float x0, float y0, float x1, float y1, String imgName, float density,
                                float elasticity, float friction) {
        // draw four rectangles and we're good
        Obstacle bottom = makeObstacleAsBox(x0 - 1, y0 - 1, Math.abs(x0 - x1) + 2, 1, imgName);
        bottom.setPhysics(density, elasticity, friction);

        Obstacle top = makeObstacleAsBox(x0 - 1, y1, Math.abs(x0 - x1) + 2, 1, imgName);
        top.setPhysics(density, elasticity, friction);

        Obstacle left = makeObstacleAsBox(x0 - 1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        left.setPhysics(density, elasticity, friction);

        Obstacle right = makeObstacleAsBox(x1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        right.setPhysics(density, elasticity, friction);
    }

    /**
     * Load an SVG line drawing generated from Inkscape. The SVG will be loaded
     * as a bunch of Obstacles. Note that not all Inkscape drawings will work as
     * expected... if you need more power than this provides, you'll have to
     * modify Svg.java
     *
     * @param svgName  Name of the svg file to load. It should be in the assets
     *                 folder
     * @param stretchX Stretch the drawing in the X dimension by this percentage
     * @param stretchY Stretch the drawing in the Y dimension by this percentage
     * @param xposeX   Shift the drawing in the X dimension. Note that shifting
     *                 occurs after stretching
     * @param xposeY   Shift the drawing in the Y dimension. Note that shifting
     *                 occurs after stretching
     * @param ac       A callback for customizing each (obstacle) line segment of the SVG
     */
    public void importLineDrawing(String svgName, float stretchX, float stretchY,
                                  float xposeX, float xposeY, Svg.ActorCallback ac) {
        // Create an SVG object to hold all the parameters, then use it to parse
        // the file
        Svg s = new Svg(stretchX, stretchY, xposeX, xposeY, ac);
        s.parse(this, svgName);
    }

    /**
     * Use this to manage the state of Mute
     */
    public void toggleMute() {
        // volume is either 1 or 0
        if (getGameFact("volume", 1) == 1) {
            // set volume to 0, set image to 'unmute'
            putGameFact("volume", 0);
        } else {
            // set volume to 1, set image to 'mute'
            putGameFact("volume", 1);
        }
        // update all music
        mMedia.resetMusicVolume();
    }

    /**
     * Use this to determine if the game is muted or not. True corresponds to
     * not muted, false corresponds to muted.
     */
    public boolean getVolume() {
        return getGameFact("volume", 1) == 1;
    }

    /**
     * Draw a picture on the current level
     * <p>
     * Note: the order in which this is called relative to other actors will
     * determine whether they go under or over this picture.
     *
     * @param x       X coordinate of bottom left corner
     * @param y       Y coordinate of bottom left corner
     * @param width   Width of the picture
     * @param height  Height of this picture
     * @param imgName Name of the picture to display
     * @param zIndex  The z index of the image. There are 5 planes: -2, -2, 0, 1,
     *                and 2. By default, everything goes to plane 0
     */
    public void drawPicture(final float x, final float y, final float width, final float height,
                            final String imgName, int zIndex) {
        addActor(makePicture(x, y, width, height, imgName), zIndex);
    }

    /**
     * Draw some text on the current level
     * <p>
     * Note: the order in which this is called relative to other actors will
     * determine whether they go under or over this text.
     *
     * @param x        X coordinate of bottom left corner of the text
     * @param y        Y coordinate of bottom left corner of the text
     * @param text     The text to display
     * @param fontName The name of the font file to use
     * @param size     The font size to use
     * @param zIndex   The z index of the image. There are 5 planes: -2, -2, 0, 1,
     *                 and 2. By default, everything goes to plane 0
     */
    public void drawText(final float x, final float y, final String text, final String fontColor, String fontName, int size, int zIndex) {
        final BitmapFont bf = mMedia.getFont(fontName, size);
        Renderable r = new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(Color.valueOf(fontColor));
                bf.getData().setScale(1 / mConfig.PIXEL_METER_RATIO);
                glyphLayout.setText(bf, text);
                bf.draw(sb, text, x, y + glyphLayout.height);
                bf.getData().setScale(1);
            }
        };
        addActor(r, zIndex);
    }

    /**
     * Draw some text on the current level, centered on a point.
     * <p>
     * Note: the order in which this is called relative to other actors will
     * determine whether they go under or over this text.
     *
     * @param centerX  X coordinate of center of the text
     * @param centerY  Y coordinate of center of the text
     * @param text     The text to display
     * @param fontName The name of the font file to use
     * @param size     The font size to use
     * @param zIndex   The z index of the image. There are 5 planes: -2, -2, 0, 1,
     *                 and 2. By default, everything goes to plane 0
     */
    public void drawTextCentered(final float centerX, final float centerY, final String text, final String fontColor, String fontName, int size, int zIndex) {
        final BitmapFont bf = mMedia.getFont(fontName, size);

        // figure out the image dimensions
        bf.getData().setScale(1 / mConfig.PIXEL_METER_RATIO);
        glyphLayout.setText(bf, text);
        final float w = glyphLayout.width;
        final float h = glyphLayout.height;
        bf.getData().setScale(1);

        // describe how to render it
        Renderable r = new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(Color.valueOf(fontColor));
                bf.getData().setScale(1 / mConfig.PIXEL_METER_RATIO);
                bf.draw(sb, text, centerX - w / 2, centerY + h / 2);
                bf.getData().setScale(1);
            }
        };
        addActor(r, zIndex);
    }

    /**
     * Generate a random number x such that 0 &lt;= x &lt; max
     *
     * @param max The largest number returned will be one less than max
     * @return a random integer
     */
    public int getRandom(int max) {
        return sGenerator.nextInt(max);
    }

    /**
     * A random number generator... We provide this so that new game developers
     * don't create lots of Random()s throughout their code
     */
    static Random sGenerator = new Random();

    /**
     * Report whether all levels should be treated as unlocked. This is useful
     * in Chooser, where we might need to prevent some levels from being played.
     */
    public boolean getUnlockMode() {
        return mConfig.mUnlockAllLevels;
    }


    /**
     * Use this to load the splash screen
     */
    public void doSplash() {
        mGame.doSplash();
    }

    /**
     * Use this to load the level-chooser screen. Note that when the chooser is
     * disabled, we jump straight to level 1.
     *
     * @param whichChooser The chooser screen to create
     */
    public void doChooser(int whichChooser) {
        mGame.doChooser(whichChooser);
    }

    /**
     * Use this to load a playable level.
     *
     * @param which The index of the level to load
     */
    public void doLevel(int which) {
        mGame.doLevel(which);
    }

    /**
     * Use this to load a help level.
     *
     * @param which The index of the help level to load
     */
    public void doHelp(int which) {
        mGame.doHelp(which);
    }

    /**
     * Use this to load a screen of the store.
     *
     * @param which The index of the help level to load
     */
    public void doStore(int which) {
        mGame.doStore(which);
    }

    /**
     * Use this to quit the game
     */
    public void doQuit() {
        mGame.doQuit();
    }

    public Animation makeAnimation(int sequenceCount, boolean repeat) {
        return new Animation(this.mMedia, sequenceCount, repeat);
    }

    public Animation makeAnimation(int timePerFrame, boolean repeat, String... imgNames) {
        return new Animation(this.mMedia, timePerFrame, repeat, imgNames);
    }

    /**
     * Use this for determining bounds of text boxes
     */
    static GlyphLayout glyphLayout = new GlyphLayout();

    /**
     * Create a Renderable that consists of an image
     *
     * @param x       The X coordinate of the bottom left corner, in pixels
     * @param y       The Y coordinate of the bottom left corner, in pixels
     * @param width   The image width, in pixels
     * @param height  The image height, in pixels
     * @param imgName The file name for the image, or ""
     * @return A Renderable of the image
     */
    Renderable makePicture(final float x, final float y, final float width, final float height,
                           String imgName) {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        final TextureRegion tr = mMedia.getImage(imgName);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw
     *
     * @param x        The X coordinate of the bottom left corner, in pixels
     * @param y        The Y coordinate of the bottom left corner, in pixels
     * @param message  The text to display... note that it can't change on the fly
     * @param fontName The font to use
     * @param size     The font size
     * @return A Renderable of the text
     */
    Renderable makeText(final int x, final int y, final String message, final String fontColor, String fontName, int size) {
        final BitmapFont bf = mMedia.getFont(fontName, size);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(Color.valueOf(fontColor));
                glyphLayout.setText(bf, message);
                bf.draw(sb, message, x, y + glyphLayout.height);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw. The text will be
     * centered vertically and horizontally on the screen
     *
     * @param message  The text to display... note that it can't change on the fly
     * @param fontName The font to use
     * @param size     The font size
     * @return A Renderable of the text
     */
    Renderable makeText(final String message, final String fontColor,
                        String fontName, int size) {
        final BitmapFont bf = mMedia.getFont(fontName, size);
        glyphLayout.setText(bf, message);
        final float x = mConfig.mWidth / 2 - glyphLayout.width / 2;
        final float y = mConfig.mHeight / 2 + glyphLayout.height / 2;
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(Color.valueOf(fontColor));
                bf.draw(sb, message, x, y);
            }
        };
    }


    /**
     * A helper method to draw text nicely. In GDX, we draw everything by giving
     * the bottom left corner, except text, which takes the top left corner.
     * This function handles the conversion, so that we can use bottom-left.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param message The text to display
     * @param bf      The BitmapFont object to use for the text's font
     * @param sb      The SpriteBatch used to render the text
     */
    void drawTextTransposed(int x, int y, String message, BitmapFont bf, SpriteBatch sb) {
        glyphLayout.setText(bf, message);
        bf.draw(sb, message, x, y + glyphLayout.height);
    }
}
