using System;

using Microsoft.Xna.Framework;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    // Uncomment code then done
    public class Projectile : PhysicsSprite
    {
        internal Vector2 mRangeFrom = new Vector2();
 
        internal float mRange = 1000;

        internal bool mDisappearOnCollide = true;

        internal int mStrength;

        internal Projectile(float width, float height, string imgName, float x, float y, int zIndex,
            bool isCircle) : base(imgName, width, height)
        {
            if (isCircle)
            {
                float radius = Math.Max(width, height);
                SetCirclePhysics(0, 0, 0, BodyType.Dynamic, true, x, y, radius / 2);
            }
            else
            {
                SetBoxPhysics(0, 0, 0, BodyType.Dynamic, true, x, y);
            }
            mBody.GravityScale = 0;
            CollisionEffect = false;
            DisableRotation();
            Level.sCurrent.addSprite(this, zIndex);
        }

        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
            if (other is Obstacle)
            {
                Obstacle o = (Obstacle)other;
                if (o.mProjectileCollision != null)
                {
                    o.mProjectileCollision(this, contact);
                    return;
                }
            }
            if (other is Projectile)
            {
                if (!mDisappearOnCollide)
                {
                    return;
                }
            }
            if (other.mBody.FixtureList[0].IsSensor)
            {
                return;
            }
            Remove(false);
        }

        public override void Update(GameTime gameTime)
        {
            float dx = Math.Abs(mBody.Position.X - mRangeFrom.X);
            float dy = Math.Abs(mBody.Position.Y - mRangeFrom.Y);
            if (dx * dx + dy * dy > mRange * mRange)
            {
                Remove(true);
                mBody.Enabled = false;
                return;
            }
            base.Update(gameTime);
        }
    }
}
