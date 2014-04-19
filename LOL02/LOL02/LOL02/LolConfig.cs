using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using LOL;

namespace LOL02
{
    public class LolConfig: LolConfiguration
    {
        /**
         * The width of the screen of your device, in pixels. The actual value here
         * isn't too important, the main point is that the ratio between this and
         * height will determine how LOL scales your game.
         */
        public int getScreenWidth() {
            return Lol.sGame.GraphicsDevice.DisplayMode.Height;
        }

        /**
         * The height of the screen of your device, in pixels.
         */
        public int getScreenHeight() {
            return Lol.sGame.GraphicsDevice.DisplayMode.Width;
        }

        /**
         * The number of levels in the game
         */
        public int getNumLevels() {
            return 82;
        }

        /**
         * The number of help levels in the game
         */
        public int getNumHelpScenes() {
            return 2;
        }

        /**
         * Is vibration support enabled?
         */
        public bool getVibration() {
            return true;
        }

        /**
         * Is the game in "unlock mode"? In unlock mode, every level can be played.
         * When unlock mode is false, you can't play a level if you haven't already
         * finished the preceding level.
         */
        public bool getUnlockMode() {
            return true;
        }

        /**
         * Is the game in "debug mode"? Debug mode includes drawing the
         * frames-per-second on every level, drawing boxes around every touchable
         * element, and drawing the raw shape for every entity in the game
         */
        public bool showDebugBoxes() {
            return true;
        }

        /**
         * To store "unlock" information, we need a unique identity for this game.
         * When you rename "com.me.mylolgame", be sure to update this!
         */
        public String getStorageKey() {
            return "com.me.mylolgame.prefs";
        }

        /**
         * When getting started, it's useful to use the "easy" versions of some
         * methods, which rely on an implicit default font. You should provide the
         * name of the font through this file, and be sure the font file (with a
         * .ttf extension) is in your assets folder.
         */
        public String getDefaultFontFace() {
            return "Default";
        }

        /**
         * The default font size for messages that are written to the screen
         */
        public int getDefaultFontSize() {
            return 32;
        }

        /**
         * The red component of the default font color
         */
        public int getDefaultFontRed() {
            return 0;
        }

        /**
         * The green component of the default font color
         */
        public int getDefaultFontGreen() {
            return 0;
        }

        /**
         * The blue component of the default font color
         */
        public int getDefaultFontBlue() {
            return 0;
        }

        /**
         * When getting started, you may wish to use default text at the end of each
         * level. This is the default text when winning the level:
         */
        public String getDefaultWinText() {
            return "Good Job";
        }

        /**
         * When getting started, you may wish to use default text at the end of each
         * level. This is the default text when losing the level:
         */
        public String getDefaultLoseText() {
            return "Try Again";
        }

        /**
         * When running on the desktop, this value will be used as the name on the
         * window's title bar
         */
        public String getGameTitle() {
            return "My LOL Game";
        }
    }
}
