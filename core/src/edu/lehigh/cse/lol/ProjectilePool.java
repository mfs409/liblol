/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.audio.Sound;

/**
 * ProjectilePool stores a set of projectiles.  We can get into lots of trouble with Box2d if we
 * make too many actors, so the projectile pool is a useful mechanism for re-using projectiles after
 * they become defunct.
 */
class ProjectilePool {
    /// The level in which this pool exists
    private MainScene mLevel;
    /// A collection of all the available projectiles
    final Projectile mPool[];
    /// The number of projectiles in the pool
    private final int mPoolSize;
    /// For limiting the number of projectiles that can be thrown
    int mProjectilesRemaining;
    /// A dampening factor to apply to projectiles thrown via "directional" mechanism
    float mDirectionalDamp;
    /// Indicates that projectiles should be sensors
    boolean mSensorProjectiles;
    /// Indicates that vector projectiles should have a fixed velocity
    boolean mEnableFixedVectorVelocity;
    /// The magnitude of the velocity for vector projectiles thrown with a fixed velocity
    float mFixedVectorVelocity;
    /// Indicate that projectiles should face in the direction they are initially thrown
    boolean mRotateVectorThrow;
    /// Index of next available projectile in the pool
    private int mNextIndex;
    /// For choosing random images for the projectiles
    boolean mRandomizeImages;
    /// Sound to play when projectiles are thrown
    Sound mThrowSound;
    /// The sound to play when a projectile disappears
    Sound mProjectileDisappearSound;

    /**
     * Create a pool of projectiles, and set the way they are thrown.
     *
     * @param game    The currently active game
     * @param size     number of projectiles that can be thrown at once
     * @param width    width of a projectile
     * @param height   height of a projectile
     * @param imgName  image to use for projectiles
     * @param strength specifies the amount of damage that a projectile does to an
     *                 enemy
     * @param zIndex   The z plane on which the projectiles should be drawn
     * @param isCircle Should projectiles have an underlying circle or box shape?
     */
    ProjectilePool(Lol game, MainScene level, int size, float width, float height, String imgName,
                   int strength, int zIndex, boolean isCircle) {
        mLevel = level;
        // set up the pool
        mPool = new Projectile[size];
        // don't draw all projectiles in same place...
        for (int i = 0; i < size; ++i) {
            mPool[i] = new Projectile(game, level, width, height, imgName, -100 - i * width,
                    -100 - i * height, zIndex, isCircle);
            mPool[i].mEnabled= false;
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
     * Throw a projectile. This is for throwing in a single, predetermined direction
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
        if (mPool[mNextIndex].mEnabled)
            return;
        // get the next projectile, reset sensor, set image
        Projectile b = mPool[mNextIndex];
        mNextIndex = (mNextIndex + 1) % mPoolSize;
        b.setCollisionsEnabled(!mSensorProjectiles);
        if (mRandomizeImages)
            b.mAnimator.updateIndex(mLevel.mGenerator);

        // calculate offset for starting position of projectile, put it on screen
        b.mRangeFrom.x = h.getXPosition() + offsetX;
        b.mRangeFrom.y = h.getYPosition() + offsetY;
        b.mBody.setActive(true);
        b.mBody.setTransform(b.mRangeFrom, 0);

        // give the projectile velocity, show it, play sound, animate the hero
        b.updateVelocity(velocityX, velocityY);
        b.mEnabled = true;
        if (mThrowSound != null)
            mThrowSound.play(Lol.getGameFact(mLevel.mConfig, "volume", 1));
        b.mDisappearSound = mProjectileDisappearSound;
        h.doThrowAnimation();
    }

    /**
     * Throw a projectile. This is for throwing in an arbitrary direction, based on the location of
     * a touch
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
    void throwAt(float heroX, float heroY, float toX, float toY, Hero h, float offsetX,
                 float offsetY) {
        // have we reached our limit?
        if (mProjectilesRemaining == 0)
            return;
        // do we need to decrease our limit?
        if (mProjectilesRemaining != -1)
            mProjectilesRemaining--;

        // is there an available projectile?
        if (mPool[mNextIndex].mEnabled)
            return;
        // get the next projectile, set sensor, set image
        Projectile b = mPool[mNextIndex];
        mNextIndex = (mNextIndex + 1) % mPoolSize;
        b.setCollisionsEnabled(!mSensorProjectiles);
        if (mRandomizeImages)
            b.mAnimator.updateIndex(mLevel.mGenerator);

        // calculate offset for starting position of projectile, put it on screen
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
        b.mEnabled= true;
        if (mThrowSound != null)
            mThrowSound.play(Lol.getGameFact(mLevel.mConfig, "volume", 1));
        b.mDisappearSound = mProjectileDisappearSound;
        h.doThrowAnimation();
    }
}
