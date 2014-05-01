using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;

namespace LibLOL
{
    // Conversion done!
    public class ProjectilePool
    {
        private Vector2 mVelocity = new Vector2();

        private Vector2 mOffset = new Vector2();

        private float mVectorDamp;

        private bool mSensorProjectiles;

        private bool mEnableFixedVectorVelocity;

        private float mFixedVectorVelocity;

        private bool mRotateVectorThrow;

        private Projectile[] mPool;

        private readonly int mPoolSize;

        private int mNextIndex;

        internal int mProjectilesRemaining;

        private bool mRandomizeImages;

        private SoundEffect mThrowSound;

        private SoundEffect mProjectileDisappearSound;

        internal ProjectilePool(int size, float width, float height, string imgName,
            float velocityX, float velocityY, float offsetX, float offsetY, int strength,
            int zIndex, bool isCircle)
        {
            mPool = new Projectile[size];
            for (int i = 0; i < size; ++i)
            {
                mPool[i] = new Projectile(width, height, imgName, -100 - i * width, -100 - i * height, zIndex, isCircle);
                mPool[i].mVisible = false;
                mPool[i].mBody.IsBullet = true;
                mPool[i].mBody.Enabled = false;
                mPool[i].mStrength = strength;
            }
            mNextIndex = 0;
            mPoolSize = size;
            mVelocity.X = velocityX;
            mVelocity.Y = velocityY;
            mOffset.X = offsetX;
            mOffset.Y = offsetY;
            mThrowSound = null;
            mProjectileDisappearSound = null;
            mProjectilesRemaining = -1;
            mSensorProjectiles = true;
        }

        internal void ThrowFixed(Hero h)
        {
            if (mProjectilesRemaining == 0)
            {
                return;
            }
            if (mProjectilesRemaining != -1)
            {
                --mProjectilesRemaining;
            }
            if (mPool[mNextIndex].mVisible)
            {
                return;
            }
            Projectile b = mPool[mNextIndex];
            mNextIndex = (mNextIndex + 1) % mPoolSize;
            b.CollisionEffect = !mSensorProjectiles;
            if (mRandomizeImages)
            {
                b.mAnimator.PickRandomIndex();
            }
            b.mRangeFrom.X = h.XPosition + mOffset.X;
            b.mRangeFrom.Y = h.YPosition + mOffset.Y;
            b.mBody.Enabled = true;
            b.mBody.SetTransform(b.mRangeFrom, 0);
            b.UpdateVelocity(mVelocity.X, mVelocity.Y);
            b.mVisible = true;
            if (mThrowSound != null)
            {
                mThrowSound.Play();
            }
            b.mDisappearSound = mProjectileDisappearSound;
            h.DoThrowAnimation();
        }

        internal void ThrowAt(float heroX, float heroY, float toX, float toY, Hero h)
        {
            if (mProjectilesRemaining == 0)
            {
                return;
            }
            if (mProjectilesRemaining != -1)
            {
                --mProjectilesRemaining;
            }
            if (mPool[mNextIndex].mVisible)
            {
                return;
            }
            Projectile b = mPool[mNextIndex];
            mNextIndex = (mNextIndex + 1) % mPoolSize;
            b.CollisionEffect = !mSensorProjectiles;
            if (mRandomizeImages)
            {
                b.mAnimator.PickRandomIndex();
            }
            b.mRangeFrom.X = heroX + mOffset.X;
            b.mRangeFrom.Y = heroX + mOffset.Y;
            b.mBody.Enabled = true;
            b.mBody.SetTransform(b.mRangeFrom, 0);

            if (mEnableFixedVectorVelocity)
            {
                float dX = toX - heroX - mOffset.X;
                float dY = toY - heroY - mOffset.Y;
                float hypotenuse = (float)Math.Sqrt(dX * dX + dY * dY);
                float tmpX = dX / hypotenuse;
                float tmpY = dY / hypotenuse;
                tmpX *= mFixedVectorVelocity;
                tmpY *= mFixedVectorVelocity;
                b.UpdateVelocity(tmpX, tmpY);
            }
            else
            {
                float dX = toX - heroX - mOffset.X;
                float dY = toY - heroY - mOffset.Y;
                float tmpX = dX * mVectorDamp;
                float tmpY = dY * mVectorDamp;
                b.UpdateVelocity(tmpX, tmpY);
            }

            if (mRotateVectorThrow)
            {
                double angle = Math.Atan2(toY - heroY - mOffset.Y, toX - heroX - mOffset.X)
                    - Math.Atan2(-1, 0);
                b.mBody.SetTransform(b.mBody.Position, (float)angle);
            }

            b.mVisible = true;
            if (mThrowSound != null)
            {
                mThrowSound.Play();
            }
            b.mDisappearSound = mProjectileDisappearSound;
            h.DoThrowAnimation();
        }

        public static float Range
        {
            set
            {
                foreach (Projectile p in Level.sCurrent.mProjectilePool.mPool)
                {
                    p.mRange = value;
                }
            }
        }

        public static void SetProjectileGravityOn()
        {
            foreach (Projectile p in Level.sCurrent.mProjectilePool.mPool)
            {
                p.mBody.GravityScale = 1;
            }
        }

        public static string ImageSource
        {
            set
            {
                foreach (Projectile p in Level.sCurrent.mProjectilePool.mPool)
                {
                    p.mAnimator.Image = value;
                }
                Level.sCurrent.mProjectilePool.mRandomizeImages = true;
            }
        }

        public static float ProjectileVectorDampeningFactor
        {
            set { Level.sCurrent.mProjectilePool.mVectorDamp = value; }
        }

        public static void EnableCollisionsForProjectiles()
        {
            Level.sCurrent.mProjectilePool.mSensorProjectiles = false;
        }

        public static float FixedVectorThrowVelocity
        {
            set
            {
                Level.sCurrent.mProjectilePool.mEnableFixedVectorVelocity = true;
                Level.sCurrent.mProjectilePool.mFixedVectorVelocity = value;
            }
        }

        public static void SetRotateVectorThrow()
        {
            Level.sCurrent.mProjectilePool.mRotateVectorThrow = true;
        }

        public static void SetCollisionOk()
        {
            foreach (Projectile p in Level.sCurrent.mProjectilePool.mPool)
            {
                p.mDisappearOnCollide = false;
            }
        }

        public static void Configure(int size, float width, float height, string imgName,
            float velocityX, float velocityY, float offsetX, float offsetY, int strength,
            int zIndex, bool isCircle)
        {
            Level.sCurrent.mProjectilePool = new ProjectilePool(size, width, height, imgName, velocityX,
                velocityY, offsetX, offsetY, strength, zIndex, isCircle);
        }

        public static int NumberOfProjectiles
        {
            set
            {
                Level.sCurrent.mProjectilePool.mProjectilesRemaining = value;
            }
        }

        public static string ThrowSound
        {
            set
            {
                Level.sCurrent.mProjectilePool.mThrowSound = Media.GetSound(value);
            }
        }

        public static string ProjectileDisappearSound
        {
            set
            {
                Level.sCurrent.mProjectilePool.mProjectileDisappearSound =
                    Media.GetSound(value);
            }
        }

        public static Animation Animation
        {
            set
            {
                foreach (Projectile p in Level.sCurrent.mProjectilePool.mPool)
                {
                    p.DefaultAnimation = value;
                }
            }
        }
    }
}
