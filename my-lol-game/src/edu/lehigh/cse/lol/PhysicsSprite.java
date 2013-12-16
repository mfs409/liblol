package edu.lehigh.cse.lol;

// TODO: clean up comments

// TODO: enable arbitrary polygon creation?

// TODO: get rid of static fields in this class?

// TODO: can we get rid of Vector2 objects and instead use the x and y directly?

// TODO: add vibration support

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

import edu.lehigh.cse.lol.Util.Action;
import edu.lehigh.cse.lol.Util.Renderable;

public abstract class PhysicsSprite implements Renderable
{
    /*
     * INTERNAL CLASSES
     */

    /**
     * This enum encapsulates the different types of PhysicsSprite entities
     */
    enum SpriteId
    {
        UNKNOWN(0), HERO(1), ENEMY(2), GOODIE(3), PROJECTILE(4), OBSTACLE(5), SVG(6), DESTINATION(7);

        /**
         * To each ID, we attach an integer value, so that we can compare the
         * different IDs and establish a hierarchy
         * for collision management
         */
        public final int _id;

        /**
         * Construct by providing the integral id
         * 
         * @param id
         *            The unique integer for this SpriteId
         */
        SpriteId(int id)
        {
            _id = id;
        }
    }

    /**
     * Track the image to display
     */
    TextureRegion _tr;

    /**
     * Type of this PhysicsSprite; useful for disambiguation in collision
     * detection
     */
    SpriteId      _psType;

    /**
     * Text that user can modify to hold additional information
     */
    String        _infoText = "";

    float         _width;

    float         _height;

    PhysicsSprite(String imgName, SpriteId id, float width, float height)
    {
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
     * Set any additional information for this sprite
     * 
     * @param text
     *            Text coming in from the programmer
     */
    public void setInfoText(String text)
    {
        _infoText = text;
    }

    /**
     * Retrieve any additional information for this sprite
     * 
     * @return The string that the programmer provided
     */
    public String getInfoText()
    {
        return _infoText;
    }

    /*
     * BASIC PHYSICS FUNCTIONALITY:
     * 
     * - PROVIDE A PHYSICS BODY AND AN ANIMATED SPRITE
     * 
     * - MANAGE SHAPE (BOX/CIRCLE), ROTATION, MOTION
     * 
     * - SUPPORT COLLISIONS
     */

    /**
     * Physics body for this object
     */
    Body              _physBody;

    /**
     * Track whether the underlying body is a circle or box
     */
    protected boolean _isCircle;

    /**
     * Each descendant defines this to address any custom logic that we need to
     * deal with on a collision
     * 
     * @param other
     *            The other entity involved in the collision
     */
    abstract void onCollide(PhysicsSprite other, Contact contact);

    /**
     * Specify that this entity should have a rectangular _physics shape
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
     *            Is this a bullet
     */
    void setBoxPhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile, float x,
            float y)
    {
        // NB: this is not a circle... it's a box...
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
     * @param density
     *            Density of the entity
     * @param elasticity
     *            Elasticity of the entity
     * @param friction
     *            Friction of the entity
     * @param type
     *            Is this static or dynamic?
     * @param isProjectile
     *            Is this a bullet
     */
    void setCirclePhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile,
            float x, float y, float r)
    {
        _isCircle = true;

        // NB: this is a circle... really!
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
     * Change whether this entity engages in _physics collisions or not
     * 
     * @param state
     *            either true or false. true indicates that the object will
     *            participate in _physics collisions. false
     *            indicates that it will not.
     */
    public void setCollisionEffect(boolean state)
    {
        _physBody.getFixtureList().get(0).setSensor(!state);
    }

    /**
     * Allow the user to adjust the default _physics settings (density,
     * elasticity, friction) for this entity
     * 
     * @param density
     *            New density of the entity
     * @param elasticity
     *            New elasticity of the entity
     * @param friction
     *            New friction of the entity
     */
    public void setPhysics(float density, float elasticity, float friction)
    {
        _physBody.getFixtureList().get(0).setDensity(density);
        _physBody.resetMassData();
        _physBody.getFixtureList().get(0).setRestitution(elasticity);
        _physBody.getFixtureList().get(0).setFriction(friction);
    }

    /**
     * Reset the _current PhysicsSprite as non-rotateable
     */
    public void disableRotation()
    {
        _physBody.setFixedRotation(true);
    }

    /**
     * Returns the X coordinate of this entity
     * 
     * @return x coordinate of bottom left corner
     */
    public float getXPosition()
    {
        return _physBody.getPosition().x - _width / 2;
    }

    /**
     * Returns the Y coordinate of this entity
     * 
     * @return y coordinate of bottom left corner
     */
    public float getYPosition()
    {
        return _physBody.getPosition().y - _height / 2; // return _sprite.getY();
    }

    /**
     * Returns the width of this entity
     * 
     * @return the entity's width
     */
    public float getWidth()
    {
        return _width;
    }

    /**
     * Return the height of this entity
     * 
     * @return the entity's height
     */
    public float getHeight()
    {
        return _height;
    }

    /**
     * Internal method for updating an entity's velocity, so that we can handle
     * its direction correctly
     * 
     * @param x
     *            The new x velocity
     * @param y
     *            The new y velocity
     */
    void updateVelocity(float x, float y)
    {
        if (_dJoint != null) {
            Level._currLevel._world.destroyJoint(_dJoint);
            _dJoint = null;
            Level._currLevel._world.destroyJoint(_wJoint);
            _wJoint = null;
        }
        _physBody.setLinearVelocity(x, y);
    }

    boolean _isTilt;

    /**
     * Indicate that the _sprite should move with the tilt of the phone
     */
    public void setMoveByTilting()
    {
        if (!_isTilt) {
            // make sure it is moveable, add it to the list of tilt entities
            if (_physBody.getType() != BodyType.DynamicBody)
                _physBody.setType(BodyType.DynamicBody);
            Level._currLevel._tilt._accelEntities.add(this);
            _isTilt = true;
            // turn off sensor behavior, so this collides with stuff...
            _physBody.getFixtureList().get(0).setSensor(false);
        }
    }

    boolean _visible = true;

    boolean _deleteQuietly;

    /**
     * Call this on an Entity to rotate it. Note that this works best on
     * boxes.
     * 
     * @param rotation
     *            amount to rotate the Entity (in degrees)
     */
    public void setRotation(float rotation)
    {
        // NB: the javadocs say "radians", but this appears to want rotation in degrees
        _physBody.setTransform(_physBody.getPosition(), rotation);
    }

    /**
     * Make an entity disappear
     * 
     * @param quiet
     *            True if the disappear sound should not be played
     */
    public void remove(boolean quiet)
    {
        // set it invisible immediately, so that future calls know to ignore
        // this PhysicsSprite
        _visible = false;
        _deleteQuietly = quiet;
        _physBody.setActive(false);

        // play a sound when we hit this thing?
        if (_disappearSound != null && !quiet)
            _disappearSound.play();

        // This is a bit slimy... we draw an obstacle here, so that we have a clean hook into the animation system, but
        // we disable its physics
        if (_disappearAnimation != null) {
            float x = getXPosition() + _disappearAnimateOffset.x;
            float y = getYPosition() + _disappearAnimateOffset.y;
            Obstacle o = Obstacle.makeAsBox(x, y, _disappearAnimateWidth, _disappearAnimateHeight, "");
            o._physBody.setActive(false);
            o.setDefaultAnimation(_disappearAnimation);
        }
    }

    /*
     * BASIC IMAGE, POSITION AND IDENTITY FUNCTIONALITY:
     * 
     * - PROVIDE AN IMAGE FOR THIS PHYSICSSPRITE
     * 
     * - ALLOW PROGRAMMER TO IDENTIFY PHYSICSSPRITES BY A STRING IDENTIFIER
     * 
     * - RETURN THE X AND Y POSITION OF A PHYSICSSPRITE
     */

    /*
     * BASIC PHYSICS FUNCTIONALITY:
     * 
     * - PROVIDE A PHYSICS BODY AND AN ANIMATED SPRITE
     * 
     * - MANAGE SHAPE (BOX/CIRCLE), ROTATION, MOTION
     * 
     * - SUPPORT COLLISIONS
     */

    /*
     * MOTION FUNCTIONALITY
     * 
     * - MOVE VIA TILT
     * 
     * - MOVE VIA FIXED VELOCITY
     * 
     * - MOVE VIA ROUTE
     */

    /**
     * Internal field to track if this hero is connected to an obstacle
     */
    DistanceJoint   _dJoint;

    WeldJoint       _wJoint;

    /**
     * Does this entity follow a route?
     */
    private boolean _isRoute = false;

    /**
     * Add velocity to this entity
     * 
     * @param x
     *            Velocity in X dimension
     * @param y
     *            Velocity in Y dimension
     * 
     * @param immuneToPhysics
     *            Should never be true for heroes! This means that gravity won't
     *            affect the entity, and it can pass through other entities
     *            without colliding.
     */
    public void addVelocity(float x, float y, boolean immuneToPhysics)
    {
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
        updateVelocity(v);
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
    public void setAbsoluteVelocity(float x, float y, boolean immuneToPhysics)
    {
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
        updateVelocity(v);
        // Disable sensor, or else this entity will go right through walls
        setCollisionEffect(true);
    }

    /**
     * Track if the object is a touch trigger
     */
    boolean _isTouchTrigger         = false;

    /**
     * Touch triggers can require certain Goodie counts in order to run
     */
    int[]   _touchTriggerActivation = new int[4];

    /**
     * An ID for each touch trigger object, in case it's useful
     */
    int     _touchTriggerID;

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
     */
    public void setTouchTrigger(int id, int activationGoodies1, int activationGoodies2, int activationGoodies3,
            int activationGoodies4)
    {
        _touchTriggerID = id;
        _isTouchTrigger = true;
        _touchTriggerActivation[0] = activationGoodies1;
        _touchTriggerActivation[1] = activationGoodies2;
        _touchTriggerActivation[2] = activationGoodies3;
        _touchTriggerActivation[3] = activationGoodies4;
    }

    /**
     * Returns the X velocity of of this entity
     * 
     * @return float Velocity in X dimension
     */
    public float getXVelocity()
    {
        return _physBody.getLinearVelocity().x;
    }

    /**
     * Returns the Y velocity of of this entity
     * 
     * @return float Velocity in Y dimension
     */
    public float getYVelocity()
    {
        return _physBody.getLinearVelocity().y;
    }

    /**
     * Make this entity move according to a route. The entity will loop back to the beginning of the route.
     * 
     * @param route
     *            The route to follow.
     * @param velocity
     *            speed at which to travel
     * @param loop
     *            Should this route loop continuously
     */
    Route   _myRoute;

    float   _routeVelocity;

    boolean _routeLoop;

    Vector2 _routeVec = new Vector2();

    public void setRoute(Route route, float velocity, boolean loop)
    {
        // This must be a KinematicBody!
        if (_physBody.getType() == BodyType.StaticBody)
            _physBody.setType(BodyType.KinematicBody);

        // save parameters
        _isRoute = true;
        _myRoute = route;
        _routeVelocity = velocity;
        _routeLoop = loop;

        // this is how we initialize a route driver:
        // first, move to the starting point
        _physBody.setTransform(_myRoute._xIndices[0] + _width/2, _myRoute._yIndices[0] + _height/2, 0);
        // second, indicate that we are working on goal #1, and set velocity
        // TODO: this needs to be in one place, instead of duplicated elsewhere
        // TODO: note that we are not getting the x,y coordinates quite right, since we're dealing with world center.
        _nextRouteGoal = 1;
        _routeVec.x = _myRoute._xIndices[_nextRouteGoal] - getXPosition();
        _routeVec.y = _myRoute._yIndices[_nextRouteGoal] - getYPosition();
        _routeVec.nor();
        _routeVec.scl(_routeVelocity);
        _physBody.setLinearVelocity(_routeVec);
        // and indicate that we aren't all done yet
        _routeDone = false;
    }

    boolean _routeDone;

    int     _nextRouteGoal;

    /**
     * Internal method for figuring out where we need to go next when driving a route
     * 
     * Note: we move the center to each goal
     */
    private void routeDriver()
    {
        // quit if we're done and we don't loop
        if (_routeDone)
            return;
        // if we haven't passed the goal, keep going. we tell if we've passed the goal by comparing the magnitudes of
        // the vectors from source to here and from goal to here
        float sx = _myRoute._xIndices[_nextRouteGoal - 1] - getXPosition();
        float sy = _myRoute._yIndices[_nextRouteGoal - 1] - getYPosition();
        float gx = _myRoute._xIndices[_nextRouteGoal] - getXPosition();
        float gy = _myRoute._yIndices[_nextRouteGoal] - getYPosition();
        boolean sameXSign = (gx >= 0 && sx >= 0) || (gx <= 0 && sx <= 0);
        boolean sameYSign = (gy >= 0 && sy >= 0) || (gy <= 0 && sy <= 0);
        if (((gx == gy) && (gx == 0)) || (sameXSign && sameYSign)) {
            _nextRouteGoal++;
            if (_nextRouteGoal == _myRoute._points) {
                // reset if it's a loop, else terminate Route
                if (_routeLoop) {
                    _physBody.setTransform(_myRoute._xIndices[0] + _width/2, _myRoute._yIndices[0] + _height/2, 0);
                    _nextRouteGoal = 1;
                    _routeVec.x = _myRoute._xIndices[_nextRouteGoal] - getXPosition();
                    _routeVec.y = _myRoute._yIndices[_nextRouteGoal] - getYPosition();
                    _routeVec.nor();
                    _routeVec.scl(_routeVelocity);
                    _physBody.setLinearVelocity(_routeVec);

                    return;
                }
                else {
                    _routeDone = true;
                    _physBody.setLinearVelocity(0, 0);
                    return;
                }
            }
            else {
                // advance to next point
                _routeVec.x = _myRoute._xIndices[_nextRouteGoal] - getXPosition();
                _routeVec.y = _myRoute._yIndices[_nextRouteGoal] - getYPosition();
                _routeVec.nor();
                _routeVec.scl(_routeVelocity);
                _physBody.setLinearVelocity(_routeVec);
                return;
            }
        }
        // NB: if we get here, we didn't need to change the velocity
    }

    /**
     * Make the entity continuously rotate. This is usually only useful for fixed objects.
     * 
     * @param duration
     *            Time it takes to complete one rotation
     */
    public void setRotationSpeed(float duration)
    {
        if (_physBody.getType() == BodyType.StaticBody)
            _physBody.setType(BodyType.KinematicBody);
        _physBody.setAngularVelocity(duration);
    }

    /*
     * TOUCH FUNCTIONALITY
     * 
     * - DRAGGING
     * 
     * - FLICKING
     * 
     * - POKE PATHS
     */

    /**
     * Track if the object is draggable
     */
    private boolean        _isDrag      = false;

    /**
     * A multiplicative factor to apply when flicking an entity
     */
    private float          _flickDampener;

    /**
     * Track if this entity can be flicked
     */
    private boolean        _isFlick     = false;

    /**
     * Track the x coordinate (screen, not _sprite) of where a flick began
     */
    private static float   _flickStartX;

    /**
     * Track the y coordinate (screen, not _sprite) of where a flick began
     */
    private static float   _flickStartY;

    /**
     * Track the entity being flicked
     */
    static PhysicsSprite   _flickEntity;

    /**
     * Internal vector for computing flicks, so that we don't need to use a Vector pool
     */
    private static Vector2 _flickVector = new Vector2();

    /**
     * An entity involved in poke path movement
     */
    static PhysicsSprite   _pokePathEntity;

    /**
     * Track if this entity can be moved via the creation of paths based on poking
     */
    private boolean        _isPokePath;

    /**
     * Track if we keep the pokeEntities or require re-touching them on every movement
     */
    private boolean        _keepPokeEntity;

    /**
     * If this is a poke path entity, how long should it take to travel its path?
     */
    private float          _pokePathVelocity;

    /**
     * An entity involved in poke velocity movement
     */
    static PhysicsSprite   _pokeVelocityEntity;

    /**
     * Track if this entity can be given velocity based on poking
     */
    private boolean        _isPokeVelocity;

    /**
     * If this is a poke velocity entity, what velocity should be used?
     */
    private float          _pokeVelocity;

    /**
     * In "chase mode", a poke entity will change its path based on dragging, and will stop when a touch ends.
     */
    private boolean        _pokeChaseMode;

    /**
     * Call this on an entity to make it draggable.
     * 
     * Be careful when dragging things. If they are small, they will be hard to touch.
     */
    public void setCanDrag(boolean immuneToPhysics)
    {
        if (immuneToPhysics)
            _physBody.setType(BodyType.KinematicBody);
        else
            _physBody.setType(BodyType.DynamicBody);
        _isDrag = true;
    }

    /**
     * a sound to play when the obstacle is touched
     */
    protected Sound _touchSound;

    /**
     * Indicate that when the player touches this obstacle, we should make a
     * sound
     * 
     * @param sound
     *            The name of the sound file to play
     */
    public void setTouchSound(String sound)
    {
        _touchSound = Media.getSound(sound);
    }

    /**
     * Does the hero throw a projectile when we touch it?
     */
    private boolean _isTouchThrow = false;

    private Hero    _throwHero;

    void handleTouchDown(float x, float y)
    {
        if (_touchSound != null)
            _touchSound.play();

        if (_isFlick) {
            registerInitialFlick();
            return;
        }

        // throw a projectile?
        if (_isTouchThrow) {
            Projectile.throwFixed(_throwHero._physBody.getPosition().x, _throwHero._physBody.getPosition().y,
                    _throwHero);
            return;
        }

        if (_isPokeVelocity) {
            _pokeVelocityEntity = this;
            // TODO: ALE._self.getEngine().vibrate(5);
            return;
        }

        if (_isPoke) {
            // TODO:
            // if (ALE._game._config.getVibration())
            // ALE._self.getEngine().vibrate(100);

            long time = System.nanoTime();

            if (this == _currentPokeSprite) {

                // double touch
                if ((time - _lastPokeTime) < _pokeDeleteThresh) {
                    // hide sprite, disable physics
                    _physBody.setActive(false);
                    _visible = false;
                }
                // repeat single-touch
                else {
                    _lastPokeTime = time;
                }
            }
            else {
                _currentPokeSprite = this;
                _lastPokeTime = time;
            }
            return;
        }
        // if this is a touch trigger, call the touchtrigger code
        if (_isTouchTrigger) {
            boolean match = true;
            for (int i = 0; i < 4; ++i)
                match &= _touchTriggerActivation[i] <= Level._currLevel._score._goodiesCollected[i];
            if (match) {
                remove(false);
                LOL._game.onTouchTrigger(_touchTriggerID, LOL._game._currLevel, this);
                return;
            }
        }
        else if (_isPokePath) {
            _pokePathEntity = this;
            // TODO: ALE._self.getEngine().vibrate(5);
            return;
        }

    }

    boolean handleTouchDrag(float x, float y)
    {
        if (_isDrag) {
            _physBody.setTransform(x, y, _physBody.getAngle());
            return false;
        }
        return true;
    }

    /*
     * POKE SUPPORT
     */

    /**
     * When a _sprite is poked, we record it here so that we know who to move on
     * the next screen touch
     */
    protected static PhysicsSprite _currentPokeSprite;

    /**
     * Track if the object is pokeable
     */
    private boolean                _isPoke           = false;

    /**
     * When a _sprite is poked, remember the time, because rapid double-clicks
     * cause deletion
     */
    private static long            _lastPokeTime;

    /**
     * Tunable constant for how much time between pokes constitutes a
     * "double click"... this is in nanoseconds, so it's half a second
     */
    private final static long      _pokeDeleteThresh = 500000000;

    /**
     * Call this on an Obstacle to make it pokeable
     * 
     * Poke the Obstacle, then poke the screen, and the Obstacle will move to
     * the location that was pressed. Poke the
     * Obstacle twice in rapid succession to delete the Obstacle.
     */
    public void setPokeable()
    {
        _isPoke = true;
    }

    /**
     * Indicate that this entity can be flicked on the screen
     * 
     * @param _dampFactor
     *            A value that is multiplied by the vector for the flick, to affect speed
     */
    public void setFlickable(float dampFactor)
    {
        if (_physBody.getType() != BodyType.DynamicBody)
            _physBody.setType(BodyType.DynamicBody);
        _isFlick = true;
        _flickDampener = dampFactor;
    }

    /**
     * record the occasion of a down press on a flickable entity
     * 
     * @param e
     *            The event, so that we can filter for down presses
     * @param x
     *            The x coordinate of where on the _sprite the touch occurred
     * @param y
     *            The y coordinate of where on the _sprite the touch occurred
     */
    private void registerInitialFlick()
    {
        // TODO:
        // ALE._self.getEngine().vibrate(100);

        // don't forget to translate the touch into a screen coordinate
        _flickStartX = _physBody.getPosition().x;// + _sprite.getX();
        _flickStartY = _physBody.getPosition().y;
        _flickEntity = this;
    }

    static void flickDone(float x, float y)
    {
        if (_flickEntity != null) {
            // if the entity was hovering, stop hovering
            _flickEntity._hover = false;
            // compute velocity for the flick
            _flickVector.x = (x - _flickStartX) * _flickEntity._flickDampener;
            _flickVector.y = (y - _flickStartY) * _flickEntity._flickDampener;
            _flickEntity.updateVelocity(_flickVector);
            // clear the flick, so we don't have strange "memory"
            // issues...
            _flickEntity = null;
        }
    }

    /**
     * Indicate that we should not "forget" a poke-path entity after we've done one movement with it
     */
    public void setKeepPokeEntity()
    {
        _keepPokeEntity = true;
    }

    void finishPoke(float x, float y)
    {
        if (_currentPokeSprite != null) {
            // TODO:
            // if (Configuration.isVibrationOn())
            // ALE._self.getEngine().vibrate(100);

            // move the object
            _currentPokeSprite._physBody.setTransform(x, y, _currentPokeSprite._physBody.getAngle());

            // forget the object
            _currentPokeSprite = null;
        }
    }

    // TODO: why does this have to be static? Can't it be a method of the _pokePathEntity?
    static boolean finishPokePath(float x, float y, boolean isDown, boolean isMove, boolean isUp)
    {
        if (_pokePathEntity != null) {
            if (isDown || (isMove && _pokePathEntity._pokeChaseMode)) {
                Route r = new Route(2).to(_pokePathEntity.getXPosition() + _pokePathEntity._width / 2,
                        _pokePathEntity.getYPosition() + _pokePathEntity._height / 2).to(x, y);
                _pokePathEntity.setAbsoluteVelocity(0, 0, false);
                _pokePathEntity.setRoute(r, _pokePathEntity._pokePathVelocity, false);

                // clear the pokePathEntity, so a future touch starts the process over
                if (!_pokePathEntity._keepPokeEntity)
                    _pokePathEntity = null;
                return false;
            }
            else if (isUp) {
                // TODO: this is dead code... we don't actually want to do anything on an up press, just let it run...
                // otherwise, it's really a velocity entity. However, we could make it an optional behavior, so let's
                // leave it for now
                if (isDown && _pokePathEntity._pokeChaseMode) {
                    // stop driving a route
                    _pokePathEntity._routeDone = true;
                    _pokePathEntity.setAbsoluteVelocity(0, 0, false);
                    // _pokePathEntity._sprite.clearEntityModifiers();
                    // clear the pokePathEntity, so a future touch starts the process over
                    if (!_pokePathEntity._keepPokeEntity)
                        _pokePathEntity = null;
                    return false;
                }
            }
        }
        return true;
    }

    static boolean finishPokeVelocity(float xx, float yy, boolean isDown, boolean isMove, boolean isUp)
    {
        if (_pokeVelocityEntity != null) {
            if (isDown || (_pokeVelocityEntity._pokeChaseMode && isMove)) {
                // Figure out a vector for the movement, so we have direction
                float x = xx - _pokeVelocityEntity._width / 2 - _pokeVelocityEntity.getXPosition();
                float y = yy - _pokeVelocityEntity._width / 2 - _pokeVelocityEntity.getYPosition();

                // make it a unit vector, and multiply by the requested velocity, to get the right magnitude
                float hypotenuse = (float) Math.sqrt(x * x + y * y);
                x = (x / hypotenuse) * _pokeVelocityEntity._pokeVelocity;
                y = (y / hypotenuse) * _pokeVelocityEntity._pokeVelocity;

                // apply velocity to the entity
                _pokeVelocityEntity.updateVelocity(x, y);

                // clear the pokeVelocityEntity, so a future touch starts the process over
                if (!_pokeVelocityEntity._keepPokeEntity)
                    _pokeVelocityEntity = null;
            }
            // TODO: should this behavior on up be a new parameter, like 'stoponup'?
            else if (isUp) {
                if (_pokeVelocityEntity._pokeChaseMode) {
                    // stop the entity
                    _pokeVelocityEntity.updateVelocity(0, 0);

                    // clear the pokePathEntity, so a future touch starts the process over
                    if (!_pokeVelocityEntity._keepPokeEntity)
                        _pokeVelocityEntity = null;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Indicate that the poke path mechanism should cause the entity to move at a constant velocity, rather than a
     * velocity scaled to the distance of travel
     * 
     * @param velocity
     *            The constant velocity for poke movement
     */
    public void setPokePathFixedVelocity(float velocity)
    {
        if (_physBody.getType() == BodyType.StaticBody)
            _physBody.setType(BodyType.KinematicBody);
        _isPokePath = true;
        // TODO: simplify this... we only need one set of poke fields...
        _pokePathVelocity = velocity;
    }

    /**
     * Call this on an Entity to indicate that it can move on the screen via poking
     * 
     * Poke the Entity, then poke the screen, and the Entity will move toward the location that was pressed, with a
     * fixed velocity
     * 
     * @param magnitude
     *            The magnitude of the velocity when the entity starts moving
     */
    public void setPokeVelocity(float magnitude)
    {
        if (_physBody.getType() == BodyType.StaticBody)
            _physBody.setType(BodyType.KinematicBody);
        _isPokeVelocity = true;
        _pokeVelocity = magnitude;
    }

    /**
     * Indicate that a poke-to-move entity will keep changing its path based on finger movements, and will stop when the
     * user stops touching the screen.
     */
    public void setPokeChaseMode()
    {
        _pokeChaseMode = true;
    }

    /*
     * ANIMATION SUPPORT
     */
    /**
     * Animation support: the cells of the default animation
     */
    Animation         _defaultAnimation;

    /**
     * The currently running animation
     */
    private Animation _currentAnimation;

    private int       _currAnimationFrame;

    private float     _currAnimationTime;

    void setCurrentAnimation(Animation a)
    {
        _currentAnimation = a;
        _currAnimationFrame = 0;
        _currAnimationTime = 0;
    }

    /**
     * Animation support: the cells of the disappearance animation
     */
    Animation       _disappearAnimation;

    /**
     * Animation support: the offset for placing the disappearance animation relative to the disappearing _sprite
     */
    final Vector2   _disappearAnimateOffset = new Vector2();

    /**
     * Animation support: the width of the disappearance animation
     */
    float           _disappearAnimateWidth;

    /**
     * Animation support: the height of the disappearance animation
     */
    float           _disappearAnimateHeight;

    /**
     * Does the entity's image flip when the hero moves backwards?
     */
    private boolean _reverseFace            = false;

    /**
     * Save the animation sequence and start it right away
     * 
     * @param cells
     *            which cells of the _sprite to show
     * @param durations
     *            duration for each cell
     */
    public void setDefaultAnimation(Animation a)
    {
        _defaultAnimation = a;
        // we'll assume we're using the default animation as our first animation...
        setCurrentAnimation(_defaultAnimation);
    }

    /**
     * Save an animation sequence for showing when we get rid of a _sprite
     * 
     * @param cells
     *            which cells of the _sprite to show
     * @param durations
     *            duration for each cell
     * @param imageName
     *            Name of the image to display for the disappear animation
     * @param offsetX
     *            X offset of top left corner relative to hero top left
     * @param offsetY
     *            Y offset of top left corner relative to hero top left
     * @param width
     *            Width of the animation image
     * @param height
     *            Height of the animation image
     */
    public void setDisappearAnimation(Animation a, float offsetX, float offsetY, float width, float height)
    {
        _disappearAnimation = a;
        _disappearAnimateOffset.x = offsetX;
        _disappearAnimateOffset.y = offsetY;
        _disappearAnimateWidth = width;
        _disappearAnimateHeight = height;
    }

    /**
     * Indicate that this entity's image should be reversed when it is moving in the negative x direction. For tilt,
     * this only
     * applies to the last hero created. For velocity, it applies to everyone.
     */
    public void setCanFaceBackwards()
    {
        _reverseFace = true;
    }

    /**
     * Internal method for updating an entity's velocity, so that we can handle its direction correctly
     * 
     * @param v
     *            a vector representing the new velocity
     */
    void updateVelocity(Vector2 v)
    {
        updateVelocity(v.x, v.y);
    }

    /*
     * TIMING SUPPORT
     * 
     * - MAKE ENTITIES APPEAR AFTER A DELAY
     * 
     * - MAKE ENTITIES DISAPPEAR AFTER A DELAY
     * 
     * - MAKE ENTITIES SHRINK/GROW OVER TIME
     */

    /**
     * Indicate that something should not appear quite yet...
     * 
     * @param delay
     *            How long to wait before displaying the thing
     */
    public void setAppearDelay(float delay)
    {
        _visible = false;
        _physBody.setActive(false);
        Timer.schedule(new Task()
        {
            @Override
            public void run()
            {
                _visible = true;
                _physBody.setActive(true);
            }
        }, delay);
    }

    /**
     * Indicate that something should disappear after a little while
     * 
     * @param delay
     *            How long to wait before hiding the thing
     * 
     * @param quiet
     *            true if the item should disappear quietly, false if it should play its disappear sound
     */
    public void setDisappearDelay(float delay, final boolean quiet)
    {
        Timer.schedule(new Task()
        {
            @Override
            public void run()
            {
                remove(quiet);
            }
        }, delay);
    }

    public void setImage(String imgName)
    {
        // minor hack so that we can have invalid png files for invisible images
        TextureRegion[] tra = Media.getImage(imgName);
        if (tra != null) {
            _tr = new TextureRegion(tra[0]);
        }
        else {
            _tr = null;
        }
    }
    
    public void resize(float x, float y, float width, float height)
    {
        _width = width;
        _height = height;
        Body _oldBody = _physBody;
        Fixture _oldFix = _oldBody.getFixtureList().get(0);
        if (_isCircle) {
            setCirclePhysics(_oldFix.getDensity(), _oldFix.getRestitution(), _oldFix.getFriction(), _oldBody.getType(),
                    _oldBody.isBullet(), x, y, (width > height) ? width / 2 : height / 2);
        }
        else {
            setBoxPhysics(_oldFix.getDensity(), _oldFix.getRestitution(), _oldFix.getFriction(), _oldBody.getType(),
                    _oldBody.isBullet(), x, y);
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
     * @param shrinkX
     *            The number of pixels by which the X dimension should shrink each second
     * @param shrinkY
     *            The number of pixels by which the Y dimension should shrink each second
     */
    public void setShrinkOverTime(final float shrinkX, final float shrinkY, final boolean keepCentered)
    {
        final Task t = new Task()
        {
            @Override
            public void run()
            {
                if (_visible) {
                    float x, y;
                    if (keepCentered) {
                        x = getXPosition() + shrinkX / 20 / 2;
                        y = getYPosition() + shrinkY / 20 / 2;
                    }
                    else {
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
                            setCirclePhysics(_oldFix.getDensity(), _oldFix.getRestitution(), _oldFix.getFriction(),
                                    _oldBody.getType(), _oldBody.isBullet(), x, y, (w > h) ? w / 2 : h / 2);
                        }
                        else {
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
                        Timer.schedule(this, .05f);
                    }
                    else {
                        remove(false);
                    }
                }
            }
        };
        Timer.schedule(t, .05f);
    }

    /*
     * HOVER and GRAVITY DEFIANCE SUPPORT
     */

    /**
     * If this entity hovers, this is the x coordinate on screen where it should appear
     */
    int             _hoverX;

    /**
     * If this entity hovers, this is the y coordinate on screen where it should appear
     */
    int             _hoverY;

    /**
     * A flag to indicate if this entity hovers
     */
    boolean         _hover       = false;

    /**
     * A vector for computing _hover placement
     */
    private Vector3 _hoverVector = new Vector3();

    /**
     * Indicate that this entity should _hover at a specific location on the screen, rather than being placed at some
     * point on the level itself. Note that the coordinates to this command are the center position of the hovering
     * entity.
     * 
     * @param x
     *            the X coordinate where the entity should appear
     * @param y
     *            the Y coordinate where the entity should appear
     */
    public void setHover(int x, int y)
    {
        // make the entity kinematic, so gravity doesn't affect it but it still participates in collisions
        // makeKinematic();
        // save the parameters
        //
        // TODO: can we just use the _hoverVector without the x, y, and flag?
        _hoverX = x;
        _hoverY = y;
        _hover = true;

        Level._currLevel._repeatEvents.add(new Action()
        {
            @Override
            public void go()
            {
                if (!_hover)
                    return;
                _hoverVector.x = _hoverX;
                _hoverVector.y = _hoverY;
                _hoverVector.z = 0;
                Level._currLevel._gameCam.unproject(_hoverVector);
                _physBody.setTransform(_hoverVector.x, _hoverVector.y, _physBody.getAngle());
            }
        });
    }

    /**
     * Indicate that this entity should have a force applied to it at all times, e.g., to defy gravity.
     * 
     * @param x
     *            The magnitude of the force in the X direction
     * @param y
     *            The magnitude of the force in the Y direction
     */
    public void setGravityDefy()
    {
        _physBody.setGravityScale(0);
    }

    /*
     * PHYSICALLY CONNECTING OBSTACLES AND HEROES
     */
    boolean isStickyTop;

    boolean isStickyBottom;

    boolean isStickyLeft;

    boolean isStickyRight;

    /**
     * Make this obstacle sticky, so that a hero will stick to it
     */
    public void setSticky(boolean top, boolean right, boolean bottom, boolean left)
    {
        isStickyTop = top;
        isStickyRight = right;
        isStickyBottom = bottom;
        isStickyLeft = left;
    }

    /*
     * AUDIO SUPPORT
     */

    /**
     * Sound to play when this disappears
     */
    protected Sound _disappearSound = null;

    /**
     * Set the sound to play when this entity disappears
     * 
     * @param soundName
     *            Name of the sound file
     */
    public void setDisappearSound(String soundName)
    {
        _disappearSound = Media.getSound(soundName);
    }

    /**
     * Indicate that touching this hero should make it throw a projectile
     */
    public void setTouchToThrow(Hero h)
    {
        _isTouchThrow = true;
        _throwHero = h;
    }

    /*
     * ONE-SIDED ENTITIES AND PASS-THROUGH ENTITIES
     */

    /**
     * Track which sides are one-sided. 0 is bottom, 1 is right, 2 is top, 3 is left
     */
    int _isOneSided    = -1;

    /**
     * Entities with a matching nonzero Id don't collide with each other
     */
    int _passThroughId = 0;

    /**
     * Indicate that this obstacle only registers collisions on one side.
     * 
     * @param side
     *            The side that registers collisions. 0 is top, 1 is right, 2 is bottom, 3 is left, -1 means "none"
     */
    public void setOneSided(int side)
    {
        _isOneSided = side;
    }

    /**
     * Indicate that this entity should not have collisions with any other entity that has the same ID
     * 
     * @param id
     *            The number for this class of non-interacting entities
     */
    public void setPassThrough(int id)
    {
        _passThroughId = id;
    }

    /*
     * OTHER FUNCTIONALITY
     */

    /**
     * Track if this entity can be re-positioned via the developer live-edit feature
     */
    boolean isLiveEdit;

    /**
     * By default, non-hero entities are not subject to gravity until they are given a path, velocity, or other form of
     * motion. This lets an entity simply fall.
     */
    public void setCanFall()
    {
        _physBody.setType(BodyType.DynamicBody);
    }

    /*
     * SUPPORT FOR CHASING THE HERO
     */

    /**
     * If this enemy is supposed to chase the hero, this determines the velocity with which it chases
     */
    private float         _chaseMultiplier = 0;

    /**
     * An internal vector for supporting chase enemies
     */
    private final Vector2 _chaseVector     = new Vector2();

    private PhysicsSprite _chaseTarget;

    /**
     * Specify that this enemy is supposed to chase the hero
     * 
     * @param speed
     *            The speed with which the enemy chases the hero
     */
    public void setChaseSpeed(float speed, PhysicsSprite ps)
    {
        _physBody.setType(BodyType.DynamicBody);
        _chaseMultiplier = speed;
        _chaseTarget = ps;
        Level._currLevel._repeatEvents.add(new Action()
        {
            @Override
            public void go()
            {
                // don't chase something that isn't visible
                if (!_chaseTarget._visible)
                    return;
                // don't run if this sprite isn't visible
                if (!_visible)
                    return;

                // chase the _chaseTarget
                // compute vector between hero and enemy
                _chaseVector.x = _chaseTarget._physBody.getPosition().x - _physBody.getPosition().x;
                _chaseVector.y = _chaseTarget._physBody.getPosition().y - _physBody.getPosition().y;

                // normalize it and then multiply by speed
                _chaseVector.nor();
                _chaseVector.x *= (_chaseMultiplier);
                // TODO: disable y position chasing for sidescrolling games?
                _chaseVector.y *= (_chaseMultiplier);

                // set Enemy velocity accordingly
                updateVelocity(_chaseVector);
            }
        });
    }

    /**
     * A flag to indicate that the hero should rotate to always appear to be facing in the direction it is traveling
     */
    private boolean       _rotateByDirection;

    /**
     * A temporary vector for the _rotateByDirection computation
     */
    private final Vector2 _rotationVector = new Vector2();

    /**
     * Indicate that this hero's rotation should be determined by the direction in which it is traveling
     */
    public void setRotationByDirection()
    {
        _rotateByDirection = true;

        Level._currLevel._repeatEvents.add(new Action()
        {
            @Override
            public void go()
            {
                // handle rotating the hero based on the direction it faces
                if (_rotateByDirection && _visible) {
                    _rotationVector.x = _physBody.getLinearVelocity().x;
                    _rotationVector.y = _physBody.getLinearVelocity().y;
                    double angle = Math.atan2(_rotationVector.y, _rotationVector.x) + Math.atan2(-1, 0);
                    _physBody.setTransform(_physBody.getPosition(), (float) angle);
                }
            }
        });
    }

    /*
     * ADVANCED CAMERA SUPPORT
     */

    /**
     * When the camera follows the hero without centering the hero, this gives us the difference between the hero and
     * camera
     */
    Vector2 _cameraOffset = new Vector2(0, 0);

    /**
     * Make the camera follow the hero, but without centering the hero on the screen
     * 
     * @param x
     *            Amount of x distance between hero and center
     * @param y
     *            Amount of y distance between hero and center
     */
    // TODO: make this part of the Level.setCameraChase code?
    public void setCameraOffset(float x, float y)
    {
        _cameraOffset.x = x;
        _cameraOffset.y = y;
    }

    private boolean _flipped;

    @Override
    public void render(SpriteBatch _spriteRender, float delta)
    {
        if (_visible) {
            // possibly run a route update
            if (_isRoute && _visible)
                routeDriver();

            // choose the default TextureRegion to show... this is how we animate
            TextureRegion tr = _tr;
            // If we've got an in-flight animation, switch to it
            if (_currentAnimation != null) {
                _currAnimationTime += delta;
                long millis = (long) (1000 * _currAnimationTime);
                // are we still in this frame?
                //
                // TODO: we can simplify this code
                if (millis <= _currentAnimation._durations[_currAnimationFrame]) {
                    tr = _currentAnimation._cells[_currentAnimation._frames[_currAnimationFrame]];
                }
                // are we on the last frame, with no loop? If so, stay where we are...
                else if (_currAnimationFrame == _currentAnimation._nextCell - 1 && !_currentAnimation._loop) {
                    tr = _currentAnimation._cells[_currentAnimation._frames[_currAnimationFrame]];
                }
                // else advance, reset, go
                else {
                    _currAnimationFrame = (_currAnimationFrame + 1) % _currentAnimation._nextCell;
                    _currAnimationTime = 0;
                    tr = _currentAnimation._cells[_currentAnimation._frames[_currAnimationFrame]];
                }
            }

            // now draw this sprite
            Vector2 pos = _physBody.getPosition();
            if (_reverseFace && _physBody.getLinearVelocity().x < 0) {
                if (!_flipped) {
                    tr.flip(true, false);
                    _flipped = true;
                }
            }
            else if (_reverseFace && _physBody.getLinearVelocity().x > 0) {
                if (_flipped) {
                    tr.flip(true, false);
                    _flipped = false;
                }
            }
            if (tr != null)
                _spriteRender.draw(tr, pos.x - _width / 2, pos.y - _height / 2, _width / 2, _height / 2, _width,
                        _height, 1, 1, MathUtils.radiansToDegrees * _physBody.getAngle());
        }
    }
}