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

namespace LOL
{
    public abstract class PhysicsSprite : Renderable
    {
        /**
         * Physics body for this PhysicsSprite
         */
        internal Body mBody;

        /**
         * Track if the underlying body is a circle
         */
        private bool mIsCircleBody;

        /**
         * Track if the underlying body is a box
         */
        private bool mIsBoxBody;

        /**
         * Track if the underlying body is a polygon
         */
        private bool mIsPolygonBody;

        /**
         * Track if the entity is currently being rendered. This is a proxy for
         * "is important to the rest of the game" and when it is false, we don't run
         * any updates on the PhysicsSprite
         */
        internal bool mVisible = true;

        /**
         * Does the entity's image flip when the hero moves backwards?
         */
        private bool mReverseFace = false;

        /**
         * We may opt to flip the image when it is moving in the -X direction. If
         * so, this tracks if the image is flipped, so that we draw its sprite
         * correctly.
         */
        private bool mIsFlipped;

        /**
         * The z index of this entity. Valid range is [-2, 2]
         */
        internal int mZIndex = 0;

        /**
         * The dimensions of the PhysicsSprite... x is width, y is height
         */
        internal Vector2 mSize = new Vector2();

        /**
         * Does this entity follow a route? If so, the RouteDriver will be used to
         * advance the entity along its route.
         */
        private RouteDriver mRoute;

        /**
         * Text that game designer can modify to hold additional information
         */
        internal string mInfoText = "";

        /**
         * Some PhysicsSprites run custom code when they are touched. This is a
         * reference to the code to run.
         */
        internal Level.TouchAction mTouchResponder;

        /**
         * When the camera follows the entity without centering on it, this gives us
         * the difference between the hero and camera
         */
        internal Vector2 mCameraOffset = new Vector2(0, 0);

        /**
         * Sometimes a hero collides with an obstacle, and should stick to it. In
         * that case, we create a pair of joints to connect the two entities. This
         * is the Distance joint that connects them (Note: for convenience, we store
         * the joints in a common parent class)
         */
        internal DistanceJoint mDJoint;

        /**
         * Sometimes a hero collides with an obstacle, and should stick to it. In
         * that case, we create a pair of joints to connect the two entities. This
         * is the Weld joint that connects them (Note: for convenience, we store the
         * joints in a common parent class)
         */
        internal WeldJoint mWJoint;

        /**
         * When we have PhysicsSprites stuck together, we might want to set a brief
         * delay before they can re-join. This field represents that delay time, in
         * milliseconds.
         */
        internal DateTime mStickyDelay;

        /**
         * a sound to play when the obstacle is touched
         */
        internal SoundEffect mTouchSound;

        /**
         * Animation support: this tracks the current state of the active animation
         * (if any)
         */
        internal Animation.AnimationDriver mAnimator;

        /**
         * Animation support: the cells of the default animation
         */
        internal Animation mDefaultAnimation;

        /**
         * Animation support: the cells of the disappearance animation
         */
        internal Animation mDisappearAnimation;

        /**
         * Animation support: the offset for placing the disappearance animation
         * relative to the disappearing sprite
         */
        internal Vector2 mDisappearAnimateOffset = new Vector2();

        /**
         * Animation support: the width of the disappearance animation
         */
        internal Vector2 mDisappearAnimateSize = new Vector2();

        /**
         * A vector for computing hover placement
         */
        internal Vector3? mHover = new Vector3();

        /**
         * Track if heroes stick to this PhysicsSprite. The array has 4 positions,
         * corresponding to top, right, bottom, left
         */
        internal bool[] mIsSticky = new bool[4];

        /**
         * Sound to play when this disappears
         */
        protected internal SoundEffect mDisappearSound;

        /**
         * Disable 3 of 4 sides of a PhysicsSprite, to allow walking through walls.
         * The value reflects the side that remains active. 0 is top, 1 is right, 2
         * is bottom, 3 is left
         */
        internal int mIsOneSided = -1;

        /**
         * Entities with a matching nonzero Id don't collide with each other
         */
        internal int mPassThroughId = 0;

        /**
         * When a PhysicsSprite collides with another PhysicsSprite, and that
         * collision is intended to cause some custom code to run, we use this
         * interface
         */
        internal delegate void CollisionCallback(PhysicsSprite ps, Contact c);

        /**
         * Create a new PhysicsSprite that does not yet have physics, but that has a
         * renderable picture
         * 
         * @param imgName The image to display
         * @param id The type of PhysicsSprite
         * @param width The width
         * @param height The height
         */
        internal PhysicsSprite(string imgName, float width, float height)
        {
            mAnimator = new Animation.AnimationDriver(imgName);
            mSize.X = width;
            mSize.Y = height;
        }

        /**
         * Each descendant defines this to address any custom logic that we need to
         * deal with on a collision
         * 
         * @param other The other entity involved in the collision
         */
        internal abstract void OnCollide(PhysicsSprite other, Contact contact);

        /**
         * Internal method for updating an entity's velocity, so that we can handle
         * its direction correctly
         * 
         * @param x The new x velocity
         * @param y The new y velocity
         */
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

        /**
         * When this PhysicsSprite is touched (down press), we run this code
         * 
         * @param x The X coordinate that was touched
         * @param y The Y coordinate that was touched
         */
        internal virtual void HandleTouchDown(float x, float y)
        {
            if (mTouchSound != null)
            {
                mTouchSound.Play();
            }
            if (mTouchResponder != null && mTouchResponder.OnDown != null)
            {
                mTouchResponder.OnDown(x, y);
            }
        }

        /**
         * When this PhysicsSprite is touched (move/drag press), we run this code
         * 
         * @param x The X coordinate that was touched
         * @param y The Y coordinate that was touched
         */
        internal bool HandleTouchDrag(float x, float y)
        {
            if (mTouchResponder != null && mTouchResponder.OnMove != null)
            {
                mTouchResponder.OnMove(x, y);
                return false;
            }
            return true;
        }

        /**
         * Every time the world advances by a timestep, we call this code. It
         * updates the PhysicsSprite and draws it.
         */
        public override void Update(GameTime gameTime)
        {
            if (mVisible)
            {
                if (mRoute != null && mVisible)
                {
                    mRoute.drive();
                }
            }
        }

        /**
         * Every time the world advances by a timestep, we call this code. It
         * updates the PhysicsSprite and draws it.
         */
        public override void Draw(SpriteBatch sb, GameTime gameTime)
        {
            if (!mVisible)
            {
                return;
            }
            
            Texture2D tr = mAnimator.getTr();
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
                Vector2 tlCorner = new Vector2(Level.sCurrent.mGameCam.drawX(mBody.Position.X), Level.sCurrent.mGameCam.drawY(mBody.Position.Y)),
                        dim = new Vector2(Level.sCurrent.mGameCam.drawWidth(mSize.X), Level.sCurrent.mGameCam.drawHeight(mSize.Y)),
                        pos = new Vector2(tlCorner.X - dim.X / 2, tlCorner.Y - dim.Y / 2);
                Rectangle srcRect = new Rectangle((int)tlCorner.X, (int)tlCorner.Y, (int)dim.X, (int)dim.Y);
                sb.Draw(tr, srcRect, null, Color.White, -mBody.Rotation, new Vector2(tr.Width / 2, tr.Height / 2), flipH, 0f);
            }
        }

        /**
         * Specify that this entity should have a rectangular physics shape
         * 
         * @param density Density of the entity
         * @param elasticity Elasticity of the entity
         * @param friction Friction of the entity
         * @param type Is this static or dynamic?
         * @param isProjectile Is this a fast-moving object
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         */
        internal void SetBoxPhysics(float density, float elasticity, float friction,
            BodyType type, bool isProjectile, float x, float y)
        {

            Vertices boxVertices = PolygonTools.CreateRectangle(mSize.X / 2, mSize.Y / 2);
            PolygonShape boxPoly = new PolygonShape(boxVertices, density);
            Vector2 position = new Vector2(x + mSize.X / 2, y + mSize.Y / 2);
            mBody = BodyFactory.CreateBody(Level.sCurrent.mWorld, position, 0f, this);
            mBody.BodyType = type;
            Fixture fd = mBody.CreateFixture(boxPoly, null);
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

        /**
         * Specify that this entity should have a polygon physics shape. You must
         * take extreme care when using this method. Polygon vertices must be given
         * in counter-clockwise order, and they must describe a convex shape.
         * 
         * @param density Density of the entity
         * @param elasticity Elasticity of the entity
         * @param friction Friction of the entity
         * @param type Is this static or dynamic?
         * @param isProjectile Is this a fast-moving object
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param vertices Up to 16 coordinates representing the vertexes of this
         *            polygon, listed as x0,y0,x1,y1,x2,y2,...
         */
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
            Fixture fd = mBody.CreateFixture(boxPoly, null);
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

        /**
         * Specify that this entity should have a circular physics shape
         * 
         * @param density Density of the entity
         * @param elasticity Elasticity of the entity
         * @param friction Friction of the entity
         * @param type Is this static or dynamic?
         * @param isProjectile Is this a fast-moving object
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param radius The radius of the underlying circle
         */
        internal void SetCirclePhysics(float density, float elasticity, float friction, BodyType type,
            bool isProjectile, float x, float y, float radius)
        {
            CircleShape c = new CircleShape(radius, density);
            Vector2 position = new Vector2(x + mSize.X / 2, y + mSize.Y / 2);
            mBody = BodyFactory.CreateBody(Level.sCurrent.mWorld, position, 0f, this);
            mBody.BodyType = type;
            Fixture fd = mBody.CreateFixture(c,null);
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

        /**
         * Any additional information for this sprite
         */
        public string TextInfo
        {
            get { return mInfoText; }
            set { mInfoText = value; }
        }

        /**
         * Make the camera follow the entity, but without centering the entity on
         * the screen
         * 
         * @param x Amount of x distance between entity and center
         * @param y Amount of y distance between entity and center
         */
        public void SetCameraOffset(float x, float y)
        {
            mCameraOffset.X = x;
            mCameraOffset.Y = y;
        }

        /**
         * Change whether this entity engages in physics collisions or not
         * 
         * @note Must be either true or false. true indicates that the object will
         *       participate in physics collisions. false indicates that it
         *       will not.
         */
        public bool CollisionEffect
        {
            set
            {
                foreach (Fixture f in mBody.FixtureList)
                {
                    f.IsSensor = !value;
                }
            }
        }

        /**
         * Allow the user to adjust the default physics settings (density,
         * elasticity, friction) for this entity
         * 
         * @param density New density of the entity
         * @param elasticity New elasticity of the entity
         * @param friction New friction of the entity
         */
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

        /**
         * Indicate that this entity should not rotate due to torque
         */
        public void DisableRotation()
        {
            mBody.FixedRotation = true;
        }

        /**
         * Returns the X coordinate of this entity
         */
        public float XPosition
        {
            get { return mBody.Position.X - mSize.X / 2; }
        }

        /**
         * Returns the Y coordinate of this entity
         */
        public float YPosition
        {
            get { return mBody.Position.Y - mSize.Y / 2; }
        }

        /**
         * Returns the width of this entity
         */
        public float Width
        {
            get { return mSize.X; }
        }

        /**
         * Return the height of this entity
         */
        public float Height
        {
            get { return mSize.Y; }
        }

        /**
         * Indicate that the entity should move with the tilt of the phone
         */
        public void SetMoveByTilting()
        {
            // If we've already added this to the set of tiltable objects, don't do
            // it again
            if (Level.sCurrent.mTilt.mAccelEntities.Contains(this))
                return;

            // make sure it is moveable, add it to the list of tilt entities
            if (mBody.BodyType != BodyType.Dynamic)
                mBody.BodyType = BodyType.Dynamic;
            Level.sCurrent.mTilt.mAccelEntities.Add(this);
            // turn off sensor behavior, so this collides with stuff...
            CollisionEffect = true;
            
        }

        /**
         * Call this on an Entity to rotate it. Note that this works best on boxes.
         * 
         * @note rotation is in degrees
         */
        public float Rotation
        {
            set { mBody.SetTransform(mBody.Position, value); }
        }

        /**
         * Make an entity disappear
         * 
         * @param quiet True if the disappear sound should not be played
         */
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
                float x = XPosition + mDisappearAnimateOffset.X;
                float y = YPosition + mDisappearAnimateOffset.Y;

                Obstacle o = Obstacle.MakeAsBox(x, y, mDisappearAnimateSize.X, mDisappearAnimateSize.Y, "");
                o.mBody.Enabled = false;
                o.DefaultAnimation = mDisappearAnimation;
            }
        }

        /**
         * Add velocity to this entity
         * 
         * @param x Velocity in X dimension
         * @param y Velocity in Y dimension
         * @param immuneToPhysics Should never be true for heroes! This means that
         *            gravity won't affect the entity, and it can pass through other
         *            entities without colliding.
         */
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
            CollisionEffect = true;
        }

        /**
         * Set the absolute velocity of this Entity
         * 
         * @param x Velocity in X dimension
         * @param y Velocity in Y dimension
         */
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
            CollisionEffect = true;
        }

        /**
         * Indicate that touching this object will cause some special code to run
         * 
         * @param id identifier for the trigger.
         * @param activationGoodies1 Number of type-1 goodies that must be collected
         *            before it works
         * @param activationGoodies2 Number of type-2 goodies that must be collected
         *            before it works
         * @param activationGoodies3 Number of type-3 goodies that must be collected
         *            before it works
         * @param activationGoodies4 Number of type-4 goodies that must be collected
         *            before it works
         * @param disapper True if the entity should disappear after the trigger
         *            completes
         */
        public void SetTouchTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4, bool disappear)
        {
            int[] touchTriggerActivation = new int[] {
                activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4
            };
            Level.TouchAction.TouchDelegate onDown = delegate(float x, float y)
            {
                bool match = true;
                for (int i = 0; i < 4; ++i)
                {
                    match &= touchTriggerActivation[i] <= Level.sCurrent.mScore.mGoodiesCollected[i];
                    if (match)
                    {
                        if (disappear)
                        {
                            Remove(false);
                        }
                        Lol.sGame.onTouchTrigger(id, Lol.sGame.mCurrLevelNum, this);
                    }
                }
            };
            mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        /**
         * Returns the X velocity of of this entity
         */
        public float XVelocity
        {
            get { return mBody.LinearVelocity.X; }
        }

        /**
         * Returns the Y velocity of of this entity
         */
        public float YVelocity
        {
            get { return mBody.LinearVelocity.Y; }
        }

        /**
         * Make this entity move according to a route. The entity can loop back to
         * the beginning of the route.
         * 
         * @param route The route to follow.
         * @param velocity speed at which to travel
         * @param loop Should this route loop continuously
         */
        public void SetRoute(Route route, float velocity, bool loop)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                mBody.BodyType = BodyType.Kinematic;
            }
            mRoute = new RouteDriver(route, velocity, loop, this);
        }

        /**
         * Make the entity continuously rotate. This is usually only useful for
         * fixed objects.
         * 
         * @param duration Time it takes to complete one rotation
         */
        public void SetRotationSpeed(float duration)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                mBody.BodyType = BodyType.Kinematic;
            }
            // Needs to be in Radians/second. 2*pi radians in a circle.
            mBody.AngularVelocity = (float)(2 * Math.PI / duration);
        }

        /**
         * Call this on an entity to make it draggable. Be careful when dragging
         * things. If they are small, they will be hard to touch.
         */
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
            Level.TouchAction.TouchDelegate onMove = (x, y) => { mBody.SetTransform(new Vector2(x, y), mBody.Rotation); };
            mTouchResponder = new Level.TouchAction();
            mTouchResponder.OnMove = onMove;

        }

        /**
         * Indicate that when the player touches this obstacle, we should play a
         * sound
         */
        public string TouchSound
        {
            set { mTouchSound = Media.getSound(value); }
        }

        /**
         * Call this on an Entity to make it pokeable. Poke the entity, then poke
         * the screen, and the entity will move to the location that was pressed.
         * Poke the entity twice in rapid succession to delete it.
         * 
         * @param deleteThresholdMillis If two touches happen within this many
         *            milliseconds, the entity will be deleted. Use 0 to disable
         *            this "delete by double-touch" feature.
         */
        public void SetPokeToPlace(long deleteThresholdMillis)
        {
            DateTime thresh = DateTime.Now.AddMilliseconds(deleteThresholdMillis);

            // Go go closures
            Level.TouchAction.TouchDelegate onDown = delegate(float x, float y)
            {
                Lol.sGame.vibrate(100);
                if (DateTime.Now < thresh)
                {
                    mBody.Enabled = false;
                    mVisible = false;
                    Level.sCurrent.mTouchResponder = null;
                    return;
                }
                else
                {
                    thresh = DateTime.Now.AddMilliseconds(deleteThresholdMillis);
                }
                Level.TouchAction.TouchDelegate sCurrentOnDown = delegate(float xx, float yy)
                {
                    Lol.sGame.vibrate(100);
                    mBody.SetTransform(new Vector2(xx, yy), mBody.Rotation);
                    Level.sCurrent.mTouchResponder = null;
                };
                Level.sCurrent.mTouchResponder = new Level.TouchAction(sCurrentOnDown, null, null);
            };
            mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        /**
         * Indicate that this entity can be flicked on the screen
         * 
         * @param dampFactor A value that is multiplied by the vector for the flick,
         *            to affect speed
         */
        public void SetFlickable(float dampFactor)
        {
            if (mBody.BodyType != BodyType.Dynamic)
            {
                mBody.BodyType = BodyType.Dynamic;
            }

            Level.TouchAction.TouchDelegate onDown = delegate(float x, float y)
            {
                Lol.sGame.vibrate(100);
                float initialX = mBody.Position.X;
                float initialY = mBody.Position.Y;

                Level.TouchAction.TouchDelegate sLevelOnUp = delegate(float xx, float yy)
                {
                    if (mVisible)
                    {
                        mHover = null;
                        UpdateVelocity(xx - initialX * dampFactor, yy - initialY * dampFactor);
                        Level.sCurrent.mTouchResponder = null;
                    }
                };
                Level.sCurrent.mTouchResponder = new Level.TouchAction(null, null, sLevelOnUp);
            };
            mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        /**
         * Configure an entity so that touching an arbitrary point on the screen
         * makes the entity move toward that point. The behavior is similar to
         * pokeToPlace, in that one touches the entity, then where she wants the
         * entity to go. However, this is much more configurable. Note that while
         * there are three boolean parameters, all 8 combinations don't necessarily
         * work in interesting ways.
         * 
         * @param velocity The constant velocity for poke movement
         * @param oncePerTouch After starting a path, does the player need to
         *            re-select (re-touch) the entity before giving it a new
         *            destinaion point?
         * @param updateOnMove Should drags cause the destination point to change?
         * @param stopOnUp When the touch is released, should the entity stop
         *            moving, or continue until it reaches the release point?
         */
        public void SetPokePath(float velocity, bool oncePerTouch, bool updateOnMove, bool stopOnUp)
        {
            if (mBody.BodyType == BodyType.Static)
            {
                mBody.BodyType = BodyType.Kinematic;
            }

            Level.TouchAction.TouchDelegate onDown = delegate(float x, float y)
            {
                Lol.sGame.vibrate(5);
                Level.TouchAction.TouchDelegate sLevelOnDown = delegate(float xx, float yy)
                {
                    Route r = new Route(2).to(XPosition, YPosition).to(xx - mSize.X / 2, yy - mSize.Y / 2);
                    SetAbsoluteVelocity(0, 0, false);
                    SetRoute(r, velocity, false);
                    if (oncePerTouch)
                    {
                        Level.sCurrent.mTouchResponder = null;
                    }
                };
                Level.TouchAction.TouchDelegate sLevelOnMove = delegate(float xx, float yy)
                {
                    if (updateOnMove)
                    {
                        sLevelOnDown(xx, yy);
                    }
                };
                Level.TouchAction.TouchDelegate sLevelOnUp = delegate(float xx, float yy)
                {
                    if (stopOnUp && mRoute != null)
                    {
                        mRoute.haltRoute();
                    }
                };
                Level.sCurrent.mTouchResponder = new Level.TouchAction(sLevelOnDown, sLevelOnMove, sLevelOnUp);
            };
            
            mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        /**
         * Save the animation sequence and start it right away
         */
        public Animation DefaultAnimation
        {
            set
            {
                mDefaultAnimation = value;
                mAnimator.setCurrentAnimation (mDefaultAnimation);
            }
        }

        /**
         * Save an animation sequence for showing when we get rid of a sprite
         * 
         * @param a The animation to display
         * @param offsetX We can offset the animation from the bottom left of the
         *            sprite (useful if animation is larger than sprite dimensions).
         *            This is the x offset.
         * @param offsetY The Y offset (see offsetX for more information)
         * @param width The width of the frames of this animation
         * @param height The height of the frames of this animation
         */
        public void SetDisappearAnimation(Animation a, float offsetX, float offsetY, float width, float height)
        {
            mDisappearAnimation = a;
            mDisappearAnimateOffset.X = offsetX;
            mDisappearAnimateOffset.Y = offsetY;
            mDisappearAnimateSize.X = width;
            mDisappearAnimateSize.Y = height;
        }

        /**
         * Indicate that this entity's image should be reversed when it is moving in
         * the negative x direction. For tilt, this only applies to the last hero
         * created. For velocity, it applies to everyone.
         */
        public void SetCanFaceBackwards()
        {
            mReverseFace = true;
        }

        /**
         * Indicate that something should not appear quite yet...
         */
        public float AppearDelay
        {
            set
            {
                mVisible = false;
                mBody.Enabled = false;
                Timer.Schedule(() => { mVisible = true; mBody.Enabled = true; }, value);
            }
        }

        /**
         * Indicate that something should disappear after a little while
         * 
         * @param delay How long to wait before hiding the thing
         * @param quiet true if the item should disappear quietly, false if it
         *            should play its disappear sound
         */
        public void SetDisappearDelay(float delay, bool quiet)
        {
            Timer.Schedule(() => { Remove(quiet); }, delay);
        }

        /**
         * Change the image being used by the entity
         * 
         * @param imgName The name of the new image file to use
         * @param index The index to use, in the case that the image was registered
         *            as animatable. When in doubt, use 0.
         */
        public void SetImage(string imgName, int index)
        {
            mAnimator.updateImage(imgName);
            mAnimator.setIndex(index);
        }

        /**
         * Change the size of an entity, and/or change its position
         * 
         * @param x The new X coordinate of its bottom left corner
         * @param y The new Y coordinate of its bototm left corner
         * @param width The new width of the entity
         * @param height The new height of the entity
         */
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

        /**
         * Indicate that this entity should shrink over time
         * 
         * @param shrinkX The number of meters by which the X dimension should
         *            shrink each second
         * @param shrinkY The number of meters by which the Y dimension should
         *            shrink each second
         * @param keepCentered Should the entity's center point stay the same as it
         *            shrinks (true), or should its bottom left corner stay in the
         *            same position (false)
         */
        public void SetShrinkOverTime(float shrinkX, float shrinkY, bool keepCentered)
        {
            Timer.Schedule(() => { ShrinkOverTimeHelper(shrinkX, shrinkY, keepCentered); }, 0.05f);
        }

        /**
         * Helper for enabling the entity to shrink over time
         * 
         * @param shrinkX The number of meters by which the X dimension should
         *            shrink each second
         * @param shrinkY The number of meters by which the Y dimension should
         *            shrink each second
         * @param keepCentered Should the entity's center point stay the same as it
         *            shrinks (true), or should its bottom left corner stay in the
         *            same position (false)
         */
        private void ShrinkOverTimeHelper(float shrinkX, float shrinkY, bool keepCentered)
        {
            if (mVisible)
            {
                float x, y;
                if (keepCentered)
                {
                    x = XPosition + shrinkX / 20 / 2;
                    y = YPosition + shrinkY / 20 / 2;
                }
                else
                {
                    x = XPosition;
                    y = YPosition;
                }
                float w = mSize.X - shrinkX / 20;
                float h = mSize.Y - shrinkY / 20;
                if (w > 0.05f && h > 0.05f)
                {
                    Resize(x, y, w, h);
                    Timer.Schedule(() => { ShrinkOverTimeHelper(shrinkX, shrinkY, keepCentered); }, 0.05f);
                }
            }
            else
            {
                Remove(false);
            }
        }

        /**
         * Indicate that this entity should hover at a specific location on the
         * screen, rather than being placed at some point on the level itself. Note
         * that the coordinates to this command are the center position of the
         * hovering entity. Also, be careful about using hover with zoom... hover is
         * relative to screen coordinates (pixels), not world coordinates, so it's
         * going to look funny to use this with zoom
         * 
         * @param x the X coordinate (in pixels) where the entity should appear
         * @param y the Y coordinate (in pixels) where the entity should appear
         */
        public void SetHover(int x, int y)
        {
            mHover = new Vector3();
            Level.Action a = delegate()
            {
                if (mHover == null) { return; }
                
                mHover = new Vector3(Level.sCurrent.mGameCam.levelX(Level.sCurrent.mGameCam.viewX(x)), Level.sCurrent.mGameCam.invertLevelY(Level.sCurrent.mGameCam.levelY(Level.sCurrent.mGameCam.viewY(y))), 0);
                
                mBody.SetTransform(new Vector2(mHover.Value.X, mHover.Value.Y), mBody.Rotation);
            };
            Level.sCurrent.mRepeatEvents.Add(a);
        }

        /**
         * Indicate that this entity should be immune to the force of gravity
         */
        public void SetGravityDefy()
        {
            mBody.GravityScale = 0;
        }

        /**
         * Make this obstacle sticky, so that a hero will stick to it
         * 
         * @param top Is the top sticky?
         * @param right Is the right side sticky?
         * @param bottom Is the bottom sticky?
         * @param left Is the left side sticky?
         */
        public void SetSticky(bool top, bool right, bool bottom, bool left)
        {
            mIsSticky = new bool[] {
                top, right, bottom, left
            };
        }

        /**
         * Set the sound to play when this entity disappears
         */
        public string DisappearSound
        {
            set
            {
                mDisappearSound = Media.getSound(value);
            }
        }

        /**
         * Indicate that touching this entity should make a hero throw a projectile
         * 
         * @param h The hero who should throw a projectile when this is touched
         */
        public void SetTouchToThrow(Hero h)
        {
            Level.TouchAction.TouchDelegate onDown = (x, y) => { Level.sCurrent.mProjectilePool.throwFixed(h); };
            mTouchResponder = new Level.TouchAction(onDown, null, null);
        }

        /**
         * Indicate that this obstacle only registers collisions on one side.
         * 
         * @note 0 is top, 1 is right, 2 is bottom, 3 is left, -1 means "none"
         */
        public int OneSided
        {
            set { mIsOneSided = value; }
        }

        /**
         * Indicate that this entity should not have collisions with any other
         * entity that has the same ID
         */
        public int PassThrough
        {
            set { mPassThroughId = value; }
        }

        /**
         * By default, non-hero entities are not subject to gravity or forces until
         * they are given a path, velocity, or other form of motion. This lets an
         * entity be subject to forces... in practice, using this in a side-scroller
         * means the entity will fall to the ground.
         */
        public void SetCanFall()
        {
            mBody.BodyType = BodyType.Dynamic;
        }

        /**
         * Specify that this entity is supposed to chase another entity
         * 
         * @param speed The speed with which it chases the other entity
         * @param target The entity to chase
         * @param chaseInX Should the entity change its x velocity?
         * @param chaseInY Should the entity change its y velocity?
         */
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

        /**
         * Indicate that this entity's rotation should be determined by the
         * direction in which it is traveling
         */
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

        /**
         * Set the z plane for this entity
         * 
         * @note Values range from -2 to 2. The default is 0.
         */
        public int ZIndex
        {
            set
            {
                Debug.Assert(value <= 2);
                Debug.Assert(value >= -2);
                Level.sCurrent.removeSprite(this, mZIndex);
                mZIndex = value;
                Level.sCurrent.addSprite(this, mZIndex);
            }
        }
    }
}