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

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import edu.lehigh.cse.lol.internals.GestureAction;
import edu.lehigh.cse.lol.internals.LolAction;

/**
 * LOL Games have a heads-up display (hud). The hud is a place for displaying
 * text and drawing touchable buttons, so that as the hero moves through the
 * level, the buttons and text can remain at the same place on the screen. This
 * class encapsulates all of the touchable buttons.
 */
public class Control {
    /**
     * The level where this control is drawn
     */
    Level mLevel;
    /**
     * Should we run code when this Control is touched?
     */
    boolean mIsTouchable;

    /**
     * Code to run when this Control is touched
     */
    GestureAction mGestureAction;

    /**
     * The rectangle on the screen that is touchable
     */
    Rectangle mRange;

    /**
     * For disabling a control and stopping its rendering
     */
    boolean mIsActive = true;

    /**
     * What image should we display, if this Control has an image associated
     * with it?
     */
    TextureRegion mImage;

    /**
     * Create a control on the heads up display
     *
     * @param imgName The name of the image to display. If "" is given as the name,
     *                it will not crash.
     * @param x       The X coordinate (in pixels) of the bottom left corner.
     * @param y       The Y coordinate (in pixels) of the bottom left corner.
     * @param width   The width of the Control
     * @param height  The height of the Control
     */
    Control(Level level, String imgName, int x, int y, int width, int height) {
        mLevel = level;
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        TextureRegion tr = level.mMedia.getImage(imgName);
        if (tr != null)
            mImage = tr;

        // set up the touchable range for the image
        mRange = new Rectangle(x, y, width, height);
        mIsTouchable = true;
    }

    /**
     * Render the control
     *
     * @param sb The SpriteBatch to use to draw the image
     */
    void render(SpriteBatch sb) {
        if (mIsActive && mImage != null)
            sb.draw(mImage, mRange.x, mRange.y, 0, 0, mRange.width, mRange.height, 1, 1, 0);
    }

    /**
     * Disable the control, so that it doesn't getLoseScene displayed
     */
    public void setInactive() {
        mIsActive = false;
    }

    /**
     * Enable the control, so that it gets displayed again
     */
    public void setActive() {
        mIsActive = true;
    }

    /**
     * Disable touch for this control
     */
    public void disableTouch() {
        mIsTouchable = false;
    }

    /**
     * Enable touch for this control
     */
    public void enableTouch() {
        mIsTouchable = true;
    }
}
