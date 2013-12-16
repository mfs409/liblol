package edu.lehigh.cse.lol;

// TODO: complete the "easy" constructor

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Animation is a way of describing a set of images that can be used to animate an entity.
 * 
 * Animations consist of regions of an image, the indices that should be played, and the time each should be shown. For
 * example, suppose there is an image "glowball.png" with 5 frames. The "cells" will consist of the 5 frames (numbered 0
 * to 4) that can be shown. We can then set an animation such as (0,300)->(2,100)->(0,600)->(4,100) to indicate that the
 * animation should show the 0th frame for 300 milliseconds, then the 2nd frame for 100 milliseconds., then the 0th
 * frame for 600 milliseconds, then the 4th frame for 100 milliseconds.
 */
public class Animation
{
    /**
     * A set of images, generated via registerAnimatableImage, that can be used as frames of an animation.
     */
    TextureRegion[] _cells;

    /**
     * This array holds the indices that should be displayed.
     */
    int[]           _frames;

    /**
     * This array holds the durations for which each of the indices should be displayed
     */
    long[]          _durations;

    /**
     * Should the animation repeat?
     */
    boolean         _loop;

    /**
     * The next available position in the _frames and _durations arrays. Note that _frames and _durations should have
     * the same length, and the same number of entries.
     */
    int             _nextCell;

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Create an animation. The animation can hold up to sequenceCount steps, but none will be initialized yet. You will
     * need to use the "to" method to initialize the steps.
     * 
     * @param imgName
     *            The animatable image that should be used
     * @param sequenceCount
     *            The number of frames in the animation
     * @param repeat
     *            true or false, depending on whether the animation should repeat
     */
    public Animation(String imgName, int sequenceCount, boolean repeat)
    {
        _cells = Media.getImage(imgName);
        _frames = new int[sequenceCount];
        _durations = new long[sequenceCount];
        _loop = repeat;
        _nextCell = 0;
    }

    /**
     * Create an animation where all of the frames are displayed for the same amount of time
     * 
     * @param imgName
     *            The animatable image that should be used
     * @param timePerFrame
     *            The time in milliseconds that each frame should be shown
     * @param repeat
     *            true or false, depending on whether the animation should repeat
     * @param frameIndices
     *            The indices of the image that should be shown
     */
    public Animation(String imgName, int timePerFrame, boolean repeat, int... frameIndices)
    {
        // TODO: add this constructor
    }

    /**
     * Add another step to the animation
     * 
     * @param frame
     *            The index within the image that should be displayed next
     * @param duration
     *            The time in milliseconds that this frame should be shown
     * @return the Animation, so that we can chain calls to "to()"
     */
    public Animation to(int frame, long duration)
    {
        _frames[_nextCell] = frame;
        _durations[_nextCell] = duration;
        _nextCell++;
        return this;
    }
}