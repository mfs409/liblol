using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input.Touch;
using FarseerPhysics.Dynamics;
using System.Diagnostics;

namespace LOL
{
    public class Level: GameScreen
    {
        /**
         * The music, if any
         */
        private Music mMusic;

        public bool ShowArrows = true;

        //internal delegate void AABBDelegate(Fixture f, bool b);

        /**
         * Whether the music is playing or not
         */
        private bool mMusicPlaying;

        /**
         * A reference to the score object, for tracking winning and losing
         */
        public Score mScore = new Score();

        /**
         * A reference to the tilt object, for managing how tilts are handled
         */
        public Tilt mTilt = new Tilt();

        /**
         * The physics world in which all entities interact
         */
        public World mWorld;

        /**
         * The set of Parallax backgrounds
         */
        public Background mBackground = new Background();

        /**
         * The scene to show when the level is created (if any)
         */
        public PreScene mPreScene;

        /**
         * The scene to show when the level is won or lost
         */
        public PostScene mPostScene = new PostScene();

        /**
         * The scene to show when the level is paused (if any)
         */
        public PauseScene mPauseScene;

        /**
         * All the sprites, in 5 planes. We draw them as planes -2, -1, 0, 1, 2
         */
        private List<List<Renderable>> mSprites = new List<List<Renderable>>(
                5);

        /**
         * The controls / heads-up-display
         */
        public List<Controls.HudEntity> mControls = new List<Controls.HudEntity>();

        /**
         * Events that get processed on the next render, then discarded
         */
        public List<Action> mOneTimeEvents = new List<Action>();

        /**
         * When the level is won or lost, this is where we store the event that
         * needs to run
         */
        public Action mEndGameEvent;

        /**
         * Events that get processed on every render
         */
        public List<Action> mRepeatEvents = new List<Action>();

        /**
         * This camera is for drawing entities that exist in the physics world
         */
        public OrthographicCamera mGameCam;

        /**
         * This camera is for drawing controls that sit above the world
         */
        public OrthographicCamera mHudCam;

        /**
         * This camera is for drawing parallax backgrounds that go behind the world
         */
        public ParallaxCamera mBgCam;

        /**
         * This is the sprite that the camera chases
         */
        private PhysicsSprite mChaseEntity;

        /**
         * The maximum x value of the camera
         */
        public int mCamBoundX;

        /**
         * The maximum y value of the camera
         */
        public int mCamBoundY;

        /**
         * The debug renderer, for printing circles and boxes for each entity
         */
        private Box2DDebugRenderer mDebugRender = new Box2DDebugRenderer();

        /**
         * The spritebatch for drawing all texture regions and fonts
         */
        private SpriteBatch mSpriteBatch = new SpriteBatch(Lol.sGame.GraphicsDevice);

        /**
         * The debug shape renderer, for putting boxes around HUD entities
         */
        private ShapeRenderer mShapeRender = new ShapeRenderer();

        /**
         * We use this to avoid garbage collection when converting screen touches to
         * camera coordinates
         */
        private Vector3 mTouchVec = new Vector3();

        /**
         * When there is a touch of an entity in the physics world, this is how we
         * find it
         */
        private PhysicsSprite mHitSprite = null;

        /**
         * This callback is used to get a touched entity from the physics world
         */
        //private AABBDelegate mTouchCallback;

        /**
         * Our polling-based multitouch uses this array to track the previous state
         * of 4 fingers
         */
        private bool[] mLastTouches = new bool[4];

        /**
         * When transitioning between a pre-scene and the game, we need to be sure
         * that a down press on the pre-scene doesn't show up as a move event on the
         * game... we achieve it via this
         */
        private bool mTouchActive = true;

        /**
         * The LOL interface requires that game designers don't have to construct
         * Level manually. To make it work, we store the current Level here
         */
        public static Level sCurrent;

        /**
         * Entities may need to set callbacks to run on a screen touch. If so, they
         * can use this.
         */
        public TouchAction mTouchResponder;

        /**
         * In levels with a projectile pool, the pool is accessed from here
         */
        public ProjectilePool mProjectilePool;

        /**
         * Wrapper for actions that we generate and then want handled during the
         * render loop
         */
        public delegate void Action ();
        
        internal delegate void OnTouch(float x, float y);

        /**
         * Wrapper for handling code that needs to run in response to a screen touch
         */
        public class TouchAction {
            public TouchAction() { }
            public TouchAction(TouchDelegate onDown, TouchDelegate onMove, TouchDelegate onUp)
            {
                this.OnDown = onDown;
                this.OnMove = onMove;
                this.OnUp = onUp;
            }

            public delegate void TouchDelegate (float x, float y);
            /**
             * Run this when the screen is initially pressed down
             * 
             * @param x The X coordinate, in pixels, of the touch
             * @param y The Y coordinate, in pixels, of the touch
             */
            public TouchDelegate OnDown;

            /**
             * Run this when the screen is held down
             * 
             * @param x The X coordinate, in pixels, of the touch
             * @param y The Y coordinate, in pixels, of the touch
             */
            public TouchDelegate OnMove;

            /**
             * Run this when the screen is released
             * 
             * @param x The X coordinate, in pixels, of the touch
             * @param y The Y coordinate, in pixels, of the touch
             */
            public TouchDelegate OnUp;
        }

        /**
         * Custom camera that can do parallax... taken directly from GDX tests
         */
        // NOTE: UNCOMMENT
        public class ParallaxCamera: OrthographicCamera {
            /**
             * This matrix helps us compute the view
             */
            private Matrix parallaxView = new Matrix(); // NOTE: Matrix4

            /**
             * This matrix helps us compute the camera.combined
             */
            private Matrix parallaxCombined = new Matrix();

            /**
             * A temporary vector for doing the calculations
             */
            private Vector3 tmp = new Vector3();

            /**
             * Another temporary vector for doing the calculations
             */
            private Vector3 tmp2 = new Vector3();

            /**
             * The constructor simply forwards to the OrthographicCamera constructor
             * 
             * @param viewportWidth Width of the camera
             * @param viewportHeight Height of the camera
             */
            public ParallaxCamera(float viewportWidth, float viewportHeight): base(viewportWidth, viewportHeight) {
                
            }

            /**
             * This is how we calculate the position of a parallax camera
             */
            public Matrix calculateParallaxMatrix(float parallaxX, float parallaxY) {
                update();
                tmp = position;
                tmp.X *= parallaxX;
                tmp.Y *= parallaxY;

                // TODO: Figure out what the equivalent of OrthographicCamera is in XNA
                // TODO: setToLookAt does not exist in XNA Matrix class
                tmp2 = tmp + direction;
                //parallaxView.setToLookAt(tmp, tmp2, up);
                parallaxCombined = projection;
                parallaxCombined *= parallaxView;
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
        public Level(int width, int height) {
            // clear any timers
            Timer.Instance.Clear();
            // save the singleton and camera bounds
            sCurrent = this;
            mCamBoundX = width;
            mCamBoundY = height;

            // warn on strange dimensions
            if (width < Lol.sGame.mConfig.getScreenWidth() / Physics.PIXEL_METER_RATIO)
                Util.log("Warning", "Your game width is less than 1/10 of the screen width");
            if (height < Lol.sGame.mConfig.getScreenHeight() / Physics.PIXEL_METER_RATIO)
                Util.log("Warning", "Your game height is less than 1/10 of the screen height");

            // set up the game camera, with 0,0 in the bottom left
            mGameCam = new OrthographicCamera(Lol.sGame.mConfig.getScreenWidth()
                    / Physics.PIXEL_METER_RATIO, Lol.sGame.mConfig.getScreenHeight()
                    / Physics.PIXEL_METER_RATIO);
            mGameCam.position = new Vector3(Lol.sGame.mConfig.getScreenWidth() / Physics.PIXEL_METER_RATIO / 2,
                    Lol.sGame.mConfig.getScreenHeight() / Physics.PIXEL_METER_RATIO / 2, 0);
            mGameCam.zoom = 1;

            // set up the heads-up display camera
            int camWidth = Lol.sGame.mConfig.getScreenWidth();
            int camHeight = Lol.sGame.mConfig.getScreenHeight();
            mHudCam = new OrthographicCamera(camWidth, camHeight);
            mHudCam.position = new Vector3(camWidth / 2, camHeight / 2, 0);

            // the background camera is like the hudcam
            mBgCam = new ParallaxCamera(camWidth, camHeight);
            mBgCam.position = new Vector3(camWidth / 2, camHeight / 2, 0);
            mBgCam.zoom = 1;

            // set up the sprite sets
            for (int i = 0; i < 5; ++i)
                mSprites.Add(new List<Renderable>());

            // set up the callback for finding out who in the physics world was
            // touched
            
            
            // When debug mode is on, print the frames per second
            if (Lol.sGame.mConfig.showDebugBoxes())
                Controls.addFPS(400, 15, Lol.sGame.mConfig.getDefaultFontFace(),
                        Lol.sGame.mConfig.getDefaultFontRed(), Lol.sGame.mConfig.getDefaultFontGreen(),
                        Lol.sGame.mConfig.getDefaultFontBlue(), 12);
        }

        internal bool mTouchCallback (Fixture fixture) {
                    // if the hit point is inside the fixture of the body we report
                    // it
                    Vector2 touch = new Vector2(mTouchVec.X, mTouchVec.Y);
                    if (fixture.TestPoint(ref touch)) {
                        PhysicsSprite hs = (PhysicsSprite)fixture.Body.UserData;
                        if (hs.mVisible) {
                            mHitSprite = hs;
                            return false;
                        }
                    }
                    return true;
                }

        public int dx(float x)
        {
            return (int)((x / mCamBoundX) * Lol.sGame.GraphicsDevice.DisplayMode.Height);
            //return (int)((x / mCamBoundX) * Lol.sGame.mConfig.getScreenWidth());
        }

        public int dy(float y)
        {
            return (int)((y / mCamBoundY) * Lol.sGame.GraphicsDevice.DisplayMode.Width);
            //return (int)((y / mCamBoundY) * Lol.sGame.mConfig.getScreenHeight());
        }

        public float lx(float x)
        {
            return ((x / Lol.sGame.GraphicsDevice.DisplayMode.Height) * mCamBoundX);
        }

        public float ly(float y)
        {
            return ((y / Lol.sGame.GraphicsDevice.DisplayMode.Width) * mCamBoundY);
        }

        /**
         * If the level has music attached to it, this starts playing it
         */
        public void playMusic() {
            if (!mMusicPlaying && mMusic != null) {
                mMusicPlaying = true;
                mMusic.play();
            }
        }

        /**
         * If the level has music attached to it, this pauses it
         */
        public void pauseMusic() {
            if (mMusicPlaying) {
                mMusicPlaying = false;
                mMusic.pause();
            }
        }

        /**
         * If the level has music attached to it, this stops it
         */
        public void stopMusic() {
            if (mMusicPlaying) {
                mMusicPlaying = false;
                mMusic.stop();
            }
        }

        /**
         * When a pre or pause scene is showing, this un-registers all touches
         */
        public void suspendTouch() {
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
        public override void Draw (GameTime gameTime) {
            // Make sure the music is playing... Note that we start music before the
            // PreScene shows
            //Lol.sGame.GraphicsDevice.Clear(Color.Green);
            //return;
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
            mWorld.Step(1/45f);

            // now handle any events that occurred on account of the world movement
            // or screen touches
            foreach (Action pe in mOneTimeEvents)
                pe();
            mOneTimeEvents.Clear();

            // handle repeat events
            foreach (Action pe in mRepeatEvents)
                pe();

            // check for end of game
            if (mEndGameEvent != null)
                mEndGameEvent();

            // The world is now static for this time step... we can display it!

            // clear the screen
            Lol.sGame.GraphicsDevice.Clear(mBackground.mColor);
            
            // prepare the main camera... we do it here, so that the parallax code
            // knows where to draw...
            adjustCamera();
            mGameCam.update();

            // draw parallax backgrounds
            mBackground.renderLayers(mSpriteBatch);

            // Render the entities in order from z=-2 through z=2
            // TODO: Figure out how to draw projection matrix for SpriteBatch
            // NOTE: UNCOMMENT
            //mSpriteBatch.setProjectionMatrix(mGameCam.combined);
            mSpriteBatch.Begin();
            foreach (List<Renderable> a in mSprites)
                foreach (Renderable r in a)
                {
                    r.Update(gameTime);
                    r.Draw(mSpriteBatch, gameTime);
                }
            mSpriteBatch.End();

            // DEBUG: draw outlines of physics entities
            // NOTE: UNCOMMENT
            //if (Lol.sGame.mConfig.showDebugBoxes())
            //    mDebugRender.render(mWorld, mGameCam.combined);

            // draw Controls
            mHudCam.update();
            // NOTE: UNCOMMENT
            //mSpriteBatch.setProjectionMatrix(mHudCam.combined);
            mSpriteBatch.Begin();
            foreach (Controls.HudEntity c in mControls)
                c.render(mSpriteBatch);
            mSpriteBatch.End();

            // DEBUG: render Controls' outlines
            // NOTE: UNCOMMENT
            if (Lol.sGame.mConfig.showDebugBoxes()) {
                //mShapeRender.setProjectionMatrix(mHudCam.combined);
                mShapeRender.begin();
                mShapeRender.Color = Color.Red;
                foreach (Controls.HudEntity pe in mControls)
                    if (pe.mRange != null)
                        mShapeRender.rect(pe.mRange.X, pe.mRange.Y, pe.mRange.Width, pe.mRange.Height);
                mShapeRender.end();
            }

            // Draw arrows
            if (ShowArrows)
            {
                mSpriteBatch.Begin();
                int w = Lol.sGame.mConfig.getScreenWidth(),
                    h = Lol.sGame.mConfig.getScreenHeight();
                Texture2D LeftBtn = Media.getImage("leftarrow")[0],
                    RightBtn = Media.getImage("rightarrow")[0],
                    UpBtn = Media.getImage("leftarrow")[0],
                    DownBtn= Media.getImage("rightarrow")[0];
                int btnWidth = 64, btnHeight = 64;
                mSpriteBatch.Draw(LeftBtn, new Rectangle(0, (h-btnHeight)/2, btnWidth, btnHeight), Color.White);
                mSpriteBatch.Draw(RightBtn, new Rectangle(w - btnWidth, (h - btnHeight) / 2, btnWidth, btnHeight), Color.White);
                mSpriteBatch.Draw(UpBtn, new Rectangle((w - btnWidth) / 2, 0, btnWidth, btnHeight), Color.White);
                mSpriteBatch.Draw(DownBtn, new Rectangle((w - btnWidth) / 2, h - btnHeight, btnWidth, btnHeight), Color.White);
                mSpriteBatch.End();
            }
        }

        /**
         * Whenever we hide the level, be sure to turn off the music
         */
        public void hide() {
            pauseMusic();
        }

        /**
         * Whenever we dispose of the level, be sure to turn off the music
         */
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
            float x = mChaseEntity.mBody.WorldCenter.X + mChaseEntity.mCameraOffset.X;
            float y = mChaseEntity.mBody.WorldCenter.Y + mChaseEntity.mCameraOffset.Y;

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
            mGameCam.position = new Vector3(x, y, 0);
        }

        /**
         * Add a renderable entity to the level, putting it into the appropriate z
         * plane
         * 
         * @param r The renderable entity
         * @param zIndex The z plane. valid values are -2, -1, 0, 1, and 2. 0 is the
         *            default.
         */
        public void addSprite(Renderable r, int zIndex) {
            Debug.Assert(zIndex >= -2);
            Debug.Assert(zIndex <= 2);
            mSprites[zIndex + 2].Add(r);
        }

        /**
         * Remove a renderable entity from its z plane
         * 
         * @param r The entity to remove
         * @param zIndex The z plane where it is expected to be
         */
        public void removeSprite(Renderable r, int zIndex) {
            Debug.Assert(zIndex >= -2);
            Debug.Assert(zIndex <= 2);
            mSprites[zIndex + 2].Remove(r);
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
            TouchCollection tc = TouchPanel.GetState();
                
            for (int i = 0; i < tc.Count; ++i) {
                // we compare the current state to the prior state, to detect down,
                // up, or move/hold. Note that we don't distinguish between move and
                // hold
                // TODO: Evaluate touch state information for this function
                
                float x = tc[i].Position.X,
                      y = tc[i].Position.Y;
                
                // if there is a touch, call the appropriate method
                if (mTouchActive && tc[i].State == TouchLocationState.Moved)
                    touchMove((int)x, (int)y);
                else if (tc[i].State == TouchLocationState.Pressed) {
                    mTouchActive = true;
                    touchDown((int)x, (int)y);
                }
                else if (mTouchActive && tc[i].State == TouchLocationState.Released)
                    touchUp((int)x, (int)y);
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
            mTouchVec = new Vector3(x,y,0);
            mHudCam.unproject(mTouchVec);
            foreach (Controls.HudEntity pe in mControls) {
                if (pe.mIsTouchable && pe.mRange.Contains((int) mTouchVec.X, (int) mTouchVec.Y)) {
                    // now convert the touch to world coordinates and pass to the
                    // control (useful for vector throw)
                    mTouchVec = new Vector3(x,y,0);
                    mGameCam.unproject(mTouchVec);
                    pe.OnDownPress(mTouchVec);
                    return;
                }
            }

            // check for sprite touch, by looking at gameCam coordinates... on
            // touch, hitSprite will change
            mHitSprite = null;
            mTouchVec = new Vector3(x,y,0);
            mGameCam.unproject(mTouchVec);
            // NOTE: UNCOMMENT
            Vector2 minTouch = new Vector2(mTouchVec.X, mTouchVec.Y), maxTouch = new Vector2(mTouchVec.X, mTouchVec.Y);
            minTouch.X -= 0.1f;
            minTouch.Y -= 0.1f;
            maxTouch.X += 0.1f;
            maxTouch.Y += 0.1f;

            FarseerPhysics.Collision.AABB aabb = new FarseerPhysics.Collision.AABB(minTouch, maxTouch);
            mWorld.QueryAABB(mTouchCallback, ref aabb);
            if (mHitSprite != null)
                mHitSprite.HandleTouchDown(x, y);
            // Handle level touches for which we've got a registered handler
            else if (mTouchResponder != null)
                mTouchResponder.OnDown(mTouchVec.X, mTouchVec.Y);
        }

        /**
         * When a finger moves on the screen, this code handles it
         * 
         * @param x The X location of the press, in screen coordinates
         * @param y The Y location of the press, in screen coordinates
         */
        private void touchMove(int x, int y) {
            // check for HUD touch first...
            mTouchVec = new Vector3(x, y, 0);
            mHudCam.unproject(mTouchVec);
            foreach (Controls.HudEntity pe in mControls) {
                if (pe.mIsTouchable && pe.OnHold != null && pe.mRange.Contains((int)mTouchVec.X, (int)mTouchVec.Y))
                {
                    // now convert the touch to world coordinates and pass to the
                    // control (useful for vector throw)
                    mTouchVec = new Vector3(x, y, 0);
                    mGameCam.unproject(mTouchVec);
                    pe.OnHold(mTouchVec);
                    return;
                }
            }
            // check for screen touch, then for dragging an entity
            mTouchVec = new Vector3(x, y, 0);
            mGameCam.unproject(mTouchVec);
            if (mTouchResponder != null)
                mTouchResponder.OnMove(mTouchVec.X, mTouchVec.Y);
            else if (mHitSprite != null)
                mHitSprite.HandleTouchDrag(mTouchVec.X, mTouchVec.Y);
        }

        /**
         * When a finger is removed from the screen, this code handles it
         * 
         * @param x The X location of the press, in screen coordinates
         * @param y The Y location of the press, in screen coordinates
         */
        public void touchUp(int x, int y) {
            // check for HUD touch first
            mTouchVec = new Vector3(x, y, 0);
            mHudCam.unproject(mTouchVec);
            foreach (Controls.HudEntity pe in mControls) {
                if (pe.mIsTouchable && pe.OnUpPress != null && pe.mRange.Contains((int)mTouchVec.X, (int)mTouchVec.Y))
                {
                    pe.OnUpPress();
                    return;
                }
            }

            // Up presses are not handled by entities, only by the screen
            mTouchVec = new Vector3(x, y, 0);
            mGameCam.unproject(mTouchVec);
            if (mTouchResponder != null && mTouchResponder.OnUp != null)
                mTouchResponder.OnUp(mTouchVec.X, mTouchVec.Y);
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
        public static void setTimerTrigger(int timerId, float howLong) {
            Timer.Schedule(delegate() {
                if (!Level.sCurrent.mScore.mGameOver)
                    Lol.sGame.onTimerTrigger(timerId, Lol.sGame.mCurrLevelNum);
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
        public static void setEnemyTimerTrigger(int timerId, float howLong, Enemy enemy) {
            Timer.Schedule(delegate() {
                if (!Level.sCurrent.mScore.mGameOver)
                    Lol.sGame.onEnemyTimerTrigger(timerId, Lol.sGame.mCurrLevelNum, enemy);
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
        public static void setScribbleMode(String imgName, float duration,
                float width, float height, float density, float elasticity,
                float friction, bool moveable, int interval) {
            // we set a callback on the Level, so that any touch to the level (down,
            // drag, up) will affect our scribbling
            Level.sCurrent.mTouchResponder = new TouchAction();
            
                /**
                 * The time of the last touch event... we use this to prevent high
                 * rates of scribble
                 */
                DateTime mLastTime = DateTime.Now;

                /**
                 * On a down press, draw a new obstacle if enough time has
                 * transpired
                 * 
                 * @param x The X coordinate of the touch
                 * @param y The Y coordinate of the touch
                 */
                Level.sCurrent.mTouchResponder.OnDown = delegate(float x, float y) {
                    // check if enough milliseconds have passed
                    DateTime delayed = mLastTime.AddMilliseconds(interval);
                    if (DateTime.Now < delayed)
                        return;
                    mLastTime = DateTime.Now;

                    // make a circular obstacle
                    Obstacle o = Obstacle.MakeAsCircle(x - width / 2, y - height / 2, width,
                            height, imgName);
                    o.SetPhysics(density, elasticity, friction);
                    if (moveable)
                        o.mBody.BodyType = BodyType.Dynamic;

                    // possibly set a timer to remove the scribble
                    if (duration > 0) {
                        Timer.Schedule(delegate() {
                            o.Remove(false);
                        }, duration);
                    }
                };

                /**
                 * On a move, do exactly the same as on down
                 * 
                 * @param x The X coordinate of the touch
                 * @param y The Y coordinate of the touch
                 */
                Level.sCurrent.mTouchResponder.OnMove = delegate(float x, float y) {
                    Level.sCurrent.mTouchResponder.OnDown(x, y);
                };
        }

    }
}
