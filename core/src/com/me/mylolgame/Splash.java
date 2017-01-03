/**
 * This is free and unencumbered software released into the public domain.
 * <p>
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * <p>
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * <p>
 * For more information, please refer to <http://unlicense.org>
 */

package com.me.mylolgame;

import edu.lehigh.cse.lol.Level;
import edu.lehigh.cse.lol.Obstacle;
import edu.lehigh.cse.lol.ScreenManager;
import edu.lehigh.cse.lol.TouchEventHandler;

/**
 * Splash encapsulates the code that will be run to configure the opening screen of the game.
 * Typically this has buttons for playing, getting help, and quitting.
 */
class Splash implements ScreenManager {
    /**
     * There is usually only one splash screen. However, the ScreenManager interface requires
     * display() to take a parameter for which screen to display.  We ignore it.
     *
     * @param index Which splash screen should be displayed (typically you can ignore this)
     * @param level The physics-based world that comprises the splash screen
     */
    public void display(int index, final Level level) {
        // set up a simple level. We could make interesting things happen, since
        // we've got a physics world, but we won't.

        // draw the background. Note that "Play", "Help", and "Quit" are part of
        // this background image.
        level.drawPicture(0, 0, 48, 32, "splash.png", 0);

        // start the music
        level.setMusic("tune.ogg");

        // This is the Play button... it switches to the first screen of the
        // level chooser. You could jump straight to the first level by using
        // "doLevel(1)", but check the configuration in MyConfig... there's a
        // field you should change if you don't want the 'back' button to go
        // from that level to the chooser.
        level.addTapControl(19.2f, 9.1f, 9.3f, 5.2f, "", new TouchEventHandler() {
            public boolean go(float x, float y) {
                level.doChooser(1);
                return true;
            }
        });

        // This is the Help button... it switches to the first screen of the
        // help system
        level.addTapControl(4.8f, 9.3f, 8, 4, "", new TouchEventHandler() {
            public boolean go(float x, float y) {
                level.doHelp(1);
                return true;
            }
        });

        // This is the Quit button
        level.addTapControl(36.3f, 9.3f, 6.9f, 3.9f, "", new TouchEventHandler() {
            public boolean go(float x, float y) {
                level.doQuit();
                return true;
            }
        });

        // Mute button is a tad tricky... we'll do it as an obstacle
        final Obstacle o = level.makeObstacleAsBox(45, 0, 2.5f, 2.5f, "");
        // figure out which image to use for the obstacle based on the current
        // volume state
        if (level.getVolume()) {
            o.setImage("audio_off.png");
        } else {
            o.setImage("audio_on.png");
        }
        // when the obstacle is touched, change the mute and then update the
        // picture for the obstacle
        o.setTapCallback(new TouchEventHandler() {
            @Override
            public boolean go(float eventPositionX, float eventPositionY) {
                level.toggleMute();
                if (level.getVolume()) {
                    o.setImage("audio_off.png");
                } else {
                    o.setImage("audio_on.png");
                }
                return true;
            }
        });
    }
}