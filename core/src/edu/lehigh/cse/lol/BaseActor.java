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
 * We use BaseActor as parent of both WorldActor (MainScene) and SceneActor (all other scenes), so that
 * core functionality (physics, animation) can be in one place, even though many of the features of
 * an WorldActor (MainScene) require a Score object, and are thus incompatible with non-Main scenes.
 */
public class BaseActor extends Renderable {
    /// The level in which this Actor exists
    final LolScene mScene;

    /// Physics body for this WorldActor
    Body mBody;
    /// Track if the underlying body is a circle
    private boolean mIsCircleBody;
    /// Track if the underlying body is a box
    private boolean mIsBoxBody;
    /// Track if the underlying body is a polygon
    private boolean mIsPolygonBody;
    /// The dimensions of the WorldActor... x is width, y is height
    Vector2 mSize;

    /// Animation support: this tracks the current state of the active animation (if any)
    Animation.Driver mAnimator;
    /// Animation support: the cells of the default animation
    Animation mDefaultAnimation;
    /// Animation support: the cells of the animation to use when moving backwards
    private Animation mDefaultReverseAnimation;
    /// The z index of this actor. Valid range is [-2, 2]
    private int mZIndex;

    /// Does this WorldActor follow a route? If so, the Driver will be used to advance the
    /// actor along its route.
    Route.Driver mRoute;

    /// Animation support: the cells of the disappearance animation
    private Animation mDisappearAnimation;
    /// Animation support: the dimensions of the disappearance animation
    private Vector2 mDisappearAnimateSize;
    /// Animation support: the offset for placing the disappearance animation relative to the
    // disappearing actor
    private final Vector2 mDisappearAnimateOffset;

    /// Text that game designer can modify to hold additional information about the actor
    private String mInfoText;
    /// Integer that the game designer can modify to hold additional information about the actor
    private int mInfoInt;

    /// Code to run when this actor is tapped
    TouchEventHandler mTapHandler;
    /// Code to run when this actor is held or released
    ToggleEventHandler mToggleHandler;

    /// Sound to play when the actor disappears
    Sound mDisappearSound;
    /// a sound to play when this actor is touched
    private Sound mTouchSound;

    /// A temporary vertex that we use when resizing
    private Vector2 mTempVector;

    /// Percentage of the way from bottom and left that we should clip
    private Vector2 mClippingBL;
    // Percentage of the way from top and right that we should clip
    private Vector2 mClippingWH;

    /**
     * Create a new BaseActor by creating an image that can be rendered to the screen
     *
     * @param scene   The scene into which this actor should be placed
     * @param imgName The image to show for this actor
     * @param width   The width of the actor's image and body, in meters
     * @param height  The height of the actor's image and body, in meters
     */
    BaseActor(LolScene scene, String imgName, float width, float height) {
        mScene = scene;
        mAnimator = new Animation.Driver(mScene.mMedia, imgName);
        mSize = new Vector2();
        mSize.x = width;
        mSize.y = height;
        mDisappearAnimateSize = new Vector2();
        mZIndex = 0;
        mDisappearAnimateOffset = new Vector2();
        mInfoText = "";
        mTempVector = new Vector2();
    }

    /**
     * Specify that this actor should have a rectangular physics shape
     *
     * @param type Is the actor's body static or dynamic?
     * @param x    The X coordinate of the bottom left corner, in meters
     * @param y    The Y coordinate of the bottom left corner, in meters
     */
    void setBoxPhysics(BodyDef.BodyType type, float x, float y) {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(mSize.x / 2, mSize.y / 2);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = mScene.mWorld.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        mBody.createFixture(fd);
        shape.dispose();
        setPhysics(0, 0, 0);

        // link the body to the actor
        mBody.setUserData(this);

        // remember this is a box
        mIsCircleBody = false;
        mIsBoxBody = true;
        mIsPolygonBody = false;
    }

    /**
     * Specify that this actor should have a polygon physics shape.
     * <p>
     * You must take extreme care when using this method. Polygon vertices must be given in
     * counter-clockwise order, and they must describe a convex shape.
     *
     * @param type     Is the actor's body static or dynamic?
     * @param x        The X coordinate of the bottom left corner, in meters
     * @param y        The Y coordinate of the bottom left corner, in meters
     * @param vertices Up to 16 coordinates representing the vertexes of this polygon, listed as
     *                 x0,y0,x1,y1,x2,y2,...
     */
    void setPolygonPhysics(BodyDef.BodyType type, float x, float y, float... vertices) {
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
        fd.shape = shape;
        mBody.createFixture(fd);
        shape.dispose();
        setPhysics(0, 0, 0);

        // link the body to the actor
        mBody.setUserData(this);

        // remember this is a polygon
        mIsCircleBody = false;
        mIsBoxBody = false;
        mIsPolygonBody = true;
    }

    /**
     * Specify that this actor should have a circular physics shape
     *
     * @param type   Is the actor's body static or dynamic?
     * @param x      The X coordinate of the bottom left corner, in meters
     * @param y      The Y coordinate of the bottom left corner, in meters
     * @param radius The radius of the underlying circle
     */
    void setCirclePhysics(BodyDef.BodyType type, float x, float y, float radius) {
        CircleShape shape = new CircleShape();
        shape.setRadius(radius);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + mSize.x / 2;
        boxBodyDef.position.y = y + mSize.y / 2;
        mBody = mScene.mWorld.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.shape = shape;
        mBody.createFixture(fd);
        shape.dispose();
        setPhysics(0, 0, 0);

        // link the body to the actor
        mBody.setUserData(this);

        // remember this is a box
        mIsCircleBody = true;
        mIsBoxBody = false;
        mIsPolygonBody = false;
    }

    /**
     * Indicate whether this actor is fast-moving, so that the physics simulator can do a better job
     * dealing with tunneling effects.
     *
     * @param state True or false, depending on whether it is fast-moving or not
     */
    void setFastMoving(boolean state) {
        mBody.setBullet(state);
    }

    /**
     * Internal method for updating an actor's velocity
     * <p>
     * We use this because we need to be careful about possibly breaking joints when we make the
     * actor move
     *
     * @param x The new x velocity
     * @param y The new y velocity
     */
    void updateVelocity(float x, float y) {
        // make sure it is not static... heroes are already Dynamic, let's just set everything else
        // that is static to kinematic... that's probably safest.
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            mBody.setType(BodyDef.BodyType.KinematicBody);
        breakJoints();
        mBody.setLinearVelocity(x, y);
    }

    /**
     * Break any joints that involve this actor, so that it can move freely.
     * <p>
     * NB: BaseActors don't have any joints to break, but classes that derive from BaseActor do
     */
    void breakJoints() {
    }

    /**
     * When this actor is touched, play its mTouchSound and then execute its mTapHandler
     *
     * @param touchVec The coordinates of the touch, in meters
     * @return True if the event was handled, false otherwise
     */
    boolean onTap(Vector3 touchVec) {
        if (mTouchSound != null)
            mTouchSound.play(Lol.getGameFact(mScene.mConfig, "volume", 1));
        return mTapHandler != null && mTapHandler.go(touchVec.x, touchVec.y);
    }

    /**
     * Every time the world advances by a timestep, we call this code to update the actor route and
     * animation, and then draw the actor
     *
     * @param sb    The spritebatch to use in order to draw this actor
     * @param delta The amount of time since the last render event
     */
    @Override
    void onRender(SpriteBatch sb, float delta) {
        // possibly run a route update
        if (mRoute != null)
            mRoute.drive();

        // choose the default TextureRegion to show... this is how we animate
        TextureRegion tr = mAnimator.getTr(delta);

        // Flip the animation?
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

        // Draw the actor
        Vector2 pos = mBody.getPosition();
        if (tr != null) {
            // If we are using FlipAndClip, we need a more complex drawing routine
            if (mClippingWH != null)
                sb.draw(tr.getTexture(),
                        // bottom left corner X, Y where we ought to draw
                        pos.x - mSize.x / 2 + mClippingBL.x * mSize.x,
                        pos.y - mSize.y / 2 + mClippingBL.y * mSize.y,
                        // offset the image by this much
                        0, 0,
                        // width and height of the image
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
                        // flip Y but not X
                        false, true);
            else
                sb.draw(tr, pos.x - mSize.x / 2, pos.y - mSize.y / 2, mSize.x / 2, mSize.y / 2,
                        mSize.x, mSize.y, 1, 1, MathUtils.radiansToDegrees * mBody.getAngle());
        }
    }

    /**
     * Indicate whether this actor engages in physics collisions or not
     *
     * @param state True or false, depending on whether the actor will participate in physics
     *              collisions or not
     */
    public void setCollisionsEnabled(boolean state) {
        // The default is for all fixtures of a actor have the same sensor state
        for (Fixture f : mBody.getFixtureList())
            f.setSensor(!state);
    }

    /**
     * Adjust the default physics settings (density, elasticity, friction) for this actor
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
     * Indicate that this actor should be immune to the force of gravity
     */
    public void setGravityDefy() {
        mBody.setGravityScale(0);
    }

    /**
     * Ensure that an actor is subject to gravitational forces.
     * <p>
     * By default, non-hero actors are not subject to gravity or forces until they are given a path,
     * velocity, or other form of motion. This lets an actor be subject to forces.  In practice,
     * using this in a side-scroller means the actor will fall to the ground.
     */
    public void setCanFall() {
        mBody.setType(BodyDef.BodyType.DynamicBody);
    }

    /**
     * Force an actor to have a Kinematic body type.  Kinematic bodies can move, but are not subject
     * to forces in the same way as Dynamic bodies.
     */
    public void setKinematic() {
        if (mBody.getType() != BodyDef.BodyType.KinematicBody)
            mBody.setType(BodyDef.BodyType.KinematicBody);
    }

    /**
     * Retrieve any additional text information for this actor
     *
     * @return The string that the programmer provided
     */
    public String getInfoText() {
        return mInfoText;
    }

    /**
     * Retrieve any additional numerical information for this actor
     *
     * @return The integer that the programmer provided
     */
    public int getInfoInt() {
        return mInfoInt;
    }

    /**
     * Set additional text information for this actor
     *
     * @param text Text to attach to the actor
     */
    public void setInfoText(String text) {
        mInfoText = text;
    }

    /**
     * Set additional numerical information for this actor
     *
     * @param newVal An integer to attach to the actor
     */
    public void setInfoInt(int newVal) {
        mInfoInt = newVal;
    }

    /**
     * Returns the X coordinate of this actor
     *
     * @return x coordinate of bottom left corner, in meters
     */
    public float getXPosition() {
        return mBody.getPosition().x - mSize.x / 2;
    }

    /**
     * Returns the Y coordinate of this actor
     *
     * @return y coordinate of bottom left corner, in meters
     */
    public float getYPosition() {
        return mBody.getPosition().y - mSize.y / 2;
    }

    /**
     * Change the position of an actor
     *
     * @param x The new X position, in meters
     * @param y The new Y position, in meters
     */
    public void setPosition(float x, float y) {
        mBody.setTransform(x + mSize.x / 2, y + mSize.y / 2, mBody.getAngle());
    }

    /**
     * Returns the width of this actor
     *
     * @return the actor's width, in meters
     */
    public float getWidth() {
        return mSize.x;
    }

    /**
     * Return the height of this actor
     *
     * @return the actor's height, in meters
     */
    public float getHeight() {
        return mSize.y;
    }

    /**
     * Change the size of an actor, and/or change its position
     *
     * @param x      The new X coordinate of its bottom left corner, in meters
     * @param y      The new Y coordinate of its bototm left corner, in meters
     * @param width  The new width of the actor, in meters
     * @param height The new height of the actor, in meters
     */
    public void resize(float x, float y, float width, float height) {
        // set new height and width
        mSize.set(width, height);
        // read old body information
        Body oldBody = mBody;
        Fixture oldFix = oldBody.getFixtureList().get(0);
        // make a new body
        if (mIsCircleBody) {
            setCirclePhysics(oldBody.getType(), x, y, (width > height) ? width / 2 : height / 2);
        } else if (mIsBoxBody) {
            setBoxPhysics(oldBody.getType(), x, y);
        } else if (mIsPolygonBody) {
            // we need to manually scale all the vertices
            float xScale = height / mSize.y;
            float yScale = width / mSize.x;
            PolygonShape ps = (PolygonShape) oldFix.getShape();
            float[] verts = new float[ps.getVertexCount() * 2];
            for (int i = 0; i < ps.getVertexCount(); ++i) {
                ps.getVertex(i, mTempVector);
                verts[2 * i] = mTempVector.x * xScale;
                verts[2 * i + 1] = mTempVector.y * yScale;
            }
            setPolygonPhysics(oldBody.getType(), x, y, verts);
        }
        // Update the user-visible physics values
        setPhysics(oldFix.getDensity(), oldFix.getRestitution(), oldFix.getFriction());
        setFastMoving(oldBody.isBullet());
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
     * Use this to find the current rotation of an actor
     *
     * @return The rotation, in radians
     */
    public float getRotation() {
        return mBody.getAngle();
    }

    /**
     * Call this on an actor to rotate it. Note that this works best on boxes.
     *
     * @param rotation amount to rotate the actor (in radians)
     */
    public void setRotation(float rotation) {
        mBody.setTransform(mBody.getPosition(), rotation);
    }

    /**
     * Make the actor continuously rotate. This is usually only useful for fixed objects.
     *
     * @param duration Time it takes to complete one rotation
     */
    public void setRotationSpeed(float duration) {
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            mBody.setType(BodyDef.BodyType.KinematicBody);
        mBody.setAngularVelocity(duration);
    }

    /**
     * Indicate that this actor should not rotate due to torque
     */
    public void disableRotation() {
        mBody.setFixedRotation(true);
    }

    /**
     * Make an actor disappear
     *
     * @param quiet True if the disappear sound should not be played
     */
    public void remove(boolean quiet) {
        // set it invisible immediately, so that future calls know to ignore this actor
        mEnabled = false;
        mBody.setActive(false);

        // play a sound when we remove this actor?
        if (mDisappearSound != null && !quiet)
            mDisappearSound.play(Lol.getGameFact(mScene.mConfig, "volume", 1));

        // To do a disappear animation after we've removed the actor, we draw an actor, so that
        // we have a clean hook into the animation system, but we disable its physics
        if (mDisappearAnimation != null) {
            float x = getXPosition() + mDisappearAnimateOffset.x;
            float y = getYPosition() + mDisappearAnimateOffset.y;
            BaseActor o = new BaseActor(mScene, "", mDisappearAnimateSize.x, mDisappearAnimateSize.y);
            o.setBoxPhysics(BodyDef.BodyType.StaticBody, x, y);
            mScene.addActor(o, 0);
            o.mBody.setActive(false);
            o.setDefaultAnimation(mDisappearAnimation);
        }
    }

    /**
     * Returns the X velocity of of this actor
     *
     * @return Velocity in X dimension, in meters per second
     */
    public float getXVelocity() {
        return mBody.getLinearVelocity().x;
    }

    /**
     * Returns the Y velocity of of this actor
     *
     * @return Velocity in Y dimension, in meters per second
     */
    public float getYVelocity() {
        return mBody.getLinearVelocity().y;
    }

    /**
     * Add velocity to this actor
     *
     * @param x Velocity in X dimension, in meters per second
     * @param y Velocity in Y dimension, in meters per second
     */
    public void addVelocity(float x, float y) {
        // ensure this is a moveable actor
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
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
     * Set the absolute velocity of this actor
     *
     * @param x Velocity in X dimension, in meters per second
     * @param y Velocity in Y dimension, in meters per second
     */
    public void setAbsoluteVelocity(float x, float y) {
        // ensure this is a moveable actor
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            mBody.setType(BodyDef.BodyType.DynamicBody);
        // change its velocity
        updateVelocity(x, y);
        // Disable sensor, or else this actor will go right through walls
        setCollisionsEnabled(true);
    }

    /**
     * Set a dampening factor to cause a moving body to slow down without colliding with anything
     *
     * @param amount The amount of damping to apply
     */
    public void setDamping(float amount) {
        mBody.setLinearDamping(amount);
    }

    /**
     * Set a dampening factor to cause a spinning body to decrease its rate of spin
     *
     * @param amount The amount of damping to apply
     */
    public void setAngularDamping(float amount) {
        mBody.setAngularDamping(amount);
    }

    /**
     * Specify some code to run when this actor is tapped
     *
     * @param handler The TouchEventHandler to run in response to the tap
     */
    public void setTapCallback(TouchEventHandler handler) {
        mTapHandler = handler;
    }

    /**
     * Specify some code to run while this actor is down-pressed and when it is released
     *
     * @param whileDownAction The code to run for as long as the actor is being pressed
     * @param onUpAction      The code to run when the actor is released
     */
    public void setToggleCallback(final LolAction whileDownAction, final LolAction onUpAction) {
        whileDownAction.mIsActive = false;

        // set up the toggle behavior
        mToggleHandler = new ToggleEventHandler() {
            public boolean go(boolean isUp, float worldX, float worldY) {
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
     * Request that this actor moves according to a fixed route
     *
     * @param route    The route to follow
     * @param velocity speed at which to travel along the route
     * @param loop     When the route completes, should we start it over again?
     */
    public void setRoute(Route route, float velocity, boolean loop) {
        // This must be a KinematicBody or a Dynamic Body!
        if (mBody.getType() == BodyDef.BodyType.StaticBody)
            mBody.setType(BodyDef.BodyType.KinematicBody);

        // Create a Driver to advance the actor's position according to the route
        mRoute = new Route.Driver(route, velocity, loop, this);
    }

    /**
     * Request that a sound plays whenever the player touches this actor
     *
     * @param sound The name of the sound file to play
     */
    public void setTouchSound(String sound) {
        mTouchSound = mScene.mMedia.getSound(sound);
    }

    /**
     * Request that a sound plays whenever this actor disappears
     *
     * @param soundName The name of the sound file to play
     */
    public void setDisappearSound(String soundName) {
        mDisappearSound = mScene.mMedia.getSound(soundName);
    }

    /**
     * Change the image being used to display the actor
     *
     * @param imgName The name of the new image file to use
     */
    public void setImage(String imgName) {
        mAnimator.updateImage(mScene.mMedia, imgName);
    }

    /**
     * Set the z plane for this actor
     *
     * @param zIndex The z plane. Values range from -2 to 2. The default is 0.
     */
    public void setZIndex(int zIndex) {
        // Coerce index into legal range, then move it
        zIndex = (zIndex < -2) ? -2 : zIndex;
        zIndex = (zIndex > 2) ? 2 : zIndex;
        mScene.removeActor(this, mZIndex);
        mZIndex = zIndex;
        mScene.addActor(this, mZIndex);
    }

    /**
     * Set the default animation sequence for this actor, and start playing it
     *
     * @param animation The animation to display
     */
    public void setDefaultAnimation(Animation animation) {
        mDefaultAnimation = animation;
        mAnimator.setCurrentAnimation(mDefaultAnimation);
    }

    /**
     * Set the animation sequence to use when the actor is moving in the negative X direction
     *
     * @param animation The animation to display
     */
    public void setDefaultReverseAnimation(Animation animation) {
        mDefaultReverseAnimation = animation;
    }

    /**
     * Set the animation sequence to use when the actor is removed from the world
     *
     * @param animation The animation to display
     * @param offsetX   Distance between the animation and the left side of the actor
     * @param offsetY   Distance between the animation and the bottom of the actor
     * @param width     The width of the animation, in case it's not the same as the actor width
     * @param height    The height of the animation, in case it's not the same as the actor height
     */
    public void setDisappearAnimation(Animation animation, float offsetX, float offsetY, float width, float height) {
        mDisappearAnimation = animation;
        mDisappearAnimateOffset.set(offsetX, offsetY);
        mDisappearAnimateSize.set(width, height);
    }

    /**
     * Set a time that should pass before this actor appears on the screen
     *
     * @param delay How long to wait before displaying the actor, in milliseconds
     */
    public void setAppearDelay(float delay) {
        mEnabled = false;
        mBody.setActive(false);
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                mEnabled = true;
                mBody.setActive(true);
            }
        }, delay);
    }

    /**
     * Request that this actor disappear after a specified amount of time
     *
     * @param delay How long to wait before hiding the actor, in milliseconds
     * @param quiet Should the item should disappear quietly, or play its disappear sound?
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
     * Indicate that this actor should shrink over time.  Note that using negative values will lead
     * to growing instead of shrinking.
     *
     * @param shrinkX      The number of meters by which the X dimension should shrink each second
     * @param shrinkY      The number of meters by which the Y dimension should shrink each second
     * @param keepCentered Should the actor's center point stay the same as it shrinks, or should
     *                     its bottom left corner stay in the same position
     */
    public void setShrinkOverTime(final float shrinkX, final float shrinkY, final boolean keepCentered) {
        // NB: we shrink 20 times per second
        final Timer.Task t = new Timer.Task() {
            @Override
            public void run() {
                if (mEnabled) {
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
                    // if the area remains >0, resize it and schedule a timer to run again
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
     * Indicate that this actor's rotation should change in response to its direction of motion
     */
    public void setRotationByDirection() {
        mScene.mRepeatEvents.add(new LolAction() {
            @Override
            public void go() {
                if (mEnabled) {
                    float x = mBody.getLinearVelocity().x;
                    float y = mBody.getLinearVelocity().y;
                    double angle = Math.atan2(y, x) + Math.atan2(-1, 0);
                    mBody.setTransform(mBody.getPosition(), (float) angle);
                }
            }
        });
    }


    /**
     * Specify that a limited amount of the actor should be displayed (image clipping)
     *
     * @param x The starting X position of the displayed portion, as a fraction from 0 to 1
     * @param y The starting Y position of the displayed portion, as a fraction from 0 to 1
     * @param w The width to display, as a fraction from 0 to 1
     * @param h The height to display, as a fraction from 0 to 1
     */
    public void setFlipAndClipRatio(float x, float y, float w, float h) {
        if (mClippingBL == null) {
            mClippingBL = new Vector2(x, y);
            mClippingWH = new Vector2(w, h);
        } else {
            mClippingBL.set(x, y);
            mClippingWH.set(w, h);
        }
    }
}
