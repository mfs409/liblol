using System;
using System.Collections.Generic;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Media;

using FarseerPhysics.Dynamics;

namespace LibLOL
{
    public class Level : GameScreen
    {
        private Song mMusic;

        private bool mMusicPlaying;

        internal Score mScore = new Score();

        // internal Tilt mTilt = new Tilt();

        internal World mWorld;

        internal Background mBackground;

        internal PreScene mPreScene;

        internal PostScene mPostScene;

        internal PauseScene mPauseScene;

        internal List<List<Renderable>> mSprites = new List<List<Renderable>>(5);

        internal List<Controls.HudEntity> mControls = new List<Controls.HudEntity>();

        internal List<Action> mOneTimeEvents = new List<Action>();

        internal Action mEndGameEvent;

        internal List<Action> mRepeatEvents = new List<Action>();

        internal OrthographicCamera mGameCam, mHudCam;

        internal ParallaxCamera mBgCam;

        private PhysicsSprite mChaseEntity;

        internal int mCamBoundX, mCamBoundY;

        private SpriteBatch mSpriteBatch;

        private Vector3 mTouchVec = new Vector3();

        private PhysicsSprite mHitSprite = null;

        private Func<Fixture, bool> mTouchCallback;

        private bool[] mLastTouches = new bool[4];

        private bool mTouchActive = true;

        internal static Level sCurrent;

        internal TouchAction mTouchResponder;

        internal ProjectilePool mProjectilePool;

        internal delegate void OnTouchDelegate(float x, float y);

        internal class TouchAction
        {
            private OnTouchDelegate onDown, onMove, onUp;
            public OnTouchDelegate OnDown
            {
                get { return onDown; }
            }
            public OnTouchDelegate OnMove
            {
                get { return onMove; }
            }
            public OnTouchDelegate OnUp
            {
                get { return onUp; }
            }

            internal TouchAction(OnTouchDelegate down, OnTouchDelegate move, OnTouchDelegate up)
            {
                onDown = (down != null ? down : (x, y) => {});
                onMove = (move != null ? move : (x, y) => {});
                onUp = (up != null ? up : (x, y) => {});
            }
        }

        internal class ParallaxCamera : OrthographicCamera
        {
            internal ParallaxCamera(float width, float height) : base(width, height)
            {

            }
        }

        internal Level(Game game) : base(game)
        {
            //sCurrent = this;
            mSpriteBatch = new SpriteBatch(game.GraphicsDevice);
            for (int i = 0; i < 5; ++i)
            {
                mSprites.Add(new List<Renderable>());
            }
        }

        internal void PlayMusic()
        {
            
        }

        internal void PauseMusic()
        {

        }

        internal void StopMusic()
        {

        }

        internal void SuspendTouch()
        {
            
        }

        public override void Update(GameTime gameTime)
        {
            mWorld.Step(1 / 60f);
            foreach (Action a in mOneTimeEvents) { a(); }
            mOneTimeEvents.Clear();

            foreach (Action a in mRepeatEvents) { a(); }

            if (mEndGameEvent != null) { mEndGameEvent(); }

            foreach (List<Renderable> l in mSprites)
            {
                foreach (Renderable r in l)
                {
                    r.Update(gameTime);
                }
            }

            base.Update(gameTime);
        }

        public override void Draw(GameTime gameTime)
        {
            mSpriteBatch.Begin();
            foreach (List<Renderable> l in mSprites)
            {
                foreach (Renderable r in l)
                {
                    r.Draw(mSpriteBatch, gameTime);
                }
            }
            mSpriteBatch.End();
            base.Draw(gameTime);
        }

        internal void AddSprite(Renderable r, int zIndex)
        {
            System.Diagnostics.Debug.Assert(zIndex >= -2);
            System.Diagnostics.Debug.Assert(zIndex <= 2);
            mSprites[zIndex + 2].Add(r);
        }

        public static void Configure(int x, int y)
        {
            sCurrent = new Level(Lol.sGame);
        }
    }
}
