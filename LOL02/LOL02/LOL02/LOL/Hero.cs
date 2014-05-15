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

using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    public class Hero : PhysicsSprite
    {
        /**
         * Strength of the hero This determines how many collisions with enemies the
         * hero can sustain before it is defeated. The default is 1, and the default
         * enemy damage amount is 2, so that the default behavior is for the hero to
         * be defeated on any collision with an enemy, with the enemy *not*
         * disappearing
         */
        private int mStrength = 1;

        /**
         * Time until the hero's invincibility runs out
         */
        private double mInvicibilityRemaining;

        /**
         * Animation support: cells involved in animation for invincibility
         */
        private Animation mInvincibleAnimation;

        /**
         * Is the hero currently in crawl mode?
         */
        private bool mCrawling;

        /**
         * Animation support: cells involved in animation for crawling
         */
        private Animation mCrawlAnimation;

        /**
         * Animation support: cells involved in animation for throwing
         */
        private Animation mThrowAnimation;

        /**
         * Animation support: seconds that constitute a throw action
         */
        private double mThrowAnimateTotalLength;

        /**
         * Animation support: how long until we stop showing the throw animation
         */
        private double mThrowAnimationTimeRemaining;

        /**
         * Track if the hero is in the air, so that it can't jump when it isn't
         * touching anything. This does not quite work as desired, but is good
         * enough for LOL
         */
        private bool mInAir;

        /**
         * When the hero jumps, this specifies the amount of velocity to add to
         * simulate a jump
         */
        private Vector2 mJumpImpulses;

        /**
         * Indicate that the hero can jump while in the air
         */
        private bool mAllowMultiJump;

        /**
         * Does the hero jump when we touch it?
         */
        private bool mIsTouchJump;

        /**
         * Animation support: cells involved in animation for jumping
         */
        private Animation mJumpAnimation;

        /**
         * Sound to play when a jump occurs
         */
        private SoundEffect mJumpSound;

        /**
         * Does the hero only start moving when we touch it?
         */
        private Vector2? mTouchAndGo;

        /**
         * For tracking the current amount of rotation of the hero
         */
        private float mCurrentRotation;

        /**
         * Construct a Hero by creating a PhysicsSprite and incrementing the number
         * of heroes created. This code should never be called directly by the game
         * designer.
         * 
         * @param width The width of the hero
         * @param height The height of the hero
         * @param imgName The name of the file that has the default image for this
         *            hero
         */
        private Hero(float width, float height, string imgName) : base(imgName, width, height)
        {
            Level.sCurrent.mScore.mHeroesCreated++;
        }

        /**
         * We can't just use the basic PhysicsSprite renderer, because we might need
         * to adjust a one-off animation (invincibility or throw) first
         * 
         * @param gameTime The current time since the game was launched
         */
        public override void Update(GameTime gameTime)
        {
            if (mThrowAnimationTimeRemaining > 0)
            {
                mThrowAnimationTimeRemaining -= gameTime.ElapsedGameTime.Milliseconds;
                if (mThrowAnimationTimeRemaining <= 0)
                {
                    mThrowAnimationTimeRemaining = 0;
                    mAnimator.setCurrentAnimation(mDefaultAnimation);
                }
            }

            if (mInvicibilityRemaining > 0)
            {
                mInvicibilityRemaining -= gameTime.ElapsedGameTime.Milliseconds;
                if (mInvicibilityRemaining <= 0)
                {
                    mInvicibilityRemaining = 0;
                    if (mInvincibleAnimation != null)
                    {
                        mAnimator.setCurrentAnimation(mDefaultAnimation);
                    }
                }
            }
            base.Update(gameTime);
        }

        /**
         * Make the hero jump, unless it is in the air and not multijump
         */
        internal void Jump()
        {
            if (mInAir)
            {
                return;
            }
            Vector2 v = new Vector2(mBody.LinearVelocity.X, mBody.LinearVelocity.Y);
            v.X += mJumpImpulses.X;
            v.Y += mJumpImpulses.Y;
            UpdateVelocity(v.X, v.Y);
            if (!mAllowMultiJump)
            {
                mInAir = true;
            }
            if (mJumpAnimation != null)
            {
                mAnimator.setCurrentAnimation(mJumpAnimation);
            }
            if (mJumpSound != null)
            {
                mJumpSound.Play();
            }
            mStickyDelay = DateTime.Now.AddMilliseconds(Lol.GlobalGameTime.ElapsedGameTime.TotalMilliseconds);
        }

        /**
         * Stop the jump animation for a hero, and make it eligible to jump again
         */
        private void StopJump()
        {
            if (mInAir || mAllowMultiJump)
            {
                mInAir = false;
                mAnimator.setCurrentAnimation (mDefaultAnimation);
            }
        }

        /**
         * When the hero is touched, we might need to take action. If so, we use
         * this to determine what to do.
         */
        internal override void HandleTouchDown(float x, float y)
        {
            if (mIsTouchJump)
            {
                Jump();
                return;
            }
            if (mTouchAndGo != null)
            {
                mHover = null;
                if (mBody.BodyType != BodyType.Dynamic)
                {
                    mBody.BodyType = BodyType.Dynamic;
                }
                SetAbsoluteVelocity(mTouchAndGo.Value.X, mTouchAndGo.Value.Y, false);
                mTouchAndGo = null;
                return;
            }
            base.HandleTouchDown(x, y);
        }

        /**
         * Internal method to make the hero's throw animation play while it is
         * throwing a projectile
         */
        internal void DoThrowAnimation()
        {
            if (mThrowAnimation != null)
            {
                mAnimator.setCurrentAnimation (mThrowAnimation);
                mThrowAnimationTimeRemaining = mThrowAnimateTotalLength;
            }
        }

        /**
         * Activated when the Hero is in crawl mode
         */
        internal void OnCrawl()
        {
            mCrawling = true;
            mBody.SetTransform(mBody.Position, -3.14159f / 2);
            if (mCrawlAnimation != null)
            {
                mAnimator.setCurrentAnimation(mCrawlAnimation);
            }
        }

        /**
         * Take the hero out of crawl mode
         */
        internal void CrawlOff()
        {
            mCrawling = false;
            mBody.SetTransform(mBody.Position, 0f);
            mAnimator.setCurrentAnimation (mDefaultAnimation);
        }

        /**
         * Put the hero in crawl mode. Note that we make the hero rotate when it is
         * crawling
         */
        internal void CrawlOn()
        {
            mCrawling = true;
            mBody.SetTransform(mBody.Position, 3.14159f / 2);
            if(mCrawlAnimation!=null)
                mAnimator.setCurrentAnimation(mCrawlAnimation);
        }

        /**
         * Change the rotation of the hero
         * 
         * @param delta How much to add to the current rotation
         */
        internal void IncreaseRotation(float delta)
        {
            if (mInAir)
            {
                mCurrentRotation += delta;
                mBody.AngularVelocity = 0;
                mBody.SetTransform(mBody.Position, mCurrentRotation);
            }
        }

        /**
         * The Hero is the dominant participant in all collisions. Whenever the hero
         * collides with something, we need to figure out what to do
         * 
         * @param other Other object involved in this collision
         * @param contact A description of the contact that caused this collision
         */
        internal override void OnCollide(PhysicsSprite other, Contact contact)
        {
            if (other is Enemy)
            {
                OnCollideWithEnemy((Enemy)other);
            }
            else if (other is Destination)
            {
                OnCollideWithDestination((Destination)other);
            }
            else if (other is Obstacle)
            {
                OnCollideWithObstacle((Obstacle)other, contact);
            }
            else if (other is Goodie)
            {
                OnCollideWithGoodie((Goodie)other);
            }
            // Need SVGSprite handler
        }

        /**
         * Dispatch method for handling Hero collisions with Enemies
         * 
         * @param e The enemy with which this hero collided
         */
        private void OnCollideWithEnemy(Enemy e)
        {
            if (e.mAlwaysDoesDamage)
            {
                Remove(false);
                Level.sCurrent.mScore.defeatHero(e);
                return;
            }
            if (mInvicibilityRemaining > 0)
            {
                if (e.mImmuneToInvicibility) { return; }
                e.Defeat(true);
            }
            else if (mCrawling && e.mDefeatByCrawl)
            {
                e.Defeat(true);
            }
            else if (mInAir && e.mDefeatByJump && YPosition > e.YPosition + e.mSize.Y / 2)
            {
                e.Defeat(true);
            }
            else if (e.mDamage >= mStrength)
            {
                Remove(false);
                Level.sCurrent.mScore.defeatHero(e);
            }
            else
            {
                AddStrength(-e.mDamage);
                e.Defeat(true);
            }
        }

        /**
         * Update the hero's strength, and then run any strength change callback
         * that has been registered
         * 
         * @param amount The amount to add (use a negative value to subtract)
         */
        private void AddStrength(int amount)
        {
            mStrength += amount;
            Lol.sGame.onStrengthChangeTrigger(Lol.sGame.mCurrLevelNum, this);
        }

        /**
         * Dispatch method for handling Hero collisions with Destinations
         * 
         * @param d The destination with which this hero collided
         */
        private void OnCollideWithDestination(Destination d)
        {
            bool match = true;
            for (int i = 0; i < 4; ++i)
            {
                match &= Level.sCurrent.mScore.mGoodiesCollected[i] >= d.mActivation[i];
            }

            if (match && (d.mHolding < d.mCapacity) && mVisible)
            {
                Remove(true);
                d.mHolding++;
                if (d.mArrivalSound != null)
                {
                    d.mArrivalSound.Play();
                }
                Level.sCurrent.mScore.onDestinationArrive();
            }
        }

        /**
         * Dispatch method for handling Hero collisions with Obstacles
         * 
         * @param o The obstacle with which this hero collided
         */
        private void OnCollideWithObstacle(Obstacle o, Contact c)
        {
            o.PlayCollideSound();

            if ((mCurrentRotation != 0) && !o.mBody.FixtureList[0].IsSensor)
            {
                IncreaseRotation(-mCurrentRotation);
            }
            if (o.mHeroCollision != null)
            {
                o.mHeroCollision(this, c);
            }
            if ((mInAir || mAllowMultiJump) && !o.mBody.FixtureList[0].IsSensor && !o.mNoJumpReenable)
            {
                StopJump();
            }
        }

        /**
         * Dispatch method for handling Hero collisions with Goodies
         * 
         * @param g The goodie with which this hero collided
         */
        private void OnCollideWithGoodie(Goodie g)
        {
            g.Remove(false);
            Level.sCurrent.mScore.onGoodieCollected(g);
            AddStrength(g.mStrengthBoost);
            if (g.mInvincibilityDuration > 0)
            {
                mInvicibilityRemaining += g.mInvincibilityDuration;
                if (mInvincibleAnimation != null)
                {
                    mAnimator.setCurrentAnimation (mInvincibleAnimation);
                }
            }
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Make a Hero with an underlying rectangular shape
         * 
         * @param x X coordinate of the hero
         * @param y Y coordinate of the hero
         * @param width width of the hero
         * @param height height of the hero
         * @param imgName File name of the default image to display
         * @return The hero that was created
         */
        public static Hero MakeAsBox(float x, float y, float width, float height, string imgName)
        {
            Hero h = new Hero(width, height, imgName);
            h.SetBoxPhysics(0, 0, 0, BodyType.Dynamic, false, x, y);
            Level.sCurrent.addSprite(h, 0);
            return h;
        }

        /**
         * Make a Hero with an underlying circular shape
         * 
         * @param x X coordinate of the hero
         * @param y Y coordinate of the hero
         * @param width width of the hero
         * @param height height of the hero
         * @param imgName File name of the default image to display
         * @return The hero that was created
         */
        public static Hero MakeAsCircle(float x, float y, float width, float height, string imgName)
        {
            float radius = Math.Max(width, height);
            Hero h = new Hero(width, height, imgName);
            h.SetCirclePhysics(0, 0, 0, BodyType.Dynamic, false, x, y, radius / 2);
            Level.sCurrent.addSprite(h, 0);
            return h;
        }

        /**
         * Give the hero more strength than the default, so it can survive more
         * collisions with enemies. Note that calling this will not run any strength
         * change callbacks... they only run in conjunction with collisions with
         * goodies or enemies.
         */
        public int Strength
        {
            set { mStrength = value; }
            get { return mStrength; }
        }

        /**
         * Indicate that upon a touch, this hero should begin moving with a specific
         * velocity
         * 
         * @param x Velocity in X dimension
         * @param y Velocity in Y dimension
         */
        public void SetTouchAndGo(float x, float y)
        {
            mTouchAndGo = new Vector2(x, y);
        }

        /**
         * Specify the X and Y velocity to give to the hero whenever it is
         * instructed to jump
         * 
         * @param x Velocity in X direction
         * @param y Velocity in Y direction
         */
        public void SetJumpImpulses(float x, float y)
        {
            mJumpImpulses = new Vector2(x, y);
        }

        /**
         * Indicate that this hero can jump while it is in the air
         */
        public void SetMultiJumpOn()
        {
            mAllowMultiJump = true;
        }

        /**
         * Indicate that touching this hero should make it jump
         */
        public void SetTouchToJump()
        {
            mIsTouchJump = true;
        }

        /**
         * Register an animation sequence, so that this hero can have a custom
         * animation while jumping
         */
        public Animation JumpAnimation
        {
            set { mJumpAnimation = value; }
        }

        /**
         * Set the sound to play when a jump occurs
         */
        public string JumpSound
        {
            set { mJumpSound = Media.getSound(value); }
        }

        /**
         * Register an animation sequence, so that this hero can have a custom
         * animation while throwing
         */
        public Animation ThrowAnimation
        {
            set
            {
                mThrowAnimation = value;
                mThrowAnimateTotalLength = 0;
                foreach (long l in value.mDurations)
                {
                    mThrowAnimateTotalLength += l;
                }
                mThrowAnimateTotalLength /= 1000;
            }
        }

        /**
         * Register an animation sequence, so that this hero can have a custom
         * animation while crawling
         */
        public Animation CrawlAnimation
        {
            set { mCrawlAnimation = value; }
        }

        /**
         * Register an animation sequence, so that this hero can have a custom
         * animation while invincible
         */
        public Animation InvicibleAnimation
        {
            set { mInvincibleAnimation = value; }
        }
    }
}
