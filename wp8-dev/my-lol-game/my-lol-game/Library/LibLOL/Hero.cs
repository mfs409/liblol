using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LibLOL
{
    public class Hero : PhysicsSprite
    {
        private int strength = 1;

        private float mInvicibilityRemaining;

        private Animation mInvincibleAnimation;

        private bool mCrawling;

        private Animation mCrawlingAnimation;

        private Animation mThrowAnimation;

        private float mThrowAnimateTotalLength;

        private float mThrowAnimationTimeRemaining;

        private bool mInAir;

        private Vector2 mJumpImpulses;

        private bool mAllowMultiJump;

        private bool mIsTouchJump;

        private Animation mJumpAnimation;

        private SoundEffect mJumpEffect;

        private Vector2 mTouchAndGo;

        private float mCurrentRotation;

        private Hero(float width, float height, String imgName) : base(imgName, width, height)
        {
            // Level.sCurrent.mScore.mHeroesCreated++;
        }

        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
        }

        public static Hero MakeAsBox(float x, float y, float width, float height, String imgName)
        {
            Hero h = new Hero(width, height, imgName);
            h.SetBoxPhysics(0, 0, 0, BodyType.Dynamic, false, x, y);
            // Add to Level.sCurrent.
            return h;
        }
    }
}
