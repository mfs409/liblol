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

import edu.lehigh.cse.lol.Facts;
import edu.lehigh.cse.lol.Level;
import edu.lehigh.cse.lol.Lol;
import edu.lehigh.cse.lol.LolCallback;
import edu.lehigh.cse.lol.Obstacle;
import edu.lehigh.cse.lol.Physics;
import edu.lehigh.cse.lol.ScreenManager;
import edu.lehigh.cse.lol.Util;

/**
 * Chooser draws the level chooser screens. Our chooser code is pretty
 * straightforward. However, the different screens are drawn in different ways,
 * to show how we can write more effective code once we are comfortable with
 * loops and basic geometry.
 */
public class Chooser implements ScreenManager {

    /**
     * This is a helper function for drawing a level button. If the level is
     * locked, the button isn't playable. Otherwise, the player can tap the
     * button to start a level.
     *
     * @param x      X coordinate of the bottom left corner of the button
     * @param y      Y coordinate of the bottom left corner of the button
     * @param width  width of the button
     * @param height height of the button
     * @param level  which level to play when the button is tapped
     */
    static void drawLevelButton(float x, float y, float width, float height, final int level) {
        // figure out the last unlocked level
        int unlocked = Facts.getGameFact("unlocked", 1);

        // for each button, start by drawing an obstacle
        Obstacle tile = Obstacle.makeAsBox(x, y, width, height, "leveltile.png");

        // if this level is unlocked, or if we are in unlocked mode, then attach
        // a callback and print the level number with a touchCallback, and then
        // put text on top of it
        if (level <= unlocked || Lol.getUnlockMode()) {
            tile.setTouchCallback(0, 0, 0, 0, false, new LolCallback() {
                public void onEvent() {
                    Lol.doLevel(level);
                }
            });
            Util.drawTextCentered(x + width / 2, y + height / 2, "" + level, 255, 255, 255, "arial.ttf", 56, 0);
        }
        // otherwise, just print an X
        else {
            Util.drawTextCentered(x + width / 2, y + height / 2, "X", 255, 255, 255, "arial.ttf", 56, 0);
        }
    }

    /**
     * This helper function is for drawing the button that takes us to the previous chooser screen
     *
     * @param x            X coordinate of bottom left corner of the button
     * @param y            Y coordinate of bottom left corner of the button
     * @param width        width of the button
     * @param height       height of the button
     * @param chooserLevel The chooser screen to create
     */
    static void drawPrevButton(float x, float y, float width, float height, final int chooserLevel) {
        Obstacle prev = Obstacle.makeAsBox(x, y, width, height, "leftarrow.png");
        prev.setTouchCallback(0, 0, 0, 0, false, new LolCallback() {
            public void onEvent() {
                Lol.doChooser(chooserLevel);
            }
        });
    }

    /**
     * This helper function is for drawing the button that takes us to the next chooser screen
     *
     * @param x            X coordinate of bottom left corner of the button
     * @param y            Y coordinate of bottom left corner of the button
     * @param width        width of the button
     * @param height       height of the button
     * @param chooserLevel The chooser screen to create
     */
    static void drawNextButton(float x, float y, float width, float height, final int chooserLevel) {
        Obstacle prev = Obstacle.makeAsBox(x, y, width, height, "rightarrow.png");
        prev.setTouchCallback(0, 0, 0, 0, false, new LolCallback() {
            public void onEvent() {
                Lol.doChooser(chooserLevel);
            }
        });
    }

    /**
     * This helper function is for drawing the button that takes us back to the splash screen
     *
     * @param x      X coordinate of bottom left corner of the button
     * @param y      Y coordinate of bottom left corner of the button
     * @param width  width of the button
     * @param height height of the button
     */
    static void drawSplashButton(float x, float y, float width, float height) {
        Obstacle prev = Obstacle.makeAsBox(x, y, width, height, "backarrow.png");
        prev.setTouchCallback(0, 0, 0, 0, false, new LolCallback() {
            public void onEvent() {
                Lol.doSplash();
            }
        });
    }

    /**
     * Describe how to draw each level of the chooser. Our chooser will have 15
     * levels per screen, so we need 7 screens.
     */
    public void display(int which) {
        // screen 1: show 1-->15
        //
        // NB: in this screen, we assume you haven't done much programming, so
        // we draw each button with its own line of code, and we don't use any
        // variables.
        if (which == 1) {
            Level.configure(48, 32);
            Physics.configure(0, 0);

            // set up background and music
            Util.drawPicture(0, 0, 48, 32, "chooser.png", 0);
            Level.setMusic("tune.ogg");

            // for each button, draw an obstacle with a touchCallback, and then
            // put text on top of it. Our buttons are 5x5, we have 1.5 meters
            // between buttons, there's an 8.5 meter border on the left and
            // right, and there's an 11 meter border on the top
            drawLevelButton(8.5f, 16, 5, 5, 1);
            drawLevelButton(15f, 16, 5, 5, 2);
            drawLevelButton(21.5f, 16, 5, 5, 3);
            drawLevelButton(28f, 16, 5, 5, 4);
            drawLevelButton(34.5f, 16, 5, 5, 5);

            drawLevelButton(8.5f, 9.5f, 5, 5, 6);
            drawLevelButton(15f, 9.5f, 5, 5, 7);
            drawLevelButton(21.5f, 9.5f, 5, 5, 8);
            drawLevelButton(28f, 9.5f, 5, 5, 9);
            drawLevelButton(34.5f, 9.5f, 5, 5, 10);

            drawLevelButton(8.5f, 3f, 5, 5, 11);
            drawLevelButton(15f, 3f, 5, 5, 12);
            drawLevelButton(21.5f, 3f, 5, 5, 13);
            drawLevelButton(28f, 3f, 5, 5, 14);
            drawLevelButton(34.5f, 3f, 5, 5, 15);

            // draw the navigation buttons
            drawNextButton(43, 9.5f, 5, 5, 2);
            drawSplashButton(0, 0, 5, 5);
        }

        // screen 2: show levels 16-->30
        //
        // NB: this time, we'll use three loops to create the three rows. By
        // using some variables in the loops, we get the same effect as the
        // previous screen. The code isn't simpler yet, but it's still pretty
        // easy to understand.
        else if (which == 2) {
            Level.configure(48, 32);
            Physics.configure(0, 0);

            // set up background and music
            Util.drawPicture(0, 0, 48, 32, "chooser.png", 0);
            Level.setMusic("tune.ogg");

            // let's use a loop to do each row
            float x = 8.5f;
            int level = 16;
            for (int i = 0; i < 5; ++i) {
                drawLevelButton(x, 16, 5, 5, level);
                level++;
                x += 6.5f;
            }

            x = 8.5f;
            for (int i = 0; i < 5; ++i) {
                drawLevelButton(x, 9.5f, 5, 5, level);
                level++;
                x += 6.5f;
            }

            x = 8.5f;
            for (int i = 0; i < 5; ++i) {
                drawLevelButton(x, 3, 5, 5, level);
                level++;
                x += 6.5f;
            }

            // draw the navigation buttons
            drawPrevButton(0, 9.5f, 5, 5, 1);
            drawNextButton(43, 9.5f, 5, 5, 3);
            drawSplashButton(0, 0, 5, 5);
        }

        // screen 3: show levels 31-->45
        //
        // NB: now we use a nested pair of loops, and we can do three rows in
        // just a few more lines than one row.
        else if (which == 3) {
            Level.configure(48, 32);
            Physics.configure(0, 0);

            // set up background and music
            Util.drawPicture(0, 0, 48, 32, "chooser.png", 0);
            Level.setMusic("tune.ogg");

            // let's use a loop to do each row and each column
            float y = 16;
            int level = 31;
            for (int r = 0; r < 3; ++r) {
                float x = 8.5f;
                for (int i = 0; i < 5; ++i) {
                    drawLevelButton(x, y, 5, 5, level);
                    level++;
                    x += 6.5f;
                }
                y -= 6.5f;
            }

            // draw the navigation buttons
            drawPrevButton(0, 9.5f, 5, 5, 2);
            drawNextButton(43, 9.5f, 5, 5, 4);
            drawSplashButton(0, 0, 5, 5);
        }

        // let's be a little more advanced... we can do all of these with the
        // same block of code:
        // screen 4: show levels 46-->60
        // screen 5: show levels 61-->75
        // screen 6: show levels 75-->90
        else if (which < 7) {
            // set-up
            Level.configure(48, 32);
            Physics.configure(0, 0);
            Util.drawPicture(0, 0, 48, 32, "chooser.png", 0);
            Level.setMusic("tune.ogg");

            // levels
            float y = 16;
            int level = (which - 1) * 15 + 1;
            for (int r = 0; r < 3; ++r) {
                float x = 8.5f;
                for (int i = 0; i < 5; ++i) {
                    drawLevelButton(x, y, 5, 5, level);
                    level++;
                    x += 6.5f;
                }
                y -= 6.5f;
            }

            // navigation buttons
            drawPrevButton(0, 9.5f, 5, 5, which - 1);
            drawNextButton(43, 9.5f, 5, 5, which + 1);
            drawSplashButton(0, 0, 5, 5);
        }

        // The final case is the 7th screen, which just shows levels 91 and 92.
        // We'll just do it by hand.
        else if (which == 7) {
            Level.configure(48, 32);
            Physics.configure(0, 0);

            // set up background and music
            Util.drawPicture(0, 0, 48, 32, "chooser.png", 0);
            Level.setMusic("tune.ogg");

            // we have 92 levels, so just draw a few buttons for now...
            drawLevelButton(8.5f, 16, 5, 5, 91);
            drawLevelButton(15f, 16, 5, 5, 92);
            drawLevelButton(21.5f, 16, 5, 5, 93);

            // draw the navigation buttons
            drawPrevButton(0, 9.5f, 5, 5, 6);
            drawSplashButton(0, 0, 5, 5);
        }
    }
}
