package edu.lehigh.cse.lol;

// TODO: clean up comments

// TODO: scribble time of -1 should leave things on the screen forever...

// TODO: we should really make this so that there are simply callbacks and far fewer static fields

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

/**
 * Obstacles are entities that change the hero's velocity upon a collision
 * 
 * There are many flavors of obstacles. They can have a physics shape that is
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
    protected Obstacle(float width, float height, String imgName)
    {
        super(imgName, SpriteId.OBSTACLE, width, height);
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
        Obstacle o = new Obstacle(width, height, imgName);
        o.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        Level._currLevel._sprites.add(o);
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
        float radius = (width > height) ? width : height;
        Obstacle o = new Obstacle(width, height, imgName);
        o.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius / 2);
        Level._currLevel._sprites.add(o);
        return o;
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
    boolean _isHeroCollideTrigger        = false;

    /**
     * Hero triggers can require certain Goodie counts in order to run
     */
    int[]   _heroTriggerActivation       = new int[4];

    /**
     * An ID for each hero trigger object, in case it's useful
     */
    int     _heroTriggerID;

    /**
     * Track if this is a "trigger" object that causes special code to run upon
     * any collision with an enemy
     */
    boolean _isEnemyCollideTrigger       = false;

    /**
     * Enemy triggers can require certain Goodie counts in order to run
     */
    int[]   _enemyTriggerActivation      = new int[4];

    /**
     * An ID for each enemy trigger object, in case it's useful
     */
    int     _enemyTriggerID;

    /**
     * How long to wait before running trigger code.
     */
    float   _enemyCollideTriggerDelay    = 0;

    /**
     * Track if this is a "trigger" object that causes special code to run upon
     * any collision with a projectile
     */
    boolean _isProjectileCollideTrigger  = false;

    /**
     * Projectile triggers can require certain Goodie counts in order to run
     */
    int[]   _projectileTriggerActivation = new int[4];

    /**
     * An ID for each projectile trigger object, in case it's useful
     */
    int     _projectileTriggerID;

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
        _heroTriggerActivation[0] = activationGoodies1;
        _heroTriggerActivation[1] = activationGoodies2;
        _heroTriggerActivation[2] = activationGoodies3;
        _heroTriggerActivation[3] = activationGoodies4;
        setCollisionEffect(false);
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
        _enemyTriggerActivation[0] = activationGoodies1;
        _enemyTriggerActivation[1] = activationGoodies2;
        _enemyTriggerActivation[2] = activationGoodies3;
        _enemyTriggerActivation[3] = activationGoodies4;
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
        _projectileTriggerActivation[0] = activationGoodies1;
        _projectileTriggerActivation[1] = activationGoodies2;
        _projectileTriggerActivation[2] = activationGoodies3;
        _projectileTriggerActivation[3] = activationGoodies4;
    }

    /*
     * SCRIBBLE SUPPORT
     * 
     * TODO: refactor into Level, use a callback?
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
    static float           _scribbleWidth;

    /**
     * Height of the picture being drawn via scribbling
     */
    static float           _scribbleHeight;

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
    public static void setScribbleOn(String imgName, float duration, float width, float height, float density,
            float elasticity, float friction, boolean moveable)
    {
        _scribbleTime = duration;
        _scribbleWidth = width;
        _scribbleHeight = height;

        _scribbleDensity = density;
        _scribbleElasticity = elasticity;
        _scribbleFriction = friction;

        // turn on scribble mode, reset scribble status vars
        Level._currLevel._scribbleMode = true;
        _scribbleDown = false;
        _scribbleX = -1000;
        _scribbleY = -1000;
        _scribbleMoveable = moveable;
        // register the scribble picture
        _scribblePic = imgName;
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
    static void doScribbleDown(float x, float y)
    {
        // remember if we made an obstacle
        Obstacle o = null;
        // is this an initial press to start scribbling?
        if (!_scribbleDown) {
            // turn on scribbling, draw an obstacle
            _scribbleDown = true;
            _scribbleX = x;
            _scribbleY = y;
            Gdx.app.log("scrib", "making");
            o = makeAsCircle(_scribbleX - _scribbleWidth / 2, _scribbleY - _scribbleHeight / 2, _scribbleWidth,
                    _scribbleHeight, _scribblePic);
            o.setPhysics(_scribbleDensity, _scribbleElasticity, _scribbleFriction);
            if (_scribbleMoveable)
                o._physBody.setType(BodyType.DynamicBody);
        }
        // if we drew something, then we will set a timer so that it disappears
        // in a few seconds
        if (o != null) {
            final Obstacle o2 = o;
            Timer.schedule(new Task()
            {
                @Override
                public void run()
                {
                    o2._visible = false;
                    o2._physBody.setActive(false);
                }
            }, _scribbleTime);
        }
    }

    // TODO: lots of redundancy with doScribbleDown...
    static void doScribbleDrag(float x, float y)
    {
        // remember if we made an obstacle
        Obstacle o = null;
        if (_scribbleDown) {
            // figure out if we're far enough away from the last object to
            // warrant drawing something new
            float newX = x;
            float newY = y;
            float xDist = _scribbleX - newX;
            float yDist = _scribbleY - newY;
            float hSquare = xDist * xDist + yDist * yDist;
            // NB: we're using euclidian distance, but we're comparing
            // squares instead of square roots
            if (hSquare > (2.5f * 2.5f)) {
                _scribbleX = newX;
                _scribbleY = newY;
                o = makeAsCircle(_scribbleX - _scribbleWidth / 2, _scribbleY - _scribbleHeight / 2, _scribbleWidth,
                        _scribbleHeight, _scribblePic);
                o.setPhysics(_scribbleDensity, _scribbleElasticity, _scribbleFriction);
                if (_scribbleMoveable)
                    o._physBody.setType(BodyType.DynamicBody);
            }
        }

        // if we drew something, then we will set a timer so that it disappears
        // in a few seconds
        if (o != null) {
            // standard hack: make a final of the object, so we can reference it
            // in the callback
            final Obstacle o2 = o;
            Timer.schedule(new Task()
            {
                @Override
                public void run()
                {
                    o2._visible = false;
                    o2._physBody.setActive(false);
                }
            }, _scribbleTime);
        }
    }

    static void doScribbleUp()
    {
        if (_scribbleDown) {
            // reset scribble vars
            _scribbleDown = false;
            _scribbleX = -1000;
            _scribbleY = -1000;
        }
    }

    /*
     * AUDIO SUPPORT
     */

    /**
     * a sound to play when the obstacle is hit by a hero
     */
    private Sound _collideSound;

    /**
     * how long to delay (in nanoseconds) between attempts to play the collide sound
     */
    private long  _collideSoundDelay;

    /**
     * Time of last collision sound
     */
    private long  _lastCollideSoundTime;

    /**
     * Indicate that when the hero collides with this obstacle, we should make a
     * sound
     * 
     * @param sound
     *            The name of the sound file to play
     * @param delay
     *            How long to wait before playing the sound again, in milliseconds
     */
    public void setCollideSound(String sound, long delay)
    {
        _collideSound = Media.getSound(sound);
        _collideSoundDelay = delay * 1000000;
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
        long now = System.nanoTime();
        if (now < _lastCollideSoundTime + _collideSoundDelay)
            return;
        _lastCollideSoundTime = now;
        _collideSound.play();
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
    void onCollide(PhysicsSprite other, Contact contact)
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
         * 
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
}