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
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.Controls.HudEntity;

import java.util.ArrayList;

/**
 * A Level is a playable portion of the game. Levels can be infinite, or they
 * can have an end goal. Level has two components. One is the part that is
 * visible to the game designer, which involves some limited control over the
 * camera and music, and the ability to request that custom code run after a
 * fixed amount of time. These timers can also be attached to a specific enemy,
 * if desired. Internally, Level is responsible for managing a set of cameras
 * used to display everything that appears on the screen. It is also responsible
 * for keeping track of everything on the screen (game entities and Controls).
 */
public class Level extends ScreenAdapter {
    /**
     * The music, if any
     */
    private Music mMusic;

    /**
     * Whether the music is playing or not
     */
    private boolean mMusicPlaying;

    /**
     * A reference to the score object, for tracking winning and losing
     */
    Score mScore = new Score();

    /**
     * A reference to the tilt object, for managing how tilts are handled
     */
    Tilt mTilt = new Tilt();

    /**
     * The physics world in which all entities interact
     */
    World mWorld;

    /**
     * The set of Parallax backgrounds
     */
    Background mBackground = new Background();

    /**
     * The scene to show when the level is created (if any)
     */
    PreScene mPreScene;

    /**
     * The scene to show when the level is won or lost
     */
    PostScene mPostScene = new PostScene();

    /**
     * The scene to show when the level is paused (if any)
     */
    PauseScene mPauseScene;

    /**
     * All the sprites, in 5 planes. We draw them as planes -2, -1, 0, 1, 2
     */
    private final ArrayList<ArrayList<Renderable>> mSprites = new ArrayList<ArrayList<Renderable>>(5);

    /**
     * The controls / heads-up-display
     */
    ArrayList<HudEntity> mControls = new ArrayList<HudEntity>();

    /**
     * Events that get processed on the next render, then discarded
     */
    ArrayList<Action> mOneTimeEvents = new ArrayList<Action>();

    /**
     * When the level is won or lost, this is where we store the event that
     * needs to run
     */
    Action mEndGameEvent;

    /**
     * Events that get processed on every render
     */
    ArrayList<Action> mRepeatEvents = new ArrayList<Action>();

    /**
     * This camera is for drawing entities that exist in the physics world
     */
    OrthographicCamera mGameCam;

    /**
     * This camera is for drawing controls that sit above the world
     */
    OrthographicCamera mHudCam;

    /**
     * This camera is for drawing parallax backgrounds that go behind the world
     */
    ParallaxCamera mBgCam;

    /**
     * This is the sprite that the camera chases
     */
    private PhysicsSprite mChaseEntity;

    /**
     * The maximum x value of the camera
     */
    int mCamBoundX;

    /**
     * The maximum y value of the camera
     */
    int mCamBoundY;

    /**
     * The debug renderer, for printing circles and boxes for each entity
     */
    private final Box2DDebugRenderer mDebugRender = new Box2DDebugRenderer();

    /**
     * The spritebatch for drawing all texture regions and fonts
     */
    private final SpriteBatch mSpriteBatch = new SpriteBatch();

    /**
     * The debug shape renderer, for putting boxes around HUD entities
     */
    private final ShapeRenderer mShapeRender = new ShapeRenderer();

    /**
     * We use this to avoid garbage collection when converting screen touches to
     * camera coordinates
     */
    private final Vector3 mTouchVec = new Vector3();

    /**
     * When there is a touch of an entity in the physics world, this is how we
     * find it
     */
    private PhysicsSprite mHitSprite = null;

    /**
     * This callback is used to get a touched entity from the physics world
     */
    private QueryCallback mTouchCallback;

    /**
     * Our polling-based multitouch uses this array to track the previous state
     * of 4 fingers
     */
    private final boolean[] mLastTouches = new boolean[4];

    /**
     * When transitioning between a pre-scene and the game, we need to be sure
     * that a down press on the pre-scene doesn't show up as a move event on the
     * game... we achieve it via this
     */
    private boolean mTouchActive = true;

    /**
     * The LOL interface requires that game designers don't have to construct
     * Level manually. To make it work, we store the current Level here
     */
    static Level sCurrent;

    /**
     * Entities may need to set callbacks to run on a screen touch. If so, they
     * can use this.
     */
    TouchAction mTouchResponder;

    /**
     * In levels with a projectile pool, the pool is accessed from here
     */
    ProjectilePool mProjectilePool;

    /**
     * Wrapper for actions that we generate and then want handled during the
     * render loop
     */
    interface Action {
        void go();
    }

    /**
     * Wrapper for handling code that needs to run in response to a screen touch
     */
    static class TouchAction {
        /**
         * Run this when the screen is initially pressed down
         * 
         * @param x The X coordinate, in pixels, of the touch
         * @param y The Y coordinate, in pixels, of the touch
         */
        void onDown(float x, float y) {
        }

        /**
         * Run this when the screen is held down
         * 
         * @param x The X coordinate, in pixels, of the touch
         * @param y The Y coordinate, in pixels, of the touch
         */
        void onMove(float x, float y) {
        }

        /**
         * Run this when the screen is released
         * 
         * @param x The X coordinate, in pixels, of the touch
         * @param y The Y coordinate, in pixels, of the touch
         */
        void onUp(float x, float y) {
        }
    }

    /**
     * Custom camera that can do parallax... taken directly from GDX tests
     */
    class ParallaxCamera extends OrthographicCamera {
        /**
         * This matrix helps us compute the view
         */
        private final Matrix4 parallaxView = new Matrix4();

        /**
         * This matrix helps us compute the camera.combined
         */
        private final Matrix4 parallaxCombined = new Matrix4();

        /**
         * A temporary vector for doing the calculations
         */
        private final Vector3 tmp = new Vector3();

        /**
         * Another temporary vector for doing the calculations
         */
        private final Vector3 tmp2 = new Vector3();

        /**
         * The constructor simply forwards to the OrthographicCamera constructor
         * 
         * @param viewportWidth Width of the camera
         * @param viewportHeight Height of the camera
         */
        ParallaxCamera(float viewportWidth, float viewportHeight) {
            super(viewportWidth, viewportHeight);
        }

        /**
         * This is how we calculate the position of a parallax camera
         */
        Matrix4 calculateParallaxMatrix(float parallaxX, float parallaxY) {
            update();
            tmp.set(position);
            tmp.x *= parallaxX;
            tmp.y *= parallaxY;

            parallaxView.setToLookAt(tmp, tmp2.set(tmp).add(direction), up);
            parallaxCombined.set(projection);
            Matrix4.mul(parallaxCombined.val, parallaxView.val);
            return parallaxCombined;
        }
    }

    /**
     * Construct a level. This is mostly using defaults, so the main work is in
     * camera setup
     * 
     * @param width The width of the level, in meters
     * @param height The height of the level, in meters
     */
    Level(int width, int height) {
        // clear any timers
        Timer.instance().clear();
        // save the singleton and camera bounds
        sCurrent = this;
        mCamBoundX = width;
        mCamBoundY = height;

        // warn on strange dimensions
        if (width < Lol.sGame.mConfig.getScreenWidth() / Physics.PIXEL_METER_RATIO)
            Gdx.app.log("Warning", "Your game width is less than 1/10 of the screen width");
        if (height < Lol.sGame.mConfig.getScreenHeight() / Physics.PIXEL_METER_RATIO)
            Gdx.app.log("Warning", "Your game height is less than 1/10 of the screen height");

        // set up the game camera, with 0,0 in the bottom left
        mGameCam = new OrthographicCamera(Lol.sGame.mConfig.getScreenWidth()
                / Physics.PIXEL_METER_RATIO, Lol.sGame.mConfig.getScreenHeight()
                / Physics.PIXEL_METER_RATIO);
        mGameCam.position.set(Lol.sGame.mConfig.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2,
                Lol.sGame.mConfig.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2, 0);
        mGameCam.zoom = 1;

        // set up the heads-up display camera
        int camWidth = Lol.sGame.mConfig.getScreenWidth();
        int camHeight = Lol.sGame.mConfig.getScreenHeight();
        mHudCam = new OrthographicCamera(camWidth, camHeight);
        mHudCam.position.set(camWidth / 2, camHeight / 2, 0);

        // the background camera is like the hudcam
        mBgCam = new ParallaxCamera(camWidth, camHeight);
        mBgCam.position.set(camWidth / 2, camHeight / 2, 0);
        mBgCam.zoom = 1;

        // set up the sprite sets
        for (int i = 0; i < 5; ++i)
            mSprites.add(new ArrayList<Renderable>());

        // set up the callback for finding out who in the physics world was
        // touched
        mTouchCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                // if the hit point is inside the fixture of the body we report
                // it
                if (fixture.testPoint(mTouchVec.x, mTouchVec.y)) {
                    PhysicsSprite hs = (PhysicsSprite)fixture.getBody().getUserData();
                    if (hs.mVisible) {
                        mHitSprite = hs;
                        return false;
                    }
                }
                return true;
            }
        };

        // When debug mode is on, print the frames per second
        if (Lol.sGame.mConfig.showDebugBoxes())
            Controls.addFPS(400, 15, Lol.sGame.mConfig.getDefaultFontFace(),
                    Lol.sGame.mConfig.getDefaultFontRed(), Lol.sGame.mConfig.getDefaultFontGreen(),
                    Lol.sGame.mConfig.getDefaultFontBlue(), 12);
    }

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
     * When a pre or pause scene is showing, this un-registers all touches
     */
    void suspendTouch() {
        mTouchActive = false;
        for (int i = 0; i < 4; ++i)
            mLastTouches[i] = true;
    }

    /**
     * This code is called every 1/45th of a second to update the game state and
     * re-draw the screen
     * 
     * @param delta The time since the last render
     */
    @Override
    public void render(float delta) {
        // Make sure the music is playing... Note that we start music before the
        // PreScene shows
        playMusic();

        // Handle pauses due to pre, pause, or post scenes... Note that these
        // handle their own screen touches... Note that postscene should come first.
        if (mPostScene != null && mPostScene.render(mSpriteBatch))
            return;
        if (mPreScene != null && mPreScene.render(mSpriteBatch))
            return;
        if (mPauseScene != null && mPauseScene.render(mSpriteBatch))
            return;

        // check for any scene touches that should generate new events to
        // process
        manageTouches();

        // handle accelerometer stuff... note that accelerometer is effectively
        // disabled during a popup... we could change that by moving this to the
        // top, but that's probably not going to produce logical behavior
        Level.sCurrent.mTilt.handleTilt();

        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it
        // seems like we should be using /delta/ instead of 1/45f
        mWorld.step(1 / 45f, 8, 3);

        // now handle any events that occurred on account of the world movement
        // or screen touches
        for (Action pe : mOneTimeEvents)
            pe.go();
        mOneTimeEvents.clear();

        // handle repeat events
        for (Action pe : mRepeatEvents)
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
        mBackground.renderLayers(mSpriteBatch);

        // Render the entities in order from z=-2 through z=2
        mSpriteBatch.setProjectionMatrix(mGameCam.combined);
        mSpriteBatch.begin();
        for (ArrayList<Renderable> a : mSprites)
            for (Renderable r : a)
                r.render(mSpriteBatch, delta);
        mSpriteBatch.end();

        // DEBUG: draw outlines of physics entities
        if (Lol.sGame.mConfig.showDebugBoxes())
            mDebugRender.render(mWorld, mGameCam.combined);

        // draw Controls
        mHudCam.update();
        mSpriteBatch.setProjectionMatrix(mHudCam.combined);
        mSpriteBatch.begin();
        for (HudEntity c : mControls)
            c.render(mSpriteBatch);
        mSpriteBatch.end();

        // DEBUG: render Controls' outlines
        if (Lol.sGame.mConfig.showDebugBoxes()) {
            mShapeRender.setProjectionMatrix(mHudCam.combined);
            mShapeRender.begin(ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            for (HudEntity pe : mControls)
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
     * If the camera is supposed to follow an entity, this code will handle
     * updating the camera position
     */
    private void adjustCamera() {
        if (mChaseEntity == null)
            return;
        // figure out the entity's position
        float x = mChaseEntity.mBody.getWorldCenter().x + mChaseEntity.mCameraOffset.x;
        float y = mChaseEntity.mBody.getWorldCenter().y + mChaseEntity.mCameraOffset.y;

        // if x or y is too close to MAX,MAX, stick with max acceptable values
        if (x > mCamBoundX - Lol.sGame.mConfig.getScreenWidth() * mGameCam.zoom
                / Physics.PIXEL_METER_RATIO / 2)
            x = mCamBoundX - Lol.sGame.mConfig.getScreenWidth() * mGameCam.zoom
            / Physics.PIXEL_METER_RATIO / 2;
        if (y > mCamBoundY - Lol.sGame.mConfig.getScreenHeight() * mGameCam.zoom
                / Physics.PIXEL_METER_RATIO / 2)
            y = mCamBoundY - Lol.sGame.mConfig.getScreenHeight() * mGameCam.zoom
            / Physics.PIXEL_METER_RATIO / 2;

        // if x or y is too close to 0,0, stick with minimum acceptable values
        //
        // NB: we do MAX before MIN, so that if we're zoomed out, we show extra
        // space at the top instead of the bottom
        if (x < Lol.sGame.mConfig.getScreenWidth() * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
            x = Lol.sGame.mConfig.getScreenWidth() * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;
        if (y < Lol.sGame.mConfig.getScreenHeight() * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
            y = Lol.sGame.mConfig.getScreenHeight() * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;

        // update the camera position
        mGameCam.position.set(x, y, 0);
    }

    /**
     * Add a renderable entity to the level, putting it into the appropriate z
     * plane
     * 
     * @param r The renderable entity
     * @param zIndex The z plane. valid values are -2, -1, 0, 1, and 2. 0 is the
     *            default.
     */
    void addSprite(Renderable r, int zIndex) {
        assert zIndex >= -2;
        assert zIndex <= 2;
        mSprites.get(zIndex + 2).add(r);
    }

    /**
     * Remove a renderable entity from its z plane
     * 
     * @param r The entity to remove
     * @param zIndex The z plane where it is expected to be
     */
    void removeSprite(Renderable r, int zIndex) {
        assert zIndex >= -2;
        assert zIndex <= 2;
        mSprites.get(zIndex + 2).remove(r);
    }

    /**
     * LOL uses polling to detect multitouch, touchdown, touchup, and
     * touchhold/touchdrag. This method, called from render, will track up to 4
     * simultaneous touches and forward them for proper processing. Note that
     * our choice of 4 points is totally arbitrary... we could do up to 10 quite
     * easily
     */
    private void manageTouches() {
        // poll for touches... we assume no more than 4 simultaneous touches
        boolean[] touchStates = new boolean[4];
        for (int i = 0; i < 4; ++i) {
            // we compare the current state to the prior state, to detect down,
            // up, or move/hold. Note that we don't distinguish between move and
            // hold
            touchStates[i] = Gdx.input.isTouched(i);
            float x = Gdx.input.getX(i);
            float y = Gdx.input.getY(i);
            // if there is a touch, call the appropriate method
            if (touchStates[i] && mLastTouches[i] && mTouchActive)
                touchMove((int)x, (int)y);
            else if (touchStates[i] && !mLastTouches[i]) {
                mTouchActive = true;
                touchDown((int)x, (int)y);
            } else if (!touchStates[i] && mLastTouches[i] && mTouchActive)
                touchUp((int)x, (int)y);
            mLastTouches[i] = touchStates[i];
        }
    }

    /**
     * When there is a down press, this code handles it
     * 
     * @param x The X location of the press, in screen coordinates
     * @param y The Y location of the press, in screen coordinates
     */
    private void touchDown(int x, int y) {
        // check for HUD touch first...
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (HudEntity pe : mControls) {
            if (pe.mIsTouchable && pe.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                // now convert the touch to world coordinates and pass to the
                // control (useful for vector throw)
                mGameCam.unproject(mTouchVec.set(x, y, 0));
                pe.onDownPress(mTouchVec);
                return;
            }
        }

        // check for sprite touch, by looking at gameCam coordinates... on
        // touch, hitSprite will change
        mHitSprite = null;
        mGameCam.unproject(mTouchVec.set(x, y, 0));
        mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f,
                mTouchVec.x + 0.1f, mTouchVec.y + 0.1f);
        if (mHitSprite != null)
            mHitSprite.handleTouchDown(x, y);
        // Handle level touches for which we've got a registered handler
        else if (mTouchResponder != null)
            mTouchResponder.onDown(mTouchVec.x, mTouchVec.y);
    }

    /**
     * When a finger moves on the screen, this code handles it
     * 
     * @param x The X location of the press, in screen coordinates
     * @param y The Y location of the press, in screen coordinates
     */
    private void touchMove(int x, int y) {
        // check for HUD touch first...
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (HudEntity pe : mControls) {
            if (pe.mIsTouchable && pe.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                // now convert the touch to world coordinates and pass to the
                // control (useful for vector throw)
                mGameCam.unproject(mTouchVec.set(x, y, 0));
                pe.onHold(mTouchVec);
                return;
            }
        }
        // check for screen touch, then for dragging an entity
        mGameCam.unproject(mTouchVec.set(x, y, 0));
        if (mTouchResponder != null)
            mTouchResponder.onMove(mTouchVec.x, mTouchVec.y);
        else if (mHitSprite != null)
            mHitSprite.handleTouchDrag(mTouchVec.x, mTouchVec.y);
    }

    /**
     * When a finger is removed from the screen, this code handles it
     * 
     * @param x The X location of the press, in screen coordinates
     * @param y The Y location of the press, in screen coordinates
     */
    void touchUp(int x, int y) {
        // check for HUD touch first
        mHudCam.unproject(mTouchVec.set(x, y, 0));
        for (HudEntity pe : mControls) {
            if (pe.mIsTouchable && pe.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                pe.onUpPress();
                return;
            }
        }

        // Up presses are not handled by entities, only by the screen
        mGameCam.unproject(mTouchVec.set(x, y, 0));
        if (mTouchResponder != null)
            mTouchResponder.onUp(mTouchVec.x, mTouchVec.y);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Create a new empty level, and configure its camera
     * 
     * @param width width of the camera
     * @param height height of the camera
     */
    public static void configure(int width, int height) {
        sCurrent = new Level(width, height);
    }

    /**
     * Identify the entity that the camera should try to keep on screen at all
     * times
     * 
     * @param ps The entity the camera should chase
     */
    public static void setCameraChase(PhysicsSprite ps) {
        sCurrent.mChaseEntity = ps;
    }

    /**
     * Set the background music for this level
     * 
     * @param musicName Name of the Music file to play
     */
    public static void setMusic(String musicName) {
        Music m = Media.getMusic(musicName);
        sCurrent.mMusic = m;
    }

    /**
     * Specify that you want some code to run after a fixed amount of time
     * passes.
     * 
     * @param timerId A (possibly) unique identifier for this timer
     * @param howLong How long to wait before the timer code runs
     */
    public static void setTimerTrigger(final int timerId, float howLong) {
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (!Level.sCurrent.mScore.mGameOver)
                    Lol.sGame.onTimerTrigger(timerId, Lol.sGame.mCurrLevelNum);
            }
        }, howLong);
    }

    /**
     * Specify that you want some code to run after a fixed amount of time
     * passes, and that it should return a specific enemy to the programmer's
     * code
     * 
     * @param timerId A (possibly) unique identifier for this timer
     * @param howLong How long to wait before the timer code runs
     * @param enemy The enemy that should be passed along
     */
    public static void setEnemyTimerTrigger(final int timerId, float howLong, final Enemy enemy) {
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (!Level.sCurrent.mScore.mGameOver)
                    Lol.sGame.onEnemyTimerTrigger(timerId, Lol.sGame.mCurrLevelNum, enemy);
            }
        }, howLong);
    }

    /**
     * Turn on scribble mode, so that scene touch events draw circular objects
     * Note: this code should be thought of as serving to demonstrate, only. If
     * you really wanted to do anything clever with scribbling, you'd certainly
     * want to change this code.
     * 
     * @param imgName The name of the image to use for scribbling
     * @param duration How long the scribble stays on screen before disappearing
     * @param width Width of the individual components of the scribble
     * @param height Height of the individual components of the scribble
     * @param density Density of each scribble component
     * @param elasticity Elasticity of the scribble
     * @param friction Friction of the scribble
     * @param moveable Can the individual items that are drawn move on account
     *            of collisions?
     * @param interval Time (in milliseconds) that must transpire between
     *            scribble events... use this to avoid outrageously high rates
     *            of scribbling
     */
    public static void setScribbleMode(final String imgName, final float duration, final float width,
            final float height, final float density, final float elasticity, final float friction,
            final boolean moveable, final int interval) {
        // we set a callback on the Level, so that any touch to the level (down,
        // drag, up) will affect our scribbling
        Level.sCurrent.mTouchResponder = new TouchAction() {
            /**
             * The time of the last touch event... we use this to prevent high
             * rates of scribble
             */
            long mLastTime;

            /**
             * On a down press, draw a new obstacle if enough time has
             * transpired
             * 
             * @param x The X coordinate of the touch
             * @param y The Y coordinate of the touch
             */
            @Override
            public void onDown(float x, float y) {
                // check if enough milliseconds have passed
                long now = System.nanoTime();
                if (now < mLastTime + interval * 1000000)
                    return;
                mLastTime = now;

                // make a circular obstacle
                final Obstacle o = Obstacle.makeAsCircle(x - width / 2, y - height / 2, width,
                        height, imgName);
                o.setPhysics(density, elasticity, friction);
                if (moveable)
                    o.mBody.setType(BodyType.DynamicBody);

                // possibly set a timer to remove the scribble
                if (duration > 0) {
                    Timer.schedule(new Task() {
                        @Override
                        public void run() {
                            o.remove(false);
                        }
                    }, duration);
                }
            }

            /**
             * On a move, do exactly the same as on down
             * 
             * @param x The X coordinate of the touch
             * @param y The Y coordinate of the touch
             */
            @Override
            public void onMove(float x, float y) {
                onDown(x, y);
            }
        };
    }
}
