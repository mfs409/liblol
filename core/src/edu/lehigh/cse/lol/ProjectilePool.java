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

/**
 * The ProjectilePool is the public interface to Projectiles. The programmer
 * can't make individual projectiles, but can configure a pool of projectiles
 * that can then be thrown by a hero.
 */
public class ProjectilePool {
    /**
     * A collection of all the available projectiles
     */
    private final Projectile mPool[];
    /**
     * The number of projectiles in the pool
     */
    private final int mPoolSize;
    /**
     * For limiting the number of projectiles that can be thrown
     */
    int mProjectilesRemaining;
    /**
     * A dampening factor to apply to projectiles thrown via "directional"
     * mechanism
     */
    private float mDirectionalDamp;
    /**
     * Indicates that projectiles should be sensors
     */
    private boolean mSensorProjectiles;
    /**
     * Indicates that vector projectiles should have a fixed velocity
     */
    private boolean mEnableFixedVectorVelocity;
    /**
     * The magnitude of the velocity for vector projectiles thrown with a fixed
     * velocity
     */
    private float mFixedVectorVelocity;
    /**
     * Indicate that projectiles should face in the direction they are initially
     * thrown
     */
    private boolean mRotateVectorThrow;
    /**
     * Index of next available projectile in the pool
     */
    private int mNextIndex;
    /**
     * For choosing random images for the projectiles
     */
    private boolean mRandomizeImages;

    /**
     * Sound to play when projectiles are thrown
     */
    private Sound mThrowSound;

    /**
     * The sound to play when a projectile disappears
     */
    private Sound mProjectileDisappearSound;

    /**
     * Create a pool of projectiles, and configure the way they are thrown. Note
     * that this is private... in LOL the programmer calls
     * ProjectilePool.configure, which forwards to this.
     *
     * @param size     number of projectiles that can be thrown at once
     * @param width    width of a projectile
     * @param height   height of a projectile
     * @param imgName  image to use for projectiles
     * @param strength specifies the amount of damage that a projectile does to an
     *                 enemy
     * @param zIndex   The z plane on which the projectiles should be drawn
     * @param isCircle Should projectiles have an underlying circle or box shape?
     */
    ProjectilePool(int size, float width, float height, String imgName, int strength, int zIndex, boolean isCircle) {
        // set up the pool
        mPool = new Projectile[size];
        // don't draw all projectiles in same place...
        for (int i = 0; i < size; ++i) {
            mPool[i] = new Projectile(width, height, imgName, -100 - i * width, -100 - i * height, zIndex, isCircle);
            mPool[i].mVisible = false;
            mPool[i].mBody.setBullet(true);
            mPool[i].mBody.setActive(false);
            mPool[i].mDamage = strength;
        }
        mNextIndex = 0;
        mPoolSize = size;
        // record vars that describe how the projectile behaves
        mThrowSound = null;
        mProjectileDisappearSound = null;
        mProjectilesRemaining = -1;
        mSensorProjectiles = true;
    }

    /**
     * Specify a limit on how far away from the Hero a projectile can go.
     * Without this, projectiles could keep on traveling forever.
     *
     * @param distance Maximum distance from the hero that a projectile can travel
     */
    public static void setRange(float distance) {
        for (Projectile p : Lol.sGame.mCurrentLevel.mProjectilePool.mPool)
            p.mRange = distance;
    }

    /**
     * Indicate that projectiles should feel the effects of gravity. Otherwise,
     * they will be (more or less) immune to gravitational forces.
     */
    public static void setProjectileGravityOn() {
        for (Projectile p : Lol.sGame.mCurrentLevel.mProjectilePool.mPool)
            p.mBody.setGravityScale(1);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Specify the image file from which to randomly choose projectile images
     *
     * @param imgName The file to use when picking images
     */
    public static void setImageSource(String imgName) {
        for (Projectile p : Lol.sGame.mCurrentLevel.mProjectilePool.mPool)
            p.mAnimator.updateImage(imgName);
        Lol.sGame.mCurrentLevel.mProjectilePool.mRandomizeImages = true;
    }

    /**
     * The "directional projectile" mechanism might lead to the projectiles
     * moving too fast. This will cause the speed to be multiplied by a factor
     *
     * @param factor The value to multiply against the projectile speed.
     */
    public static void setProjectileVectorDampeningFactor(float factor) {
        Lol.sGame.mCurrentLevel.mProjectilePool.mDirectionalDamp = factor;
    }

    /**
     * Indicate that all projectiles should participate in collisions, rather
     * than disappearing when they collide with other actors
     */
    public static void enableCollisionsForProjectiles() {
        Lol.sGame.mCurrentLevel.mProjectilePool.mSensorProjectiles = false;
    }

    /**
     * Indicate that projectiles thrown with the "directional" mechanism should
     * have a fixed velocity
     *
     * @param velocity The magnitude of the velocity for projectiles
     */
    public static void setFixedVectorThrowVelocity(float velocity) {
        Lol.sGame.mCurrentLevel.mProjectilePool.mEnableFixedVectorVelocity = true;
        Lol.sGame.mCurrentLevel.mProjectilePool.mFixedVectorVelocity = velocity;
    }

    /**
     * Indicate that projectiles thrown via the "directional" mechanism should
     * be rotated to face in their direction or movement
     */
    public static void setRotateVectorThrow() {
        Lol.sGame.mCurrentLevel.mProjectilePool.mRotateVectorThrow = true;
    }

    /**
     * Indicate that when two projectiles collide, they should both remain on
     * screen
     */
    public static void setCollisionOk() {
        for (Projectile p : Lol.sGame.mCurrentLevel.mProjectilePool.mPool)
            p.mDisappearOnCollide = false;
    }

    /**
     * Describe the behavior of projectiles in a scene. You must call this if
     * you intend to use projectiles in your scene.
     *
     * @param size     number of projectiles that can be thrown at once
     * @param width    width of a projectile
     * @param height   height of a projectile
     * @param imgName  image to use for projectiles
     * @param strength specifies the amount of damage that a projectile does to an
     *                 enemy
     * @param zIndex   The z plane on which the projectiles should be drawn
     * @param isCircle Should projectiles have an underlying circle or box shape?
     */
    public static void configure(int size, float width, float height, String imgName, int strength, int zIndex,
                                 boolean isCircle) {
        Lol.sGame.mCurrentLevel.mProjectilePool = new ProjectilePool(size, width, height, imgName, strength, zIndex,
                isCircle);
    }

    /**
     * Set a limit on the total number of projectiles that can be thrown
     *
     * @param number How many projectiles are available
     */
    public static void setNumberOfProjectiles(int number) {
        Lol.sGame.mCurrentLevel.mProjectilePool.mProjectilesRemaining = number;
    }

    /**
     * Specify a sound to play when the projectile is thrown
     *
     * @param soundName Name of the sound file to play
     */
    public static void setThrowSound(String soundName) {
        Lol.sGame.mCurrentLevel.mProjectilePool.mThrowSound = Media.getSound(soundName);
    }

    /**
     * Specify the sound to play when a projectile disappears
     *
     * @param soundName the name of the sound file to play
     */
    public static void setProjectileDisappearSound(String soundName) {
        Lol.sGame.mCurrentLevel.mProjectilePool.mProjectileDisappearSound = Media.getSound(soundName);
    }

    /**
     * Specify how projectiles should be animated
     *
     * @param a The animation object to use for each projectile that is thrown
     */
    public static void setAnimation(Animation a) {
        for (Projectile p : Lol.sGame.mCurrentLevel.mProjectilePool.mPool)
            p.setDefaultAnimation(a);
    }

    /**
     * Throw a projectile. This is for throwing in a single, predetermined
     * direction
     *
     * @param h         The hero who is performing the throw
     * @param offsetX   specifies the x distance between the bottom left of the
     *                  projectile and the bottom left of the hero throwing the
     *                  projectile
     * @param offsetY   specifies the y distance between the bottom left of the
     *                  projectile and the bottom left of the hero throwing the
     *                  projectile
     * @param velocityX The X velocity of the projectile when it is thrown
     * @param velocityY The Y velocity of the projectile when it is thrown
     */
    void throwFixed(Hero h, float offsetX, float offsetY, float velocityX, float velocityY) {
        // have we reached our limit?
        if (mProjectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (mProjectilesRemaining != -1)
            mProjectilesRemaining--;

        // is there an available projectile?
        if (mPool[mNextIndex].mVisible)
            return;
        // get the next projectile, reset sensor, set image
        Projectile b = mPool[mNextIndex];
        mNextIndex = (mNextIndex + 1) % mPoolSize;
        b.setCollisionsEnabled(!mSensorProjectiles);
        if (mRandomizeImages)
            b.mAnimator.pickRandomIndex();

        // calculate offset for starting position of projectile, put it on
        // screen
        b.mRangeFrom.x = h.getXPosition() + offsetX;
        b.mRangeFrom.y = h.getYPosition() + offsetY;
        b.mBody.setActive(true);
        b.mBody.setTransform(b.mRangeFrom, 0);

        // give the projectile velocity, show it, play sound, animate the hero
        b.updateVelocity(velocityX, velocityY);
        b.mVisible = true;
        if (mThrowSound != null)
            mThrowSound.play(Facts.getGameFact("volume", 1));
        b.mDisappearSound = mProjectileDisappearSound;
        h.doThrowAnimation();
    }

    /**
     * Throw a projectile. This is for throwing in an arbitrary direction, based
     * on the location of a touch
     *
     * @param heroX   x coordinate of the bottom left corner of the thrower
     * @param heroY   y coordinate of the bottom left corner of the thrower
     * @param toX     x coordinate of the point at which to throw
     * @param toY     y coordinate of the point at which to throw
     * @param h       The hero who is performing the throw
     * @param offsetX specifies the x distance between the bottom left of the
     *                projectile and the bottom left of the hero throwing the
     *                projectile
     * @param offsetY specifies the y distance between the bottom left of the
     *                projectile and the bottom left of the hero throwing the
     *                projectile
     */
    void throwAt(float heroX, float heroY, float toX, float toY, Hero h, float offsetX, float offsetY) {
        // have we reached our limit?
        if (mProjectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (mProjectilesRemaining != -1)
            mProjectilesRemaining--;

        // is there an available projectile?
        if (mPool[mNextIndex].mVisible)
            return;
        // get the next projectile, set sensor, set image
        Projectile b = mPool[mNextIndex];
        mNextIndex = (mNextIndex + 1) % mPoolSize;
        b.setCollisionsEnabled(!mSensorProjectiles);
        if (mRandomizeImages)
            b.mAnimator.pickRandomIndex();

        // calculate offset for starting position of projectile, put it on
        // screen
        b.mRangeFrom.x = heroX + offsetX;
        b.mRangeFrom.y = heroY + offsetY;
        b.mBody.setActive(true);
        b.mBody.setTransform(b.mRangeFrom, 0);

        // give the projectile velocity
        if (mEnableFixedVectorVelocity) {
            // compute a unit vector
            float dX = toX - heroX - offsetX;
            float dY = toY - heroY - offsetY;
            float hypotenuse = (float) Math.sqrt(dX * dX + dY * dY);
            float tmpX = dX / hypotenuse;
            float tmpY = dY / hypotenuse;
            // multiply by fixed velocity
            tmpX *= mFixedVectorVelocity;
            tmpY *= mFixedVectorVelocity;
            b.updateVelocity(tmpX, tmpY);
        } else {
            float dX = toX - heroX - offsetX;
            float dY = toY - heroY - offsetY;
            // compute absolute vector, multiply by dampening factor
            float tmpX = dX * mDirectionalDamp;
            float tmpY = dY * mDirectionalDamp;
            b.updateVelocity(tmpX, tmpY);
        }

        // rotate the projectile
        if (mRotateVectorThrow) {
            double angle = Math.atan2(toY - heroY - offsetY, toX - heroX - offsetX) - Math.atan2(-1, 0);
            b.mBody.setTransform(b.mBody.getPosition(), (float) angle);
        }

        // show the projectile, play sound, and animate the hero
        b.mVisible = true;
        if (mThrowSound != null)
            mThrowSound.play(Facts.getGameFact("volume", 1));
        b.mDisappearSound = mProjectileDisappearSound;
        h.doThrowAnimation();
    }
}
