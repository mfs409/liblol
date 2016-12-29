package edu.lehigh.cse.lol;

import com.badlogic.gdx.math.Vector3;

/**
 * TouchEventHandler is a wrapper for code that ought to run in response to a touch event.
 *
 * We can use TouchEventHandlers to specify how a game should respond to a tap, press, release, or
 * pan event.
 */
public abstract class TouchEventHandler {
    /// A flag to control whether the event is allowed to execute or not
    public boolean mIsActive = true;

    /// The control to which this handler is attached, if any
    public Control mAttachedControl = null;

    /// An actor associated with this handler, if any
    public Actor mAttachedActor = null;

    /// A flag to track if the event is being held
    public boolean isHolding = false;

    /// An extra boolean parameter to the go() function, when used for toggle events that need to
    /// know a down/up state
    public boolean isUp = false;

    /// An extra float parameter to the go() function, when used for pan events that need a deltaX
    public float deltaX = 0;

    /// An extra float parameter to the go() function, when used for pan events that need a deltaY
    public float deltaY = 0;

    /**
     * The go() method encapsulates the code that should be run in response to a touch event.
     */
    abstract public boolean go(float eventPositionX, float eventPositionY);
}
