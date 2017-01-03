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

import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

/**
 * Animation is a way of describing a set of images that can be used to do flip-book animation on
 * any actor.  Animations consist of the names of images, and the time that each should be shown.
 * <p>
 * There are two ways to make an animation. The more powerful uses to() to chain
 * together frame/duration pairs. The less powerful uses a constructor with more
 * parameters to define the entire animation in equal-duration pieces.
 */
public class Animation {
    /// A reference to the media object, so we can find images easily
    private Media mMedia;
    /// A set of images that can be used as frames of an animation.
    private final TextureRegion[] mCells;
    /// Should the animation repeat?
    private final boolean mLoop;
    /// This array holds the durations for which each of the images should be displayed
    private long[] mDurations;
    /// The next available position in the frames and durations arrays. Note that frames and
    /// durations should have the same length, and the same number of entries.
    private int mNextCell;

    /**
     * Create the shell of a complex animation. The animation can hold up to sequenceCount steps,
     * but none will be initialized yet. After constructing like this, a programmer should use the
     * "to" method to initialize the steps.
     *
     * @param media         The Media object, with references to all images that comprise the game
     * @param sequenceCount The number of frames in the animation
     * @param repeat        Either true or false, depending on whether the animation should repeat
     */
    Animation(Media media, int sequenceCount, boolean repeat) {
        mMedia = media;
        mCells = new TextureRegion[sequenceCount];
        mDurations = new long[sequenceCount];
        mLoop = repeat;
        mNextCell = 0;
    }

    /**
     * Create an animation where all of the frames are displayed for the same
     * amount of time
     *
     * @param media        The Media object, with references to all images that comprise the game
     * @param timePerFrame The time in milliseconds that each frame should be shown
     * @param repeat       Either true or false, depending on whether the animation should repeat
     * @param imgNames     The names of the images that should be shown
     */
    Animation(Media media, int timePerFrame, boolean repeat, String... imgNames) {
        mMedia = media;
        mCells = new TextureRegion[imgNames.length];
        mDurations = new long[imgNames.length];
        mLoop = repeat;
        mNextCell = imgNames.length;
        for (int i = 0; i < mNextCell; ++i) {
            mDurations[i] = timePerFrame;
            mCells[i] = mMedia.getImage(imgNames[i]);
        }
    }

    /**
     * Get the duration of the entire animation sequence
     * @return The duration, in milliseconds
     */
    long getDuration() {
        long result = 0;
        for (long l : mDurations)
            result += l;
        return result;
    }

    /**
     * Add a step to an animation
     *
     * @param imgName  The name of the image to add to the animation
     * @param duration The time in milliseconds that this image should be shown
     * @return the Animation, so that we can chain calls to "to()"
     */
    public Animation to(String imgName, long duration) {
        mCells[mNextCell] = mMedia.getImage(imgName);
        mDurations[mNextCell] = duration;
        mNextCell++;
        return this;
    }

    /**
     * Driver is an internal class that actors can use to figure out which frame of an
     * animation to show next
     */
    static class Driver {
        /// The currently running animation
        Animation mCurrentAnimation;
        /// The images that comprise the current animation will be the elements of this array
        private TextureRegion[] mImages;
        /// The index to display from <code>mImages</code>, for the case where there is no active
        /// animation. This is useful for animateByGoodieCount.
        private int mImageIndex;
        /// The frame of the currently running animation that is being displayed
        private int mActiveFrame;
        /// The amount of time for which the current frame has been displayed
        private float mElapsedTime;

        /**
         * Build a Driver by giving it an image name. This allows us to use Driver to
         * display non-animated images
         *
         * @param media   The media object, with all of the game's loaded images
         * @param imgName The name of the image file to use
         */
        Driver(Media media, String imgName) {
            updateImage(media, imgName);
        }

        /**
         * Set the current animation, so that it will start running
         *
         * @param animation The animation to start using
         */
        void setCurrentAnimation(Animation animation) {
            mCurrentAnimation = animation;
            mActiveFrame = 0;
            mElapsedTime = 0;
        }

        /**
         * Change the source for the default image to display
         *
         * @param media   The media object, with all of the game's loaded images
         * @param imgName The name of the image file to use
         */
        void updateImage(Media media, String imgName) {
            if (mImages == null)
                mImages = new TextureRegion[1];
            mImages[0] = media.getImage(imgName);
            mImageIndex = 0;
        }

        /**
         * Request a random index from the mImages array to pick an image to display
         *
         * @param generator A random number generator.  We pass in the game's generator, so that we
         *                  can keep the length of the mImages array private
         */
        void updateIndex(Random generator) {
            mImageIndex = generator.nextInt(mImages.length);
        }

        /**
         * When an actor renders, we use this method to figure out which image to display
         *
         * @param delta The time since the last render
         * @return The TextureRegion to display
         */
        TextureRegion getTr(float delta) {
            // If we're in 'show a specific image' mode, then show an image
            if (mCurrentAnimation == null) {
                return (mImages == null) ? null : mImages[mImageIndex];
            }
            // Advance the time
            mElapsedTime += delta;
            long millis = (long) (1000 * mElapsedTime);
            // are we still in this frame?
            if (millis <= mCurrentAnimation.mDurations[mActiveFrame]) {
                return mCurrentAnimation.mCells[mActiveFrame];
            }
            // are we on the last frame, with no loop? If so, stay where we are
            else if (mActiveFrame == mCurrentAnimation.mNextCell - 1 && !mCurrentAnimation.mLoop) {
                return mCurrentAnimation.mCells[mActiveFrame];
            }
            // advance the animation and start its timer from zero
            mActiveFrame = (mActiveFrame + 1) % mCurrentAnimation.mNextCell;
            mElapsedTime = 0;
            return mCurrentAnimation.mCells[mActiveFrame];
        }
    }
}