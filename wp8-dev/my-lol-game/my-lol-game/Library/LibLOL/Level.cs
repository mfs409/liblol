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

        // internal Background mBackground = new Background();

        // internal PreScene mPreScene;

        // internal PostScene mPostScene = new PostScene();

        // internal PauseScene mPauseScene;

        internal List<Action> mOneTimeEvents = new List<Action>();

        internal Action mEndGameEvent;

        internal List<Action> mRepeatEvents = new List<Action>();


        // More stuff missing.

        private SpriteBatch mSpriteBatch;

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

        internal Level(Game game) : base(game)
        {
            mSpriteBatch = new SpriteBatch(game.GraphicsDevice);
        }
    }
}
