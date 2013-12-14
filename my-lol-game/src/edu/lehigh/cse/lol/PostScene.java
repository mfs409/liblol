package edu.lehigh.cse.lol;

// TODO: clean up comments

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import edu.lehigh.cse.lol.Level.Renderable;

public class PostScene
{
    boolean               _disable;

    // TODO: externalize these strings to Configuration.java
    String                _winText     = "Next Level";

    String                _loseText    = "Try Again";

    ArrayList<Renderable> _winSprites  = new ArrayList<Renderable>();

    ArrayList<Renderable> _loseSprites = new ArrayList<Renderable>();

    private boolean       _win;

    boolean               _visible;
    /**
     * Sound to play when the level is won
     */
    Sound _winSound;

    /**
     * Sound to play when the level is lost
     */
    Sound _loseSound;

    /**
     * Set the sound to play when the level is won
     * 
     * @param soundName
     *            Name of the sound file to play
     */
    public static void setWinSound(String soundName)
    {
        getCurrPostScene()._winSound = Media.getSound(soundName);
    }

    /**
     * Set the sound to play when the level is lost
     * 
     * @param soundName
     *            Name of the sound file to play
     */
    public static void setLoseSound(String soundName)
    {
        getCurrPostScene()._loseSound = Media.getSound(soundName);
    }

    /**
     * Get the PostScene that is configured for the current level, or create a blank one if none exists.
     * 
     * @return
     */
    private static PostScene getCurrPostScene()
    {
        PostScene ps = Level._currLevel._postScene;
        if (ps != null)
            return ps;
        ps = new PostScene();
        Level._currLevel._postScene = ps;
        return ps;
    }

    // TODO: make font face a parameter
    public static void addExtraWinText(String text, int x, int y, int red, int green, int blue, int size)
    {
        PostScene tmp = getCurrPostScene();
        tmp._winSprites.add(Util.makeText(x, y, text, red, green, blue, size));
    }

    // TODO: make font face a parameter
    public static void addExtraLoseText(String text, int x, int y, int red, int green, int blue, int size)
    {
        PostScene tmp = getCurrPostScene();
        tmp._loseSprites.add(Util.makeText(x, y, text, red, green, blue, size));
    }

    // TODO: make font face a parameter
    public static void addExtraWinTextCentered(String text, int red, int green, int blue, int size)
    {
        PostScene tmp = getCurrPostScene();
        tmp._winSprites.add(Util.makeCenteredText(text, red, green, blue, size));
    }

    // TODO: make font face a parameter
    public static void addExtraLoseTextCentered(String text, int red, int green, int blue, int size)
    {
        PostScene tmp = getCurrPostScene();
        tmp._loseSprites.add(Util.makeCenteredText(text, red, green, blue, size));
    }

    public static void addWinImage(String imgName, int x, int y, int width, int height)
    {
        PostScene tmp = getCurrPostScene();
        tmp._winSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    public static void addLoseImage(String imgName, int x, int y, int width, int height)
    {
        PostScene tmp = getCurrPostScene();
        tmp._loseSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    // Use "" to disable
    public static void setDefaultWinText(String text)
    {
        getCurrPostScene()._winText = text;
    }

    // Use "" to disable
    public static void setDefaultLoseText(String text)
    {
        getCurrPostScene()._loseText = text;
    }

    public static void disable()
    {
        getCurrPostScene()._disable = true;
    }

    void setWin(boolean win)
    {
        _win = win;

        if (_disable) {
            finish();
            return;
        }

        _visible = true;

        // The default text to display can change at the last second, so we don't compute it until right here
        if (win) {
            if (_winSound != null)
                _winSound.play();
            // TODO: externalize font information, and add font face
            _winSprites.add(Util.makeCenteredText(_winText, 255, 255, 255, 32));
        }
        else {
            if (_loseSound != null)
                _loseSound.play();
            // TODO: externalize font information, and add font face
            _loseSprites.add(Util.makeCenteredText(_loseText, 255, 255, 255, 32));
        }
    }

    void finish()
    {
        // we turn off music here, so that music plays during the PostScene
        Level._currLevel.stopMusic();
        
        if (!_win) {
            LOL._game.doPlayLevel(LOL._game._currLevel);
        }
        else {
            if (LOL._game._currLevel == LOL._game._config.getNumLevels()) {
                LOL._game.doChooser();
            }
            else {
                LOL._game._currLevel++;
                LOL._game.doPlayLevel(LOL._game._currLevel);
            }
        }
    }

    boolean render(SpriteBatch _spriteRender, LOL _game)
    {
        if (!_visible)
            return false;
        if (Gdx.input.justTouched()) {
            _visible = false;
            finish();
            return true;
        }
        ArrayList<Renderable> _sprites = (_win) ? _winSprites : _loseSprites;

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClearColor(0, 0, 0, 1); // NB: can change color here...
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Level._currLevel._hudCam.update();
        _spriteRender.setProjectionMatrix(Level._currLevel._hudCam.combined);
        _spriteRender.begin();

        for (Renderable r : _sprites)
            r.render(_spriteRender, 0);

        _spriteRender.end();
        Controls.updateTimerForPause(Gdx.graphics.getDeltaTime());
        return true;
    }
}
