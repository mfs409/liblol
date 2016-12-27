package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;

/**
 * BaseLevel stores the functionality that is common across all of the different renderable,
 * updatable containers that store actors in a physical world.
 */
class BaseLevel {
    /// A reference to the game object, so we can access session facts and the state machine
    protected final Lol mGame;

    /// A reference to the game-wide configuration variables
    protected final Config mConfig;

    /// A reference to the object that stores all of the sounds and images we use in the game
    protected final Media mMedia;

    /// The maximum x and y values of the camera
    protected final Vector2 mCamBound = new Vector2();

    /// This camera is for drawing actors that exist in the physics world
    protected final OrthographicCamera mGameCam;

    /// The physics world in which all actors interact
    protected final World mWorld;

    /// Events that get processed on the next render, then discarded
    protected final ArrayList<LolAction> mOneTimeEvents;

    /// Events that get processed on every render
    protected final ArrayList<LolAction> mRepeatEvents;

    /// Anything in the world that can be rendered, in 5 planes [-2, -1, 0, 1, 2]
    protected final ArrayList<ArrayList<Renderable>> mRenderables;

    /// This callback is used to get a touched actor from the physics world
    protected final QueryCallback mTouchCallback;

    /// We use this to avoid garbage collection when converting screen touches to camera coordinates
    protected final Vector3 mTouchVec = new Vector3();

    /// When there is a touch of an actor in the physics world, this is how we find it
    protected Actor mHitActor = null;

    /**
     * Construct a basic level.  A level has a camera and a phyics world, actors who live in that
     * world, and the supporting infrastructure to make it all work.
     *
     * @param config The configuration object describing this game
     * @param media  References to all image and sound assets
     * @param game   The game that is being played
     */
    BaseLevel(Config config, Media media, Lol game) {
        // clear any timers
        Timer.instance().clear();

        // save game configuration information
        mGame = game;
        mConfig = config;
        mMedia = media;

        // set up the event lists
        mOneTimeEvents = new ArrayList<>();
        mRepeatEvents = new ArrayList<>();

        // set up the renderables
        mRenderables = new ArrayList<>(5);
        for (int i = 0; i < 5; ++i)
            mRenderables.add(new ArrayList<Renderable>());

        // set up the game camera, with (0, 0) in the bottom left
        float w = mConfig.mWidth / mConfig.PIXEL_METER_RATIO;
        float h = mConfig.mHeight / mConfig.PIXEL_METER_RATIO;
        mGameCam = new OrthographicCamera(w, h);
        mGameCam.position.set(w / 2, h / 2, 0);
        mGameCam.zoom = 1;

        // set default camera bounds
        setCameraBounds(w, h);

        // create a world with no default gravitational forces
        mWorld = new World(new Vector2(0, 0), true);
        // Set up collision handlers
        configureCollisionHandlers();

        // set up the callback for finding out who in the physics world was
        // touched
        mTouchCallback = new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                // if the hit point is inside the fixture of the body we report
                // it
                if (fixture.testPoint(mTouchVec.x, mTouchVec.y)) {
                    Actor hs = (Actor) fixture.getBody().getUserData();
                    if (hs.mVisible) {
                        mHitActor = hs;
                        return false;
                    }
                }
                return true;
            }
        };

    }

    /**
     * Configure the camera bounds for a level
     * <p>
     * TODO: set upper and lower bounds, instead of assuming a lower bound of (0, 0)
     *
     * @param width  width of the camera
     * @param height height of the camera
     */
    public void setCameraBounds(float width, float height) {
        mCamBound.set(width, height);

        // warn on strange dimensions
        if (width < mConfig.mWidth / mConfig.PIXEL_METER_RATIO)
            Lol.message(mConfig, "Warning", "Your game width is less than 1/10 of the screen width");
        if (height < mConfig.mHeight / mConfig.PIXEL_METER_RATIO)
            Lol.message(mConfig, "Warning", "Your game height is less than 1/10 of the screen height");
    }

    /**
     * This code is called every 1/45th of a second to update the game state and
     * re-draw the screen
     *
     * @param delta The time since the last render
     */
    void render(float delta, Box2DDebugRenderer debugRender, SpriteBatch sb) {
        // Advance the physics world by 1/45 of a second.
        //
        // NB: in Box2d, This is the recommended rate for phones, though it
        // seems like we should be using /delta/ instead of 1/45f
        mWorld.step(1 / 45f, 8, 3);

        // now handle any events that occurred on account of the world movement
        // or screen touches
        for (LolAction pe : mOneTimeEvents)
            pe.go();
        mOneTimeEvents.clear();

        // handle repeat events
        for (LolAction pe : mRepeatEvents) {
            if (pe.mIsActive)
                pe.go();
        }
    }

    /**
     * When a hero collides with a "sticky" obstacle, this is the code we run to
     * figure out what to do
     *
     * @param sticky  The sticky actor... it should always be an obstacle for now
     * @param other   The other actor... it should always be a hero for now
     * @param contact A description of the contact event
     */
    protected void handleSticky(final Actor sticky, final Actor other, Contact contact) {
        // don't create a joint if we've already got one
        if (other.mDJoint != null)
            return;
        // don't create a joint if we're supposed to wait
        if (System.currentTimeMillis() < other.mStickyDelay)
            return;
        // handle sticky obstacles... only do something if we're hitting the
        // obstacle from the correct direction
        if ((sticky.mIsSticky[0] && other.getYPosition() >= sticky.getYPosition() + sticky.mSize.y)
                || (sticky.mIsSticky[1] && other.getXPosition() + other.mSize.x <= sticky.getXPosition())
                || (sticky.mIsSticky[3] && other.getXPosition() >= sticky.getXPosition() + sticky.mSize.x)
                || (sticky.mIsSticky[2] && other.getYPosition() + other.mSize.y <= sticky.getYPosition())) {
            // create distance and weld joints... somehow, the combination is
            // needed to get this to work. Note that this function runs during
            // the box2d step, so we need to make the joint in a callback that
            // runs later
            final Vector2 v = contact.getWorldManifold().getPoints()[0];
            mOneTimeEvents.add(new LolAction() {
                @Override
                public void go() {
                    other.mBody.setLinearVelocity(0, 0);
                    DistanceJointDef d = new DistanceJointDef();
                    d.initialize(sticky.mBody, other.mBody, v, v);
                    d.collideConnected = true;
                    other.mDJoint = (DistanceJoint) mWorld.createJoint(d);
                    WeldJointDef w = new WeldJointDef();
                    w.initialize(sticky.mBody, other.mBody, v);
                    w.collideConnected = true;
                    other.mWJoint = (WeldJoint) mWorld.createJoint(w);
                }
            });
        }
    }

    /**
     * Configure physics for the current level
     */
    protected void configureCollisionHandlers() {

        // set up the collision handlers
        mWorld.setContactListener(new ContactListener() {
            /**
             * When two bodies start to collide, we can use this to forward to
             * our onCollide methods
             */
            @Override
            public void beginContact(final Contact contact) {
                // Get the bodies, make sure both are actors
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();
                if (!(a instanceof Actor) || !(b instanceof Actor))
                    return;

                // the order is Hero, Enemy, Goodie, Projectile, Obstacle, Destination
                //
                // Of those, Hero, Enemy, and Projectile are the only ones with
                // a non-empty onCollide
                final Actor c0;
                final Actor c1;
                if (a instanceof Hero) {
                    c0 = (Actor) a;
                    c1 = (Actor) b;
                } else if (b instanceof Hero) {
                    c0 = (Actor) b;
                    c1 = (Actor) a;
                } else if (a instanceof Enemy) {
                    c0 = (Actor) a;
                    c1 = (Actor) b;
                } else if (b instanceof Enemy) {
                    c0 = (Actor) b;
                    c1 = (Actor) a;
                } else if (a instanceof Projectile) {
                    c0 = (Actor) a;
                    c1 = (Actor) b;
                } else if (b instanceof Projectile) {
                    c0 = (Actor) b;
                    c1 = (Actor) a;
                } else {
                    return;
                }

                // Schedule an event to run as soon as the physics world
                // finishes its step.
                //
                // NB: this is called from render, while world is updating...
                // you can't modify the world or its actors until the update
                // finishes, so we have to schedule collision-based updates to
                // run after the world update.
                mOneTimeEvents.add(new LolAction() {
                    @Override
                    public void go() {
                        c0.onCollide(c1, contact);
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
                // get the bodies, make sure both are actors
                Object a = contact.getFixtureA().getBody().getUserData();
                Object b = contact.getFixtureB().getBody().getUserData();
                if (!(a instanceof Actor) || !(b instanceof Actor))
                    return;
                Actor gfoA = (Actor) a;
                Actor gfoB = (Actor) b;

                // handle sticky obstacles... only do something if at least one
                // actor is a sticky actor
                if (gfoA.mIsSticky[0] || gfoA.mIsSticky[1] || gfoA.mIsSticky[2] || gfoA.mIsSticky[3]) {
                    handleSticky(gfoA, gfoB, contact);
                    return;
                } else if (gfoB.mIsSticky[0] || gfoB.mIsSticky[1] || gfoB.mIsSticky[2] || gfoB.mIsSticky[3]) {
                    handleSticky(gfoB, gfoA, contact);
                    return;
                }

                // if the actors have the same passthrough ID, and it's
                // not zero, then disable the contact
                if (gfoA.mPassThroughId != 0 && gfoA.mPassThroughId == gfoB.mPassThroughId) {
                    contact.setEnabled(false);
                    return;
                }

                // is either one-sided? If not, we're done
                Actor onesided = null;
                Actor other;
                if (gfoA.mIsOneSided > -1) {
                    onesided = gfoA;
                    other = gfoB;
                } else if (gfoB.mIsOneSided > -1) {
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
                    Vector2 vector2 = other.mBody.getLinearVelocityFromWorldPoint(worldManiFold.getPoints()[i]);
                    // disable based on the value of isOneSided and the vector
                    // between the actors
                    if (onesided.mIsOneSided == 0 && vector2.y < 0)
                        contact.setEnabled(false);
                    else if (onesided.mIsOneSided == 2 && vector2.y > 0)
                        contact.setEnabled(false);
                    else if (onesided.mIsOneSided == 1 && vector2.x > 0)
                        contact.setEnabled(false);
                    else if (onesided.mIsOneSided == 3 && vector2.x < 0)
                        contact.setEnabled(false);
                }
            }

            /**
             * We ignore postsolve
             */
            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
            }
        });
    }
}