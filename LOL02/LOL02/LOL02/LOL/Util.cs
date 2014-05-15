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

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Input.Touch;

namespace LOL
{
    /**
     * The Util class stores a few helper functions that we use inside of LOL, and a
     * few simple wrappers that we give to the game developer
     */
    public class Util
    {
        /**
         * Last touch position
         */
        public static Vector2 touch = Vector2.Zero;

        /**
         * Checks if a touch input was released.
         * 
         * @return true if touch input was released, false otherwise
         */
        public static bool justTouched()
        {
            TouchCollection tc = TouchPanel.GetState();
            if (tc.Count > 0)
            {
                bool touched = tc[0].State == TouchLocationState.Released;
                if (touched)
                {
                    touch = tc[0].Position;
                    return true;
                }
            }
            return false;
        }

        /**
         * Converts coords from LibGDX/Android dimensions to WP8 dimensions.
         * 
         * @param x the X coordinate in LibGDX dimensions
         * @return x in WP8 dimensions
         */
        public static int ax(int x)
        {
            int w = Lol.sGame.lolConfig().getScreenWidth();
            return (int)((float)x / 480 * w);
        }

        /**
         * Converts height from LibGDX/Android dimensions to WP8 dimensions.
         * 
         * @param y the height in LibGDX dimensions
         * @return height in WP8 dimensions
         */
        public static int ah(int y)
        {
            return ay(y, false);
        }

        /**
         * Converts Y from LibGDX/Android dimensions to WP8 dimensions.
         * 
         * @param y the Y coordinate in LibGDX dimensions
         * @return y to WP8 dimensions
         */
        public static int ay(int y)
        {
            return ay(y, true);
        }

        /**
         * Converts coords from LibGDX/Android dimensions to WP8 dimensions.
         * 
         * @param y the Y coordinate in LibGDX dimensions
         * @param useHeight invert Y (true)
         * @return y to WP8 dimensions
         */
        public static int ay(int y, bool useHeight)
        {
            int h = Lol.sGame.lolConfig().getScreenHeight();
            if (useHeight)
            {
                return h - (int)((float)y / 320 * h);
            }
            return (int)((float)y / 320 * h);
        }

        /**
         * Converts coords from LibGDX/Android dimensions to WP8 dimensions.
         * 
         * @param y the Y coordinate in LibGDX dimensions
         * @param h the height of the object for inversion purposes
         * @return y inverted and converted to WP8 dimensions
         */
        public static int ay(int y, int h)
        {
            return ay(y, true) - ah(h);
        }

        /**
         * Logs a message to the Debug console.
         * 
         * @param type the type of message
         * @param msg the message to log
         */
        public static void log(String type, String msg)
        {
            System.Diagnostics.Debug.WriteLine("[" + type + "]: " + msg);
        }

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
        public static AnonRenderable makePicture(float x, float y, float width,
                float height, String imgName) {
            // set up the image to display
            

            // NB: this will fail gracefully (no crash) for invalid file names
            Texture2D[] trs = Media.getImage(imgName);
            Texture2D tr = (trs != null) ? trs[0] : null;
            return new AnonRenderable(delegate(SpriteBatch sb, GameTime gameTime) {
                    if (tr != null)
                        sb.Draw(tr, new Rectangle((int)x, (int)y, (int) width, (int) height), null, Color.White, 0, Vector2.Zero, SpriteEffects.None, 0);
                });
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
        public static AnonRenderable makeText(int x, int y, String message, int red,
                int green, int blue, String fontName, int size) {
            Font bf = Media.getFont(fontName, size);
            AnonRenderable r= new AnonRenderable(delegate(SpriteBatch sb, GameTime elapsed) {
                    bf.Color = new Color(((float)red) / 256, ((float)green) / 256, ((float)blue) / 256, 1);
                    sb.DrawString(bf.Face, message, new Vector2(x, y), bf.Color);
                });
            return r;
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
        public static Renderable makeText(String message, int red, int green,
                int blue, String fontName, int size) {
            Font bf = Media.getFont(fontName, size);
            float x = Lol.sGame.mConfig.getScreenWidth() / 2
                    - bf.Face.MeasureString(message).X / 2;
            float y = Lol.sGame.mConfig.getScreenHeight() / 2
                    + bf.Face.MeasureString(message).Y / 2;
            AnonRenderable r = new AnonRenderable(delegate(SpriteBatch sb, GameTime elapsed) {
                    bf.Color = new Color(((float)red) / 256, ((float)green) / 256, ((float)blue) / 256, 1);
                    sb.DrawString(bf.Face, message, new Vector2(x, y), bf.Color);
                });
            return r;
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
            return sGenerator.Next(max);
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
            Obstacle bottom = Obstacle.MakeAsBox(x0 - 1, y0 - 1, Math.Abs(x0 - x1) + 2, 1, imgName);
            bottom.SetPhysics(density, elasticity, friction);

            Obstacle top = Obstacle.MakeAsBox(x0 - 1, y1, Math.Abs(x0 - x1) + 2, 1, imgName);
            top.SetPhysics(density, elasticity, friction);

            Obstacle left = Obstacle.MakeAsBox(x0 - 1, y0 - 1, 1, Math.Abs(y0 - y1) + 2, imgName);
            left.SetPhysics(density, elasticity, friction);

            Obstacle right = Obstacle.MakeAsBox(x1, y0 - 1, 1, Math.Abs(y0 - y1) + 2, imgName);
            right.SetPhysics(density, elasticity, friction);
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
        public static void drawPicture(int x, int y, int width, int height,
                String imgName, int zIndex) {
            OrthographicCamera c = Level.sCurrent.mGameCam;
            x = c.screenX(x);
            y = c.screenY(y);
            width = c.screenX(width);
            height = c.screenY(height);
            Level.sCurrent.addSprite(Util.makePicture(x, y, width, height, imgName), zIndex);
        }

        /**
         * Draw some text on the current level Note: the order in which this is
         * called relative to other entities will determine whether they go under or
         * over this text.
         * 
         * @param x X coordinate of bottom left corner of the text
         * @param y Y coordinate of bottom left corner of the text
         * @param text The text to display
         * @param red The red component of the color (0-255)
         * @param green The green component of the color (0-255)
         * @param blue The blue component of the color (0-255)
         * @param fontName The name of the font file to use
         * @param size The font size to use
         * @param zIndex The z index of the image. There are 5 planes: -2, -2, 0, 1,
         *            and 2. By default, everything goes to plane 0
         */
        public static void drawText(int x, int y, String text, int red,
                int green, int blue, String fontName, int size, int zIndex) {
            Font bf = Media.getFont(fontName, size);
            AnonRenderable r = new AnonRenderable(delegate(SpriteBatch sb, GameTime elapsed)
            {
                bf.Color = new Color(((float)red) / 256, ((float)green) / 256, ((float)blue) / 256, 1);
                // NOTE: Can't scale text
                //bf.setScale(1 / Physics.PIXEL_METER_RATIO);
                sb.DrawString(bf.Face, text, new Vector2(Level.sCurrent.mGameCam.drawX(x), Level.sCurrent.mGameCam.drawY(y) + bf.Face.MeasureString(text).Y), bf.Color);
                //bf.setScale(1);
            });
            Level.sCurrent.addSprite(r, zIndex);
        }

    }
}
