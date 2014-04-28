using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;

namespace LibLOL
{
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

        internal ProjectilePool(int size, float width, float height, String imgName,
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
    }
}
