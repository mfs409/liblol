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
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;

/**
 * The Hero is the focal point of a game. While it is technically possible to have many heroes, or
 * invisible heroes that exist just so that the player has to keep bad things from happening to the
 * hero, it is usually the case that a game has one hero who moves around on the screen, possibly
 * jumping and crawling.
 */
public class Hero extends WorldActor {
    /// Strength of the hero. This determines how many collisions with enemies the hero can sustain
    /// before it is defeated. The default is 1, and the default enemy damage amount is 2, so that
    /// the default behavior is for the hero to be defeated on any collision with an enemy, with the
    /// enemy *not* disappearing
    private int mStrength;
    /// For tracking if the game should end immediately when this hero is defeated
    private boolean mMustSurvive;
    /// Code to run when the hero's strength changes
    private LolActorEvent mStrengthChangeCallback;

    /// Time until the hero's invincibility runs out
    private float mInvincibleRemaining;
    /// cells involved in animation for invincibility
    private Animation mInvincibleAnimation;

    /// cells involved in animation for throwing
    private Animation mThrowAnimation;
    /// seconds that constitute a throw action
    private float mThrowAnimateTotalLength;
    /// how long until we stop showing the throw animation
    private float mThrowAnimationTimeRemaining;

    /// Track if the hero is in the air, so that it can't jump when it isn't touching anything. This
    /// does not quite work as desired, but is good enough for LOL
    private boolean mInAir;
    /// When the hero jumps, this specifies the amount of velocity to add to simulate a jump
    private Vector2 mJumpImpulses;
    /// Indicate that the hero can jump while in the air
    private boolean mAllowMultiJump;
    /// Sound to play when a jump occurs
    private Sound mJumpSound;
    /// cells involved in animation for jumping
    private Animation mJumpAnimation;

    /// Is the hero currently in crawl mode?
    private boolean mCrawling;
    /// cells involved in animation for crawling
    private Animation mCrawlAnimation;

    /// For tracking the current amount of rotation of the hero
    private float mCurrentRotation;

    /**
     * Construct a Hero, but don't give it any physics yet
     *
     * @param game    The currently active game
     * @param scene   The scene into which the Hero is being placed
     * @param width   The width of the hero
     * @param height  The height of the hero
     * @param imgName The name of the file that has the default image for this hero
     */
    Hero(Lol game, MainScene scene, float width, float height, String imgName) {
        super(game, scene, imgName, width, height);
        mStrength = 1;
    }

    /**
     * Code to run when rendering the Hero.
     *
     * NB:  We can't just use the basic renderer, because we might need to adjust a one-off
     *      animation (invincibility or throw) first
     *
     * @param sb    The SpriteBatch to use for drawing this hero
     * @param delta The time since the last render
     */
    @Override
    void onRender(SpriteBatch sb, float delta) {
        // determine when to turn off throw animations
        if (mThrowAnimationTimeRemaining > 0) {
            mThrowAnimationTimeRemaining -= delta;
            if (mThrowAnimationTimeRemaining <= 0) {
                mThrowAnimationTimeRemaining = 0;
                mAnimator.setCurrentAnimation(mDefaultAnimation);
            }
        }

        // determine when to turn off invincibility and cease invincibility animation
        if (mInvincibleRemaining > 0) {
            mInvincibleRemaining -= delta;
            if (mInvincibleRemaining <= 0) {
                mInvincibleRemaining = 0;
                if (mInvincibleAnimation != null)
                    mAnimator.setCurrentAnimation(mDefaultAnimation);
            }
        }
        super.onRender(sb, delta);
    }

    /**
     * Make the hero jump, unless it is in the air and not multi-jump
     */
    void jump() {
        // NB: multi-jump prevents us from ever setting mInAir, so this is safe:
        if (mInAir)
            return;
        Vector2 v = mBody.getLinearVelocity();
        v.add(mJumpImpulses);
        updateVelocity(v.x, v.y);
        if (!mAllowMultiJump)
            mInAir = true;
        if (mJumpAnimation != null)
            mAnimator.setCurrentAnimation(mJumpAnimation);
        if (mJumpSound != null)
            mJumpSound.play(Lol.getGameFact(mScene.mConfig, "volume", 1));
        // suspend creation of sticky joints, so the hero can actually move
        mStickyDelay = System.currentTimeMillis() + 10;
    }

    /**
     * Stop the jump animation for a hero, and make it eligible to jump again
     */
    private void stopJump() {
        if (mInAir || mAllowMultiJump) {
            mInAir = false;
            mAnimator.setCurrentAnimation(mDefaultAnimation);
        }
    }

    /**
     * Make the hero's throw animation play while it is throwing a projectile
     */
    void doThrowAnimation() {
        if (mThrowAnimation != null) {
            mAnimator.setCurrentAnimation(mThrowAnimation);
            mThrowAnimationTimeRemaining = mThrowAnimateTotalLength;
        }
    }

    /**
     * Put the hero in crawl mode. Note that we make the hero rotate when it is crawling
     */
    void crawlOn() {
        if (mCrawling) {
            return;
        }
        mCrawling = true;
        mBody.setTransform(mBody.getPosition(), -3.14159f / 2);
        if (mCrawlAnimation != null)
            mAnimator.setCurrentAnimation(mCrawlAnimation);
    }

    /**
     * Take the hero out of crawl mode
     */
    void crawlOff() {
        if (!mCrawling) {
            return;
        }
        mCrawling = false;
        mBody.setTransform(mBody.getPosition(), 0);
        mAnimator.setCurrentAnimation(mDefaultAnimation);
    }

    /**
     * Change the rotation of the hero
     *
     * @param delta How much to add to the current rotation
     */
    void increaseRotation(float delta) {
        if (mInAir) {
            mCurrentRotation += delta;
            mBody.setAngularVelocity(0);
            mBody.setTransform(mBody.getPosition(), mCurrentRotation);
        }
    }

    /**
     * Code to run when a Hero collides with a WorldActor.
     *
     * The Hero is the dominant participant in all collisions. Whenever the hero collides with
     * something, we need to figure out what to do
     *
     * @param other   Other object involved in this collision
     * @param contact A description of the contact that caused this collision
     */
    @Override
    void onCollide(WorldActor other, Contact contact) {
        // NB: we currently ignore Projectile and Hero
        if (other instanceof Enemy)
            onCollideWithEnemy((Enemy) other);
        else if (other instanceof Destination)
            onCollideWithDestination((Destination) other);
        else if (other instanceof Obstacle)
            onCollideWithObstacle((Obstacle) other, contact);
        else if (other instanceof Goodie)
            onCollideWithGoodie((Goodie) other);
    }

    /**
     * Dispatch method for handling Hero collisions with Destinations
     *
     * @param destination The destination with which this hero collided
     */
    private void onCollideWithDestination(Destination destination) {
        // The hero must have enough goodies, and the destination must have room
        boolean match = true;
        for (int i = 0; i < 4; ++i)
            match &= mGame.mManager.mGoodiesCollected[i] >= destination.mActivation[i];
        if (match && (destination.mHolding < destination.mCapacity) && mEnabled) {
            // hide the hero quietly, since the destination might make a sound
            remove(true);
            destination.mHolding++;
            if (destination.mArrivalSound != null)
                destination.mArrivalSound.play(Lol.getGameFact(mScene.mConfig, "volume", 1));
            mGame.mManager.onDestinationArrive();
        }
    }

    /**
     * Dispatch method for handling Hero collisions with Enemies
     *
     * @param enemy The enemy with which this hero collided
     */
    private void onCollideWithEnemy(Enemy enemy) {
        // if the enemy always defeats the hero, no matter what, then defeat the hero
        if (enemy.mAlwaysDoesDamage) {
            remove(false);
            mGame.mManager.defeatHero(enemy);
            if (mMustSurvive)
                mGame.mManager.endLevel(false);
            return;
        }
        // handle hero invincibility
        if (mInvincibleRemaining > 0) {
            // if the enemy is immune to invincibility, do nothing
            if (enemy.mImmuneToInvincibility)
                return;
            enemy.defeat(true);
        }
        // defeat by crawling?
        else if (mCrawling && enemy.mDefeatByCrawl) {
            enemy.defeat(true);
        }
        // defeat by jumping only if the hero's bottom is above the enemy's mid-section
        else if (mInAir && enemy.mDefeatByJump &&
                getYPosition() > enemy.getYPosition() + enemy.mSize.y / 2) {
            enemy.defeat(true);
        }
        // when we can't defeat it by losing strength, remove the hero
        else if (enemy.mDamage >= mStrength) {
            remove(false);
            mGame.mManager.defeatHero(enemy);
            if (mMustSurvive)
                mGame.mManager.endLevel(false);
        }
        // when we can defeat it by losing strength
        else {
            addStrength(-enemy.mDamage);
            enemy.defeat(true);
        }
    }

    /**
     * Update the hero's strength, and then run the strength change callback (if any)
     *
     * @param amount The amount to add (use a negative value to subtract)
     */
    private void addStrength(int amount) {
        mStrength += amount;
        if (mStrengthChangeCallback != null) {
            mStrengthChangeCallback.go(this);
        }
    }

    /**
     * Dispatch method for handling Hero collisions with Obstacles
     *
     * @param o The obstacle with which this hero collided
     */
    private void onCollideWithObstacle(Obstacle o, Contact contact) {
        // do we need to play a sound?
        o.playCollideSound();

        // reset rotation of hero if this obstacle is not a sensor
        if ((mCurrentRotation != 0) && !o.mBody.getFixtureList().get(0).isSensor())
            increaseRotation(-mCurrentRotation);

        // if there is code attached to the obstacle for modifying the hero's behavior, run it
        if (o.mHeroCollision != null)
            o.mHeroCollision.go(o, this, contact);

        // If this is a wall, then mark us not in the air so we can do more jumps. Note that sensors
        // should not enable jumps for the hero.
        if ((mInAir || mAllowMultiJump) && !o.mBody.getFixtureList().get(0).isSensor() &&
            !o.mNoJumpReenable) {
            stopJump();
        }
    }

    /**
     * Dispatch method for handling Hero collisions with Goodies
     *
     * @param g The goodie with which this hero collided
     */
    private void onCollideWithGoodie(Goodie g) {
        // hide the goodie, count it, and update strength
        g.remove(false);
        mGame.mManager.onGoodieCollected(g);
        addStrength(g.mStrengthBoost);

        // deal with invincibility by updating invincible time and running an animation
        if (g.mInvincibilityDuration > 0) {
            mInvincibleRemaining += g.mInvincibilityDuration;
            if (mInvincibleAnimation != null)
                mAnimator.setCurrentAnimation(mInvincibleAnimation);
        }
    }

    /**
     * Return the hero's strength
     *
     * @return The strength of the hero
     */
    public int getStrength() {
        return mStrength;
    }

    /**
     * Change the hero's strength.
     *
     * NB: calling this will not run any strength change callbacks... they only run in conjunction
     *     with collisions with goodies or enemies.
     *
     * @param amount The new strength of the hero
     */
    public void setStrength(int amount) {
        mStrength = amount;
    }

    /**
     * Indicate that upon a touch, this hero should begin moving with a specific velocity
     *
     * @param x Velocity in X dimension
     * @param y Velocity in Y dimension
     */
    public void setTouchAndGo(final float x, final float y) {
        mTapHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                mHover = null;
                // if it was hovering, its body type won't be Dynamic
                if (mBody.getType() != BodyType.DynamicBody)
                    mBody.setType(BodyType.DynamicBody);
                setAbsoluteVelocity(x, y);
                // turn off isTouchAndGo, so we can't double-touch
                mTapHandler = null;
                return true;
            }
        };
    }

    /**
     * Specify the X and Y velocity to give to the hero whenever it is instructed to jump
     *
     * @param x Velocity in X direction
     * @param y Velocity in Y direction
     */
    public void setJumpImpulses(float x, float y) {
        mJumpImpulses = new Vector2(x, y);
    }

    /**
     * Indicate that this hero can jump while it is in the air
     */
    public void setMultiJumpOn() {
        mAllowMultiJump = true;
    }

    /**
     * Indicate that touching this hero should make it jump
     */
    public void setTouchToJump() {
        mTapHandler = new TouchEventHandler() {
            public boolean go(float worldX, float worldY) {
                jump();
                return true;
            }
        };
    }

    /**
     * Register an animation sequence for when the hero is jumping
     *
     * @param animation The animation to display
     */
    public void setJumpAnimation(Animation animation) {
        mJumpAnimation = animation;
    }

    /**
     * Set the sound to play when a jump occurs
     *
     * @param soundName The name of the sound file to use
     */
    public void setJumpSound(String soundName) {
        mJumpSound = mScene.mMedia.getSound(soundName);
    }

    /**
     * Register an animation sequence for when the hero is throwing a projectile
     *
     * @param animation The animation to display
     */
    public void setThrowAnimation(Animation animation) {
        mThrowAnimation = animation;
        // compute the length of the throw sequence, so that we can get our
        // timer right for restoring the default animation
        mThrowAnimateTotalLength = animation.getDuration() / 1000;
    }

    /**
     * Register an animation sequence for when the hero is crawling
     *
     * @param animation The animation to display
     */
    public void setCrawlAnimation(Animation animation) {
        mCrawlAnimation = animation;
    }

    /**
     * Register an animation sequence for when the hero is invincible
     *
     * @param a The animation to display
     */
    public void setInvincibleAnimation(Animation a) {
        mInvincibleAnimation = a;
    }

    /**
     * Indicate that the level should end immediately if this hero is defeated
     */
    public void setMustSurvive() {
        mMustSurvive = true;
    }

    /**
     * Provide code to run when the hero's strength changes
     *
     * @param callback The code to run.
     */
    public void setStrengthChangeCallback(LolActorEvent callback) {
        mStrengthChangeCallback = callback;
    }
}
