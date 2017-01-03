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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Projectiles are actors that can be thrown from the hero's location in order
 * to remove enemies. Note that there is no public interface to this file.
 * Projectiles should be controlled via ProjectilePool.
 */
class Projectile extends WorldActor {
    /// This is the initial point of the throw
    final Vector2 mRangeFrom;

    /// We have to be careful in side-scrollers, or else projectiles can continue traveling
    // off-screen forever. This field lets us cap the distance away from the hero that a projectile
    // can travel before we make it disappear.
    float mRange;

    /// When projectiles collide, and they are not sensors, one will disappear. We can keep both on
    // screen by setting this false
    boolean mDisappearOnCollide;

    /// How much damage does this projectile do?
    int mDamage;

    /**
     * Internal method to create a projectile. Projectiles have an underlying
     * circle as their physics body
     *
     * @param width    width of the projectile
     * @param height   height of the projectile
     * @param imgName  Name of the image file to use for this projectile
     * @param x        initial x position of the projectile
     * @param y        initial y position of the projectile
     * @param zIndex   The z plane of the projectile
     * @param isCircle True if it is a circle, false if it is a box
     */
    Projectile(Lol game, MainScene level, float width, float height, String imgName, float x, float y, int zIndex, boolean isCircle) {
        super(game, level, imgName, width, height);
        if (isCircle) {
            float radius = Math.max(width, height);
            setCirclePhysics(BodyType.DynamicBody, x, y, radius / 2);
        } else {
            setBoxPhysics(BodyType.DynamicBody, x, y);
        }
        setFastMoving(true);
        mBody.setGravityScale(0);
        setCollisionsEnabled(false);
        disableRotation();
        mScene.addActor(this, zIndex);
        mDisappearOnCollide = true;
        mRangeFrom = new Vector2();
        mRange = 1000;
    }

    /**
     * Standard collision detection routine. This only causes a callback on
     * hitting an obstacle, which makes the projectile disappear, or on hitting
     * a projectile, which is a bit funny because one of the two projectiles
     * will live.
     *
     * @param other   The other actor involved in the collision
     * @param contact A description of the contact
     */
    @Override
    void onCollide(WorldActor other, Contact contact) {
        // if this is an obstacle, check if it is a projectile callback, and if
        // so, do the callback
        if (other instanceof Obstacle) {
            Obstacle o = (Obstacle) other;
            if (o.mProjectileCollision != null) {
                o.mProjectileCollision.go(o, this, contact);
                // return... don't remove the projectile
                return;
            }
        }
        if (other instanceof Projectile) {
            if (!mDisappearOnCollide)
                return;
        }
        // only disappear if other is not a sensor
        if (other.mBody.getFixtureList().get(0).isSensor())
            return;
        remove(false);
    }

    /**
     * When drawing a projectile, we first check if it is too far from its
     * starting point. We only draw it if it is not.
     */
    @Override
    public void render(SpriteBatch sb, float delta) {
        // eliminate the projectile quietly if it has traveled too far
        float dx = Math.abs(mBody.getPosition().x - mRangeFrom.x);
        float dy = Math.abs(mBody.getPosition().y - mRangeFrom.y);
        if (dx * dx + dy * dy > mRange * mRange) {
            remove(true);
            mBody.setActive(false);
            return;
        }
        super.render(sb, delta);
    }
}