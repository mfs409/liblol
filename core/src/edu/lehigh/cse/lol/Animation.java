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
 * Animation is a way of describing a set of images that can be used to do flip-book animation on
 * any BaseActor.  Animations consist of the names of images, and the time that each should be
 * shown.
 * <p>
 * There are two ways to make an animation. The more powerful uses to() to chain
 * together frame/duration pairs. The less powerful uses a constructor with more
 * parameters to define the entire animation in equal-duration pieces.
 */
public class Animation {
    /// A reference to the media object, so we can find images easily
    Media mMedia;
    /// A set of images that can be used as frames of an animation.
    final TextureRegion[] mCells;
    /// Should the animation repeat?
    final boolean mLoop;
    /// This array holds the durations for which each of the images should be displayed
    long[] mDurations;
    /// The next available position in the frames and durations arrays. Note that frames and
    /// durations should have the same length, and the same number of entries.
    int mNextCell;

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
}