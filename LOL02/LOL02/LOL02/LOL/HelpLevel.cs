using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input.Touch;

namespace LOL
{
    public class HelpLevel: GameScreen
    {
        /**
         * The background color of the help level
         */
        private Color mColor = new Color();

        /**
         * All the sprites that need to be drawn
         */
        private List<Renderable> mSprites = new List<Renderable>();

        /**
         * The camera to use when drawing
         */
        private OrthographicCamera mHelpCam;

        /**
         * The spritebatch to use when drawing
         */
        private SpriteBatch mSb;

        /**
         * In LOL, we avoid having the game designer construct objects. To that end,
         * the HelpLevel is accessed through a singleton.
         */
        public static HelpLevel sCurrentLevel;

        /**
         * When the game designer creates a help level, she uses configure, which
         * calls this to create the internal context
         */
        private HelpLevel() {
            // save the static context
            sCurrentLevel = this;

            // set up the camera
            int camWidth = Lol.sGame.mConfig.getScreenWidth();
            int camHeight = Lol.sGame.mConfig.getScreenHeight();
            mHelpCam = new OrthographicCamera(camWidth, camHeight);
            mHelpCam.position.set(camWidth / 2, camHeight / 2, 0);

            // set up the renderer
            mSb = new SpriteBatch(Lol.GD);
        }

        /**
         * The main render loop for Help Levels. There's nothing fancy here
         * 
         * @param delta The time that has transpired since the last render
         */
        public void Draw(GameTime delta) {
            // Poll for a new touch (down-press)
            // On down-press, either advance to the next help scene, or return to
            // the splash screen
            TouchCollection tc = TouchPanel.GetState();
            if (tc.Count > 0) {
                if (Lol.sGame.mCurrHelpNum < Lol.sGame.mConfig.getNumHelpScenes()) {
                    Lol.sGame.mCurrHelpNum++;
                    Lol.sGame.doHelpLevel(Lol.sGame.mCurrHelpNum);
                    return;
                }
                Lol.sGame.doSplash();
                return;
            }

            // render all sprites
            Lol.GD.Clear(Color.Black);
            mHelpCam.update();
            mSb.setProjectionMatrix(mHelpCam.combined);
            mSb.Begin();
            foreach (Renderable c in mSprites)
                c.render(mSb, new GameTime());
            mSb.End();
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Configure a help level by setting its background color
         * 
         * @param red The red component of the background color (0-255)
         * @param green The green component of the background color (0-255)
         * @param blue The blue component of the background color (0-255)
         */
        public static void configure(int red, int green, int blue) {
            sCurrentLevel = new HelpLevel();
            sCurrentLevel.mColor.R = (byte)(((float)red) / 256);
            sCurrentLevel.mColor.G = (byte)(((float)green) / 256);
            sCurrentLevel.mColor.B = (byte)(((float)blue) / 256);
        }

        /**
         * Draw a picture on the current help scene Note: the order in which this is
         * called relative to other entities will determine whether they go under or
         * over this picture.
         * 
         * @param x X coordinate of bottom left corner
         * @param y Y coordinate of bottom left corner
         * @param width Width of the picture
         * @param height Height of this picture
         * @param imgName Name of the picture to display
         */
        public static void drawPicture(int x, int y, int width, int height,
                String imgName) {
            // set up the image to display
            sCurrentLevel.mSprites.Add(Util.makePicture(x, y, width, height, imgName));
        }

        /**
         * Print a message on the current help scene. This version of the addText
         * method uses the default font.
         * 
         * @param x X coordinate of text
         * @param y Y coordinate of text
         * @param message The message to display
         */
        static public void drawText(int x, int y, String message) {
            sCurrentLevel.mSprites.Add(Util.makeText(x, y, message,
                    Lol.sGame.mConfig.getDefaultFontRed(), Lol.sGame.mConfig.getDefaultFontGreen(),
                    Lol.sGame.mConfig.getDefaultFontBlue(), Lol.sGame.mConfig.getDefaultFontFace(),
                    Lol.sGame.mConfig.getDefaultFontSize()));
        }

        /**
         * Print a message on the current help scene. This version of the addText
         * method allows the programmer to specify the appearance of the font
         * 
         * @param x The X coordinate of the bottom left corner (in pixels)
         * @param y The Y coordinate of the bottom left corner (in pixels)
         * @param message The message to display
         * @param red The red portion of text color (0-255)
         * @param green The green portion of text color (0-255)
         * @param blue The blue portion of text color (0-255)
         * @param fontname The name of the font file to use
         * @param size The font size to use (20 is usually a good value)
         */
        static public void drawText(int x, int y, String message, int red,
                int green, int blue, String fontName, int size) {
            sCurrentLevel.mSprites.Add(Util.makeText(x, y, message, red, green, blue, fontName, size));
        }

    }
}
