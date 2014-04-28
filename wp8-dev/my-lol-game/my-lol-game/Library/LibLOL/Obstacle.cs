using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LibLOL
{
    class Obstacle : PhysicsSprite
    {
        internal CollisionCallback mHeroCollision, mEnemyCollision,
            mProjectileCollision;

        internal bool mNoJumpReenable;

        private SoundEffect mCollideSound;

        private long mCollideSoundDelay;

        private long mLastCollideSoundTime;

        public bool ReSet
        {
            set
            {
                mNoJumpReenable = !value;
            }
        }

        protected Obstacle(float width, float height, String imgName) : base(imgName, width, height)
        {
        }

        internal void PlayCollideSound()
        {
            if (mCollideSound == null) { return; }
            long now = Lol.GlobalGameTime.ElapsedTicks;
            if (now < mLastCollideSoundTime + mCollideSoundDelay) { return; }
            mLastCollideSoundTime = now;
            mCollideSound.Play();
        }

        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
        }

        public static Obstacle MakeAsBox(float x, float y, float width, float height, String imgName)
        {
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetBoxPhysics(0, 0, 0, BodyType.Static, false, x, y);
            // Level.sCurrent.AddSprite(o, 0);
            return o;
        }

        public static Obstacle MakeAsPolygon(float x, float y, float width, float height,
            String imgName, params float[] verts)
        {
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetPolygonPhysics(0, 0, 0, BodyType.Static, false, x, y, verts);
            // Level.sCurrent.AddSprite(o, 0);
            return o;
        }

        public static Obstacle MakeAsCircle(float x, float y, float width, float height, String imgName)
        {
            float radius = Math.Max(width, height);
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetCirclePhysics(0, 0, 0, BodyType.Static, false, x, y, radius / 2);
            // Level.sCurrent.AddSprite(o, 0);
            return o;
        }

        public void SetDamp(float factor)
        {
            SetCollisionEffect(false);
            mHeroCollision = delegate(PhysicsSprite h, Contact c)
            {
                Vector2 v = h.mBody.LinearVelocity;
                v.X *= factor;
                v.Y *= factor;
                UpdateVelocity(v.X, v.Y);
            };
        }

        public void SetSpeedBoost(float boostAmountX, float boostAmountY, float duration)
        {

        }

        public void SetHeroCollisionTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4, float delay)
        {
            int[] counts = new int[] {
                activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4
            };
            SetCollisionEffect(false);

        }

        public void SetEnemyCollisionTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4, float delay)
        {
            int[] counts = new int[] {
                activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4
            };
        }

        public void SetProjectileCollisionTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4, float delay)
        {
            int[] counts = new int[] {
                activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4
            };

        }

        public void SetCollideSound(String sound, long delay)
        {

            mCollideSoundDelay = (long)((delay / 1000.0) * System.Diagnostics.Stopwatch.Frequency);
        }

    }
}
