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

using System;

using Microsoft.Xna.Framework;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    /**
     * Projectiles are entities that can be thrown from the hero's location in order
     * to remove enemies. Note that there is no public interface to this file.
     * Projectiles should be controlled via ProjectilePool.
     */
    public class Projectile : PhysicsSprite
    {
        /**
         * This is the initial point of the throw
         */
        internal Vector2 mRangeFrom = new Vector2();

        /**
         * We have to be careful in side-scrollers, or else projectiles can continue
         * traveling off-screen forever. This field lets us cap the distance away
         * from the hero that a projectile can travel before we make it disappear.
         */
        internal float mRange = 1000;

        /**
         * When projectiles collide, and they are not sensors, one will disappear.
         * We can keep both on screen by setting this true
         */
        internal bool mDisappearOnCollide = true;

        /**
         * How much damage does this projectile do?
         */
        internal int mStrength;

        /**
         * Internal method to create a projectile. Projectiles have an underlying
         * circle as their physics body
         * 
         * @param width width of the projectile
         * @param height height of the projectile
         * @param imgName Name of the image file to use for this projectile
         * @param x initial x position of the projectile
         * @param y initial y position of the projectile
         * @param zIndex The z plane of the projectile
         * @param isCircle True if it is a circle, false if it is a box
         */
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

        /**
         * Standard collision detection routine. This only triggers on hitting an
         * obstacle, which makes the projectile disappear, or on hitting a
         * projectile, which is a bit funny because one of the two projectiles will
         * live.
         * 
         * @param other The other entity involved in the collision
         * @param contact A description of the contact
         */
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

        /**
         * When drawing a projectile, we first check if it is too far from its
         * starting point
         */
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
