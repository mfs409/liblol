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
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.internals.AnimationDriver;
import edu.lehigh.cse.lol.internals.GestureAction;
import edu.lehigh.cse.lol.internals.LolAction;
import edu.lehigh.cse.lol.internals.Renderable;
import edu.lehigh.cse.lol.internals.RouteDriver;

/**
 * Actor is the base class upon which every game actor is built. Every actor has
 * a physics representation (rectangle, circle, or convex polygon). Actors
 * typically have an image associated with them, too, so that they have a visual
 * appearance during gameplay.
 *
 * A game should rarely deal with Actor objects directly, instead using Hero,
 * Goodie, Destination, Enemy, Obstacle, and Projectile objects.
 */
public abstract class Actor implements Renderable {
    /**
     * Animation support: the offset for placing the disappearance animation
     * relative to the disappearing actor
     */
    final Vector2 mDisappearAnimateOffset = new Vector2();
    /**
     * Physics body for this Actor
     */
    public Body mBody;
    /**
     * The dimensions of the Actor... x is width, y is height
     */
    public Vector2 mSize = new Vector2();
    /**
     * Sound to play when this disappears
     */
    protected Sound mDisappearSound;
    /**
     * Track if the actor is currently being rendered. This is a proxy for
     * "is important to the rest of the game" and when it is false, we don't run
     * any updates on the actor
     */
    boolean mVisible = true;

    /**
     * The z index of this actor. Valid range is [-2, 2]
     */
    int mZIndex = 0;
    /**
     * Text that game designer can modify to hold additional information
     */
    String mInfoText = "";
    /**
     * Some actors run custom code when they are touched. This is a reference to
     * the code to run.
     */
    GestureAction mGestureResponder;
    /**
     * When the camera follows the actor without centering on it, this gives us
     * the difference between the actor and camera
     */
    Vector2 mCameraOffset = new Vector2(0, 0);
    /**
     * Sometimes an actor collides with another actor, and should stick to it.
     * In that case, we create a pair of joints to connect the two actors. This
     * is the Distance joint that connects them
     */
    DistanceJoint mDJoint;
    /**
     * Sometimes an actor collides with another actor, and should stick to it.
     * In that case, we create a pair of joints to connect the two actors. This
     * is the Weld joint that connects them
     */
    WeldJoint mWJoint;
    /**
     * We allow the programmer to manually weld objects together. For it to
     * work, we need a local WeldJoint
     */
    WeldJoint mExplicitWeldJoint;
    /**
     * When we have actors stuck together, we might want to set a brief delay
     * before they can re-join. This field represents that delay time, in
     * milliseconds.
     */
    long mStickyDelay;
    /**
     * a sound to play when this actor is touched
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
     * Animation support: the cells of the animation to use when moving
     * backwards
     */
    Animation mDefaultReverseAnimation;
    /**
     * Animation support: the cells of the disappearance animation
     */
    Animation mDisappearAnimation;
    /**
     * Animation support: the dimensions of the disappearance animation
     */
    Vector2 mDisappearAnimateSize = new Vector2();
    /**
     * A vector for computing hover placement
     */
    Vector3 mHover = new Vector3();
    /**
     * Track if Heros stick to this Actor. The array has 4 positions,
     * corresponding to top, right, bottom, left
     */
    boolean[] mIsSticky = new boolean[4];
    /**
     * Disable 3 of 4 sides of a Actors, to allow walking through walls. The
     * value reflects the side that remains active. 0 is top, 1 is right, 2 is
     * bottom, 3 is left
     */
    int mIsOneSided = -1;
    /**
     * Actors with a matching nonzero Id don't collide with each other
     */
    int mPassThroughId = 0;
    /**
     * A temporary vertex that we use when resizing
     */
    Vector2 mTmpVert = new Vector2();
    /**
     * A definition for when we attach a revolute joint to this actor
     */
    RevoluteJointDef mRevJointDef;
    /**
     * A joint that allows this actor to revolve around another
     */
    Joint mRevJoint;
    /**
     * A definition for when we attach a distance joint to this actor
     */
    DistanceJointDef mDistJointDef;
    /**
     * A joint that allows this actor to stay within a fixed distance of another
     */
    Joint mDistJoint;
    /**
     * If this actor is chasing another actor, we track who is being chased via
     * this field
     */
    Actor mChaseTarget;
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
     * Does this Actor follow a route? If so, the RouteDriver will be used to
     * advance the actor along its route.
     */
    private RouteDriver mRoute;

    /**
     * Create a new actor that does not yet have physics, but that has a
     * renderable picture
     *
     * @param imgName The image to display
     * @param width   The width
     * @param height  The height
     */
    Actor(String imgName, float width, float height) {
        mAnimator = new AnimationDriver(imgName);
        mSize.x = width;
        mSize.y = height;
    }

    /**
     * Each descendant defines this to address any custom logic that we need to
     * deal with on a collision
     *
     * @param other The other actor involved in the collision
     */
    abstract void onCollide(Actor other, Contact contact);

    /**
     * Internal method for updating an actor's velocity, so that we can handle
     * its direction correctly
     *
     * @param x The new x velocity
     * @param y The new y velocity
     */
    void updateVelocity(float x, float y) {
        // make sure it is moveable... heroes are already Dynamic, let's just
        // set everything else that is static to kinematic... that's probably
        // safest.
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        // Clobber any joints, or this won't be able to move
        if (mDJoint != null) {
            Lol.sGame.mCurrentLevel.mWorld.destroyJoint(mDJoint);
            mDJoint = null;
            Lol.sGame.mCurrentLevel.mWorld.destroyJoint(mWJoint);
            mWJoint = null;
        }
        mBody.setLinearVelocity(x, y);
    }

    /**
     * Whenever any class derived from Actor is touched, we want to play the
     * touchsound before we run the gesture responder.
     *
     * @param touchVec The x/y/z coordinates of the touch
     * @return True if the event was handled, false otherwise
     */
    boolean onTap(Vector3 touchVec) {
        if (mTouchSound != null)
            mTouchSound.play(Facts.getGameFact("volume", 1));
        if (mGestureResponder != null) {
            mGestureResponder.onTap(touchVec);
            return true;
        }
        return false;
    }

    /**
     * Specify that this Actor should have a rectangular physics shape
     *
     * @param density      Density of the actor
     * @param elasticity   Elasticity of the actor
     * @param friction     Friction of the actor
     * @param type         Is this static or dynamic?
     * @param isProjectile Is this a fast-moving object
     * @param x            The X coordinate of the bottom left corner
     * @param y            The Y coordinate of the bottom left corner
     */
    void setBoxPhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile, float x,
                       float y) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(mSize.x / 2, mSize.y / 2);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = Lol.sGame.mCurrentLevel.mWorld.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.density = density;
        fd.restitution = elasticity;
        fd.friction = friction;
        fd.shape = shape;
        mBody.createFixture(fd);

        // link the body to the actor
        mBody.setUserData(this);
        shape.dispose();

        if (isProjectile)
            mBody.setBullet(true);

        // remember this is a box
        mIsCircleBody = false;
        mIsBoxBody = true;
        mIsPolygonBody = false;
    }

    /**
     * Specify that this Actor should have a polygon physics shape. You must
     * take extreme care when using this method. Polygon vertices must be given
     * in counter-clockwise order, and they must describe a convex shape.
     *
     * @param density      Density of the actor
     * @param elasticity   Elasticity of the actor
     * @param friction     Friction of the actor
     * @param type         Is this static or dynamic?
     * @param isProjectile Is this a fast-moving object
     * @param x            The X coordinate of the bottom left corner
     * @param y            The Y coordinate of the bottom left corner
     * @param vertices     Up to 16 coordinates representing the vertexes of this
     *                     polygon, listed as x0,y0,x1,y1,x2,y2,...
     */
    void setPolygonPhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile,
                           float x, float y, float... vertices) {
        PolygonShape shape = new PolygonShape();
        Vector2[] verts = new Vector2[vertices.length / 2];
        for (int i = 0; i < vertices.length; i += 2)
            verts[i / 2] = new Vector2(vertices[i], vertices[i + 1]);
        // print some debug info, since vertices are tricky
        for (Vector2 vert : verts) Util.message("vert", "at " + vert.x + "," + vert.y);
        shape.set(verts);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = Lol.sGame.mCurrentLevel.mWorld.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.density = density;
        fd.restitution = elasticity;
        fd.friction = friction;
        fd.shape = shape;
        mBody.createFixture(fd);

        // link the body to the actor
        mBody.setUserData(this);
        shape.dispose();

        if (isProjectile)
            mBody.setBullet(true);

        // remember this is a polygon
        mIsCircleBody = false;
        mIsBoxBody = false;
        mIsPolygonBody = true;
    }

    /**
     * Specify that this Actor should have a circular physics shape
     *
     * @param density      Density of the actor
     * @param elasticity   Elasticity of the actor
     * @param friction     Friction of the actor
     * @param type         Is this static or dynamic?
     * @param isProjectile Is this a fast-moving object
     * @param x            The X coordinate of the bottom left corner
     * @param y            The Y coordinate of the bottom left corner
     * @param radius       The radius of the underlying circle
     */
    void setCirclePhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile,
                          float x, float y, float radius) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = Lol.sGame.mCurrentLevel.mWorld.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.density = density;
        fd.restitution = elasticity;
        fd.friction = friction;
        fd.shape = shape;
        mBody.createFixture(fd);
        shape.dispose();

        if (isProjectile)
            mBody.setBullet(true);

        // link the body to the actor
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
     * updates the Actor and draws it. User code should never call this.
     */
    @Override
    public void render(SpriteBatch sb, float delta) {
        // skip all rendering and updates if not visible
        if (mVisible) {
            // possibly run a route update
            if (mRoute != null)
                mRoute.drive();

            // choose the default TextureRegion to show... this is how we
            // animate
            TextureRegion tr = mAnimator.getTr(delta);

            // now draw this actor, flipping the image it if necessary
            Vector2 pos = mBody.getPosition();
            if (mDefaultReverseAnimation != null && mBody.getLinearVelocity().x < 0) {
                if (mAnimator.mCurrentAnimation != mDefaultReverseAnimation) {
                    mAnimator.setCurrentAnimation(mDefaultReverseAnimation);
                }
            } else if (mDefaultReverseAnimation != null && mBody.getLinearVelocity().x > 0) {
                if (mAnimator.mCurrentAnimation == mDefaultReverseAnimation) {
                    if (mDefaultAnimation != null) {
                        mAnimator.setCurrentAnimation(mDefaultAnimation);
                    }
                }
            }
            if (tr != null) {
                sb.draw(tr, pos.x - mSize.x / 2, pos.y - mSize.y / 2, mSize.x / 2, mSize.y / 2, mSize.x, mSize.y, 1, 1,
                        MathUtils.radiansToDegrees * mBody.getAngle());
            }
        }
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Retrieve any additional information for this actor
     *
     * @return The string that the programmer provided
     */
    public String getInfoText() {
        return mInfoText;
    }

    /**
     * Set any additional information for this actor
     *
     * @param text Text coming in from the programmer
     */
    public void setInfoText(String text) {
        mInfoText = text;
    }

    /**
     * Make the camera follow the actor, but without centering the actor on the
     * screen
     *
     * @param x Amount of x distance between actor and center
     * @param y Amount of y distance between actor and center
     */
    public void setCameraOffset(float x, float y) {
        mCameraOffset.x = x;
        mCameraOffset.y = y;
    }

    /**
     * Change whether this actor engages in physics collisions or not
     *
     * @param state either true or false. true indicates that the object will
     *              participate in physics collisions. false indicates that it
     *              will not.
     */
    public void setCollisionsEnabled(boolean state) {
        // The default is for all fixtures of a actor have the same
        // sensor state
        for (Fixture f : mBody.getFixtureList())
            f.setSensor(!state);
    }

    /**
     * Adjust the default physics settings (density, elasticity, friction) for
     * this actor
     *
     * @param density    New density of the actor
     * @param elasticity New elasticity of the actor
     * @param friction   New friction of the actor
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
     * Indicate that this actor should not rotate due to torque
     */
    public void disableRotation() {
        mBody.setFixedRotation(true);
    }

    /**
     * Returns the X coordinate of this actor
     *
     * @return x coordinate of bottom left corner
     */
    public float getXPosition() {
        return mBody.getPosition().x - mSize.x / 2;
    }

    /**
     * Returns the Y coordinate of this actor
     *
     * @return y coordinate of bottom left corner
     */
    public float getYPosition() {
        return mBody.getPosition().y - mSize.y / 2;
    }

    /**
     * Change the position of an Actor
     * @param x The new X position
     * @param y The new Y position
     */
    public void setPosition(float x, float y) {
        mBody.setTransform(x+mSize.x/2, y+mSize.y/2, mBody.getAngle());
    }

    /**
     * Returns the width of this actor
     *
     * @return the actor's width
     */
    public float getWidth() {
        return mSize.x;
    }

    /**
     * Return the height of this actor
     *
     * @return the actor's height
     */
    public float getHeight() {
        return mSize.y;
    }

    /**
     * Indicate that the actor should move with the tilt of the phone
     */
    public void setMoveByTilting() {
        // If we've already added this to the set of tiltable objects, don't do
        // it again
        if (Lol.sGame.mCurrentLevel.mTilt.mAccelActors.contains(this))
            return;

        // make sure it is moveable, add it to the list of tilt actors
        if (mBody.getType() != BodyType.DynamicBody)
            mBody.setType(BodyType.DynamicBody);
        Lol.sGame.mCurrentLevel.mTilt.mAccelActors.add(this);
        // turn off sensor behavior, so this collides with stuff...
        setCollisionsEnabled(true);
    }

    /**
     * Use this to find the current rotation of an actor
     *
     * @return The rotation, in degrees
     */
    public float getRotation() {
        return mBody.getAngle();
    }

    /**
     * Call this on an actor to rotate it. Note that this works best on boxes.
     *
     * @param rotation amount to rotate the actor (in degrees)
     */
    public void setRotation(float rotation) {
        mBody.setTransform(mBody.getPosition(), rotation);
    }

    /**
     * Indicate whether the actor is currently visible or not.
     *
     * @return true if the actor is visible, false if it is currently in a
     * hidden state (i.e., because it has been collected, defeated, or
     * removed)
     */
    public boolean getVisible() {
        return mVisible;
    }

    /**
     * Make an actor disappear
     *
     * @param quiet True if the disappear sound should not be played
     */
    public void remove(boolean quiet) {
        // set it invisible immediately, so that future calls know to ignore
        // this actor
        mVisible = false;
        mBody.setActive(false);

        // play a sound when we remove this actor?
        if (mDisappearSound != null && !quiet)
            mDisappearSound.play(Facts.getGameFact("volume", 1));

        // This is a bit of a hack... to do a disappear animation after we've
        // removed the actor, we draw an obstacle, so that we have a clean hook
        // into the animation system, but we disable its physics
        if (mDisappearAnimation != null) {
            float x = getXPosition() + mDisappearAnimateOffset.x;
            float y = getYPosition() + mDisappearAnimateOffset.y;
            Obstacle o = Obstacle.makeAsBox(x, y, mDisappearAnimateSize.x, mDisappearAnimateSize.y, "");
            o.mBody.setActive(false);
            o.setDefaultAnimation(mDisappearAnimation);
        }
    }

    /**
     * Add velocity to this actor
     *
     * @param x               Velocity in X dimension
     * @param y               Velocity in Y dimension
     * @param immuneToPhysics Should never be true for heroes! This means that gravity won't
     *                        affect the actor, and it can pass through other actors without
     *                        colliding.
     */
    public void addVelocity(float x, float y, boolean immuneToPhysics) {
        // ensure this is a moveable actor
        if (mBody.getType() == BodyType.StaticBody)
            if (immuneToPhysics)
                mBody.setType(BodyType.KinematicBody);
            else
                mBody.setType(BodyType.DynamicBody);

        // Add to the velocity of the actor
        Vector2 v = mBody.getLinearVelocity();
        v.y += y;
        v.x += x;
        updateVelocity(v.x, v.y);
        // Disable sensor, or else this actor will go right through walls
        setCollisionsEnabled(true);
    }

    /**
     * Set the absolute velocity of this actor
     *
     * @param x Velocity in X dimension
     * @param y Velocity in Y dimension
     */
    public void setAbsoluteVelocity(float x, float y, boolean immuneToPhysics) {
        // ensure this is a moveable actor
        if (mBody.getType() == BodyType.StaticBody)
            if (immuneToPhysics)
                mBody.setType(BodyType.KinematicBody);
            else
                mBody.setType(BodyType.DynamicBody);

        // change its velocity
        updateVelocity(x, y);
        // Disable sensor, or else this actor will go right through walls
        setCollisionsEnabled(true);
    }

    /**
     * Set a dampening factor to cause a moving body to slow down without
     * colliding with anything
     *
     * @param amount The amount of damping to apply
     */
    public void setDamping(float amount) {
        mBody.setLinearDamping(amount);
    }

    /**
     * Set a dampening factor to cause a spinning body to decrease its rate of
     * spin
     *
     * @param amount The amount of damping to apply
     */
    public void setAngularDamping(float amount) {
        mBody.setAngularDamping(amount);
    }

    /**
     * Indicate that touching this object will cause some special code to run
     *
     * @param activationGoodies1 Number of type-1 goodies that must be collected before it
     *                           works
     * @param activationGoodies2 Number of type-2 goodies that must be collected before it
     *                           works
     * @param activationGoodies3 Number of type-3 goodies that must be collected before it
     *                           works
     * @param activationGoodies4 Number of type-4 goodies that must be collected before it
     *                           works
     * @param disappear          True if the actor should disappear when the callback runs
     * @param callback           The callback to run when the actor is touched
     */
    public void setTouchCallback(int activationGoodies1, int activationGoodies2, int activationGoodies3,
                                 int activationGoodies4, final boolean disappear, final LolCallback callback) {
        final int[] touchCallbackActivation = new int[]{activationGoodies1, activationGoodies2, activationGoodies3,
                activationGoodies4};
        // set the code to run on touch
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                // check if we've got enough goodies
                boolean match = true;
                for (int i = 0; i < 4; ++i)
                    match &= touchCallbackActivation[i] <= Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[i];
                // if so, run the callback
                if (match) {
                    if (disappear)
                        remove(false);
                    callback.mAttachedActor = Actor.this;
                    callback.onEvent();
                }
                return true;
            }
        };
    }

    /**
     * Indicate that when this actor stops, we should run custom code
     *
     * @param callback The callback to run when the actor stops
     */
    public void setStopCallback(final LolCallback callback) {
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            boolean moving = false;

            @Override
            public void go() {
                Vector2 speed = mBody.getLinearVelocity();
                if (!moving && (Math.abs(speed.x) > 0 || Math.abs(speed.y) > 0))
                    moving = true;
                else if (moving && speed.x == 0 && speed.y == 0) {
                    callback.mAttachedActor = Actor.this;
                    callback.onEvent();
                    moving = false;
                }
            }
        });
    }

    /**
     * Returns the X velocity of of this actor
     *
     * @return Velocity in X dimension
     */
    public float getXVelocity() {
        return mBody.getLinearVelocity().x;
    }

    /**
     * Returns the Y velocity of of this actor
     *
     * @return Velocity in Y dimension
     */
    public float getYVelocity() {
        return mBody.getLinearVelocity().y;
    }

    /**
     * Make this actor move according to a route. The actor can loop back to the
     * beginning of the route.
     *
     * @param route    The route to follow.
     * @param velocity speed at which to travel
     * @param loop     Should this route loop continuously?
     */
    public void setRoute(Route route, float velocity, boolean loop) {
        // This must be a KinematicBody or a Dynamic Body!
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);

        // Create a RouteDriver to advance the actor's position according to
        // the route
        mRoute = new RouteDriver(route, velocity, loop, this);
    }

    /**
     * Make the actor continuously rotate. This is usually only useful for fixed
     * objects.
     *
     * @param duration Time it takes to complete one rotation
     */
    public void setRotationSpeed(float duration) {
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        mBody.setAngularVelocity(duration);
    }

    /**
     * Call this on an actor to make it draggable. Be careful when dragging
     * things. If they are small, they will be hard to touch.
     *
     * @param immuneToPhysics Indicate whether the actor should pass through other objects
     *                        or collide with them
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
     * @param sound The name of the sound file to play
     */
    public void setTouchSound(String sound) {
        mTouchSound = Media.getSound(sound);
    }

    /**
     * Call this on an actor to make it pokeable. Poke the actor, then poke the
     * screen, and the actor will move to the location that was pressed. Poke
     * the actor twice in rapid succession to delete it.
     *
     * @param deleteThresholdMillis If two touches happen within this many milliseconds, the actor
     *                              will be deleted. Use 0 to disable this
     *                              "delete by double-touch" feature.
     */
    public void setPokeToPlace(long deleteThresholdMillis) {
        // convert threshold to nanoseconds
        final long deleteThreshold = deleteThresholdMillis;
        // set the code to run on touch
        mGestureResponder = new GestureAction() {
            long mLastPokeTime;
            boolean mEnabled = true;

            @Override
            public boolean onTap(Vector3 tapLocation) {
                if (!mEnabled)
                    return false;
                Lol.sGame.vibrate(100);
                long time = System.currentTimeMillis();
                // double touch
                if ((time - mLastPokeTime) < deleteThreshold) {
                    // hide actor, disable physics
                    mBody.setActive(false);
                    mVisible = false;
                    mEnabled = false;
                    return true;
                }
                // repeat single-touch
                else {
                    mLastPokeTime = time;
                }
                // set a screen handler to detect when/where to move the actor
                Lol.sGame.mCurrentLevel.mGestureResponders.add(new GestureAction() {
                    boolean mEnabled = true;

                    @Override
                    public boolean onTap(Vector3 tapLocation) {
                        if (!mEnabled || !mVisible)
                            return false;
                        Lol.sGame.vibrate(100);
                        // move the object
                        mBody.setTransform(tapLocation.x, tapLocation.y, mBody.getAngle());
                        // clear the Level responder
                        mEnabled = false;
                        return true;
                    }
                });
                return true;
            }
        };
    }

    /**
     * Indicate that this actor can be flicked on the screen
     *
     * @param dampFactor A value that is multiplied by the vector for the flick, to
     *                   affect speed
     */
    public void setFlickable(final float dampFactor) {
        // make sure the body is a dynamic body, because it really doesn't make
        // sense to flick it otherwise
        if (mBody.getType() != BodyType.DynamicBody)
            mBody.setType(BodyType.DynamicBody);

        Lol.sGame.mCurrentLevel.mGestureResponders.add(new GestureAction() {
            @Override
            public boolean onFling(Vector3 touchVec) {
                // note: may need to disable hovering
                if (Lol.sGame.mCurrentLevel.mHitActor == Actor.this) {
                    mHover = null;
                    updateVelocity((touchVec.x) * dampFactor, (touchVec.y) * dampFactor);
                }
                return true;
            }
        });
    }

    /**
     * Configure an actor so that touching an arbitrary point on the screen
     * makes the actor move toward that point. The behavior is similar to
     * pokeToPlace, in that one touches the actor, then where she wants the
     * actor to go. However, this involves moving with velocity, instead of
     * teleporting
     *
     * @param velocity     The constant velocity for poke movement
     * @param oncePerTouch After starting a path, does the player need to re-select
     *                     (re-touch) the actor before giving it a new destinaion point?
     */
    public void setPokePath(final float velocity, final boolean oncePerTouch) {
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                Lol.sGame.vibrate(5);
                Lol.sGame.mCurrentLevel.mGestureResponders.add(new GestureAction() {
                    boolean mEnabled = true;

                    @Override
                    public boolean onTap(Vector3 touchVec) {
                        if (!mEnabled)
                            return false;
                        Route r = new Route(2).to(getXPosition(), getYPosition()).to(touchVec.x - mSize.x / 2,
                                touchVec.y - mSize.y / 2);
                        setAbsoluteVelocity(0, 0, false);
                        setRoute(r, velocity, false);
                        if (oncePerTouch)
                            mEnabled = false;
                        return true;
                    }
                });
                return true;
            }
        };
    }

    /**
     * Configure an actor so that touching an arbitrary point on the screen
     * makes the actor move toward that point. The behavior is similar to
     * pokePath, except that as the finger moves, the actor keeps changing its
     * destination accordingly.
     *
     * @param velocity     The constant velocity for poke movement
     * @param oncePerTouch After starting a path, does the player need to re-select
     *                     (re-touch) the actor before giving it a new destinaion point?
     * @param stopOnUp     When the touch is released, should the actor stop moving, or
     *                     continue in the same direction?
     */
    public void setFingerChase(final float velocity, final boolean oncePerTouch, final boolean stopOnUp) {
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                Lol.sGame.vibrate(5);
                Lol.sGame.mCurrentLevel.mGestureResponders.add(new GestureAction() {
                    boolean mEnabled = true;

                    @Override
                    public boolean onDown(Vector3 touchVec) {
                        if (!mEnabled)
                            return false;
                        Route r = new Route(2).to(getXPosition(), getYPosition()).to(touchVec.x - mSize.x / 2,
                                touchVec.y - mSize.y / 2);
                        setAbsoluteVelocity(0, 0, false);
                        setRoute(r, velocity, false);
                        return true;
                    }

                    @Override
                    public boolean onUp(Vector3 touchVec) {
                        if (!mEnabled)
                            return false;
                        if (stopOnUp && mRoute != null)
                            mRoute.haltRoute();
                        if (oncePerTouch)
                            mEnabled = false;
                        return true;
                    }

                    @Override
                    public boolean onPan(Vector3 touchVec, float deltaX, float deltaY) {
                        if (!mEnabled)
                            return false;
                        return onDown(touchVec);
                    }

                    @Override
                    public boolean onPanStop(Vector3 touchVec) {
                        if (!mEnabled)
                            return false;
                        return onUp(touchVec);
                    }
                });
                return true;
            }
        };
    }

    /**
     * Save the animation sequence and start it right away
     *
     * @param a The animation to display
     */
    public void setDefaultAnimation(Animation a) {
        mDefaultAnimation = a;
        // we'll assume we're using the default animation as our first
        // animation...
        mAnimator.setCurrentAnimation(mDefaultAnimation);
    }

    /**
     * Save the animation sequence that we'll use when the actor is moving in
     * the negative X direction
     *
     * @param a The animation to display
     */
    public void setDefaultReverseAnimation(Animation a) {
        mDefaultReverseAnimation = a;
    }

    /**
     * Save an animation sequence for showing when we get rid of a actor
     *
     * @param a       The animation to display
     * @param offsetX We can offset the animation from the bottom left of the actor
     *                (useful if animation is larger than actor dimensions). This is
     *                the x offset.
     * @param offsetY The Y offset (see offsetX for more information)
     * @param width   The width of the frames of this animation
     * @param height  The height of the frames of this animation
     */
    public void setDisappearAnimation(Animation a, float offsetX, float offsetY, float width, float height) {
        mDisappearAnimation = a;
        mDisappearAnimateOffset.x = offsetX;
        mDisappearAnimateOffset.y = offsetY;
        mDisappearAnimateSize.x = width;
        mDisappearAnimateSize.y = height;
    }

    /**
     * Indicate that something should not appear quite yet...
     *
     * @param delay How long to wait before displaying the thing
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
     * @param delay How long to wait before hiding the thing
     * @param quiet true if the item should disappear quietly, false if it should
     *              play its disappear sound
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
     * Change the image being used to display the actor
     *
     * @param imgName The name of the new image file to use
     * @param index   The index to use, in the case that the image was registered as
     *                animatable. When in doubt, use 0.
     */
    public void setImage(String imgName, int index) {
        mAnimator.updateImage(imgName);
        mAnimator.setIndex(index);
    }

    /**
     * Change the size of an actor, and/or change its position
     *
     * @param x      The new X coordinate of its bottom left corner
     * @param y      The new Y coordinate of its bototm left corner
     * @param width  The new width of the actor
     * @param height The new height of the actor
     */
    public void resize(float x, float y, float width, float height) {
        // To scale a polygon, we'll need a scaling factor, so we can
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
            setCirclePhysics(oldFix.getDensity(), oldFix.getRestitution(), oldFix.getFriction(), oldBody.getType(),
                    oldBody.isBullet(), x, y, (width > height) ? width / 2 : height / 2);
        } else if (mIsBoxBody) {
            Fixture oldFix = oldBody.getFixtureList().get(0);
            setBoxPhysics(oldFix.getDensity(), oldFix.getRestitution(), oldFix.getFriction(), oldBody.getType(),
                    oldBody.isBullet(), x, y);
        } else if (mIsPolygonBody) {
            Fixture oldFix = oldBody.getFixtureList().get(0);
            // we need to manually scale all the vertices
            PolygonShape ps = (PolygonShape) oldFix.getShape();
            float[] verts = new float[ps.getVertexCount() * 2];
            for (int i = 0; i < ps.getVertexCount(); ++i) {
                ps.getVertex(i, mTmpVert);
                verts[2 * i] = mTmpVert.x * xscale;
                verts[2 * i + 1] = mTmpVert.y * yscale;
            }
            setPolygonPhysics(oldFix.getDensity(), oldFix.getRestitution(), oldFix.getFriction(), oldBody.getType(),
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
     * Indicate that this actor should shrink over time
     *
     * @param shrinkX      The number of meters by which the X dimension should shrink
     *                     each second
     * @param shrinkY      The number of meters by which the Y dimension should shrink
     *                     each second
     * @param keepCentered Should the actor's center point stay the same as it shrinks
     *                     (true), or should its bottom left corner stay in the same
     *                     position (false)
     */
    public void setShrinkOverTime(final float shrinkX, final float shrinkY, final boolean keepCentered) {
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
     * Indicate that this actor should hover at a specific location on the
     * screen, rather than being placed at some point on the level itself. Note
     * that the coordinates to this command are the center position of the
     * hovering actor. Also, be careful about using hover with zoom... hover is
     * relative to screen coordinates (pixels), not world coordinates, so it's
     * going to look funny to use this with zoom
     *
     * @param x the X coordinate (in pixels) where the actor should appear
     * @param y the Y coordinate (in pixels) where the actor should appear
     */
    public void setHover(final int x, final int y) {
        mHover = new Vector3();
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (mHover == null)
                    return;
                mHover.x = x;
                mHover.y = y;
                mHover.z = 0;
                Lol.sGame.mCurrentLevel.mGameCam.unproject(mHover);
                mBody.setTransform(mHover.x, mHover.y, mBody.getAngle());
            }
        });
    }

    /**
     * Indicate that this actor should be immune to the force of gravity
     */
    public void setGravityDefy() {
        mBody.setGravityScale(0);
    }

    /**
     * Make this actor sticky, so that another actor will stick to it
     *
     * @param top    Is the top sticky?
     * @param right  Is the right side sticky?
     * @param bottom Is the bottom sticky?
     * @param left   Is the left side sticky?
     */
    public void setSticky(boolean top, boolean right, boolean bottom, boolean left) {
        mIsSticky = new boolean[]{top, right, bottom, left};
    }

    /**
     * Set the sound to play when this actor disappears
     *
     * @param soundName Name of the sound file
     */
    public void setDisappearSound(String soundName) {
        mDisappearSound = Media.getSound(soundName);
    }

    /**
     * Indicate that touching this actor should make a hero throw a projectile
     *
     * @param h         The hero who should throw a projectile when this is touched
     * @param offsetX   specifies the x distance between the bottom left of the
     *                  projectile and the bottom left of the hero throwing the
     *                  projectile
     * @param offsetY   specifies the y distance between the bottom left of the
     *                  projectile and the bottom left of the hero throwing the
     *                  projectile
     * @param velocityX The X velocity of the projectile when it is thrown
     * @param velocityY The Y velocity of the projectile when it is thrown
     */
    public void setTouchToThrow(final Hero h, final float offsetX, final float offsetY, final float velocityX,
                                final float velocityY) {
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                Lol.sGame.mCurrentLevel.mProjectilePool.throwFixed(h, offsetX, offsetY, velocityX, velocityY);
                return true;
            }
        };
    }

    /**
     * Indicate that this obstacle only registers collisions on one side.
     *
     * @param side The side that registers collisions. 0 is top, 1 is right, 2 is
     *             bottom, 3 is left, -1 means "none"
     */
    public void setOneSided(int side) {
        mIsOneSided = side;
    }

    /**
     * Indicate that this actor should not have collisions with any other actor
     * that has the same ID
     *
     * @param id The number for this class of non-interacting actors
     */
    public void setPassThrough(int id) {
        mPassThroughId = id;
    }

    /**
     * By default, non-hero actors are not subject to gravity or forces until
     * they are given a path, velocity, or other form of motion. This lets an
     * actor be subject to forces... in practice, using this in a side-scroller
     * means the actor will fall to the ground.
     */
    public void setCanFall() {
        mBody.setType(BodyType.DynamicBody);
    }

    /**
     * Specify that this actor is supposed to chase another actor
     *
     * @param speed    The speed with which it chases the other actor
     * @param target   The actor to chase
     * @param chaseInX Should the actor change its x velocity?
     * @param chaseInY Should the actor change its y velocity?
     */
    public void setChaseSpeed(final float speed, final Actor target, final boolean chaseInX, final boolean chaseInY) {
        mChaseTarget = target;
        mBody.setType(BodyType.DynamicBody);
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                // don't chase something that isn't visible
                if (!target.mVisible)
                    return;
                // don't run if this actor isn't visible
                if (!mVisible)
                    return;
                // compute vector between actors, and normalize it
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
     * Specify that this actor is supposed to chase another actor, but using
     * fixed X and Y velocities
     *
     * @param target     The actor to chase
     * @param xMagnitude The magnitude in the x direction, if ignoreX is false
     * @param yMagnitude The magnitude in the y direction, if ignoreY is false
     * @param ignoreX    False if we should apply xMagnitude, true if we should keep
     *                   the hero's existing X velocity
     * @param ignoreY    False if we should apply yMagnitude, true if we should keep
     *                   the hero's existing Y velocity
     */
    public void setChaseFixedMagnitude(final Actor target, final float xMagnitude, final float yMagnitude,
                                       final boolean ignoreX, final boolean ignoreY) {
        mChaseTarget = target;
        mBody.setType(BodyType.DynamicBody);
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                // don't chase something that isn't visible
                if (!target.mVisible)
                    return;
                // don't run if this actor isn't visible
                if (!mVisible)
                    return;
                // determine directions for X and Y
                int xDir = (target.getXPosition() > getXPosition()) ? 1 : -1;
                int yDir = (target.getYPosition() > getYPosition()) ? 1 : -1;
                float x = (ignoreX) ? getXVelocity() : xDir * xMagnitude;
                float y = (ignoreY) ? getYVelocity() : yDir * yMagnitude;
                // apply velocity
                updateVelocity(x, y);
            }
        });
    }

    /**
     * Get the actor being chased by this actor
     *
     * @return The actor being chased
     */
    public Actor getChaseactor() {
        return mChaseTarget;
    }

    /**
     * Indicate that this actor's rotation should be determined by the direction
     * in which it is traveling
     */
    public void setRotationByDirection() {
        Lol.sGame.mCurrentLevel.mRepeatEvents.add(new LolAction() {
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
     * Set the z plane for this actor
     *
     * @param zIndex The z plane. Values range from -2 to 2. The default is 0.
     */
    public void setZIndex(int zIndex) {
        assert (zIndex <= 2);
        assert (zIndex >= -2);
        Lol.sGame.mCurrentLevel.removeActor(this, mZIndex);
        mZIndex = zIndex;
        Lol.sGame.mCurrentLevel.addActor(this, mZIndex);
    }

    /**
     * Create a revolute joint between this actor and some other actor. Note
     * that both actors need to have some mass (density &gt; 0) or else this won't
     * work.
     *
     * @param anchor       The actor around which this actor will rotate
     * @param anchorX      The X coordinate (relative to the center of the actor) where
     *                     the joint fuses to the anchor
     * @param anchorY      The Y coordinate (relative to the center of the actor) where
     *                     the joint fuses to the anchor
     * @param localAnchorX The X coordinate (relative to the center of the actor) where
     *                     the joint fuses to this actor
     * @param localAnchorY The Y coordinate (relative to the center of the actor) where
     *                     the joint fuses to this actor
     */
    public void setRevoluteJoint(Actor anchor, float anchorX, float anchorY, float localAnchorX, float localAnchorY) {
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
        mRevJoint = Lol.sGame.mCurrentLevel.mWorld.createJoint(mRevJointDef);
    }

    /**
     * Attach a motor to make a joint turn
     *
     * @param motorSpeed  Speed in radians per second
     * @param motorTorque torque of the motor... when in doubt, go with
     *                    Float.POSITIVE_INFINITY
     */
    public void setRevoluteJointMotor(float motorSpeed, float motorTorque) {
        // destroy the previously created joint, change the definition,
        // re-create the joint
        Lol.sGame.mCurrentLevel.mWorld.destroyJoint(mRevJoint);
        mRevJointDef.enableMotor = true;
        mRevJointDef.motorSpeed = motorSpeed;
        mRevJointDef.maxMotorTorque = motorTorque;
        mRevJoint = Lol.sGame.mCurrentLevel.mWorld.createJoint(mRevJointDef);
    }

    /**
     * Set upper and lower bounds on the rotation of the joint
     *
     * @param upper The upper bound in radians
     * @param lower The lower bound in radians
     */
    public void setRevoluteJointLimits(float upper, float lower) {
        // destroy the previously created joint, change the definition,
        // re-create the joint
        Lol.sGame.mCurrentLevel.mWorld.destroyJoint(mRevJoint);
        mRevJointDef.upperAngle = upper;
        mRevJointDef.lowerAngle = lower;
        mRevJointDef.enableLimit = true;
        mRevJoint = Lol.sGame.mCurrentLevel.mWorld.createJoint(mRevJointDef);
    }

    /**
     * Create a weld joint between this actor and some other actor, to force the
     * actors to stick together.
     *
     * @param other  The actor that will be fused to this actor
     * @param otherX The X coordinate (relative to the center of the actor) where
     *               the joint fuses to the other actor
     * @param otherY The Y coordinate (relative to the center of the actor) where
     *               the joint fuses to the other actor
     * @param localX The X coordinate (relative to the center of the actor) where
     *               the joint fuses to this actor
     * @param localY The Y coordinate (relative to the center of the actor) where
     *               the joint fuses to this actor
     * @param angle  The angle between the actors
     */
    public void setWeldJoint(Actor other, float otherX, float otherY, float localX, float localY, float angle) {
        WeldJointDef w = new WeldJointDef();
        w.bodyA = mBody;
        w.bodyB = other.mBody;
        w.localAnchorA.set(localX, localY);
        w.localAnchorB.set(otherX, otherY);
        w.referenceAngle = angle;
        w.collideConnected = false;
        mExplicitWeldJoint = (WeldJoint) Lol.sGame.mCurrentLevel.mWorld.createJoint(w);
    }

    /**
     * Create a distance joint between this actor and some other actor
     *
     * @param anchor       The actor to which this actor is connected
     * @param anchorX      The X coordinate (relative to the center of the actor) where
     *                     the joint fuses to the anchor
     * @param anchorY      The Y coordinate (relative to the center of the actor) where
     *                     the joint fuses to the anchor
     * @param localAnchorX The X coordinate (relative to the center of the actor) where
     *                     the joint fuses to this actor
     * @param localAnchorY The Y coordinate (relative to the center of the actor) where
     *                     the joint fuses to this actor
     */
    public void setDistanceJoint(Actor anchor, float anchorX, float anchorY, float localAnchorX, float localAnchorY) {
        // make the body dynamic
        setCanFall();

        // set up a joint so the head can't move too far
        mDistJointDef = new DistanceJointDef();
        mDistJointDef.bodyA = anchor.mBody;
        mDistJointDef.bodyB = mBody;
        mDistJointDef.localAnchorA.set(anchorX, anchorY);
        mDistJointDef.localAnchorB.set(localAnchorX, localAnchorY);
        mDistJointDef.collideConnected = false;
        mDistJointDef.dampingRatio = 0.1f;
        mDistJointDef.frequencyHz = 2;

        mDistJoint = Lol.sGame.mCurrentLevel.mWorld.createJoint(mDistJointDef);
    }

    /**
     * Modify an existing distance joint by changing the distance between the actors
     * 
     * @param newDist The new distance between the actors involved in the joint
     */
    public void setDistance(float newDist) {
    	DistanceJoint dj = (DistanceJoint) mDistJoint;
    	dj.setLength(newDist);
    }
    
    /**
     * In some cases, we need to force an actor to have a kinematic body type
     */
    public void setKinematic() {
        if (mBody.getType() != BodyType.KinematicBody)
            mBody.setType(BodyType.KinematicBody);
    }
}
