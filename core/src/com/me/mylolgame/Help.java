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

import edu.lehigh.cse.lol.Level;
import edu.lehigh.cse.lol.Lol;
import edu.lehigh.cse.lol.LolCallback;
import edu.lehigh.cse.lol.ScreenManager;

/**
 * Technically, Help can be anything... even playable levels. In this
 * demonstration, it's just a bit of information. It's a good place to put
 * instructions, credits, etc.
 */
public class Help implements ScreenManager {

    /**
     * Describe how to draw each level of help. Our help will have 2 screens
     */
    public void display(int which, final Level level) {
        // Our first scene describes the color coding that we use for the
        // different entities in the game
        if (which == 1) {
            // set up a basic screen
            level.configureCamera(48, 32);
            level.configureGravity(0, 0);
            level.setBackgroundColor(255, 255, 255);

            // put some information on the screen
            level.drawText(5, 26, "The levels of this game\ndemonstrate LOL features", 0, 0, 0, "arial.ttf", 40, 0);

            // draw a legend, using obstacles and text
            level.makeObstacleAsBox(5, 20, 3, 3, "greenball.png");
            level.drawText(9, 21, "You control the hero", 0, 0, 0, "arial.ttf", 24, 0);

            level.makeObstacleAsBox(5, 16, 3, 3, "blueball.png");
            level.drawText(9, 17, "Collect these goodies", 0, 0, 0, "arial.ttf", 24, 0);

            level.makeObstacleAsBox(5, 12, 3, 3, "redball.png");
            level.drawText(9, 13, "Avoid or defeat enemies", 0, 0, 0, "arial.ttf", 24, 0);

            level.makeObstacleAsBox(5, 8, 3, 3, "mustardball.png");
            level.drawText(9, 9, "Reach the destination", 0, 0, 0, "arial.ttf", 24, 0);

            level.makeObstacleAsBox(5, 4, 3, 3, "purpleball.png");
            level.drawText(9, 5, "These are walls", 0, 0, 0, "arial.ttf", 24, 0);

            level.makeObstacleAsBox(5, 0, 3, 3, "greyball.png");
            level.drawText(9, 1, "Throw projectiles", 0, 0, 0, "arial.ttf", 24, 0);

            // set up a control to go to the next level on screen press
            level.addCallbackControl(0, 0, 960, 640, "", new LolCallback() {
                public void onEvent() {
                    level.doHelp(2);
                }
            });
        }

        // Our second help scene is just here to show that it is possible to
        // have more than one help scene.
        else if (which == 2) {
            level.configureCamera(48, 32);
            level.configureGravity(0, 0);
            level.setBackgroundColor(255, 255, 0);

            // for now, just print a message
            level.drawText(10, 15, "Be sure to read the code\n" + "while you play, so you can see\n"
                    + "how everything works", 55, 110, 165, "arial.ttf", 14, 0);

            // set up a control to go to the splash screen on screen press
            level.addCallbackControl(0, 0, 960, 640, "", new LolCallback() {
                public void onEvent() {
                    level.doSplash();
                }
            });
        }
    }
}
