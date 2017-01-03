/**
 * This is free and unencumbered software released into the public domain.
 * <p/>
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

package com.me.mylolgame;

import edu.lehigh.cse.lol.Config;

/**
 * Any configuration that the programmer needs to provide to Lol should go here.
 * <p/>
 * Config stores things like screen dimensions, default text and font configuration,
 * and the names of all the assets (images and sounds) used by the game.
 * <p/>
 * Be sure to look at the Levels.java file for how each level of the game is
 * drawn, as well as Splash.java, Chooser.java, Help.java, and Store.java.
 */
public class MyConfig extends Config {

    /**
     * The MyConfig object is used to pass configuration information to the LOL
     * system.
     * <p/>
     * To see documentation for any of these variables, hover your mouse
     * over the word on the left side of the equals sign.
     */
    public MyConfig() {
        // The size of the screen, and some game behavior configuration
        mWidth = 960;
        mHeight = 640;
        mPixelMeterRatio = 20;
        mEnableVibration = true;
        mGameTitle = "My Lol Game";
        mDefaultWinText = "Good Job";
        mDefaultLoseText = "Try Again";
        mShowDebugBoxes = true;

        // Chooser configuration
        mNumLevels = 94;
        mEnableChooser = true;
        mUnlockAllLevels = true;

        // For persistent storage on Android
        mStorageKey = "com.me.mylolgame.prefs";

        // Font configuration
        mDefaultFontFace = "arial.ttf";
        mDefaultFontSize = 32;
        mDefaultFontColor = "#FFFFFF";

        // list the images that the game will use
        mImageNames = new String[]{
                // The non-animated actors in the game
                "greenball.png", "mustardball.png", "redball.png", "blueball.png",
                "purpleball.png", "greyball.png",
                // Images that we use for buttons in the Splash and Chooser
                "leftarrow.png", "rightarrow.png", "backarrow.png", "leveltile.png",
                "audio_on.png", "audio_off.png",
                // Background images we use on QuickScenes
                "red.png", "msg1.png", "msg2.png", "fade.png",
                // The backgrounds for the Splash and Chooser
                "splash.png", "chooser.png",
                // Layers for Parallax backgrounds and foregrounds
                "mid.png", "front.png", "back.png",
                // The animation for a star with legs
                "legstar1.png", "legstar2.png", "legstar3.png", "legstar4.png",
                "legstar5.png", "legstar6.png", "legstar7.png", "legstar8.png",
                // The animation for the star with legs, with each image flipped
                "fliplegstar1.png", "fliplegstar2.png", "fliplegstar3.png", "fliplegstar4.png",
                "fliplegstar5.png", "fliplegstar6.png", "fliplegstar7.png", "fliplegstar8.png",
                // The flying star animation
                "flystar1.png", "flystar2.png",
                // Animation for a star that expands and then disappears
                "starburst1.png", "starburst2.png", "starburst3.png", "starburst4.png",
                // eight colored stars
                "colorstar1.png", "colorstar2.png", "colorstar3.png", "colorstar4.png",
                "colorstar5.png", "colorstar6.png", "colorstar7.png", "colorstar8.png",
        };

        // list the sound effects that the game will use
        mSoundNames = new String[]{
                "hipitch.ogg", "lowpitch.ogg",
                "losesound.ogg", "winsound.ogg",
                "slowdown.ogg", "woowoowoo.ogg", "fwapfwap.ogg",
        };

        // list the background music files that the game will use
        mMusicNames = new String[]{"tune.ogg"};

        // don't change these lines unless you know what you are doing
        mLevels = new Levels();
        mChooser = new Chooser();
        mHelp = new Help();
        mSplash = new Splash();
        mStore = new Store();
    }
}