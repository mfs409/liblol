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

import edu.lehigh.cse.lol.Control;
import edu.lehigh.cse.lol.Level;
import edu.lehigh.cse.lol.Lol;
import edu.lehigh.cse.lol.LolCallback;
import edu.lehigh.cse.lol.Obstacle;
import edu.lehigh.cse.lol.Physics;
import edu.lehigh.cse.lol.ScreenManager;
import edu.lehigh.cse.lol.Util;

/**
 * Splash encapsulates the code that will be run to configure the opening screen
 * of the game. Typically this has buttons for playing, getting help, and
 * quitting.
 */
public class Splash implements ScreenManager {

    /**
     * There is usually only one splash screen. However, the ScreenManager
     * interface requires display() to take a parameter.  We ignore it.
     */
    public void display(int which) {
        // set up a simple level. We could make interesting things happen, since
        // we've got a physics world, but we won't.
        Level.configure(48, 32);
        Physics.configure(0, 0);

        // draw the background. Note that "Play", "Help", and "Quit" are part of
        // this background image.
        Util.drawPicture(0, 0, 48, 32, "splash.png", 0);

        // start the music
        Level.setMusic("tune.ogg");

        // This is the Play button... it switches to the first screen of the
        // level chooser. You could jump straight to the first level by using
        // "doLevel(1)", but check the configuration in MyLolGame... there's a
        // field you should change if you don't want the 'back' button to go
        // from that level to the chooser.
        Control.addCallbackControl(384, 182, 186, 104, "", new LolCallback() {
            public void onEvent() {
                Lol.doChooser(1);
            }
        });

        // This is the Help button... it switches to the first screen of the
        // help system
        Control.addCallbackControl(96, 186, 160, 80, "", new LolCallback() {
            public void onEvent() {
                Lol.doHelp(1);
            }
        });

        // This is the Quit button
        Control.addCallbackControl(726, 186, 138, 78, "", new LolCallback() {
            public void onEvent() {
                Lol.doQuit();
            }
        });

        // Mute button is a tad tricky... we'll do it as an obstacle
        Obstacle o = Obstacle.makeAsBox(45, 0, 2.5f, 2.5f, "");
        // figure out which image to use for the obstacle based on the current
        // volume state
        if (Lol.getVolume()) {
            o.setImage("audio_off.png", 0);
        } else {
            o.setImage("audio_on.png", 0);
        }
        // when the obstacle is touched, change the mute and then update the
        // picture for the obstacle
        o.setTouchCallback(0, 0, 0, 0, false, new LolCallback() {
            public void onEvent() {
                Lol.toggleMute();
                if (Lol.getVolume()) {
                    mAttachedActor.setImage("audio_off.png", 0);
                } else {
                    mAttachedActor.setImage("audio_on.png", 0);
                }
            }
        });
    }
}