using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Input.Touch;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Audio;

namespace LOL
{
    public class PostScene
    {
        /**
         * Track if the PostScene is visible. Initially it is not.
         */
        private bool mVisible;

        /**
         * The default is to show a simple PostScene after every level. If we don't
         * want any PostScene, we can disable it via this flag.
         */
        private bool mDisable;

        /**
         * The default text to display when a level is won.
         */
        private String mWinText;

        /**
         * The default text to display when a level is lost.
         */
        private String mLoseText;

        /**
         * The pictures and text to display when a level is won.
         */
        private List<Renderable> mWinSprites = new List<Renderable>();

        /**
         * The pictures and text to display when a level is lost.
         */
        private List<Renderable> mLoseSprites = new List<Renderable>();

        /**
         * Track if the level has been won or lost
         */
        private bool mWin;

        /**
         * Sound to play when the level is won
         */
        private SoundEffect mWinSound;

        /**
         * Sound to play when the level is lost
         */
        private SoundEffect mLoseSound;

        /**
         * Simple constructor: we need to be sure that the default win and lose text
         * are set
         */
        public PostScene() {
            mWinText = Lol.sGame.mConfig.getDefaultWinText();
            mLoseText = Lol.sGame.mConfig.getDefaultLoseText();
        }

        /**
         * Get the PostScene that is configured for the current level, or create a
         * blank one if none exists. We use this as a convenience since the LOL
         * paradigm is that the game desginer calls static methods on PostScene to
         * configure an existing object.
         * 
         * @return The current PostScene
         */
        private static PostScene getCurrPostScene() {
            PostScene ps = Level.sCurrent.mPostScene;
            if (ps != null)
                return ps;
            ps = new PostScene();
            Level.sCurrent.mPostScene = ps;
            return ps;
        }

        /**
         * Indicate that the level is over, and has either been won or lost
         * 
         * @param win Use 'true' to indicate that the level was won, 'false'
         *            otherwise
         */
        public void setWin(bool win) {
            mWin = win;

            // if PostScene is disabled for this level, just move to the next level
            if (mDisable) {
                finish();
                return;
            }

            // make the PostScene visible
            mVisible = true;

            // The default text to display can change at the last second, so we
            // don't compute it until right here... also, play music
            if (win) {
                if (mWinSound != null)
                    mWinSound.Play();
                mWinSprites
                .Add(Util.makeText(mWinText, 255, 255, 255,
                        Lol.sGame.mConfig.getDefaultFontFace(),
                        Lol.sGame.mConfig.getDefaultFontSize()));
            } else {
                if (mLoseSound != null)
                    mLoseSound.Play();
                mLoseSprites
                .Add(Util.makeText(mLoseText, 255, 255, 255,
                        Lol.sGame.mConfig.getDefaultFontFace(),
                        Lol.sGame.mConfig.getDefaultFontSize()));
            }
        }

        /**
         * This runs when the PostScene is cleared, and moves gameplay to the
         * appropriate level
         */
        private void finish() {
            // we turn off music here, so that music plays during the PostScene
            Level.sCurrent.stopMusic();

            // remove the previous level
            Level.sCurrent = null;

            // repeat on loss, else go to next level (or chooser)
            if (!mWin) {
                Lol.sGame.doPlayLevel(Lol.sGame.mCurrLevelNum);
            } else {
                if (Lol.sGame.mCurrLevelNum == Lol.sGame.mConfig.getNumLevels()) {
                    Lol.sGame.doChooser();
                } else {
                    Lol.sGame.mCurrLevelNum++;
                    Lol.sGame.doPlayLevel(Lol.sGame.mCurrLevelNum);
                }
            }
        }

        /**
         * Draw the current PostScene, but only if it is visible
         * 
         * @param sb The SpriteBatch to use to draw this PostScene
         * @return true if the PostScene was drawn, false otherwise
         */
        public bool render(SpriteBatch sb) {
            if (!mVisible)
                return false;
            TouchCollection tc = TouchPanel.GetState();
            
            if (tc.Count > 0) {
                mVisible = false;
                finish();
                return true;
            }
            List<Renderable> sprites = (mWin) ? mWinSprites : mLoseSprites;

            // next we clear the color buffer and set the camera matrices
            Lol.GD.Clear(Color.Black);

            Level.sCurrent.mHudCam.update();
            sb.setProjectionMatrix(Level.sCurrent.mHudCam.combined);
            sb.Begin();
            foreach (Renderable r in sprites)
                r.render(sb, new GameTime());
            sb.End();
            return true;
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Set the sound to play when the level is won
         * 
         * @param soundName Name of the sound file to play
         */
        public static void setWinSound(String soundName) {
            getCurrPostScene().mWinSound = Media.getSound(soundName);
        }

        /**
         * Set the sound to play when the level is lost
         * 
         * @param soundName Name of the sound file to play
         */
        public static void setLoseSound(String soundName) {
            getCurrPostScene().mLoseSound = Media.getSound(soundName);
        }

        /**
         * Set the text that should be drawn, centered, when the level is won
         * 
         * @param text The text to display. Use "" to disable
         */
        public static void setDefaultWinText(String text) {
            getCurrPostScene().mWinText = text;
        }

        /**
         * Set the text that should be drawn, centered, when the level is lost
         * 
         * @param text The text to display. Use "" to disable
         */
        public static void setDefaultLoseText(String text) {
            getCurrPostScene().mLoseText = text;
        }

        /**
         * Draw text on the PostScene that shows when the level is won
         * 
         * @param text The text to display
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param red The red component of the color (0-255)
         * @param green The green component of the color (0-255)
         * @param blue The blue component of the color (0-255)
         * @param fontName The name of the font to use
         * @param size The font size to use
         */
        public static void addExtraWinText(String text, int x, int y, int red, int green, int blue,
                String fontName, int size) {
            PostScene tmp = getCurrPostScene();
            tmp.mWinSprites.Add(Util.makeText(x, y, text, red, green, blue, fontName, size));
        }

        /**
         * Draw text on the PostScene that shows when the level is lost
         * 
         * @param text The text to display
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param red The red component of the color (0-255)
         * @param green The green component of the color (0-255)
         * @param blue The blue component of the color (0-255)
         * @param fontName The name of the font to use
         * @param size The font size to use
         */
        public static void addExtraLoseText(String text, int x, int y, int red, int green, int blue,
                String fontName, int size) {
            PostScene tmp = getCurrPostScene();
            tmp.mLoseSprites.Add(Util.makeText(x, y, text, red, green, blue, fontName, size));
        }

        /**
         * Draw text on the PostScene that shows when the level is won, and center
         * the text
         * 
         * @param text The text to display
         * @param red The red component of the color (0-255)
         * @param green The green component of the color (0-255)
         * @param blue The blue component of the color (0-255)
         * @param fontName The name of the font to use
         * @param size The font size to use
         */
        public static void addExtraWinText(String text, int red, int green, int blue, String fontName,
                int size) {
            PostScene tmp = getCurrPostScene();
            tmp.mWinSprites.Add(Util.makeText(text, red, green, blue, fontName, size));
        }

        /**
         * Draw text on the PostScene that shows when the level is lost, and center
         * the text
         * 
         * @param text The text to display
         * @param red The red component of the color (0-255)
         * @param green The green component of the color (0-255)
         * @param blue The blue component of the color (0-255)
         * @param fontName The name of the font to use
         * @param size The font size to use
         */
        public static void addExtraLoseText(String text, int red, int green, int blue, String fontName,
                int size) {
            PostScene tmp = getCurrPostScene();
            tmp.mLoseSprites.Add(Util.makeText(text, red, green, blue, fontName, size));
        }

        /**
         * Draw a picture on the PostScene that shows when the level is won
         * 
         * @param imgName The name of the image file that should be displayed
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param width The width of the image
         * @param height The height of the image
         */
        public static void addWinImage(String imgName, int x, int y, int width, int height) {
            PostScene tmp = getCurrPostScene();
            tmp.mWinSprites.Add(Util.makePicture(x, y, width, height, imgName));
        }

        /**
         * Draw a picture on the PostScene that shows when the level is lost
         * 
         * @param imgName The name of the image file that should be displayed
         * @param x The X coordinate of the bottom left corner
         * @param y The Y coordinate of the bottom left corner
         * @param width The width of the image
         * @param height The height of the image
         */
        public static void addLoseImage(String imgName, int x, int y, int width, int height) {
            PostScene tmp = getCurrPostScene();
            tmp.mLoseSprites.Add(Util.makePicture(x, y, width, height, imgName));
        }

        /**
         * Indicate that this level's PostScene should not be displayed
         */
        public static void disable() {
            getCurrPostScene().mDisable = true;
        }

    }
}
