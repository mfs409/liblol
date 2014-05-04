using System;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LibLOL
{
    // Uncomment code to be done
    public class Enemy : PhysicsSprite
    {
        internal int mDamage = 2;

        internal string mOnDefeatHeroText = "";

        internal bool mDefeatByCrawl;

        internal bool mDefeatByJump;

        internal bool mImmuneToInvicibility;

        internal bool mAlwaysDoesDamage;

        private bool mDisappearOnTouch;

        private CollisionCallback mDefeatCallback;

        private Enemy(float width, float height, string imgName) : base(imgName, width, height)
        {
            Level.sCurrent.mScore.mEnemiesCreated++;
        }

        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
            if (other is Obstacle)
            {
                OnCollideWithObstacle((Obstacle)other, contact);
            }
            if (other is Projectile)
            {
                OnCollideWithProjectile((Projectile)other);
            }
        }

        private void OnCollideWithObstacle(Obstacle o, Contact c)
        {
            if (o.mEnemyCollision != null)
                o.mEnemyCollision(this, c);
        }

        private void OnCollideWithProjectile(Projectile p)
        {
            if (!p.mVisible) { return; }
            mDamage -= p.mStrength;
            if (mDamage <= 0)
            {
                p.Remove(true);
                Defeat(true);
            }
            else
            {
                p.Remove(false);
            }
          
        }

        internal override void HandleTouchDown(float x, float y)
        {
            if (mDisappearOnTouch)
            {
                // Lol.sGame.Vibrate(100);
                Defeat(true);
                return;
            }
            base.HandleTouchDown(x, y);
        }

        public static Enemy MakeAsBox(float x, float y, float width, float height, string imgName)
        {
            Enemy e = new Enemy(width, height, imgName);
            e.SetBoxPhysics(0, 0, 0, BodyType.Static, false, x, y);
            //Level.sCurrent.AddSprite(e, 0);
            return e;
        }

        public static Enemy MakeAsCircle(float x, float y, float width, float height, string imgName)
        {
            float radius = Math.Max(width, height);
            Enemy e = new Enemy(width, height, imgName);
            e.SetCirclePhysics(0, 0, 0, BodyType.Static, false, x, y, radius / 2);
            // Level.sCurrent.AddSprite(e, 0);
            return e;
        }

        /*public void SetDamage(int amount)
        {
            mDamage = amount;
        }*/

        public int Damage
        {
            set { mDamage = value; }
        }

        /*public void SetDefeatHeroText(string message)
        {
            mOnDefeatHeroText = message;
        }*/

        public string DefeatHeroText
        {
            set { mOnDefeatHeroText = value; }
        }

        public void Defeat(bool increaseScore)
        {
            Remove(false);

            if (increaseScore)
            {
                Level.sCurrent.mScore.OnDefeatEnemy();
            }

            if (mDefeatCallback != null)
            {
                mDefeatCallback(this, null);
            }
        }

        public void SetDefeatByCrawl()
        {
            mDefeatByCrawl = true;

            CollisionEffect = false;
        }

        public void SetResistInvicibility()
        {
            mImmuneToInvicibility = true;
        }

        public void SetImmuneToInvicibility()
        {
            mAlwaysDoesDamage = true;
        }

        public void SetDisappearOnTouch()
        {
            mDisappearOnTouch = true;
        }

        public void SetDefeatTrigger(int id)
        {
            mDefeatCallback = (ps, c) => { Lol.sGame.OnEnemyDefeatTrigger(id, Lol.sGame.mCurrLevelNum, this); };
        }

        public void ClearDefeatTrigger()
        {
            mDefeatCallback = null;
        }

        public void SetDefeatByJump()
        {
            mDefeatByJump = true;
        }
    }
}
