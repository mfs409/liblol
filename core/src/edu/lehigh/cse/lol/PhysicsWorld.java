package edu.lehigh.cse.lol;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
import java.util.Random;
import java.util.TreeMap;

/**
 * PhysicsWorld stores the functionality that is common across all of the different renderable,
 * updatable containers that store actors in a physical world.
 */
class PhysicsWorld {
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

    /// A map for storing the level facts for the current level
    protected final TreeMap<String, Integer> mLevelFacts;

    /// A map for storing the actors in the current level
    protected final TreeMap<String, Actor> mLevelActors;

    /// A reference to the tilt object, for managing how tilts are handled
    ///
    /// TODO: make private
    Tilt mTilt = new Tilt();

    /// The set of Parallax backgrounds
    protected final Background mBackground = new Background();

    /// The set of Parallax foregrounds
    protected final Foreground mForeground = new Foreground();

    /// This camera is for drawing parallax backgrounds that go in front of or behind the world
    ///
    /// TODO: make private
    ParallaxCamera mBgCam;

    /// This is the Actor that the camera chases
    protected Actor mChaseActor;

    /// Actors may need to set callbacks to run on a screen touch. If so, they can use this.
    ///
    /// TODO: make private
    ArrayList<GestureAction> mGestureResponders = new ArrayList<>();

    /// In levels with a projectile pool, the pool is accessed from here
    ///
    /// TODO: make private
    ProjectilePool mProjectilePool;

    /// The music, if any
    protected Music mMusic;

    /// Whether the music is playing or not
    private boolean mMusicPlaying;

    /// A random number generator... We provide this so that new game developers don't create lots
    /// of Random()s throughout their code
    final Random mGenerator = new Random();

    /// Use this for determining bounds of text boxes
    final GlyphLayout mGlyphLayout = new GlyphLayout();

    /**
     * Construct a basic level.  A level has a camera and a phyics world, actors who live in that
     * world, and the supporting infrastructure to make it all work.
     *
     * @param config The configuration object describing this game
     * @param media  References to all image and sound assets
     * @param game   The game that is being played
     */
    PhysicsWorld(Config config, Media media, Lol game) {
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
        mCamBound.set(w, h);

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

        // reset the per-level object store
        mLevelFacts = new TreeMap<>();
        mLevelActors = new TreeMap<>();
    }

    /**
     * Add an actor to the level, putting it into the appropriate z plane
     *
     * @param actor  The actor to add
     * @param zIndex The z plane. valid values are -2, -1, 0, 1, and 2. 0 is the
     *               default.
     */
    void addActor(Renderable actor, int zIndex) {
        assert zIndex >= -2;
        assert zIndex <= 2;
        mRenderables.get(zIndex + 2).add(actor);
    }

    /**
     * Remove an actor from its z plane
     *
     * @param actor  The actor to remove
     * @param zIndex The z plane where it is expected to be
     */
    void removeActor(Renderable actor, int zIndex) {
        assert zIndex >= -2;
        assert zIndex <= 2;
        mRenderables.get(zIndex + 2).remove(actor);
    }

    /**
     * The main render loop calls this to determine what to do when there is a
     * phone tilt
     */
    void handleTilt() {
        if (mTilt.mGravityMax == null)
            return;

        // these temps are for storing the accelerometer forces we measure
        float xGravity = 0;
        float yGravity = 0;

        // if we're on a phone, read from the accelerometer device, taking into
        // account the rotation of the device
        Application.ApplicationType appType = Gdx.app.getType();
        if (appType == Application.ApplicationType.Android || appType == Application.ApplicationType.iOS) {
            float rot = Gdx.input.getRotation();
            if (rot == 0) {
                xGravity = -Gdx.input.getAccelerometerX();
                yGravity = -Gdx.input.getAccelerometerY();
            } else if (rot == 90) {
                xGravity = Gdx.input.getAccelerometerY();
                yGravity = -Gdx.input.getAccelerometerX();
            } else if (rot == 180) {
                xGravity = Gdx.input.getAccelerometerX();
                yGravity = Gdx.input.getAccelerometerY();
            } else if (rot == 270) {
                xGravity = -Gdx.input.getAccelerometerY();
                yGravity = Gdx.input.getAccelerometerX();
            }
        }
        // if we're on a computer, we simulate tilt with the arrow keys
        else {
            if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT))
                xGravity = -15f;
            else if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT))
                xGravity = 15f;
            else if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP))
                yGravity = 15f;
            else if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN))
                yGravity = -15f;
        }

        // Apply the gravity multiplier
        xGravity *= mTilt.mMultiplier;
        yGravity *= mTilt.mMultiplier;

        // ensure x is within the -GravityMax.x : GravityMax.x range
        xGravity = (xGravity > mConfig.PIXEL_METER_RATIO * mTilt.mGravityMax.x) ? mConfig.PIXEL_METER_RATIO * mTilt.mGravityMax.x
                : xGravity;
        xGravity = (xGravity < mConfig.PIXEL_METER_RATIO * -mTilt.mGravityMax.x) ? mConfig.PIXEL_METER_RATIO * -mTilt.mGravityMax.x
                : xGravity;

        // ensure y is within the -GravityMax.y : GravityMax.y range
        yGravity = (yGravity > mConfig.PIXEL_METER_RATIO * mTilt.mGravityMax.y) ? mConfig.PIXEL_METER_RATIO * mTilt.mGravityMax.y
                : yGravity;
        yGravity = (yGravity < mConfig.PIXEL_METER_RATIO * -mTilt.mGravityMax.y) ? mConfig.PIXEL_METER_RATIO * -mTilt.mGravityMax.y
                : yGravity;

        // If we're in 'velocity' mode, apply the accelerometer reading to each
        // actor as a fixed velocity
        if (mTilt.mTiltVelocityOverride) {
            // if X is clipped to zero, set each actor's Y velocity, leave X
            // unchanged
            if (mTilt.mGravityMax.x == 0) {
                for (Actor gfo : mTilt.mAccelActors)
                    if (gfo.mBody.isActive())
                        gfo.updateVelocity(gfo.mBody.getLinearVelocity().x, yGravity);
            }
            // if Y is clipped to zero, set each actor's X velocity, leave Y
            // unchanged
            else if (mTilt.mGravityMax.y == 0) {
                for (Actor gfo : mTilt.mAccelActors)
                    if (gfo.mBody.isActive())
                        gfo.updateVelocity(xGravity, gfo.mBody.getLinearVelocity().y);
            }
            // otherwise we set X and Y velocity
            else {
                for (Actor gfo : mTilt.mAccelActors)
                    if (gfo.mBody.isActive())
                        gfo.updateVelocity(xGravity, yGravity);
            }
        }
        // when not in velocity mode, apply the accelerometer reading to each
        // actor as a force
        else {
            for (Actor gfo : mTilt.mAccelActors)
                if (gfo.mBody.isActive())
                    gfo.mBody.applyForceToCenter(xGravity, yGravity, true);
        }
    }

    /**
     * Create a Renderable that consists of some text to draw
     *
     * @param x        The X coordinate of the bottom left corner, in pixels
     * @param y        The Y coordinate of the bottom left corner, in pixels
     * @param message  The text to display... note that it can't change on the fly
     * @param fontName The font to use
     * @param size     The font size
     * @return A Renderable of the text
     */
    Renderable makeText(final int x, final int y, final String message, final String fontColor, String fontName, int size) {
        final BitmapFont bf = mMedia.getFont(fontName, size);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(Color.valueOf(fontColor));
                mGlyphLayout.setText(bf, message);
                bf.draw(sb, message, x, y + mGlyphLayout.height);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw. The text will be
     * centered vertically and horizontally on the screen
     *
     * @param message  The text to display... note that it can't change on the fly
     * @param fontName The font to use
     * @param size     The font size
     * @return A Renderable of the text
     */
    Renderable makeText(final String message, final String fontColor,
                        String fontName, int size) {
        final BitmapFont bf = mMedia.getFont(fontName, size);
        mGlyphLayout.setText(bf, message);
        final float x = mConfig.mWidth / 2 - mGlyphLayout.width / 2;
        final float y = mConfig.mHeight / 2 + mGlyphLayout.height / 2;
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(Color.valueOf(fontColor));
                bf.draw(sb, message, x, y);
            }
        };
    }


    /**
     * A helper method to draw text nicely. In GDX, we draw everything by giving
     * the bottom left corner, except text, which takes the top left corner.
     * This function handles the conversion, so that we can use bottom-left.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param message The text to display
     * @param bf      The BitmapFont object to use for the text's font
     * @param sb      The SpriteBatch used to render the text
     */
    void drawTextTransposed(int x, int y, String message, BitmapFont bf, SpriteBatch sb) {
        mGlyphLayout.setText(bf, message);
        bf.draw(sb, message, x, y + mGlyphLayout.height);
    }

    /**
     * Create a Renderable that consists of an image
     *
     * @param x       The X coordinate of the bottom left corner, in pixels
     * @param y       The Y coordinate of the bottom left corner, in pixels
     * @param width   The image width, in pixels
     * @param height  The image height, in pixels
     * @param imgName The file name for the image, or ""
     * @return A Renderable of the image
     */
    Renderable makePicture(final float x, final float y, final float width, final float height,
                           String imgName) {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        final TextureRegion tr = mMedia.getImage(imgName);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
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

    /**
     * If the level has music attached to it, this starts playing it
     */
    void playMusic() {
        if (!mMusicPlaying && mMusic != null) {
            mMusicPlaying = true;
            mMusic.play();
        }
    }

    /**
     * If the level has music attached to it, this pauses it
     */
    void pauseMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.pause();
        }
    }

    /**
     * If the level has music attached to it, this stops it
     */
    void stopMusic() {
        if (mMusicPlaying) {
            mMusicPlaying = false;
            mMusic.stop();
        }
    }

    /**
     * If the camera is supposed to follow an actor, this code will handle
     * updating the camera position
     */
    protected void adjustCamera() {
        if (mChaseActor == null)
            return;
        // figure out the actor's position
        float x = mChaseActor.mBody.getWorldCenter().x + mChaseActor.mCameraOffset.x;
        float y = mChaseActor.mBody.getWorldCenter().y + mChaseActor.mCameraOffset.y;

        // if x or y is too close to MAX,MAX, stick with max acceptable values
        if (x > mCamBound.x - mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
            x = mCamBound.x - mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;
        if (y > mCamBound.y - mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
            y = mCamBound.y - mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;

        // if x or y is too close to 0,0, stick with minimum acceptable values
        //
        // NB: we do MAX before MIN, so that if we're zoomed out, we show extra
        // space at the top instead of the bottom
        if (x < mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
            x = mConfig.mWidth * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;
        if (y < mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2)
            y = mConfig.mHeight * mGameCam.zoom / mConfig.PIXEL_METER_RATIO / 2;

        // update the camera position
        mGameCam.position.set(x, y, 0);
    }

    /**
     * Tilt provides a mechanism for moving actors on the screen. To use tilt, you
     * must enableTilt it for a level, and also indicate that some actors move via
     * tilting. Tilt has two flavors: tilt can cause gravitational effects, where a
     * sustained tilt causes acceleration (this is the default), or it can cause
     * actors to move with a fixed velocity. Be careful when using tilt. Different
     * phones' accelerometers vary in terms of sensitivity. It is possible to set
     * multipliers and/or caps on the effect of Tilt, but these may not suffice to
     * make your game playable and enjoyable.
     */
    class Tilt {
        /**
         * List of actors that change behavior based on tilt
         */
        ArrayList<Actor> mAccelActors = new ArrayList<>();
        /**
         * Magnitude of the maximum gravity the accelerometer can create
         */
        Vector2 mGravityMax;
        /**
         * Track if we have an override for gravity to be translated into velocity
         */
        boolean mTiltVelocityOverride;
        /**
         * A multiplier to make gravity change faster or slower than the
         * accelerometer default
         */
        float mMultiplier = 1;
    }
}