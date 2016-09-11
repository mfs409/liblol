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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.util.TreeMap;

/**
 * The MediaFactory provides a mechanism for registering all of our images,
 * sounds, and fonts Strictly speaking, we can re-create fonts on the fly
 * whenever we need to. Caching them here is an optimization.
 */
public class Media {
    /**
     * Store the fonts used by this game
     */
    private final TreeMap<String, BitmapFont> mFonts = new TreeMap<>();

    /**
     * Store the sounds used by this game
     */
    private final TreeMap<String, Sound> mSounds = new TreeMap<>();

    /**
     * Store the music used by this game
     */
    private final TreeMap<String, Music> mTunes = new TreeMap<>();

    /**
     * Store the images used by this game
     */
    private final TreeMap<String, TextureRegion[]> mImages = new TreeMap<>();

    /**
     * When a game is disposed of, the images are managed by libGDX. Fonts are
     * too, except that references to old fonts don't resurrect nicely. Clearing
     * the collection when the game disposes is satisfactory to avoid visual
     * glitches when the game comes back to the foreground.
     */
    static void onDispose() {
        Lol.sGame.mMedia.mFonts.clear();
    }

    /**
     * Get the font described by the file name and font size
     *
     * @param fontFileName The filename for the font. This should be in the android
     *                     project's assets, and should end in .ttf
     * @param fontSize     The size to display
     * @return A font object that can be used to render text
     */
    static BitmapFont getFont(String fontFileName, int fontSize) {
        // we store fonts as their filename appended with their size
        String key = fontFileName + "--" + fontSize;

        // check if we've already got this font, return it if we do
        BitmapFont f = Lol.sGame.mMedia.mFonts.get(key);
        if (f != null) {
            // just to play it safe, make the font white... the caller can
            // change this
            f.setColor(1, 1, 1, 1);
            return f;
        }

        // Generate the font, save it, and return it
        //
        // NB: if this crashes, the user will get a reasonably good error
        // message
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontFileName));
        parameter.size = fontSize;
        generator.scaleForPixelHeight(fontSize);
        parameter.minFilter = Texture.TextureFilter.Linear;
        parameter.magFilter = Texture.TextureFilter.Linear;

        f = generator.generateFont(parameter);
        generator.dispose();
        Lol.sGame.mMedia.mFonts.put(key, f);
        return f;
    }

    /**
     * Internal method to retrieve a sound by name
     *
     * @param soundName Name of the sound file to retrieve
     * @return a Sound object that can be used for sound effects
     */
    public static Sound getSound(String soundName) {
        Sound ret = Lol.sGame.mMedia.mSounds.get(soundName);
        if (ret == null)
            Util.message("ERROR", "Error retreiving sound '" + soundName + "'");
        return ret;
    }

    /**
     * Internal method to retrieve a music object by name
     *
     * @param musicName Name of the music file to retrieve
     * @return a Music object that can be used to play background music
     */
    static Music getMusic(String musicName) {
        Music ret = Lol.sGame.mMedia.mTunes.get(musicName);
        if (ret == null)
            Util.message("ERROR", "Error retreiving music '" + musicName + "'");
        return ret;
    }

    /**
     * Internal method to retrieve an image by name. User code should not call
     * this.
     *
     * @param imgName Name of the image file to retrieve
     * @return a TextureRegion object that can be used to create Actors
     */
    public static TextureRegion[] getImage(String imgName) {
        TextureRegion[] ret = Lol.sGame.mMedia.mImages.get(imgName);
        if (ret == null)
            Util.message("ERROR", "Error retreiving image '" + imgName + "'");
        return ret;
    }

    /**
     * On a volume change event, this will change the volume of all music
     * objects
     */
    static void resetMusicVolume() {
        for (Music m : Lol.sGame.mMedia.mTunes.values()) {
            m.setVolume(Facts.getGameFact("volume", 1));
        }
    }

    /*
     * PUBLIC INTERFACE
     */

    /**
     * Register an image file, so that it can be used later. Images should be
     * .png files. Note that images with internal animations (i.e., gifs) do not
     * work correctly. You should use cell-based animation instead.
     *
     * @param imgName the name of the image file (assumed to be in the "assets"
     *                folder). This should be of the form "image.png", and should be
     *                of type "png". "jpeg" images work too, but usually look bad in
     *                games
     */
    static public void registerImage(String imgName) {
        // Create an array with one entry
        TextureRegion[] tr = new TextureRegion[1];
        tr[0] = new TextureRegion(new Texture(Gdx.files.internal(imgName)));
        Lol.sGame.mMedia.mImages.put(imgName, tr);
    }

    /**
     * Register an animatable image file, so that it can be used later. The
     * difference between regular images and animatable images is that
     * animatable images should be thought of as having multiple columns and
     * rows, which allow cell-based animation. Images should be .png files. Note
     * that images with internal animations (i.e., gifs) do not work correctly.
     * You should use cell-based animation instead.
     *
     * @param imgName the name of the image file (assumed to be in the "assets"
     *                folder). This should be of the form "image.png", and should be
     *                of type "png"
     * @param columns The number of columns that comprise this image file
     * @param rows    The number of rows that comprise this image file
     */
    static public void registerAnimatableImage(String imgName, int columns, int rows) {
        // Load the file as a texture
        Texture t = new Texture(Gdx.files.internal(imgName));
        // carve the image into cells of equal width and height
        int width = t.getWidth() / columns;
        int height = t.getHeight() / rows;
        TextureRegion[][] trgrid = TextureRegion.split(t, width, height);
        // put all entries into a 1-D array
        TextureRegion[] trs = new TextureRegion[columns * rows];
        int index = 0;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                trs[index] = trgrid[i][j];
                index++;
            }
        }
        Lol.sGame.mMedia.mImages.put(imgName, trs);
    }

    /**
     * Register a music file, so that it can be used later. Music should be in
     * .ogg format. You can use Audacity to convert music as needed. mp3 files
     * should work too, but may have licensing restrictions in some countries.
     *
     * @param musicName the name of the music file (assumed to be in the "assets"
     *                  folder). This should be of the form "song.ogg", and should be
     *                  of type "ogg".
     * @param loop      either true or false, to indicate whether the song should
     *                  repeat when it reaches the end
     */
    static public void registerMusic(String musicName, boolean loop) {
        Music m = Gdx.audio.newMusic(Gdx.files.internal(musicName));
        m.setLooping(loop);
        m.setVolume(Facts.getGameFact("volume", 1));
        Lol.sGame.mMedia.mTunes.put(musicName, m);
    }

    /**
     * Register a sound file, so that it can be used later. Sounds should be
     * .ogg files. You can use Audacity to convert sounds as needed. mp3 files
     * should work too
     *
     * @param soundName the name of the sound file (assumed to be in the "assets"
     *                  folder). This should be of the form "sound.ogg", and should be
     *                  of type "ogg".
     */
    static public void registerSound(String soundName) {
        Sound s = Gdx.audio.newSound(Gdx.files.internal(soundName));
        Lol.sGame.mMedia.mSounds.put(soundName, s);
    }
}
