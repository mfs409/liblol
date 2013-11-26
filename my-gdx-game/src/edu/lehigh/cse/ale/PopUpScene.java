package edu.lehigh.cse.ale;

import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class PopUpScene {

    /**
     * Print a message on a black background, and wait for a timer to expire
     * 
     * @param message
     *            The message to display
     * @param duration
     *            Time to display the message
     */
    static public void showTextTimed(String message, float duration)
    {
        // forward to the more powerful method
        showTextTimed(message, duration, 255, 255, 255, 30);
    }

    /**
     * Print a message on a black background, and wait for a timer to expire. This version of the method adds the
     * ability to customize the font
     * 
     * @param message
     *            The message to display
     * @param duration
     *            Time to display the message
     * @param red
     *            The red component of the font color
     * @param green
     *            The green component of the font color
     * @param blue
     *            The blue component of the font color
     * @param size
     *            The size of the font
     */
    static public void showTextTimed(String message, float duration, int red, int green, int blue, int fontSize)
    {
    	
    	Level._current.setPopUp(message, red, green, blue, fontSize);
    	Timer.schedule(new Task(){@Override public void run(){Level._current._showPopUp = false;}}, duration);
/*
    	CameraScene child = configurePopup(false, duration);
        Text t = new Text(0, 0, Util.makeFont(red, green, blue, fontSize), message, ALE._self
                .getVertexBufferObjectManager());
        float w = t.getWidth();
        float h = t.getHeight();
        t.setPosition(Configuration.getCameraWidth() / 2 - w / 2, Configuration.getCameraHeight() / 2 - h / 2);
        child.attachChild(t);
        Level._current.setChildSceneModal(child);
        */
    }

    static public void showImageTimed(String imgName, float duration, float x, float y, float width, float height)
    {
        Level._current.setPopUpImage(Media.getImage(imgName), x, y, width, height);
        Timer.schedule(new Task(){@Override public void run(){Level._current._showPopUp = false;}}, duration);
    }
	
}
