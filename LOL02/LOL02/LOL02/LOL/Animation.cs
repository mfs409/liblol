using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LOL
{
    public class Animation
    {
        /**
         * A set of images, generated via registerAnimatableImage, that can be used
         * as frames of an animation.
         */
        private Texture2D[] mCells;

        /**
         * This array holds the indices that should be displayed.
         */
        public int[] mFrames;

        /**
         * This array holds the durations for which each of the indices should be
         * displayed
         */
        public long[] mDurations;

        /**
         * Should the animation repeat?
         */
        private bool mLoop;

        /**
         * The next available position in the frames and durations arrays. Note that
         * frames and durations should have the same length, and the same number of
         * entries.
         */
        public int mNextCell;

        /**
         * AnimationDriver is an internal class that PhysicsSprites can use to
         * figure out which frame of an animation to show next
         */
        public class AnimationDriver {
            /**
             * The images that comprise the current animation will be the elements
             * of this array
             */
            public Texture2D[] mImages;

            /**
             * The index to display from mImages for the case where there is no
             * active animation. This is useful for animateByGoodieCount.
             */
            public int mImageIndex;

            /**
             * The currently running animation
             */
            private Animation mCurrentAnimation;

            /**
             * The frame of the currently running animation that is being displayed
             */
            private int mCurrentAnimationFrame;

            /**
             * The amout of time for which the current frame has been displayed
             */
            private float mCurrentAnimationTime;

            /**
             * Build an AnimationDriver by giving it an imageName. This allows us to
             * use AnimationDriver for displaying non-animated images
             * 
             * @param imgName The name of the image file to use
             */
            public AnimationDriver(String imgName) {
                updateImage(imgName);
            }

            /**
             * Set the current animation, and reset internal fields
             * 
             * @param a The animation to start using
             */
            public void setCurrentAnimation(Animation a) {
                mCurrentAnimation = a;
                mCurrentAnimationFrame = 0;
                mCurrentAnimationTime = 0;
            }

            /**
             * Change the source for the default image to display
             * 
             * @param imgName The name of the image file to use
             */
            public void updateImage(String imgName) {
                mImages = Media.getImage(imgName);
                mImageIndex = 0;
            }

            /**
             * Change the index of the default image to display
             * 
             * @param i The index to use
             */
            public void setIndex(int i) {
                mImageIndex = i;
            }

            /**
             * Request a random index from the mImages array to pick an image to
             * display
             */
            public void pickRandomIndex() {
                mImageIndex = Util.getRandom(mImages.Length);
            }

            /**
             * When a PhysicsSprite renders, we use this method to figure out which
             * textureRegion to display
             * 
             * @param delta The time since the last render
             * @return The TextureRegion to display
             */
            public Texture2D getTr(float delta) {
                if (mCurrentAnimation == null) {
                    if (mImages == null)
                        return null;
                    return mImages[mImageIndex];
                }
                mCurrentAnimationTime += delta;
                long millis = (long)(1000 * mCurrentAnimationTime);
                // are we still in this frame?
                if (millis <= mCurrentAnimation.mDurations[mCurrentAnimationFrame]) {
                    return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
                }
                // are we on the last frame, with no loop? If so, stay where we
                // are...
                else if (mCurrentAnimationFrame == mCurrentAnimation.mNextCell - 1
                        && !mCurrentAnimation.mLoop) {
                    return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
                }
                // else advance, reset, go
                else {
                    mCurrentAnimationFrame = (mCurrentAnimationFrame + 1) % mCurrentAnimation.mNextCell;
                    mCurrentAnimationTime = 0;
                    return mCurrentAnimation.mCells[mCurrentAnimation.mFrames[mCurrentAnimationFrame]];
                }
            }
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Create an animation. The animation can hold up to sequenceCount steps,
         * but none will be initialized yet. You will need to use the "to" method to
         * initialize the steps.
         * 
         * @param imgName The animate-able image that should be used
         * @param sequenceCount The number of frames in the animation
         * @param repeat Either true or false, depending on whether the animation
         *            should repeat
         */
        public Animation(String imgName, int sequenceCount, bool repeat) {
            mCells = Media.getImage(imgName);
            mFrames = new int[sequenceCount];
            mDurations = new long[sequenceCount];
            mLoop = repeat;
            mNextCell = 0;
        }

        /**
         * Create an animation where all of the frames are displayed for the same
         * amount of time
         * 
         * @param imgName The animate-able image that should be used
         * @param timePerFrame The time in milliseconds that each frame should be
         *            shown
         * @param repeat true or false, depending on whether the animation should
         *            repeat
         * @param frameIndices The indices of the image that should each be shown
         *            for timePerFrame milliseconds
         */
        public Animation(String imgName, int timePerFrame, bool repeat, params int[] frameIndices) {
            mCells = Media.getImage(imgName);
            mFrames = new int[frameIndices.Length];
            mDurations = new long[frameIndices.Length];
            mLoop = repeat;
            mNextCell = frameIndices.Length;
            for (int i = 0; i < mNextCell; ++i) {
                mDurations[i] = timePerFrame;
                mFrames[i] = frameIndices[i];
            }
        }

        /**
         * Add another step to the animation
         * 
         * @param frame The index within the image that should be displayed next
         * @param duration The time in milliseconds that this frame should be shown
         * @return the Animation, so that we can chain calls to "to()"
         */
        public Animation to(int frame, long duration) {
            mFrames[mNextCell] = frame;
            mDurations[mNextCell] = duration;
            mNextCell++;
            return this;
        }
    }
}
