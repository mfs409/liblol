package edu.lehigh.cse.lol;

// TODO: clean up comments

// TODO: Could be cleaner... refactor is due.

// TODO: this needs to be commented, and it would be nice if we could have a
// more robust pop-up builder, but in terms of prior ALE functionality, this is
// satisfactory

// TODO: get rid of statics

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class PreScene
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
                done();
            }
        }, duration);
    }

    private static void done()
    {
        _showPopUp = false;
        _popupText = null;
        _popUpImgTr = null;
    }

    static public void showImageTimed(String imgName, float duration, float x, float y, float width, float height)
    {
        setPopUpImage(Media.getImage(imgName)[0], x, y, width, height);
        Timer.schedule(new Task()
        {
            @Override
            public void run()
            {
                done();
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
        setPopUpImage(Media.getImage(imgName)[0], x, y, width, height);
    }

    /**
     * 
     * @param x
     * @param y
     * @return true if the event was unhandled
     */
    public static boolean onTouch(int x, int y)
    {
        if (!_showPopUp)
            return true;
        done();
        return false;
    }

    static boolean        _showPopUp;

    private static String _popupText;

    private static float  _popupRed;

    private static float  _popupGreen;

    private static float  _popupBlue;

    private static int    _popupSize;

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

    static void reset()
    {
        PreScene._popUpImgTr = null;
        PreScene._popupText = null;
        _showPopUp = false;
    }

    static boolean show(SpriteBatch _spriteRender, LOL _game)
    {
        if (!_showPopUp)
            return false;

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClearColor(0, 0, 0, 1); // NB: can change color here...
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Level._currLevel._hudCam.update();
        _spriteRender.setProjectionMatrix(Level._currLevel._hudCam.combined);
        _spriteRender.begin();

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

        _spriteRender.end();
        Controls.updateTimerForPause(Gdx.graphics.getDeltaTime());
        return true;
    }
}
