package edu.lehigh.cse.lol;

// TODO: comment

// TODO: fix the positioning of text here, and in controls

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.lehigh.cse.lol.Level.Renderable;

/**
 * 
 * 
 * NB: the ScreenAdapter is a convenience since this is a simple renderable screen
 * 
 * TODO: use ScreenAdapter elsewhere?
 */
public class HelpLevel extends ScreenAdapter // implements Screen
{
    /*
     * SUPPORT FOR BUILDING HELP SCENES
     */

    /**
     * Reset the help scene so that we can make the next part of the help message
     * 
     * @param red
     *            red component of help screen background color
     * @param green
     *            green component of help screen background color
     * @param blue
     *            blue component of help screen background color
     */

    /**
     * Draw a picture on the _current help scene
     * 
     * Note: the order in which this is called relative to other entities will determine whether they go under or over
     * this picture.
     * 
     * @param x
     *            X coordinate of top left corner
     * 
     * @param y
     *            Y coordinate of top left corner
     * 
     * @param width
     *            Width of the picture
     * 
     * @param height
     *            Height of this picture
     * 
     * @param imgName
     *            Name of the picture to display
     * 
     * @return the picture on the screen, so that it can be animated if need be
     */
    public static void drawPicture(final int x, final int y, final int width, final int height, String imgName)
    {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        TextureRegion[] trs = Media.getImage(imgName);
        final TextureRegion tr = (trs != null) ? trs[0] : null;
        _currLevel._sprites.add(new Renderable(){
            @Override
            public void render(SpriteBatch sb, float elapsed)
            {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }});
    }

    /**
     * Print a message on the _current help scene. This version of the addText method uses the default font.
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
        drawText(x, y, message, 255, 255, 255, 20);
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
    static public void drawText(final int x, final int y, final String message, final int red, final int green, final int blue, int size)
    {
        final BitmapFont bf = Media.getFont("arial.ttf", size);
        _currLevel._sprites.add(new Renderable(){
            @Override
            public void render(SpriteBatch sb, float elapsed)
            {
                bf.setColor(((float)red)/256, ((float)green)/256, ((float)blue)/256, 1);
                bf.drawMultiLine(sb, message, x, y);
            }});
    }

    /*
     * HELP LEVELS
     */

    static HelpLevel             _currLevel;

    public ArrayList<Renderable> _sprites = new ArrayList<Renderable>();

    OrthographicCamera           _hudCam;

    // a spritebatch and a font for text rendering and a Texture to draw our
    // boxes
    private SpriteBatch          _spriteRender;

    LOL                          _game;

    public HelpLevel(int width, int height, LOL game)
    {
        _game = game;
        _currLevel = this;

        int camWidth = _game._config.getScreenWidth();
        int camHeight = _game._config.getScreenHeight();
        _hudCam = new OrthographicCamera(camWidth, camHeight);
        _hudCam.position.set(camWidth / 2, camHeight / 2, 0);

        // next we create a renderer for drawing sprites
        _spriteRender = new SpriteBatch();

        // A place for everything we draw... need 5 eventually
        _sprites = new ArrayList<Renderable>();
    }

    @Override
    public void render(float delta)
    {
        // poll for new touches
        if (Gdx.input.justTouched())
            nextHelp();

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClearColor(_red, _green, _blue, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // render the hud
        _hudCam.update();
        _spriteRender.setProjectionMatrix(_hudCam.combined);
        _spriteRender.begin();
        // do the displayable stuff
        for (Renderable c : _sprites) {
            c.render(_spriteRender, 0);
        }
        _spriteRender.end();
    }

    private float _red;

    private float _green;

    private float _blue;

    /**
     * Configure a level by setting its background color
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
        _currLevel = new HelpLevel(LOL._game._config.getScreenWidth(), LOL._game._config.getScreenWidth(), LOL._game);
        _currLevel._red = ((float) red) / 256;
        _currLevel._green = ((float) green) / 256;
        _currLevel._blue = ((float) blue) / 256;
    }

    /**
     * Advance to the next help scene
     */
    void nextHelp()
    {
        if (_game._currHelp < _game._config.getNumHelpScenes()) {
            _game._currHelp++;
            _game.doHelpLevel(_game._currHelp);
        }
        else {
            _game.doSplash();
        }
    }
}
