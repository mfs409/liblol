using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace LOL
{
    /**
     * The LOLConfiguration interface describes some configuration details of the
     * game
     */
    public interface LolConfiguration
    {
        /**
         * The width of the screen of your device, in pixels. The actual value here
         * isn't too important, the main point is that the ratio between this and
         * height will determine how LOL scales your game.
         */
        int getScreenWidth();

        /**
         * The height of the screen of your device, in pixels.
         */
        int getScreenHeight();

        /**
         * The number of levels in the game
         */
        int getNumLevels();

        /**
         * The number of help levels in the game
         */
        int getNumHelpScenes();

        /**
         * Is vibration support enabled?
         */
        bool getVibration();

        /**
         * Is the game in "unlock mode"? In unlock mode, every level can be played.
         * When unlock mode is false, you can't play a level if you haven't already
         * finished the preceding level.
         */
        bool getUnlockMode();

        /**
         * Is the game in "debug mode"? Debug mode includes drawing the
         * frames-per-second on every level, drawing boxes around every touchable
         * element, and drawing the raw shape for every entity in the game
         */
        bool showDebugBoxes();

        /**
         * To store "unlock" information, we need a unique identity for this game.
         * It should probably be based on whatever you rename "com.me.mylolgame"
         */
        String getStorageKey();

        /**
         * When getting started, it's useful to use the "easy" versions of some
         * methods, which rely on an implicit default font. You should provide the
         * name of the font through this file, and be sure the font file (with a
         * .ttf extension) is in your assets folder.
         */
        String getDefaultFontFace();

        /**
         * The size of the default font
         */
        int getDefaultFontSize();

        /**
         * The red component of the color of the default font
         */
        int getDefaultFontRed();

        /**
         * The green component of the color of the default font
         */
        int getDefaultFontGreen();

        /**
         * The blue component of the color of the default font
         */
        int getDefaultFontBlue();

        /**
         * When getting started, you may wish to use default text at the end of each
         * level. This is the default text when winning the level:
         */
        String getDefaultWinText();

        /**
         * When getting started, you may wish to use default text at the end of each
         * level. This is the default text when losing the level:
         */
        String getDefaultLoseText();

        /**
         * When running on the desktop, this value will be used as the name on the
         * window's title bar
         */
        String getGameTitle();
    }

}
