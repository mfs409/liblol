package edu.lehigh.cse.lol;

public class SceneActor extends BaseActor {
    /// Should we run code when this Control is touched?
    boolean mIsTouchable;

    /// Code to run when this Control is touched
    TouchEventHandler mPanHandler;
    TouchEventHandler mPanStopHandler;
    TouchEventHandler mZoomHandler;
    TouchEventHandler mDownHandler;

    SceneActor(LolScene scene, String imgName, float width, float height) {
        super(scene, imgName, width, height);
    }

    /**
     * Disable touch for this control
     */
    public void disableTouch() {
        mIsTouchable = false;
    }

    /**
     * Enable touch for this control
     */
    public void enableTouch() {
        mIsTouchable = true;
    }

}
