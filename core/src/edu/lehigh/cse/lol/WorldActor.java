/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

/**
 * WorldActor is the base class upon which every actor in the main game is built. Every actor has a
 * physics representation (rectangle, circle, or convex polygon). Actors typically have an image
 * associated with them, too, so that they have a visual appearance during gameplay.
 * <p>
 * A game should rarely deal with WorldActor objects directly, instead using Hero, Goodie,
 * Destination, Enemy, Obstacle, and Projectile objects.
 */
public abstract class WorldActor extends BaseActor {
    /// A reference to the top-level Lol object
    final Lol mGame;
    /// Some actors run custom code when they are touched. This is a reference to the code to run.
    TouchEventHandler mDragHandler;
    /// When the camera follows the actor without centering on it, this gives us the difference
    /// between the actor and camera
    Vector2 mCameraOffset = new Vector2(0, 0);
    /// Sometimes an actor collides with another actor, and should stick to it. In that case, we
    /// create two joints to connect the two actors. This is the Distance joint that connects them
    DistanceJoint mDJoint;
    /// Sometimes an actor collides with another actor, and should stick to it.  In that case, we
    /// create two joints to connect the two actors. This is the Weld joint that connects them
    WeldJoint mWJoint;
    /// We allow the programmer to manually weld objects together. For it to work, we need a local
    /// WeldJoint
    private WeldJoint mExplicitWeldJoint;
    /// When we have actors stuck together, we might want to set a brief delay before they can
    /// re-join. This field represents that delay time, in milliseconds.
    long mStickyDelay;
    /// A vector for computing hover placement
    Vector3 mHover = new Vector3();
    /// Track if Heros stick to this WorldActor. The array has 4 positions, corresponding to top,
    /// right, bottom, left
    boolean[] mIsSticky = new boolean[4];
    /// Disable 3 of 4 sides of a Actors, to allow walking through walls. The value reflects the
    /// side that remains active. 0 is top, 1 is right, 2 is bottom, 3 is left
    int mIsOneSided = -1;
    /// Actors with a matching nonzero Id don't collide with each other
    int mPassThroughId = 0;
    /// A definition for when we attach a revolute joint to this actor
    private RevoluteJointDef mRevJointDef;
    /// A joint that allows this actor to revolve around another
    private Joint mRevJoint;
    /// A definition for when we attach a distance joint to this actor
    private DistanceJointDef mDistJointDef;
    /// A joint that allows this actor to stay within a fixed distance of another
    private Joint mDistJoint;
    /// If this actor is chasing another actor, we track who is being chased via this field
    private WorldActor mChaseTarget;

    /**
     * Create a new actor that does not yet have physics, but that has a renderable picture
     *
     * @param game    The currently active game
     * @param scene   The scene into which the actor is being placed
     * @param imgName The image to display
     * @param width   The width
     * @param height  The height
     */
    WorldActor(Lol game, MainScene scene, String imgName, float width, float height) {
        super(scene, imgName, width, height);
        mGame = game;
    }

    /**
     * Indicate that when this actor stops, we should run custom code
     *
     * @param callback The callback to run when the actor stops
     */
    public void setStopCallback(final LolActorEvent callback) {
        mScene.mRepeatEvents.add(new LolAction() {
            boolean moving = false;
            @Override
            public void go() {
                Vector2 speed = mBody.getLinearVelocity();
                if (!moving && (Math.abs(speed.x) > 0 || Math.abs(speed.y) > 0))
                    moving = true;
                else if (moving && speed.x == 0 && speed.y == 0) {
                    callback.go(WorldActor.this);
                    moving = false;
                }
            }
        });
    }

    /**
     * Each descendant defines this to address any custom logic that we need to deal with on a
     * collision
     *
     * @param other   Other object involved in this collision
     * @param contact A description of the contact that caused this collision
     */
    abstract void onCollide(WorldActor other, Contact contact);

    /**
     * Make the camera follow the actor, but without centering the actor on the screen
     *
     * @param x Amount of x distance between actor and center
     * @param y Amount of y distance between actor and center
     */
    public void setCameraOffset(float x, float y) {
        mCameraOffset.x = x;
        mCameraOffset.y = y;
    }

    /**
     * Indicate that the actor should move with the tilt of the phone
     */
    public void setMoveByTilting() {
        // If we've already added this to the set of tiltable objects, don't do it again
        if (((MainScene) mScene).mTiltActors.contains(this))
            return;
        // make sure it is moveable, add it to the list of tilt actors
        if (mBody.getType() != BodyType.DynamicBody)
            mBody.setType(BodyType.DynamicBody);
        ((MainScene) mScene).mTiltActors.add(this);
        // turn off sensor behavior, so this collides with stuff...
        setCollisionsEnabled(true);
    }

    /**
     * Indicate that touching this object will cause some special code to run
     *
     * @param activationGoodies1 Number of type-1 goodies that must be collected before it works
     * @param activationGoodies2 Number of type-2 goodies that must be collected before it works
     * @param activationGoodies3 Number of type-3 goodies that must be collected before it works
     * @param activationGoodies4 Number of type-4 goodies that must be collected before it works
     * @param disappear          True if the actor should disappear when the callback runs
     * @param callback           The callback to run when the actor is touched
     */
    public void setTouchCallback(int activationGoodies1, int activationGoodies2,
                                 int activationGoodies3, int activationGoodies4,
                                 final boolean disappear, final LolActorEvent callback) {
        final int[] touchCallbackActivation = new int[]{activationGoodies1, activationGoodies2,
                activationGoodies3, activationGoodies4};
        // set the code to run on touch
        mTapHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                // check if we've got enough goodies
                boolean match = true;
                for (int i = 0; i < 4; ++i)
                    match &= touchCallbackActivation[i] <= mGame.mManager.mGoodiesCollected[i];
                // if so, run the callback
                if (match) {
                    if (disappear)
                        remove(false);
                    callback.go(WorldActor.this);
                }
                return true;
            }
        };
    }

    /**
     * Call this on an actor to make it draggable. Be careful when dragging things. If they are
     * small, they will be hard to touch.
     *
     * @param immuneToPhysics Indicate whether the actor should pass through other objects or
     *                        collide with them
     */
    public void setCanDrag(boolean immuneToPhysics) {
        if (immuneToPhysics)
            mBody.setType(BodyType.KinematicBody);
        else
            mBody.setType(BodyType.DynamicBody);
        mDragHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                mBody.setTransform(worldX, worldY, mBody.getAngle());
                return true;
            }
        };
    }

    /**
     * Call this on an actor to make it pokeable. Poke the actor, then poke the screen, and the
     * actor will move to the location that was pressed. Poke the actor twice in rapid succession to
     * delete it.
     *
     * @param deleteThresholdMillis If two touches happen within this many milliseconds, the actor
     *                              will be deleted. Use 0 to disable this "delete by double-touch"
     *                              feature.
     */
    public void setPokeToPlace(long deleteThresholdMillis) {
        // convert threshold to nanoseconds
        final long deleteThreshold = deleteThresholdMillis;
        // set the code to run on touch
        mTapHandler = new TouchEventHandler() {
            long mLastPokeTime;
            boolean mRunning = true;

            public boolean go(float worldX, float worldY) {
                if (!mRunning)
                    return false;
                Lol.vibrate(mScene.mConfig, 100);
                long time = System.currentTimeMillis();
                // double touch
                if ((time - mLastPokeTime) < deleteThreshold) {
                    // hide actor, disable physics
                    mBody.setActive(false);
                    mEnabled = false;
                    mRunning = false;
                    return true;
                }
                // repeat single-touch
                else {
                    mLastPokeTime = time;
                }
                // set a screen handler to detect when/where to move the actor
                mScene.mTapHandlers.add(new TouchEventHandler() {
                    boolean mIsRunning = true;

                    public boolean go(float worldX, float worldY) {
                        if (!mIsRunning || !mEnabled)
                            return false;
                        Lol.vibrate(mScene.mConfig, 100);
                        // move the object
                        mBody.setTransform(worldX, worldY, mBody.getAngle());
                        // clear the Level responder
                        mIsRunning = false;
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
     * @param dampFactor A value that is multiplied by the vector for the flick, to affect speed
     */
    public void setFlickable(final float dampFactor) {
        // make sure the body is a dynamic body
        setCanFall();

        ((MainScene) mScene).mFlingHandlers.add(new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                // note: may need to disable hovering
                if (mScene.mHitActor == WorldActor.this) {
                    mHover = null;
                    updateVelocity(worldX * dampFactor, worldY * dampFactor);
                }
                return true;
            }
        });
    }

    /**
     * Configure an actor so that touching an arbitrary point on the screen makes the actor move
     * toward that point. The behavior is similar to pokeToPlace, in that one touches the actor,
     * then where she wants the actor to go. However, this involves moving with velocity, instead of
     * teleporting
     *
     * @param velocity     The constant velocity for poke movement
     * @param oncePerTouch After starting a path, does the player need to re-select (re-touch) the
     *                     actor before giving it a new destinaion point?
     */
    public void setPokePath(final float velocity, final boolean oncePerTouch) {
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        mTapHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                Lol.vibrate(mScene.mConfig, 5);
                mScene.mTapHandlers.add(new TouchEventHandler() {
                    boolean mRunning = true;

                    public boolean go(float worldX, float worldY) {
                        if (!mRunning)
                            return false;
                        Route r = new Route(2).to(getXPosition(), getYPosition()).to(worldX - mSize.x / 2,
                                worldY - mSize.y / 2);
                        setAbsoluteVelocity(0, 0);
                        setRoute(r, velocity, false);
                        if (oncePerTouch)
                            mRunning = false;
                        return true;
                    }
                });
                return true;
            }
        };
    }

    /**
     * Configure an actor so that touching an arbitrary point on the screen makes the actor move
     * toward that point. The behavior is similar to pokePath, except that as the finger moves, the
     * actor keeps changing its destination accordingly.
     *
     * @param velocity     The constant velocity for poke movement
     * @param oncePerTouch After starting a path, does the player need to re-select (re-touch) the
     *                     actor before giving it a new destinaion point?
     * @param stopOnUp     When the touch is released, should the actor stop moving, or continue in
     *                     the same direction?
     */
    public void setFingerChase(final float velocity, final boolean oncePerTouch,
                               final boolean stopOnUp) {
        if (mBody.getType() == BodyType.StaticBody)
            mBody.setType(BodyType.KinematicBody);
        mTapHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                Lol.vibrate(mScene.mConfig, 5);

                // on a down (or, indirectly, a pan), do this
                final TouchEventHandler down = new TouchEventHandler() {
                    public boolean go(float worldX, float worldY) {
                        if (!mTapHandler.mIsActive)
                            return false;
                        Route r = new Route(2).to(getXPosition(), getYPosition()).to(worldX - mSize.x / 2,
                                worldY - mSize.y / 2);
                        setAbsoluteVelocity(0, 0);
                        setRoute(r, velocity, false);
                        return true;
                    }
                };
                final PanEventHandler pan = new PanEventHandler() {
                    @Override
                    public boolean go(float eventPositionX, float eventPositionY, float deltaX, float deltaY) {
                        return down.go(eventPositionX, eventPositionY);
                    }
                };
                // on an up (or a panstop), do this
                TouchEventHandler up = new TouchEventHandler() {
                    public boolean go(float worldX, float worldY) {
                        if (!mTapHandler.mIsActive)
                            return false;
                        if (stopOnUp && mRoute != null)
                            mRoute.haltRoute();
                        if (oncePerTouch)
                            mTapHandler.mIsActive = false;
                        return true;
                    }
                };
                ((MainScene) mScene).mUpHandlers.add(up);
                ((MainScene) mScene).mPanStopHandlers.add(up);
                ((MainScene) mScene).mDownHandlers.add(down);
                ((MainScene) mScene).mPanHandlers.add(pan);
                return true;
            }
        };
    }

    /**
     * Indicate that this actor should hover at a specific location on the screen, rather than being
     * placed at some point on the level itself. Note that the coordinates to this command are the
     * center position of the hovering actor. Also, be careful about using hover with zoom... hover
     * is relative to screen coordinates (pixels), not world coordinates, so it's going to look
     * funny to use this with zoom
     *
     * @param x the X coordinate (in pixels) where the actor should appear
     * @param y the Y coordinate (in pixels) where the actor should appear
     */
    public void setHover(final int x, final int y) {
        mHover = new Vector3();
        mScene.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (mHover == null)
                    return;
                mHover.x = x;
                mHover.y = y;
                mHover.z = 0;
                mScene.mCamera.unproject(mHover);
                mBody.setTransform(mHover.x, mHover.y, mBody.getAngle());
            }
        });
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
     * Indicate that touching this actor should make a hero throw a projectile
     *
     * @param h         The hero who should throw a projectile when this is touched
     * @param offsetX   specifies the x distance between the bottom left of the projectile and the
     *                  bottom left of the hero throwing the projectile
     * @param offsetY   specifies the y distance between the bottom left of the projectile and the
     *                  bottom left of the hero throwing the projectile
     * @param velocityX The X velocity of the projectile when it is thrown
     * @param velocityY The Y velocity of the projectile when it is thrown
     */
    public void setTouchToThrow(final Hero h, final float offsetX, final float offsetY,
                                final float velocityX, final float velocityY) {
        mTapHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                ((MainScene) mScene).mProjectilePool.throwFixed(h, offsetX, offsetY, velocityX, velocityY);
                return true;
            }
        };
    }

    /**
     * Indicate that this obstacle only registers collisions on one side.
     *
     * @param side The side that registers collisions. 0 is top, 1 is right, 2 is bottom, 3 is left,
     *             -1 means "none"
     */
    public void setOneSided(int side) {
        mIsOneSided = side;
    }

    /**
     * Indicate that this actor should not have collisions with any other actor that has the same ID
     *
     * @param id The number for this class of non-interacting actors
     */
    public void setPassThrough(int id) {
        mPassThroughId = id;
    }

    /**
     * Specify that this actor is supposed to chase another actor
     *
     * @param speed    The speed with which it chases the other actor
     * @param target   The actor to chase
     * @param chaseInX Should the actor change its x velocity?
     * @param chaseInY Should the actor change its y velocity?
     */
    public void setChaseSpeed(final float speed, final WorldActor target, final boolean chaseInX,
                              final boolean chaseInY) {
        mChaseTarget = target;
        mBody.setType(BodyType.DynamicBody);
        mScene.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                // don't chase something that isn't visible
                if (!target.mEnabled)
                    return;
                // don't run if this actor isn't visible
                if (!mEnabled)
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
     * Specify that this actor is supposed to chase another actor, but using fixed X/Y velocities
     *
     * @param target     The actor to chase
     * @param xMagnitude The magnitude in the x direction, if ignoreX is false
     * @param yMagnitude The magnitude in the y direction, if ignoreY is false
     * @param ignoreX    False if we should apply xMagnitude, true if we should keep the hero's
     *                   existing X velocity
     * @param ignoreY    False if we should apply yMagnitude, true if we should keep the hero's
     *                   existing Y velocity
     */
    public void setChaseFixedMagnitude(final WorldActor target, final float xMagnitude,
                                       final float yMagnitude, final boolean ignoreX,
                                       final boolean ignoreY) {
        mChaseTarget = target;
        mBody.setType(BodyType.DynamicBody);
        mScene.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                // don't chase something that isn't visible
                if (!target.mEnabled)
                    return;
                // don't run if this actor isn't visible
                if (!mEnabled)
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
    public WorldActor getChaseactor() {
        return mChaseTarget;
    }

    /**
     * Create a revolute joint between this actor and some other actor. Note that both actors need
     * to have some mass (density can't be 0) or else this won't work.
     *
     * @param anchor       The actor around which this actor will rotate
     * @param anchorX      The X coordinate (relative to center) where joint fuses to the anchor
     * @param anchorY      The Y coordinate (relative to center) where joint fuses to the anchor
     * @param localAnchorX The X coordinate (relative to center) where joint fuses to this actor
     * @param localAnchorY The Y coordinate (relative to center) where joint fuses to this actor
     */
    public void setRevoluteJoint(WorldActor anchor, float anchorX, float anchorY,
                                 float localAnchorX, float localAnchorY) {
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
        mRevJoint = mScene.mWorld.createJoint(mRevJointDef);
    }

    /**
     * Attach a motor to make a joint turn
     *
     * @param motorSpeed  Speed in radians per second
     * @param motorTorque torque of the motor... when in doubt, go with Float.POSITIVE_INFINITY
     */
    public void setRevoluteJointMotor(float motorSpeed, float motorTorque) {
        // destroy the previously created joint, change the definition, re-create the joint
        mScene.mWorld.destroyJoint(mRevJoint);
        mRevJointDef.enableMotor = true;
        mRevJointDef.motorSpeed = motorSpeed;
        mRevJointDef.maxMotorTorque = motorTorque;
        mRevJoint = mScene.mWorld.createJoint(mRevJointDef);
    }

    /**
     * Set upper and lower bounds on the rotation of the joint
     *
     * @param upper The upper bound in radians
     * @param lower The lower bound in radians
     */
    public void setRevoluteJointLimits(float upper, float lower) {
        // destroy the previously created joint, change the definition, re-create the joint
        mScene.mWorld.destroyJoint(mRevJoint);
        mRevJointDef.upperAngle = upper;
        mRevJointDef.lowerAngle = lower;
        mRevJointDef.enableLimit = true;
        mRevJoint = mScene.mWorld.createJoint(mRevJointDef);
    }

    /**
     * Create a weld joint between this actor and some other actor, to force the actors to stick
     * together.
     *
     * @param other  The actor that will be fused to this actor
     * @param otherX The X coordinate (relative to center) where joint fuses to the other actor
     * @param otherY The Y coordinate (relative to center) where joint fuses to the other actor
     * @param localX The X coordinate (relative to center) where joint fuses to this actor
     * @param localY The Y coordinate (relative to center) where joint fuses to this actor
     * @param angle  The angle between the actors
     */
    public void setWeldJoint(WorldActor other, float otherX, float otherY, float localX,
                             float localY, float angle) {
        WeldJointDef w = new WeldJointDef();
        w.bodyA = mBody;
        w.bodyB = other.mBody;
        w.localAnchorA.set(localX, localY);
        w.localAnchorB.set(otherX, otherY);
        w.referenceAngle = angle;
        w.collideConnected = false;
        mExplicitWeldJoint = (WeldJoint) mScene.mWorld.createJoint(w);
    }

    /**
     * Create a distance joint between this actor and some other actor
     *
     * @param anchor       The actor to which this actor is connected
     * @param anchorX      The X coordinate (relative to center) where joint fuses to the anchor
     * @param anchorY      The Y coordinate (relative to center) where joint fuses to the anchor
     * @param localAnchorX The X coordinate (relative to center) where joint fuses to this actor
     * @param localAnchorY The Y coordinate (relative to center) where joint fuses to this actor
     */
    public void setDistanceJoint(WorldActor anchor, float anchorX, float anchorY,
                                 float localAnchorX, float localAnchorY) {
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

        mDistJoint = mScene.mWorld.createJoint(mDistJointDef);
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
     * Break any joints connecting this actor
     */
    @Override
    void breakJoints() {
        // Clobber any joints, or this won't be able to move
        if (mDJoint != null) {
            mScene.mWorld.destroyJoint(mDJoint);
            mDJoint = null;
            mScene.mWorld.destroyJoint(mWJoint);
            mWJoint = null;
        }
    }
}
