package edu.lehigh.cse.lol;

// TODO: clean up comments

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.Level.Renderable;

// TODO: the only difference between this and a 'pause scene' is that a 'pause scene' can have a button for going back
// to the main menu, and thus required more nuance in its click handling

public class PreScene
{
    /**
     * Get the PreScene that is configured for the current level, or create a blank one if none exists.
     * 
     * @return
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

    // TODO: add font
    //
    // TODO: add option for centering
    public static void addText(String text, int x, int y, int red, int green, int blue, int size)
    {
        PreScene tmp = getCurrPreScene();
        tmp._sprites.add(Util.makeText(x, y, text, red, green, blue, size));
    }

    public static void addImage(String imgName, int x, int y, int width, int height)
    {
        PreScene tmp = getCurrPreScene();
        tmp._sprites.add(Util.makePicture(x, y, width, height, imgName));
    }

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

    ArrayList<Renderable> _sprites      = new ArrayList<Renderable>();

    boolean               _clickToClear = true;

    boolean               _visible      = true;

    boolean render(SpriteBatch _spriteRender, LOL _game)
    {
        // if the pop-up scene is not visible, do nothing
        if (!_visible)
            return false;
        // if we're supposed to be listening for clicks, and we get one, then disable the pop-up scene
        if (_clickToClear) {
            if (Gdx.input.justTouched()) {
                _visible = false;
                return false;
            }
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
