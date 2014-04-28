﻿using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;

using FarseerPhysics.Dynamics;
using FarseerPhysics.Dynamics.Contacts;

namespace LibLOL
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

        private Hero(float width, float height, String imgName) : base(imgName, width, height)
        {
            // Level.sCurrent.mScore.mHeroesCreated++;
        }

        internal override void Update(GameTime gameTime)
        {
            if (mThrowAnimationTimeRemaining > 0)
            {
                mThrowAnimationTimeRemaining -= gameTime.ElapsedGameTime.Milliseconds;
                if (mThrowAnimationTimeRemaining <= 0)
                {
                    mThrowAnimationTimeRemaining = 0;
                    mAnimator.SetCurrentAnimation(mDefaultAnimation);
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
                        mAnimator.SetCurrentAnimation(mDefaultAnimation);
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
                mAnimator.SetCurrentAnimation(mJumpAnimation);
            }
            if (mJumpSound != null)
            {
                mJumpSound.Play();
            }
            mStickyDelay = Lol.GlobalGameTime.ElapsedTicks + (long)(0.01 * System.Diagnostics.Stopwatch.Frequency);


        }

        private void StopJump()
        {
            if (mInAir || mAllowMultiJump)
            {
                mInAir = false;
                mAnimator.SetCurrentAnimation(mDefaultAnimation);
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
                mAnimator.SetCurrentAnimation(mThrowAnimation);
                mThrowAnimationTimeRemaining = mThrowAnimateTotalLength;
            }
        }

        internal void OnCrawl()
        {
            mCrawling = true;
            mBody.SetTransform(mBody.Position, -3.14159f / 2);
            if (mCrawlAnimation != null)
            {
                mAnimator.SetCurrentAnimation(mCrawlAnimation);
            }
        }

        internal void CrawlOff()
        {
            mCrawling = false;
            mBody.SetTransform(mBody.Position, 0f);
            mAnimator.SetCurrentAnimation(mDefaultAnimation);
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

            }
            else if (other is Goodie)
            {

            }
        }

        private void OnCollideWithEnemy(Enemy e)
        {

        }

        private void OnCollideWithDestination(Destination d)
        {
            System.Diagnostics.Debug.WriteLine("OnCollideWithDestination");
        }

        private void OnCollideWithObstacle(Obstacle o, Contact c)
        {

        }

        private void OnCollideWithGoodie(Goodie g)
        {

        }

        public static Hero MakeAsBox(float x, float y, float width, float height, String imgName)
        {
            Hero h = new Hero(width, height, imgName);
            h.SetBoxPhysics(0, 0, 0, BodyType.Dynamic, false, x, y);
            // Level.sCurrent.AddSprite(h, 0);
            return h;
        }

        public static Hero MakeAsCircle(float x, float y, float width, float height, String imgName)
        {
            float radius = Math.Max(width, height);
            Hero h = new Hero(width, height, imgName);
            h.SetCirclePhysics(0, 0, 0, BodyType.Dynamic, false, x, y, radius / 2);
            // Leve.sCurrent.AddSprite(h, 0);
            return h;
        }

        public void SetStrength(int amount)
        {
            mStrength = amount;

        }

        public int GetStrength()
        {
            return mStrength;
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

        public void SetJumpAnimation(Animation a)
        {
            mJumpAnimation = a;
        }

        public void SetJumpSound(String soundName)
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
        }
    }
}
