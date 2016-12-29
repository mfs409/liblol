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

/**
 * Animation is a way of describing a set of images that can be used to animate
 * an actor. Animations consist of regions of an image, the indices that should
 * be played, and the time each should be shown. For example, suppose there is
 * an image "glowball.png" with 5 frames. The "cells" will consist of the 5
 * frames (numbered 0 to 4) that can be shown. We can then set an animation such
 * as (0,300)-&gt;(2,100)-&gt;(0,600)-&gt;(4,100) to indicate that the animation should
 * show the 0th frame for 300 milliseconds, then the 2nd frame for 100
 * milliseconds, then the 0th frame for 600 milliseconds, then the 4th frame for
 * 100 milliseconds.
 * <p>
 * There are two ways to make an animation. The more powerful uses to() to chain
 * together frame/duration pairs. The less powerful uses a constructor with more
 * parameters to define the entire animation in equal-duration pieces.
 */
public class Animation {
    /// A reference to the media object, so we can find assets easily
    Media mMedia;

    /// A set of images, generated via registerAnimatableImage, that can be used as frames of an
    // animation.
    final TextureRegion[] mCells;

    /// Should the animation repeat?
    final boolean mLoop;

    /// This array holds the durations for which each of the images should be displayed
    long[] mDurations;

    /// The next available position in the frames and durations arrays. Note that frames and
    // durations should have the same length, and the same number of entries.
    int mNextCell;

    /**
     * Create an animation. The animation can hold up to sequenceCount steps,
     * but none will be initialized yet. You will need to use the "to" method to
     * initialize the steps.
     *
     * @param sequenceCount The number of frames in the animation
     * @param repeat        Either true or false, depending on whether the animation
     *                      should repeat
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
     * @param timePerFrame The time in milliseconds that each frame should be shown
     * @param repeat       true or false, depending on whether the animation should
     *                     repeat
     * @param imgNames     The names of the images that should each be shown for
     *                     timePerFrame milliseconds
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
     * Add another step to the animation
     *
     * @param imgName  The name of the image to display next
     * @param duration The time in milliseconds that this frame should be shown
     * @return the Animation, so that we can chain calls to "to()"
     */
    public Animation to(String imgName, long duration) {
        mCells[mNextCell] = mMedia.getImage(imgName);
        mDurations[mNextCell] = duration;
        mNextCell++;
        return this;
    }
}