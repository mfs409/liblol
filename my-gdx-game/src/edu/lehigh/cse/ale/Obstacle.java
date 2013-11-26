package edu.lehigh.cse.ale;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

/**
 * Obstacles are entities that change the hero's velocity upon a collision
 * 
 * There are many flavors of obstacles. They can have a _physics shape that is
 * circular or square. They can have default
 * collision behavior or custom behavior. They can be moved by dragging. They
 * can move by touching the object and then
 * touching a point on the screen. They can have "damp" behavior, which is a way
 * to do tricks with Physics (such as zoom
 * strips or friction pads). A method for drawing bounding boxes on the screen
 * is also available, as is a means of
 * creating "trigger" obstacles that cause user-specified code to run upon any
 * collision. There is also a simple object
 * type for loading SVG files, such as those created by Inkscape.
 * 
 * TODO: add delays for non-enemy collide triggers
 */
public class Obstacle extends PhysicsSprite
{
    /*
     * BASIC FUNCTIONALITY
     */

    /**
     * Internal constructor to build an Obstacle.
     * 
     * This should never be invoked directly. Instead, use the 'addXXX' methods
     * of the Object class.
     * 
     * @param x
     *            X position of top left corner
     * @param y
     *            Y position of top left corner
     * @param width
     *            width of this Obstacle
     * @param height
     *            height of this Obstacle
     * @param tr
     *            image to use for this Obstacle
     */
    protected Obstacle(float width, float height, TextureRegion tr)
    {
        super(tr, SpriteId.OBSTACLE, width, height);
    }

    /**
     * Draw an obstacle with an underlying box shape
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param imgName
     *            Name of image file to use
     * @return The obstacle, so that it can be further modified
     */
    public static Obstacle makeAsBox(float x, float y, float width, float height, String imgName)
    {
        Obstacle o = new Obstacle(width, height, Media.getImage(imgName));
        o.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        o._physBody.getFixtureList().get(0).setSensor(false);
        Level._current._sprites.add(o);
        return o;
    }

    /**
     * Draw an obstacle with an underlying circle shape
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param imgName
     *            Name of image file to use
     * @return The obstacle, so that it can be further modified
     */
    public static Obstacle makeAsCircle(float x, float y, float width, float height, String imgName)
    {
        Obstacle o = new Obstacle(width, height, Media.getImage(imgName));
        float radius = (width > height) ? width : height;
        o.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius);
        Level._current._sprites.add(o);
        return o;
    }

    /**
     * Call this on an Obstacle to rotate it. Note that this works best on
     * boxes.
     * 
     * @param rotation
     *            amount to rotate the Obstacle (in degrees)
     */
    public void setRotation(float rotation)
    {
        // rotate it
        _physBody.setTransform(_physBody.getPosition(), rotation);
        // _sprite.setRotation(rotation);
    }

    /*
     * SIMPLE HERO INTERACTIVITY
     */

    /**
     * Track if the obstacle has an active "dampening" factor for custom
     * _physics tricks
     */
    boolean _isDamp;

    /**
     * The dampening factor of this obstacle
     */
    float   _dampFactor;

    /**
     * Indicate that this is a speed boost object
     */
    boolean _isSpeedBoost;

    /**
     * Speed boost to apply in X direction when this obstacle is encountered
     */
    float   _speedBoostX;

    /**
     * Speed boost to apply in Y direction when this obstacle is encountered
     */
    float   _speedBoostY;

    /**
     * Duration for which speed boost is to be applied
     */
    float   _speedBoostDuration;

    /**
     * Indicate that this obstacle does not re-enable jumping for the hero
     */
    boolean _noJumpReenable;

    /**
     * Call this on an Obstacle to give it a dampening factor.
     * 
     * A hero can glide over damp Obstacles. Damp factors can be negative to
     * cause a reverse direction, less than 1 to
     * cause a slowdown (friction pads), or greater than 1 to serve as zoom
     * pads.
     * 
     * @param factor
     *            Value to multiply the hero's velocity when it is on this
     *            Obstacle
     */
    public void setDamp(float factor)
    {
        // We have the fixtureDef for this object, but it's the Fixture that we
        // really need to modify. Find it, and set it to be a sensor
        _physBody.getFixtureList().get(0).setSensor(true);
        // set damp info
        _dampFactor = factor;
        _isDamp = true;
    }

    /**
     * Call this on an event to make it behave like a "damp" obstacle, except
     * with a constant additive (or subtractive)
     * effect on the hero's speed.
     * 
     * @param boostAmountX
     *            The amount to add to the hero's X velocity
     * @param boostAmountY
     *            The amount to add to the hero's Y velocity
     * @param boostDuration
     *            How long should the speed boost last (use -1 to indicate
     *            "forever")
     */
    public void setSpeedBoost(float boostAmountX, float boostAmountY, float boostDuration)
    {
        // We have the fixtureDef for this object, but it's the Fixture that we
        // really need to modify. Find it, and set it to be a sensor
        _physBody.getFixtureList().get(0).setSensor(true);

        // save the parameters, so that we can use them later
        _speedBoostX = boostAmountX;
        _speedBoostY = boostAmountY;
        _speedBoostDuration = boostDuration;
        _isSpeedBoost = true;
    }

    /**
     * Control whether the hero can jump if it collides with this obstacle while
     * in the air
     * 
     * @param enable
     *            true if the hero can jump again, false otherwise
     */
    public void setReJump(boolean enable)
    {
        _noJumpReenable = !enable;
    }

    /*
     * SIMPLE ENEMY INTERACTIVITY
     */

    /**
     * Track if the obstacle can modify the enemy jump velocity
     */
    boolean _isEnemyJump       = false;

    /**
     * Jump applied in Y direction when this obstacle is encountered
     */
    float   _enemyXJumpImpulse = 0;

    /**
     * Jump applied in X direction when this obstacle is encountered
     */
    float   _enemyYJumpImpulse = 0;

    /**
     * Method to set an obstacle that modifies the enemy jump velocity.
     * 
     * @param x
     *            The new x velocity
     * @param y
     *            The new y velocity
     */
    public void setEnemyJump(float x, float y)
    {
        _enemyYJumpImpulse = y;
        _enemyXJumpImpulse = x;
        _isEnemyJump = true;
    }

    /*
     * EVENT TRIGGER SUPPORT
     */

    /**
     * Track if this is a "trigger" object that causes special code to run upon
     * any collision with a hero
     */
    boolean _isHeroCollideTrigger         = false;

    /**
     * Hero triggers can require certain Goodie counts in order to run
     */
    int     _heroTriggerActivation1       = 0;

    /**
     * Hero triggers can require certain Goodie counts in order to run
     */
    int     _heroTriggerActivation2       = 0;

    /**
     * Hero triggers can require certain Goodie counts in order to run
     */
    int     _heroTriggerActivation3       = 0;

    /**
     * Hero triggers can require certain Goodie counts in order to run
     */
    int     _heroTriggerActivation4       = 0;

    /**
     * An ID for each hero trigger object, in case it's useful
     */
    int     _heroTriggerID;

    /**
     * Track if this is a "trigger" object that causes special code to run upon
     * any collision with an enemy
     */
    boolean _isEnemyCollideTrigger        = false;

    /**
     * Enemy triggers can require certain Goodie counts in order to run
     */
    int     _enemyTriggerActivation1      = 0;

    /**
     * Enemy triggers can require certain Goodie counts in order to run
     */
    int     _enemyTriggerActivation2      = 0;

    /**
     * Enemy triggers can require certain Goodie counts in order to run
     */
    int     _enemyTriggerActivation3      = 0;

    /**
     * Enemy triggers can require certain Goodie counts in order to run
     */
    int     _enemyTriggerActivation4      = 0;

    /**
     * An ID for each enemy trigger object, in case it's useful
     */
    int     _enemyTriggerID;

    /**
     * How long to wait before running trigger code.
     */
    float   _enemyCollideTriggerDelay     = 0;

    /**
     * Track if this is a "trigger" object that causes special code to run upon
     * any collision with a projectile
     */
    boolean _isProjectileCollideTrigger   = false;

    /**
     * Projectile triggers can require certain Goodie counts in order to run
     */
    int     _projectileTriggerActivation1 = 0;

    /**
     * Projectile triggers can require certain Goodie counts in order to run
     */
    int     _projectileTriggerActivation2 = 0;

    /**
     * Projectile triggers can require certain Goodie counts in order to run
     */
    int     _projectileTriggerActivation3 = 0;

    /**
     * Projectile triggers can require certain Goodie counts in order to run
     */
    int     _projectileTriggerActivation4 = 0;

    /**
     * An ID for each projectile trigger object, in case it's useful
     */
    int     _projectileTriggerID;

    /**
     * Track if the object is a touch trigger
     */
    boolean _isTouchTrigger               = false;

    /**
     * Touch triggers can require certain Goodie counts in order to run
     */
    int     _touchTriggerActivation1      = 0;

    /**
     * Touch triggers can require certain Goodie counts in order to run
     */
    int     _touchTriggerActivation2      = 0;

    /**
     * Touch triggers can require certain Goodie counts in order to run
     */
    int     _touchTriggerActivation3      = 0;

    /**
     * Touch triggers can require certain Goodie counts in order to run
     */
    int     _touchTriggerActivation4      = 0;

    /**
     * An ID for each touch trigger object, in case it's useful
     */
    int     _touchTriggerID;

    /**
     * Make the object a trigger object, so that custom code will run when a
     * hero runs over (or under) it
     * 
     * @param activationGoodies
     *            Number of goodies that must be collected before this trigger
     *            works
     * @param id
     *            identifier for the trigger
     * 
     * @deprecated Use setHeroCollisionTriger() (5 parameters) instead
     */
    @Deprecated
    public void setHeroCollisionTrigger(int activationGoodies, int id)
    {
        _heroTriggerID = id;
        _isHeroCollideTrigger = true;
        _heroTriggerActivation1 = activationGoodies;
        setCollisionEffect(false);
    }

    /**
     * Make the object a trigger object, so that custom code will run when a
     * hero runs over (or under) it
     * 
     * @param id
     *            identifier for the trigger
     * @param activationGoodies1
     *            Number of type-1 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies2
     *            Number of type-2 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies3
     *            Number of type-3 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies4
     *            Number of type-4 goodies that must be collected before this
     *            trigger works
     */
    public void setHeroCollisionTrigger(int id, int activationGoodies1, int activationGoodies2, int activationGoodies3,
            int activationGoodies4)
    {
        _heroTriggerID = id;
        _isHeroCollideTrigger = true;
        _heroTriggerActivation1 = activationGoodies1;
        _heroTriggerActivation2 = activationGoodies2;
        _heroTriggerActivation3 = activationGoodies3;
        _heroTriggerActivation4 = activationGoodies4;
        setCollisionEffect(false);
    }

    /**
     * Make the object a trigger object, so that custom code will run when a
     * enemy runs over (or under) it
     * 
     * @param activationGoodies
     *            Number of goodies that must be collected before this trigger
     *            works
     * @param id
     *            identifier for the trigger
     * 
     * @deprecated Use 5-paramter version of this function instead
     */
    @Deprecated
    public void setEnemyCollisionTrigger(int activationGoodies, int id)
    {
        _enemyTriggerID = id;
        _isEnemyCollideTrigger = true;
        _enemyTriggerActivation1 = activationGoodies;
    }

    /**
     * Make the object a trigger object, so that custom code will run when a
     * enemy runs over (or under) it
     * 
     * @param id
     *            identifier for the trigger
     * @param activationGoodies1
     *            Number of type-1 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies2
     *            Number of type-2 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies3
     *            Number of type-3 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies4
     *            Number of type-4 goodies that must be collected before this
     *            trigger works
     */
    public void setEnemyCollisionTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4)
    {
        _enemyTriggerID = id;
        _isEnemyCollideTrigger = true;
        _enemyTriggerActivation1 = activationGoodies1;
        _enemyTriggerActivation2 = activationGoodies2;
        _enemyTriggerActivation3 = activationGoodies3;
        _enemyTriggerActivation4 = activationGoodies4;
    }

    /**
     * Set a delay between when an enemy collides with this trigger, and when
     * this trigger actually executes
     * 
     * @param delayDuration
     *            the delay, in seconds
     */
    public void setEnemyCollideTriggerDelay(float delayDuration)
    {
        _enemyCollideTriggerDelay = delayDuration;
    }

    /**
     * Make the object a trigger object, so that custom code will run when a
     * projectile hits it.
     * 
     * @param activationGoodies
     *            Number of goodies that must be collected before this trigger
     *            works
     * @param id
     *            identifier for the trigger
     * @deprecated Use 5-paramter version of the function instead
     */
    @Deprecated
    public void setProjectileCollisionTrigger(int activationGoodies, int id)
    {
        _projectileTriggerID = id;
        _isProjectileCollideTrigger = true;
        _projectileTriggerActivation1 = activationGoodies;
    }

    /**
     * Make the object a trigger object, so that custom code will run when a
     * projectile hits it.
     * 
     * @param id
     *            identifier for the trigger
     * @param activationGoodies1
     *            Number of type-1 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies2
     *            Number of type-2 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies3
     *            Number of type-3 goodies that must be collected before this
     *            trigger works
     * @param activationGoodies4
     *            Number of type-4 goodies that must be collected before this
     *            trigger works
     */
    public void setProjectileCollisionTrigger(int id, int activationGoodies1, int activationGoodies2,
            int activationGoodies3, int activationGoodies4)
    {
        _projectileTriggerID = id;
        _isProjectileCollideTrigger = true;
        _projectileTriggerActivation1 = activationGoodies1;
        _projectileTriggerActivation2 = activationGoodies2;
        _projectileTriggerActivation3 = activationGoodies3;
        _projectileTriggerActivation4 = activationGoodies4;
    }

    /**
     * Indicate that touching this object will cause some special code to run
     * 
     * @param activationGoodies
     *            Number of goodies that must be collected before it works
     * @param id
     *            identifier for the trigger.
     * @deprecated Use the 5-paramter version of this funciton instead
     */
    @Deprecated
    public void setTouchTrigger(int activationGoodies, int id)
    {
        _touchTriggerID = id;
        _isTouchTrigger = true;
        _touchTriggerActivation1 = activationGoodies;
        // TODO:
        // Level._current.registerTouchArea(_sprite);
        // Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        // Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
    }

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
        _touchTriggerActivation1 = activationGoodies1;
        _touchTriggerActivation2 = activationGoodies2;
        _touchTriggerActivation3 = activationGoodies3;
        _touchTriggerActivation4 = activationGoodies4;
        // TODO:
        // Level._current.registerTouchArea(_sprite);
        // Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        // Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
    }

    /*
     * POKE SUPPORT
     * 
     * TODO: move this into PhysicsSprite, then refactor the onTouch handlers
     */

    /**
     * When a _sprite is poked, we record it here so that we know who to move on
     * the next screen touch
     */
    protected static Obstacle    _currentPokeSprite;

    /**
     * Rather than use a Vector2 pool, we'll keep a vector around for all poke
     * operations
     */
    private final static Vector2 _pokeVector       = new Vector2();

    /**
     * Track if the object is pokeable
     */
    private boolean              _isPoke           = false;

    /**
     * When a _sprite is poked, remember the time, because rapid double-clicks
     * cause deletion
     */
    private static float         _lastPokeTime;

    /**
     * Tunable constant for how much time between pokes constitutes a
     * "double click"
     */
    private final static float   _pokeDeleteThresh = 0.5f;

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

        // TODO
        // Level._current.registerTouchArea(_sprite);
        // Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        // Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
        // Level._current.setOnSceneTouchListener(ALE._self);
    }

    /*
     * SCRIBBLE SUPPORT
     */

    /**
     * The image to draw when we are in scribble mode
     */
    private static String  _scribblePic;

    /**
     * The last x coordinate of a scribble
     */
    private static float   _scribbleX;

    /**
     * The last y coordinate of a scribble
     */
    private static float   _scribbleY;

    /**
     * True if we are in mid-scribble
     */
    private static boolean _scribbleDown;

    /**
     * Density of objects drawn via scribbling
     */
    private static float   _scribbleDensity;

    /**
     * Elasticity of objects drawn via scribbling
     */
    private static float   _scribbleElasticity;

    /**
     * Friction of objects drawn via scribbling
     */
    private static float   _scribbleFriction;

    /**
     * Time before a scribbled object disappears
     */
    static float           _scribbleTime;

    /**
     * Width of the picture being drawn via scribbling
     */
    static int             _scribbleWidth;

    /**
     * Height of the picture being drawn via scribbling
     */
    static int             _scribbleHeight;

    /**
     * Track if the scribble objects move, or are stationary
     */
    static boolean         _scribbleMoveable;

    /**
     * Turn on scribble mode, so that scene touch events draw an object
     * 
     * @param imgName
     *            The name of the image to use for scribbling
     * @param duration
     *            How long the scribble stays on screen before disappearing
     * @param width
     *            Width of the individual components of the scribble
     * @param height
     *            Height of the individual components of the scribble
     * @param density
     *            Density of each scribble component
     * @param elasticity
     *            Elasticity of the scribble
     * @param friction
     *            Friction of the scribble
     * @param moveable
     *            Can the individual items that are drawn move on account of
     *            collisions?
     */
    public static void setScribbleOn(String imgName, float duration, int width, int height, float density,
            float elasticity, float friction, boolean moveable)
    {
        _scribbleTime = duration;
        _scribbleWidth = width;
        _scribbleHeight = height;

        _scribbleDensity = density;
        _scribbleElasticity = elasticity;
        _scribbleFriction = friction;

        // turn on scribble mode, reset scribble status vars
        // TODO:
        // Level._scribbleMode = true;
        _scribbleDown = false;
        _scribbleX = -1000;
        _scribbleY = -1000;
        _scribbleMoveable = moveable;
        // register the scribble picture
        _scribblePic = imgName;
        // turn on touch handling for this scene
        // Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        // Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
        // Level._current.setOnSceneTouchListener(ALE._self);
    }

    /**
     * Code to handle the processing of a scribble event. Whenever we have a
     * scribble event, we will draw an obstacle on
     * the scene. Note that there are some hard-coded values that should become
     * parameters to setScribbleMode()
     * 
     * @param event
     *            The screen touch event
     */
    private static void doScribble(/* final TouchEvent event */)
    {
        /*
         * // remember if we made an obstacle
         * Obstacle o = null;
         * // is this an initial press to start scribbling?
         * if (event.getAction() == TouchEvent.ACTION_DOWN) {
         * if (!_scribbleDown) {
         * // turn on scribbling, draw an obstacle
         * _scribbleDown = true;
         * _scribbleX = event.getX();
         * _scribbleY = event.getY();
         * o = makeAsCircle(_scribbleX, _scribbleY, _scribbleWidth,
         * _scribbleHeight, _scribblePic);
         * o.setPhysics(_scribbleDensity, _scribbleElasticity,
         * _scribbleFriction);
         * if (_scribbleMoveable)
         * o.makeMoveable();
         * }
         * }
         * // is this a finger drag?
         * else if (event.getAction() == TouchEvent.ACTION_MOVE) {
         * if (_scribbleDown) {
         * // figure out if we're far enough away from the last object to
         * // warrant drawing something new
         * float newX = event.getX();
         * float newY = event.getY();
         * float xDist = _scribbleX - newX;
         * float yDist = _scribbleY - newY;
         * float hSquare = xDist * xDist + yDist * yDist;
         * // NB: we're using euclidian distance, but we're comparing
         * // squares instead of square roots
         * if (hSquare > (2.5f * 2.5f)) {
         * _scribbleX = newX;
         * _scribbleY = newY;
         * o = makeAsCircle(_scribbleX, _scribbleY, _scribbleWidth,
         * _scribbleHeight, _scribblePic);
         * o.setPhysics(_scribbleDensity, _scribbleElasticity,
         * _scribbleFriction);
         * if (_scribbleMoveable)
         * o.makeMoveable();
         * }
         * }
         * }
         * // is this a release event?
         * else if (event.getAction() == TouchEvent.ACTION_UP) {
         * if (_scribbleDown) {
         * // reset scribble vars
         * _scribbleDown = false;
         * _scribbleX = -1000;
         * _scribbleY = -1000;
         * }
         * }
         * // if we drew something, then we will set a timer so that it
         * disappears
         * // in a few seconds
         * if (o != null) {
         * // standard hack: make a final of the object, so we can reference it
         * // in the callback
         * final Obstacle o2 = o;
         * // set up a timer to run in a few seconds
         * TimerHandler th = new TimerHandler(_scribbleTime, false, new
         * ITimerCallback()
         * {
         * 
         * @Override
         * public void onTimePassed(TimerHandler pTimerHandler)
         * {
         * o2._sprite.setVisible(false);
         * o2._physBody.setActive(false);
         * }
         * });
         * Level._current.registerUpdateHandler(th);
         * }
         */
    }

    /*
     * AUDIO SUPPORT
     */

    /**
     * a sound to play when the obstacle is hit by a hero
     */
    private Sound   _collideSound;

    /**
     * how long to delay between attempts to play the collide sound
     */
    private float   _collideSoundDelay;

    /**
     * Time of last collision sound
     */
    private float   _lastCollideSoundTime;

    /**
     * a sound to play when the obstacle is touched
     */
    protected Sound _touchSound;

    /**
     * Indicate that when the hero collides with this obstacle, we should make a
     * sound
     * 
     * @param sound
     *            The name of the sound file to play
     * @param delay
     *            How long to wait before playing the sound again
     */
    public void setCollideSound(String sound, float delay)
    {
        _collideSound = Media.getSound(sound);
        _collideSoundDelay = delay;
    }

    /**
     * Internal method for playing a sound when a hero collides with this
     * obstacle
     */
    void playCollideSound()
    {
        if (_collideSound == null)
            return;

        // Make sure we have waited long enough
        // TODO:
        // float now = ALE._self.getEngine().getSecondsElapsedTotal();
        // if (now < _lastCollideSoundTime + _collideSoundDelay)
        // return;
        // _lastCollideSoundTime = now;
        // _collideSound.play();
    }

    /**
     * Indicate that when the player touches this obstacle, we should make a
     * sound
     * 
     * @param sound
     *            The name of the sound file to play
     */
    public void setTouchSound(String sound)
    {
        // save the sound
        _touchSound = Media.getSound(sound);

        // turn on the touch handler
        // TODO:
        // Level._current.registerTouchArea(_sprite);
        // Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        // Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
    }

    /*
     * TOUCH-TO-THROW
     */

    /**
     * Track if touching this obstacle causes the hero to throw a projectile
     */
    boolean _isTouchToThrow = false;

    /**
     * Indicate that touching this obstacle will cause the hero to throw a
     * projectile
     */
    public void setTouchToThrow()
    {
        _isTouchToThrow = true;
        // turn on the touch handler
        // TODO:
        // Level._current.registerTouchArea(_sprite);
        // /Level._current.setTouchAreaBindingOnActionDownEnabled(true);
        // Level._current.setTouchAreaBindingOnActionMoveEnabled(true);
    }

    /*
     * COLLISION SUPPORT
     */

    /**
     * Called when this Obstacle is the dominant obstacle in a collision
     * 
     * Note: This Obstacle is /never/ the dominant obstacle in a collision,
     * since it is #6 or #7
     * 
     * @param other
     *            The other entity involved in this collision
     */
    void onCollide(PhysicsSprite other)
    {
    }

    /*
     * INTERNAL FUNCTIONS
     */
    /**
     * When the scene is touched, we use this to figure out if we need to move a
     * PokeObject
     * 
     * @param scene
     *            The scene that was touched
     * @param event
     *            A description of the touch event
     * @returns true if we handled the event
     */
    static boolean handleSceneTouch(/* final Scene scene, final TouchEvent event */)
    {
        /*
         * // only do this if we have a valid scene, valid _physics, a valid
         * // currentSprite, and a down press
         * if (Level._physics != null) {
         * switch (event.getAction()) {
         * case TouchEvent.ACTION_DOWN:
         * if (_currentPokeSprite != null) {
         * if (Configuration.isVibrationOn())
         * ALE._self.getEngine().vibrate(100);
         * // move the object
         * _pokeVector.set(event.getX() /
         * PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, event.getY()
         * / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
         * _currentPokeSprite._physBody.setTransform(_pokeVector,
         * _currentPokeSprite._physBody.getAngle());
         * _currentPokeSprite = null;
         * return true;
         * }
         * }
         * 
         * // if we are here, there wasn't an ACTION_DOWN that we processed for
         * // an oustanding Poke object, so we should see if this is a scribble
         * // event
         * if (Level._scribbleMode) {
         * doScribble(event);
         * return true;
         * }
         * }
         * return PhysicsSprite.handleSceneTouch(scene, event);
         */
        return false;
    }

    /**
     * Whenever an Obstacle is touched, this code runs automatically.
     * 
     * @param e
     *            Nature of the touch (down, up, etc)
     * @param x
     *            X position of the touch
     * @param y
     *            Y position of the touch
     */
    // @Override
    protected boolean onSpriteAreaTouched(/* TouchEvent e, float x, float y */)
    {
        /*
         * // on a down press of a live-edit object, just run the editor and
         * return
         * if (e.isActionDown() && isLiveEdit) {
         * ALE.launchLiveEditor(this);
         * return true;
         * }
         * 
         * // do we need to make a sound?
         * if (e.isActionDown() && _touchSound != null)
         * _touchSound.play();
         * 
         * // handle touch-to-shoot
         * if (e.isActionDown() && _isTouchToThrow) {
         * if (Level._lastHero != null)
         * Projectile.throwFixed(Level._lastHero._sprite.getX(),
         * Level._lastHero._sprite.getY());
         * return true;
         * }
         * 
         * // if the object is a poke object, things are a bit more complicated
         * if (_isPoke) {
         * // only act on depress, not on release or drag
         * if (e.getAction() == MotionEvent.ACTION_DOWN) {
         * if (Configuration.isVibrationOn())
         * ALE._self.getEngine().vibrate(100);
         * float time = ALE._self.getEngine().getSecondsElapsedTotal();
         * if (this == Obstacle._currentPokeSprite) {
         * // double touch
         * if ((time - _lastPokeTime) < _pokeDeleteThresh) {
         * // hide _sprite, disable _physics, make not touchable
         * _physBody.setActive(false);
         * Level._current.unregisterTouchArea(_sprite);
         * _sprite.setVisible(false);
         * }
         * // repeat single-touch
         * else {
         * _lastPokeTime = time;
         * }
         * }
         * // new single touch
         * else {
         * // record the active _sprite
         * _currentPokeSprite = this;
         * _lastPokeTime = time;
         * }
         * }
         * return true;
         * }
         * 
         * // if this is a touch trigger, call the touchtrigger code
         * if (_isTouchTrigger && e.isActionDown()) {
         * if ((_touchTriggerActivation1 <= Score._goodiesCollected1)
         * && (_touchTriggerActivation2 <= Score._goodiesCollected2)
         * && (_touchTriggerActivation3 <= Score._goodiesCollected3)
         * && (_touchTriggerActivation4 <= Score._goodiesCollected4))
         * {
         * remove(false);
         * ALE._self.onTouchTrigger(_touchTriggerID, MenuManager._currLevel,
         * this);
         * return true;
         * }
         * }
         * return super.onSpriteAreaTouched(e, x, y);
         */
        return false;
    }

    /*
     * LOGICALLY CONNECTING OBSTACLES
     */

    /**
     * Holds the peer obstacle, as set by the programmer
     */
    Obstacle _peer;

    /**
     * Store an obstacle that is the peer of this obstacle. This is useful when
     * we want to allow one obstacle's trigger
     * to cause another obstacle to be changed.
     * 
     * @param peer
     *            The "other" obstacle
     */
    public void setParent(Obstacle peer)
    {
        _peer = peer;
    }

    /**
     * Return an obstacle's peer, as was saved earlier
     * 
     * @return The peer obstacle
     */
    public Obstacle getPeer()
    {
        return _peer;
    }

    /*
     * PHYSICALLY CONNECTING OBSTACLES AND HEROES
     */
    boolean isSticky;

    /**
     * Make this obstacle sticky, so that a hero will stick to it
     */
    public void setSticky()
    {
        isSticky = true;
    }
}