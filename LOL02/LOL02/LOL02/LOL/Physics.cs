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

using FarseerPhysics.Collision;
using FarseerPhysics.Common;
using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;
using FarseerPhysics.Dynamics.Joints;
using FarseerPhysics.Factories;

namespace LOL
{
    /**
     * Game designers can configure the physics of a level (i.e., the default
     * forces, if any), via this class. Internally, the class constructs a box2d
     * physics world and instantiates the callbacks needed to ensure that LOL works
     * correctly.
     */
    public class Physics
    {
        /**
         * This ratio means that every 10 pixels on the screen will correspond to a
         * meter. Note that 'pixels' are defined in terms of what a programmer's
         * Config says, not the actual screen size, because the programmer's Config
         * gets scaled to screen dimensions.
         */
        internal static float PIXEL_METER_RATIO = 10;

        /**
         * When a hero collides with a "sticky" obstacle, this is the code we run to
         * figure out what to do
         * 
         * @param sticky The sticky entity... it should always be an obstacle for
         *            now
         * @param other The other entity... it should always be a hero for now
         * @param contact A description of the contact event
         */
        internal static void HandleSticky(PhysicsSprite sticky, PhysicsSprite other, Contact contact)
        {
            if (other.mDJoint != null) { return; }
            if (other.mStickyDelay != null && DateTime.Now < other.mStickyDelay)
            {
                return;
            }
            if ((sticky.mIsSticky[0] && other.YPosition >= sticky.YPosition + sticky.mSize.Y)
                || (sticky.mIsSticky[1] && other.XPosition + other.mSize.X <= sticky.XPosition)
                || (sticky.mIsSticky[3] && other.XPosition >= sticky.XPosition + sticky.mSize.X)
                || (sticky.mIsSticky[2] && other.YPosition + other.mSize.Y <= sticky.YPosition))
            {
                Vector2 normal;
                FixedArray2<Vector2> points;
                contact.GetWorldManifold(out normal, out points);
                Vector2 v = points[0];
                Level.sCurrent.mOneTimeEvents.Add(() =>
                {
                    other.mBody.LinearVelocity = new Vector2(0, 0);
                    DistanceJoint d = JointFactory.CreateDistanceJoint(Level.sCurrent.mWorld, sticky.mBody, other.mBody, v, v, false);
                    d.CollideConnected = true;
                    other.mDJoint = d;

                    WeldJoint w = JointFactory.CreateWeldJoint(Level.sCurrent.mWorld, sticky.mBody, other.mBody, v, v, false);
                    w.CollideConnected = true;
                    other.mWJoint = w;
                });

            }
        }

        /**
         * Configure physics for the current level
         * 
         * @param defaultXGravity The default force moving entities to the left
         *            (negative) or right (positive)... Usually zero
         * @param defaultYGravity The default force pushing the hero down (negative)
         *            or up (positive)... Usually zero or -10
         */
        public static void Configure(float defaultXGravity, float defaultYGravity)
        {
            Level.sCurrent.mWorld = new World(new Vector2(defaultXGravity, defaultYGravity));
            BeginContactDelegate beginContact = delegate(Contact contact)
            {
                Object a = contact.FixtureA.Body.UserData;
                Object b = contact.FixtureB.Body.UserData;

                if (!(a is PhysicsSprite) || !(b is PhysicsSprite)) { return false; }
                PhysicsSprite c0, c1;
                if (a is Hero)
                {
                    c0 = (PhysicsSprite)a;
                    c1 = (PhysicsSprite)b;
                }
                else if (b is Hero)
                {
                    c0 = (PhysicsSprite)b;
                    c1 = (PhysicsSprite)a;
                }
                else if (a is Enemy)
                {
                    c0 = (PhysicsSprite)a;
                    c1 = (PhysicsSprite)b;
                }
                else if (b is Enemy)
                {
                    c0 = (PhysicsSprite)b;
                    c1 = (PhysicsSprite)a;
                }
                else if (a is Projectile)
                {
                    c0 = (PhysicsSprite)a;
                    c1 = (PhysicsSprite)b;
                }
                else if (b is Projectile)
                {
                    c0 = (PhysicsSprite)b;
                    c1 = (PhysicsSprite)a;
                }
                else
                {
                    return true;
                }
                Level.sCurrent.mOneTimeEvents.Add(delegate()
                {
                    c0.OnCollide(c1, contact);
                });
                return true;
            };
            PreSolveDelegate preSolve = delegate(Contact contact, ref Manifold oldManifold)
            {
                Object a = contact.FixtureA.Body.UserData;
                Object b = contact.FixtureB.Body.UserData;

                if (!(a is PhysicsSprite) || !(b is PhysicsSprite)) { return; }

                PhysicsSprite gfoA = (PhysicsSprite)a;
                PhysicsSprite gfoB = (PhysicsSprite)b;

                if (gfoA.mIsSticky[0] || gfoA.mIsSticky[1] || gfoA.mIsSticky[2] || gfoA.mIsSticky[3])
                {
                    HandleSticky(gfoA, gfoB, contact);
                    return;
                }
                else if (gfoB.mIsSticky[0] || gfoB.mIsSticky[1] || gfoB.mIsSticky[2] || gfoB.mIsSticky[3])
                {
                    HandleSticky(gfoB, gfoA, contact);
                    return;
                }

                if (gfoA.mPassThroughId != 0 && gfoA.mPassThroughId == gfoB.mPassThroughId)
                {
                    contact.Enabled = false;
                    return;
                }

                PhysicsSprite onesided = null;
                PhysicsSprite other = null;

                if (gfoA.mIsOneSided > -1)
                {
                    onesided = gfoA;
                    other = gfoB;
                }
                else if (gfoB.mIsOneSided > -1)
                {
                    onesided = gfoB;
                    other = gfoA;
                }
                else { return; }

                Vector2 normal;
                FixedArray2<Vector2> points;
                contact.GetWorldManifold(out normal, out points);
                for (int i = 0; i < 2; ++i)
                {
                    Vector2 v2 = points[i];
                    if (onesided.mIsOneSided == 0 && v2.Y < 0)
                    {
                        contact.Enabled = false;
                    }
                    else if (onesided.mIsOneSided == 2 && v2.Y > 0)
                    {
                        contact.Enabled = false;
                    }
                    else if (onesided.mIsOneSided == 1 && v2.X > 0)
                    {
                        contact.Enabled = false;
                    }
                    else if (onesided.mIsOneSided == 3 && v2.X < 0)
                    {
                        contact.Enabled = false;
                    }
                }
            };

            Level.sCurrent.mWorld.ContactManager.BeginContact += beginContact;
            Level.sCurrent.mWorld.ContactManager.PreSolve += preSolve;
        }
    }
}
