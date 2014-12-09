package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * This interface is used to store items that can be rendered
 */
interface LolRenderable {
    /**
     * Render something to the screen
     * 
     * @param sb
     *            The SpriteBatch to use for rendering
     * @param elapsed
     *            The time since the last render
     */
    void render(SpriteBatch sb, float elapsed);
}