using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    // Conversion complete!
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

            internal AnimationDriver(string imgName)
            {
                Image = imgName;
            }

            /*internal void SetCurrentAnimation(Animation a)
            {
                mCurrentAnimation = a;
                mCurrentAnimationFrame = 0;
                mCurrentAnimationTime = 0;
            }*/

            internal Animation CurrentAnimation
            {
                set
                {
                    mCurrentAnimation = value;
                    mCurrentAnimationFrame = 0;
                    mCurrentAnimationTime = 0;
                }
            }

            /*internal void UpdateImage(string imgName)
            {
                mImages = Media.GetImage(imgName);
                mImageIndex = 0;
            }*/

            internal string Image
            {
                set
                {
                    mImages = Media.GetImage(value);
                    mImageIndex = 0;
                }
            }

            /*internal void SetIndex(int i)
            {
                mImageIndex = i;
            }*/

            internal int Index
            {
                set { mImageIndex = value; }
            }

            internal void PickRandomIndex()
            {
                mImageIndex = Util.GetRandom(mImages.Length);
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

        public Animation(string imgName, int sequenceCount, bool repeat)
        {
            mCells = Media.GetImage(imgName);
            mFrames = new int[sequenceCount];
            mDurations = new long[sequenceCount];
            mLoop = repeat;
            mNextCell = 0;
        }

        public Animation(string imgName, int timePerFrame, bool repeat, params int[] frameIndices)
        {
            mCells = Media.GetImage(imgName);
            mFrames = new int[frameIndices.Length];
            mDurations = new long[frameIndices.Length];
            mLoop = repeat;
            mNextCell = frameIndices.Length;
            for (int i = 0; i < mNextCell; ++i)
            {
                mDurations[i] = timePerFrame;
                mFrames[i] = frameIndices[i];
            }
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
