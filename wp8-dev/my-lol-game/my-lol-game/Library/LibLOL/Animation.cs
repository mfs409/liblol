using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    public class Animation
    {
        private readonly Texture2D[] mCells;

        internal int[] mFrames;

        internal long[] mDurations;

        private readonly bool mLoop;

        internal int mNextCell;

        internal class AnimationDriver
        {
            internal Texture2D[] mImages;

            internal int mImageIndex;

            private Animation mCurrentAnimation;

            private int mCurrentAnimationFrame;

            private float mCurrentAnimationTime;

            internal AnimationDriver(String imgName)
            {
                UpdateImage(imgName);
            }

            internal void SetCurrentAnimation(Animation a)
            {
                mCurrentAnimation = a;
                mCurrentAnimationFrame = 0;
                mCurrentAnimationTime = 0;
            }

            internal void UpdateImage(String imgName)
            {
                mImages = Media.GetImage(imgName);
                mImageIndex = 0;
            }

            internal void SetIndex(int i)
            {
                mImageIndex = i;
            }

            internal void PickRandomIndex()
            {

            }

            internal Texture2D GetTr(GameTime gameTime)
            {
                if (mCurrentAnimation == null)
                {
                    if (mImages == null)
                    {
                        return null;
                    }
                    return mImages[mImageIndex];
                }
                mCurrentAnimationTime += (float)gameTime.ElapsedGameTime.TotalSeconds;
                long millis = (long)(1000 * mCurrentAnimationTime);
                if (millis <= mCurrentAnimation.mDurations[mCurrentAnimationFrame])
                {
                    return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
                }
                else if (mCurrentAnimationFrame == mCurrentAnimation.mNextCell - 1 && !mCurrentAnimation.mLoop)
                {
                    return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
                }
                else
                {
                    mCurrentAnimationFrame = (mCurrentAnimationFrame + 1) % mCurrentAnimation.mNextCell;
                    mCurrentAnimationTime = 0;
                    return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
                }
            }
        }

        public Animation(String imgName, int sequenceCount, bool repeat)
        {

        }

        public Animation(String imgName, int timePerFrame, bool repeat, params int[] frameIndices)
        {
            
        }

        public Animation To(int frame, long duration)
        {
            mFrames[mNextCell] = frame;
            mDurations[mNextCell] = duration;
            mNextCell++;
            return this;
        }
    }
}
