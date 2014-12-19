/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.internals.Renderable;

/**
 * PreScene provides a way to put a pop-up on the screen before a level begins.
 * A PreScene can include arbitrary text and pictures.
 */
public class PreScene {
    /**
     * The text and pictures to display
     */
    private final ArrayList<Renderable> mSprites = new ArrayList<Renderable>();

    /**
     * True if we must click in order to clear the PreScene
     */
    private boolean mClickToClear = true;

    /**
     * True when the scene is being displayed
     */
    boolean mVisible = true;

    /**
     * For handling touches
     */
    private final Vector3 mV = new Vector3();

    /**
     * Time that the PauseScene started being shown, so we can update timers
     */
    private long showingAt;

    /**
     * Get the PreScene that is configured for the current level, or create a
     * blank one if none exists. We use this as a convenience since the LOL
     * paradigm is that the game designer calls static methods on PreScene to
     * configure an existing object.
     * 
     * @return The current PreScene
     */
    private static PreScene getCurrPreScene() {
        PreScene ps = Lol.sGame.mCurrentLevel.mPreScene;
        if (ps != null)
            return ps;
        ps = new PreScene();
        Lol.sGame.mCurrentLevel.mPreScene = ps;
        // pause the timer
        Timer.instance().stop();
        ps.showingAt = System.currentTimeMillis();
        return ps;
    }

    /**
     * Hide the PreScene, and resume any timers.
     */
    private void hide() {
        // resume timers only on clickToClear, because that's the only time
        // they're suspended
        if (mClickToClear) {
            long showTime = System.currentTimeMillis() - showingAt;
            showTime /= 1000000;
            Timer.instance().delay(showTime);
            Timer.instance().start();
        }
        Lol.sGame.mCurrentLevel.mPreScene.mVisible = false;
    }

    /**
     * Handler to run when the screen is tapped while the PreScene is being
     * displayed
     */
    void onTap(float x, float y) {
        Lol.sGame.mCurrentLevel.mHudCam.unproject(mV.set(x, y, 0));
        // hide the scene only if it's click-to-clear
        if (mVisible && mClickToClear) {
            hide();
            Lol.sGame.mCurrentLevel.liftAllButtons(mV);
        }
    }

    /**
     * Render this PreScene
     * 
     * @param sb
     *            The SpriteBatch to use when rendering
     * @return true if we drew something, false otherwise
     */
    boolean render(SpriteBatch sb) {
        // if the scene is not visible, do nothing
        if (!mVisible)
            return false;

        // clear screen and draw sprites... we can use the level's hudCam
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Lol.sGame.mCurrentLevel.mHudCam.update();
        sb.setProjectionMatrix(Lol.sGame.mCurrentLevel.mHudCam.combined);
        sb.begin();
        for (Renderable r : mSprites)
            r.render(sb, 0);
        sb.end();
        return true;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Add some text to the PreScene
     * 
     * @param text
     *            The text to display
     * @param x
     *            X coordinate of the text
     * @param y
     *            Y coordinate of the text
     * @param red
     *            Redness of the text color
     * @param green
     *            Greenness of the text color
     * @param blue
     *            Blueness of the text color
     * @param fontName
     *            The font file to use
     * @param size
     *            The size of the text
     */
    public static void addText(String text, int x, int y, int red, int green, int blue, String fontName, int size) {
        getCurrPreScene().mSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    /**
     * Add some text to the PreScene, and center it vertically and horizontally
     * 
     * @param text
     *            The text to display
     * @param red
     *            Redness of the text color
     * @param green
     *            Greenness of the text color
     * @param blue
     *            Blueness of the text color
     * @param fontName
     *            The font file to use
     * @param size
     *            The size of the text
     */
    public static void addText(String text, int red, int green, int blue, String fontName, int size) {
        getCurrPreScene().mSprites.add(Util.makeText(text, red, green, blue, fontName, size));
    }

    /**
     * Add an image to the PreScene
     * 
     * @param imgName
     *            The file name for the image to display
     * @param x
     *            X coordinate of the bottom left corner
     * @param y
     *            Y coordinate of the bottom left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     */
    public static void addImage(String imgName, int x, int y, int width, int height) {
        getCurrPreScene().mSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * The default is for a PreScene to show until the user touches it to
     * dismiss it. To have the PreScene disappear after a fixed time instead,
     * use this.
     * 
     * @param duration
     *            The time, in seconds, before the PreScene should disappear.
     */
    public static void setExpire(float duration) {
        if (duration > 0) {
            getCurrPreScene().mClickToClear = false;
            // resume timers, or this won't work
            Timer.instance().start();
            Timer.schedule(new Task() {
                @Override
                public void run() {
                    getCurrPreScene().hide();
                }
            }, duration);
        }
    }
}
