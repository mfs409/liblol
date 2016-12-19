package edu.lehigh.cse.lol;

import com.badlogic.gdx.math.Vector3;

/**
 * TouchEventHandler is a wrapper for code that ought to run in response to a touch event.
 *
 * We can use TouchEventHandlers to specify how a game should respond to a tap, press, release, or
 * pan event.
 */
public abstract class TouchEventHandler {
    // A flag to control whether the event is allowed to execute or not
    public boolean mIsActive = true;

    // The control to which this handler is attached, if any
    public Control mAttachedControl = null;

    /**
     * The go() method encapsulates the code that should be run in response to a touch event.
     *
     * @param eventPosition The coordinates of the touch event that caused this handler to run.
     */
    abstract public void go(Vector3 eventPosition);
}
