using System;
using System.Collections.Generic;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Media;

using FarseerPhysics.Dynamics;

namespace LibLOL
{
    public class Level : DrawableGameComponent
    {
        private Song mMusic;

        private bool mMusicPlaying;

        internal Score mScore = new Score();

        // internal Tilt mTilt = new Tilt();

        internal World mWorld;

        internal Background mBackground = new Background();

        internal PreScene mPreScene;

        internal PostScene mPostScene = new PostScene();

        internal PauseScene mPauseScene;

        internal List<List<Renderable>> mSprites = new List<List<Renderable>>(5);

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

        }

        internal Level(Game game) : base(game)
        {
            mSpriteBatch = new SpriteBatch(game.GraphicsDevice);
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
    }
}
