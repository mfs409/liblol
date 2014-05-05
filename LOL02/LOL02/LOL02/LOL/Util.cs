using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Input.Touch;

namespace LOL
{
    public class Util
    {
        public static Vector2 touch = Vector2.Zero;

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

        public static int ax(int x)
        {
            int w = Lol.sGame.lolConfig().getScreenWidth();
            return (int)((float)x / 480 * w);
        }

        public static int ah(int y)
        {
            return ay(y, false);
        }

        public static int ay(int y)
        {
            return ay(y, true);
        }

        public static int ay(int y, bool useHeight)
        {
            int h = Lol.sGame.lolConfig().getScreenHeight();
            if (useHeight)
            {
                return h - (int)((float)y / 320 * h); //210
            }
            return (int)((float)y / 320 * h); //210
        }

        public static int ay(int y, int h)
        {
            return ay(y, true) - ah(h);
        }

        public static void log(String type, String msg)
        {
            // TODO: Log some stuff...
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
            //
            // NB: this will fail gracefully (no crash) for invalid file names
            Texture2D[] trs = Media.getImage(imgName);
            Texture2D tr = (trs != null) ? trs[0] : null;
            return new AnonRenderable(delegate(SpriteBatch sb, GameTime gameTime) {
                    if (tr != null)
                        sb.Draw(tr, new Vector2(x, y), new Rectangle(0, 0, (int) width, (int) height), Color.White);
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
                    // NOTE: drawMultiline was here
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
                sb.DrawString(bf.Face, text, new Vector2(x, y + bf.Face.MeasureString(text).Y), bf.Color);

                //bf.setScale(1);
            });
            Level.sCurrent.addSprite(r, zIndex);
        }

    }
}
