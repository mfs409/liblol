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

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Animation is a way of describing a set of images that can be used to animate
 * an entity. Animations consist of regions of an image, the indices that should
 * be played, and the time each should be shown. For example, suppose there is
 * an image "glowball.png" with 5 frames. The "cells" will consist of the 5
 * frames (numbered 0 to 4) that can be shown. We can then set an animation such
 * as (0,300)->(2,100)->(0,600)->(4,100) to indicate that the animation should
 * show the 0th frame for 300 milliseconds, then the 2nd frame for 100
 * milliseconds, then the 0th frame for 600 milliseconds, then the 4th frame for
 * 100 milliseconds.
 */
public class Animation {
    /**
     * A set of images, generated via registerAnimatableImage, that can be used
     * as frames of an animation.
     */
    private final TextureRegion[] mCells;

    /**
     * This array holds the indices that should be displayed.
     */
    int[] mFrames;

    /**
     * This array holds the durations for which each of the indices should be
     * displayed
     */
    long[] mDurations;

    /**
     * Should the animation repeat?
     */
    private final boolean mLoop;

    /**
     * The next available position in the frames and durations arrays. Note that
     * frames and durations should have the same length, and the same number of
     * entries.
     */
    int mNextCell;

    /**
     * AnimationDriver is an internal class that PhysicsSprites can use to
     * figure out which frame of an animation to show next
     */
    static class AnimationDriver {
        /**
         * The images that comprise the current animation will be the elements
         * of this array
         */
        TextureRegion[] mImages;

        /**
         * The index to display from mImages for the case where there is no
         * active animation. This is useful for animateByGoodieCount.
         */
        int mImageIndex;

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
        AnimationDriver(String imgName) {
            updateImage(imgName);
        }

        /**
         * Set the current animation, and reset internal fields
         * 
         * @param a The animation to start using
         */
        void setCurrentAnimation(Animation a) {
            mCurrentAnimation = a;
            mCurrentAnimationFrame = 0;
            mCurrentAnimationTime = 0;
        }

        /**
         * Change the source for the default image to display
         * 
         * @param imgName The name of the image file to use
         */
        void updateImage(String imgName) {
            mImages = Media.getImage(imgName);
            mImageIndex = 0;
        }

        /**
         * Change the index of the default image to display
         * 
         * @param i The index to use
         */
        void setIndex(int i) {
            mImageIndex = i;
        }

        /**
         * Request a random index from the mImages array to pick an image to
         * display
         */
        void pickRandomIndex() {
            mImageIndex = Util.getRandom(mImages.length);
        }

        /**
         * When a PhysicsSprite renders, we use this method to figure out which
         * textureRegion to display
         * 
         * @param delta The time since the last render
         * @return The TextureRegion to display
         */
        TextureRegion getTr(float delta) {
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
    public Animation(String imgName, int sequenceCount, boolean repeat) {
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
    public Animation(String imgName, int timePerFrame, boolean repeat, int... frameIndices) {
        mCells = Media.getImage(imgName);
        mFrames = new int[frameIndices.length];
        mDurations = new long[frameIndices.length];
        mLoop = repeat;
        mNextCell = frameIndices.length;
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
