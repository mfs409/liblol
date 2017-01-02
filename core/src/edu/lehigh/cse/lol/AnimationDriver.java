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
 * AnimationDriver is an internal class that actors can use to figure out which frame of an
 * animation to show next
 */
class AnimationDriver {
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
     * Build an AnimationDriver by giving it an image name. This allows us to use AnimationDriver to
     * display non-animated images
     *
     * @param media   The media object, with all of the game's loaded images
     * @param imgName The name of the image file to use
     */
    AnimationDriver(Media media, String imgName) {
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
     * @param generator A random number generator.  We pass in the game's generator, so that we can
     *                  keep the length of the mImages array private
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