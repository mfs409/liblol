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

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

import edu.lehigh.cse.lol.internals.CollisionCallback;
import edu.lehigh.cse.lol.internals.GestureAction;

/**
 * Enemies are things to be avoided or defeated by the hero. Every enemy can be
 * defeated via projectiles. They can also be defeated by colliding with
 * invincible heroes, or by colliding with a hero whose strength is &gt;= the
 * enemy's damage, though that case results in the hero losing strength. A level
 * can require all enemies to be defeated before the level can be won. Note that
 * Enemies can move in a variety of ways
 */
public class Enemy extends Actor {
    /**
     * Amount of damage this enemy does to a hero on a collision. The default is
     * 2, so that an enemy will defeat a hero and not disappear.
     */
    int mDamage = 2;

    /**
     * Message to display when this enemy defeats the last hero
     */
    String mOnDefeatHeroText = "";

    /**
     * Does a crawling hero avoid being damaged by this enemy?
     */
    boolean mDefeatByCrawl;

    /**
     * Does an in-air hero avoid being damaged by this enemy?
     */
    boolean mDefeatByJump;

    /**
     * Is this enemy immune to invincibility? That means it won't hurt the
     * enemy, but it won't disappear
     */
    boolean mImmuneToInvincibility;

    /**
     * Does the enemy do damage even to an invincible hero?
     */
    boolean mAlwaysDoesDamage;

    /**
     * A callback to run when the enemy is defeated
     */
    private CollisionCallback mDefeatCallback;

    /**
     * Create an Enemy. This should never be called directly.
     *
     * @param width   Width of this enemy
     * @param height  Height of this enemy
     * @param imgName Image to display
     */
    protected Enemy(float width, float height, String imgName) {
        super(imgName, width, height);
        Lol.sGame.mCurrentLevel.mScore.mEnemiesCreated++;
    }

    /**
     * Make an enemy that has an underlying rectangular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the enemy
     * @param height  The height of the enemy
     * @param imgName The name of the image to display
     * @return The enemy, so that it can be modified further
     */
    public static Enemy makeAsBox(float x, float y, float width, float height, String imgName) {
        Enemy e = new Enemy(width, height, imgName);
        e.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        Lol.sGame.mCurrentLevel.addActor(e, 0);
        return e;
    }
    /**
     * Draw an enemy with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this
     *                polygon, listed as x0,y0,x1,y1,x2,y2,...
     * @return The enemy, so that it can be further modified
     */
    public static Enemy makeAsPolygon(float x, float y, float width, float height, String imgName, float... verts) {
        Enemy e = new Enemy(width, height, imgName);
        e.setPolygonPhysics(0, 0, 0, BodyType.StaticBody, false, x, y, verts);
        Lol.sGame.mCurrentLevel.addActor(e, 0);
        return e;
    }

    /**
     * Make an enemy that has an underlying circular shape.
     *
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom right corner
     * @param width   The width of the enemy
     * @param height  The height of the enemy
     * @param imgName The name of the image to display
     * @return The enemy, so that it can be modified further
     */
    public static Enemy makeAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Enemy e = new Enemy(radius, radius, imgName);
        e.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius / 2);
        Lol.sGame.mCurrentLevel.addActor(e, 0);
        return e;
    }

    /**
     * Collision behavior of enemies. Based on our Actor numbering scheme, the
     * only concerns are to ensure that when a projectile hits this enemy, we
     * remove the enemy and hide the projectile, and to handle collisions with
     * certain obstacles
     *
     * @param other   The other actor involved in the collision
     * @param contact The contact information for the collision
     */
    @Override
    void onCollide(Actor other, Contact contact) {
        // collision with obstacles
        if (other instanceof Obstacle)
            onCollideWithObstacle((Obstacle) other, contact);
        // collision with projectiles
        if (other instanceof Projectile)
            onCollideWithProjectile((Projectile) other);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Dispatch method for handling Enemy collisions with Obstacles
     *
     * @param o       The obstacle with which this Enemy collided
     * @param contact The contact information for the collision
     */
    private void onCollideWithObstacle(final Obstacle o, Contact contact) {
        // handle any callbacks the obstacle has
        if (o.mEnemyCollision != null)
            o.mEnemyCollision.go(this, contact);
    }

    /**
     * Dispatch method for handling Enemy collisions with projectiles
     *
     * @param p The projectile with which this enemy collided
     */
    private void onCollideWithProjectile(Projectile p) {
        // only work with active projectiles
        if (!p.mVisible)
            return;
        // compute damage to determine if the enemy is defeated
        mDamage -= p.mDamage;
        if (mDamage <= 0) {
            // hide the projectile quietly, so that the sound of the enemy can
            // be heard
            p.remove(true);
            // remove this enemy
            defeat(true);
        } else {
            // hide the projectile
            p.remove(false);
        }
    }

    /**
     * Set the amount of damage that this enemy does to a hero
     *
     * @param amount Amount of damage. Default is 2, since heroes have a default
     *               strength of 1, so that the enemy defeats the hero but does not
     *               disappear.
     */
    public void setDamage(int amount) {
        mDamage = amount;
    }

    /**
     * If this enemy defeats the last hero of the board, this is the message
     * that will be displayed
     *
     * @param message The message to display
     */
    public void setDefeatHeroText(String message) {
        mOnDefeatHeroText = message;
    }

    /**
     * When an enemy is defeated, this is the code sequence we run to figure out
     * how gameplay should change.
     *
     * @param increaseScore Indicate if we should increase the score when this enemy is
     *                      defeated
     */
    public void defeat(boolean increaseScore) {
        // remove the enemy from the screen
        remove(false);

        // possibly update score
        if (increaseScore)
            Lol.sGame.mCurrentLevel.mScore.onDefeatEnemy();

        // handle defeat callbacks
        if (mDefeatCallback != null)
            mDefeatCallback.go(this, null);
    }

    /**
     * Indicate that this enemy can be defeated by crawling into it
     */
    public void setDefeatByCrawl() {
        mDefeatByCrawl = true;
        // make sure heroes don't ricochet off of this enemy when defeating it
        // via crawling
        setCollisionsEnabled(false);
    }

    /**
     * Make this enemy resist invincibility
     */
    public void setResistInvincibility() {
        mImmuneToInvincibility = true;
    }

    /**
     * Make this enemy damage the hero even when the hero is invincible
     */
    public void setImmuneToInvincibility() {
        mAlwaysDoesDamage = true;
    }

    /**
     * Indicate that if the player touches this enemy, the enemy will be removed
     * from the game
     */
    public void setDisappearOnTouch() {
        mGestureResponder = new GestureAction() {
            @Override
            public boolean onTap(Vector3 touchVec) {
                Lol.sGame.vibrate(100);
                defeat(true);
                mGestureResponder = null;
                return true;
            }
        };
    }

    /**
     * Make the enemy a "defeat callback" enemy, so that custom code will run
     * when this enemy is defeated
     *
     * @param callback The callback to run when the enemy is defeated
     */
    public void setDefeatCallback(final LolCallback callback) {
        mDefeatCallback = new CollisionCallback() {
            @Override
            public void go(Actor ps, Contact c) {
                callback.mAttachedActor = Enemy.this;
                callback.onEvent();
            }
        };
    }

    /**
     * Mark this enemy as no longer being a defeat callback enemy
     */
    public void clearDefeatCallback() {
        mDefeatCallback = null;
    }

    /**
     * Mark this enemy as one that can be defeated by jumping
     */
    public void setDefeatByJump() {
        mDefeatByJump = true;
    }
}
