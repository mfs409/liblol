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

package edu.lehigh.cse.lol;

import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

import edu.lehigh.cse.lol.internals.QuickScene;

/**
 * PreScene provides a way to put a pop-up on the screen before a level begins.
 * A PreScene can include arbitrary text and pictures.
 */
public class PreScene extends QuickScene {

    /**
     * Get the PreScene that is configured for the current level, or create a
     * blank one if none exists.
     *
     * @return The current PreScene
     */
    public static PreScene get() {
        PreScene scene = Lol.sGame.mCurrentLevel.mPreScene;
        if (scene != null)
            return scene;
        scene = new PreScene();
        // immediately make the scene visible
        scene.mVisible = true;
        Lol.sGame.mCurrentLevel.mPreScene = scene;
        // NB: disable the timer so the game doesn't start playing in the
        // background
        scene.suspendClock();
        return scene;
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Hide the PreScene, and resume any timers.
     */
    protected void dismiss() {
        mVisible = false;
        if (mClickToClear) {
            long showTime = System.currentTimeMillis() - mDisplayTime;
            Timer.instance().delay(showTime);
            Timer.instance().start();
        }
    }

    /**
     * The default is for a PreScene to show until the user touches it to
     * dismiss it. To have the PreScene disappear after a fixed time instead,
     * use this.
     *
     * @param duration The time, in seconds, before the PreScene should disappear.
     */
    public void setExpire(float duration) {
        if (duration > 0) {
            mClickToClear = false;
            // resume timers, or this won't work
            Timer.instance().start();
            Timer.schedule(new Task() {
                @Override
                public void run() {
                    dismiss();
                }
            }, duration);
        }
    }
}
