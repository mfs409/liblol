using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    // Uncomment code and done.
    public class Obstacle : PhysicsSprite
    {
        internal CollisionCallback mHeroCollision, mEnemyCollision,
            mProjectileCollision;

        internal bool mNoJumpReenable;

        private SoundEffect mCollideSound;

        private long mCollideSoundDelay;

        private long mLastCollideSoundTime;

        protected Obstacle(float width, float height, string imgName) : base(imgName, width, height)
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

        public static Obstacle MakeAsBox(float x, float y, float width, float height, string imgName)
        {
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetBoxPhysics(0, 0, 0, BodyType.Static, false, x, y);
            Level.sCurrent.addSprite(o, 0);
            return o;
        }

        public static Obstacle MakeAsPolygon(float x, float y, float width, float height,
            string imgName, params float[] verts)
        {
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetPolygonPhysics(0, 0, 0, BodyType.Static, false, x, y, verts);
            Level.sCurrent.addSprite(o, 0);
            return o;
        }

        public static Obstacle MakeAsCircle(float x, float y, float width, float height, string imgName)
        {
            float radius = Math.Max(width, height);
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetCirclePhysics(0, 0, 0, BodyType.Static, false, x, y, radius / 2);
            Level.sCurrent.addSprite(o, 0);
            return o;
        }

        public float Damp
        {
            set
            {
                CollisionEffect = false;
                mHeroCollision = delegate(PhysicsSprite h, Contact c)
                {
                    Vector2 v = h.mBody.LinearVelocity;
                    v.X *= value;
                    v.Y *= value;
                    UpdateVelocity(v.X, v.Y);
                };
            }
        }

        public void SetSpeedBoost(float boostAmountX, float boostAmountY, float boostDuration)
        {
            CollisionEffect = false;
            mHeroCollision = delegate(PhysicsSprite h, Contact c)
            {
                Vector2 v = h.mBody.LinearVelocity;
                v.X += boostAmountX;
                v.Y += boostAmountY;
                h.UpdateVelocity(v.X, v.Y);
                if (boostDuration > 0)
                {
                    Action a = delegate()
                    {
                        Vector2 vv = h.mBody.LinearVelocity;
                        vv.X -= boostAmountX;
                        vv.Y -= boostAmountY;
                        h.UpdateVelocity(vv.X, vv.Y);
                    };
                    Timer.Schedule(a, boostDuration);
                }
            };
        }

        public bool ReJump
        {
            set
            {
                mNoJumpReenable = !value;
            }
        }

        public void SetHeroCollisionTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4, float delay)
        {
            int[] counts = new int[] {
                activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4
            };
            CollisionEffect = false;
            mHeroCollision = delegate(PhysicsSprite ps, Contact c)
            {
                if (c.Enabled)
                {
                    bool match = true;
                    for (int i = 0; i < 4; ++i)
                    {
                        match &= counts[i] <= Level.sCurrent.mScore.mGoodiesCollected[i];
                    }
                    if (match)
                    {
                        if (delay <= 0)
                        {
                            Lol.sGame.onHeroCollideTrigger(id, Lol.sGame.mCurrLevelNum, this, (Hero)ps);
                            return;
                        }
                        Timer.Schedule(() => { Lol.sGame.onHeroCollideTrigger(id, Lol.sGame.mCurrLevelNum, this, (Hero)ps); },
                            delay);
                    }
                }
            };
        }

        public void SetEnemyCollisionTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4, float delay)
        {
            int[] enemyTriggerActivation = new int[] {
                activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4
            };
            mEnemyCollision = delegate(PhysicsSprite ps, Contact c)
            {
                bool match = true;
                for (int i = 0; i < 4; ++i)
                {
                    match &= enemyTriggerActivation[i] <= Level.sCurrent.mScore.mGoodiesCollected[i];
                }
                if (match)
                {
                    if (delay <= 0)
                    {
                        Lol.sGame.onEnemyCollideTrigger(id, Lol.sGame.mCurrLevelNum, null, (Enemy)ps);
                        return;
                    }
                    Timer.Schedule(() => { Lol.sGame.onEnemyCollideTrigger(id, Lol.sGame.mCurrLevelNum, null, (Enemy)ps); },
                        delay);
                }
            };
        }

        public void SetProjectileCollisionTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4)
        {
            int[] projectileTriggerActivation = new int[] {
                activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4
            };
            mProjectileCollision = delegate(PhysicsSprite ps, Contact c)
            {
                bool match = true;
                for (int i = 0; i < 4; ++i)
                {
                    match &= projectileTriggerActivation[i] <= Level.sCurrent.mScore.mGoodiesCollected[i];
                }
                if (match)
                {
                    Lol.sGame.onProjectileCollideTrigger(id, Lol.sGame.mCurrLevelNum, this, (Projectile)ps);
                }
            };

        }

        public void SetCollideSound(String sound, long delay)
        {
            mCollideSoundDelay = delay / 1000;
            // Stopwatch issues (tls)
            //mCollideSoundDelay = (long)((delay / 1000.0) * System.Diagnostics.Stopwatch.Frequency);
        }

    }
}
