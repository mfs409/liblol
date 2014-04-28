using System;

using Microsoft.Xna.Framework;

using FarseerPhysics.Collision;
using FarseerPhysics.Common;
using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;
using FarseerPhysics.Dynamics.Joints;
using FarseerPhysics.Factories;

namespace LibLOL
{
    public class Physics
    {
        internal static float PIXEL_METER_RATIO = 10;

        internal static void HandleSticky(PhysicsSprite sticky, PhysicsSprite other, Contact contact)
        {
            if (other.mDJoint != null) { return; }
            if (Lol.GlobalGameTime.ElapsedTicks < other.mStickyDelay)
            {
                return;
            }
            if ((sticky.mIsSticky[0] && other.GetYPosition() >= sticky.GetYPosition() + sticky.mSize.Y)
                || (sticky.mIsSticky[1] && other.GetXPosition() + other.mSize.X <= sticky.GetXPosition())
                || (sticky.mIsSticky[3] && other.GetXPosition() >= sticky.GetXPosition() + sticky.mSize.X)
                || (sticky.mIsSticky[2] && other.GetYPosition() + other.mSize.Y <= sticky.GetYPosition()))
            {
                Vector2 normal;
                FixedArray2<Vector2> points;
                contact.GetWorldManifold(out normal, out points);
                Vector2 v = points[0];
                Level.sCurrent.mOneTimeEvents.Add(delegate()
                {
                    other.mBody.LinearVelocity = new Vector2(0, 0);
                    DistanceJoint d = JointFactory.CreateDistanceJoint(Level.sCurrent.mWorld, sticky.mBody, other.mBody, v, v);
                    d.CollideConnected = true;
                    other.mDJoint = d;

                    WeldJoint w = JointFactory.CreateWeldJoint(Level.sCurrent.mWorld, sticky.mBody, other.mBody, v, v);
                    w.CollideConnected = true;
                    other.mWJoint = w;
                });

            }
        }

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
                    return false;
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
