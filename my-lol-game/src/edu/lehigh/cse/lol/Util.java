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

import java.util.Random;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * The Util class stores a few helper functions that we use inside of LOL, and a
 * few simple wrappers that we give to the game developer
 */
public class Util {
    /**
     * A random number generator... We provide this so that new game developers
     * don't create lots of Random()s throughout their code
     */
    private static Random sGenerator = new Random();

    /**
     * Create a Renderable that consists of an image
     * 
     * @param x The X coordinate of the bottom left corner, in pixels
     * @param y The Y coordinate of the bottom left corner, in pixels
     * @param width The image width, in pixels
     * @param height The image height, in pixels
     * @param imgName The file name for the image, or ""
     * @return A Renderable of the image
     */
    static Renderable makePicture(final float x, final float y, final float width,
            final float height, String imgName) {
        // set up the image to display
        //
        // NB: this will fail gracefully (no crash) for invalid file names
        TextureRegion[] trs = Media.getImage(imgName);
        final TextureRegion tr = (trs != null) ? trs[0] : null;
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
     * @param x The X coordinate of the bottom left corner, in pixels
     * @param y The Y coordinate of the bottom left corner, in pixels
     * @param message The text to display... note that it can't change on the
     *            fly
     * @param red The red component of the font color (0-255)
     * @param green The green component of the font color (0-255)
     * @param blue The blue component of the font color (0-255)
     * @param fontName The font to use
     * @param size The font size
     * @return A Renderable of the text
     */
    static Renderable makeText(final int x, final int y, final String message, final int red,
            final int green, final int blue, String fontName, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(((float)red) / 256, ((float)green) / 256, ((float)blue) / 256, 1);
                bf.drawMultiLine(sb, message, x, y + bf.getMultiLineBounds(message).height);
            }
        };
    }

    /**
     * Create a Renderable that consists of some text to draw. The text will be
     * centered vertically and horizontally
     * 
     * @param message The text to display... note that it can't change on the
     *            fly
     * @param red The red component of the font color (0-255)
     * @param green The green component of the font color (0-255)
     * @param blue The blue component of the font color (0-255)
     * @param fontName The font to use
     * @param size The font size
     * @return A Renderable of the text
     */
    static Renderable makeText(final String message, final int red, final int green,
            final int blue, String fontName, int size) {
        final BitmapFont bf = Media.getFont(fontName, size);
        final float x = Lol.sGame.mConfig.getScreenWidth() / 2
                - bf.getMultiLineBounds(message).width / 2;
        final float y = Lol.sGame.mConfig.getScreenHeight() / 2
                + bf.getMultiLineBounds(message).height / 2;
        return new Renderable() {
            @Override
            public void render(SpriteBatch sb, float elapsed) {
                bf.setColor(((float)red) / 256, ((float)green) / 256, ((float)blue) / 256, 1);
                bf.drawMultiLine(sb, message, x, y);
            }
        };
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Generate a random number x such that 0 <= x < max
     * 
     * @param max The largest number returned will be one less than max
     * @return a random integer
     */
    public static int getRandom(int max) {
        return sGenerator.nextInt(max);
    }

    /**
     * Draw a box on the scene Note: the box is actually four narrow rectangles
     * 
     * @param x0 X coordinate of top left corner
     * @param y0 Y coordinate of top left corner
     * @param x1 X coordinate of bottom right corner
     * @param y1 Y coordinate of bottom right corner
     * @param imgName name of the image file to use when drawing the rectangles
     * @param density Density of the rectangle. When in doubt, use 1
     * @param elasticity Elasticity of the rectangle. When in doubt, use 0
     * @param friction Friction of the rectangle. When in doubt, use 1
     */
    static public void drawBoundingBox(int x0, int y0, int x1, int y1, String imgName,
            float density, float elasticity, float friction) {
        // draw four rectangles and we're good
        Obstacle bottom = Obstacle.makeAsBox(x0 - 1, y0 - 1, Math.abs(x0 - x1) + 2, 1, imgName);
        bottom.setPhysics(density, elasticity, friction);

        Obstacle top = Obstacle.makeAsBox(x0 - 1, y1, Math.abs(x0 - x1) + 2, 1, imgName);
        top.setPhysics(density, elasticity, friction);

        Obstacle left = Obstacle.makeAsBox(x0 - 1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        left.setPhysics(density, elasticity, friction);

        Obstacle right = Obstacle.makeAsBox(x1, y0 - 1, 1, Math.abs(y0 - y1) + 2, imgName);
        right.setPhysics(density, elasticity, friction);
    }

    /**
     * Draw a picture on the current level Note: the order in which this is
     * called relative to other entities will determine whether they go under or
     * over this picture.
     * 
     * @param x X coordinate of bottom left corner
     * @param y Y coordinate of bottom left corner
     * @param width Width of the picture
     * @param height Height of this picture
     * @param imgName Name of the picture to display
     * @param zIndex The z index of the image. There are 5 planes: -2, -2, 0, 1,
     *            and 2. By default, everything goes to plane 0
     */
    public static void drawPicture(final int x, final int y, final int width, final int height,
            final String imgName, int zIndex) {
        Level.sCurrent.addSprite(Util.makePicture(x, y, width, height, imgName), zIndex);
    }
}
