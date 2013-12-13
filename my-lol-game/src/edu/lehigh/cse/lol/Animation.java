package edu.lehigh.cse.lol;

// TODO: clean up comments

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Note: an animation does not include information for /playing/ the animation.
 * Thus it can be re-used by many entities.
 */
public class Animation
{
    TextureRegion[] _cells;

    int[]           _frames;

    long[]          _durations;

    boolean         _loop;

    int             _nextCell;

    public Animation(String imgName, int sequenceCount, boolean repeat)
    {
        _cells = Media.getImage(imgName);
        _frames = new int[sequenceCount];
        _durations = new long[sequenceCount];
        _loop = repeat;
        _nextCell = 0;
    }

    // TODO: add this constructor
    public Animation(String imgName, int timePerFrame, boolean repeat, int ... frameIndices)
    {
        
    }
    
    public Animation to(int frame, long duration)
    {
        _frames[_nextCell] = frame;
        _durations[_nextCell] = duration;
        _nextCell++;
        return this;
    }
}