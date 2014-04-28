using System;
using System.Diagnostics;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Audio;

using FarseerPhysics;
using FarseerPhysics.Collision.Shapes;
using FarseerPhysics.Common;
using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;
using FarseerPhysics.Dynamics.Joints;
using FarseerPhysics.Factories;

namespace LibLOL
{
    public abstract class PhysicsSprite
    {
        internal Body mBody;

        private bool mIsCircleBody;

        private bool mIsBoxBody;

        private bool mIsPolygonBody;

        internal bool mVisible = true;

        private bool mReverseFace = false;

        private bool mIsFlipped;

        internal int mZIndex = 0;

        internal Vector2 mSize = new Vector2();

        private RouteDriver mRoute;

        internal String mInfoText = "";

        internal Level.TouchAction mTouchResponder;

        internal Vector2 mCameraOffset = new Vector2(0, 0);

        internal DistanceJoint mDJoint;

        internal WeldJoint mWJoint;

        internal long mStickyDelay;

        internal SoundEffect mTouchSound;

        internal Animation.AnimationDriver mAnimator;

        internal Animation mDefaultAnimation;

        internal Animation mDisappearAnimation;

        internal Vector2 mDisappearAnimateOffset = new Vector2();

        internal Vector2 mDisappearAnimateSize = new Vector2();

        internal Vector3? mHover = new Vector3();

        internal bool[] mIsSticky = new bool[4];

        protected SoundEffect mDisappearSound;

        internal int mIsOneSided = -1;

        internal int mPassThroughId = 0;

        //internal Vector2 mTmpVert = new Vector2();

        internal delegate void CollisionCallback(PhysicsSprite ps, Contact c);

        internal PhysicsSprite(String imgName, float width, float height)
        {
            mAnimator = new Animation.AnimationDriver(imgName);
            mSize.X = width;
            mSize.Y = height;
        }

        internal abstract void OnCollide(PhysicsSprite other, Contact contact);

        internal void UpdateVelocity(float x, float y)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                mBody.BodyType = BodyType.Kinematic;
            }
            if (mDJoint != null)
            {
                Level.sCurrent.mWorld.RemoveJoint(mDJoint);
                mDJoint = null;
                Level.sCurrent.mWorld.RemoveJoint(mWJoint);
                mWJoint = null;
            }
            mBody.LinearVelocity = new Vector2(x, y);
        }

        internal virtual void HandleTouchDown(float x, float y)
        {
            if (mTouchSound != null)
            {
                mTouchSound.Play();
            }
            if (mTouchResponder != null)
            {
                mTouchResponder.OnDown(x, y);
            }
        }

        internal bool HandleTouchDrag(float x, float y)
        {
            if (mTouchResponder != null)
            {
                mTouchResponder.OnMove(x, y);
                return false;
            }
            return true;
        }

        internal virtual void Update(GameTime gameTime)
        {
            if (mVisible)
            {
                if (mRoute != null && mVisible)
                {
                    mRoute.Drive();
                }
            }
        }

        internal virtual void Draw(SpriteBatch sb, GameTime gameTime)
        {
            Texture2D tr = mAnimator.GetTr(gameTime);
            SpriteEffects flipH = SpriteEffects.None;
            if (mReverseFace && mBody.LinearVelocity.X < 0)
            {
                if (!mIsFlipped)
                {
                    flipH = SpriteEffects.FlipHorizontally;
                    mIsFlipped = true;
                }
            }
            else if (mReverseFace && mBody.LinearVelocity.X > 0)
            {
                if (mIsFlipped)
                {
                    flipH = SpriteEffects.FlipHorizontally;
                    mIsFlipped = false;
                }
            }
            if (tr != null)
            {
                Vector2 pos = new Vector2(mBody.Position.X - mSize.X / 2, mBody.Position.Y - mSize.Y / 2);

                sb.Draw(tr, pos, null, null, new Vector2(mSize.X / 2, mSize.Y / 2), 
                    mBody.Rotation, null, Color.White, flipH, 0f);
            }
        }

        internal void SetBoxPhysics(float density, float elasticity, float friction,
            BodyType type, bool isProjectile, float x, float y)
        {

            Vertices boxVertices = PolygonTools.CreateRectangle(mSize.X / 2, mSize.Y / 2);
            PolygonShape boxPoly = new PolygonShape(boxVertices, density);
            Vector2 position = new Vector2(x + mSize.X / 2, y + mSize.Y / 2);
            mBody = BodyFactory.CreateBody(Level.sCurrent.mWorld, position, 0f, this);
            mBody.BodyType = type;
            Fixture fd = mBody.CreateFixture(boxPoly);
            fd.Friction = friction;
            fd.Restitution = elasticity;
            
            if (isProjectile)
            {
                mBody.IsBullet = true;
            }

            mIsCircleBody = false;
            mIsBoxBody = true;
            mIsPolygonBody = false;
            
        }

        internal void SetPolygonPhysics(float density, float elasticity, float friction, BodyType type,
            bool isProjectile, float x, float y, params float[] vertices)
        {
            Vector2[] verts = new Vector2[vertices.Length / 2];
            for (int i = 0; i < vertices.Length; i += 2)
            {
                verts[i / 2] = new Vector2(vertices[i], vertices[i + 1]);
            }
            PolygonShape boxPoly = new PolygonShape(new Vertices(verts), density);
            Vector2 position = new Vector2(x + mSize.X / 2, y + mSize.Y / 2);
            mBody = BodyFactory.CreateBody(Level.sCurrent.mWorld, position, 0f, this);
            mBody.BodyType = type;
            Fixture fd = mBody.CreateFixture(boxPoly);
            fd.Friction = friction;
            fd.Restitution = elasticity;

            if (isProjectile)
            {
                mBody.IsBullet = true;
            }

            mIsCircleBody = false;
            mIsBoxBody = false;
            mIsPolygonBody = true;
        }

        internal void SetCirclePhysics(float density, float elasticity, float friction, BodyType type,
            bool isProjectile, float x, float y, float radius)
        {
            CircleShape c = new CircleShape(radius, density);
            Vector2 position = new Vector2(x + mSize.X / 2, y + mSize.Y / 2);
            mBody = BodyFactory.CreateBody(Level.sCurrent.mWorld, position, 0f, this);
            mBody.BodyType = type;
            Fixture fd = mBody.CreateFixture(c);
            fd.Friction = friction;
            fd.Restitution = elasticity;

            if (isProjectile)
            {
                mBody.IsBullet = true;
            }

            mIsCircleBody = true;
            mIsBoxBody = false;
            mIsPolygonBody = false;
        }

        /*
         * PUBLIC INTERFACE
         */

        public void SetTextInfo(String text)
        {
            mInfoText = text;
        }

        public String GetTextInfo()
        {
            return mInfoText;
        }

        public void SetCameraOffset(float x, float y)
        {

        }

        public void SetCollisionEffect(bool state)
        {
            foreach (Fixture f in mBody.FixtureList)
            {
                f.IsSensor = !state;
            }
        }

        public void SetPhysics(float density, float elasticity, float friction)
        {
            foreach (Fixture f in mBody.FixtureList)
            {
                f.Restitution = elasticity;
                f.Friction = friction;
                f.Shape.Density = density;
            }
            mBody.ResetMassData();
        }

        public void DisableRotation()
        {
            mBody.FixedRotation = true;
        }

        public float GetXPosition()
        {
            return mBody.Position.X - mSize.X / 2;
        }

        public float GetYPosition()
        {
            return mBody.Position.Y - mSize.Y / 2;
        }

        public float GetWidth()
        {
            return mSize.X;
        }

        public float GetHeight()
        {
            return mSize.Y;
        }

        public void SetMoveByTilting()
        {
            // Some stuff regarding Level
            SetCollisionEffect(true);
        }

        public void SetRotation(float rotation)
        {
            mBody.SetTransform(mBody.Position, rotation);
        }

        public void Remove(bool quiet)
        {
            mVisible = false;
            mBody.Enabled = false;

            if (mDisappearSound != null && !quiet)
            {
                mDisappearSound.Play();
            }

            if (mDisappearAnimation != null)
            {
                // do stuff
            }
        }

        public void AddVelocity(float x, float y, bool immuneToPhysics)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                if (immuneToPhysics)
                {
                    mBody.BodyType = BodyType.Kinematic;
                }
                else
                {
                    mBody.BodyType = BodyType.Dynamic;
                }
            }

            Vector2 v = mBody.LinearVelocity;
            v.X += x;
            v.Y += y;
            UpdateVelocity(v.X, v.Y);
            SetCollisionEffect(true);
        }

        public void SetAbsoluteVelocity(float x, float y, bool immuteToPhysics)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                if (immuteToPhysics)
                {
                    mBody.BodyType = BodyType.Kinematic;
                }
                else
                {
                    mBody.BodyType = BodyType.Dynamic;
                }
            }

            UpdateVelocity(x, y);
            SetCollisionEffect(true);
        }

        //public void SetTouchTrigger(...)

        public float GetXVelocity()
        {
            return mBody.LinearVelocity.X;
        }

        public float GetYVelocity()
        {
            return mBody.LinearVelocity.Y;
        }

        public void SetRoute(Route route, float velocity, bool loop)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                mBody.BodyType = BodyType.Kinematic;
            }
            mRoute = new RouteDriver(route, velocity, loop, this);
        }

        public void SetRotationSpeed(float duration)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                mBody.BodyType = BodyType.Kinematic;
            }
            // Needs to be in Radians/second. 2*pi radians in a circle.
            mBody.AngularVelocity = (float)(2 * Math.PI / duration);
        }

        public void SetCanDrag(bool immuneToPhysics)
        {
            if (immuneToPhysics)
            {
                mBody.BodyType = BodyType.Kinematic;
            }
            else
            {
                mBody.BodyType = BodyType.Dynamic;
            }
            Level.OnTouchDelegate onMove = (x, y) => { mBody.SetTransform(new Vector2(x, y), mBody.Rotation); };
            mTouchResponder = new Level.TouchAction(null, onMove, null);

        }

        public void SetTouchSound(String sound)
        {

        }

        public void SetPokeToPlace(long deleteThresholdMillis)
        {
            // Convert deleteThresholdMillis to seconds, then multiple by frequency of stopwatch to get number of ticks.
            long deleteThresholdTicks = (long)(deleteThresholdMillis / 1000.0) * System.Diagnostics.Stopwatch.Frequency;

            // Go go closures
            long mLastPokeTicks = 0;
            Level.OnTouchDelegate onDown = delegate(float x, float y)
            {
                // Lol.sGame.Vibrate(100);
                long ticks = Lol.GlobalGameTime.ElapsedTicks;
                if ((ticks - mLastPokeTicks) < deleteThresholdTicks)
                {
                    mBody.Enabled = false;
                    mVisible = false;
                    Level.sCurrent.mTouchResponder = null;
                    return;
                }
                else
                {
                    mLastPokeTicks = ticks;
                }
                Level.OnTouchDelegate sCurrentOnDown = delegate(float xx, float yy)
                {
                    // Lol.sGame.Vibrate(100);
                    mBody.SetTransform(new Vector2(xx, yy), mBody.Rotation);
                    Level.sCurrent.mTouchResponder = null;
                };
                Level.sCurrent.mTouchResponder = new Level.TouchAction(sCurrentOnDown, null, null);
            };
            mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        public void SetFlickable(float damnFactor)
        {
            if (mBody.BodyType != BodyType.Dynamic)
            {
                mBody.BodyType = BodyType.Dynamic;
            }

            Level.OnTouchDelegate onDown = delegate(float x, float y)
            {
                // Lol.sGame.Vibrate(100);
                float initialX = mBody.Position.X;
                float initialY = mBody.Position.Y;

                Level.OnTouchDelegate sLevelOnUp = delegate(float xx, float yy)
                {
                    if (mVisible)
                    {
                        mHover = null;
                        UpdateVelocity((xx - initialX) * damnFactor, (yy - initialY) * damnFactor);
                        Level.sCurrent.mTouchResponder = null;
                    }
                };
                Level.sCurrent.mTouchResponder = new Level.TouchAction(null, null, sLevelOnUp);
            };
            mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        public void SetPokePath(float velocity, bool oncePerTouch, bool updateOnMove, bool stopOnUp)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                mBody.BodyType = BodyType.Kinematic;
            }

            Level.OnTouchDelegate onDown = delegate(float x, float y)
            {
                // Lol.sGame.Vibrate(5);
                Level.OnTouchDelegate sLevelOnDown = delegate(float xx, float yy)
                {
                    Route r = new Route(2).To(GetXPosition(), GetYPosition()).To(xx - mSize.X / 2, yy - mSize.Y / 2);
                    SetAbsoluteVelocity(0, 0, false);
                    SetRoute(r, velocity, false);
                    if (oncePerTouch)
                    {
                        Level.sCurrent.mTouchResponder = null;
                    }
                };
                Level.OnTouchDelegate sLevelOnMove = delegate(float xx, float yy)
                {
                    if (updateOnMove)
                    {
                        sLevelOnDown(xx, yy);
                    }
                };
                Level.OnTouchDelegate sLevelOnUp = delegate(float xx, float yy)
                {
                    if (stopOnUp && mRoute != null)
                    {
                        mRoute.HaltRoute();
                    }
                };
                Level.sCurrent.mTouchResponder = new Level.TouchAction(sLevelOnDown, sLevelOnMove, sLevelOnUp);
            };
            mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        public void SetDefaultAnimation(Animation a)
        {
            mDefaultAnimation = a;
            mAnimator.SetCurrentAnimation(mDefaultAnimation);
        }

        public void SetDisappearAnimation(Animation a, float offsetX, float offsetY, float width, float height)
        {
            mDisappearAnimation = a;
            mDisappearAnimateOffset.X = offsetX;
            mDisappearAnimateOffset.Y = offsetY;
            mDisappearAnimateSize.X = width;
            mDisappearAnimateSize.Y = height;
        }

        public void SetCanFaceBackwards()
        {
            mReverseFace = true;
        }

        public void SetAppearDelay(float delay)
        {

        }

        public void SetDisappearDelay(float delay)
        {

        }

        public void SetImage(String imgName, int index)
        {
            mAnimator.UpdateImage(imgName);
            mAnimator.SetIndex(index);
        }

        public void Resize(float x, float y, float width, float height)
        {
            float xscale = height / mSize.Y;
            float yscale = width / mSize.X;

            mSize.X = width;
            mSize.Y = height;

            Body oldBody = mBody;

            if (mIsCircleBody)
            {
                Fixture oldFix = oldBody.FixtureList[0];
                SetCirclePhysics(oldFix.Shape.Density, oldFix.Restitution, oldFix.Friction, oldBody.BodyType,
                    oldBody.IsBullet, x, y, (width > height) ? width / 2 : height / 2);
            }
            else if (mIsBoxBody)
            {
                Fixture oldFix = oldBody.FixtureList[0];
                SetBoxPhysics(oldFix.Shape.Density, oldFix.Restitution, oldFix.Friction, oldBody.BodyType,
                    oldBody.IsBullet, x, y);
            }
            else if (mIsPolygonBody)
            {
                Fixture oldFix = oldBody.FixtureList[0];
                PolygonShape ps = (PolygonShape)oldFix.Shape;
                float[] verts = new float[ps.Vertices.Count * 2];
                for(int i = 0; i < ps.Vertices.Count; ++i)
                {
                    Vector2 v = ps.Vertices[i];
                    verts[2 * i] = v.X * xscale;
                    verts[2 * i + 1] = v.Y * yscale;
                }
                SetPolygonPhysics(ps.Density, oldFix.Restitution, oldFix.Friction, oldBody.BodyType,
                    oldBody.IsBullet, x, y, verts);
            }
            mBody.AngularVelocity = oldBody.AngularVelocity;
            mBody.SetTransform(oldBody.Position, oldBody.Rotation);
            mBody.GravityScale = oldBody.GravityScale;
            mBody.LinearDamping = oldBody.LinearDamping;
            mBody.LinearVelocity = oldBody.LinearVelocity;
            oldBody.Enabled = false;
        }

        public void SetShrinkOverTime(float shrinkX, float shrinkY, bool keepCentered)
        {

        }

        public void SetHover(int x, int y)
        {

        }

        public void SetGravityDefy()
        {
            mBody.GravityScale = 0;
        }

        public void SetSticky(bool top, bool right, bool bottom, bool left)
        {
            mIsSticky = new bool[] {
                top, right, bottom, left
            };
        }

        public void SetDisappearSound(String soundName)
        {

        }

        public void SetTouchToThrow(Hero h)
        {
            //Level.OnTouch onDown = (x, y) => { Level.sCurrent.mProjectilePool.throwFixed(h); };
            // mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        public void SetOneSided(int side)
        {
            mIsOneSided = side;
        }

        public void SetPassThrough(int id)
        {
            mPassThroughId = id;
        }

        public void SetCanFall()
        {
            mBody.BodyType = BodyType.Dynamic;
        }

        public void SetChaseSpeed(float speed, PhysicsSprite target, bool chaseInX, bool chaseInY)
        {
            mBody.BodyType = BodyType.Dynamic;
            Level.sCurrent.mRepeatEvents.Add(() =>
            {
                if (!target.mVisible) { return; }
                if (!mVisible) { return; }
                float x = target.mBody.Position.X - mBody.Position.X;
                float y = target.mBody.Position.Y - mBody.Position.Y;
                float denom = (float)Math.Sqrt(x * x + y * y);
                x /= denom;
                y /= denom;
                x *= speed;
                y *= speed;
                if (!chaseInX)
                {
                    x = mBody.LinearVelocity.X;
                    y *= 2;
                }
                if (!chaseInY)
                {
                    y = mBody.LinearVelocity.Y;
                    x *= 2;
                }
                UpdateVelocity(x, y);
            });
        }

        public void SetRotationByDirection()
        {
            Level.sCurrent.mRepeatEvents.Add(() =>
            {
                if (mVisible)
                {
                    float x = mBody.LinearVelocity.X;
                    float y = mBody.LinearVelocity.Y;
                    double angle = Math.Atan2(y, x) + Math.Atan2(-1, 0);
                    mBody.SetTransform(mBody.Position, (float)angle);
                }
            });
        }

        public void SetZIndex(int zIndex)
        {
            Debug.Assert(zIndex <= 2);
            Debug.Assert(zIndex >= -2);
            //Level.sCurrent.RemoveSprite(this, mZIndex);
            mZIndex = zIndex;
            //Level.sCurrent.AddSprite(this, mZIndex);
        }
    }
}