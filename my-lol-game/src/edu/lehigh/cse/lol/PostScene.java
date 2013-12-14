package edu.lehigh.cse.lol;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import edu.lehigh.cse.lol.Level.Renderable;

// TODO: migrate stuff out of Level (text, sound, image) if it has to deal with end of game

// TODO: clean up comments

public class PostScene 
{
    ArrayList<Renderable> _winSprites  = new ArrayList<Renderable>();
    ArrayList<Renderable> _loseSprites = new ArrayList<Renderable>();

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

    public static void addExtraWinText(String text, int x, int y, int red, int green, int blue, int size)
    {
        PostScene tmp = getCurrPostScene();
        tmp._winSprites.add(Util.makeText(x, y, text, red, green, blue, size));
    }

    public static void addExtraLoseText(String text, int x, int y, int red, int green, int blue, int size)
    {
        PostScene tmp = getCurrPostScene();
        tmp._loseSprites.add(Util.makeText(x, y, text, red, green, blue, size));
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
    public static void setWinText(String text)
    {
        getCurrPostScene()._winText = text;
    }
    public static void setLoseText(String text)
    {
        getCurrPostScene()._loseText = text;        
    }

    // externalize these strings, or make them defaults?
    String _winText = "Next Level";
    String _loseText = "Try Again";
    
    /**
     * 
     * @param x
     * @param y
     * @return true if the event was unhandled
     */
    boolean onTouch(int x, int y)
    {
        if (!_visible)
            return true;
        popUpDone();
        return false;
    }

    private void popUpDone()
    {
        _visible = false;
        if (!_win) {
            LOL._game.doPlayLevel(LOL._game._currLevel);
        }
        else {
            if (LOL._game._currLevel == LOL._game._config.getNumLevels()) {
                // TODO: untested
                LOL._game.doChooser();
            }
            else {
                LOL._game._currLevel++;
                LOL._game.doPlayLevel(LOL._game._currLevel);
            }
        }
    }

    private boolean _win;
    
    void setWin(boolean win) {
        _win = win;
        _visible = true;
        
        // TODO: compute the text and font for displaying win/lose stuff
    }
    
    
    boolean       _visible;

    boolean render(SpriteBatch _spriteRender, LOL _game)
    {
        if (!_visible)
            return false;
        ArrayList<Renderable> _sprites = (_win) ? _winSprites : _loseSprites;

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClearColor(0, 0, 0, 1); // NB: can change color here...
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Level._currLevel._hudCam.update();
        _spriteRender.setProjectionMatrix(Level._currLevel._hudCam.combined);
        _spriteRender.begin();

        for (Renderable r : _sprites)
            r.render(_spriteRender, 0);

        // TODO: draw the win or lose text here, centered nicely
        
        _spriteRender.end();
        Controls.updateTimerForPause(Gdx.graphics.getDeltaTime());
        return true;
    }

}
