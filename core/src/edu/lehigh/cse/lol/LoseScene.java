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

import edu.lehigh.cse.lol.internals.QuickScene;

/**
 * LoseScene provides a way to display text and images after a level is lost,
 * before gameplay resumes. A LoseScene can include arbitrary text and pictures.
 *
 * Every level gets a PostScene automatically, but it can be disabled.
 */
public class LoseScene extends QuickScene {
    /**
     * The default text to display when a level is lost.
     */
    private String mLoseText;

    /**
     * Construct by setting the default lose text
     */
    public LoseScene() {
        mLoseText = Lol.sGame.mDefaultLoseText;
    }

    /**
     * Get the LoseScene that is configured for the current level, or create a
     * blank one if none exists.
     *
     * @return The current LoseScene
     */
    public static LoseScene get() {
        LoseScene scene = Lol.sGame.mCurrentLevel.mLoseScene;
        if (scene != null)
            return scene;
        scene = new LoseScene();
        Lol.sGame.mCurrentLevel.mLoseScene = scene;
        return scene;
    }

    /**
     * When the level is lost, we call this, which will show the LoseScene
     * unless it is disabled
     */
    void show() {
        // if LoseScene is disabled for this level, just restart the level
        if (mDisable) {
            dismiss();
            return;
        }

        // make the LoseScene visible
        mVisible = true;

        // The default text to display can change at the last second, so we
        // don't compute it until right here... also, play music
        if (mSound != null)
            mSound.play(Facts.getGameFact("volume", 1));
        addText(mLoseText, 255, 255, 255, Lol.sGame.mDefaultFontFace, Lol.sGame.mDefaultFontSize);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * This runs when the LoseScene is cleared, and restarts the level
     */
    protected void dismiss() {
        mVisible = false;

        // we turn off music here, so that music plays during the PostScene
        Lol.sGame.mCurrentLevel.stopMusic();

        // remove the previous level
        Lol.sGame.mCurrentLevel = null;

        // repeat the level
        Lol.doLevel(Lol.sGame.mModeStates[Lol.PLAY]);
    }

    /**
     * Set the text that should be drawn, centered, when the level is lost
     *
     * @param text The text to display. Use "" to disable
     */
    public void setDefaultText(String text) {
        mLoseText = text;
    }
}
