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
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;

import edu.lehigh.cse.lol.internals.Renderable;

/**
 * PauseScene provides a way to suspend gameplay briefly and display information
 * to the player. A PauseScene can include arbitrary text and pictures.
 */
public class PauseScene {
    /**
     * When we draw clickable buttons on the screen, this is how we know where
     * the buttons are and what to do when they are clicked
     */
    private static class Button {
        /**
         * The region that can be clicked
         */
        Rectangle mRect;

        /**
         * The Callback to run when this button is pressed
         */
        LolCallback mCallback;
    }

    /**
     * All text and images that go on the PauseScreen are stored here
     */
    private final ArrayList<Renderable> mSprites = new ArrayList<Renderable>();

    /**
     * Track if the PauseScene is visible. Initially it is not.
     */
    boolean mVisible;

    /**
     * A PauseScene can have a back button, which we represent with this
     * Rectangle
     */
    private Rectangle mBackRectangle;

    /**
     * For handling touches
     */
    private final Vector3 mV = new Vector3();

    /**
     * Time that the PauseScene started being shown, so we can update timers
     */
    private long showingAt;

    /**
     * For debug rendering
     */
    private final ShapeRenderer mShapeRender = new ShapeRenderer();

    /**
     * For suppressing clear clicks
     */
    private boolean mSuppressClearClick;

    /**
     * All buttons on the PauseScene are stored here
     */
    private final ArrayList<Button> mButtons = new ArrayList<Button>();

    /**
     * Get the PauseScene that is configured for the current level, or create a
     * blank one if none exists. We use this as a convenience since the LOL
     * paradigm is that the game designer calls static methods on PauseScene to
     * configure an existing object.
     * 
     * @return The current PauseScene
     */
    private static PauseScene getCurrPauseScene() {
        PauseScene ps = Lol.sGame.mCurrentLevel.mPauseScene;
        if (ps != null)
            return ps;
        ps = new PauseScene();
        Lol.sGame.mCurrentLevel.mPauseScene = ps;
        return ps;
    }

    /**
     * Handler to run when the screen is tapped while the PauseScene is being
     * displayed
     */
    void onTap(float x, float y) {
        // check if it's to the 'back to chooser' button
        Lol.sGame.mCurrentLevel.mHudCam.unproject(mV.set(x, y, 0));
        if (mBackRectangle != null && mBackRectangle.contains(mV.x, mV.y)) {
            mVisible = false;
            Lol.sGame.handleBack();
        }

        // check for taps to the buttons
        for (Button b : mButtons) {
            if (b.mRect.contains(mV.x, mV.y)) {
                dismiss();
                b.mCallback.onEvent();
                return;
            }
        }

        // swallow any clicks
        Lol.sGame.mCurrentLevel.liftAllButtons(mV);

        // only clear the screen if click supress is off
        if (!mSuppressClearClick)
            dismiss();
    }

    /**
     * Internal method to draw a PauseScene
     * 
     * @param sb
     *            The SpriteBatch used to draw the text and pictures
     * @return true if the PauseScene was drawn, false otherwise
     */
    boolean render(SpriteBatch sb) {
        // if the pop-up scene is not visible, do nothing
        if (!mVisible)
            return false;

        // clear screen and draw sprites via HudCam
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Lol.sGame.mCurrentLevel.mHudCam.update();
        sb.setProjectionMatrix(Lol.sGame.mCurrentLevel.mHudCam.combined);
        sb.begin();
        for (Renderable r : mSprites)
            r.render(sb, 0);
        sb.end();

        // DEBUG: show where the buttons' boxes are
        if (Lol.sGame.mShowDebugBoxes) {
            mShapeRender.setProjectionMatrix(Lol.sGame.mCurrentLevel.mHudCam.combined);
            mShapeRender.begin(ShapeType.Line);
            mShapeRender.setColor(Color.RED);
            if (mBackRectangle != null)
                mShapeRender.rect(mBackRectangle.x, mBackRectangle.y, mBackRectangle.width, mBackRectangle.height);
            for (Button b : mButtons)
                mShapeRender.rect(b.mRect.x, b.mRect.y, b.mRect.width, b.mRect.height);
            mShapeRender.end();
        }

        return true;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Draw text on the PauseScene
     * 
     * @param text
     *            The text to display
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom left corner
     * @param red
     *            The red component of the color (0-255)
     * @param green
     *            The green component of the color (0-255)
     * @param blue
     *            The blue component of the color (0-255)
     * @param fontName
     *            The name of the font to use
     * @param size
     *            The font size to use
     */
    public static void addText(String text, int x, int y, int red, int green, int blue, String fontName, int size) {
        getCurrPauseScene().mSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    /**
     * Draw text on the PauseScene, and center the text vertically and
     * horizontally
     * 
     * @param text
     *            The text to display
     * @param red
     *            The red component of the color (0-255)
     * @param green
     *            The green component of the color (0-255)
     * @param blue
     *            The blue component of the color (0-255)
     * @param fontName
     *            The name of the font to use
     * @param size
     *            The font size to use
     */
    public static void addText(String text, int red, int green, int blue, String fontName, int size) {
        getCurrPauseScene().mSprites.add(Util.makeText(text, red, green, blue, fontName, size));
    }

    /**
     * Draw a picture on the PauseScene
     * 
     * @param imgName
     *            The name of the image file that should be displayed
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom left corner
     * @param width
     *            The width of the image
     * @param height
     *            The height of the image
     */
    public static void addImage(String imgName, int x, int y, int width, int height) {
        getCurrPauseScene().mSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * Draw a picture on the PauseScene, but indicate that touching the picture
     * will cause the level to stop playing, and control to return to the
     * chooser.
     * 
     * @param imgName
     *            The name of the image file that should be displayed
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom left corner
     * @param width
     *            The width of the image
     * @param height
     *            The height of the image
     */
    public static void addBackButton(String imgName, int x, int y, int width, int height) {
        getCurrPauseScene().mBackRectangle = new Rectangle(x, y, width, height);
        getCurrPauseScene().mSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * Show the pause screen
     */
    public static void show() {
        Timer.instance().stop();
        getCurrPauseScene().mVisible = true;
        getCurrPauseScene().showingAt = System.currentTimeMillis();
    }

    /**
     * Clear everything off of the level's pause scene, so it can be reused
     */
    public static void reset() {
        getCurrPauseScene().mButtons.clear();
        getCurrPauseScene().mSprites.clear();
        getCurrPauseScene().mBackRectangle = null;
    }

    /**
     * Place a new touchable button on the PauseScene. When the button is
     * pressed, the pause scene will be closed, and an onPauseSceneCallback
     * function will be called in your game, passing in the value of callbackId.
     * 
     * @param x
     *            The X coordinate of the bottom left corner
     * @param y
     *            The Y coordinate of the bottom left corner
     * @param width
     *            The width of the image
     * @param height
     *            The height of the image
     * @param callbackId
     *            The value to pass back to the main game
     */
    public static void addCallbackButton(int x, int y, int width, int height, LolCallback callback) {
        Button b = new Button();
        b.mRect = new Rectangle(x, y, width, height);
        b.mCallback = callback;
        getCurrPauseScene().mButtons.add(b);
    }

    /**
     * Indicate that tapping the non-button parts of the PauseScene shouldn't
     * return immediately to the game.
     */
    public static void suppressClearClick() {
        getCurrPauseScene().mSuppressClearClick = true;
    }

    /**
     * Stop showing the PauseScene
     */
    public static void dismiss() {
        // otherwise, just clear the pauseScene (be sure to resume timers)
        getCurrPauseScene().mVisible = false;
        long showTime = System.currentTimeMillis() - getCurrPauseScene().showingAt;
        Timer.instance().delay(showTime);
        Timer.instance().start();
    }
}
