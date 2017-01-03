package edu.lehigh.cse.lol;

/**
 * TouchEventHandler is a wrapper for code that ought to run in response to a touch event.
 *
 * We can use TouchEventHandlers to specify how a game should respond to a tap, press, release, or
 * pan event.
 */
public abstract class ToggleEventHandler {
    /// A flag to control whether the event is allowed to execute or not
    public boolean mIsActive = true;

    /// The actor who generated this touch event
    public BaseActor mSource = null;

    /// A flag to track if the event is being held
    public boolean isHolding = false;

    /**
     * The go() method encapsulates the code that should be run in response to a touch event.
     */
    abstract public boolean go(boolean isUp, float eventPositionX, float eventPositionY);
}
