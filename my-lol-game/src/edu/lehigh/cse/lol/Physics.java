
package edu.lehigh.cse.lol;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;

import edu.lehigh.cse.lol.Level.Action;

/**
 * Game designers can configure the physics of a level (i.e., the default
 * forces, if any), via this class. Internally, the class constructs a box2d
 * physics world and instantiates the callbacks needed to ensure that LOL works
 * correctly.
 */
public class Physics {
    /**
     * This ratio means that every 10 pixels on the screen will correspond to a
     * meter. Note that 'pixels' are defined in terms of what a programmer's
     * Config says, not the actual screen size, because the programmer's Config
     * gets scaled to screen dimensions.
     */
    static final float PIXEL_METER_RATIO = 10;

    static void handleSticky(final PhysicsSprite sticky, final PhysicsSprite other, Contact contact) {
        // don't create a joint if we've already got one
        if (other._dJoint != null)
            return;
        // don't create a joint if we're supposed to wait
        if (System.nanoTime() < other._stickyDelay)
            return;
        // handle sticky obstacles... only do something if we're hitting the
        // obstacle from the right direction
        if ((sticky.isStickyTop && other.getYPosition() >= sticky.getYPosition() + sticky._height)
                || (sticky.isStickyLeft && other.getXPosition() + other._width <= sticky
                        .getXPosition())
                || (sticky.isStickyRight && other.getXPosition() >= sticky.getXPosition()
                        + sticky._width)
                || (sticky.isStickyBottom && other.getYPosition() + other._height <= sticky
                        .getYPosition())) {
            // create distance and weld joints... somehow, the combination is
            // needed to get this to work
            final Vector2 v = contact.getWorldManifold().getPoints()[0];
            Level._currLevel._oneTimeEvents.add(new Action() {
                @Override
                public void go() {
                    other._physBody.setLinearVelocity(0, 0);
                    DistanceJointDef d = new DistanceJointDef();
                    d.initialize(sticky._physBody, other._physBody, v, v);
                    d.collideConnected = true;
                    other._dJoint = (DistanceJoint)Level._currLevel._world.createJoint(d);
                    WeldJointDef w = new WeldJointDef();
                    w.initialize(sticky._physBody, other._physBody, v);
                    w.collideConnected = true;
                    other._wJoint = (WeldJoint)Level._currLevel._world.createJoint(w);
                }
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
    public static void configure(float defaultXGravity, float defaultYGravity) {
        // create a world with gravity
        Level._currLevel._world = new World(new Vector2(defaultXGravity, defaultYGravity), true);

        // set up the collision handlers
        Level._currLevel._world.setContactListener(new ContactListener() {
            /**
             * When two bodies start to collide, we can use this to forward to
             * our onCollide methods
             */
            @Override
            public void beginContact(Contact contact) {
                // Get the bodies, make sure both are PhysicsSprites
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();
                if (!(a instanceof PhysicsSprite) || !(b instanceof PhysicsSprite))
                    return;

                // Figure out which one has the smaller type (Hero is smallest)
                PhysicsSprite gfoA = (PhysicsSprite)a;
                PhysicsSprite gfoB = (PhysicsSprite)b;
                if (gfoA._psType._id > gfoB._psType._id) {
                    PhysicsSprite tmp = gfoA;
                    gfoA = gfoB;
                    gfoB = tmp;
                }
                final PhysicsSprite _a = gfoA;
                final PhysicsSprite _b = gfoB;
                final Contact _c = contact;
                // Schedule an event to run as soon as the physics world
                // finishes its step.
                //
                // NB: this is called from render, while _world is updating...
                // you can't modify the world or its
                // entities until the update finishes, so we have to schedule
                // collision-based updates to run after the
                // world update.
                Level._currLevel._oneTimeEvents.add(new Action() {
                    public void go() {
                        _a.onCollide(_b, _c);
                    }
                });
            }

            /**
             * We ignore endcontact
             */
            @Override
            public void endContact(Contact contact) {
            }

            /**
             * Presolve is a hook for disabling certain collisions. We use it
             * for collision immunity, sticky obstacles, and one-way walls
             */
            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                // get the bodies, make sure both are PhysicsSprites
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();
                if (!(a instanceof PhysicsSprite) || !(b instanceof PhysicsSprite))
                    return;
                PhysicsSprite gfoA = (PhysicsSprite)a;
                PhysicsSprite gfoB = (PhysicsSprite)b;

                // handle sticky obstacles... only do something if at least one
                // entity is a sticky entity
                if (gfoA.isStickyBottom || gfoA.isStickyTop || gfoA.isStickyLeft
                        || gfoA.isStickyRight) {
                    handleSticky(gfoA, gfoB, contact);
                    return;
                } else if (gfoB.isStickyBottom || gfoB.isStickyTop || gfoB.isStickyLeft
                        || gfoB.isStickyRight) {
                    handleSticky(gfoB, gfoA, contact);
                    return;
                }

                // if the PhysicsSprites have the same passthrough ID, and it's
                // not zero, then disable the contact
                if (gfoA._passThroughId != 0 && gfoA._passThroughId == gfoB._passThroughId) {
                    contact.setEnabled(false);
                    return;
                }

                // is either one-sided? If not, we're done
                PhysicsSprite onesided = null;
                PhysicsSprite other = null;
                if (gfoA._isOneSided > -1) {
                    onesided = gfoA;
                    other = gfoB;
                } else if (gfoB._isOneSided > -1) {
                    onesided = gfoB;
                    other = gfoA;
                } else {
                    return;
                }

                // if we're here, see if we should be disabling a one-sided
                // obstacle collision
                WorldManifold worldManiFold = contact.getWorldManifold();
                int numPoints = worldManiFold.getNumberOfContactPoints();
                for (int i = 0; i < numPoints; i++) {
                    Vector2 vector2 = other._physBody.getLinearVelocityFromWorldPoint(worldManiFold
                            .getPoints()[i]);
                    // disable based on the value of _isOneSided and the vector
                    // between the entities
                    if (onesided._isOneSided == 0 && vector2.y < 0)
                        contact.setEnabled(false);
                    else if (onesided._isOneSided == 2 && vector2.y > 0)
                        contact.setEnabled(false);
                    else if (onesided._isOneSided == 1 && vector2.x > 0)
                        contact.setEnabled(false);
                    else if (onesided._isOneSided == 3 && vector2.x < 0)
                        contact.setEnabled(false);
                }
            }

            /**
             * We don't do anything fancy on postsolve
             */
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }
}
