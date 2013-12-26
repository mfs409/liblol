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

/**
 * PostScene provides a way to display text and images after a level is
 * won/lost, before gameplay resumes. A PostScene can include arbitrary text and
 * pictures.
 */
public class PostScene {

    /**
     * Track if the PostScene is visible. Initially it is not.
     */
    private boolean mVisible;

    /**
     * The default is to show a simple PostScene after every level. If we don't
     * want any PostScene, we can disable it via this flag.
     */
    private boolean mDisable;

    /**
     * The default text to display when a level is won.
     */
    private String mWinText;

    /**
     * The default text to display when a level is lost.
     */
    private String mLoseText;

    /**
     * The pictures and text to display when a level is won.
     */
    private ArrayList<Renderable> mWinSprites = new ArrayList<Renderable>();

    /**
     * The pictures and text to display when a level is lost.
     */
    private ArrayList<Renderable> mLoseSprites = new ArrayList<Renderable>();

    /**
     * Track if the level has been won or lost
     */
    private boolean mWin;

    /**
     * Sound to play when the level is won
     */
    private Sound mWinSound;

    /**
     * Sound to play when the level is lost
     */
    private Sound mLoseSound;

    /**
     * Simple constructor: we need to be sure that the default win and lose text
     * are set
     */
    PostScene() {
        mWinText = LOL.sGame.mConfig.getDefaultWinText();
        mLoseText = LOL.sGame.mConfig.getDefaultLoseText();
    }

    /**
     * Get the PostScene that is configured for the current level, or create a
     * blank one if none exists. We use this as a convenience since the LOL
     * paradigm is that the game desginer calls static methods on PostScene to
     * configure an existing object.
     * 
     * @return The current PostScene
     */
    private static PostScene getCurrPostScene() {
        PostScene ps = Level.sCurrent.mPostScene;
        if (ps != null)
            return ps;
        ps = new PostScene();
        Level.sCurrent.mPostScene = ps;
        return ps;
    }

    /**
     * Indicate that the level is over, and has either been won or lost
     * 
     * @param win Use 'true' to indicate that the level was won, 'false'
     *            otherwise
     */
    void setWin(boolean win) {
        mWin = win;

        // if PostScene is disabled for this level, just move to the next level
        if (mDisable) {
            finish();
            return;
        }

        // make the PostScene visible
        mVisible = true;

        // The default text to display can change at the last second, so we
        // don't compute it until right here... also, play music
        if (win) {
            if (mWinSound != null)
                mWinSound.play();
            mWinSprites
                    .add(Util.makeText(mWinText, 255, 255, 255,
                            LOL.sGame.mConfig.getDefaultFontFace(),
                            LOL.sGame.mConfig.getDefaultFontSize()));
        } else {
            if (mLoseSound != null)
                mLoseSound.play();
            mLoseSprites
                    .add(Util.makeText(mLoseText, 255, 255, 255,
                            LOL.sGame.mConfig.getDefaultFontFace(),
                            LOL.sGame.mConfig.getDefaultFontSize()));
        }
    }

    /**
     * This runs when the PostScene is cleared, and moves gameplay to the
     * appropriate level
     */
    private void finish() {
        // we turn off music here, so that music plays during the PostScene
        Level.sCurrent.stopMusic();

        // remove the previous level
        Level.sCurrent = null;
        
        // repeat on loss, else go to next level (or chooser)
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

    /**
     * Draw the current PostScene, but only if it is visible
     * 
     * @param sb The SpriteBatch to use to draw this PostScene
     * @return true if the PostScene was drawn, false otherwise
     */
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

    /*
     * PUBLIC INTERFACE
     */

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
     * Set the text that should be drawn, centered, when the level is won
     * 
     * @param text The text to display. Use "" to disable
     */
    public static void setDefaultWinText(String text) {
        getCurrPostScene().mWinText = text;
    }

    /**
     * Set the text that should be drawn, centered, when the level is lost
     * 
     * @param text The text to display. Use "" to disable
     */
    public static void setDefaultLoseText(String text) {
        getCurrPostScene().mLoseText = text;
    }

    /**
     * Draw text on the PostScene that shows when the level is won
     * 
     * @param text The text to display
     * @param x The X coordinate of the bottom left corner
     * @param y The Y coordinate of the bottom left corner
     * @param red The red component of the color (0-255)
     * @param green The green component of the color (0-255)
     * @param blue The blue component of the color (0-255)
     * @param fontName The name of the font to use
     * @param size The font size to use
     */
    public static void addExtraWinText(String text, int x, int y, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp.mWinSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    /**
     * Draw text on the PostScene that shows when the level is lost
     * 
     * @param text The text to display
     * @param x The X coordinate of the bottom left corner
     * @param y The Y coordinate of the bottom left corner
     * @param red The red component of the color (0-255)
     * @param green The green component of the color (0-255)
     * @param blue The blue component of the color (0-255)
     * @param fontName The name of the font to use
     * @param size The font size to use
     */
    public static void addExtraLoseText(String text, int x, int y, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp.mLoseSprites.add(Util.makeText(x, y, text, red, green, blue, fontName, size));
    }

    /**
     * Draw text on the PostScene that shows when the level is won, and center
     * the text
     * 
     * @param text The text to display
     * @param red The red component of the color (0-255)
     * @param green The green component of the color (0-255)
     * @param blue The blue component of the color (0-255)
     * @param fontName The name of the font to use
     * @param size The font size to use
     */
    public static void addExtraWinText(String text, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp.mWinSprites.add(Util.makeText(text, red, green, blue, fontName, size));
    }

    /**
     * Draw text on the PostScene that shows when the level is lost, and center
     * the text
     * 
     * @param text The text to display
     * @param red The red component of the color (0-255)
     * @param green The green component of the color (0-255)
     * @param blue The blue component of the color (0-255)
     * @param fontName The name of the font to use
     * @param size The font size to use
     */
    public static void addExtraLoseText(String text, int red, int green, int blue,
            String fontName, int size) {
        PostScene tmp = getCurrPostScene();
        tmp.mLoseSprites.add(Util.makeText(text, red, green, blue, fontName, size));
    }

    /**
     * Draw a picture on the PostScene that shows when the level is won
     * 
     * @param imgName The name of the image file that should be displayed
     * @param x The X coordinate of the bottom left corner
     * @param y The Y coordinate of the bottom left corner
     * @param width The width of the image
     * @param height The height of the image
     */
    public static void addWinImage(String imgName, int x, int y, int width, int height) {
        PostScene tmp = getCurrPostScene();
        tmp.mWinSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * Draw a picture on the PostScene that shows when the level is lost
     * 
     * @param imgName The name of the image file that should be displayed
     * @param x The X coordinate of the bottom left corner
     * @param y The Y coordinate of the bottom left corner
     * @param width The width of the image
     * @param height The height of the image
     */
    public static void addLoseImage(String imgName, int x, int y, int width, int height) {
        PostScene tmp = getCurrPostScene();
        tmp.mLoseSprites.add(Util.makePicture(x, y, width, height, imgName));
    }

    /**
     * Indicate that this level's PostScene should not be displayed
     */
    public static void disable() {
        getCurrPostScene().mDisable = true;
    }
}
