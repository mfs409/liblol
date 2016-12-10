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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.Random;

import edu.lehigh.cse.lol.internals.Renderable;

/**
 * The Util class stores a few helper functions that we use inside of LOL, and a
 * few simple wrappers that we give to the game developer
 */
public class Util {
    /**
     * Use this for determining bounds of text boxes
     */
    static GlyphLayout glyphLayout = new GlyphLayout();

    /**
     * A random number generator... We provide this so that new game developers
     * don't create lots of Random()s throughout their code
     */
    private static Random sGenerator = new Random();

    /**
     * Create a Renderable that consists of an image
     *
     * @param x       The X coordinate of the bottom left corner, in pixels
     * @param y       The Y coordinate of the bottom left corner, in pixels
     * @param width   The image width, in pixels
     * @param height  The image height, in pixels
     * @param imgName The file name for the image, or ""
     * @return A Renderable of the image
     */
    public static Renderable makePicture(final float x, final float y, final float width, final float height,
                                         String imgName) {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        final TextureRegion tr = Media.getImage(imgName);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                if (tr != null)
                    sb.draw(tr, x, y, 0, 0, width, height, 1, 1, 0);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw
     *
     * @param x        The X coordinate of the bottom left corner, in pixels
     * @param y        The Y coordinate of the bottom left corner, in pixels
     * @param message  The text to display... note that it can't change on the fly
     * @param red      The red component of the font color (0-255)
     * @param green    The green component of the font color (0-255)
     * @param blue     The blue component of the font color (0-255)
     * @param fontName The font to use
     * @param size     The font size
     * @return A Renderable of the text
     */
    public static Renderable makeText(final int x, final int y, final String message, final int red, final int green,
                                      final int blue, String fontName, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                glyphLayout.setText(bf, message);
                bf.draw(sb, message, x, y + glyphLayout.height);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw. The text will be
     * centered vertically and horizontally on the screen
     *
     * @param message  The text to display... note that it can't change on the fly
     * @param red      The red component of the font color (0-255)
     * @param green    The green component of the font color (0-255)
     * @param blue     The blue component of the font color (0-255)
     * @param fontName The font to use
     * @param size     The font size
     * @return A Renderable of the text
     */
    public static Renderable makeText(final String message, final int red, final int green, final int blue,
                                      String fontName, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        glyphLayout.setText(bf, message);
        final float x = Lol.sGame.mWidth / 2 - glyphLayout.width / 2;
        final float y = Lol.sGame.mHeight / 2 + glyphLayout.height / 2;
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(((float) red) / 256, ((float) green) / 256, ((float) blue) / 256, 1);
                bf.draw(sb, message, x, y);
            }
        };
    }

    /**
     * Instead of using Gdx.app.log directly, and potentially writing a lot of
     * debug info in a production setting, we use this to only dump to the log
     * when debug mode is on
     *
     * @param tag  The message tag
     * @param text The message text
     */
    static void message(String tag, String text) {
        if (Lol.sGame.mShowDebugBoxes)
            Gdx.app.log(tag, text);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Generate a random number x such that 0 &lt;= x &lt; max
     *
     * @param max The largest number returned will be one less than max
     * @return a random integer
     */
    public static int getRandom(int max) {
        return sGenerator.nextInt(max);
    }


    /**
     * A helper method to draw text nicely. In GDX, we draw everything by giving
     * the bottom left corner, except text, which takes the top left corner.
     * This function handles the conversion, so that we can use bottom-left.
     *
     * @param x       The X coordinate of the bottom left corner (in pixels)
     * @param y       The Y coordinate of the bottom left corner (in pixels)
     * @param message The text to display
     * @param bf      The BitmapFont object to use for the text's font
     * @param sb      The SpriteBatch used to render the text
     */
    static void drawTextTransposed(int x, int y, String message, BitmapFont bf, SpriteBatch sb) {
        glyphLayout.setText(bf, message);
        bf.draw(sb, message, x, y + glyphLayout.height);
    }


    /**
     * Look up a fact that was stored for the current game session. If no such
     * fact exists, defaultVal will be returned.
     *
     * @param factName   The name used to store the fact
     * @param defaultVal The value to return if the fact does not exist
     * @return The integer value corresponding to the last value stored
     */
     static int getGameFact(String factName, int defaultVal) {
        Preferences prefs = Gdx.app.getPreferences(Lol.sGame.mStorageKey);
        return prefs.getInteger(factName, defaultVal);
    }

    /**
     * Save a fact about the current game session. If the factName has already
     * been used for this game session, the new value will overwrite the old.
     *
     * @param factName  The name for the fact being saved
     * @param factValue The integer value that is the fact being saved
     */
      static void putGameFact(String factName, int factValue) {
        Preferences prefs = Gdx.app.getPreferences(Lol.sGame.mStorageKey);
        prefs.putInteger(factName, factValue);
        prefs.flush();
    }

}
