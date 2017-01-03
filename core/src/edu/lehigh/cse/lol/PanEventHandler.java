package edu.lehigh.cse.lol;

public abstract class PanEventHandler {
    /// A flag to control whether the event is allowed to execute or not
    public boolean mIsActive = true;

    /// The actor who generated this touch event
    public BaseActor mSource = null;

    /**
     * The go() method encapsulates the code that should be run in response to a touch event.
     */
    abstract public boolean go(float eventPositionX, float eventPositionY, float deltaX, float deltaY);
}
