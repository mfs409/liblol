package edu.lehigh.cse.lol;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Timer;

/**
 * BaseActor is the parent of all Actor types.
 * <p>
 * We use BaseActor as parent of both Actor (MainScene) and SimpleActor (all other scenes), so that
 * core functionality (physics, animation) can be in one place, even though many of the features of
 * an Actor (MainScene) require a Score object, and are thus incompatible with non-Main scenes.
 */
public class BaseActor extends Renderable {
    /// The level in which this Actor exists
    final LolScene mScene;

    /// Animation support: the offset for placing the disappearance animation relative to the
    // disappearing actor
    private final Vector2 mDisappearAnimateOffset = new Vector2();

    /// Track if the actor is currently being rendered. This is a proxy for "is important to the
    /// rest of the game" and when it is false, we don't run any updates on the actor
    boolean mVisible = true;

    /// Does this Actor follow a route? If so, the RouteDriver will be used to advance the actor
    /// along its route.
    RouteDriver mRoute;

    /// Sound to play when the actor disappears
    Sound mDisappearSound;

    /// Animation support: this tracks the current state of the active animation (if any)
    AnimationDriver mAnimator;

    /// Physics body for this Actor
    Body mBody;

    /// Animation support: the cells of the default animation
    Animation mDefaultAnimation;

    /// Animation support: the cells of the animation to use when moving backwards
    private Animation mDefaultReverseAnimation;

    /// The dimensions of the Actor... x is width, y is height
    Vector2 mSize = new Vector2();

    /// The z index of this actor. Valid range is [-2, 2]
    private int mZIndex = 0;

    /// Text that game designer can modify to hold additional information about the actor
    private String mInfoText = "";

    /// Integer that the game designer can modify to hold additional information about the actor
    private int mInfoInt;

    TouchEventHandler mTapHandler;
    TouchEventHandler mToggleHandler;

    /// a sound to play when this actor is touched
    private Sound mTouchSound;

    /// Animation support: the cells of the disappearance animation
    private Animation mDisappearAnimation;

    /// Animation support: the dimensions of the disappearance animation
    private Vector2 mDisappearAnimateSize;

    /// A temporary vertex that we use when resizing
    private Vector2 mTmpVert = new Vector2();

    /// Track if the underlying body is a circle
    private boolean mIsCircleBody;

    /// Track if the underlying body is a box
    private boolean mIsBoxBody;

    /// Track if the underlying body is a polygon
    private boolean mIsPolygonBody;

    /// Percentage of the way from bottom and left that we should clip
    private Vector2 mClippingBL;

    // Percentage of the way from top and right that we should clip
    private Vector2 mClippingWH;

    BaseActor(LolScene scene, String imgName, float width, float height) {
        mScene = scene;
        mAnimator = new AnimationDriver(mScene, imgName);
        mSize.x = width;
        mSize.y = height;
        mDisappearAnimateSize = new Vector2();
    }

    /**
     * Every time the world advances by a timestep, we call this code. It
     * updates the Actor and draws it. User code should never call this.
     */
    @Override
    void render(SpriteBatch sb, float delta) {
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

                // Draws a rectangle with the bottom left corner at x,y having the given width and
                // height in pixels. The rectangle is offset by originX, originY relative to the
                // origin. Scale specifies the scaling factor by which the rectangle should be
                // scaled around originX, originY. Rotation specifies the angle of counter clockwise
                // rotation of the rectangle around originX, originY. The portion of the Texture
                // given by srcX, srcY and srcWidth, srcHeight is used. These coordinates and sizes
                // are given in texels. FlipX and flipY specify whether the texture portion should
                // be flipped horizontally or vertically.

                if (mClippingWH != null)
                    // x, y, originX, originY, width, height, scaleX, scaleY, rotation, srcX, srcY, srcWidth, srcHeight, flipX, flipY
                    sb.draw(tr.getTexture(),
                            // bottom left corner X, Y where we ought to draw
                            pos.x - mSize.x / 2 + mClippingBL.x * mSize.x,
                            pos.y - mSize.y / 2 + mClippingBL.y * mSize.y,
                            // offset the image by this much
                            0, 0,
                            // width and height of the image: these are good
                            mSize.x * (mClippingWH.x - mClippingBL.x),
                            mSize.y * (mClippingWH.y - mClippingBL.y),
                            // scaling of the image
                            1, 1,
                            // rotation of the image
                            MathUtils.radiansToDegrees * mBody.getAngle(),
                            // source x and y positions
                            (int) (mClippingBL.x * tr.getTexture().getWidth()),
                            (int) (mClippingBL.y * tr.getTexture().getHeight()),
                            // source width and height
                            (int) (tr.getRegionWidth() * (mClippingWH.x - mClippingBL.x)),
                            (int) (tr.getRegionHeight() * (mClippingWH.y - mClippingBL.y)),
                            // flip in x or y?
                            false, true);

                else
                    // x, y, originX, originY, width, height, scaleX, scaleY, rotation
                    sb.draw(tr, pos.x - mSize.x / 2, pos.y - mSize.y / 2, mSize.x / 2, mSize.y / 2, mSize.x, mSize.y, 1, 1, MathUtils.radiansToDegrees * mBody.getAngle());
            }
        }
    }

    /**
     * It's a total cop-out, but I can't figure out how to do this without also flipping the image
     * @param x
     * @param y
     * @param w
     * @param h
     */
    public void setFlipAndClipRatio(float x, float y, float w, float h) {
        if (mClippingBL == null) {
            mClippingBL = new Vector2(x, y);
            mClippingWH = new Vector2(w, h);
        }
        else {
            mClippingBL.set(x, y);
            mClippingWH.set(w, h);
        }
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
            mTouchSound.play(Lol.getGameFact(mScene.mConfig, "volume", 1));
        return mTapHandler != null && mTapHandler.go(touchVec.x, touchVec.y);
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
    void setBoxPhysics(float density, float elasticity, float friction, BodyDef.BodyType type, boolean isProjectile, float x,
                       float y) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(mSize.x / 2, mSize.y / 2);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = mScene.mWorld.createBody(boxBodyDef);

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
    void setPolygonPhysics(float density, float elasticity, float friction, BodyDef.BodyType type, boolean isProjectile,
                           float x, float y, float... vertices) {
        PolygonShape shape = new PolygonShape();
        Vector2[] verts = new Vector2[vertices.length / 2];
        for (int i = 0; i < vertices.length; i += 2)
            verts[i / 2] = new Vector2(vertices[i], vertices[i + 1]);
        // print some debug info, since vertices are tricky
        for (Vector2 vert : verts)
            Lol.message(mScene.mConfig, "vert", "at " + vert.x + "," + vert.y);
        shape.set(verts);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = mScene.mWorld.createBody(boxBodyDef);

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
    void setCirclePhysics(float density, float elasticity, float friction, BodyDef.BodyType type, boolean isProjectile,
                          float x, float y, float radius) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = mScene.mWorld.createBody(boxBodyDef);

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

    public int getInfoInt() {
        return mInfoInt;
    }

    public void setInfoInt(int newVal) {
        mInfoInt = newVal;
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
     *
     * @param x The new X position
     * @param y The new Y position
     */
    public void setPosition(float x, float y) {
        mBody.setTransform(x + mSize.x / 2, y + mSize.y / 2, mBody.getAngle());
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
            mDisappearSound.play(Lol.getGameFact(mScene.mConfig, "volume", 1));

        // This is a bit of a hack... to do a disappear animation after we've
        // removed the actor, we draw an obstacle, so that we have a clean hook
        // into the animation system, but we disable its physics
        if (mDisappearAnimation != null) {
            float x = getXPosition() + mDisappearAnimateOffset.x;
            float y = getYPosition() + mDisappearAnimateOffset.y;
            BaseActor o = new BaseActor(mScene, "", mDisappearAnimateSize.x, mDisappearAnimateSize.y);
            o.setBoxPhysics(0, 0, 0, BodyDef.BodyType.StaticBody, false, x, y);
            mScene.addActor(o, 0);

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
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            if (immuneToPhysics)
                mBody.setType(BodyDef.BodyType.KinematicBody);
            else
                mBody.setType(BodyDef.BodyType.DynamicBody);

        // Add to the velocity of the actor
        Vector2 v = mBody.getLinearVelocity();
        v.y += y;
        v.x += x;
        updateVelocity(v.x, v.y);
        // Disable sensor, or else this actor will go right through walls
        setCollisionsEnabled(true);
    }

    /**
     * Internal method for updating an actor's velocity, so that we can go
     * its direction correctly
     *
     * @param x The new x velocity
     * @param y The new y velocity
     */
    void updateVelocity(float x, float y) {
        // make sure it is moveable... heroes are already Dynamic, let's just
        // set everything else that is static to kinematic... that's probably
        // safest.
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            mBody.setType(BodyDef.BodyType.KinematicBody);
        onUpdateVelocity();
        mBody.setLinearVelocity(x, y);
    }

    /**
     * No-op in this actor, but we need it for updateVelocity to work correctly
     * <p>
     * TODO: consider refactoring so that we don't need this
     */
    void onUpdateVelocity() {
    }

    /**
     * Set the absolute velocity of this actor
     *
     * @param x Velocity in X dimension
     * @param y Velocity in Y dimension
     */
    public void setAbsoluteVelocity(float x, float y, boolean immuneToPhysics) {
        // ensure this is a moveable actor
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            if (immuneToPhysics)
                mBody.setType(BodyDef.BodyType.KinematicBody);
            else
                mBody.setType(BodyDef.BodyType.DynamicBody);

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

    // TODO: create a test for this
    public void setTapCallback(final LolAction action) {
        mTapHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                action.go();
                return true;
            }
        };
    }

    // TODO: create a test for this
    public void setToggleCallback(final LolAction whileDownAction, final LolAction onUpAction) {
        whileDownAction.mIsActive = false;

        // set up the toggle behavior
        mToggleHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                if (isUp) {
                    whileDownAction.mIsActive = false;
                    if (onUpAction != null)
                        onUpAction.go();
                } else {
                    whileDownAction.mIsActive = true;
                }
                return true;
            }
        };
        mScene.mRepeatEvents.add(whileDownAction);
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
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            mBody.setType(BodyDef.BodyType.KinematicBody);

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
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            mBody.setType(BodyDef.BodyType.KinematicBody);
        mBody.setAngularVelocity(duration);
    }

    /**
     * Indicate that when the player touches this obstacle, we should play a
     * sound
     *
     * @param sound The name of the sound file to play
     */
    public void setTouchSound(String sound) {
        mTouchSound = mScene.mMedia.getSound(sound);
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
     * Save an animation sequence for showing when we getLoseScene rid of a actor
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
        Timer.schedule(new Timer.Task() {
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
        Timer.schedule(new Timer.Task() {
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
     */
    public void setImage(String imgName) {
        mAnimator.updateImage(mScene, imgName);
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
        final Timer.Task t = new Timer.Task() {
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
     * Indicate that this actor should be immune to the force of gravity
     */
    public void setGravityDefy() {
        mBody.setGravityScale(0);
    }

    /**
     * Set the sound to play when this actor disappears
     *
     * @param soundName Name of the sound file
     */
    public void setDisappearSound(String soundName) {
        mDisappearSound = mScene.mMedia.getSound(soundName);
    }

    /**
     * By default, non-hero actors are not subject to gravity or forces until
     * they are given a path, velocity, or other form of motion. This lets an
     * actor be subject to forces... in practice, using this in a side-scroller
     * means the actor will fall to the ground.
     */
    public void setCanFall() {
        mBody.setType(BodyDef.BodyType.DynamicBody);
    }

    /**
     * Indicate that this actor's rotation should be determined by the direction
     * in which it is traveling
     */
    public void setRotationByDirection() {
        mScene.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                // go rotating the hero based on the direction it faces
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
        // Coerce index into legal range
        if (zIndex < -2) {
            zIndex = -2;
        }
        if (zIndex > 2) {
            zIndex = 2;
        }
        mScene.removeActor(this, mZIndex);
        mZIndex = zIndex;
        mScene.addActor(this, mZIndex);
    }

    /**
     * In some cases, we need to force an actor to have a kinematic body type
     */
    public void setKinematic() {
        if (mBody.getType() != BodyDef.BodyType.KinematicBody)
            mBody.setType(BodyDef.BodyType.KinematicBody);
    }
}
