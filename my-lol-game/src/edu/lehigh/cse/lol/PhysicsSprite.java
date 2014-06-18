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

package edu.lehigh.cse.lol;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.Animation.AnimationDriver;
import edu.lehigh.cse.lol.Level.Action;
import edu.lehigh.cse.lol.Level.GestureAction;

/**
 * PhysicsSprite is the base class upon which every game entity is built
 */
public abstract class PhysicsSprite implements Lol.Renderable {
    /**
     * When a PhysicsSprite collides with another PhysicsSprite, and that
     * collision is intended to cause some custom code to run, we use this
     * interface
     */
    interface CollisionCallback {
        /**
         * Respond to a collision with a PhysicsSprite. Note that one of the
         * collision entities is not named; it should be clear from the context
         * in which this was constructed.
         * 
         * @param ps
         *            The PhysicsSprite involved in the collision
         * @param c
         *            A description of the contact, in case it is useful
         */
        void go(final PhysicsSprite ps, Contact c);
    }

    /**
     * Physics body for this PhysicsSprite
     */
    Body mBody;

    /**
     * Track if the underlying body is a circle
     */
    private boolean mIsCircleBody;

    /**
     * Track if the underlying body is a box
     */
    private boolean mIsBoxBody;

    /**
     * Track if the underlying body is a polygon
     */
    private boolean mIsPolygonBody;

    /**
     * Track if the entity is currently being rendered. This is a proxy for
     * "is important to the rest of the game" and when it is false, we don't run
     * any updates on the PhysicsSprite
     */
    boolean mVisible = true;

    /**
     * Does the entity's image flip when the hero moves backwards?
     */
    private boolean mReverseFace = false;

    /**
     * We may opt to flip the image when it is moving in the -X direction. If
     * so, this tracks if the image is flipped, so that we draw its sprite
     * correctly.
     */
    private boolean mIsFlipped;

    /**
     * The z index of this entity. Valid range is [-2, 2]
     */
    int mZIndex = 0;

    /**
     * The dimensions of the PhysicsSprite... x is width, y is height
     */
    Vector2 mSize = new Vector2();

    /**
     * Does this entity follow a route? If so, the RouteDriver will be used to
     * advance the entity along its route.
     */
    private RouteDriver mRoute;

    /**
     * Text that game designer can modify to hold additional information
     */
    String mInfoText = "";

    /**
     * Some PhysicsSprites run custom code when they are touched. This is a
     * reference to the code to run.
     */
    GestureAction mGestureResponder;

    /**
     * When the camera follows the entity without centering on it, this gives us
     * the difference between the hero and camera
     */
    Vector2 mCameraOffset = new Vector2(0, 0);

    /**
     * Sometimes a hero collides with an obstacle, and should stick to it. In
     * that case, we create a pair of joints to connect the two entities. This
     * is the Distance joint that connects them (Note: for convenience, we store
     * the joints in a common parent class)
     */
    DistanceJoint mDJoint;

    /**
     * Sometimes a hero collides with an obstacle, and should stick to it. In
     * that case, we create a pair of joints to connect the two entities. This
     * is the Weld joint that connects them (Note: for convenience, we store the
     * joints in a common parent class)
     */
    WeldJoint mWJoint;

    /**
     * When we have PhysicsSprites stuck together, we might want to set a brief
     * delay before they can re-join. This field represents that delay time, in
     * milliseconds.
     */
    long mStickyDelay;

    /**
     * a sound to play when the obstacle is touched
     */
    Sound mTouchSound;

    /**
     * Animation support: this tracks the current state of the active animation
     * (if any)
     */
    AnimationDriver mAnimator;

    /**
     * Animation support: the cells of the default animation
     */
    Animation mDefaultAnimation;

    /**
     * Animation support: the cells of the disappearance animation
     */
    Animation mDisappearAnimation;

    /**
     * Animation support: the offset for placing the disappearance animation
     * relative to the disappearing sprite
     */
    final Vector2 mDisappearAnimateOffset = new Vector2();

    /**
     * Animation support: the width of the disappearance animation
     */
    Vector2 mDisappearAnimateSize = new Vector2();

    /**
     * A vector for computing hover placement
     */
    Vector3 mHover = new Vector3();

    /**
     * Track if heroes stick to this PhysicsSprite. The array has 4 positions,
     * corresponding to top, right, bottom, left
     */
    boolean[] mIsSticky = new boolean[4];

    /**
     * Sound to play when this disappears
     */
    protected Sound mDisappearSound;

    /**
     * Disable 3 of 4 sides of a PhysicsSprite, to allow walking through walls.
     * The value reflects the side that remains active. 0 is top, 1 is right, 2
     * is bottom, 3 is left
     */
    int mIsOneSided = -1;

    /**
     * Entities with a matching nonzero Id don't collide with each other
     */
    int mPassThroughId = 0;

    /**
     * A temporary vertex that we use when resizing
     */
    Vector2 tmpVert = new Vector2();

    /**
     * A definition for when we attach a revolute joint to this entity
     */
    RevoluteJointDef mRevJointDef;

    /**
     * A joint that allows this entity to revolve around another
     */
    Joint mRevJoint;

    /**
     * Create a new PhysicsSprite that does not yet have physics, but that has a
     * renderable picture
     * 
     * @param imgName
     *            The image to display
     * @param id
     *            The type of PhysicsSprite
     * @param width
     *            The width
     * @param height
     *            The height
     */
    PhysicsSprite(String imgName, float width, float height) {
        mAnimator = new AnimationDriver(imgName);
        mSize.x = width;
        mSize.y = height;
    }

    /**
     * Each descendant defines this to address any custom logic that we need to
     * deal with on a collision
     * 
     * @param other
     *            The other entity involved in the collision
     */
    abstract void onCollide(PhysicsSprite other, Contact contact);

    /**
     * Internal method for updating an entity's velocity, so that we can handle
     * its direction correctly
     * 
     * @param x
     *            The new x velocity
     * @param y
     *            The new y velocity
     */
    void updateVelocity(float x, float y) {
        // make sure it is moveable... heroes are already Dynamic, let's just
        // set everything else that is static to kinematic... that's probably
        // safest.
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        // Clobber any joints, or this won't be able to move
        if (mDJoint != null) {
            Level.sCurrent.mWorld.destroyJoint(mDJoint);
            mDJoint = null;
            Level.sCurrent.mWorld.destroyJoint(mWJoint);
            mWJoint = null;
        }
        mBody.setLinearVelocity(x, y);
    }

    /**
     * Whenever any class derived from this is touched, we want to play the
     * touchsound before we run the gesture responder.
     * 
     * @param touchVec
     *            The x/y/z coordinates of the touch
     * @return True if the event was handled, false otherwise
     */
    boolean onTap(Vector3 touchVec) {
        if (mTouchSound != null)
            mTouchSound.play();
        if (mGestureResponder != null) {
            mGestureResponder.onTap(touchVec);
            return true;
        }
        return false;
    }

    /**
     * Specify that this entity should have a rectangular physics shape
     * 
     * @param density
     *            Density of the entity
     * @param elasticity
     *            Elasticity of the entity
     * @param friction
     *            Friction of the entity
     * @param type
     *            Is this static or dynamic?
     * @param isProjectile
     *            Is this a fast-moving object
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom left corner
     */
    void setBoxPhysics(float density, float elasticity, float friction,
            BodyType type, boolean isProjectile, float x, float y) {
        PolygonShape boxPoly = new PolygonShape();
        boxPoly.setAsBox(mSize.x / 2, mSize.y / 2);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = Level.sCurrent.mWorld.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.density = density;
        fd.restitution = elasticity;
        fd.friction = friction;
        fd.shape = boxPoly;
        // NB: could use fd.filter to prevent some from colliding with others...
        mBody.createFixture(fd);

        // link the body to the sprite
        mBody.setUserData(this);
        boxPoly.dispose();

        if (isProjectile)
            mBody.setBullet(true);

        // remember this is a box
        mIsCircleBody = false;
        mIsBoxBody = true;
        mIsPolygonBody = false;
    }

    /**
     * Specify that this entity should have a polygon physics shape. You must
     * take extreme care when using this method. Polygon vertices must be given
     * in counter-clockwise order, and they must describe a convex shape.
     * 
     * @param density
     *            Density of the entity
     * @param elasticity
     *            Elasticity of the entity
     * @param friction
     *            Friction of the entity
     * @param type
     *            Is this static or dynamic?
     * @param isProjectile
     *            Is this a fast-moving object
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom left corner
     * @param vertices
     *            Up to 16 coordinates representing the vertexes of this
     *            polygon, listed as x0,y0,x1,y1,x2,y2,...
     */
    void setPolygonPhysics(float density, float elasticity, float friction,
            BodyType type, boolean isProjectile, float x, float y,
            float... vertices) {
        PolygonShape boxPoly = new PolygonShape();
        Vector2[] verts = new Vector2[vertices.length / 2];
        for (int i = 0; i < vertices.length; i += 2) {
            verts[i / 2] = new Vector2(vertices[i], vertices[i + 1]);
        }
        for (int i = 0; i < verts.length; ++i)
            Util.message("vert", "at " + verts[i].x + "," + verts[i].y);
        boxPoly.set(verts);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = Level.sCurrent.mWorld.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.density = density;
        fd.restitution = elasticity;
        fd.friction = friction;
        fd.shape = boxPoly;
        // NB: could use fd.filter to prevent some from colliding with others...
        mBody.createFixture(fd);

        // link the body to the sprite
        mBody.setUserData(this);
        boxPoly.dispose();

        if (isProjectile)
            mBody.setBullet(true);

        // remember this is a polygon
        mIsCircleBody = false;
        mIsBoxBody = false;
        mIsPolygonBody = true;
    }

    /**
     * Specify that this entity should have a circular physics shape
     * 
     * @param density
     *            Density of the entity
     * @param elasticity
     *            Elasticity of the entity
     * @param friction
     *            Friction of the entity
     * @param type
     *            Is this static or dynamic?
     * @param isProjectile
     *            Is this a fast-moving object
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom left corner
     * @param radius
     *            The radius of the underlying circle
     */
    void setCirclePhysics(float density, float elasticity, float friction,
            BodyType type, boolean isProjectile, float x, float y, float radius) {
        CircleShape c = new CircleShape();
        c.setRadius(radius);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = Level.sCurrent.mWorld.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.density = density;
        fd.restitution = elasticity;
        fd.friction = friction;
        fd.shape = c;
        // NB: could use fd.filter to prevent some from colliding with others...
        mBody.createFixture(fd);
        c.dispose();

        if (isProjectile)
            mBody.setBullet(true);

        // link the body to the sprite
        mBody.setUserData(this);

        // remember this is a box
        mIsCircleBody = true;
        mIsBoxBody = false;
        mIsPolygonBody = false;
    }

    /*
     * OVERRIDES FROM LOL.RENDERABLE
     */

    /**
     * Every time the world advances by a timestep, we call this code. It
     * updates the PhysicsSprite and draws it. User code should never call this.
     */
    @Override
    public void render(SpriteBatch sb, float delta) {
        // skip all rendering and updates if not visible
        if (mVisible) {
            // possibly run a route update
            if (mRoute != null && mVisible)
                mRoute.drive();

            // choose the default TextureRegion to show... this is how we
            // animate
            TextureRegion tr = mAnimator.getTr(delta);

            // now draw this sprite, flipping it if necessary
            Vector2 pos = mBody.getPosition();
            if (mReverseFace && mBody.getLinearVelocity().x < 0) {
                if (!mIsFlipped) {
                    tr.flip(true, false);
                    mIsFlipped = true;
                }
            } else if (mReverseFace && mBody.getLinearVelocity().x > 0) {
                if (mIsFlipped) {
                    tr.flip(true, false);
                    mIsFlipped = false;
                }
            }
            if (tr != null)
                sb.draw(tr, pos.x - mSize.x / 2, pos.y - mSize.y / 2,
                        mSize.x / 2, mSize.y / 2, mSize.x, mSize.y, 1, 1,
                        MathUtils.radiansToDegrees * mBody.getAngle());
        }
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Set any additional information for this sprite
     * 
     * @param text
     *            Text coming in from the programmer
     */
    public void setInfoText(String text) {
        mInfoText = text;
    }

    /**
     * Retrieve any additional information for this sprite
     * 
     * @return The string that the programmer provided
     */
    public String getInfoText() {
        return mInfoText;
    }

    /**
     * Make the camera follow the entity, but without centering the entity on
     * the screen
     * 
     * @param x
     *            Amount of x distance between entity and center
     * @param y
     *            Amount of y distance between entity and center
     */
    public void setCameraOffset(float x, float y) {
        mCameraOffset.x = x;
        mCameraOffset.y = y;
    }

    /**
     * Change whether this entity engages in physics collisions or not
     * 
     * @param state
     *            either true or false. true indicates that the object will
     *            participate in physics collisions. false indicates that it
     *            will not.
     */
    public void setCollisionEffect(boolean state) {
        // The default is for all fixtures of a PhysicsSprite have the same
        // sensor state
        for (Fixture f : mBody.getFixtureList())
            f.setSensor(!state);
    }

    /**
     * Allow the user to adjust the default physics settings (density,
     * elasticity, friction) for this entity
     * 
     * @param density
     *            New density of the entity
     * @param elasticity
     *            New elasticity of the entity
     * @param friction
     *            New friction of the entity
     */
    public void setPhysics(float density, float elasticity, float friction) {
        for (Fixture f : mBody.getFixtureList()) {
            f.setDensity(density);
            f.setRestitution(elasticity);
            f.setFriction(friction);
        }
        mBody.resetMassData();
    }

    /**
     * Indicate that this entity should not rotate due to torque
     */
    public void disableRotation() {
        mBody.setFixedRotation(true);
    }

    /**
     * Returns the X coordinate of this entity
     * 
     * @return x coordinate of bottom left corner
     */
    public float getXPosition() {
        return mBody.getPosition().x - mSize.x / 2;
    }

    /**
     * Returns the Y coordinate of this entity
     * 
     * @return y coordinate of bottom left corner
     */
    public float getYPosition() {
        return mBody.getPosition().y - mSize.y / 2;
    }

    /**
     * Returns the width of this entity
     * 
     * @return the entity's width
     */
    public float getWidth() {
        return mSize.x;
    }

    /**
     * Return the height of this entity
     * 
     * @return the entity's height
     */
    public float getHeight() {
        return mSize.y;
    }

    /**
     * Indicate that the entity should move with the tilt of the phone
     */
    public void setMoveByTilting() {
        // If we've already added this to the set of tiltable objects, don't do
        // it again
        if (Level.sCurrent.mTilt.mAccelEntities.contains(this))
            return;

        // make sure it is moveable, add it to the list of tilt entities
        if (mBody.getType() != BodyType.DynamicBody)
            mBody.setType(BodyType.DynamicBody);
        Level.sCurrent.mTilt.mAccelEntities.add(this);
        // turn off sensor behavior, so this collides with stuff...
        setCollisionEffect(true);
    }

    /**
     * Call this on an Entity to rotate it. Note that this works best on boxes.
     * 
     * @param rotation
     *            amount to rotate the Entity (in degrees)
     */
    public void setRotation(float rotation) {
        // NB: the javadocs say "radians", but this appears to want rotation in
        // degrees
        mBody.setTransform(mBody.getPosition(), rotation);
    }

    /**
     * Make an entity disappear
     * 
     * @param quiet
     *            True if the disappear sound should not be played
     */
    public void remove(boolean quiet) {
        // set it invisible immediately, so that future calls know to ignore
        // this PhysicsSprite
        mVisible = false;
        mBody.setActive(false);

        // play a sound when we remove this thing?
        if (mDisappearSound != null && !quiet)
            mDisappearSound.play();

        // This is a bit slimy... we draw an obstacle here, so that we have a
        // clean hook into the animation system, but we disable its physics
        if (mDisappearAnimation != null) {
            float x = getXPosition() + mDisappearAnimateOffset.x;
            float y = getYPosition() + mDisappearAnimateOffset.y;
            Obstacle o = Obstacle.makeAsBox(x, y, mDisappearAnimateSize.x,
                    mDisappearAnimateSize.y, "");
            o.mBody.setActive(false);
            o.setDefaultAnimation(mDisappearAnimation);
        }
    }

    /**
     * Add velocity to this entity
     * 
     * @param x
     *            Velocity in X dimension
     * @param y
     *            Velocity in Y dimension
     * @param immuneToPhysics
     *            Should never be true for heroes! This means that gravity won't
     *            affect the entity, and it can pass through other entities
     *            without colliding.
     */
    public void addVelocity(float x, float y, boolean immuneToPhysics) {
        // ensure this is a moveable entity
        if (mBody.getType() == BodyType.StaticBody)
            if (immuneToPhysics)
                mBody.setType(BodyType.KinematicBody);
            else
                mBody.setType(BodyType.DynamicBody);

        // Add to the velocity of the entity
        Vector2 v = mBody.getLinearVelocity();
        v.y += y;
        v.x += x;
        updateVelocity(v.x, v.y);
        // Disable sensor, or else this entity will go right through walls
        setCollisionEffect(true);
    }

    /**
     * Set the absolute velocity of this Entity
     * 
     * @param x
     *            Velocity in X dimension
     * @param y
     *            Velocity in Y dimension
     */
    public void setAbsoluteVelocity(float x, float y, boolean immuneToPhysics) {
        // ensure this is a moveable entity
        if (mBody.getType() == BodyType.StaticBody)
            if (immuneToPhysics)
                mBody.setType(BodyType.KinematicBody);
            else
                mBody.setType(BodyType.DynamicBody);

        // change its velocity
        updateVelocity(x, y);
        // Disable sensor, or else this entity will go right through walls
        setCollisionEffect(true);
    }

    /**
     * Set a dampening factor to cause a moving body to slow down without
     * colliding with anything
     * 
     * @param amount
     *            The amount of daming to apply
     */
    public void setDamping(float amount) {
        mBody.setLinearDamping(amount);
    }

    /**
     * Indicate that touching this object will cause some special code to run
     * 
     * @param id
     *            identifier for the trigger.
     * @param activationGoodies1
     *            Number of type-1 goodies that must be collected before it
     *            works
     * @param activationGoodies2
     *            Number of type-2 goodies that must be collected before it
     *            works
     * @param activationGoodies3
     *            Number of type-3 goodies that must be collected before it
     *            works
     * @param activationGoodies4
     *            Number of type-4 goodies that must be collected before it
     *            works
     * @param disapper
     *            True if the entity should disappear after the trigger
     *            completes
     */
    public void setTouchTrigger(final int id, int activationGoodies1,
            int activationGoodies2, int activationGoodies3,
            int activationGoodies4, final boolean disappear) {
        final int[] touchTriggerActivation = new int[] { activationGoodies1,
                activationGoodies2, activationGoodies3, activationGoodies4 };
        // set the code to run on touch
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                // check if we've got enough goodies
                boolean match = true;
                for (int i = 0; i < 4; ++i)
                    match &= touchTriggerActivation[i] <= Level.sCurrent.mScore.mGoodiesCollected[i];
                // if so, run the trigger
                if (match) {
                    if (disappear)
                        remove(false);
                    Lol.sGame.onTouchTrigger(id, Lol.sGame.mCurrLevelNum,
                            PhysicsSprite.this);
                }
                return true;
            }
        };
    }

    /**
     * Indicate that when this entity stops, we should run custom code
     * 
     * @param triggerId
     *            An id to attach to this event
     */
    public void setStopTrigger(final int triggerId) {
        Level.sCurrent.mRepeatEvents.add(new Action(){
            boolean moving = false;
            @Override
            public void go() {
                Vector2 speed = mBody.getLinearVelocity();
                if (!moving && (speed.x > 0 || speed.y > 0))
                    moving = true;
                else if (moving && speed.x == 0 && speed.y == 0)
                    Lol.sGame.onStopTrigger(triggerId, Lol.sGame.mCurrLevelNum, PhysicsSprite.this);
            }});
    }

    /**
     * Returns the X velocity of of this entity
     * 
     * @return float Velocity in X dimension
     */
    public float getXVelocity() {
        return mBody.getLinearVelocity().x;
    }

    /**
     * Returns the Y velocity of of this entity
     * 
     * @return float Velocity in Y dimension
     */
    public float getYVelocity() {
        return mBody.getLinearVelocity().y;
    }

    /**
     * Make this entity move according to a route. The entity can loop back to
     * the beginning of the route.
     * 
     * @param route
     *            The route to follow.
     * @param velocity
     *            speed at which to travel
     * @param loop
     *            Should this route loop continuously
     */
    public void setRoute(Route route, float velocity, boolean loop) {
        // This must be a KinematicBody!
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);

        // Create a RouteDriver to advance the entity's position according to
        // the route
        mRoute = new RouteDriver(route, velocity, loop, this);
    }

    /**
     * Make the entity continuously rotate. This is usually only useful for
     * fixed objects.
     * 
     * @param duration
     *            Time it takes to complete one rotation
     */
    public void setRotationSpeed(float duration) {
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        mBody.setAngularVelocity(duration);
    }

    /**
     * Call this on an entity to make it draggable. Be careful when dragging
     * things. If they are small, they will be hard to touch.
     */
    public void setCanDrag(boolean immuneToPhysics) {
        if (immuneToPhysics)
            mBody.setType(BodyType.KinematicBody);
        else
            mBody.setType(BodyType.DynamicBody);
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onDrag(Vector3 touchVec) {
                mBody.setTransform(touchVec.x, touchVec.y, mBody.getAngle());
                return true;
            }
        };
    }

    /**
     * Indicate that when the player touches this obstacle, we should play a
     * sound
     * 
     * @param sound
     *            The name of the sound file to play
     */
    public void setTouchSound(String sound) {
        mTouchSound = Media.getSound(sound);
    }

    /**
     * Call this on an Entity to make it pokeable. Poke the entity, then poke
     * the screen, and the entity will move to the location that was pressed.
     * Poke the entity twice in rapid succession to delete it.
     * 
     * @param deleteThresholdMillis
     *            If two touches happen within this many milliseconds, the
     *            entity will be deleted. Use 0 to disable this
     *            "delete by double-touch" feature.
     */
    public void setPokeToPlace(long deleteThresholdMillis) {
        // convert threshold to nanoseconds
        final long deleteThreshold = deleteThresholdMillis * 1000000;
        // set the code to run on touch
        mGestureResponder = new GestureAction() {
            long mLastPokeTime;

            @Override
            public boolean onTap(Vector3 tapLocation) {
                Lol.sGame.vibrate(100);
                long time = System.nanoTime();
                // double touch
                if ((time - mLastPokeTime) < deleteThreshold) {
                    // hide sprite, disable physics
                    mBody.setActive(false);
                    mVisible = false;
                    Level.sCurrent.mGestureResponder = null;
                    return true;
                }
                // repeat single-touch
                else {
                    mLastPokeTime = time;
                }
                // set a screen handler to detect when/where to move the entity
                Level.sCurrent.mGestureResponder = new GestureAction() {
                    @Override
                    public boolean onTap(Vector3 tapLocation) {
                        Lol.sGame.vibrate(100);
                        // move the object
                        mBody.setTransform(tapLocation.x, tapLocation.y,
                                mBody.getAngle());
                        // clear the Level responder
                        Level.sCurrent.mGestureResponder = null;
                        return true;
                    }
                };
                return true;
            }
        };
    }

    /**
     * Indicate that this entity can be flicked on the screen
     * 
     * @param dampFactor
     *            A value that is multiplied by the vector for the flick, to
     *            affect speed
     */
    public void setFlickable(final float dampFactor) {
        // make sure the body is a dynamic body, because it really doesn't make
        // sense to flick it otherwise
        if (mBody.getType() != BodyType.DynamicBody)
            mBody.setType(BodyType.DynamicBody);

        Level.sCurrent.mGestureResponder = new GestureAction() {
            @Override
            public boolean onFling(Vector3 touchVec) {
                if (Level.sCurrent.mHitSprite == PhysicsSprite.this) {
                    mHover = null;
                    updateVelocity((touchVec.x) * dampFactor, (touchVec.y)
                            * dampFactor);
                }
                return true;
            }
        };
    }

    /**
     * Configure an entity so that touching an arbitrary point on the screen
     * makes the entity move toward that point. The behavior is similar to
     * pokeToPlace, in that one touches the entity, then where she wants the
     * entity to go. However, this involves moving with velocity, instead of
     * teleporting
     * 
     * @param velocity
     *            The constant velocity for poke movement
     * @param oncePerTouch
     *            After starting a path, does the player need to re-select
     *            (re-touch) the entity before giving it a new destinaion point?
     */
    public void setPokePath(final float velocity, final boolean oncePerTouch) {
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                Lol.sGame.vibrate(5);
                Level.sCurrent.mGestureResponder = new GestureAction() {
                    @Override
                    public boolean onTap(Vector3 touchVec) {
                        Route r = new Route(2).to(getXPosition(),
                                getYPosition()).to(touchVec.x - mSize.x / 2,
                                touchVec.y - mSize.y / 2);
                        setAbsoluteVelocity(0, 0, false);
                        setRoute(r, velocity, false);
                        if (oncePerTouch)
                            Level.sCurrent.mGestureResponder = null;
                        return true;
                    }
                };
                return true;
            }
        };
    }

    /**
     * Configure an entity so that touching an arbitrary point on the screen
     * makes the entity move toward that point. The behavior is similar to
     * pokePath, except that as the finger moves, the entity keeps changing its
     * destination accordingly.
     * 
     * @param velocity
     *            The constant velocity for poke movement
     * @param oncePerTouch
     *            After starting a path, does the player need to re-select
     *            (re-touch) the entity before giving it a new destinaion point?
     * @param stopOnUp
     *            When the touch is released, should the entity stop moving, or
     *            continue in the same direction?
     */
    public void setFingerChase(final float velocity,
            final boolean oncePerTouch, final boolean stopOnUp) {
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                Lol.sGame.vibrate(5);
                Level.sCurrent.mGestureResponder = new GestureAction() {
                    @Override
                    public boolean onDown(Vector3 touchVec) {
                        Route r = new Route(2).to(getXPosition(),
                                getYPosition()).to(touchVec.x - mSize.x / 2,
                                touchVec.y - mSize.y / 2);
                        setAbsoluteVelocity(0, 0, false);
                        setRoute(r, velocity, false);
                        return true;
                    }

                    @Override
                    public boolean onUp(Vector3 touchVec) {
                        if (stopOnUp && mRoute != null)
                            mRoute.haltRoute();
                        if (oncePerTouch)
                            Level.sCurrent.mGestureResponder = null;
                        return true;
                    }

                    @Override
                    public boolean onPan(Vector3 touchVec, float deltaX,
                            float deltaY) {
                        return onDown(touchVec);
                    }

                    @Override
                    public boolean onPanStop(Vector3 touchVec) {
                        return onUp(touchVec);
                    }
                };
                return true;
            }
        };
    }

    /**
     * Save the animation sequence and start it right away
     * 
     * @param a
     *            The animation do display
     */
    public void setDefaultAnimation(Animation a) {
        mDefaultAnimation = a;
        // we'll assume we're using the default animation as our first
        // animation...
        mAnimator.setCurrentAnimation(mDefaultAnimation);
    }

    /**
     * Save an animation sequence for showing when we get rid of a sprite
     * 
     * @param a
     *            The animation to display
     * @param offsetX
     *            We can offset the animation from the bottom left of the sprite
     *            (useful if animation is larger than sprite dimensions). This
     *            is the x offset.
     * @param offsetY
     *            The Y offset (see offsetX for more information)
     * @param width
     *            The width of the frames of this animation
     * @param height
     *            The height of the frames of this animation
     */
    public void setDisappearAnimation(Animation a, float offsetX,
            float offsetY, float width, float height) {
        mDisappearAnimation = a;
        mDisappearAnimateOffset.x = offsetX;
        mDisappearAnimateOffset.y = offsetY;
        mDisappearAnimateSize.x = width;
        mDisappearAnimateSize.y = height;
    }

    /**
     * Indicate that this entity's image should be reversed when it is moving in
     * the negative x direction. For tilt, this only applies to the last hero
     * created. For velocity, it applies to everyone.
     */
    public void setCanFaceBackwards() {
        mReverseFace = true;
    }

    /**
     * Indicate that something should not appear quite yet...
     * 
     * @param delay
     *            How long to wait before displaying the thing
     */
    public void setAppearDelay(float delay) {
        mVisible = false;
        mBody.setActive(false);
        Timer.schedule(new Task() {
            @Override
            public void run() {
                mVisible = true;
                mBody.setActive(true);
            }
        }, delay);
    }

    /**
     * Indicate that something should disappear after a little while
     * 
     * @param delay
     *            How long to wait before hiding the thing
     * @param quiet
     *            true if the item should disappear quietly, false if it should
     *            play its disappear sound
     */
    public void setDisappearDelay(float delay, final boolean quiet) {
        Timer.schedule(new Task() {
            @Override
            public void run() {
                remove(quiet);
            }
        }, delay);
    }

    /**
     * Change the image being used by the entity
     * 
     * @param imgName
     *            The name of the new image file to use
     * @param index
     *            The index to use, in the case that the image was registered as
     *            animatable. When in doubt, use 0.
     */
    public void setImage(String imgName, int index) {
        mAnimator.updateImage(imgName);
        mAnimator.setIndex(index);
    }

    /**
     * Change the size of an entity, and/or change its position
     * 
     * @param x
     *            The new X coordinate of its bottom left corner
     * @param y
     *            The new Y coordinate of its bototm left corner
     * @param width
     *            The new width of the entity
     * @param height
     *            The new height of the entity
     */
    public void resize(float x, float y, float width, float height) {
        // To scale a polygon, we'll need to scaling factor here, so we can
        // manually scale each point
        float xscale = height / mSize.y;
        float yscale = width / mSize.x;
        // set new height and width
        mSize.x = width;
        mSize.y = height;
        // read old body information
        Body oldBody = mBody;
        // make a new body
        if (mIsCircleBody) {
            Fixture oldFix = oldBody.getFixtureList().get(0);
            setCirclePhysics(oldFix.getDensity(), oldFix.getRestitution(),
                    oldFix.getFriction(), oldBody.getType(),
                    oldBody.isBullet(), x, y, (width > height) ? width / 2
                            : height / 2);
        } else if (mIsBoxBody) {
            Fixture oldFix = oldBody.getFixtureList().get(0);
            setBoxPhysics(oldFix.getDensity(), oldFix.getRestitution(),
                    oldFix.getFriction(), oldBody.getType(),
                    oldBody.isBullet(), x, y);
        } else if (mIsPolygonBody) {
            Fixture oldFix = oldBody.getFixtureList().get(0);
            // we need to manually scale all the vertices
            PolygonShape ps = (PolygonShape) oldFix.getShape();
            float[] verts = new float[ps.getVertexCount() * 2];
            for (int i = 0; i < ps.getVertexCount(); ++i) {
                ps.getVertex(i, tmpVert);
                verts[2 * i] = tmpVert.x * xscale;
                verts[2 * i + 1] = tmpVert.y * yscale;
            }
            setPolygonPhysics(oldFix.getDensity(), oldFix.getRestitution(),
                    oldFix.getFriction(), oldBody.getType(),
                    oldBody.isBullet(), x, y, verts);
        }
        // clone forces
        mBody.setAngularVelocity(oldBody.getAngularVelocity());
        mBody.setTransform(mBody.getPosition(), oldBody.getAngle());
        mBody.setGravityScale(oldBody.getGravityScale());
        mBody.setLinearDamping(oldBody.getLinearDamping());
        mBody.setLinearVelocity(oldBody.getLinearVelocity());
        // disable the old body
        oldBody.setActive(false);
    }

    /**
     * Indicate that this entity should shrink over time
     * 
     * @param shrinkX
     *            The number of meters by which the X dimension should shrink
     *            each second
     * @param shrinkY
     *            The number of meters by which the Y dimension should shrink
     *            each second
     * @param keepCentered
     *            Should the entity's center point stay the same as it shrinks
     *            (true), or should its bottom left corner stay in the same
     *            position (false)
     */
    public void setShrinkOverTime(final float shrinkX, final float shrinkY,
            final boolean keepCentered) {
        final Task t = new Task() {
            @Override
            public void run() {
                if (mVisible) {
                    float x, y;
                    if (keepCentered) {
                        x = getXPosition() + shrinkX / 20 / 2;
                        y = getYPosition() + shrinkY / 20 / 2;
                    } else {
                        x = getXPosition();
                        y = getYPosition();
                    }
                    float w = mSize.x - shrinkX / 20;
                    float h = mSize.y - shrinkY / 20;
                    // if the area remains >0, resize it and schedule a timer to
                    // run again
                    if ((w > 0.05f) && (h > 0.05f)) {
                        resize(x, y, w, h);
                        Timer.schedule(this, .05f);
                    } else {
                        remove(false);
                    }
                }
            }
        };
        Timer.schedule(t, .05f);
    }

    /**
     * Indicate that this entity should hover at a specific location on the
     * screen, rather than being placed at some point on the level itself. Note
     * that the coordinates to this command are the center position of the
     * hovering entity. Also, be careful about using hover with zoom... hover is
     * relative to screen coordinates (pixels), not world coordinates, so it's
     * going to look funny to use this with zoom
     * 
     * @param x
     *            the X coordinate (in pixels) where the entity should appear
     * @param y
     *            the Y coordinate (in pixels) where the entity should appear
     */
    public void setHover(final int x, final int y) {
        mHover = new Vector3();
        Level.sCurrent.mRepeatEvents.add(new Action() {
            @Override
            public void go() {
                if (mHover == null)
                    return;
                mHover.x = x;
                mHover.y = y;
                mHover.z = 0;
                Level.sCurrent.mGameCam.unproject(mHover);
                mBody.setTransform(mHover.x, mHover.y, mBody.getAngle());
            }
        });
    }

    /**
     * Indicate that this entity should be immune to the force of gravity
     */
    public void setGravityDefy() {
        mBody.setGravityScale(0);
    }

    /**
     * Make this obstacle sticky, so that a hero will stick to it
     * 
     * @param top
     *            Is the top sticky?
     * @param right
     *            Is the right side sticky?
     * @param bottom
     *            Is the bottom sticky?
     * @param left
     *            Is the left side sticky?
     */
    public void setSticky(boolean top, boolean right, boolean bottom,
            boolean left) {
        mIsSticky = new boolean[] { top, right, bottom, left };
    }

    /**
     * Set the sound to play when this entity disappears
     * 
     * @param soundName
     *            Name of the sound file
     */
    public void setDisappearSound(String soundName) {
        mDisappearSound = Media.getSound(soundName);
    }

    /**
     * Indicate that touching this entity should make a hero throw a projectile
     * 
     * @param h
     *            The hero who should throw a projectile when this is touched
     */
    public void setTouchToThrow(final Hero h) {
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                Level.sCurrent.mProjectilePool.throwFixed(h);
                return true;
            }
        };
    }

    /**
     * Indicate that this obstacle only registers collisions on one side.
     * 
     * @param side
     *            The side that registers collisions. 0 is top, 1 is right, 2 is
     *            bottom, 3 is left, -1 means "none"
     */
    public void setOneSided(int side) {
        mIsOneSided = side;
    }

    /**
     * Indicate that this entity should not have collisions with any other
     * entity that has the same ID
     * 
     * @param id
     *            The number for this class of non-interacting entities
     */
    public void setPassThrough(int id) {
        mPassThroughId = id;
    }

    /**
     * By default, non-hero entities are not subject to gravity or forces until
     * they are given a path, velocity, or other form of motion. This lets an
     * entity be subject to forces... in practice, using this in a side-scroller
     * means the entity will fall to the ground.
     */
    public void setCanFall() {
        mBody.setType(BodyType.DynamicBody);
    }

    /**
     * Specify that this entity is supposed to chase another entity
     * 
     * @param speed
     *            The speed with which it chases the other entity
     * @param target
     *            The entity to chase
     * @param chaseInX
     *            Should the entity change its x velocity?
     * @param chaseInY
     *            Should the entity change its y velocity?
     */
    public void setChaseSpeed(final float speed, final PhysicsSprite target,
            final boolean chaseInX, final boolean chaseInY) {
        mBody.setType(BodyType.DynamicBody);
        Level.sCurrent.mRepeatEvents.add(new Action() {
            @Override
            public void go() {
                // don't chase something that isn't visible
                if (!target.mVisible)
                    return;
                // don't run if this sprite isn't visible
                if (!mVisible)
                    return;
                // compute vector between entities, and normalize it
                float x = target.mBody.getPosition().x - mBody.getPosition().x;
                float y = target.mBody.getPosition().y - mBody.getPosition().y;
                float denom = (float) Math.sqrt(x * x + y * y);
                x /= denom;
                y /= denom;
                // multiply by speed
                x *= speed;
                y *= speed;
                // remove changes for disabled directions, and boost the other
                // dimension a little bit
                if (!chaseInX) {
                    x = mBody.getLinearVelocity().x;
                    y *= 2;
                }
                if (!chaseInY) {
                    y = mBody.getLinearVelocity().y;
                    x *= 2;
                }
                // apply velocity
                updateVelocity(x, y);
            }
        });
    }

    /**
     * Indicate that this entity's rotation should be determined by the
     * direction in which it is traveling
     */
    public void setRotationByDirection() {
        Level.sCurrent.mRepeatEvents.add(new Action() {
            @Override
            public void go() {
                // handle rotating the hero based on the direction it faces
                if (mVisible) {
                    float x = mBody.getLinearVelocity().x;
                    float y = mBody.getLinearVelocity().y;
                    double angle = Math.atan2(y, x) + Math.atan2(-1, 0);
                    mBody.setTransform(mBody.getPosition(), (float) angle);
                }
            }
        });
    }

    /**
     * Set the z plane for this entity
     * 
     * @param zIndex
     *            The z plane. Values range from -2 to 2. The default is 0.
     */
    public void setZIndex(int zIndex) {
        assert (zIndex <= 2);
        assert (zIndex >= -2);
        Level.sCurrent.removeSprite(this, mZIndex);
        mZIndex = zIndex;
        Level.sCurrent.addSprite(this, mZIndex);
    }

    /**
     * Create a revolute joint between this entity and some other entity
     * 
     * @param anchor
     *            The entity around which this entity will rotate
     * @param anchorX
     *            The X coordinate (relative to the center of the entity) where
     *            the joint fuses to the anchor
     * @param anchorY
     *            The Y coordinate (relative to the center of the entity) where
     *            the joint fuses to the anchor
     * @param localAnchorX
     *            The X coordinate (relative to the center of the entity) where
     *            the joint fuses to this entity
     * @param localAnchorY
     *            The Y coordinate (relative to the center of the entity) where
     *            the joint fuses to this entity
     */
    public void setRevoluteJoint(PhysicsSprite anchor, float anchorX,
            float anchorY, float localAnchorX, float localAnchorY) {
        // make the body dynamic
        setCanFall();
        // create joint, connect anchors
        mRevJointDef = new RevoluteJointDef();
        mRevJointDef.bodyA = anchor.mBody;
        mRevJointDef.bodyB = mBody;
        mRevJointDef.localAnchorA.set(anchorX, anchorY);
        mRevJointDef.localAnchorB.set(localAnchorX, localAnchorY);
        // rotator and anchor don't collide
        mRevJointDef.collideConnected = false;
        mRevJointDef.referenceAngle = 0;
        mRevJointDef.enableLimit = false;
        mRevJoint = Level.sCurrent.mWorld.createJoint(mRevJointDef);
    }

    /**
     * Attach a motor to make a joint turn
     * 
     * @param motorSpeed
     *            Speed in radians per second
     * @param motorTorque
     *            torque of the motor... when in doubt, go with
     *            Float.POSITIVE_INFINITY
     */
    public void setRevoluteJointMotor(float motorSpeed, float motorTorque) {
        // destroy the previously created joint, change the definition,
        // re-create the joint
        Level.sCurrent.mWorld.destroyJoint(mRevJoint);
        mRevJointDef.enableMotor = true;
        mRevJointDef.motorSpeed = motorSpeed;
        mRevJointDef.maxMotorTorque = motorTorque;
        mRevJoint = Level.sCurrent.mWorld.createJoint(mRevJointDef);
    }

    /**
     * Set upper and lower bounds on the rotation of the joint
     * 
     * @param upper
     *            The upper bound in radians
     * @param lower
     *            The lower bound in radians
     */
    public void setRevoluteJointLimits(float upper, float lower) {
        // destroy the previously created joint, change the definition,
        // re-create the joint
        Level.sCurrent.mWorld.destroyJoint(mRevJoint);
        mRevJointDef.upperAngle = upper;
        mRevJointDef.lowerAngle = lower;
        mRevJointDef.enableLimit = true;
        mRevJoint = Level.sCurrent.mWorld.createJoint(mRevJointDef);
    }
}
