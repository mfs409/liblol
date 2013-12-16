package edu.lehigh.cse.lol;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.Util.Renderable;

/**
 * PreScene provides a way to put a pop-up on the screen before a level begins. A PreScene can include arbitrary text
 * and pictures.
 */
public class PreScene
{
    /*
     * INTERNAL INTERFACE
     */

    /**
     * The text and pictures to display
     */
    ArrayList<Renderable> _sprites      = new ArrayList<Renderable>();

    /**
     * True if we must click in order to clear the PreScene
     */
    boolean               _clickToClear = true;

    /**
     * True when the scene is being displayed
     */
    boolean               _visible      = true;

    /**
     * Get the PreScene that is configured for the current level, or create a blank one if none exists. We use this as a
     * convenience since the LOL paradigm is that the game designer calls static methods on PreScene to configure an
     * existing object.
     * 
     * @return The current PreScene
     */
    private static PreScene getCurrPreScene()
    {
        PreScene ps = Level._currLevel._preScene;
        if (ps != null)
            return ps;
        ps = new PreScene();
        Level._currLevel._preScene = ps;
        return ps;
    }

    /**
     * Render this PreScene
     * 
     * @param sb
     *            The SpriteBatch to use when rendering
     * 
     * @return true if we drew something, false otherwise
     */
    boolean render(SpriteBatch sb)
    {
        // if the scene is not visible, do nothing
        if (!_visible)
            return false;
        // if we're supposed to be listening for clicks, and we get one, then disable the scene
        if (_clickToClear) {
            if (Gdx.input.justTouched()) {
                _visible = false;
                return false;
            }
        }
        // OK, we should render the scene...

        // clear screen and draw sprites... we can use the level's hudCam
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Level._currLevel._hudCam.update();
        sb.setProjectionMatrix(Level._currLevel._hudCam.combined);
        sb.begin();
        for (Renderable r : _sprites)
            r.render(sb, 0);
        sb.end();

        // be sure to update anything related to timers in the main game
        Controls.updateTimerForPause(Gdx.graphics.getDeltaTime());
        return true;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Add some text to the PreScene
     * 
     * @param text
     *            The text to display
     * @param x
     *            X coordinate of the text
     * @param y
     *            Y coordinate of the text
     * @param red
     *            Redness of the text color
     * @param green
     *            Greenness of the text color
     * @param blue
     *            Blueness of the text color
     * @param fontName
     *            The font file to use
     * @param size
     *            The size of the text
     */
    public static void addText(String text, int x, int y, int red, int green, int blue, String fontName, int size)
    {
        getCurrPreScene()._sprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    /**
     * Add some text to the PreScene, and center it
     * 
     * @param text
     *            The text to display
     * @param red
     *            Redness of the text color
     * @param green
     *            Greenness of the text color
     * @param blue
     *            Blueness of the text color
     * @param fontName
     *            The font file to use
     * @param size
     *            The size of the text
     */
    public static void addCenteredText(String text, int red, int green, int blue, String fontName, int size)
    {
        getCurrPreScene()._sprites.add(Util.makeCenteredText(text, red, green, blue, fontName, size));
    }

    /**
     * Add an image to the PreScene
     * 
     * @param imgName
     *            The file name for the image to display
     * @param x
     *            X coordinate of the bottom left corner
     * @param y
     *            Y coordinate of the bottom left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     */
    public static void addImage(String imgName, int x, int y, int width, int height)
    {
        getCurrPreScene()._sprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * The default is for a PreScene to show until the user touches it to dismiss it. To have the PreScene disappear
     * after a fixed time instead, use this.
     * 
     * @param duration
     *            The time, in seconds, before the PreScene should disappear.
     */
    public static void setExpire(float duration)
    {
        if (duration > 0) {
            getCurrPreScene()._clickToClear = false;
            Timer.schedule(new Task()
            {
                @Override
                public void run()
                {
                    Level._currLevel._preScene._visible = false;
                }
            }, duration);
        }
    }
}