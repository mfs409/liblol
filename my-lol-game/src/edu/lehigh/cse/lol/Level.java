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

import java.util.ArrayList;

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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

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
     * When there is a gesture on the screen, we will convert the event's
     * coordinates to world coordinates, then use this to handle it. This object
     * can be attached to PhysicsSprites, Controls, or to the Level itself, to
     * specify a handler for certain events.
     */
    static class GestureAction {
        /**
         * We offer a HOLD/RELEASE gesture. This flag tells us if we're in a
         * hold event.
         */
        boolean mHolding;

        /**
         * Handle a drag event
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean onDrag(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a down press (hopefully to turn it into a hold/release)
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        public boolean onDown(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle an up press (hopefully to turn it into a release)
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        public boolean onUp(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a tap event
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean onTap(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a pan event
         * 
         * @param touchVec
         *            The x/y/z world coordinates of the touch
         * @param deltaX
         *            the change in X scale, in screen coordinates
         * @param deltaY
         *            the change in Y scale, in screen coordinates
         */
        boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
            return false;
        }

        /**
         * Handle a pan stop event
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean onPanStop(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a fling event
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean onFling(Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a toggle event. This is usually built from a down and an up.
         * 
         * @param touchVec
         *            The x/y/z coordinates of the touch
         */
        boolean toggle(boolean isUp, Vector3 touchVec) {
            return false;
        }

        /**
         * Handle a zoom event
         * 
         * @param initialDistance
         *            The distance between fingers when the pinch started
         * @param distance
         *            The current distance between fingers
         */
        boolean zoom(float initialDistance, float distance) {
            return false;
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
         * @param viewportWidth
         *            Width of the camera
         * @param viewportHeight
         *            Height of the camera
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
     * To properly handle gestures, we need to provide the code to run on each
     * type of gesture we care about.
     */
    class LolGestureManager extends GestureAdapter {
        /**
         * When the screen is tapped, this code forwards the tap to the
         * appropriate GestureAction
         * 
         * @param x
         *            X coordinate of the tap
         * @param y
         *            Y coordinate of the tap
         * @param count
         *            1 for single click, 2 for double-click
         * @param button
         *            The mouse button that was pressed
         */
        @Override
        public boolean tap(float x, float y, int count, int button) {
            // if any pop-up scene is showing, forward the tap to the scene and
            // return true, so that the event doesn't get passed to the Scene
            if (mPostScene != null && mPostScene.mVisible == true) {
                mPostScene.onTap();
                return true;
            } else if (mPreScene != null && mPreScene.mVisible == true) {
                mPreScene.onTap(x, y);
                return true;
            } else if (mPauseScene != null && mPauseScene.mVisible == true) {
                mPauseScene.onTap(x, y);
                return true;
            }

            // check if we tapped a control
            mHudCam.unproject(mTouchVec.set(x, y, 0));
            for (Controls.Control c : mTapControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(x, y, 0));
                    c.mGestureAction.onTap(mTouchVec);
                    return true;
                }
            }

            // check if we tapped an entity
            mHitSprite = null;
            mGameCam.unproject(mTouchVec.set(x, y, 0));
            mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                    mTouchVec.y + 0.1f);
            if (mHitSprite != null && mHitSprite.onTap(mTouchVec))
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
         * @param velocityX
         *            X velocity of the fling
         * @param velocityY
         *            Y velocity of the fling
         * @param button
         *            The mouse button that caused the fling
         */
        @Override
        public boolean fling(float velocityX, float velocityY, int button) {
            // we only fling at the whole-level layer
            mGameCam.unproject(mTouchVec.set(velocityX, velocityY, 0));
            for (GestureAction ga : Level.sCurrent.mGestureResponders) {
                if (ga.onFling(mTouchVec))
                    return true;
            }
            return false;
        }

        /**
         * Handle pan events
         * 
         * @param x
         *            X coordinate of current touch
         * @param y
         *            Y coordinate of current touch
         * @param deltaX
         *            change in X
         * @param deltaY
         *            change in Y
         */
        @Override
        public boolean pan(float x, float y, float deltaX, float deltaY) {
            // check if we panned a control
            mHudCam.unproject(mTouchVec.set(x, y, 0));
            for (Controls.Control c : mPanControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(x, y, 0));
                    c.mGestureAction.onPan(mTouchVec, deltaX, deltaY);
                    return true;
                }
            }

            // did we pan the level?
            mGameCam.unproject(mTouchVec.set(x, y, 0));
            for (GestureAction ga : Level.sCurrent.mGestureResponders) {
                if (ga.onPan(mTouchVec, deltaX, deltaY))
                    return true;
            }
            return false;
        }

        /**
         * Handle end-of-pan event
         * 
         * @param x
         *            X coordinate of the tap
         * @param y
         *            Y coordinate of the tap
         * @param pointer
         *            The finger that was used?
         * @param button
         *            The mouse button that was pressed
         */
        @Override
        public boolean panStop(float x, float y, int pointer, int button) {
            // check if we panStopped a control
            mHudCam.unproject(mTouchVec.set(x, y, 0));
            for (Controls.Control c : mPanControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(x, y, 0));
                    c.mGestureAction.onPanStop(mTouchVec);
                    return true;
                }
            }

            // handle panstop on level
            mGameCam.unproject(mTouchVec.set(x, y, 0));
            for (GestureAction ga : Level.sCurrent.mGestureResponders)
                if (ga.onPanStop(mTouchVec))
                    return true;
            return false;
        }

        @Override
        public boolean zoom(float initialDistance, float distance) {
            for (Controls.Control c : mZoomControls) {
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
     * button' sorts of things), so we need a low-level input adapter, too.
     */
    class LolInputManager extends InputAdapter {
        /**
         * Handle when a downward touch happens
         * 
         * @param screenX
         *            X coordinate of the tap
         * @param screenY
         *            Y coordinate of the tap
         * @param pointer
         *            The finger that was used?
         * @param button
         *            The mouse button that was pressed
         */
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {

            // check if we down-pressed a control
            mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
            for (Controls.Control c : mToggleControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.toggle(false, mTouchVec);
                    return true;
                }
            }

            // pass to pinch-zoom?
            for (Controls.Control c : mZoomControls) {
                if (c.mIsTouchable && c.mIsActive && c.mRange.contains(mTouchVec.x, mTouchVec.y)) {
                    mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                    c.mGestureAction.onDown(mTouchVec);
                    return true;
                }
            }

            // check for sprite touch, by looking at gameCam coordinates... on
            // touch, hitSprite will change
            mHitSprite = null;
            mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
            mWorld.QueryAABB(mTouchCallback, mTouchVec.x - 0.1f, mTouchVec.y - 0.1f, mTouchVec.x + 0.1f,
                    mTouchVec.y + 0.1f);

            // PhysicsSprites don't respond to DOWN... if it's a down on a
            // physicssprite, we are supposed to remember the most recently
            // touched physicssprite, and that's it
            if (mHitSprite != null)
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
         * @param screenX
         *            X coordinate of the tap
         * @param screenY
         *            Y coordinate of the tap
         * @param pointer
         *            The finger that was used?
         * @param button
         *            The mouse button that was pressed
         */
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            // check if we down-pressed a control
            mHudCam.unproject(mTouchVec.set(screenX, screenY, 0));
            for (Controls.Control c : mToggleControls) {
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
         * @param screenX
         *            X coordinate of the drag
         * @param screenY
         *            Y coordinate of the drag
         * @param pointer
         *            The finger that was used
         */
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            if (mHitSprite != null && mHitSprite.mGestureResponder != null) {
                mGameCam.unproject(mTouchVec.set(screenX, screenY, 0));
                return mHitSprite.mGestureResponder.onDrag(mTouchVec);
            }
            for (GestureAction ga : mGestureResponders)
                if (ga.onDrag(mTouchVec))
                    return true;
            return false;
        }
    }

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
    private final ArrayList<ArrayList<Util.Renderable>> mSprites = new ArrayList<ArrayList<Util.Renderable>>(5);

    /**
     * Input Controls
     */
    ArrayList<Controls.Control> mControls = new ArrayList<Controls.Control>();

    /**
     * Output Displays
     */
    ArrayList<Displays.Display> mDisplays = new ArrayList<Displays.Display>();

    /**
     * Controls that have a tap event
     */
    ArrayList<Controls.Control> mTapControls = new ArrayList<Controls.Control>();

    /**
     * Controls that have a pan event
     */
    ArrayList<Controls.Control> mPanControls = new ArrayList<Controls.Control>();

    /**
     * Controls that have a pinch zoom event
     */
    ArrayList<Controls.Control> mZoomControls = new ArrayList<Controls.Control>();

    /**
     * Toggle Controls
     */
    ArrayList<Controls.Control> mToggleControls = new ArrayList<Controls.Control>();

    /**
     * Events that get processed on the next render, then discarded
     */
    ArrayList<Util.Action> mOneTimeEvents = new ArrayList<Util.Action>();

    /**
     * When the level is won or lost, this is where we store the event that
     * needs to run
     */
    Util.Action mEndGameEvent;

    /**
     * Events that get processed on every render
     */
    ArrayList<Util.Action> mRepeatEvents = new ArrayList<Util.Action>();

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
    Actor mChaseEntity;

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
    Actor mHitSprite = null;

    /**
     * This callback is used to get a touched entity from the physics world
     */
    private QueryCallback mTouchCallback;

    /**
     * The LOL interface requires that game designers don't have to construct
     * Level manually. To make it work, we store the current Level here
     */
    static Level sCurrent;

    /**
     * Entities may need to set callbacks to run on a screen touch. If so, they
     * can use this.
     */
    ArrayList<GestureAction> mGestureResponders = new ArrayList<GestureAction>();

    /**
     * In levels with a projectile pool, the pool is accessed from here
     */
    ProjectilePool mProjectilePool;

    /**
     * Code to run when a level is won
     */
    SimpleCallback mWinCallback;

    /**
     * Code to run when a level is lost
     */
    SimpleCallback mLoseCallback;

    /**
     * Construct a level. This is mostly using defaults, so the main work is in
     * camera setup
     * 
     * @param width
     *            The width of the level, in meters
     * @param height
     *            The height of the level, in meters
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
        Facts.resetLevelFacts();

        // save the singleton and camera bounds
        sCurrent = this;
        mCamBoundX = width;
        mCamBoundY = height;

        // warn on strange dimensions
        if (width < Lol.sGame.mConfig.getScreenWidth() / Physics.PIXEL_METER_RATIO)
            Util.message("Warning", "Your game width is less than 1/10 of the screen width");
        if (height < Lol.sGame.mConfig.getScreenHeight() / Physics.PIXEL_METER_RATIO)
            Util.message("Warning", "Your game height is less than 1/10 of the screen height");

        // set up the game camera, with 0,0 in the bottom left
        mGameCam = new OrthographicCamera(Lol.sGame.mConfig.getScreenWidth() / Physics.PIXEL_METER_RATIO,
                Lol.sGame.mConfig.getScreenHeight() / Physics.PIXEL_METER_RATIO);
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
            mSprites.add(new ArrayList<Util.Renderable>());

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
                        mHitSprite = hs;
                        return false;
                    }
                }
                return true;
            }
        };

        // When debug mode is on, print the frames per second
        if (Lol.sGame.mConfig.showDebugBoxes())
            Displays.addFPS(800, 15, Lol.sGame.mConfig.getDefaultFontFace(), Lol.sGame.mConfig.getDefaultFontRed(),
                    Lol.sGame.mConfig.getDefaultFontGreen(), Lol.sGame.mConfig.getDefaultFontBlue(), 12);
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
        if (x > mCamBoundX - Lol.sGame.mConfig.getScreenWidth() * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
            x = mCamBoundX - Lol.sGame.mConfig.getScreenWidth() * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;
        if (y > mCamBoundY - Lol.sGame.mConfig.getScreenHeight() * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2)
            y = mCamBoundY - Lol.sGame.mConfig.getScreenHeight() * mGameCam.zoom / Physics.PIXEL_METER_RATIO / 2;

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
     * @param r
     *            The renderable entity
     * @param zIndex
     *            The z plane. valid values are -2, -1, 0, 1, and 2. 0 is the
     *            default.
     */
    void addSprite(Util.Renderable r, int zIndex) {
        assert zIndex >= -2;
        assert zIndex <= 2;
        mSprites.get(zIndex + 2).add(r);
    }

    /**
     * Remove a renderable entity from its z plane
     * 
     * @param r
     *            The entity to remove
     * @param zIndex
     *            The z plane where it is expected to be
     */
    void removeSprite(Util.Renderable r, int zIndex) {
        assert zIndex >= -2;
        assert zIndex <= 2;
        mSprites.get(zIndex + 2).remove(r);
    }

    /**
     * A hack for stopping events when a pause screen is opened
     * 
     * @param touchVec
     *            The location of the touch that interacted with the pause
     *            screen.
     */
    void liftAllButtons(Vector3 touchVec) {
        for (Controls.Control c : mToggleControls) {
            if (c.mIsActive && c.mIsTouchable) {
                c.mGestureAction.toggle(true, touchVec);
            }
        }
        for (GestureAction ga : mGestureResponders) {
            ga.onPanStop(mTouchVec);
            ga.onUp(mTouchVec);
        }
    }

    /*
     * SCREEN (SCREENADAPTER) OVERRIDES
     */

    /**
     * This code is called every 1/45th of a second to update the game state and
     * re-draw the screen
     * 
     * @param delta
     *            The time since the last render
     */
    @Override
    public void render(float delta) {
        // in debug mode, any click will report the coordinates of the click...
        // this is very useful when trying to adjust screen coordinates
        if (Lol.sGame.mConfig.showDebugBoxes()) {
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

        // Handle pauses due to pre, pause, or post scenes... Note that these
        // handle their own screen touches... Note that postscene should come
        // first.
        if (mPostScene != null && mPostScene.render(mSpriteBatch))
            return;
        if (mPreScene != null && mPreScene.render(mSpriteBatch))
            return;
        if (mPauseScene != null && mPauseScene.render(mSpriteBatch))
            return;

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
        for (Util.Action pe : mOneTimeEvents)
            pe.go();
        mOneTimeEvents.clear();

        // handle repeat events
        for (Util.Action pe : mRepeatEvents)
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
        for (ArrayList<Util.Renderable> a : mSprites)
            for (Util.Renderable r : a)
                r.render(mSpriteBatch, delta);
        mSpriteBatch.end();

        // DEBUG: draw outlines of physics entities
        if (Lol.sGame.mConfig.showDebugBoxes())
            mDebugRender.render(mWorld, mGameCam.combined);

        // draw Controls
        mHudCam.update();
        mSpriteBatch.setProjectionMatrix(mHudCam.combined);
        mSpriteBatch.begin();
        for (Controls.Control c : mControls)
            if (c.mIsActive)
                c.render(mSpriteBatch);
        for (Displays.Display d : mDisplays)
            d.render(mSpriteBatch);
        mSpriteBatch.end();

        // DEBUG: render Controls' outlines
        if (Lol.sGame.mConfig.showDebugBoxes()) {
            mShapeRender.setProjectionMatrix(mHudCam.combined);
            mShapeRender.begin(ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            for (Controls.Control pe : mControls)
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

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Create a new empty level, and configure its camera
     * 
     * @param width
     *            width of the camera
     * @param height
     *            height of the camera
     */
    public static void configure(int width, int height) {
        sCurrent = new Level(width, height);
    }

    /**
     * Identify the entity that the camera should try to keep on screen at all
     * times
     * 
     * @param ps
     *            The entity the camera should chase
     */
    public static void setCameraChase(Actor ps) {
        sCurrent.mChaseEntity = ps;
    }

    /**
     * Set the background music for this level
     * 
     * @param musicName
     *            Name of the Music file to play
     */
    public static void setMusic(String musicName) {
        Music m = Media.getMusic(musicName);
        sCurrent.mMusic = m;
    }

    /**
     * Specify that you want some code to run after a fixed amount of time
     * passes.
     * 
     * @param howLong
     *            How long to wait before the timer code runs
     * @param sc
     *            A SimpleCallback to run
     */
    public static void setTimerCallback(float howLong, final SimpleCallback sc) {
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (!Level.sCurrent.mScore.mGameOver)
                    sc.onEvent();
            }
        }, howLong);
    }

    /**
     * Specify that you want some code to run repeatedly
     * 
     * @param howLong
     *            How long to wait before the timer code runs for the first time
     * @param interval
     *            The time between subsequent executions of the code
     * @param sc
     *            A SimpleCallback to run
     */
    public static void setTimerCallback(float howLong, float interval, final SimpleCallback sc) {
        Timer.schedule(new Task() {
            @Override
            public void run() {
                if (!Level.sCurrent.mScore.mGameOver)
                    sc.onEvent();
            }
        }, howLong, interval);
    }

    /**
     * Turn on scribble mode, so that scene touch events draw circular objects
     * Note: this code should be thought of as serving to demonstrate, only. If
     * you really wanted to do anything clever with scribbling, you'd certainly
     * want to change this code.
     * 
     * @param imgName
     *            The name of the image to use for scribbling
     * @param duration
     *            How long the scribble stays on screen before disappearing
     * @param width
     *            Width of the individual components of the scribble
     * @param height
     *            Height of the individual components of the scribble
     * @param density
     *            Density of each scribble component
     * @param elasticity
     *            Elasticity of the scribble
     * @param friction
     *            Friction of the scribble
     * @param moveable
     *            Can the individual items that are drawn move on account of
     *            collisions?
     * @param interval
     *            Time (in milliseconds) that must transpire between scribble
     *            events... use this to avoid outrageously high rates of
     *            scribbling
     */
    public static void setScribbleMode(final String imgName, final float duration, final float width,
            final float height, final float density, final float elasticity, final float friction,
            final boolean moveable, final int interval) {
        // we set a callback on the Level, so that any touch to the level (down,
        // drag, up) will affect our scribbling
        Level.sCurrent.mGestureResponders.add(new GestureAction() {
            /**
             * The time of the last touch event... we use this to prevent high
             * rates of scribble
             */
            long mLastTime;

            /**
             * On a down press, draw a new obstacle if enough time has
             * transpired
             * 
             * @param x
             *            The X coordinate of the touch
             * @param y
             *            The Y coordinate of the touch
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
                return true;
            }
        });
    }

    /**
     * Manually set the zoom level of the game
     * 
     * @param zoom
     *            The amount of zoom (1 is no zoom, >1 zooms out)
     */
    public static void setZoom(float zoom) {
        Level.sCurrent.mGameCam.zoom = zoom;
        Level.sCurrent.mBgCam.zoom = zoom;
    }

    /**
     * Register a callback so that custom code will run when the level is won
     * 
     * @param sc
     *            The code to run
     */
    public static void setWinCallback(SimpleCallback sc) {
        Level.sCurrent.mWinCallback = sc;
    }

    /**
     * Register a callback so that custom code will run when the level is lost
     * 
     * @param sc
     *            The code to run
     */
    public static void setLoseCallback(SimpleCallback sc) {
        Level.sCurrent.mLoseCallback = sc;
    }
}
