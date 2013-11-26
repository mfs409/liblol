package edu.lehigh.cse.ale;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public abstract class PhysicsSprite {

    /*
     * INTERNAL CLASSES
     */

	/**
     * This enum encapsulates the different types of PhysicsSprite entities
     */
    enum SpriteId {
        UNKNOWN(0), HERO(1), ENEMY(2), GOODIE(3), PROJECTILE(4), OBSTACLE(5), SVG(6), DESTINATION(7);

        /**
         * To each ID, we attach an integer value, so that we can compare the different IDs and establish a hierarchy
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
     * Type of this PhysicsSprite; useful for disambiguation in collision detection
     */
    SpriteId _psType;

    /**
     * Text that user can modify to hold additional information
     */
    String             _infoText = "";

    float _width;
    float _height;
    
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
    Body             _physBody;

    /**
     * Track whether the underlying body is a circle or box
     */
    protected boolean          _isCircle;

    /**
     * Remember if this _physics body can rotate
     */
    protected boolean          _canRotate = true;

    /**
     * Each descendant defines this to address any custom logic that we need to deal with on a collision
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
    void setBoxPhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile, float x, float y)
    {
    	// NB: this is not a circle... it's a box...
		PolygonShape boxPoly = new PolygonShape();
		boxPoly.setAsBox(_width/2, _height/2);
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = type;
		boxBodyDef.position.x = x;
		boxBodyDef.position.y = y;
		_physBody = Level._current._world.createBody(boxBodyDef);

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
    void setCirclePhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile, float x, float y, float r)
    {
        _isCircle = true;

		// NB: this is a circle... really!
		CircleShape c = new CircleShape();
		c.setRadius(r);
		BodyDef boxBodyDef = new BodyDef();
		boxBodyDef.type = type;
		boxBodyDef.position.x = x;
		boxBodyDef.position.y = y;
		_physBody = Level._current._world.createBody(boxBodyDef);

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
     *            either true or false. true indicates that the object will participate in _physics collisions. false
     *            indicates that it will not.
     */
    public void setCollisionEffect(boolean state)
    {
        _physBody.getFixtureList().get(0).setSensor(!state);
    }

    /**
     * Allow the user to adjust the default _physics settings (density, elasticity, friction) for this entity
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
        // get information from previous body
        boolean wasSensor = _physBody.getFixtureList().get(0).isSensor();
        boolean _isBullet = _physBody.isBullet();
        BodyType _t = _physBody.getType();

        // delete old body, make a new body
        deletePhysicsBody();
        // TODO
        //if (_isCircle)
        //    setCirclePhysics(density, elasticity, friction, _t, _isBullet);
        //else
        //    setBoxPhysics(density, elasticity, friction, _t, _isBullet);
        // patch up if it was a sensor
        if (wasSensor)
            setCollisionEffect(false);
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
        //if (_isCircle)
        //    setCirclePhysics(_density, _elasticity, _friction, _t, _isBullet);
        //else
        //    setBoxPhysics(_density, _elasticity, _friction, _t, _isBullet);
        // patch up if it was a sensor
        if (wasSensor)
            setCollisionEffect(false);
    }

    /**
     * Delete the current physics attached to this Sprite.
     * 
     * We use this when we're changing the body on the fly, as it simplifies the task of removing the old body.
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
        return _physBody.getPosition().x; //return _sprite.getY();
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
     * Internal method for updating an entity's velocity, so that we can handle its direction correctly
     * 
     * @param x 
     *          The new x velocity
     * @param y 
     *          The new y velocity
     */
    void updateVelocity(float x, float y) 
    {
        _physBody.setLinearVelocity(x, y);
        
        // TODO: support reverse face
        // manage changing direction of entity
        //if (_reverseFace && x != 0)
        //    _sprite.setFlippedHorizontal(x < 0);
    }

    boolean _isTilt;
    
    /**
     * Indicate that the _sprite should move with the tilt of the phone
     */
    public void setMoveByTilting()
    {
        if (!_isTilt) {
            // make sure it is moveable, add it to the list of tilt entities
            // TODO
        	// makeMoveable();
            synchronized (Tilt._accelEntities) {
                Tilt._accelEntities.add(this);
            }
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
    public void scheduleRemove(boolean quiet)
    {
        // set it invisible immediately, so that future calls know to ignore this PhysicsSprite
        _visible = false;
        _deleteQuietly = quiet;
        Level._current._deletions.add(this);
    }
    
    public void remove () 
    {
        _physBody.setActive(false);
    	// TODO
    	/*
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
        */
    }

}