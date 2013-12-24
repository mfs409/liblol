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
    private static final Vector2 sVelocity = new Vector2();

    /**
     * When throwing, we start from the top left corner of the thrower, and then
     * use this to determine the initial x and y position of the projectile
     */
    private static final Vector2 sOffset = new Vector2();

    /**
     * We have to be careful in side-scrollers, or else projectiles can continue
     * traveling off-screen forever. This field lets us cap the distance away
     * from the hero that a projectile can travel before we make it disappear.
     */
    private static float sRange;

    /**
     * This is the initial point of the throw
     */
    private final Vector2 mRangeFrom = new Vector2();

    /**
     * How much damage does this projectile do?
     */
    static int sStrength;

    /**
     * A dampening factor to apply to projectiles thrown via Vector
     */
    private static float sVectorDamp;

    /**
     * Indicates that projectiles should be sensors
     */
    private static boolean sSensorProjectiles;

    /**
     * Indicates that vector projectiles should have a fixed velocity
     */
    private static boolean sEnableFixedVectorVelocity;

    /**
     * The magnitude of the velocity for vector projectiles thrown with a fixed
     * velocity
     */
    private static float sFixedVectorVelocity;

    /**
     * Indicate that projectiles should face in the direction they are initially
     * thrown
     */
    private static boolean sRotateVectorThrow;

    /**
     * A collection of all the available projectiles
     */
    private static Projectile sPool[];

    /**
     * The number of projectiles in the pool
     */
    private static int sPoolSize;

    /**
     * Index of next available projectile in the pool
     */
    private static int sNextIndex;

    /**
     * For limiting the number of projectiles that can be thrown
     */
    static int sProjectilesRemaining;

    private static boolean sDisappearOnCollide;

    private static boolean sRandomizeImages;
    
    /**
     * Sound to play when projectiles are fired
     */
    private static Sound sThrowSound;

    /**
     * The sound to play when a projectile disappears
     */
    private static Sound sProjectileDisappearSound;

    /**
     * Internal method to create a projectile. Projectiles have an underlying
     * circle as their physics body
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
        mBody.setGravityScale(0);
        setCollisionEffect(false);
        disableRotation();
        Level.sCurrent.addSprite(this, zIndex);
    }

    /**
     * Specify a limit on how far away from the Hero a projectile can go.
     * Without this, projectiles could keep on traveling forever.
     * 
     * @param x Maximum x distance from the hero that a projectile can travel
     * @param y Maximum y distance from the hero that a projectile can travel
     */
    public static void setRange(float distance) {
        sRange = distance;
    }

    /**
     * Indicate that projectiles should feel the effects of gravity. Otherwise,
     * they will be (more or less) immune to gravitational forces.
     */
    public static void setProjectileGravityOn() {
        for (Projectile p : sPool)
            p.mBody.setGravityScale(1);
    }

    /**
     * Specify the number of cells from which to choose a random projectile
     * image
     * 
     * @param range This number indicates the number of cells
     */
    public static void setImageSource(String imgName) {
        for (Projectile p : sPool)
            p.mAnimator.updateImage(imgName);
        sRandomizeImages = true;
        
    }

    /**
     * The "vector projectile" mechanism might lead to the projectiles moving
     * too fast. This will cause the speed to be multiplied by a factor
     * 
     * @param factor The value to multiply against the projectile speed.
     */
    public static void setProjectileVectorDampeningFactor(float factor) {
        sVectorDamp = factor;
    }

    /**
     * Indicate that all projectiles should participate in collisions
     */
    public static void enableCollisionsForProjectiles() {
        sSensorProjectiles = false;
    }

    /**
     * Indicate that projectiles thrown with the "vector" mechanism should have
     * a fixed velocity
     * 
     * @param velocity The magnitude of the velocity for projectiles
     */
    public static void setFixedVectorThrowVelocity(float velocity) {
        sEnableFixedVectorVelocity = true;
        sFixedVectorVelocity = velocity;
    }

    /**
     * Indicate that projectiles thrown via the "vector" mechanism should be
     * rotated to face in their initial direction
     */
    public static void setRotateVectorThrow() {
        sRotateVectorThrow = true;
    }

    public void setCollisionOk() {
        sDisappearOnCollide = false;
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
     * @param mStrength specifies the amount of damage that a projectile does
     *            to an enemy
     */
    public static void configure(int size, float width, float height, String imgName,
            float velocityX, float velocityY, float offsetX, float offsetY, int strength,
            int zIndex, boolean isCircle) {
        // set up the pool
        sPool = new Projectile[size];
        // don't draw all projectiles in same place...
        for (int i = 0; i < size; ++i) {
            sPool[i] = new Projectile(width, height, imgName, -100 - i * width, -100 - i * height,
                    zIndex, isCircle);
            sPool[i].mVisible = false;
            sPool[i].mBody.setBullet(true);
            sPool[i].mBody.setActive(false);
        }
        sNextIndex = 0;
        sPoolSize = size;
        // record vars that describe how the projectile behaves
        sStrength = strength;
        sVelocity.set(velocityX, velocityY);
        sOffset.set(offsetX, offsetY);
        sRange = 1000;
        sThrowSound = null;
        sProjectileDisappearSound = null;
        sProjectilesRemaining = -1;
        sSensorProjectiles = true;
        sDisappearOnCollide = true;
    }

    /**
     * Set a limit on the total number of projectiles that can be thrown
     * 
     * @param number How many projectiles are available
     */
    public static void setNumberOfProjectiles(int number) {
        sProjectilesRemaining = number;
    }

    /**
     * Specify a sound to play when the projectile is thrown
     * 
     * @param soundName Name of the sound file to play
     */
    public static void setThrowSound(String soundName) {
        sThrowSound = Media.getSound(soundName);
    }

    /**
     * Specify the sound to play when a projectile disappears
     * 
     * @param soundName the name of the sound file to play
     */
    public static void setProjectileDisappearSound(String soundName) {
        sProjectileDisappearSound = Media.getSound(soundName);
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
        for (Projectile p : sPool)
            p.setDefaultAnimation(a);
    }

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
        if (other.mSpriteType == SpriteId.OBSTACLE) {
            Obstacle o = (Obstacle)other;
            if (o.mProjectileCollision != null) {
                o.mProjectileCollision.go(this, contact);
                // return... don't remove the projectile
                return;
            }
        }
        if (other.mSpriteType == SpriteId.PROJECTILE) {
            if (!sDisappearOnCollide)
                return;
        }
        // only disappear if other is not a sensor
        if (other.mBody.getFixtureList().get(0).isSensor())
            return;
        remove(false);
    }

    /**
     * Throw a projectile
     * 
     * @param xx x coordinate of the top left corner of the thrower
     * @param yy y coordinate of the top left corner of the thrower
     */
    static void throwFixed(float xx, float yy, Hero h) {
        // have we reached our limit?
        if (sProjectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (sProjectilesRemaining != -1)
            sProjectilesRemaining--;

        // is there an available projectile?
        if (sPool[sNextIndex].mVisible)
            return;
        // calculate offset for starting position of projectile
        // get the next projectile
        Projectile b = sPool[sNextIndex];
        b.mRangeFrom.x = xx + sOffset.x;
        b.mRangeFrom.y = yy + sOffset.y;

        // reset its sensor
        b.setCollisionEffect(!sSensorProjectiles);

        // set its sprite
        if (sRandomizeImages)
            b.mAnimator.pickRandomIndex();

        sNextIndex = (sNextIndex + 1) % sPoolSize;

        // put the projectile on the screen and place it in the physics world
        b.mBody.setActive(true);
        b.mBody.setTransform(b.mRangeFrom, 0);

        // give the projectile velocity
        b.updateVelocity(sVelocity.x, sVelocity.y);

        // make the projectile visible
        b.mVisible = true;
        if (sThrowSound != null)
            sThrowSound.play();
        b.mDisappearSound = sProjectileDisappearSound;

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
        if (sProjectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (sProjectilesRemaining != -1)
            sProjectilesRemaining--;

        // is there an available projectile?
        if (sPool[sNextIndex].mVisible)
            return;
        // get the next projectile
        Projectile b = sPool[sNextIndex];
        // calculate offset for starting position of projectile
        b.mRangeFrom.x = heroX + sOffset.x;
        b.mRangeFrom.y = heroY + sOffset.y;

        // reset its sensor
        b.setCollisionEffect(!sSensorProjectiles);

        sNextIndex = (sNextIndex + 1) % sPoolSize;
        // put the projectile on the screen and place it in the physics world
        b.mBody.setActive(true);
        b.mBody.setTransform(b.mRangeFrom, 0);

        // give the projectile velocity
        if (sEnableFixedVectorVelocity) {
            // compute a unit vector
            float dX = toX - heroX - sOffset.x;
            float dY = toY - heroY - sOffset.y;
            float hypotenuse = (float)Math.sqrt(dX * dX + dY * dY);
            float tmpX = dX / hypotenuse;
            float tmpY = dY / hypotenuse;
            // multiply by fixed velocity
            tmpX *= sFixedVectorVelocity;
            tmpY *= sFixedVectorVelocity;
            b.updateVelocity(tmpX, tmpY);
        } else {
            float dX = toX - heroX - sOffset.x;
            float dY = toY - heroY - sOffset.y;
            // compute absolute vector, multiply by dampening factor
            float tmpX = dX * sVectorDamp;
            float tmpY = dY * sVectorDamp;
            b.updateVelocity(tmpX, tmpY);
        }

        // rotate the projectile
        if (sRotateVectorThrow) {
            double angle = Math.atan2(toY - heroY - sOffset.y, toX - heroX - sOffset.x)
                    - Math.atan2(-1, 0);
            b.mBody.setTransform(b.mBody.getPosition(), (float)angle);
        }

        // make the projectile visible
        b.mVisible = true;
        if (sThrowSound != null)
            sThrowSound.play();
        b.mDisappearSound = sProjectileDisappearSound;

        // now animate the hero to do the throw:
        h.doThrowAnimation();
    }

    /**
     * Internal method for negating gravity in side scrollers and for enforcing
     * the projectile range
     */
    @Override
    public void render(SpriteBatch sb, float delta) {
        // eliminate the projectile quietly if it has traveled too far
        float dx = Math.abs(mBody.getPosition().x - mRangeFrom.x);
        float dy = Math.abs(mBody.getPosition().y - mRangeFrom.y);
        if (dx * dx + dy * dy > sRange * sRange) {
            remove(true);
            mBody.setActive(false);
            return;
        }
        super.render(sb, delta);
    }
}
