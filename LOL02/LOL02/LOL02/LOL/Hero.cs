using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LOL
{
    public class Hero : PhysicsSprite
    {
        private int mStrength = 1;

        private double mInvicibilityRemaining;

        private Animation mInvincibleAnimation;

        private bool mCrawling;

        private Animation mCrawlAnimation;

        private Animation mThrowAnimation;

        private double mThrowAnimateTotalLength;

        private double mThrowAnimationTimeRemaining;

        private bool mInAir;

        private Vector2 mJumpImpulses;

        private bool mAllowMultiJump;

        private bool mIsTouchJump;

        private Animation mJumpAnimation;

        private SoundEffect mJumpSound;

        private Vector2? mTouchAndGo;

        private float mCurrentRotation;

        private Hero(float width, float height, string imgName) : base(imgName, width, height)
        {
            Level.sCurrent.mScore.mHeroesCreated++;
        }

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
            // System.Diagnostics.Stopwatch doesn't exist (tls)
            //mStickyDelay = Lol.GlobalGameTime.ElapsedTicks + (long)(0.01 * FarseerPhysics.Common.Stopwatch.Frequency);
            mStickyDelay = Lol.GlobalGameTime.ElapsedTicks;
        }

        private void StopJump()
        {
            if (mInAir || mAllowMultiJump)
            {
                mInAir = false;
                mAnimator.setCurrentAnimation (mDefaultAnimation);
            }
        }

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

        internal void DoThrowAnimation()
        {
            if (mThrowAnimation != null)
            {
                mAnimator.setCurrentAnimation (mThrowAnimation);
                mThrowAnimationTimeRemaining = mThrowAnimateTotalLength;
            }
        }

        internal void OnCrawl()
        {
            mCrawling = true;
            mBody.SetTransform(mBody.Position, -3.14159f / 2);
            if (mCrawlAnimation != null)
            {
                mAnimator.setCurrentAnimation(mCrawlAnimation);
            }
        }

        internal void CrawlOff()
        {
            mCrawling = false;
            mBody.SetTransform(mBody.Position, 0f);
            mAnimator.setCurrentAnimation (mDefaultAnimation);
        }

        internal void CrawlOn()
        {
            mCrawling = true;
            mBody.SetTransform(mBody.Position, 3.14159f / 2);
            if(mCrawlAnimation!=null)
                mAnimator.setCurrentAnimation(mCrawlAnimation);
        }

        internal void IncreaseRotation(float delta)
        {
            if (mInAir)
            {
                mCurrentRotation += delta;
                mBody.AngularVelocity = 0;
                mBody.SetTransform(mBody.Position, mCurrentRotation);
            }
        }

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

        private void AddStrength(int amount)
        {
            mStrength += amount;
            Lol.sGame.onStrengthChangeTrigger(Lol.sGame.mCurrLevelNum, this);
        }

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

        public static Hero MakeAsBox(float x, float y, float width, float height, string imgName)
        {
            Hero h = new Hero(width, height, imgName);
            h.SetBoxPhysics(0, 0, 0, BodyType.Dynamic, false, x, y);
            Level.sCurrent.addSprite(h, 0);
            return h;
        }

        public static Hero MakeAsCircle(float x, float y, float width, float height, string imgName)
        {
            float radius = Math.Max(width, height);
            Hero h = new Hero(width, height, imgName);
            h.SetCirclePhysics(0, 0, 0, BodyType.Dynamic, false, x, y, radius / 2);
            Level.sCurrent.addSprite(h, 0);
            return h;
        }

        /*public void SetStrength(int amount)
        {
            mStrength = amount;
        }

        public int GetStrength()
        {
            return mStrength;
        }*/

        public int Strength
        {
            set { mStrength = value; }
            get { return mStrength; }
        }

        public void SetTouchAndGo(float x, float y)
        {
            mTouchAndGo = new Vector2(x, y);
        }

        public void SetJumpImpulses(float x, float y)
        {
            mJumpImpulses = new Vector2(x, y);
        }

        public void SetMultiJumpOn()
        {
            mAllowMultiJump = true;
        }

        public void SetTouchToJump()
        {
            mIsTouchJump = true;
        }

        /*public void SetJumpAnimation(Animation a)
        {
            mJumpAnimation = a;
        }

        public void SetJumpSound(string soundName)
        {

        }

        public void SetThrowAnimation(Animation a)
        {
            mThrowAnimation = a;

            mThrowAnimateTotalLength = 0;
            foreach (long l in a.mDurations)
            {
                mThrowAnimateTotalLength += l;
            }
            mThrowAnimateTotalLength /= 1000;
        }

        public void SetCrawlAnimation(Animation a)
        {
            mCrawlAnimation = a;
        }

        public void SetInvicibleAnimation(Animation a)
        {
            mInvincibleAnimation = a;
        }*/

        public Animation JumpAnimation
        {
            set { mJumpAnimation = value; }
        }

        public string JumpSound
        {
            set { mJumpSound = Media.getSound(value); }
        }

        public Animation ThrowAnimation
        {
            set { mThrowAnimation = value; }
        }

        public Animation CrawlAnimation
        {
            set { mCrawlAnimation = value; }
        }

        public Animation InvicibleAnimation
        {
            set { mInvincibleAnimation = value; }
        }
    }
}
