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
import edu.lehigh.cse.lol.ScreenManager;
import edu.lehigh.cse.lol.TouchEventHandler;

/**
 * Technically, Help can be anything... even playable levels. In this
 * demonstration, it's just a bit of information. It's a good place to put
 * instructions, credits, etc.
 */
public class Help implements ScreenManager {

    /**
     * Describe how to draw each level of help. Our help will have 2 screens
     */
    public void display(int index, final Level level) {
        // Our first scene describes the color coding that we use for the
        // different entities in the game
        if (index == 1) {
            // set up a basic screen
            level.setBackgroundColor("#FFFFFF");

            // put some information on the screen
            level.addText(5, 26, "arial.ttf", "#000000", 40, "", "", level.DisplayFixedText("The levels of this game\ndemonstrate LOL features"), 0);

            // draw a legend, using obstacles and text
            level.makeObstacleAsBox(5, 20, 3, 3, "greenball.png");
            level.addText(9, 21, "arial.ttf", "#000000", 24, "", "", level.DisplayFixedText("You control the hero"), 0);

            level.makeObstacleAsBox(5, 16, 3, 3, "blueball.png");
            level.addText(9, 17, "arial.ttf", "#000000", 24, "", "", level.DisplayFixedText("Collect these goodies"), 0);

            level.makeObstacleAsBox(5, 12, 3, 3, "redball.png");
            level.addText(9, 13, "arial.ttf", "#000000", 24, "", "", level.DisplayFixedText("Avoid or defeat enemies"), 0);

            level.makeObstacleAsBox(5, 8, 3, 3, "mustardball.png");
            level.addText(9, 9, "arial.ttf", "#000000", 24, "", "", level.DisplayFixedText("Reach the destination"), 0);

            level.makeObstacleAsBox(5, 4, 3, 3, "purpleball.png");
            level.addText(9, 5, "arial.ttf", "#000000", 24, "", "", level.DisplayFixedText("These are walls"), 0);

            level.makeObstacleAsBox(5, 0, 3, 3, "greyball.png");
            level.addText(9, 1, "arial.ttf", "#000000", 24, "", "", level.DisplayFixedText("Throw projectiles"), 0);

            // set up a control to go to the next level on screen press
            level.addTapControl(0, 0, 960, 640, "", new TouchEventHandler() {
                public boolean go(float x, float y) {
                    level.doHelp(2);
                    return true;
                }
            });
        }

        // Our second help scene is just here to show that it is possible to
        // have more than one help scene.
        else if (index == 2) {
            level.setBackgroundColor("#FFFF00");

            // for now, just print a message
            level.addText(10, 15, "arial.ttf", "#376EA5", 14, "", "", level.DisplayFixedText("Be sure to read the code\nwhile you play, so you can see\nhow everything works"), 0);

            // set up a control to go to the splash screen on screen press
            level.addTapControl(0, 0, 960, 640, "", new TouchEventHandler() {
                public boolean go(float x, float y) {
                    level.doSplash();
                    return true;
                }
            });
        }
    }
}
