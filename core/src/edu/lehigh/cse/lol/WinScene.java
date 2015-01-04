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
 * WinScene provides a way to display text and images after a level is won,
 * before gameplay resumes. A WinScene can include arbitrary text and pictures.
 *
 * Every level gets a WinScene automatically, but it can be disabled.
 */
public class WinScene extends QuickScene {
    /**
     * The default text to display when a level is won.
     */
    private String mWinText;

    /**
     * Construct by setting the default win text
     */
    public WinScene() {
        mWinText = Lol.sGame.mDefaultWinText;
    }

    /**
     * Get the WinScene that is configured for the current level, or create a
     * blank one if none exists.
     *
     * @return The current WinScene
     */
    public static WinScene get() {
        WinScene scene = Lol.sGame.mCurrentLevel.mWinScene;
        if (scene != null)
            return scene;
        scene = new WinScene();
        Lol.sGame.mCurrentLevel.mWinScene = scene;
        return scene;
    }

    /**
     * When the level is won, we call this, which will show the WinScene unless
     * it is disabled
     */
    void show() {
        // if WinScene is disabled for this level, just move to the next level
        if (mDisable) {
            dismiss();
            return;
        }

        // make the PostScene visible
        mVisible = true;

        // The default text to display can change at the last second, so we
        // don't compute it until right here... also, play music
        if (mSound != null)
            mSound.play(Facts.getGameFact("volume", 1));
        get().addText(mWinText, 255, 255, 255, Lol.sGame.mDefaultFontFace, Lol.sGame.mDefaultFontSize);
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * This runs when the WinScene is cleared, and moves gameplay to the
     * appropriate level
     */
    protected void dismiss() {
        mVisible = false;

        // we turn off music here, so that music plays during the PostScene
        Lol.sGame.mCurrentLevel.stopMusic();

        // remove the previous level
        Lol.sGame.mCurrentLevel = null;

        // go to next level (or chooser)
        if (Lol.sGame.mModeStates[Lol.PLAY] == Lol.sGame.mNumLevels) {
            Lol.doChooser(1);
        } else {
            Lol.sGame.mModeStates[Lol.PLAY]++;
            Lol.doLevel(Lol.sGame.mModeStates[Lol.PLAY]);
        }
    }

    /**
     * Set the text that should be drawn, centered, when the level is won
     *
     * @param text The text to display. Use "" to disable
     */
    public void setDefaultWinText(String text) {
        mWinText = text;
    }
}
