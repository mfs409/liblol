package edu.lehigh.cse.ale;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

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
    public TextureRegion _tr;
    
    /**
     * Type of this PhysicsSprite; useful for disambiguation in collision detection
     */
    protected SpriteId _psType;

    /**
     * Text that user can modify to hold additional information
     */
    String             _infoText = "";

    public float _width;
    public float _height;
    
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
    public Body             _physBody  = null;

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
    void setBoxPhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile)
    {
        // FixtureDef fd = PhysicsFactory.createFixtureDef(density, elasticity, friction, false);
        // _physBody = PhysicsFactory.createBoxBody(Level._physics, _sprite, type, fd);
        if (isProjectile)
            _physBody.setBullet(true);
        // _pc = new PhysicsConnector(_sprite, _physBody, true, _canRotate);
        // Level._physics.registerPhysicsConnector(_pc);
        _physBody.setUserData(this);
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
    void setCirclePhysics(float density, float elasticity, float friction, BodyType type, boolean isProjectile)
    {
        // define the _fixture and body
        // FixtureDef fd = PhysicsFactory.createFixtureDef(density, elasticity, friction, false);
        // _physBody = PhysicsFactory.createCircleBody(Level._physics, _sprite, type, fd);
        if (isProjectile)
            _physBody.setBullet(true);
        // connect sprite and _fixture
        // _pc = new PhysicsConnector(_sprite, _physBody, true, _canRotate);
        // Level._physics.registerPhysicsConnector(_pc);
        // attach this to the body for collision callbacks
        _physBody.setUserData(this);
        // remember this is a circle
        _isCircle = true;
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
        // get information from previous body
        boolean wasSensor = _physBody.getFixtureList().get(0).isSensor();
        boolean _isBullet = _physBody.isBullet();
        BodyType _t = _physBody.getType();

        // delete old body, make a new body
        deletePhysicsBody();
        if (_isCircle)
            setCirclePhysics(density, elasticity, friction, _t, _isBullet);
        else
            setBoxPhysics(density, elasticity, friction, _t, _isBullet);
        // patch up if it was a sensor
        if (wasSensor)
            setCollisionEffect(false);
    }

    /**
     * Reset the _current PhysicsSprite as non-rotateable
     */
    public void disableRotation()
    {
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
        if (_isCircle)
            setCirclePhysics(_density, _elasticity, _friction, _t, _isBullet);
        else
            setBoxPhysics(_density, _elasticity, _friction, _t, _isBullet);
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

    ///// STOPPED AT POINT OF COPYING MOTION FUNCTIONALITY
    
    

    /**
     * Returns the X coordinate of this entity
     * 
     * @return x coordinate of top left corner
     */
    public float getXPosition()
    {
        // return _sprite.getX();
    	return 0;
    }

    /**
     * Returns the Y coordinate of this entity
     * 
     * @return y coordinate of top left corner
     */
    public float getYPosition()
    {
        return 0; //return _sprite.getY();
    }

    /**
     * Returns the width of this entity
     * 
     * @return the entity's width
     */
    public float getWidth()
    {
    	return 0; //return _sprite.getWidth();
    }

    /**
     * Return the height of this entity
     * 
     * @return the entity's height
     */
    public float getHeight()
    {
    	return 0; //return _sprite.getHeight();
    }


}

