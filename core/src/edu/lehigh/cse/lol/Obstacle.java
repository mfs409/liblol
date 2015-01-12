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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.internals.CollisionCallback;

/**
 * Obstacles are usually walls, except they can move, and can be used to run all
 * sorts of abritrary code that changes the game, or the behavior of the things
 * that collide with them. It's best to think of them as being both "a wall" and
 * "a catch-all for any behavior that we don't have anywhere else".
 */
public class Obstacle extends Actor {
    /**
     * One of the main uses of obstacles is to use hero/obstacle collisions as a
     * way to run custom code. This callback defines what code to run when a
     * hero collides with this obstacle.
     */
    CollisionCallback mHeroCollision;

    /**
     * This callback is for when an enemy collides with an obstacle
     */
    CollisionCallback mEnemyCollision;

    /**
     * This callback is for when a projectile collides with an obstacle
     */
    CollisionCallback mProjectileCollision;

    /**
     * Indicate that this obstacle does not re-enable jumping for the hero
     */
    boolean mNoJumpReenable;

    /**
     * a sound to play when the obstacle is hit by a hero
     */
    private Sound mCollideSound;

    /**
     * how long to delay (in nanoseconds) between attempts to play the collide
     * sound
     */
    private long mCollideSoundDelay;

    /**
     * Time of last collision sound
     */
    private long mLastCollideSoundTime;

    /**
     * Internal constructor to build an Obstacle. This should never be invoked
     * directly. Instead, use the 'addXXX' methods of the Object class.
     *
     * @param width   width of this Obstacle
     * @param height  height of this Obstacle
     * @param imgName Name of the image file to use
     */
    protected Obstacle(float width, float height, String imgName) {
        super(imgName, width, height);
    }

    /**
     * Draw an obstacle with an underlying box shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @return The obstacle, so that it can be further modified
     */
    public static Obstacle makeAsBox(float x, float y, float width, float height, String imgName) {
        Obstacle o = new Obstacle(width, height, imgName);
        o.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false, x, y);
        Lol.sGame.mCurrentLevel.addActor(o, 0);
        return o;
    }

    /**
     * Draw an obstacle with an underlying polygon shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @param verts   Up to 16 coordinates representing the vertexes of this
     *                polygon, listed as x0,y0,x1,y1,x2,y2,...
     * @return The obstacle, so that it can be further modified
     */
    public static Obstacle makeAsPolygon(float x, float y, float width, float height, String imgName, float... verts) {
        Obstacle o = new Obstacle(width, height, imgName);
        o.setPolygonPhysics(0, 0, 0, BodyType.StaticBody, false, x, y, verts);
        Lol.sGame.mCurrentLevel.addActor(o, 0);
        return o;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Draw an obstacle with an underlying circle shape
     *
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the obstacle
     * @param height  Height of the obstacle
     * @param imgName Name of image file to use
     * @return The obstacle, so that it can be further modified
     */
    public static Obstacle makeAsCircle(float x, float y, float width, float height, String imgName) {
        float radius = Math.max(width, height);
        Obstacle o = new Obstacle(width, height, imgName);
        o.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false, x, y, radius / 2);
        Lol.sGame.mCurrentLevel.addActor(o, 0);
        return o;
    }

    /**
     * Internal method for playing a sound when a hero collides with this
     * obstacle
     */
    void playCollideSound() {
        if (mCollideSound == null)
            return;

        // Make sure we have waited long enough
        long now = System.currentTimeMillis();
        if (now < mLastCollideSoundTime + mCollideSoundDelay)
            return;
        mLastCollideSoundTime = now;
        mCollideSound.play(Facts.getGameFact("volume", 1));
    }

    /**
     * Called when this Obstacle is the dominant obstacle in a collision
     *
     * Note: This Obstacle is /never/ the dominant obstacle in a collision,
     * since it is #6 or #7
     *
     * @param other   The other actor involved in this collision
     * @param contact A description of the collision
     */
    @Override
    void onCollide(Actor other, Contact contact) {
    }

    /**
     * Call this on an Obstacle to make it into a pad that changes the hero's
     * speed when the hero glides over it.
     *
     * These "pads" will multiply the hero's speed by the factor given as a
     * parameter. Factors can be negative to cause a reverse direction, less
     * than 1 to cause a slowdown (friction pads), or greater than 1 to serve as
     * zoom pads.
     *
     * @param factor Value to multiply the hero's velocity when it collides with
     *               this Obstacle
     */
    public void setPad(final float factor) {
        // disable collisions on this obstacle
        setCollisionsEnabled(false);
        // register a callback to multiply the hero's speed by factor
        mHeroCollision = new CollisionCallback() {
            @Override
            public void go(Actor h, Contact c) {
                Vector2 v = h.mBody.getLinearVelocity();
                v.scl(factor);
                h.updateVelocity(v.x, v.y);
            }
        };
    }

    /**
     * Call this on an obstacle to make it behave like a "pad" obstacle, except
     * with a constant additive (or subtractive) effect on the hero's speed.
     *
     * @param boostAmountX  The amount to add to the hero's X velocity
     * @param boostAmountY  The amount to add to the hero's Y velocity
     * @param boostDuration How long should the speed boost last (use -1 to indicate
     *                      "forever")
     */
    public void setSpeedBoost(final float boostAmountX, final float boostAmountY, final float boostDuration) {
        // disable collisions on this obstacle
        setCollisionsEnabled(false);
        // register a callback to change the hero's speed
        mHeroCollision = new CollisionCallback() {
            @Override
            public void go(final Actor h, Contact c) {
                // boost the speed
                Vector2 v = h.mBody.getLinearVelocity();
                v.x += boostAmountX;
                v.y += boostAmountY;
                h.updateVelocity(v.x, v.y);
                // now set a timer to un-boost the speed
                if (boostDuration > 0) {
                    // set up a timer to shut off the boost
                    Timer.schedule(new Task() {
                        @Override
                        public void run() {
                            Vector2 v = h.mBody.getLinearVelocity();
                            v.x -= boostAmountX;
                            v.y -= boostAmountY;
                            h.updateVelocity(v.x, v.y);
                        }
                    }, boostDuration);
                }
            }
        };
    }

    /**
     * Control whether the hero can jump if it collides with this obstacle while
     * in the air
     *
     * @param enable true if the hero can jump again, false otherwise
     */
    public void setReJump(boolean enable) {
        mNoJumpReenable = !enable;
    }

    /**
     * Make the object a callback object, so that custom code will run when a
     * /hero/ collides with it
     *
     * @param activationGoodies1 Number of type-1 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies2 Number of type-2 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies3 Number of type-3 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies4 Number of type-4 goodies that must be collected before this
     *                           callback works
     * @param delay              The time between when the collision happens, and when the
     *                           callback code runs. Use 0 for immediately
     * @param callback           The code to run when the collision happens
     */
    public void setHeroCollisionCallback(int activationGoodies1, int activationGoodies2, int activationGoodies3,
                                         int activationGoodies4, final float delay, final LolCallback callback) {
        // save the required goodie counts, turn off collisions
        final int[] counts = new int[]{activationGoodies1, activationGoodies2, activationGoodies3, activationGoodies4};
        setCollisionsEnabled(false);

        // register a callback
        mHeroCollision = new CollisionCallback() {
            @Override
            public void go(final Actor ps, Contact c) {
                // Make sure the contact is active (it's not if this is a
                // pass-through event)
                if (c.isEnabled()) {
                    // check if callback is activated, if so run Callback code
                    boolean match = true;
                    for (int i = 0; i < 4; ++i)
                        match &= counts[i] <= Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[i];
                    if (match) {
                        // run now, or delay?
                        if (delay <= 0) {
                            callback.mAttachedActor = Obstacle.this;
                            callback.mCollideActor = ps;
                            callback.onEvent();
                        } else {
                            Timer.schedule(new Task() {
                                @Override
                                public void run() {
                                    callback.mAttachedActor = Obstacle.this;
                                    callback.mCollideActor = ps;
                                    callback.onEvent();
                                }
                            }, delay);
                        }
                    }
                }
            }
        };
    }

    /**
     * Make the object a callback object, so that custom code will run when an
     * /enemy/ collides with it
     *
     * @param activationGoodies1 Number of type-1 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies2 Number of type-2 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies3 Number of type-3 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies4 Number of type-4 goodies that must be collected before this
     *                           callback works
     * @param delay              The time between when the collision happens, and when the
     *                           callback code runs. Use 0 for immediately
     * @param callback           The code to run when an enemy collides with this obstacle
     */
    public void setEnemyCollisionCallback(int activationGoodies1, int activationGoodies2, int activationGoodies3,
                                          int activationGoodies4, final float delay, final LolCallback callback) {
        /**
         * Enemy callbacks can require certain Goodie counts in order to run
         */
        final int[] enemyCallbackActivation = new int[]{activationGoodies1, activationGoodies2, activationGoodies3,
                activationGoodies4};

        mEnemyCollision = new CollisionCallback() {
            @Override
            public void go(final Actor ps, Contact c) {
                boolean match = true;
                for (int i = 0; i < 4; ++i)
                    match &= enemyCallbackActivation[i] <= Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[i];
                if (match) {
                    // run the callback after a delay, or immediately?
                    if (delay <= 0) {
                        callback.mAttachedActor = Obstacle.this;
                        callback.mCollideActor = ps;
                        callback.onEvent();
                    } else {
                        Timer.schedule(new Task() {
                            @Override
                            public void run() {
                                callback.mAttachedActor = Obstacle.this;
                                callback.mCollideActor = ps;
                                callback.onEvent();
                            }
                        }, delay);
                    }
                }
            }
        };
    }

    /**
     * Make the object a callback object, so that custom code will run when a
     * /projectile/ collides with it.
     *
     * @param activationGoodies1 Number of type-1 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies2 Number of type-2 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies3 Number of type-3 goodies that must be collected before this
     *                           callback works
     * @param activationGoodies4 Number of type-4 goodies that must be collected before this
     *                           callback works
     * @param callback           The code to run on a collision
     */
    public void setProjectileCollisionCallback(int activationGoodies1, int activationGoodies2, int activationGoodies3,
                                               int activationGoodies4, final LolCallback callback) {
        final int[] projectileCallbackActivation = new int[]{activationGoodies1, activationGoodies2,
                activationGoodies3, activationGoodies4};

        mProjectileCollision = new CollisionCallback() {
            @Override
            public void go(Actor ps, Contact c) {
                boolean match = true;
                for (int i = 0; i < 4; ++i)
                    match &= projectileCallbackActivation[i] <= Lol.sGame.mCurrentLevel.mScore.mGoodiesCollected[i];
                if (match) {
                    callback.mAttachedActor = Obstacle.this;
                    callback.mCollideActor = ps;
                    callback.onEvent();
                }
            }
        };
    }

    /**
     * Indicate that when the hero collides with this obstacle, we should make a
     * sound
     *
     * @param sound The name of the sound file to play
     * @param delay How long to wait before playing the sound again, in
     *              milliseconds
     */
    public void setCollideSound(String sound, long delay) {
        mCollideSound = Media.getSound(sound);
        mCollideSoundDelay = delay * 1000000;
    }
}
