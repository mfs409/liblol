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

import edu.lehigh.cse.lol.HelpLevel;

public class HelpScreens {
    public static void display(int whichScene) {
        // Note: this is not very good help right now. It's just a demo

        // Our first scene describes the color coding that we use for the
        // different entities in the game
        if (whichScene == 1) {
            HelpLevel.configure(255, 255, 255);
            HelpLevel.drawText(50, 240, "The levels of this game\ndemonstrate LOL features");

            HelpLevel.drawPicture(50, 200, 30, 30, "greenball.png");
            HelpLevel.drawText(100, 200, "You control the hero");

            HelpLevel.drawPicture(50, 160, 30, 30, "blueball.png");
            HelpLevel.drawText(100, 160, "Collect these goodies");

            HelpLevel.drawPicture(50, 120, 30, 30, "redball.png");
            HelpLevel.drawText(100, 120, "Avoid or defeat enemies");

            HelpLevel.drawPicture(50, 80, 30, 30, "mustardball.png");
            HelpLevel.drawText(100, 80, "Reach the destination");

            HelpLevel.drawPicture(50, 40, 30, 30, "purpleball.png");
            HelpLevel.drawText(100, 40, "These are walls");

            HelpLevel.drawPicture(50, 0, 30, 30, "greyball.png");
            HelpLevel.drawText(100, 0, "Throw projectiles");
        }
        // Our second help scene is just here to show that it is possible to
        // have more than one help scene.
        else if (whichScene == 2) {
            HelpLevel.configure(255, 255, 0);
            HelpLevel.drawText(100, 150, "Be sure to read the MyLolGame.java code\n"
                    + "while you play, so you can see\n" + "how everything works", 55, 110, 165, "arial.ttf", 14);
        }
    }
}
