/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Projectiles are entities that can be thrown from the hero's location in order
 * to remove enemies There are three main parts to the Projectile subsystem: -
 * The Projectile type is a PhysicsSprite that flies across the screen. - The
 * Projectile pool is how a level is set to have projectiles. Configuring the
 * pool ensures that there are projectiles that can be thrown - The throwing
 * mechanism is how projectiles are put onto the screen and given velocity.
 */
public class Projectile extends PhysicsSprite {
    /**
     * The velocity of a projectile when it is thrown
     */
    private static final Vector2 _velocity = new Vector2();

    /**
     * When throwing, we start from the top left corner of the thrower, and then
     * use this to determine the initial x and y position of the projectile
     */
    private static final Vector2 _offset = new Vector2();

    /**
     * We have to be careful in side-scrollers, or else projectiles can continue
     * traveling off-screen forever. This field lets us cap the distance away
     * from the hero that a projectile can travel before we make it disappear.
     */
    private static float _range;

    /**
     * This is the initial point of the throw
     */
    private final Vector2 _rangeFrom = new Vector2();

    /**
     * How much _damage does this projectile do?
     */
    static int _strength;

    /**
     * A dampening factor to apply to projectiles thrown via Vector
     */
    private static float _vectorDamp;

    /**
     * Indicates that projectiles should be sensors
     */
    private static boolean _sensorProjectiles;

    /**
     * Indicates that vector projectiles should have a fixed velocity
     */
    private static boolean _enableFixedVectorVelocity;

    /**
     * The magnitude of the velocity for vector projectiles thrown with a fixed
     * velocity
     */
    private static float _fixedVectorVelocity;

    /**
     * Indicate that projectiles should face in the direction they are initially
     * thrown
     */
    private static boolean _rotateVectorThrow;

    /**
     * Internal method to create a projectile. Projectiles have an underlying
     * circle as their _physics body
     * 
     * @param x initial x position of the projectile
     * @param y initial y position of the projectile
     * @param width width of the projectile
     * @param height height of the projectile
     * @param ttr animatable image to display as the projectile
     */
    private Projectile(float width, float height, String imgName, float x, float y, int zIndex,
            boolean isCircle) {
        super(imgName, SpriteId.PROJECTILE, width, height);
        if (isCircle) {
            float radius = (width > height) ? width : height;
            setCirclePhysics(0, 0, 0, BodyType.DynamicBody, true, x, y, radius / 2);
        } else {
            setBoxPhysics(0, 0, 0, BodyType.DynamicBody, true, x, y);
        }
        _physBody.setGravityScale(0);
        setCollisionEffect(false);
        disableRotation();
        Level._currLevel.addSprite(this, zIndex);
    }

    /**
     * Specify a limit on how far away from the Hero a projectile can go.
     * Without this, projectiles could keep on traveling forever.
     * 
     * @param x Maximum x distance from the hero that a projectile can travel
     * @param y Maximum y distance from the hero that a projectile can travel
     */
    public static void setRange(float distance) {
        _range = distance;
    }

    /**
     * Indicate that projectiles should feel the effects of gravity. Otherwise,
     * they will be (more or less) immune to gravitational forces.
     */
    public static void setProjectileGravityOn() {
        for (Projectile p : _pool)
            p._physBody.setGravityScale(1);
    }

    static boolean _randomizeImages;
    
    /**
     * Specify the number of cells from which to choose a random projectile
     * image
     * 
     * @param range This number indicates the number of cells
     */
    public static void setImageSource(String imgName) {
        for (Projectile p : _pool)
            p._animator.updateImage(imgName);
        _randomizeImages = true;
        
    }

    /**
     * The "vector projectile" mechanism might lead to the projectiles moving
     * too fast. This will cause the speed to be multiplied by a factor
     * 
     * @param factor The value to multiply against the projectile speed.
     */
    public static void setProjectileVectorDampeningFactor(float factor) {
        _vectorDamp = factor;
    }

    /**
     * Indicate that all projectiles should participate in collisions
     */
    public static void enableCollisionsForProjectiles() {
        _sensorProjectiles = false;
    }

    /**
     * Indicate that projectiles thrown with the "vector" mechanism should have
     * a fixed velocity
     * 
     * @param velocity The magnitude of the velocity for projectiles
     */
    public static void setFixedVectorThrowVelocity(float velocity) {
        _enableFixedVectorVelocity = true;
        _fixedVectorVelocity = velocity;
    }

    /**
     * Indicate that projectiles thrown via the "vector" mechanism should be
     * rotated to face in their initial direction
     */
    public static void setRotateVectorThrow() {
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
    private static int _poolSize;

    /**
     * Index of next available projectile in the pool
     */
    private static int _nextIndex;

    /**
     * For limiting the number of projectiles that can be thrown
     */
    static int _projectilesRemaining;

    static boolean _disappearOnCollide;

    void setCollisionOk() {
        _disappearOnCollide = false;
    }

    /**
     * Describe the behavior of projectiless in a scene. You must call this if
     * you intend to use projectiles in your scene
     * 
     * @param size number of projectiles that can be thrown at once
     * @param width width of a projectile
     * @param height height of a projectile
     * @param imgName name image to use for projectiles
     * @param velocityX x velocity of projectiles
     * @param velocityY y velocity of projectiles
     * @param offsetX specifies the x distance between the origin of the
     *            projectile and the origin of the hero throwing the projectile
     * @param offsetY specifies the y distance between the origin of the
     *            projectile and the origin of the hero throwing the projectile
     * @param mStrength specifies the amount of _damage that a projectile does
     *            to an enemy
     */
    public static void configure(int size, float width, float height, String imgName,
            float velocityX, float velocityY, float offsetX, float offsetY, int strength,
            int zIndex, boolean isCircle) {
        // set up the pool
        _pool = new Projectile[size];
        // don't draw all projectiles in same place...
        for (int i = 0; i < size; ++i) {
            _pool[i] = new Projectile(width, height, imgName, -100 - i * width, -100 - i * height,
                    zIndex, isCircle);
            _pool[i]._visible = false;
            _pool[i]._physBody.setBullet(true);
            _pool[i]._physBody.setActive(false);
        }
        _nextIndex = 0;
        _poolSize = size;
        // record vars that describe how the projectile behaves
        _strength = strength;
        _velocity.set(velocityX, velocityY);
        _offset.set(offsetX, offsetY);
        _range = 1000;
        _throwSound = null;
        _projectileDisappearSound = null;
        _projectilesRemaining = -1;
        _sensorProjectiles = true;
        _disappearOnCollide = true;
    }

    /**
     * Set a limit on the total number of projectiles that can be thrown
     * 
     * @param number How many projectiles are available
     */
    public static void setNumberOfProjectiles(int number) {
        _projectilesRemaining = number;
    }

    /*
     * AUDIO SUPPORT
     */

    /**
     * Sound to play when projectiles are fired
     */
    static Sound _throwSound;

    /**
     * The sound to play when a projectile disappears
     */
    private static Sound _projectileDisappearSound;

    /**
     * Specify a sound to play when the projectile is thrown
     * 
     * @param soundName Name of the sound file to play
     */
    public static void setThrowSound(String soundName) {
        _throwSound = Media.getSound(soundName);
    }

    /**
     * Specify the sound to play when a projectile disappears
     * 
     * @param soundName the name of the sound file to play
     */
    public static void setProjectileDisappearSound(String soundName) {
        _projectileDisappearSound = Media.getSound(soundName);
    }

    /*
     * ANIMATION SUPPORT
     */

    /**
     * Specify how projectiles should be animated
     * 
     * @param frames a listing of the order in which frames of the underlying
     *            image should be displayed
     * @param durations time to display each frame
     */
    public static void setAnimation(Animation a) {
        for (Projectile p : _pool)
            p.setDefaultAnimation(a);
    }

    /*
     * COLLISION SUPPORT
     */

    /**
     * Standard collision detection routine Since we have a careful ordering
     * scheme, this only triggers on hitting an obstacle, which makes the
     * projectile disappear, or on hitting a projectile, which is a bit funny
     * because one of the two projectiles will live.
     * 
     * @param other The other entity involved in the collision
     */
    protected void onCollide(PhysicsSprite other, Contact contact) {
        // if this is an obstacle, check if it is a projectile trigger, and if
        // so, do the callback
        if (other._psType == SpriteId.OBSTACLE) {
            Obstacle o = (Obstacle)other;
            if (o._projectileCollision != null) {
                o._projectileCollision.go(this, contact);
                // return... don't remove the projectile
                return;
            }
        }
        if (other._psType == SpriteId.PROJECTILE) {
            if (!_disappearOnCollide)
                return;
        }
        // only disappear if other is not a sensor
        if (other._physBody.getFixtureList().get(0).isSensor())
            return;
        remove(false);
    }

    /*
     * MECHANICS FOR THROWING PROJECTILES
     */

    /**
     * Throw a projectile
     * 
     * @param xx x coordinate of the top left corner of the thrower
     * @param yy y coordinate of the top left corner of the thrower
     */
    static void throwFixed(float xx, float yy, Hero h) {
        // have we reached our limit?
        if (_projectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (_projectilesRemaining != -1)
            _projectilesRemaining--;

        // is there an available projectile?
        if (_pool[_nextIndex]._visible)
            return;
        // calculate offset for starting position of projectile
        // get the next projectile
        Projectile b = _pool[_nextIndex];
        b._rangeFrom.x = xx + _offset.x;
        b._rangeFrom.y = yy + _offset.y;

        // reset its sensor
        b.setCollisionEffect(!_sensorProjectiles);

        // set its _sprite
        if (_randomizeImages)
            b._animator.pickRandomIndex();

        _nextIndex = (_nextIndex + 1) % _poolSize;

        // put the projectile on the screen and place it in the _physics world
        b._physBody.setActive(true);
        b._physBody.setTransform(b._rangeFrom, 0);

        // give the projectile velocity
        b.updateVelocity(_velocity.x, _velocity.y);

        // make the projectile visible
        b._visible = true;
        if (_throwSound != null)
            _throwSound.play();
        b._disappearSound = _projectileDisappearSound;

        // now animate the hero to do the throw:
        h.doThrowAnimation();
    }

    /**
     * Throw a projectile in a specific direction, instead of the default
     * direction
     * 
     * @param heroX x coordinate of the top left corner of the thrower
     * @param heroY y coordinate of the top left corner of the thrower
     * @param toX x coordinate of the point at which to throw
     * @param toY y coordinate of the point at which to throw
     */
    static void throwAt(float heroX, float heroY, float toX, float toY, Hero h) {
        // have we reached our limit?
        if (_projectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (_projectilesRemaining != -1)
            _projectilesRemaining--;

        // is there an available projectile?
        if (_pool[_nextIndex]._visible)
            return;
        // get the next projectile
        Projectile b = _pool[_nextIndex];
        // calculate offset for starting position of projectile
        b._rangeFrom.x = heroX + _offset.x;
        b._rangeFrom.y = heroY + _offset.y;

        // reset its sensor
        b.setCollisionEffect(!_sensorProjectiles);

        _nextIndex = (_nextIndex + 1) % _poolSize;
        // put the projectile on the screen and place it in the _physics world
        b._physBody.setActive(true);
        b._physBody.setTransform(b._rangeFrom, 0);

        // give the projectile velocity
        if (_enableFixedVectorVelocity) {
            // compute a unit vector
            float dX = toX - heroX - _offset.x;
            float dY = toY - heroY - _offset.y;
            float hypotenuse = (float)Math.sqrt(dX * dX + dY * dY);
            float tmpX = dX / hypotenuse;
            float tmpY = dY / hypotenuse;
            // multiply by fixed velocity
            tmpX *= _fixedVectorVelocity;
            tmpY *= _fixedVectorVelocity;
            b.updateVelocity(tmpX, tmpY);
        } else {
            float dX = toX - heroX - _offset.x;
            float dY = toY - heroY - _offset.y;
            // compute absolute vector, multiply by dampening factor
            float tmpX = dX * _vectorDamp;
            float tmpY = dY * _vectorDamp;
            b.updateVelocity(tmpX, tmpY);
        }

        // rotate the projectile
        if (_rotateVectorThrow) {
            double angle = Math.atan2(toY - heroY - _offset.y, toX - heroX - _offset.x)
                    - Math.atan2(-1, 0);
            b._physBody.setTransform(b._physBody.getPosition(), (float)angle);
        }

        // make the projectile visible
        b._visible = true;
        if (_throwSound != null)
            _throwSound.play();
        b._disappearSound = _projectileDisappearSound;

        // now animate the hero to do the throw:
        h.doThrowAnimation();
    }

    /*
     * INTERNAL METHODS
     */

    /**
     * Internal method for negating gravity in side scrollers and for enforcing
     * the projectile range
     */
    @Override
    public void render(SpriteBatch sb, float delta) {
        // eliminate the projectile quietly if it has traveled too far
        float dx = Math.abs(_physBody.getPosition().x - _rangeFrom.x);
        float dy = Math.abs(_physBody.getPosition().y - _rangeFrom.y);
        if (dx * dx + dy * dy > _range * _range) {
            remove(true);
            _physBody.setActive(false);
            return;
        }
        super.render(sb, delta);
    }
}
