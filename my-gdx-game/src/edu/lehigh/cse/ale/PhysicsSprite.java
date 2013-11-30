package edu.lehigh.cse.ale;

// STATUS: in progress

// TODO: enable arbitrary polygon creation?

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

public abstract class PhysicsSprite
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

    PhysicsSprite(TextureRegion tr, SpriteId id, float width, float height)
    {
        _psType = id;
        _tr = tr;
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
     * Remember if this _physics body can rotate
     */
    protected boolean _canRotate = true;

    /**
     * Each descendant defines this to address any custom logic that we need to
     * deal with on a collision
     * 
     * @param other
     *            The other entity involved in the collision
     */
    abstract void onCollide(PhysicsSprite other);

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
        boxBodyDef.position.x = x+_width/2;
        boxBodyDef.position.y = y+_height/2;
        _physBody = GameLevel._currLevel._world.createBody(boxBodyDef);

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
        boxBodyDef.position.x = x + _width/2;
        boxBodyDef.position.y = y + _height/2;
        _physBody = GameLevel._currLevel._world.createBody(boxBodyDef);

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
        
        /*
         * // get information from previous body
         * boolean wasSensor = _physBody.getFixtureList().get(0).isSensor();
         * boolean _isBullet = _physBody.isBullet();
         * BodyType _t = _physBody.getType();
         * 
         * // delete old body, make a new body
         * deletePhysicsBody();
         * // TODO
         * //if (_isCircle)
         * // setCirclePhysics(density, elasticity, friction, _t, _isBullet);
         * //else
         * // setBoxPhysics(density, elasticity, friction, _t, _isBullet);
         * // patch up if it was a sensor
         * if (wasSensor)
         * setCollisionEffect(false);
         */
    }

    /**
     * Reset the _current PhysicsSprite as non-rotateable
     */
    public void disableRotation()
    {
        // TODO: is this all we need?
        _physBody.setFixedRotation(true);
        // set a flag, copy key parameters
        _canRotate = false;
        boolean wasSensor = _physBody.getFixtureList().get(0).isSensor();
        float _density = _physBody.getFixtureList().get(0).getDensity();
        float _elasticity = _physBody.getFixtureList().get(0).getRestitution();
        float _friction = _physBody.getFixtureList().get(0).getFriction();
        boolean _isBullet = _physBody.isBullet();
        BodyType _t = _physBody.getType();
        // remove old body, create new body of appropriate type
        deletePhysicsBody();
        // TODO:
        // if (_isCircle)
        // setCirclePhysics(_density, _elasticity, _friction, _t, _isBullet);
        // else
        // setBoxPhysics(_density, _elasticity, _friction, _t, _isBullet);
        // patch up if it was a sensor
        if (wasSensor)
            setCollisionEffect(false);
    }

    /**
     * Delete the current physics attached to this Sprite.
     * 
     * We use this when we're changing the body on the fly, as it simplifies the
     * task of removing the old body.
     */
    void deletePhysicsBody()
    {
        _physBody.setActive(false);
        // NB: should use world.destroyBody(box);
        // Level._physics.getPhysicsConnectorManager().remove(_pc);
        // Level._physics.destroyBody(_physBody);
    }

    /**
     * Returns the X coordinate of this entity
     * 
     * @return x coordinate of top left corner
     */
    public float getXPosition()
    {
        return _physBody.getPosition().y;
    }

    /**
     * Returns the Y coordinate of this entity
     * 
     * @return y coordinate of top left corner
     */
    public float getYPosition()
    {
        return _physBody.getPosition().x; // return _sprite.getY();
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
        _physBody.setLinearVelocity(x, y);

        // TODO: support reverse face
        // manage changing direction of entity
        // if (_reverseFace && x != 0)
        // _sprite.setFlippedHorizontal(x < 0);
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
            Tilt._accelEntities.add(this);
            _isTilt = true;
            // turn off sensor behavior, so this collides with stuff...
            _physBody.getFixtureList().get(0).setSensor(false);
        }
    }

    boolean _visible = true;

    boolean _deleteQuietly;

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
        // TODO
        /*
         * _sprite.setVisible(false);
         * if (_disappearAnimateCells != null) {
         * float x = _sprite.getX() + _disappearAnimateOffset.x;
         * float y = _sprite.getY() + _disappearAnimateOffset.y;
         * TiledTextureRegion ttr = Media.getImage(_disappearAnimateImageName);
         * AnimatedSprite as = new AnimatedSprite(x, y, _disappearAnimateWidth,
         * _disappearAnimateHeight, ttr,
         * ALE._self.getVertexBufferObjectManager());
         * Level._current.attachChild(as);
         * as.animate(_disappearAnimateDurations, _disappearAnimateCells,
         * false);
         * }
         * // play a sound when we hit this thing?
         * if (_disappearSound != null && !quiet)
         * _disappearSound.play();
         * // disable the _physics body
         * _physBody.setActive(false);
         */
    }

    
    
    /*
     * INTERNAL CLASSES
     */

    /**
     * A wrapper around the AnimatedSprite type.
     * 
     * The AnimatedSprite type is not very orthogonal, in that it expects to be used in an is-a relationship instead of
     * a has-a. Thus certain AndEngine functionality assumes that methods will be overloaded.
     * 
     * To compensate for that design, we extend AnimatedSprite, and then we place callbacks into the Override methods,
     * so that they will direct to our custom codes instead.
     */
    /*
     * BASIC IMAGE, POSITION AND IDENTITY FUNCTIONALITY:
     * 
     * - PROVIDE AN IMAGE FOR THIS PHYSICSSPRITE
     * 
     * - ALLOW PROGRAMMER TO IDENTIFY PHYSICSSPRITES BY A STRING IDENTIFIER
     * 
     * - RETURN THE X AND Y POSITION OF A PHYSICSSPRITE
     */

    /**
     * Create the image for this entity, and set its type
     * 
     * Note that we don't do anything with the physics, since physics needs to be customized
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param ttr
     *            Image to use
     * @param spriteId
     *            Type of this entity (Hero, Enemy, etc)
     */
    /*
    PhysicsSprite(float x, float y, float width, float height, TiledTextureRegion ttr, SpriteId spriteId)
    {
        _sprite = new SpriteType(x, y, width, height, ttr, this);
        _psType = spriteId;
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
     * Does this entity follow a route?
     */
    boolean   _isRoute     = false;

    /**
     * Rather than pooling Vector2 objects, we keep one around for use when dealing with routes
     */
    private Vector2   _routeVector = new Vector2();

    /**
     * When we make a sprite moveable, we need to be sure it has a DynamicBody...
     *//*
    void makeMoveable()
    {
        // if the body is not dynamic, copy all parameters, re-create the body as Dynamic
        if (_physBody.getType() != BodyType.DynamicBody) {
            float _density = _physBody.getFixtureList().get(0).getDensity();
            float _elasticity = _physBody.getFixtureList().get(0).getRestitution();
            float _friction = _physBody.getFixtureList().get(0).getFriction();
            boolean _isBullet = _physBody.isBullet();
            boolean isSensor = _physBody.getFixtureList().get(0).isSensor();
            deletePhysicsBody();
            if (_isCircle)
                setCirclePhysics(_density, _elasticity, _friction, BodyType.DynamicBody, _isBullet);
            else
                setBoxPhysics(_density, _elasticity, _friction, BodyType.DynamicBody, _isBullet);
            if (isSensor)
                setCollisionEffect(false);
        }
    }

    /**
     * Sometimes it is useful to make a sprite kinematic instead of dynamic
     *//*
    void makeKinematic()
    {
        // if the body is not kinematic, copy all parameters, re-create the body as kinematic
        if (_physBody.getType() != BodyType.KinematicBody) {
            float _density = _physBody.getFixtureList().get(0).getDensity();
            float _elasticity = _physBody.getFixtureList().get(0).getRestitution();
            float _friction = _physBody.getFixtureList().get(0).getFriction();
            boolean _isBullet = _physBody.isBullet();
            boolean isSensor = _physBody.getFixtureList().get(0).isSensor();
            deletePhysicsBody();
            if (_isCircle)
                setCirclePhysics(_density, _elasticity, _friction, BodyType.KinematicBody, _isBullet);
            else
                setBoxPhysics(_density, _elasticity, _friction, BodyType.KinematicBody, _isBullet);
            if (isSensor)
                setCollisionEffect(false);
        }
    }


    /**
     * Add velocity to this entity
     * 
     * @param x
     *            Velocity in X dimension
     * @param y
     *            Velocity in Y dimension
     *//*
    public void addVelocity(float x, float y)
    {
        // ensure this is a moveable entity
        makeMoveable();

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
     *//*
    public void setAbsoluteVelocity(float x, float y)
    {
        // ensure this is a moveable entity
        makeMoveable();

        // change its velocity
        Vector2 v = _physBody.getLinearVelocity();
        v.y = y;
        v.x = x;
        updateVelocity(v);
        // Disable sensor, or else this entity will go right through walls
        setCollisionEffect(true);
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
    Route _myRoute;
    float _routeVelocity;
    boolean _routeLoop;
    public void setRoute(Route route, float velocity, boolean loop)
    {
        // This must be a KinematicBody!
        _physBody.setType(BodyType.KinematicBody);

        // save parameters
        _isRoute = true;
        _myRoute = route;
        _routeVelocity = velocity;
        _routeLoop = loop;
        
        // this is how we initialize a route driver:
        // first, move to the starting point
        _physBody.setTransform(_myRoute._xIndices[0], _myRoute._yIndices[0], 0);
        // second, indicate that we are working on goal #1, and set velocity
        _nextRouteGoal = 1;
        _physBody.setLinearVelocity(_myRoute._xIndices[_nextRouteGoal] - _physBody.getPosition().x, _myRoute._yIndices[_nextRouteGoal] - _physBody.getPosition().y);
        // third, make sure we get updated
        GameLevel._currLevel._routes.add(this);
        // and indicate that we aren't all done yet
        _routeDone = false;
    }

    boolean _routeDone;
    int _nextRouteGoal;
    
    /**
     * Internal method for figuring out where we need to go next when driving a route
     * 
     * Note: we move the center to each goal
     */
    void routeDriver()
    {
        // quit if we're done and we don't loop
        if (_routeDone)
            return;
        // if we haven't passed the goal, keep going.  we tell if we've passed the goal by comparing the magnitudes of the vectors from source to here and from goal to here
        float sx = _myRoute._xIndices[_nextRouteGoal - 1] - _physBody.getPosition().x;
        float sy = _myRoute._yIndices[_nextRouteGoal - 1] - _physBody.getPosition().y;
        float gx= _myRoute._xIndices[_nextRouteGoal] - _physBody.getPosition().x;
        float gy = _myRoute._yIndices[_nextRouteGoal] - _physBody.getPosition().y;
        boolean sameXSign = (gx >= 0 && sx >= 0) || (gx<=0&&sx<=0);
        boolean sameYSign = (gy >= 0 && sy >= 0) || (gy<=0&&sy<=0);
        if (((gx == gy)&&(gx == 0)) || (sameXSign && sameYSign)) {
            _nextRouteGoal++;
            if (_nextRouteGoal == _myRoute._points) {
                // reset if it's a loop, else terminate Route
                if (_routeLoop) {
                    _physBody.setTransform(_myRoute._xIndices[0], _myRoute._yIndices[0], 0);
                    _nextRouteGoal = 1;
                    _physBody.setLinearVelocity(_myRoute._xIndices[_nextRouteGoal] - _physBody.getPosition().x, _myRoute._yIndices[_nextRouteGoal] - _physBody.getPosition().y);
                    return;
                }
                else {
                    _routeDone = true;
                    return;
                }
            }
            else {
                // advance to next point
                _physBody.setLinearVelocity(_myRoute._xIndices[_nextRouteGoal] - _physBody.getPosition().x, _myRoute._yIndices[_nextRouteGoal] - _physBody.getPosition().y);
                return;
            }   
        }
        else {
            // NB: if we get here, we didn't need to change the velocity
            return;
        }
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
        //_sprite.registerEntityModifier(new LoopEntityModifier(new RotationModifier(duration, 0, 360)));
    }

    /**
     * Move an entity's image. This has well-defined behavior, except that when we apply a route to an entity, we need
     * to move its _physics body along with the image.
     * 
     * @param x
     *            The x coordinate of the top left corner
     * @param y
     *            The y coordinate of the top left corner
     * 
     * @return True if there was a route, false otherwise
     *//*
    protected boolean setSpritePosition(float x, float y)
    {
        // if we don't have a route, use the default behavior
        if (!_isRoute)
            return false;

        // otherwise, move the body based on where the _sprite just went
        _sprite.setX(x);
        _sprite.setY(y);
        _routeVector.x = (x + _sprite.getWidth() * 0.5f) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
        _routeVector.y = (y + _sprite.getHeight() * 0.5f) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
        _physBody.setTransform(_routeVector, 0);
        _routeVector.x = 0;
        _routeVector.y = 0;
        updateVelocity(_routeVector);
        _physBody.setAngularVelocity(0);
        return true;
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
    private boolean              _isDrag      = false;

    /**
     * Rather than use a Vector2 pool, we'll keep a vector around for drag operations
     */
    private static final Vector2 _dragVector  = new Vector2();

    /**
     * A multiplicative factor to apply when flicking an entity
     */
    private float                _flickDampener;

    /**
     * Track if this entity can be flicked
     */
    private boolean              _isFlick     = false;

    /**
     * Track the x coordinate (screen, not _sprite) of where a flick began
     */
    private static float         _flickStartX;

    /**
     * Track the y coordinate (screen, not _sprite) of where a flick began
     */
    private static float         _flickStartY;

    /**
     * Track the entity being flicked
     */
    private static PhysicsSprite _flickEntity;

    /**
     * Internal vector for computing flicks, so that we don't need to use a Vector pool
     */
    private static Vector2       _flickVector = new Vector2();

    /**
     * An entity involved in poke path movement
     */
    private static PhysicsSprite _pokePathEntity;

    /**
     * Track if this entity can be moved via the creation of paths based on poking
     */
    private boolean              _isPokePath;

    /**
     * Track if we keep the pokeEntities or require re-touching them on every movement
     */
    private boolean              _keepPokeEntity;

    /**
     * If this is a poke path entity, how long should it take to travel its path?
     */
    private float                _pokeTravelTime;

    /**
     * Indicate that we're reusing _pokeTravelTime as a velocity
     */
    private boolean              _pokeFixedVelocity;

    /**
     * An entity involved in poke velocity movement
     */
    private static PhysicsSprite _pokeVelocityEntity;

    /**
     * Track if this entity can be given velocity based on poking
     */
    private boolean              _isPokeVelocity;

    /**
     * If this is a poke velocity entity, what velocity should be used?
     */
    private float                _pokeVelocity;

    /**
     * In "chase mode", a poke entity will change its path based on dragging, and will stop when a touch ends.
     */
    private boolean              _pokeChaseMode;

    /**
     * Call this on an entity to make it draggable.
     * 
     * Be careful when dragging things. If they are small, they will be hard to touch.
     * 
     * @param canMove
     *            true if the obstacle can move as a result of collisions with other things, false otherwise.
     *//*
    public void setCanDrag(boolean canMove)
    {
        if (canMove)
            makeMoveable();
        _isDrag = true;
        makeTouchable();
    }

    /**
     * Indicate that this entity can be flicked on the screen
     * 
     * @param _dampFactor
     *            A value that is multiplied by the vector for the flick, to affect speed
     *//*
    public void setFlickable(float dampFactor)
    {
        makeMoveable();
        _isFlick = true;
        _flickDampener = dampFactor;

        makeTouchable();
        // NB: this will cause Framework to call to Obstacle which will call to
        // PhysicsSprite
        Level._current.setOnSceneTouchListener(ALE._self);
    }

    /**
     * Turn on touch handling for this sprite, so that a handler will run whenever it is touched
     *//*
    private void makeTouchable()
    {
        Level._current.registerTouchArea(_sprite);
        Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
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
     *//*
    private void registerInitialFlick(TouchEvent e, float x, float y)
    {
        if (e.getAction() == TouchEvent.ACTION_DOWN) {
            ALE._self.getEngine().vibrate(100);
            // don't forget to translate the touch into a screen coordinate
            _flickStartX = x + _sprite.getX();
            _flickStartY = y + _sprite.getY();
            _flickEntity = this;
        }
    }

    /**
     * Indicate that we should not "forget" a poke-path entity after we've done one movement with it
     */
    public void setKeepPokeEntity()
    {
        _keepPokeEntity = true;
    }

    /**
     * Handle a scene touch that corresponds to the release of a flick object
     * 
     * @param scene
     *            The scene that was touched
     * @param te
     *            A description of the touch event
     * @return True if we handled the event
     *//*
    static boolean handleSceneTouch(final Scene scene, final TouchEvent te)
    {
        if (Level._physics != null) {
            // only do this if we have a valid scene, valid _physics, a valid
            // _flickEntity, and an UP action
            if (_flickEntity != null) {
                if (te.getAction() == TouchEvent.ACTION_UP) {
                    // if the entity was hovering, stop hovering
                    _flickEntity._hover = false;
                    // make sure the entity is now a DynamicBody
                    _flickEntity.makeMoveable();
                    // compute velocity for the flick
                    _flickVector.x = (te.getX() - _flickStartX) * _flickEntity._flickDampener;
                    _flickVector.y = (te.getY() - _flickStartY) * _flickEntity._flickDampener;
                    _flickEntity.updateVelocity(_flickVector);
                    // clear the flick, so we don't have strange "memory"
                    // issues...
                    _flickEntity = null;
                    return true;
                }
            }
            // only do this if we're making a poke path
            else if (_pokePathEntity != null) {
                if (te.getAction() == TouchEvent.ACTION_DOWN || (_pokePathEntity._pokeChaseMode && te.getAction() == TouchEvent.ACTION_MOVE)) {
                    Route r = new Route(2).to(_pokePathEntity.getXPosition(), _pokePathEntity.getYPosition()).to(
                            te.getX(), te.getY());
                    
                    if (_pokePathEntity._reverseFace) 
                        _pokePathEntity.reverseFace(te.getX() < _pokePathEntity._sprite.getX());

                    // remove any older paths
                    _pokePathEntity._sprite.clearEntityModifiers();

                    // drop a new path on this entity
                    _pokePathEntity.makeMoveable();
                    final PhysicsSprite ps = _pokePathEntity;
                    IEntityModifierListener l = new IEntityModifierListener()
                    {
                        @Override
                        public void onModifierStarted(IModifier<IEntity> arg0, IEntity arg1)
                        {
                        }

                        @Override
                        public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem)
                        {
                            ps._isRoute = false;
                        }
                    };
                    if (_pokePathEntity._pokeFixedVelocity) {
                        // travel at a fixed velocity
                        float dx = _pokePathEntity.getXPosition() - te.getX();
                        dx = dx * dx;
                        float dy = _pokePathEntity.getYPosition() - te.getY();
                        dy = dy * dy;
                        float dist = (float) Math.sqrt(dx + dy) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
                        float time = dist / _pokePathEntity._pokeTravelTime;
                        _pokePathEntity._sprite.registerEntityModifier(new PathModifier(time, r, l));
                    }
                    else {
                        // travel for a fixed amount of time
                        _pokePathEntity._sprite.registerEntityModifier(new PathModifier(
                                _pokePathEntity._pokeTravelTime, r, l));
                    }
                    _pokePathEntity._isRoute = true;

                    // clear the pokePathEntity, so a future touch starts the process over
                    if (!_pokePathEntity._keepPokeEntity)
                        _pokePathEntity = null;
                }
                else if (te.getAction() == TouchEvent.ACTION_UP) {
                    if (_pokePathEntity._pokeChaseMode) {
                        // remove any older paths
                        _pokePathEntity._sprite.clearEntityModifiers();

                        // clear the pokePathEntity, so a future touch starts the process over
                        if (!_pokePathEntity._keepPokeEntity)
                            _pokePathEntity = null;
                    }
                }
            }
            else if (_pokeVelocityEntity != null) {
                if (te.getAction() == TouchEvent.ACTION_DOWN || (_pokeVelocityEntity._pokeChaseMode && te.getAction() == TouchEvent.ACTION_MOVE)) {
                    // Figure out a vector for the movement, so we have direction
                    float x = te.getX() - _pokeVelocityEntity._sprite.getX();
                    float y = te.getY() - _pokeVelocityEntity._sprite.getY();

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
                else if (te.getAction() == TouchEvent.ACTION_UP) {
                    if (_pokeVelocityEntity._pokeChaseMode) {
                        // stop the entity
                        _pokeVelocityEntity.updateVelocity(0, 0);

                        // clear the pokePathEntity, so a future touch starts the process over
                        if (!_pokeVelocityEntity._keepPokeEntity)
                            _pokeVelocityEntity = null;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Hook for custom logic whenever a PhysicSprite's underlying AnimatedSprite calls onAreaTouched... this will often
     * be overloaded
     * 
     * @param e
     *            The touch event that occurred
     * @param x
     *            X coordinate of the touch
     * @param y
     *            Y coordinate of the touch
     * 
     * @return True iff the event was handled
     *//*
    protected boolean onSpriteAreaTouched(TouchEvent e, float x, float y)
    {
        // on a down press of a live-edit object, just run the editor and return
        if (e.isActionDown() && isLiveEdit) {
            ALE.launchLiveEditor(this);
            return true;
        }

        // if the object is a drag object, then move it according to the
        // location of the user's finger
        if (_isDrag) {
            if (Configuration.isVibrationOn())
                ALE._self.getEngine().vibrate(100);
            // before setting position, shut off velocity and acceleration
            _physBody.setAngularVelocity(0);
            updateVelocity(0, 0);
            // now update position
            float newX = e.getX() - _sprite.getWidth() / 2;
            float newY = e.getY() - _sprite.getHeight() / 2;
            this.setSpritePosition(newX, newY);
            _dragVector.x = newX;
            _dragVector.y = newY;
            _physBody.setTransform(_dragVector.mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT),
                    _physBody.getAngle());
            return true;
        }
        else if (_isFlick) {
            registerInitialFlick(e, x, y);
            return true;
        }
        else if (_isPokePath && e.isActionDown()) {
            _pokePathEntity = this;
            ALE._self.getEngine().vibrate(5);
            return true;
        }
        else if (_isPokeVelocity && e.isActionDown()) {
            _pokeVelocityEntity = this;
            ALE._self.getEngine().vibrate(5);
            return true;
        }
        // remember: returning false means that this handler didn't do anything,
        // so we should propagate the event to another handler
        return false;
    }

    /**
     * Call this on an Entity to indicate that it can move on the screen via poking
     * 
     * Poke the Entity, then poke the screen, and the Entity will move to the location that was pressed by following a
     * path.
     * 
     * @param duration
     *            The time it takes to move to the new location
     *//*
    public void setPokePath(float duration)
    {
        _isPokePath = true;
        _pokeTravelTime = duration;
        Level._current.registerTouchArea(_sprite);
        Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
        Level._current.setOnSceneTouchListener(ALE._self);
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
        _pokeTravelTime = velocity;
        _pokeFixedVelocity = true;
    }

    /**
     * Call this on an Entity to indicate that it can move on the screen via poking
     * 
     * Poke the Entity, then poke the screen, and the Entity will move toward the location that was pressed, with a
     * fixed velocity
     * 
     * @param magnitude
     *            The magnitude of the velocity when the entity starts moving
     *//*
    public void setPokeVelocity(float magnitude)
    {
        makeMoveable();
        _isPokeVelocity = true;
        _pokeVelocity = magnitude;
        Level._current.registerTouchArea(_sprite);
        Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
        Level._current.setOnSceneTouchListener(ALE._self);
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
    int[]         _defaultAnimateCells;
    /**
     * Animation support: the durations for the default animation
     */
    long[]        _defaultAnimateDurations;

    /**
     * Animation support: the cells of the disappearance animation
     */
    int[]         _disappearAnimateCells;

    /**
     * Animation support: the durations for the disappearance animation
     */
    long[]        _disappearAnimateDurations;
    /**
     * Animation support: name of the image to use for a disappearance animation
     */
    String        _disappearAnimateImageName;

    /**
     * Animation support: the offset for placing the disappearance animation relative to the disappearing _sprite
     */
    final Vector2 _disappearAnimateOffset = new Vector2();

    /**
     * Animation support: the width of the disappearance animation
     */
    float         _disappearAnimateWidth;

    /**
     * Animation support: the height of the disappearance animation
     */
    float         _disappearAnimateHeight;

    /**
     * Does the entity's image flip when the hero moves backwards?
     */
    private boolean       _reverseFace            = false;

    /**
     * Save the animation sequence and start it right away
     * 
     * @param cells
     *            which cells of the _sprite to show
     * @param durations
     *            duration for each cell
     *//*
    public void setDefaultAnimation(int[] cells, long[] durations)
    {
        _defaultAnimateCells = cells;
        _defaultAnimateDurations = durations;
        _sprite.animate(_defaultAnimateDurations, _defaultAnimateCells, true);
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
    public void setDisappearAnimation(int[] cells, long[] durations, String imageName, float offsetX, float offsetY,
            float width, float height)
    {
        _disappearAnimateCells = cells;
        _disappearAnimateDurations = durations;
        _disappearAnimateImageName = imageName;
        _disappearAnimateOffset.x = offsetX;
        _disappearAnimateOffset.y = offsetY;
        _disappearAnimateWidth = width;
        _disappearAnimateHeight = height;
    }

    /**
     * Indicate that this entity's image should be reversed when it is moving in the negative x direction.  For tilt, this only
     * applies to the last hero created.  For velocity, it applies to everyone.
     */
    public void setCanFaceBackwards()
    {
        _reverseFace = true;
    }

    /**
     * Internal method for updating an entity's velocity, so that we can handle its direction correctly
     * 
     * @param v a vector representing the new velocity
     */
    void updateVelocity(Vector2 v) 
    {
        updateVelocity(v.x, v.y);
    }
    
    /**
     * Internal method for updating an entity's velocity, so that we can handle its direction correctly
     * 
     * @param x 
     *          The new x velocity
     * @param y 
     *          The new y velocity
     *//*
    void updateVelocity(float x, float y) 
    {
        _physBody.setLinearVelocity(x, y);
        // manage changing direction of entity
        if (_reverseFace && x != 0)
            _sprite.setFlippedHorizontal(x < 0);
    }
    
    /**
     * Internal method for checking an entity's velocity in those cases where we apply force
     * 
     * @param flip true if the entity should face backwards
     *//*
    void reverseFace(boolean flip) 
    {
        if (_reverseFace)
            _sprite.setFlippedHorizontal(flip);
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
     *//*
    public void setAppearDelay(float delay)
    {
        // hide the picture and disable the _physics on this object
        _sprite.setVisible(false);
        _physBody.setActive(false);
        // set a timer for turning said entities on
        TimerHandler th = new TimerHandler(delay, false, new ITimerCallback()
        {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler)
            {
                _sprite.setVisible(true);
                _physBody.setActive(true);
            }
        });
        Level._current.registerUpdateHandler(th);
    }

    /**
     * Indicate that something should disappear after a little while
     * 
     * @param delay
     *            How long to wait before hiding the thing
     * 
     * @param quiet
     *            true if the item should disappear quietly, false if it should play its disappear sound
     *//*
    public void setDisappearDelay(float delay, final boolean quiet)
    {
        // set a timer for disabling the thing
        TimerHandler th = new TimerHandler(delay, false, new ITimerCallback()
        {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler)
            {
                remove(quiet);
            }
        });
        Level._current.registerUpdateHandler(th);
    }

    /**
     * Indicate that this entity should shrink over time
     * 
     * @param shrinkX
     *            The number of pixels by which the X dimension should shrink each second
     * @param shrinkY
     *            The number of pixels by which the Y dimension should shrink each second
     *//*
    public void setShrinkOverTime(final float shrinkX, final float shrinkY)
    {
        // set a timer for handling the shrink
        TimerHandler th = new TimerHandler(.05f, true, new ITimerCallback()
        {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler)
            {
                float x = _sprite.getX();
                float y = _sprite.getY();
                float w = _sprite.getWidth();
                float h = _sprite.getHeight();
                w -= shrinkX / 20;
                h -= shrinkY / 20;
                if ((w > 0) && (h > 0)) {
                    // disable the old _fixture, but remember if it was a sensor
                    boolean wasSensor = _physBody.getFixtureList().get(0).isSensor();
                    _physBody.setActive(false);
                    Level._physics.getPhysicsConnectorManager().remove(_pc);
                    Level._physics.destroyBody(_physBody);

                    // update the position
                    x += shrinkX / 20 / 2;
                    y += shrinkY / 20 / 2;
                    _sprite.setX(x);
                    _sprite.setY(y);
                    _sprite.setWidth(w);
                    _sprite.setHeight(h);

                    // attach a new _fixture that is appropriate for our resized
                    // _sprite
                    float _density = _physBody.getFixtureList().get(0).getDensity();
                    float _elasticity = _physBody.getFixtureList().get(0).getRestitution();
                    float _friction = _physBody.getFixtureList().get(0).getFriction();
                    boolean _isBullet = _physBody.isBullet();
                    BodyType _t = _physBody.getType();

                    if (_isCircle)
                        setCirclePhysics(_density, _elasticity, _friction, _t, _isBullet);
                    else
                        setBoxPhysics(_density, _elasticity, _friction, _t, _isBullet);
                    // patch up if it was a sensor
                    if (wasSensor)
                        setCollisionEffect(false);
                }
                else {
                    remove(false);
                    Level._current.unregisterUpdateHandler(pTimerHandler);
                }
            }
        });
        Level._current.registerUpdateHandler(th);
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
    private Vector2 _hoverVector = new Vector2();

    /**
     * For hovering in the Y dimension but not the X dimension
     */
    private Vector2 _gravityDefy;
    
    /**
     * Hook for custom logic whenever a PhysicSprite's underlying AnimatedSprite calls onManagedUpdate... this will
     * often be overloaded, so be sure to call back to it from any onSpriteManagedUpdate
     *//*
    protected void onSpriteManagedUpdate()
    {
        if (_hover) {
            float x = _hoverX + ALE._self._camera.getCenterX() - Configuration.getCameraWidth() / 2;
            float y = _hoverY + ALE._self._camera.getCenterY() - Configuration.getCameraHeight() / 2;

            _hoverVector.x = x;
            _hoverVector.y = y;
            _hoverVector.mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
            _physBody.setTransform(_hoverVector, _physBody.getAngle());
        }
    }

    /**
     * Indicate that this entity should _hover at a specific location on the screen, rather than being placed at some
     * point on the level itself. Note that the coordinates to this command are the center position of the hovering
     * entity.
     * 
     * @param x
     *            the X coordinate where the entity should appear
     * @param y
     *            the Y coordinate where the entity should appear
     *//*
    public void setHover(int x, int y)
    {
        // make the entity kinematic, so gravity doesn't affect it but it still participates in collisions
        makeKinematic();
        // save the parameters
        _hoverX = x;
        _hoverY = y;
        _hover = true;
    }

    /**
     * Indicate that this entity should have a force applied to it at all times, e.g., to defy gravity.
     * 
     * @param x
     *            The magnitude of the force in the X direction
     * @param y
     *            The magnitude of the force in the Y direction
     */
    public void setGravityDefy(float x, float y) 
    {
        _physBody.setBullet(true);
        _gravityDefy = new Vector2(x, y);
        Level._noGravity.add(this);
    }
    
    /**
     * A callback to defy gravity
     *//*
    void defyGravity() 
    {
        _physBody.applyForce(_gravityDefy.x * _physBody.getMass(), _gravityDefy.y * _physBody.getMass(),
                _physBody.getWorldCenter().x, _physBody.getWorldCenter().y);
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

    /*
     * ONE-SIDED ENTITIES AND PASS-THROUGH ENTITIES
     */

    /**
     * Track which sides are one-sided. 0 is top, 1 is right, 2 is bottom, 3 is left
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
     * Developer feature: indicate that whenever this entity is touched, a dialog box should appear to allow the
     * programmer to adjust size, position, and physics.
     *//*
    public void setLiveEdit()
    {
        isLiveEdit = true;
        makeTouchable();
    }

    /**
     * By default, non-hero entities are not subject to gravity until they are given a path, velocity, or other form of
     * motion. This lets an entity simply fall.
     *//*
    public void setCanFall()
    {
        makeMoveable();
    }

    /**
     * Make an entity disappear
     * 
     * @param quiet
     *            True if the disappear sound should not be played
     *//*
    public void remove(boolean quiet)
    {
        _sprite.setVisible(false);
        if (_disappearAnimateCells != null) {
            float x = _sprite.getX() + _disappearAnimateOffset.x;
            float y = _sprite.getY() + _disappearAnimateOffset.y;
            TiledTextureRegion ttr = Media.getImage(_disappearAnimateImageName);
            AnimatedSprite as = new AnimatedSprite(x, y, _disappearAnimateWidth, _disappearAnimateHeight, ttr,
                    ALE._self.getVertexBufferObjectManager());
            Level._current.attachChild(as);
            as.animate(_disappearAnimateDurations, _disappearAnimateCells, false);
        }
        // play a sound when we hit this thing?
        if (_disappearSound != null && !quiet)
            _disappearSound.play();
        // disable the _physics body
        _physBody.setActive(false);
    }
    */
    
}