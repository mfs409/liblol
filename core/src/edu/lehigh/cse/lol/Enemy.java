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

import com.badlogic.gdx.physics.box2d.Contact;

/**
 * Enemies are things to be avoided or defeated by the Hero. Enemies do damage to heroes when they
 * collide with heroes, and enemies can be defeated by heroes, in a variety of ways.
 */
public class Enemy extends WorldActor {
    /// Amount of damage this enemy does to a hero on a collision. The default is 2, so that an
    /// enemy will defeat a hero and not disappear.
    int mDamage;
    /// Message to display when this enemy defeats the last hero
    String mOnDefeatHeroText;
    /// Does a crawling hero automatically defeat this enemy?
    boolean mDefeatByCrawl;
    /// Does an in-air hero automatically defeat this enemy
    boolean mDefeatByJump;
    /// When the enemy collides with an invincible hero, does the enemy stay alive?
    boolean mImmuneToInvincibility;
    /// When the enemy collides with an invincible hero, does it stay alive and damage the hero?
    boolean mAlwaysDoesDamage;
    /// A callback to run when the enemy is defeated
    private LolActorEvent mDefeatCallback;

    /**
     * Create a basic Enemy.  The enemy won't yet have any physics attached to it.
     *
     * @param game    The currently active game
     * @param scene   The scene into which the destination is being placed
     * @param width   Width of this enemy
     * @param height  Height of this enemy
     * @param imgName Image to display
     */
    Enemy(Lol game, MainScene scene, float width, float height, String imgName) {
        super(game, scene, imgName, width, height);
        mDamage = 2;
        mOnDefeatHeroText = "";
    }

    /**
     * Code to run when an Enemy collides with a WorldActor.
     * <p>
     * Based on our WorldActor numbering scheme, the only concerns are collisions with Obstacles
     * and Projectiles
     *
     * @param other   Other actor involved in this collision
     * @param contact A description of the collision
     */
    @Override
    void onCollide(WorldActor other, Contact contact) {
        // collision with obstacles
        if (other instanceof Obstacle)
            onCollideWithObstacle((Obstacle) other, contact);
        // collision with projectiles
        if (other instanceof Projectile)
            onCollideWithProjectile((Projectile) other);
    }

    /**
     * Dispatch method for handling Enemy collisions with Obstacles
     *
     * @param obstacle The obstacle with which this Enemy collided
     * @param contact A description of the collision
     */
    private void onCollideWithObstacle(final Obstacle obstacle, Contact contact) {
        // handle any callbacks the obstacle has
        if (obstacle.mEnemyCollision != null)
            obstacle.mEnemyCollision.go(obstacle, this, contact);
    }

    /**
     * Dispatch method for handling Enemy collisions with Projectiles
     *
     * @param projectile The projectile with which this Enemy collided
     */
    private void onCollideWithProjectile(Projectile projectile) {
        // ignore inactive projectiles
        if (!projectile.mEnabled)
            return;
        // compute damage to determine if the enemy is defeated
        mDamage -= projectile.mDamage;
        if (mDamage <= 0) {
            // hide the projectile quietly, so that the sound of the enemy can
            // be heard
            projectile.remove(true);
            // remove this enemy
            defeat(true);
        } else {
            // hide the projectile
            projectile.remove(false);
        }
    }

    /**
     * Set the amount of damage that this enemy does to a hero
     *
     * @param amount Amount of damage. The default is 2, since heroes have a default strength of 1,
     *               so that the enemy defeats the hero but does not disappear.
     */
    public void setDamage(int amount) {
        mDamage = amount;
    }

    /**
     * If this enemy defeats the last hero of the board, this is the message that will be displayed
     *
     * @param message The message to display
     */
    public void setDefeatHeroText(String message) {
        mOnDefeatHeroText = message;
    }

    /**
     * When an enemy is defeated, this this code figures out how gameplay should change.
     *
     * @param increaseScore Indicate if we should increase the score when this enemy is defeated
     */
    public void defeat(boolean increaseScore) {
        // remove the enemy from the screen
        remove(false);

        // possibly update score
        if (increaseScore)
            mGame.mManager.onDefeatEnemy();

        // run any defeat callbacks
        if (mDefeatCallback != null)
            mDefeatCallback.go(this);
    }

    /**
     * Indicate that this enemy can be defeated by crawling into it
     */
    public void setDefeatByCrawl() {
        mDefeatByCrawl = true;
        // make sure heroes don't ricochet off of this enemy when defeating it via crawling
        setCollisionsEnabled(false);
    }

    /**
     * Mark this enemy as one that can be defeated by jumping
     */
    public void setDefeatByJump() {
        mDefeatByJump = true;
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
     * Indicate that if the player touches this enemy, the enemy will be removed from the game
     */
    public void setDisappearOnTouch() {
        mTapHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                Lol.vibrate(mScene.mConfig, 100);
                defeat(true);
                mTapHandler = null;
                return true;
            }
        };
    }

    /**
     * Provide code to run when this Enemy is defeated
     *
     * @param callback The callback to run when the enemy is defeated.  Note that a value of
     *                 <code>null</code> will remove a previously-set callback
     */
    public void setDefeatCallback(LolActorEvent callback) {
        mDefeatCallback = callback;
    }
}
