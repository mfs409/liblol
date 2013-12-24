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

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class PostScene {
    
    boolean mVisible;

    boolean mDisable;

    String mWinText;

    String mLoseText;

    ArrayList<Renderable> mWinSprites = new ArrayList<Renderable>();

    ArrayList<Renderable> mLoseSprites = new ArrayList<Renderable>();

    private boolean mWin;

    /**
     * Sound to play when the level is won
     */
    Sound mWinSound;

    /**
     * Sound to play when the level is lost
     */
    Sound mLoseSound;

    PostScene() {
        mWinText = LOL.sGame.mConfig.getDefaultWinText();
        mLoseText = LOL.sGame.mConfig.getDefaultLoseText();
    }

    /**
     * Set the sound to play when the level is won
     * 
     * @param soundName Name of the sound file to play
     */
    public static void setWinSound(String soundName) {
        getCurrPostScene().mWinSound = Media.getSound(soundName);
    }

    /**
     * Set the sound to play when the level is lost
     * 
     * @param soundName Name of the sound file to play
     */
    public static void setLoseSound(String soundName) {
        getCurrPostScene().mLoseSound = Media.getSound(soundName);
    }

    /**
     * Get the PostScene that is configured for the current level, or create a
     * blank one if none exists.
     * 
     * @return
     */
    private static PostScene getCurrPostScene() {
        PostScene ps = Level.sCurrent.mPostScene;
        if (ps != null)
            return ps;
        ps = new PostScene();
        Level.sCurrent.mPostScene = ps;
        return ps;
    }

    public static void addExtraWinText(String text, int x, int y, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp.mWinSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    public static void addExtraLoseText(String text, int x, int y, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp.mLoseSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    public static void addExtraWinTextCentered(String text, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp.mWinSprites.add(Util.makeCenteredText(text, red, green, blue, fontName, size));
    }

    public static void addExtraLoseTextCentered(String text, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp.mLoseSprites.add(Util.makeCenteredText(text, red, green, blue, fontName, size));
    }

    public static void addWinImage(String imgName, int x, int y, int width, int height) {
        PostScene tmp = getCurrPostScene();
        tmp.mWinSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    public static void addLoseImage(String imgName, int x, int y, int width, int height) {
        PostScene tmp = getCurrPostScene();
        tmp.mLoseSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    // Use "" to disable
    public static void setDefaultWinText(String text) {
        getCurrPostScene().mWinText = text;
    }

    // Use "" to disable
    public static void setDefaultLoseText(String text) {
        getCurrPostScene().mLoseText = text;
    }

    public static void disable() {
        getCurrPostScene().mDisable = true;
    }

    void setWin(boolean win) {
        mWin = win;

        if (mDisable) {
            finish();
            return;
        }

        mVisible = true;

        // The default text to display can change at the last second, so we
        // don't compute it until right here
        if (win) {
            if (mWinSound != null)
                mWinSound.play();
            mWinSprites.add(Util.makeCenteredText(mWinText, 255, 255, 255,
                    LOL.sGame.mConfig.getDefaultFontFace(), LOL.sGame.mConfig.getDefaultFontSize()));
        } else {
            if (mLoseSound != null)
                mLoseSound.play();
            mLoseSprites.add(Util.makeCenteredText(mLoseText, 255, 255, 255,
                    LOL.sGame.mConfig.getDefaultFontFace(), LOL.sGame.mConfig.getDefaultFontSize()));
        }
    }

    void finish() {
        // we turn off music here, so that music plays during the PostScene
        Level.sCurrent.stopMusic();

        if (!mWin) {
            LOL.sGame.doPlayLevel(LOL.sGame.mCurrLevelNum);
        } else {
            if (LOL.sGame.mCurrLevelNum == LOL.sGame.mConfig.getNumLevels()) {
                LOL.sGame.doChooser();
            } else {
                LOL.sGame.mCurrLevelNum++;
                LOL.sGame.doPlayLevel(LOL.sGame.mCurrLevelNum);
            }
        }
    }

    boolean render(SpriteBatch sb) {
        if (!mVisible)
            return false;
        if (Gdx.input.justTouched()) {
            mVisible = false;
            finish();
            return true;
        }
        ArrayList<Renderable> sprites = (mWin) ? mWinSprites : mLoseSprites;

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClearColor(0, 0, 0, 1); // NB: can change color here...
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Level.sCurrent.mHudCam.update();
        sb.setProjectionMatrix(Level.sCurrent.mHudCam.combined);
        sb.begin();
        for (Renderable r : sprites)
            r.render(sb, 0);
        sb.end();
        return true;
    }
}
