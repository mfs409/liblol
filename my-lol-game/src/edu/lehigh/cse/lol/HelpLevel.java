package edu.lehigh.cse.lol;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * HelpLevel provides an interface for drawing to the help screens of a game
 */
public class HelpLevel extends ScreenAdapter
{
    /*
     * INTERNAL DATA FOR RENDERING A HELP LEVEL
     */

    /**
     * The background color of the help level
     */
    private Color                 _c       = new Color();

    /**
     * All the sprites that need to be drawn
     */
    private ArrayList<Renderable> _sprites = new ArrayList<Renderable>();

    /**
     * The camera to use when drawing
     */
    private OrthographicCamera    _camera;

    /**
     * The spritebatch to use when drawing
     */
    private SpriteBatch           _sb;

    /**
     * In LOL, we avoid having the game designer construct objects. To that end, the HelpLevel is accessed through a
     * singleton.
     */
    static HelpLevel              _currLevel;

    /**
     * When the game designer creates a help level, she uses configure, which calls this to create the internal context
     */
    private HelpLevel()
    {
        // save the static context
        _currLevel = this;

        // set up the camera
        int camWidth = LOL._game._config.getScreenWidth();
        int camHeight = LOL._game._config.getScreenHeight();
        _camera = new OrthographicCamera(camWidth, camHeight);
        _camera.position.set(camWidth / 2, camHeight / 2, 0);

        // set up the renderer
        _sb = new SpriteBatch();
    }

    /**
     * The main render loop for Help Levels. There's nothing fancy here
     * 
     * @param delta
     *            The time that has transpired since the last render
     */
    @Override
    public void render(float delta)
    {
        // Poll for a new touch (down-press)
        // On down-press, either advance to the next help scene, or return to the splash screen
        if (Gdx.input.justTouched()) {
            if (LOL._game._currHelp < LOL._game._config.getNumHelpScenes()) {
                LOL._game._currHelp++;
                LOL._game.doHelpLevel(LOL._game._currHelp);
                return;
            }
            LOL._game.doSplash();
            return;
        }

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClearColor(_c.r, _c.g, _c.b, _c.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // render
        _camera.update();
        _sb.setProjectionMatrix(_camera.combined);
        _sb.begin();
        for (Renderable c : _sprites)
            c.render(_sb, 0);
        _sb.end();
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Configure a help level by setting its background color
     * 
     * @param red
     *            The red component of the background color
     * @param green
     *            The green component of the background color
     * @param blue
     *            The blue component of the background color
     */
    public static void configure(int red, int green, int blue)
    {
        _currLevel = new HelpLevel();
        _currLevel._c.r = ((float) red) / 256;
        _currLevel._c.g = ((float) green) / 256;
        _currLevel._c.b = ((float) blue) / 256;
    }

    /**
     * Draw a picture on the current help scene
     * 
     * Note: the order in which this is called relative to other entities will determine whether they go under or over
     * this picture.
     * 
     * @param x
     *            X coordinate of bottom left corner
     * 
     * @param y
     *            Y coordinate of bottom left corner
     * 
     * @param width
     *            Width of the picture
     * 
     * @param height
     *            Height of this picture
     * 
     * @param imgName
     *            Name of the picture to display
     */
    public static void drawPicture(final int x, final int y, final int width, final int height, String imgName)
    {
        // set up the image to display
        _currLevel._sprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * Print a message on the current help scene. This version of the addText method uses the default font.
     * 
     * @param x
     *            X coordinate of text
     * 
     * @param y
     *            Y coordinate of text
     * 
     * @param message
     *            The message to display
     */
    static public void drawText(int x, int y, String message)
    {
        _currLevel._sprites.add(Util.makeText(x, y, message, 255, 255, 255, LOL._game._config.getDefaultFont(), 20));
    }

    /**
     * Print a message on the _current help scene. This version of the addText method allows the programmer to specify
     * the appearance of the font
     * 
     * @param x
     *            X coordinate of the top left corner of where the text should appear on screen
     * 
     * @param y
     *            Y coordinate of the top left corner of where the text should appear on screen
     * 
     * @param message
     *            The message to display
     * 
     * @param red
     *            A value between 0 and 255, indicating the red aspect of the font color
     * 
     * @param green
     *            A value between 0 and 255, indicating the green aspect of the font color
     * 
     * @param blue
     *            A value between 0 and 255, indicating the blue aspect of the font color
     * 
     * @param size
     *            The size of the font used to display the message
     */
    static public void drawText(final int x, final int y, final String message, final int red, final int green,
            final int blue, String fontName, int size)
    {
        _currLevel._sprites.add(Util.makeText(x, y, message, red, green, blue, fontName, size));
    }
}
