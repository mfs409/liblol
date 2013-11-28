package edu.lehigh.cse.ale;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

/**
 * Projectiles are entities that can be thrown from the hero's location in order to remove enemies
 * 
 * There are three main parts to the Projectile subsystem:
 * 
 * - The Projectile type is a PhysicsSprite that flies across the screen.
 * 
 * - The Projectile pool is how a level is set to have projectiles. Configuring the pool ensures that there are
 * projectiles that can be thrown
 * 
 * - The throwing mechanism is how projectiles are put onto the screen and given velocity.
 */
public class Projectile extends PhysicsSprite
{
    /*
     * BASIC SUPPORT
     */

    /**
     * The force that is applied to a projectile to negate gravity
     */
    private static final Vector2 _negGravity = new Vector2();

    /**
     * The velocity of a projectile when it is thrown
     */
    private static final Vector2 _velocity   = new Vector2();

    /**
     * When throwing, we start from the top left corner of the thrower, and then use this to determine the initial x and
     * y position of the projectile
     */
    private static final Vector2 _offset     = new Vector2();

    /**
     * We have to be careful in side-scrollers, or else projectiles can continue traveling off-screen forever. This
     * field lets us cap the distance away from the hero that a projectile can travel before we make it disappear.
     */
    private static final Vector2 _range      = new Vector2();

    /**
     * The initial position of a projectile.
     */
    private static final Vector2 _position   = new Vector2();

    /**
     * A spare vector for computation
     */
    private static final Vector2 _tmp        = new Vector2();

    /**
     * How much _damage does this projectile do?
     */
    static int                   _strength;

    /**
     * If this is > 0, it specifies the range of cells that can be used as the projectile image
     */
    private static int           _randomizeProjectileSprites;

    /**
     * Indicate that projectile gravity is enabled (default is false)
     */
    private static boolean       _gravityEnabled;

    /**
     * A dampening factor to apply to projectiles thrown via Vector
     */
    private static float         _vectorDamp;

    /**
     * Indicates that projectiles should be sensors
     */
    private static boolean       _sensorProjectiles;

    /**
     * Indicates that vector projectiles should have a fixed velocity
     */
    private static boolean       _enableFixedVectorVelocity;

    /**
     * The magnitude of the velocity for vector projectiles thrown with a fixed velocity
     */
    private static float         _fixedVectorVelocity;

    /**
     * Indicate that projectiles should face in the direction they are initially thrown
     */
    private static boolean       _rotateVectorThrow;

    /**
     * Internal method to create a projectile. Projectiles have an underlying circle as their _physics body
     * 
     * @param x
     *            initial x position of the projectile
     * @param y
     *            initial y position of the projectile
     * @param width
     *            width of the projectile
     * @param height
     *            height of the projectile
     * @param ttr
     *            animatable image to display as the projectile
     */
    private Projectile(float x, float y, float width, float height, TextureRegion ttr)
    {
        super(ttr, SpriteId.PROJECTILE, width, height);
        // TODO: // setCirclePhysics(0, 0, 0, BodyType.DynamicBody, true);
        setCollisionEffect(false);
        disableRotation();
    }

    /**
     * Specify a limit on how far away from the Hero a projectile can go. Without this, projectiles could keep on
     * traveling forever.
     * 
     * @param x
     *            Maximum x distance from the hero that a projectile can travel
     * @param y
     *            Maximum y distance from the hero that a projectile can travel
     */
    public static void setRange(float x, float y)
    {
        _range.x = x;
        _range.y = y;
    }

    /**
     * Indicate that projectiles should feel the effects of gravity. Otherwise, they will be (more or less) immune to
     * gravitational forces.
     */
    public static void setProjectileGravityOn()
    {
        _gravityEnabled = true;
    }

    /**
     * Specify the number of cells from which to choose a random projectile image
     * 
     * @param range
     *            This number indicates the number of cells
     */
    public static void setImageRange(int range)
    {
        _randomizeProjectileSprites = range;
    }

    /**
     * The "vector projectile" mechanism might lead to the projectiles moving too fast. This will cause the speed to be
     * multiplied by a factor
     * 
     * @param factor
     *            The value to multiply against the projectile speed.
     */
    public static void setProjectileVectorDampeningFactor(float factor)
    {
        _vectorDamp = factor;
    }

    /**
     * Indicate that all projectiles should participate in collisions
     */
    public static void enableCollisionsForProjectiles()
    {
        _sensorProjectiles = false;
    }

    /**
     * Indicate that projectiles thrown with the "vector" mechanism should have a fixed velocity
     * 
     * @param velocity
     *            The magnitude of the velocity for projectiles
     */
    public static void setFixedVectorThrowVelocity(float velocity)
    {
        _enableFixedVectorVelocity = true;
        _fixedVectorVelocity = velocity;
    }

    /**
     * Indicate that projectiles thrown via the "vector" mechanism should be rotated to face in their initial direction
     */
    public static void setRotateVectorThrow()
    {
        _rotateVectorThrow = true;
    }

    /*
     * PROJECTILE POOL SUPPORT
     */

    /**
     * A collection of all the available projectiles
     */
    private static Projectile _pool[];

    /**
     * The number of projectiles in the pool
     */
    private static int        _poolSize;

    /**
     * Index of next available projectile in the pool
     */
    private static int        _nextIndex;

    /**
     * For limiting the number of projectiles that can be thrown
     */
    static int                _projectilesRemaining;

    /**
     * Describe the behavior of projectiless in a scene.
     * 
     * You must call this if you intend to use projectiles in your scene
     * 
     * @param size
     *            number of projectiles that can be thrown at once
     * 
     * @param width
     *            width of a projectile
     * 
     * @param height
     *            height of a projectile
     * 
     * @param imgName
     *            name image to use for projectiles
     * 
     * @param velocityX
     *            x velocity of projectiles
     * 
     * @param velocityY
     *            y velocity of projectiles
     * 
     * @param offsetX
     *            specifies the x distance between the origin of the projectile and the origin of the hero throwing the
     *            projectile
     * @param offsetY
     *            specifies the y distance between the origin of the projectile and the origin of the hero throwing the
     *            projectile
     * @param _strength
     *            specifies the amount of _damage that a projectile does to an enemy
     *//*
    public static void configure(int size, float width, float height, String imgName, float velocityX, float velocityY,
            float offsetX, float offsetY, int strength)
    {
        // configure the image to use
        TiledTextureRegion ttr = Media.getImage(imgName);
        // set up the pool
        _pool = new Projectile[size];
        for (int i = 0; i < size; ++i) {
            _pool[i] = new Projectile(-100, -100, width, height, ttr);
            _pool[i]._sprite.setVisible(false);
            _pool[i]._physBody.setBullet(true);
            _pool[i]._physBody.setActive(false);
            Level._current.attachChild(_pool[i]._sprite);
        }
        _nextIndex = 0;
        _poolSize = size;
        // record vars that describe how the projectile behaves
        _strength = strength;
        _velocity.x = velocityX;
        _velocity.y = velocityY;
        _offset.x = offsetX;
        _offset.y = offsetY;
        _negGravity.x = -Level._initXGravity;
        _negGravity.y = -Level._initYGravity;
        _range.x = 1000;
        _range.y = 1000;
        // zero out animation
        _animationCells = null;
        _animationDurations = null;
        _throwSound = null;
        _projectileDisappearSound = null;
        _gravityEnabled = false;
        _projectilesRemaining = -1;
        _randomizeProjectileSprites = 0;
        _sensorProjectiles = true;
    }

    /**
     * Set a limit on the total number of projectiles that can be thrown
     * 
     * @param number
     *            How many projectiles are available
     */
    public static void setNumberOfProjectiles(int number)
    {
        _projectilesRemaining = number;
    }

    /*
     * AUDIO SUPPORT
     */

    /**
     * Sound to play when projectiles are fired
     */
    static Sound         _throwSound;

    /**
     * The sound to play when a projectile disappears
     */
    private static Sound _projectileDisappearSound;

    /**
     * Specify a sound to play when the projectile is thrown
     * 
     * @param soundName
     *            Name of the sound file to play
     */
    public static void setThrowSound(String soundName)
    {
        Sound s = Media.getSound(soundName);
        _throwSound = s;
    }

    /**
     * Specify the sound to play when a projectile disappears
     * 
     * @param soundName
     *            the name of the sound file to play
     */
    public static void setProjectileDisappearSound(String soundName)
    {
        _projectileDisappearSound = Media.getSound(soundName);
    }

    /*
     * ANIMATION SUPPORT
     */

    /**
     * Order in which to display cells when the projectile is animated
     */
    private static int[]  _animationCells;

    /**
     * Durations during which cells are displayed when the projectile is animated
     */
    private static long[] _animationDurations;

    /**
     * Specify how projectiles should be animated
     * 
     * @param frames
     *            a listing of the order in which frames of the underlying image should be displayed
     * @param durations
     *            time to display each frame
     */
    public static void setAnimation(int[] frames, long[] durations)
    {
        _animationCells = frames;
        _animationDurations = durations;
    }

    /*
     * COLLISION SUPPORT
     */

    /**
     * Standard collision detection routine
     * 
     * Since we have a careful ordering scheme, this only triggers on hitting an obstacle, which makes the projectile
     * disappear, or on hitting a projectile, which is a bit funny because one of the two projectiles will live.
     * 
     * @param other
     *            The other entity involved in the collision
     */
    protected void onCollide(PhysicsSprite other)
    {
        // TODO:
        /*
        // if this is an obstacle, check if it is a projectile trigger, and if so, do the callback
        if (other._psType == SpriteId.OBSTACLE) {
            Obstacle o = (Obstacle) other;
            if (o._isProjectileCollideTrigger 
                    && (o._projectileTriggerActivation1 <= Score._goodiesCollected1)
                    && (o._projectileTriggerActivation2 <= Score._goodiesCollected2)
                    && (o._projectileTriggerActivation3 <= Score._goodiesCollected3)
                    && (o._projectileTriggerActivation4 <= Score._goodiesCollected4)) 
            {
                ALE._self.onProjectileCollideTrigger(o._projectileTriggerID,
                        MenuManager._currLevel, o, this);
                return;
            }
        }
        // only disappear if other is not a sensor
        if (other._physBody.getFixtureList().get(0).isSensor())
            return;
        remove(false);
        */
    }

    /*
     * MECHANICS FOR THROWING PROJECTILES
     */

    /**
     * Throw a projectile
     * 
     * @param xx
     *            x coordinate of the top left corner of the thrower
     * @param yy
     *            y coordinate of the top left corner of the thrower
     *//*
    static void throwFixed(float xx, float yy)
    {
        // have we reached our limit?
        if (_projectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (_projectilesRemaining != -1)
            _projectilesRemaining--;

        // is there an available projectile?
        if (_pool[_nextIndex]._sprite.isVisible())
            return;
        // calculate offset for starting position of projectile
        float x = xx + _offset.x;
        float y = yy + _offset.y;
        // get the next projectile
        Projectile b = _pool[_nextIndex];

        // reset its sensor
        b.setCollisionEffect(!_sensorProjectiles);

        // set its _sprite
        if (Projectile._randomizeProjectileSprites > 0)
            b._sprite.stopAnimation(Util.getRandom(Projectile._randomizeProjectileSprites));

        _nextIndex = (_nextIndex + 1) % _poolSize;

        // put the projectile on the screen and place it in the _physics world
        _position.x = x;
        _position.y = y;
        _position.mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
        b._physBody.setActive(true);
        b._physBody.setTransform(_position, 0);

        // give the projectile velocity
        b.updateVelocity(_velocity);

        // make the projectile visible
        b._sprite.setVisible(true);
        if (_animationCells != null) {
            b._sprite.animate(_animationDurations, _animationCells, true);
        }
        if (_throwSound != null)
            _throwSound.play();
        b._disappearSound = _projectileDisappearSound;

        // now animate the hero to do the throw:
        Level._lastHero.doThrowAnimation();
    }

    /**
     * Throw a projectile in a specific direction, instead of the default direction
     * 
     * @param heroX
     *            x coordinate of the top left corner of the thrower
     * @param heroY
     *            y coordinate of the top left corner of the thrower
     * @param toX
     *            x coordinate of the point at which to throw
     * @param toY
     *            y coordinate of the point at which to throw
     *//*
    static void throwAt(float heroX, float heroY, float toX, float toY)
    {
        // have we reached our limit?
        if (_projectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (_projectilesRemaining != -1)
            _projectilesRemaining--;

        // is there an available projectile?
        if (_pool[_nextIndex]._sprite.isVisible())
            return;
        // calculate offset for starting position of projectile
        float x = heroX + _offset.x;
        float y = heroY + _offset.y;
        // get the next projectile
        Projectile b = _pool[_nextIndex];

        // reset its sensor
        b.setCollisionEffect(!_sensorProjectiles);

        // set its _sprite
        if (Projectile._randomizeProjectileSprites > 0) {
            b._sprite.stopAnimation(Util.getRandom(Projectile._randomizeProjectileSprites));
        }

        _nextIndex = (_nextIndex + 1) % _poolSize;
        // put the projectile on the screen and place it in the _physics world
        _position.x = x;
        _position.y = y;
        _position.mul(1 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
        b._physBody.setActive(true);
        b._physBody.setTransform(_position, 0);

        // give the projectile velocity
        if (_enableFixedVectorVelocity) {
            // compute a unit vector
            float dX = toX - heroX - _offset.x;
            float dY = toY - heroY - _offset.y;
            float hypotenuse = (float) Math.sqrt(dX * dX + dY * dY);
            _tmp.x = dX / hypotenuse;
            _tmp.y = dY / hypotenuse;
            // multiply by fixed velocity
            _tmp.x *= _fixedVectorVelocity;
            _tmp.y *= _fixedVectorVelocity;
            b.updateVelocity(_tmp);
        }
        else {
            float dX = toX - heroX - _offset.x;
            float dY = toY - heroY - _offset.y;
            // compute absolute vector, multiply by dampening factor
            _tmp.x = dX * _vectorDamp;
            _tmp.y = dY * _vectorDamp;
            b.updateVelocity(_tmp);
        }

        // rotate the projectile
        if (_rotateVectorThrow) {
            double angle = Math.atan2(toY - heroY - _offset.y, toX - heroX - _offset.x) - Math.atan2(-1, 0);
            b._sprite.setRotation(180 / (3.1415926f) * (float) angle);
        }
        
        // make the projectile visible
        b._sprite.setVisible(true);
        if (_animationCells != null) {
            b._sprite.animate(_animationDurations, _animationCells, true);
        }
        if (_throwSound != null)
            _throwSound.play();
        b._disappearSound = _projectileDisappearSound;

        // now animate the hero to do the throw:
        Level._lastHero.doThrowAnimation();
    }

    /*
     * INTERNAL METHODS
     */

    /**
     * Internal method for negating gravity in side scrollers and for enforcing the projectile range
     *//*
    protected void onSpriteManagedUpdate()
    {
        // eliminate the projectile quietly if it has traveled too far
        Hero h = Level._lastHero;
        if (h != null) {
            if ((Math.abs(h._sprite.getX() - _sprite.getX()) > _range.x)
                    || (Math.abs(h._sprite.getY() - _sprite.getY()) > _range.y)) {
                remove(true);
                _physBody.setActive(false);
                super.onSpriteManagedUpdate();
                return;
            }
        }
        // do we need to negate gravity?
        if (_gravityEnabled) {
            super.onSpriteManagedUpdate();
            return;
        }
        // if we are visible, and if there is gravity, apply negative gravity
        if (_sprite.isVisible()) {
            if (_negGravity.x != 0 || _negGravity.y != 0)
                _physBody.applyForce(_negGravity, _physBody.getWorldCenter());
        }
        super.onSpriteManagedUpdate();
    }
*/
}
