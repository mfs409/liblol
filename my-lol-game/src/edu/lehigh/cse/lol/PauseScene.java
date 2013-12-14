package edu.lehigh.cse.lol;

// TODO: clean up comments

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import edu.lehigh.cse.lol.Level.Renderable;

// TODO: we need a level to demo this... it should put a pause button on the hud that causes this to show

// TODO: need to be able to add a 'back to menu' button

// TODO: we could add support for muting via another button on this screen

public class PauseScene
{
    ArrayList<Renderable> _sprites = new ArrayList<Renderable>();

    boolean               _visible = true;

    /**
     * Get the PauseScene that is configured for the current level, or create a blank one if none exists.
     * 
     * @return
     */
    void create()
    {
        Level._currLevel._pauseScene = new PauseScene();
    }

    // TODO: add font parameter
    public static void addText(String text, int x, int y, int red, int green, int blue, int size)
    {
        Level._currLevel._pauseScene._sprites.add(Util.makeText(x, y, text, red, green, blue, size));
    }

    // TODO: add font parameter
    public static void addCenteredText(String text, int red, int green, int blue, int size)
    {
        Level._currLevel._pauseScene._sprites.add(Util.makeCenteredText(text, red, green, blue, size));
    }

    public static void addImage(String imgName, int x, int y, int width, int height)
    {
        Level._currLevel._pauseScene._sprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    boolean render(SpriteBatch _spriteRender, LOL _game)
    {
        // if the pop-up scene is not visible, do nothing
        if (!_visible)
            return false;
        // if there's a touch, return
        //
        // TODO: need to make this more nuanced if we want to add buttons...
        if (Gdx.input.justTouched()) {
            _visible = false;
            return false;
        }
        // OK, we should render the scene...

        // clear screen and draw sprites... we can use hudCam
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Level._currLevel._hudCam.update();
        _spriteRender.setProjectionMatrix(Level._currLevel._hudCam.combined);
        _spriteRender.begin();
        for (Renderable r : _sprites)
            r.render(_spriteRender, 0);
        _spriteRender.end();

        // be sure to update anything related to timers in the main game
        Controls.updateTimerForPause(Gdx.graphics.getDeltaTime());
        return true;
    }
}
