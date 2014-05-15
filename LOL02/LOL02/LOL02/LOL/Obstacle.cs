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
using Microsoft.Xna.Framework.Audio;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    /**
     * Obstacles are usually walls, except they can move, and can be used to run all
     * sorts of abritrary code that changes the game, or the behavior of the things
     * that collide with them
     */
    public class Obstacle : PhysicsSprite
    {
        /**
         * One of the main uses of obstacles is to use hero/obstacle collisions as a
         * way to run custom code. These callbacks define what code to run when a
         * hero, enemy, or projectile collides with this obstacle.
         */
        internal CollisionCallback mHeroCollision, mEnemyCollision,
            mProjectileCollision;

        /**
         * Indicate that this obstacle does not re-enable jumping for the hero
         */
        internal bool mNoJumpReenable;

        /**
         * a sound to play when the obstacle is hit by a hero
         */
        private SoundEffect mCollideSound;

        /**
         * how long to delay (in nanoseconds) between attempts to play the collide
         * sound
         */
        private long mCollideSoundDelay;

        /**
         * Time of last collision sound
         */
        private DateTime mLastCollideSoundTime;

        /**
         * Internal constructor to build an Obstacle. This should never be invoked
         * directly. Instead, use the 'addXXX' methods of the Object class.
         * 
         * @param width width of this Obstacle
         * @param height height of this Obstacle
         * @param imgName Name of the image file to use
         */
        protected Obstacle(float width, float height, string imgName) : base(imgName, width, height)
        {
        }

        /**
         * Internal method for playing a sound when a hero collides with this
         * obstacle
         */
        internal void PlayCollideSound()
        {
            if (mCollideSound == null) { return; }
            if (mLastCollideSoundTime != null && DateTime.Now < mLastCollideSoundTime.AddMilliseconds(mCollideSoundDelay)) { return; }
            mLastCollideSoundTime = DateTime.Now;
            mCollideSound.Play();
        }

        /**
         * Called when this Obstacle is the dominant obstacle in a collision Note:
         * This Obstacle is /never/ the dominant obstacle in a collision, since it
         * is #6 or #7
         * 
         * @param other The other entity involved in this collision
         * @param contact A description of the collision
         */
        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
            
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Draw an obstacle with an underlying box shape
         * 
         * @param x X coordinate of the bottom left corner
         * @param y Y coordinate of the bottom left corner
         * @param width Width of the obstacle
         * @param height Height of the obstacle
         * @param imgName Name of image file to use
         * @return The obstacle, so that it can be further modified
         */
        public static Obstacle MakeAsBox(float x, float y, float width, float height, string imgName)
        {
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetBoxPhysics(0, 0, 0, BodyType.Static, false, x, y);
            Level.sCurrent.addSprite(o, 0);
            return o;
        }

        /**
         * Draw an obstacle with an underlying polygon shape
         * 
         * @param x X coordinate of the bottom left corner
         * @param y Y coordinate of the bottom left corner
         * @param width Width of the obstacle
         * @param height Height of the obstacle
         * @param imgName Name of image file to use
         * @param vertices Up to 16 coordinates representing the vertexes of this
         *            polygon, listed as x0,y0,x1,y1,x2,y2,...
         * @return The obstacle, so that it can be further modified
         */
        public static Obstacle MakeAsPolygon(float x, float y, float width, float height,
            string imgName, params float[] verts)
        {
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetPolygonPhysics(0, 0, 0, BodyType.Static, false, x, y, verts);
            Level.sCurrent.addSprite(o, 0);
            return o;
        }

        /**
         * Draw an obstacle with an underlying circle shape
         * 
         * @param x X coordinate of the bottom left corner
         * @param y Y coordinate of the bottom left corner
         * @param width Width of the obstacle
         * @param height Height of the obstacle
         * @param imgName Name of image file to use
         * @return The obstacle, so that it can be further modified
         */
        public static Obstacle MakeAsCircle(float x, float y, float width, float height, string imgName)
        {
            float radius = Math.Max(width, height);
            Obstacle o = new Obstacle(width, height, imgName);
            o.SetCirclePhysics(0, 0, 0, BodyType.Static, false, x, y, radius / 2);
            Level.sCurrent.addSprite(o, 0);
            return o;
        }

        /**
         * Call this on an Obstacle to give it a dampening factor. A hero can glide
         * over damp Obstacles. Damp factors can be negative to cause a reverse
         * direction, less than 1 to cause a slowdown (friction pads), or greater
         * than 1 to serve as zoom pads.
         */
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
                    h.UpdateVelocity(v.X, v.Y);
                };
            }
        }

        /**
         * Call this on an event to make it behave like a "damp" obstacle, except
         * with a constant additive (or subtractive) effect on the hero's speed.
         * 
         * @param boostAmountX The amount to add to the hero's X velocity
         * @param boostAmountY The amount to add to the hero's Y velocity
         * @param boostDuration How long should the speed boost last (use -1 to
         *            indicate "forever")
         */
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

        /**
         * Control whether the hero can jump if it collides with this obstacle while
         * in the air
         * 
         * @note set true if the hero can jump again, false otherwise
         */
        public bool ReJump
        {
            set
            {
                mNoJumpReenable = !value;
            }
        }

        /**
         * Make the object a trigger object, so that custom code will run when a
         * hero collides with it
         * 
         * @param id identifier for the trigger
         * @param activationGoodies1 Number of type-1 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies2 Number of type-2 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies3 Number of type-3 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies4 Number of type-4 goodies that must be collected
         *            before this trigger works
         * @param delay The time between when the collision happens, and when the
         *            trigger code runs. Use 0 for immediately
         */
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

        /**
         * Make the object a trigger object, so that custom code will run when an
         * enemy collides with it
         * 
         * @param id identifier for the trigger
         * @param activationGoodies1 Number of type-1 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies2 Number of type-2 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies3 Number of type-3 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies4 Number of type-4 goodies that must be collected
         *            before this trigger works
         * @param delay The time between when the collision happens, and when the
         *            trigger code runs. Use 0 for immediately
         */
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

        /**
         * Make the object a trigger object, so that custom code will run when a
         * projectile collides with it.
         * 
         * @param id identifier for the trigger
         * @param activationGoodies1 Number of type-1 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies2 Number of type-2 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies3 Number of type-3 goodies that must be collected
         *            before this trigger works
         * @param activationGoodies4 Number of type-4 goodies that must be collected
         *            before this trigger works
         */
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

        /**
         * Indicate that when the hero collides with this obstacle, we should make a
         * sound
         * 
         * @param sound The name of the sound file to play
         * @param delay How long to wait before playing the sound again, in
         *            milliseconds
         */
        public void SetCollideSound(String sound, long delay)
        {
            mCollideSoundDelay = delay / 1000;
        }

    }
}
