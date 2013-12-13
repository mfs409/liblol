package edu.lehigh.cse.ale;

// TODO: clean up comments

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;

import edu.lehigh.cse.ale.Level.Action;

public class Physics
{
    static final float PIXEL_METER_RATIO = 10;

    public static void configure(float defaultXGravity, float defaultYGravity)
    {
        // we instantiate a new World with a proper gravity vector
        // and tell it to sleep when possible.
        Level._currLevel._world = new World(new Vector2(defaultXGravity, defaultYGravity), true);

        // set up the collision handler
        Level._currLevel._world.setContactListener(new ContactListener()
        {
            // quick demo to show that we can manage collisions just like in ALE
            @Override
            public void beginContact(Contact contact)
            {
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();

                // we only do more if both are GFObjects
                if (!(a instanceof PhysicsSprite) || !(b instanceof PhysicsSprite))
                    return;

                // filter so that the one with the smaller type handles the
                // collision
                PhysicsSprite gfoA = (PhysicsSprite) a;
                PhysicsSprite gfoB = (PhysicsSprite) b;
                if (gfoA._psType._id > gfoB._psType._id) {
                    PhysicsSprite tmp = gfoA;
                    gfoA = gfoB;
                    gfoB = tmp;
                }
                final PhysicsSprite _a = gfoA;
                final PhysicsSprite _b = gfoB;
                final Contact _c = contact;
                // NB: this is very important. This code runs when two bodies
                // are in the act of colliding, during the physics world's step
                // function. While the world is 'stepping', it is illegal to
                // modify the world. Rather than take every function that might
                // ever run and push it into a delay handler, we instead delay
                // the handling of the collision until after the physics step
                // completes
                Level._currLevel._oneTimeEvents.add(new Action()
                {
                    public void go()
                    {
                        _a.onCollide(_b, _c);
                    }
                });
            }

            @Override
            public void endContact(Contact contact)
            {
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold)
            {
                // get the bodies
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();

                // Convert to PhysicsSprites
                if (!(a instanceof PhysicsSprite) || !(b instanceof PhysicsSprite))
                    return;
                PhysicsSprite gfoA = (PhysicsSprite) a;
                PhysicsSprite gfoB = (PhysicsSprite) b;

                // NB: this is where we need to handle joints for sticky objects

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
                }
                else if (gfoB._isOneSided > -1) {
                    onesided = gfoB;
                    other = gfoA;
                }
                else {
                    return;
                }

                // if we're here, see if we should be disabling a one-sided
                // obstacle collision
                WorldManifold worldManiFold = contact.getWorldManifold();
                int numPoints = contact.getWorldManifold().getNumberOfContactPoints();
                for (int i = 0; i < numPoints; i++) {
                    Vector2 vector2 = other._physBody.getLinearVelocityFromWorldPoint(worldManiFold.getPoints()[i]);
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

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse)
            {
            }
        });
    }
}