package edu.lehigh.cse.ale;

// STATUS: this should become unnecessary once we factor out input processing

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Game;

/**
 * The LibGDX Screen object is nice, but it doesn't have anything for touch
 * events. This resolves the problem for us in a clean way.
 * 
 * @author spear
 * 
 *         TODO: I bet there is a has-a option to avoid this is-a nastiness...
 *         See Gdx.input.setInputProcessor() as used in parallax example...
 * 
 */
public interface MyScreen
{
    /**
     * Called when the screen should render itself.
     * 
     * @param delta
     *            The time in seconds since the last render.
     */
    public void render(float delta);

    /** @see ApplicationListener#resize(int, int) */
    public void resize(int width, int height);

    /** Called when this screen becomes the current screen for a {@link Game}. */
    public void show();

    /**
     * Called when this screen is no longer the current screen for a
     * {@link Game}.
     */
    public void hide();

    /** @see ApplicationListener#pause() */
    public void pause();

    /** @see ApplicationListener#resume() */
    public void resume();

    /** Called when this screen should release all resources. */
    public void dispose();

    public boolean touchDown(int x, int y, int pointer, int newParam);

    public boolean touchDragged(int x, int y, int pointer);

    public boolean touchUp(int x, int y, int pointer, int button);
}
