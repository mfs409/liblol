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

package com.me.mylolgame;

// TODO: add a way to have multiple screens in the introduction to a level
// TODO: make sure all angles use same units (degrees or radians)
// TODO: Add SVG collision callbacks?
// TODO: clean up the last 6 levels
// TODO: add a 'demos' section?
// TODO: add a 'store'?
// TODO: add 'share' button?
// TODO: Verify comments
// TODO: verify chooser and level music stops on Android events
// TODO: Hero animation sequences could use work... we can lose information (e.g., if
//       invincibility runs out while jumping), and we don't have invincible+X or jump+crawl animation
// TODO: Make sure we have good error messages for common mistakes (filenames, animation, routes)
// TODO: make panning return to the chasesprite more nicely
// TODO: verify tilt gets its directions correct
// TODO: make sure music stops when game quits (on phone)

import edu.lehigh.cse.lol.ChooserConfiguration;
import edu.lehigh.cse.lol.Lol;
import edu.lehigh.cse.lol.LolConfiguration;
import edu.lehigh.cse.lol.Media;

public class MyLolGame extends Lol {

    /**
     * Configure all the images and sounds used by our game
     */
    @Override
    public void nameResources() {
        // load regular (non-animated) images
        Media.registerImage("greenball.png");
        Media.registerImage("mustardball.png");
        Media.registerImage("red.png");
        Media.registerImage("leftarrow.png");
        Media.registerImage("rightarrow.png");
        Media.registerImage("backarrow.png");
        Media.registerImage("redball.png");
        Media.registerImage("blueball.png");
        Media.registerImage("purpleball.png");
        Media.registerImage("msg1.png");
        Media.registerImage("msg2.png");
        Media.registerImage("fade.png");
        Media.registerImage("greyball.png");
        Media.registerImage("leveltile.png");
        Media.registerImage("audio_on.png");
        Media.registerImage("audio_off.png");

        // load the image we show on the main screen
        Media.registerImage("splash.png");

        // load the image we show on the chooser screen
        Media.registerImage("chooser.png");

        // load background images
        Media.registerImage("mid.png");
        Media.registerImage("front.png");
        Media.registerImage("back.png");

        // load animated images (a.k.a. Sprite Sheets)
        Media.registerAnimatableImage("stars.png", 8, 1);
        Media.registerAnimatableImage("stars_flipped.png", 8, 1);
        Media.registerAnimatableImage("flystar.png", 2, 1);
        Media.registerAnimatableImage("starburst.png", 4, 1);
        Media.registerAnimatableImage("colorstar.png", 8, 1);

        // load sounds
        Media.registerSound("hipitch.ogg");
        Media.registerSound("lowpitch.ogg");
        Media.registerSound("losesound.ogg");
        Media.registerSound("slowdown.ogg");
        Media.registerSound("woowoowoo.ogg");
        Media.registerSound("fwapfwap.ogg");
        Media.registerSound("winsound.ogg");

        // load background music
        Media.registerMusic("tune.ogg", true);
    }

    /**
     * Describe how to draw the first scene that displays when the game app is
     * started
     */
    @Override
    public void configureSplash() {
        SplashScreen.display();
    }

    /**
     * Describe how to draw the initial state of each level of our game
     *
     * @param whichLevel
     *            The level to be drawn
     */
    @Override
    public void configureLevel(int whichLevel) {
        GameLevels.display(whichLevel);
    }

    /**
     * Describe how each help scene ought to be drawn. Every game must implement
     * this method to describe how each help scene should appear. Note that you
     * *must* specify the maximum number of help scenes for your game in the
     * Config.java file. If you specify "0", then you can leave this code blank.
     *
     * @param whichScene
     *            The help scene being drawn. The game engine will set this
     *            value to indicate which scene needs to be drawn.
     */
    @Override
    public void configureHelpScene(int whichScene) {
        HelpScreens.display(whichScene);
    }

    @Override
    public void configureChooser(int whichScreen) {
        ChooserScreens.display(whichScreen);
    }
    
    /**
     * Mandatory method. Don't change this.
     */
    @Override
    public LolConfiguration lolConfig() {
        return new LolConfig();
    }

    /**
     * Mandatory method. Don't change this.
     */
    @Override
    public ChooserConfiguration chooserConfig() {
        return new ChooserConfig();
    }
}
