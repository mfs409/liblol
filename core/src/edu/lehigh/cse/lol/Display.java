/**
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p/>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p/>
 * For more information, please refer to <http://unlicense.org>
 */

package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * LOL Games have a heads-up display (hud). The hud is a place for displaying
 * text and drawing touchable buttons, so that as the hero moves through the
 * level, the buttons and text can remain at the same place on the screen. This
 * class encapsulates all of the displayable text.
 */
public class Display {
    /**
     * The level in which this display is to be drawn
     */
    Level mLevel;

    /**
     * What color should we use to draw text
     */
    Color mColor = new Color(0, 0, 0, 1);

    /**
     * The font object to use
     */
    BitmapFont mFont;

    /**
     * The constructor keeps track of the text color, but that's it.
     *
     * @param red      The red portion of text color (0-255)
     * @param green    The green portion of text color (0-255)
     * @param blue     The blue portion of text color (0-255)
     * @param fontName The name of the .ttf font file to use
     * @param fontSize The point size of the font
     */
    Display(Level level, int red, int green, int blue, String fontName, int fontSize) {
        mLevel = level;
        mColor.r = ((float) red) / 256;
        mColor.g = ((float) green) / 256;
        mColor.b = ((float) blue) / 256;
        mFont = level.mMedia.getFont(fontName, fontSize);
    }

    /**
     * Render the text. Since each control needs to getLoseScene its text at the time it
     * is rendered, we don't provide a default implementation.
     *
     * @param sb The SpriteBatch to use to draw the image
     */
    void render(SpriteBatch sb) {
    }
}
