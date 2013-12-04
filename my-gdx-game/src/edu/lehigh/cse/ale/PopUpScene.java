package edu.lehigh.cse.ale;

// STATUS: this needs to be commented, and it would be nice if we could have a
// more robust pop-up builder, but in terms of prior ALE functionality, this is
// satisfactory

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.ale.Level.PendingEvent;

public class PopUpScene
{

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
     * Print a message on a black background, and wait for a timer to expire.
     * This version of the method adds the
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
        setPopUp(message, red, green, blue, fontSize);
        Timer.schedule(new Task()
        {
            @Override
            public void run()
            {
                _showPopUp = false;
                _popupText = null;
                _popUpImgTr = null;
            }
        }, duration);
    }

    static public void showImageTimed(String imgName, float duration, float x, float y, float width, float height)
    {
        setPopUpImage(Media.getImage(imgName), x, y, width, height);
        Timer.schedule(new Task()
        {
            @Override
            public void run()
            {
                _showPopUp = false;
                _popupText = null;
                _popUpImgTr = null;
            }
        }, duration);
    }

    /**
     * Print a message on a black background, and wait for a screen touch
     * 
     * @param message
     *            The message to display
     */
    static public void showTextAndWait(String message)
    {
        // forward to the more powerful method
        showTextAndWait(message, 255, 255, 255, 32);
    }

    /**
     * Print a message on a black background, and wait for a screen touch.
     * 
     * This version of the function adds the ability to customize the font
     * 
     * @param message
     *            The message to display
     * 
     * @param red
     *            The red component of the font color
     * 
     * @param green
     *            The green component of the font color
     * 
     * @param blue
     *            The blue component of the font color
     * 
     * @param size
     *            The size of the font
     */
    static public void showTextAndWait(String message, int red, int green, int blue, int fontSize)
    {
        setPopUp(message, red, green, blue, fontSize);
        Level._currLevel.addTouchEvent(0, 0, ALE._game._config.getScreenWidth(),
                ALE._game._config.getScreenHeight(), true, new PendingEvent()
                {
                    public void go()
                    {
                        _showPopUp = false;
                        _popupText = null;
                        _popUpImgTr = null;
                    }
                });
    }

    /**
     * Show an image on screen and wait for a screen touch.
     * 
     * @param imgName
     *            name of the image holding the message to be displayed
     * @param x
     *            X coordinate of the top left corner
     * @param y
     *            Y coordinate of the top left corner
     */
    static public void showImageAndWait(String imgName, float x, float y, float width, float height)
    {
        setPopUpImage(Media.getImage(imgName), x, y, width, height);
        Level._currLevel.addTouchEvent(0, 0, ALE._game._config.getScreenWidth(),
                ALE._game._config.getScreenHeight(), true, new PendingEvent()
                {
                    public void go()
                    {
                        _showPopUp = false;
                        _popupText = null;
                        _popUpImgTr = null;
                    }
                });
    }

    static boolean _showPopUp;

    static String  _popupText;

    static float   _popupRed;

    static float   _popupGreen;

    static float   _popupBlue;

    static int     _popupSize;

    static void setPopUp(String msg, int red, int green, int blue, int size)
    {
        _popupText = msg;
        _popupRed = red;
        _popupGreen = green;
        _popupBlue = blue;
        _popupRed /= 256;
        _popupGreen /= 256;
        _popupBlue /= 256;
        _popupSize = size;
        _showPopUp = true;        
    }

    static TextureRegion _popUpImgTr;

    static float         _popUpImgX;

    static float         _popUpImgY;

    static float         _popUpImgW;

    static float         _popUpImgH;

    static void setPopUpImage(TextureRegion tr, float x, float y, float width, float height)
    {
        _popUpImgTr = tr;
        _popUpImgX = x;
        _popUpImgY = y;
        _popUpImgW = width;
        _popUpImgH = height;
        _showPopUp = true;
    }

    static void show(SpriteBatch _spriteRender, ALE _game)
    {
        if (_popUpImgTr != null)
            _spriteRender.draw(_popUpImgTr, _popUpImgX, _popUpImgY, 0, 0, _popUpImgW, _popUpImgH, 1, 1, 0);
        if (_popupText != null) {
            int camWidth = _game._config.getScreenWidth();
            int camHeight = _game._config.getScreenHeight();

            BitmapFont f = Media.getFont("arial.ttf", _popupSize);
            String msg = _popupText;
            float w = f.getMultiLineBounds(msg).width;
            float h = f.getMultiLineBounds(msg).height;
            f.setColor(_popupRed, _popupGreen, _popupBlue, 1);
            f.drawMultiLine(_spriteRender, msg, camWidth / 2 - w / 2, camHeight / 2 + h / 2);
        }
    }
}
