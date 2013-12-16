package edu.lehigh.cse.lol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

/**
 * The splash screen is the first thing the user sees when playing the game. It has buttons for playing, getting help,
 * and quitting. It is configured through a SplashConfiguration object.
 */
public class Splash extends ScreenAdapter
{
    /**
     * The camera for displaying the scene
     */
    private OrthographicCamera _cam;

    /**
     * A rectangle for tracking the location of the play button
     */
    private Rectangle          _play;

    /**
     * A rectangle for tracking the location of the help button
     */
    private Rectangle          _help;

    /**
     * A rectangle for tracking the location of the quit button
     */
    private Rectangle          _quit;

    /**
     * For handling touches
     */
    private Vector3            _v  = new Vector3();

    /**
     * For rendering
     */
    private SpriteBatch        _sb = new SpriteBatch();

    /**
     * For debug rendering
     */
    private ShapeRenderer      _sr = new ShapeRenderer();

    /**
     * The image to display
     */
    private TextureRegion      _tr;

    /**
     * The music to play
     */
    Music                      _music;

    /**
     * Track if the music is actually playing
     */
    boolean                    _musicPlaying;

    /**
     * Basic configuration: get the image and music, configure the locations of the play/help/quit buttons
     */
    public Splash()
    {
        // configure the camera, center it on the screen
        int w = LOL._game._config.getScreenWidth();
        int h = LOL._game._config.getScreenHeight();
        _cam = new OrthographicCamera(w, h);
        _cam.position.set(w / 2, h / 2, 0);

        // set up the play, help, and quit buttons
        SplashConfiguration sc = LOL._game._splashConfig;
        _play = new Rectangle(sc.getPlayX(), sc.getPlayY(), sc.getPlayWidth(), sc.getPlayHeight());
        if (LOL._game._config.getNumHelpScenes() > 0) {
            _help = new Rectangle(sc.getHelpX(), sc.getHelpY(), sc.getHelpWidth(), sc.getHelpHeight());
        }
        _quit = new Rectangle(sc.getQuitX(), sc.getQuitY(), sc.getQuitWidth(), sc.getQuitHeight());

        // get the background image and music
        _tr = new TextureRegion(new Texture(Gdx.files.internal(sc.getBackgroundImage())));
        if (LOL._game._splashConfig.getMusic() != null)
            _music = Media.getMusic(sc.getMusic());
    }

    /**
     * Start the music if it's not already playing
     */
    public void playMusic()
    {
        if (!_musicPlaying) {
            _musicPlaying = true;
            _music.play();
        }
    }

    /**
     * Pause the music if it's playing
     */
    public void pauseMusic()
    {
        if (_musicPlaying) {
            _musicPlaying = false;
            _music.pause();
        }
    }

    /**
     * Stop the music if it's playing
     */
    public void stopMusic()
    {
        if (_musicPlaying) {
            _musicPlaying = false;
            _music.stop();
        }
    }

    /**
     * Draw the splash screen
     * 
     * @param delta
     *            time since the screen was last displayed
     */
    @Override
    public void render(float delta)
    {
        // make sure the music is playing
        playMusic();

        // If there is a new down-touch, figure out if it was to a button
        if (Gdx.input.justTouched()) {
            // translate the touch into camera coordinates
            _cam.unproject(_v.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            // DEBUG: print the location of the touch... this is really useful when trying to figure out the coordinates
            // of the rectangles
            if (LOL._game._config.showDebugBoxes()) {
                Gdx.app.log("touch", "(" + _v.x + ", " + _v.y + ")");
            }
            // check if the touch was inside one of our buttons, and act accordingly
            if (_quit.contains(_v.x, _v.y)) {
                stopMusic();
                LOL._game.doQuit();
            }
            if (_play.contains(_v.x, _v.y)) {
                stopMusic();
                LOL._game.doChooser();
            }
            if (_help != null && _help.contains(_v.x, _v.y)) {
                stopMusic();
                LOL._game.doHelpLevel(1);
            }
        }

        // now draw the screen...
        GLCommon gl = Gdx.gl;
        gl.glClearColor(1, 0, 0, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        _cam.update();
        _sb.setProjectionMatrix(_cam.combined);
        _sb.begin();
        _sb.enableBlending();
        _sb.draw(_tr, 0, 0, LOL._game._config.getScreenWidth(), LOL._game._config.getScreenHeight());
        _sb.end();

        // DEBUG: show where the buttons' boxes are
        if (LOL._game._config.showDebugBoxes()) {
            _sr.setProjectionMatrix(_cam.combined);
            _sr.begin(ShapeType.Line);
            _sr.setColor(Color.RED);
            _sr.rect(_play.x, _play.y, _play.width, _play.height);
            _sr.rect(_help.x, _help.y, _help.width, _help.height);
            _sr.rect(_quit.x, _quit.y, _quit.width, _quit.height);
            _sr.end();
        }
    }

    /**
     * When this scene goes away, make sure the music gets turned off
     */
    @Override
    public void dispose()
    {
        stopMusic();
    }

    /**
     * When this scene goes away, make sure the music gets turned off
     */
    @Override
    public void hide()
    {
        pauseMusic();
    }
}
