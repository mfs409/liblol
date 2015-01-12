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

package edu.lehigh.cse.lol.internals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;

import java.util.ArrayList;

import edu.lehigh.cse.lol.Lol;
import edu.lehigh.cse.lol.LolCallback;
import edu.lehigh.cse.lol.Media;
import edu.lehigh.cse.lol.Util;

abstract public class QuickScene {
    /**
     * The text and pictures to display
     */
    public final ArrayList<Renderable> mSprites = new ArrayList<>();
    /**
     * All buttons on the scene are stored here
     */
    public final ArrayList<Button> mButtons = new ArrayList<>();
    /**
     * For handling touches
     */
    public final Vector3 mTmpVec = new Vector3();
    /**
     * For debug rendering
     */
    public final ShapeRenderer mShapeRender = new ShapeRenderer();
    /**
     * A flag for disabling the scene, so it won't display
     */
    public boolean mDisable;
    /**
     * Track if the Scene is visible. Initially it is not.
     */
    public boolean mVisible;
    /**
     * Sound to play when the scene is displayed
     */
    public Sound mSound;
    /**
     * Time that the Scene started being shown, so we can update timers
     */
    public long mDisplayTime;
    /**
     * True if we must click in order to clear the scene
     */
    public boolean mClickToClear = true;

    /**
     * Pause the timer when this screen is shown
     */
    protected void suspendClock() {
        // pause the timer
        Timer.instance().stop();
        mDisplayTime = System.currentTimeMillis();
    }

    /**
     * Internal method to draw a QuickScene
     *
     * @param sb The SpriteBatch used to draw the text and pictures
     * @return true if the PauseScene was drawn, false otherwise
     */
    public boolean render(SpriteBatch sb) {
        // if the scene is not visible, do nothing
        if (!mVisible)
            return false;

        // clear screen and draw images/text via HudCam
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
            for (Button b : mButtons)
                mShapeRender.rect(b.mRect.x, b.mRect.y, b.mRect.width, b.mRect.height);
            mShapeRender.end();
        }

        return true;
    }

    /**
     * Handler to run when the screen is tapped while the scene is being
     * displayed
     */
    public void onTap(float x, float y) {
        // ignore if not visible
        if (!mVisible)
            return;

        // check for taps to the buttons
        Lol.sGame.mCurrentLevel.mHudCam.unproject(mTmpVec.set(x, y, 0));
        for (Button b : mButtons) {
            if (b.mRect.contains(mTmpVec.x, mTmpVec.y)) {
                dismiss();
                b.mCallback.onEvent();
                return;
            }
        }

        // hide the scene only if it's click-to-clear
        if (mClickToClear) {
            dismiss();
            Lol.sGame.mCurrentLevel.liftAllButtons(mTmpVec);
        }
    }

    /**
     * This is the code to remove a scene. It is specific to they type of scene
     * being displayed
     */
    abstract protected void dismiss();

    /**
     * Set the sound to play when the screen is displayed
     *
     * @param soundName Name of the sound file to play
     */
    public void setSound(String soundName) {
        mSound = Media.getSound(soundName);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Add some text to the scene, and center it vertically and horizontally
     *
     * @param text     The text to display
     * @param red      Redness of the text color
     * @param green    Greenness of the text color
     * @param blue     Blueness of the text color
     * @param fontName The font file to use
     * @param size     The size of the text
     */
    public void addText(String text, int red, int green, int blue, String fontName, int size) {
        mSprites.add(Util.makeText(text, red, green, blue, fontName, size));
    }

    /**
     * Add some text to the scene, at an exact location
     *
     * @param text     The text to display
     * @param x        X coordinate of the text
     * @param y        Y coordinate of the text
     * @param red      Redness of the text color
     * @param green    Greenness of the text color
     * @param blue     Blueness of the text color
     * @param fontName The font file to use
     * @param size     The size of the text
     */
    public void addText(String text, int x, int y, int red, int green, int blue, String fontName, int size) {
        mSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    /**
     * Indicate that this scene should not be displayed
     */
    public void disable() {
        mDisable = true;
    }

    /**
     * Add an image to the scene
     *
     * @param imgName The file name for the image to display
     * @param x       X coordinate of the bottom left corner
     * @param y       Y coordinate of the bottom left corner
     * @param width   Width of the image
     * @param height  Height of the image
     */
    public void addImage(String imgName, int x, int y, int width, int height) {
        mSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * Draw a picture on the scene, but indicate that touching the picture will
     * cause the level to stop playing, and control to return to the chooser.
     *
     * @param imgName The name of the image file that should be displayed
     * @param x       The X coordinate of the bottom left corner
     * @param y       The Y coordinate of the bottom left corner
     * @param width   The width of the image
     * @param height  The height of the image
     */
    public void addBackButton(String imgName, int x, int y, int width, int height) {
        Button b = new Button();
        b.mRect = new Rectangle(x, y, width, height);
        b.mCallback = new LolCallback() {
            @Override
            public void onEvent() {
                mVisible = false;
                Lol.sGame.handleBack();
            }
        };
        mButtons.add(b);
        mSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * Place a new touchable button on the scene. When the button is pressed,
     * the scene will be closed, and the callback will run
     *
     * @param x        The X coordinate of the bottom left corner
     * @param y        The Y coordinate of the bottom left corner
     * @param width    The width of the image
     * @param height   The height of the image
     * @param callback The code to run when the button is pressed
     */
    public void addCallbackButton(int x, int y, int width, int height, LolCallback callback) {
        Button b = new Button();
        b.mRect = new Rectangle(x, y, width, height);
        b.mCallback = callback;
        mButtons.add(b);
    }

    /**
     * Indicate that tapping the non-button parts of the scene shouldn't return
     * immediately to the game.
     */
    public void suppressClearClick() {
        mClickToClear = false;
    }

    /**
     * When we draw clickable buttons on the screen, this is how we know where
     * the buttons are and what to do when they are clicked
     */
    public static class Button {
        /**
         * The region that can be clicked
         */
        public Rectangle mRect;

        /**
         * The Callback to run when this button is pressed
         */
        public LolCallback mCallback;
    }

}
