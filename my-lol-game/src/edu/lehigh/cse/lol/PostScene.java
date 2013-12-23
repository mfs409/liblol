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
    
    boolean _visible;

    boolean _disable;

    String _winText;

    String _loseText;

    ArrayList<Renderable> _winSprites = new ArrayList<Renderable>();

    ArrayList<Renderable> _loseSprites = new ArrayList<Renderable>();

    private boolean _win;

    /**
     * Sound to play when the level is won
     */
    Sound _winSound;

    /**
     * Sound to play when the level is lost
     */
    Sound _loseSound;

    PostScene() {
        _winText = LOL._game._config.getDefaultWinText();
        _loseText = LOL._game._config.getDefaultLoseText();
    }

    /**
     * Set the sound to play when the level is won
     * 
     * @param soundName Name of the sound file to play
     */
    public static void setWinSound(String soundName) {
        getCurrPostScene()._winSound = Media.getSound(soundName);
    }

    /**
     * Set the sound to play when the level is lost
     * 
     * @param soundName Name of the sound file to play
     */
    public static void setLoseSound(String soundName) {
        getCurrPostScene()._loseSound = Media.getSound(soundName);
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
        tmp._winSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    public static void addExtraLoseText(String text, int x, int y, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp._loseSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    public static void addExtraWinTextCentered(String text, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp._winSprites.add(Util.makeCenteredText(text, red, green, blue, fontName, size));
    }

    public static void addExtraLoseTextCentered(String text, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp._loseSprites.add(Util.makeCenteredText(text, red, green, blue, fontName, size));
    }

    public static void addWinImage(String imgName, int x, int y, int width, int height) {
        PostScene tmp = getCurrPostScene();
        tmp._winSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    public static void addLoseImage(String imgName, int x, int y, int width, int height) {
        PostScene tmp = getCurrPostScene();
        tmp._loseSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    // Use "" to disable
    public static void setDefaultWinText(String text) {
        getCurrPostScene()._winText = text;
    }

    // Use "" to disable
    public static void setDefaultLoseText(String text) {
        getCurrPostScene()._loseText = text;
    }

    public static void disable() {
        getCurrPostScene()._disable = true;
    }

    void setWin(boolean win) {
        _win = win;

        if (_disable) {
            finish();
            return;
        }

        _visible = true;

        // The default text to display can change at the last second, so we
        // don't compute it until right here
        if (win) {
            if (_winSound != null)
                _winSound.play();
            _winSprites.add(Util.makeCenteredText(_winText, 255, 255, 255,
                    LOL._game._config.getDefaultFontFace(), LOL._game._config.getDefaultFontSize()));
        } else {
            if (_loseSound != null)
                _loseSound.play();
            _loseSprites.add(Util.makeCenteredText(_loseText, 255, 255, 255,
                    LOL._game._config.getDefaultFontFace(), LOL._game._config.getDefaultFontSize()));
        }
    }

    void finish() {
        // we turn off music here, so that music plays during the PostScene
        Level.sCurrent.stopMusic();

        if (!_win) {
            LOL._game.doPlayLevel(LOL._game._currLevel);
        } else {
            if (LOL._game._currLevel == LOL._game._config.getNumLevels()) {
                LOL._game.doChooser();
            } else {
                LOL._game._currLevel++;
                LOL._game.doPlayLevel(LOL._game._currLevel);
            }
        }
    }

    boolean render(SpriteBatch sb) {
        if (!_visible)
            return false;
        if (Gdx.input.justTouched()) {
            _visible = false;
            finish();
            return true;
        }
        ArrayList<Renderable> _sprites = (_win) ? _winSprites : _loseSprites;

        // next we clear the color buffer and set the camera matrices
        Gdx.gl.glClearColor(0, 0, 0, 1); // NB: can change color here...
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Level.sCurrent.mHudCam.update();
        sb.setProjectionMatrix(Level.sCurrent.mHudCam.combined);
        sb.begin();
        for (Renderable r : _sprites)
            r.render(sb, 0);
        sb.end();
        return true;
    }
}
