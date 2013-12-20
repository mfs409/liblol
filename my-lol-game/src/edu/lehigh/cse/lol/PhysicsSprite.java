
package edu.lehigh.cse.lol;

// TODO: enable arbitrary polygon creation?

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
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.Animation.AnimationDriver;
import edu.lehigh.cse.lol.Level.Action;
import edu.lehigh.cse.lol.Level.TouchAction;

public abstract class PhysicsSprite implements Renderable {
    /*
     * TYPES AND INTERFACES
     */

    /**
     * This enum encapsulates the different types of PhysicsSprite entities
     */
    enum SpriteId {
        UNKNOWN(0), HERO(1), ENEMY(2), GOODIE(3), PROJECTILE(4), OBSTACLE(5), SVG(6), DESTINATION(7);

        /**
         * To each ID, we attach an integer value, so that we can compare the
         * different IDs and establish a hierarchy for collision management
         */
        public final int _id;

        /**
         * Construct by providing the integral id
         * 
         * @param id The unique integer for this SpriteId
         */
        SpriteId(int id) {
            _id = id;
        }
    }

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
         * @param ps The PhysicsSprite involved in the collision
         * @param c A description of the contact, in case it is useful
         */
        void go(final PhysicsSprite ps, Contact c);
    }

    /*
     * FIELDS
     */

    /**
     * Type of this PhysicsSprite; useful for disambiguation in collision
     * detection
     */
    SpriteId _psType;

    /**
     * Physics body for this PhysicsSprite
     */
    Body _physBody;

    /**
     * Track whether the underlying body is a circle or box
     */
    boolean _isCircle;

    /**
     * Track if the entity is currently being rendered. This is a proxy for
     * "is important to the rest of the game" and when it is false, we don't run
     * any updates on the PhysicsSprite
     */
    boolean _visible = true;

    /**
     * The default image to display 
     * 
     * TODO: consider moving into AnimationDriver?
     */
    TextureRegion _tr;

    /**
     * Does the entity's image flip when the hero moves backwards?
     */
    private boolean _reverseFace = false;

    /**
     * We may opt to flip the image when it is moving in the -X direction. If
     * so, this tracks if the image is flipped, so that we draw its sprite
     * correctly. 
     * 
     * TODO: consider moving into AnimationDriver?
     */
    private boolean _flipped;

    /**
     * The z index of this entity. Valid range is [-2, 2]
     */
    int _zIndex = 0;

    /**
     * The width of the PhysicsSprite
     */
    float _width;

    /**
     * The height of the PhysicsSprite
     */
    float _height;

    /**
     * Does this entity follow a route? If so, the RouteDriver will be used to
     * advance the entity along its route.
     */
    private RouteDriver _route;

    /**
     * Text that game designer can modify to hold additional information
     */
    String _infoText = "";

    /**
     * Some PhysicsSprites run custom code when they are touched. This is a
     * reference to the code to run.
     */
    TouchAction _touchResponder;

    /**
     * When the camera follows the entity without centering on it, this gives us
     * the difference between the hero and camera
     */
    Vector2 _cameraOffset = new Vector2(0, 0);

    /**
     * Sometimes a hero collides with an obstacle, and should stick to it. In
     * that case, we create a pair of joints to connect the two entities. This
     * is the Distance joint that connects them (Note: for convenience, we store
     * the joints in a common parent class)
     */
    DistanceJoint _dJoint;

    /**
     * Sometimes a hero collides with an obstacle, and should stick to it. In
     * that case, we create a pair of joints to connect the two entities. This
     * is the Weld joint that connects them (Note: for convenience, we store the
     * joints in a common parent class)
     */
    WeldJoint _wJoint;

    /**
     * When we have PhysicsSprites stuck together, we might want to set a brief
     * delay before they can re-join. This field represents that delay time, in
     * milliseconds.
     */
    long _stickyDelay;

    /**
     * a sound to play when the obstacle is touched
     */
    Sound _touchSound;

    /**
     * Animation support: this tracks the current state of the active animation
     * (if any)
     */
    AnimationDriver _animator = new AnimationDriver();

    /**
     * Animation support: the cells of the default animation
     */
    Animation _defaultAnimation;

    /**
     * Animation support: the cells of the disappearance animation
     */
    Animation _disappearAnimation;

    /**
     * Animation support: the offset for placing the disappearance animation
     * relative to the disappearing _sprite
     */
    final Vector2 _disappearAnimateOffset = new Vector2();

    /**
     * Animation support: the width of the disappearance animation
     */
    float _disappearAnimateWidth;

    /**
     * Animation support: the height of the disappearance animation
     */
    float _disappearAnimateHeight;

    /**
     * A vector for computing hover placement
     */
    Vector3 _hoverVector = new Vector3();

    /**
     * Track if heroes stick to the top of this PhysicsSprite
     */
    boolean isStickyTop;

    /**
     * Track if heroes stick to the bottom of this PhysicsSprite
     */
    boolean isStickyBottom;

    /**
     * Track if heroes stick to the left side of this PhysicsSprite
     */
    boolean isStickyLeft;

    /**
     * Track if heroes stick to the right side of this PhysicsSprite
     */
    boolean isStickyRight;

    /**
     * Sound to play when this disappears
     */
    protected Sound _disappearSound = null;

    /**
     * Disable 3 of 4 sides of a PhysicsSprite, to allow walking through walls.
     * The value reflects the side that remains active. 0 is bottom, 1 is right,
     * 2 is top, 3 is left
     */
    int _isOneSided = -1;

    /**
     * Entities with a matching nonzero Id don't collide with each other
     */
    int _passThroughId = 0;

    /**
     * Create a new PhysicsSprite that does not yet have physics, but that has a
     * renderable picture
     * 
     * @param imgName The image to display
     * @param id The type of PhysicsSprite
     * @param width The width
     * @param height The height
     */
    PhysicsSprite(String imgName, SpriteId id, float width, float height) {
        _psType = id;
        // minor hack so that we can have invalid png files for invisible images
        TextureRegion[] tra = Media.getImage(imgName);
        if (tra != null) {
            _tr = new TextureRegion(tra[0]);
        }
        _width = width;
        _height = height;
    }

    /**
     * Each descendant defines this to address any custom logic that we need to
     * deal with on a collision
     * 
     * @param other The other entity involved in the collision
     */
    abstract void onCollide(PhysicsSprite other, Contact contact);

    /**
     * Internal method for updating an entity's velocity, so that we can handle
     * its direction correctly
     * 
     * @param x The new x velocity
     * @param y The new y velocity
     */
    void updateVelocity(float x, float y) {
        if (_dJoint != null) {
            Level._currLevel._world.destroyJoint(_dJoint);
            _dJoint = null;
            Level._currLevel._world.destroyJoint(_wJoint);
            _wJoint = null;
        }
        _physBody.setLinearVelocity(x, y);
    }

    /**
     * When this PhysicsSprite is touched (down press), we run this code
     * 
     * @param x The X coordinate that was touched
     * @param y The Y coordinate that was touched
     */
    void handleTouchDown(float x, float y) {
        if (_touchSound != null)
            _touchSound.play();
        if (_touchResponder != null)
            _touchResponder.onDown(x, y);
    }

    /**
     * When this PhysicsSprite is touched (move/drag press), we run this code
     * 
     * @param x The X coordinate that was touched
     * @param y The Y coordinate that was touched
     */
    boolean handleTouchDrag(float x, float y) {
        if (_touchResponder != null) {
            _touchResponder.onMove(x, y);
            return false;
        }
        return true;
    }

    /**
     * Every time the world advances by a timestep, we call this code. It
     * updates the PhysicsSprite and draws it.
     */
    @Override
    public void render(SpriteBatch _spriteRender, float delta) {
        // skip all rendering and updates if not visible
        if (_visible) {
            // possibly run a route update
            if (_route != null && _visible)
                _route.drive();

            // choose the default TextureRegion to show... this is how we
            // animate
            TextureRegion tr = _tr;
            // If we've got an in-flight animation, switch to it
            if (_animator.isActive())
                tr = _animator.getTr(delta);

            // now draw this sprite, flipping it if necessary
            Vector2 pos = _physBody.getPosition();
            if (_reverseFace && _physBody.getLinearVelocity().x < 0) {
                if (!_flipped) {
                    tr.flip(true, false);
                    _flipped = true;
                }
            } else if (_reverseFace && _physBody.getLinearVelocity().x > 0) {
                if (_flipped) {
                    tr.flip(true, false);
                    _flipped = false;
                }
            }
            if (tr != null)
                _spriteRender.draw(tr, pos.x - _width / 2, pos.y - _height / 2, _width / 2,
                        _height / 2, _width, _height, 1, 1,
                        MathUtils.radiansToDegrees * _physBody.getAngle());
        }
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Set any additional information for this sprite
     * 
     * @param text Text coming in from the programmer
     */
    public void setInfoText(String text) {
        _infoText = text;
    }

    /**
     * Retrieve any additional information for this sprite
     * 
     * @return The string that the programmer provided
     */
    public String getInfoText() {
        return _infoText;
    }

    /**
     * Make the camera follow the entity, but without centering the entity on
     * the screen
     * 
     * @param x Amount of x distance between entity and center
     * @param y Amount of y distance between entity and center
     */
    public void setCameraOffset(float x, float y) {
        _cameraOffset.x = x;
        _cameraOffset.y = y;
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
    void setBoxPhysics(float density, float elasticity, float friction, BodyType type,
            boolean isProjectile, float x, float y) {
        PolygonShape boxPoly = new PolygonShape();
        boxPoly.setAsBox(_width / 2, _height / 2);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + _width / 2;
        boxBodyDef.position.y = y + _height / 2;
        _physBody = Level._currLevel._world.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.density = density;
        fd.restitution = elasticity;
        fd.friction = friction;
        fd.shape = boxPoly;
        // NB: could use fd.filter to prevent some from colliding with others...
        _physBody.createFixture(fd);

        // link the body to the sprite
        _physBody.setUserData(this);
        boxPoly.dispose();

        if (isProjectile)
            _physBody.setBullet(true);

        // remember this is a box
        _isCircle = false;
    }

    /**
     * Specify that this entity should have a circular _physics shape
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
    void setCirclePhysics(float density, float elasticity, float friction, BodyType type,
            boolean isProjectile, float x, float y, float r) {
        _isCircle = true;
        CircleShape c = new CircleShape();
        c.setRadius(r);
        BodyDef boxBodyDef = new BodyDef();
        boxBodyDef.type = type;
        boxBodyDef.position.x = x + _width / 2;
        boxBodyDef.position.y = y + _height / 2;
        _physBody = Level._currLevel._world.createBody(boxBodyDef);

        FixtureDef fd = new FixtureDef();
        fd.density = density;
        fd.restitution = elasticity;
        fd.friction = friction;
        fd.shape = c;
        // NB: could use fd.filter to prevent some from colliding with others...
        _physBody.createFixture(fd);
        c.dispose();

        if (isProjectile)
            _physBody.setBullet(true);

        // link the body to the sprite
        _physBody.setUserData(this);
    }

    /**
     * Change whether this entity engages in physics collisions or not
     * 
     * @param state either true or false. true indicates that the object will
     *            participate in physics collisions. false indicates that it
     *            will not.
     */
    public void setCollisionEffect(boolean state) {
        _physBody.getFixtureList().get(0).setSensor(!state);
    }

    /**
     * Allow the user to adjust the default physics settings (density,
     * elasticity, friction) for this entity
     * 
     * @param density New density of the entity
     * @param elasticity New elasticity of the entity
     * @param friction New friction of the entity
     */
    public void setPhysics(float density, float elasticity, float friction) {
        _physBody.getFixtureList().get(0).setDensity(density);
        _physBody.resetMassData();
        _physBody.getFixtureList().get(0).setRestitution(elasticity);
        _physBody.getFixtureList().get(0).setFriction(friction);
    }

    /**
     * Indicate that this entity should not rotate due to torque
     */
    public void disableRotation() {
        _physBody.setFixedRotation(true);
    }

    /**
     * Returns the X coordinate of this entity
     * 
     * @return x coordinate of bottom left corner
     */
    public float getXPosition() {
        return _physBody.getPosition().x - _width / 2;
    }

    /**
     * Returns the Y coordinate of this entity
     * 
     * @return y coordinate of bottom left corner
     */
    public float getYPosition() {
        return _physBody.getPosition().y - _height / 2; // return
                                                        // _sprite.getY();
    }

    /**
     * Returns the width of this entity
     * 
     * @return the entity's width
     */
    public float getWidth() {
        return _width;
    }

    /**
     * Return the height of this entity
     * 
     * @return the entity's height
     */
    public float getHeight() {
        return _height;
    }

    /**
     * Indicate that the entity should move with the tilt of the phone
     */
    public void setMoveByTilting() {
        // If we've already added this to the set of tiltable objects, don't do
        // it again
        if (Level._currLevel._tilt._accelEntities.contains(this))
            return;

        // make sure it is moveable, add it to the list of tilt entities
        if (_physBody.getType() != BodyType.DynamicBody)
            _physBody.setType(BodyType.DynamicBody);
        Level._currLevel._tilt._accelEntities.add(this);
        // turn off sensor behavior, so this collides with stuff...
        _physBody.getFixtureList().get(0).setSensor(false);
    }

    /**
     * Call this on an Entity to rotate it. Note that this works best on boxes.
     * 
     * @param rotation amount to rotate the Entity (in degrees)
     */
    public void setRotation(float rotation) {
        // NB: the javadocs say "radians", but this appears to want rotation in
        // degrees
        _physBody.setTransform(_physBody.getPosition(), rotation);
    }

    /**
     * Make an entity disappear
     * 
     * @param quiet True if the disappear sound should not be played
     */
    public void remove(boolean quiet) {
        // set it invisible immediately, so that future calls know to ignore
        // this PhysicsSprite
        _visible = false;
        _physBody.setActive(false);

        // play a sound when we hit this thing?
        if (_disappearSound != null && !quiet)
            _disappearSound.play();

        // This is a bit slimy... we draw an obstacle here, so that we have a
        // clean hook into the animation system, but
        // we disable its physics
        if (_disappearAnimation != null) {
            float x = getXPosition() + _disappearAnimateOffset.x;
            float y = getYPosition() + _disappearAnimateOffset.y;
            Obstacle o = Obstacle.makeAsBox(x, y, _disappearAnimateWidth, _disappearAnimateHeight,
                    "");
            o._physBody.setActive(false);
            o.setDefaultAnimation(_disappearAnimation);
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
    public void addVelocity(float x, float y, boolean immuneToPhysics) {
        // ensure this is a moveable entity
        if (_physBody.getType() == BodyType.StaticBody)
            if (immuneToPhysics)
                _physBody.setType(BodyType.KinematicBody);
            else
                _physBody.setType(BodyType.DynamicBody);

        // Add to the velocity of the entity
        Vector2 v = _physBody.getLinearVelocity();
        v.y += y;
        v.x += x;
        updateVelocity(v.x, v.y);
        // Disable sensor, or else this entity will go right through walls
        setCollisionEffect(true);
    }

    /**
     * Set the absolute velocity of this Entity
     * 
     * @param x Velocity in X dimension
     * @param y Velocity in Y dimension
     */
    public void setAbsoluteVelocity(float x, float y, boolean immuneToPhysics) {
        // ensure this is a moveable entity
        if (_physBody.getType() == BodyType.StaticBody)
            if (immuneToPhysics)
                _physBody.setType(BodyType.KinematicBody);
            else
                _physBody.setType(BodyType.DynamicBody);

        // change its velocity
        Vector2 v = _physBody.getLinearVelocity();
        v.y = y;
        v.x = x;
        updateVelocity(v.x, v.y);
        // Disable sensor, or else this entity will go right through walls
        setCollisionEffect(true);
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
    public void setTouchTrigger(final int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4, final boolean disappear) {
        final int[] _touchTriggerActivation = new int[] {
                activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4
        };
        // set the code to run on touch
        _touchResponder = new TouchAction() {
            @Override
            public void onDown(float x, float y) {
                // check if we've got enough goodies
                boolean match = true;
                for (int i = 0; i < 4; ++i)
                    match &= _touchTriggerActivation[i] <= Level._currLevel._score._goodiesCollected[i];
                // if so, run the trigger
                if (match) {
                    if (disappear)
                        remove(false);
                    LOL._game.onTouchTrigger(id, LOL._game._currLevel, PhysicsSprite.this);
                }
            }
        };
    }

    /**
     * Returns the X velocity of of this entity
     * 
     * @return float Velocity in X dimension
     */
    public float getXVelocity() {
        return _physBody.getLinearVelocity().x;
    }

    /**
     * Returns the Y velocity of of this entity
     * 
     * @return float Velocity in Y dimension
     */
    public float getYVelocity() {
        return _physBody.getLinearVelocity().y;
    }

    /**
     * Make this entity move according to a route. The entity will loop back to
     * the beginning of the route.
     * 
     * @param route The route to follow.
     * @param velocity speed at which to travel
     * @param loop Should this route loop continuously
     */
    public void setRoute(Route route, float velocity, boolean loop) {
        // This must be a KinematicBody!
        if (_physBody.getType() == BodyType.StaticBody)
            _physBody.setType(BodyType.KinematicBody);

        // Create a RouteDriver to advance the entity's position according to
        // the route
        _route = new RouteDriver(route, velocity, loop, this);
    }

    /**
     * Make the entity continuously rotate. This is usually only useful for
     * fixed objects.
     * 
     * @param duration Time it takes to complete one rotation
     */
    public void setRotationSpeed(float duration) {
        if (_physBody.getType() == BodyType.StaticBody)
            _physBody.setType(BodyType.KinematicBody);
        _physBody.setAngularVelocity(duration);
    }

    /**
     * Call this on an entity to make it draggable. Be careful when dragging
     * things. If they are small, they will be hard to touch.
     */
    public void setCanDrag(boolean immuneToPhysics) {
        if (immuneToPhysics)
            _physBody.setType(BodyType.KinematicBody);
        else
            _physBody.setType(BodyType.DynamicBody);
        _touchResponder = new TouchAction() {
            @Override
            public void onMove(float x, float y) {
                _physBody.setTransform(x, y, _physBody.getAngle());
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
        _touchSound = Media.getSound(sound);
    }

    /**
     * Call this on an Entity to make it pokeable Poke the entity, then poke the
     * screen, and the entity will move to the location that was pressed. Poke
     * the entity twice in rapid succession to delete it.
     */
    public void setPokeToPlace(long deleteThresholdMillis) {
        // convert threshold to nanoseconds
        final long deleteThreshold = deleteThresholdMillis * 1000000;
        // set the code to run on touch
        _touchResponder = new TouchAction() {
            long _lastPokeTime;

            @Override
            public void onDown(float x, float y) {
                LOL._game.vibrate(100);
                long time = System.nanoTime();
                // double touch
                if ((time - _lastPokeTime) < deleteThreshold) {
                    // hide sprite, disable physics
                    _physBody.setActive(false);
                    _visible = false;
                    Level._currLevel._touchResponder = null;
                    return;
                }
                // repeat single-touch
                else {
                    _lastPokeTime = time;
                }
                // set a screen handler to detect when/where to move the entity
                Level._currLevel._touchResponder = new TouchAction() {
                    @Override
                    public void onDown(float x, float y) {
                        LOL._game.vibrate(100);
                        // move the object
                        _physBody.setTransform(x, y, _physBody.getAngle());
                        // clear the Level responder
                        Level._currLevel._touchResponder = null;
                    }

                };
            }
        };
    }

    /**
     * Indicate that this entity can be flicked on the screen
     * 
     * @param dampFactor A value that is multiplied by the vector for the flick,
     *            to affect speed
     */
    public void setFlickable(final float dampFactor) {
        // make sure the body is a dynamic body, because it really doesn't make
        // sense to flick it otherwise
        if (_physBody.getType() != BodyType.DynamicBody)
            _physBody.setType(BodyType.DynamicBody);
        // register a handler so that when this entity is touched, we'll start
        // processing a flick
        _touchResponder = new TouchAction() {
            @Override
            public void onDown(float x, float y) {
                LOL._game.vibrate(100);
                // remember the current position of the entity
                final float initialX = _physBody.getPosition().x;
                final float initialY = _physBody.getPosition().y;
                // set a handler to run when the screen is touched
                Level._currLevel._touchResponder = new TouchAction() {
                    @Override
                    public void onUp(float x, float y) {
                        // If the entity isn't visible we're done
                        if (_visible) {
                            // if the entity was hovering, stop hovering
                            _hoverVector = null;
                            // compute velocity for the flick and apply velocity
                            updateVelocity((x - initialX) * dampFactor, (y - initialY) * dampFactor);
                            // Unregister this handler... the flick is done
                            Level._currLevel._touchResponder = null;
                        }
                    }
                };
            }
        };
    }

    /**
     * Indicate that the poke path mechanism should cause the entity to move at
     * a constant velocity, rather than a velocity scaled to the distance of
     * travel
     * 
     * @param velocity The constant velocity for poke movement
     */
    // TODO: rethink these parameters a little bit more, then clean up levels 71
    // and 79. Are there really 8 possible behaviors, or is the real number much
    // smaller? I think the right answer is "2"
    public void setPokePath(final float velocity, final boolean oncePerTouch,
            final boolean updateOnMove, final boolean stopOnUp) {
        if (_physBody.getType() == BodyType.StaticBody)
            _physBody.setType(BodyType.KinematicBody);
        _touchResponder = new TouchAction() {
            @Override
            public void onDown(float x, float y) {
                LOL._game.vibrate(5);
                Level._currLevel._touchResponder = new TouchAction() {
                    @Override
                    public void onDown(float x, float y) {
                        Route r = new Route(2).to(getXPosition(), getYPosition()).to(
                                x - _width / 2, y - _height / 2);
                        setAbsoluteVelocity(0, 0, false);
                        setRoute(r, velocity, false);
                        // clear the pokePathEntity, so a future touch starts
                        // the process over
                        if (oncePerTouch)
                            Level._currLevel._touchResponder = null;
                    }

                    @Override
                    public void onMove(float x, float y) {
                        if (updateOnMove)
                            onDown(x, y);
                    }

                    @Override
                    public void onUp(float x, float y) {
                        if (stopOnUp && _route != null) {
                            _route.haltRoute();
                        }
                    }
                };
            }
        };
    }

    /**
     * Save the animation sequence and start it right away
     * 
     * @param cells which cells of the _sprite to show
     * @param durations duration for each cell
     */
    public void setDefaultAnimation(Animation a) {
        _defaultAnimation = a;
        // we'll assume we're using the default animation as our first
        // animation...
        _animator.setCurrentAnimation(_defaultAnimation);
    }

    /**
     * Save an animation sequence for showing when we get rid of a _sprite
     * 
     * @param cells which cells of the _sprite to show
     * @param durations duration for each cell
     * @param imageName Name of the image to display for the disappear animation
     * @param offsetX X offset of top left corner relative to hero top left
     * @param offsetY Y offset of top left corner relative to hero top left
     * @param width Width of the animation image
     * @param height Height of the animation image
     */
    public void setDisappearAnimation(Animation a, float offsetX, float offsetY, float width,
            float height) {
        _disappearAnimation = a;
        _disappearAnimateOffset.x = offsetX;
        _disappearAnimateOffset.y = offsetY;
        _disappearAnimateWidth = width;
        _disappearAnimateHeight = height;
    }

    /**
     * Indicate that this entity's image should be reversed when it is moving in
     * the negative x direction. For tilt, this only applies to the last hero
     * created. For velocity, it applies to everyone.
     */
    public void setCanFaceBackwards() {
        _reverseFace = true;
    }

    /**
     * Indicate that something should not appear quite yet...
     * 
     * @param delay How long to wait before displaying the thing
     */
    public void setAppearDelay(float delay) {
        _visible = false;
        _physBody.setActive(false);
        Timer.schedule(new Task() {
            @Override
            public void run() {
                _visible = true;
                _physBody.setActive(true);
            }
        }, delay);
    }

    /**
     * Indicate that something should disappear after a little while
     * 
     * @param delay How long to wait before hiding the thing
     * @param quiet true if the item should disappear quietly, false if it
     *            should play its disappear sound
     */
    public void setDisappearDelay(float delay, final boolean quiet) {
        Timer.schedule(new Task() {
            @Override
            public void run() {
                remove(quiet);
            }
        }, delay);
    }

    public void setImage(String imgName) {
        // minor hack so that we can have invalid png files for invisible images
        TextureRegion[] tra = Media.getImage(imgName);
        if (tra != null) {
            _tr = new TextureRegion(tra[0]);
        } else {
            _tr = null;
        }
    }

    public void resize(float x, float y, float width, float height) {
        _width = width;
        _height = height;
        Body _oldBody = _physBody;
        Fixture _oldFix = _oldBody.getFixtureList().get(0);
        if (_isCircle) {
            setCirclePhysics(_oldFix.getDensity(), _oldFix.getRestitution(), _oldFix.getFriction(),
                    _oldBody.getType(), _oldBody.isBullet(), x, y, (width > height) ? width / 2
                            : height / 2);
        } else {
            setBoxPhysics(_oldFix.getDensity(), _oldFix.getRestitution(), _oldFix.getFriction(),
                    _oldBody.getType(), _oldBody.isBullet(), x, y);
        }
        // clone forces
        _physBody.setAngularVelocity(_oldBody.getAngularVelocity());
        _physBody.setTransform(_physBody.getPosition(), _oldBody.getAngle());
        _physBody.setGravityScale(_oldBody.getGravityScale());
        _physBody.setLinearDamping(_oldBody.getLinearDamping());
        _physBody.setLinearVelocity(_oldBody.getLinearVelocity());
        // now disable the old body
        _oldBody.setActive(false);

    }

    /**
     * Indicate that this entity should shrink over time
     * 
     * @param shrinkX The number of pixels by which the X dimension should
     *            shrink each second
     * @param shrinkY The number of pixels by which the Y dimension should
     *            shrink each second
     */
    public void setShrinkOverTime(final float shrinkX, final float shrinkY,
            final boolean keepCentered) {
        final Task t = new Task() {
            @Override
            public void run() {
                if (_visible) {
                    float x, y;
                    if (keepCentered) {
                        x = getXPosition() + shrinkX / 20 / 2;
                        y = getYPosition() + shrinkY / 20 / 2;
                    } else {
                        x = getXPosition();
                        y = getYPosition();
                    }
                    float w = _width - shrinkX / 20;
                    float h = _height - shrinkY / 20;
                    if ((w > 0) && (h > 0)) {
                        _width = w;
                        _height = h;
                        Body _oldBody = _physBody;
                        Fixture _oldFix = _oldBody.getFixtureList().get(0);
                        if (_isCircle) {
                            setCirclePhysics(_oldFix.getDensity(), _oldFix.getRestitution(),
                                    _oldFix.getFriction(), _oldBody.getType(), _oldBody.isBullet(),
                                    x, y, (w > h) ? w / 2 : h / 2);
                        } else {
                            setBoxPhysics(_oldFix.getDensity(), _oldFix.getRestitution(),
                                    _oldFix.getFriction(), _oldBody.getType(), _oldBody.isBullet(),
                                    x, y);
                        }
                        // clone forces
                        _physBody.setAngularVelocity(_oldBody.getAngularVelocity());
                        _physBody.setTransform(_physBody.getPosition(), _oldBody.getAngle());
                        _physBody.setGravityScale(_oldBody.getGravityScale());
                        _physBody.setLinearDamping(_oldBody.getLinearDamping());
                        _physBody.setLinearVelocity(_oldBody.getLinearVelocity());
                        // now disable the old body
                        _oldBody.setActive(false);
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
     * Indicate that this entity should _hover at a specific location on the
     * screen, rather than being placed at some point on the level itself. Note
     * that the coordinates to this command are the center position of the
     * hovering entity.
     * 
     * @param x the X coordinate where the entity should appear
     * @param y the Y coordinate where the entity should appear
     */
    public void setHover(final int x, final int y) {
        _hoverVector = new Vector3();
        Level._currLevel._repeatEvents.add(new Action() {
            @Override
            public void go() {
                if (_hoverVector == null)
                    return;
                _hoverVector.x = x;
                _hoverVector.y = y;
                _hoverVector.z = 0;
                Level._currLevel._gameCam.unproject(_hoverVector);
                _physBody.setTransform(_hoverVector.x, _hoverVector.y, _physBody.getAngle());
            }
        });
    }

    /**
     * Indicate that this entity should have a force applied to it at all times,
     * e.g., to defy gravity.
     * 
     * @param x The magnitude of the force in the X direction
     * @param y The magnitude of the force in the Y direction
     */
    public void setGravityDefy() {
        _physBody.setGravityScale(0);
    }

    /**
     * Make this obstacle sticky, so that a hero will stick to it
     */
    public void setSticky(boolean top, boolean right, boolean bottom, boolean left) {
        isStickyTop = top;
        isStickyRight = right;
        isStickyBottom = bottom;
        isStickyLeft = left;
    }

    /**
     * Set the sound to play when this entity disappears
     * 
     * @param soundName Name of the sound file
     */
    public void setDisappearSound(String soundName) {
        _disappearSound = Media.getSound(soundName);
    }

    /**
     * Indicate that touching this hero should make it throw a projectile
     */
    public void setTouchToThrow(final Hero h) {
        _touchResponder = new TouchAction() {
            @Override
            public void onDown(float x, float y) {
                Projectile.throwFixed(h._physBody.getPosition().x, h._physBody.getPosition().y, h);
            }
        };
    }

    /**
     * Indicate that this obstacle only registers collisions on one side.
     * 
     * @param side The side that registers collisions. 0 is top, 1 is right, 2
     *            is bottom, 3 is left, -1 means "none"
     */
    public void setOneSided(int side) {
        _isOneSided = side;
    }

    /**
     * Indicate that this entity should not have collisions with any other
     * entity that has the same ID
     * 
     * @param id The number for this class of non-interacting entities
     */
    public void setPassThrough(int id) {
        _passThroughId = id;
    }

    /**
     * By default, non-hero entities are not subject to gravity until they are
     * given a path, velocity, or other form of motion. This lets an entity
     * simply fall.
     */
    public void setCanFall() {
        _physBody.setType(BodyType.DynamicBody);
    }

    /**
     * Specify that this enemy is supposed to chase the hero
     * 
     * @param speed The speed with which the enemy chases the hero
     */
    public void setChaseSpeed(final float speed, final PhysicsSprite target,
            final boolean chaseInX, final boolean chaseInY) {
        _physBody.setType(BodyType.DynamicBody);
        Level._currLevel._repeatEvents.add(new Action() {
            @Override
            public void go() {
                // don't chase something that isn't visible
                if (!target._visible)
                    return;
                // don't run if this sprite isn't visible
                if (!_visible)
                    return;

                // chase the _chaseTarget
                // compute vector between hero and enemy
                float x = target._physBody.getPosition().x - _physBody.getPosition().x;
                float y = target._physBody.getPosition().y - _physBody.getPosition().y;

                // normalize it
                float denom = (float)Math.sqrt(x * x + y * y);
                x /= denom;
                y /= denom;
                // multiply by speed
                x *= speed;
                y *= speed;
                if (!chaseInX)
                    x = _physBody.getLinearVelocity().x;
                if (!chaseInY)
                    y = _physBody.getLinearVelocity().y;
                // set Enemy velocity accordingly
                updateVelocity(x, y);
            }
        });
    }

    /**
     * Indicate that this hero's rotation should be determined by the direction
     * in which it is traveling
     */
    public void setRotationByDirection() {
        Level._currLevel._repeatEvents.add(new Action() {
            @Override
            public void go() {
                // handle rotating the hero based on the direction it faces
                if (_visible) {
                    float x = _physBody.getLinearVelocity().x;
                    float y = _physBody.getLinearVelocity().y;
                    double angle = Math.atan2(y, x) + Math.atan2(-1, 0);
                    _physBody.setTransform(_physBody.getPosition(), (float)angle);
                }
            }
        });
    }

    /**
     * Set the z plane for this sprite
     * 
     * @param zIndex The z plane. Values range from -2 to 2. The default is 0.
     */
    public void setZIndex(int zIndex) {
        Level._currLevel.removeSprite(this, _zIndex);
        this._zIndex = zIndex;
        Level._currLevel.addSprite(this, _zIndex);
    }
}
