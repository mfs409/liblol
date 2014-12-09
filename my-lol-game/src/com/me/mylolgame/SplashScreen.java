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

import edu.lehigh.cse.lol.Splash;

public class SplashScreen {
    public static void display() {
        // Describe the regions of the screen that correspond to the play, help,
        // quit, and mute buttons. If you are having trouble figuring these out,
        // note that clicking on the splash screen will display xy coordinates
        // in the Console to help
        Splash.drawPlayButton(384, 182, 186, 104);
        Splash.drawHelpButton(96, 186, 160, 80);
        Splash.drawQuitButton(726, 186, 138, 78);
        Splash.drawMuteButton(900, 0, 50, 52, "audio_on.png", "audio_off.png");

        // Provide a name for the background image
        Splash.setBackground("splash.png");

        // Provide a name for the music file
        Splash.setMusic("tune.ogg");
    }
}
