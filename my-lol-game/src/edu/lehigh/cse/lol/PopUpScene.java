package edu.lehigh.cse.lol;

// TODO: enable this to stand on its own as a "pause" screen, in addition to being useful as the common core for
// PreScene and PostScene... That will probably involve factoring much of this into a parent class...

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import edu.lehigh.cse.lol.Level.Renderable;

public class PopUpScene
{
    // TODO: handle the "click to remove" vs. "timer to remove" here, instead of in pre/post
    
    ArrayList<Renderable> _sprites = new ArrayList<Renderable>();

    /**
     * Draw a picture on this scene
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
     */
    void drawPicture(final int x, final int y, final int width, final int height, String imgName)
    {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        TextureRegion[] trs = Media.getImage(imgName);
        final TextureRegion tr = (trs != null) ? trs[0] : null;
        _sprites.add(new Renderable()
        {
            @Override
            public void render(SpriteBatch sb, float elapsed)
            {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        });
    }

    /**
     * Print a message on this scene.
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
    public void drawText(final int x, final int y, final String message, final int red, final int green,
            final int blue, int size)
    {
        final BitmapFont bf = Media.getFont("arial.ttf", size);
        _sprites.add(new Renderable()
        {
            @Override
            public void render(SpriteBatch sb, float elapsed)
            {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                bf.drawMultiLine(sb, message, x, y);
            }
        });
    }

    boolean       _visible;

    boolean render(SpriteBatch _spriteRender, LOL _game)
    {
        if (!_visible)
            return false;

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
