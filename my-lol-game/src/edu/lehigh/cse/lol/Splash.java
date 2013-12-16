package edu.lehigh.cse.lol;

// TODO: clean up code and comments

// TODO: add debug rendering to this?

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GLCommon;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class Splash extends ScreenAdapter
{
    /**
     * The camera we will use
     */
    OrthographicCamera _camera;

    /**
     * A rectangle for tracking the location of the play button
     */
    Rectangle          _play;

    /**
     * A rectangle for tracking the location of the help button
     */
    Rectangle          _help;

    /**
     * A rectangle for tracking the location of the quit button
     */
    Rectangle          _quit;

    /**
     * For handling touches
     */
    Vector3            _touchVec;

    /**
     * For rendering
     */
    SpriteBatch        _sb;

    /**
     * The splash screen texture region
     */
    TextureRegion      _tr;

    /**
     * Set up the splash screen
     * 
     * @param game
     *            The main game object
     */
    public Splash()
    {
        int CAMERA_WIDTH = LOL._game._config.getScreenWidth();
        int CAMERA_HEIGHT = LOL._game._config.getScreenHeight();

        // configure the camera, center it on the screen
        _camera = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
        _camera.position.set(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2, 0);

        // set up the play, help, and quit buttons
        _play = new Rectangle(LOL._game._splashConfig.getPlayX(), LOL._game._splashConfig.getPlayY()
                - LOL._game._splashConfig.getPlayHeight(), LOL._game._splashConfig.getPlayWidth(),
                LOL._game._splashConfig.getPlayHeight());
        if (LOL._game._config.getNumHelpScenes() > 0) {
            _help = new Rectangle(LOL._game._splashConfig.getHelpX(), LOL._game._splashConfig.getHelpY()
                    - LOL._game._splashConfig.getHelpHeight(), LOL._game._splashConfig.getHelpWidth(),
                    LOL._game._splashConfig.getHelpHeight());
        }
        _quit = new Rectangle(LOL._game._splashConfig.getQuitX(), LOL._game._splashConfig.getQuitY()
                - LOL._game._splashConfig.getQuitHeight(), LOL._game._splashConfig.getQuitWidth(),
                LOL._game._splashConfig.getQuitHeight());

        // prepare for touches
        _touchVec = new Vector3();

        // set up our images
        _tr = new TextureRegion(new Texture(Gdx.files.internal(LOL._game._config.getSplashBackground())));

        // and our sprite batcher
        _sb = new SpriteBatch();

        // config music?
        if (LOL._game._config.getSplashMusic() != null)
            _music = Media.getMusic(LOL._game._config.getSplashMusic());
    }

    @Override
    public void render(float delta)
    {
        // for now, stick everything in here...
        playMusic();

        // was there a touch?
        if (Gdx.input.justTouched()) {
            // translate the touch into _touchVec
            _camera.unproject(_touchVec.set(Gdx.input.getX(), Gdx.input.getY(), 0));
            if (_quit.contains(_touchVec.x, _touchVec.y)) 
                LOL._game.doQuit();
            if (_play.contains(_touchVec.x, _touchVec.y)) 
                LOL._game.doChooser();
            if (_help != null && _help.contains(_touchVec.x, _touchVec.y)) 
                LOL._game.doHelpLevel(1);
        }

        // now draw the screen...
        GLCommon gl = Gdx.gl;
        gl.glClearColor(1, 0, 0, 1);
        gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        _camera.update();

        int width = LOL._game._config.getScreenWidth();
        int height = LOL._game._config.getScreenHeight();
        
        _sb.setProjectionMatrix(_camera.combined);
        _sb.begin();
        _sb.enableBlending();
        _sb.draw(_tr, 0, 0, width, height);

        // [TODO]: these need to use the _game._config... it also wouldn't hurt to
        // have objects to store these text entities, so that we can precompute
        // more and set up the rectangles correctly...
        BitmapFont f = Media.getFont("arial.ttf", 30);
        float w = f.getBounds("Demo Game").width;
        float h = f.getBounds("test").height;
        f.draw(_sb, "Demo Game", width / 2 - w / 2, height - 5 - h);
        w = f.getBounds("Play").width;
        f.draw(_sb, "Play", LOL._game._splashConfig.getPlayX(), LOL._game._splashConfig.getPlayY());
        if (_help != null) {
            w = f.getBounds("Help").width;
            f.draw(_sb, "Help", width / 2 - w / 2, height - 5 - h - 30 - h - 30 - h);
        }
        w = f.getBounds("Quit").width;
        f.draw(_sb, "Quit", width / 2 - w / 2, height - 5 - h - 30 - h - 30 - h - 30 - h);
        _sb.end();
    }

    /*
     * MUSIC MANAGEMENT
     */

    Music   _music;

    boolean _musicPlaying = false;

    public void playMusic()
    {
        if (!_musicPlaying) {
            _musicPlaying = true;
            _music.play();
        }
    }

    public void pauseMusic()
    {
        if (_musicPlaying) {
            _musicPlaying = false;
            _music.pause();
        }
    }

    public void stopMusic()
    {
        if (_musicPlaying) {
            _musicPlaying = false;
            _music.stop();
        }
    }

    @Override
    public void dispose()
    {
        stopMusic();
    }

    @Override
    public void hide()
    {
        pauseMusic();
    }
}
