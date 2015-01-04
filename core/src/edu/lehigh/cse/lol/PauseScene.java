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

import edu.lehigh.cse.lol.internals.QuickScene;

/**
 * PauseScene provides a way to suspend gameplay briefly and display information
 * to the player. A PauseScene can include arbitrary text and pictures.
 */
public class PauseScene extends QuickScene {

    /**
     * Get the PauseScene that is configured for the current level, or create a
     * blank one if none exists.
     *
     * @return The current PauseScene
     */
    public static PauseScene get() {
        PauseScene scene = Lol.sGame.mCurrentLevel.mPauseScene;
        if (scene != null)
            return scene;
        scene = new PauseScene();
        Lol.sGame.mCurrentLevel.mPauseScene = scene;
        return scene;
    }

    /**
     * Clear everything off of the level's pause scene, so it can be reused
     */
    public void reset() {
        Lol.sGame.mCurrentLevel.mPauseScene = new PauseScene();
    }

    /**
     * Show the pause screen
     */
    public void show() {
        Timer.instance().stop();
        mVisible = true;
        mDisplayTime = System.currentTimeMillis();
        if (mSound != null)
            mSound.play(Facts.getGameFact("volume", 1));
    }

    /**
     * Stop showing the PauseScene
     */
    public void dismiss() {
        // clear the pauseScene (be sure to resume timers)
        mVisible = false;
        if (mClickToClear) {
            long showTime = System.currentTimeMillis() - mDisplayTime;
            Timer.instance().delay(showTime);
            Timer.instance().start();
        }
    }
}
