package edu.lehigh.cse.lol;

// TODO: clean up comments

// TODO: should we call them "sprite sheets"?

import java.util.Hashtable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * The MediaFactory provides a mechanism for registering all of our images and
 * sounds
 */
public class Media
{
    /**
     * Making fonts can get expensive... to lighten the load, we'll cache any
     * fonts we make
     */
    static final Hashtable<String, BitmapFont>              _fonts  = new Hashtable<String, BitmapFont>();

    /**
     * Store the sounds used by this game
     */
    static private final Hashtable<String, Sound>           _sounds = new Hashtable<String, Sound>();

    /**
     * Store the music used by this game
     */
    static private final Hashtable<String, Music>           _tunes  = new Hashtable<String, Music>();

    /**
     * Store the images used by this game
     */
    static private final Hashtable<String, TextureRegion[]> _images = new Hashtable<String, TextureRegion[]>();

    public static BitmapFont getFont(String fontFileName, int fontSize)
    {
        // we store pre-made fonts as their filename appended with their size
        String key = fontFileName + "--" + fontSize;

        // check if we've already got this font
        BitmapFont f = _fonts.get(key);
        if (f != null) {
            // reset to white
            //
            // TODO: we're going to have trouble with multiple colors for the same screen at some point... what to do?
            f.setColor(1, 1, 1, 1);
            return f;
        }

        // NB: cleaner way of doing fonts
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontFileName));
        f = generator.generateFont(fontSize, FreeTypeFontGenerator.DEFAULT_CHARS, false);
        generator.dispose();
        _fonts.put(key, f);
        return f;
    }

    /**
     * Internal method to retrieve a sound by name
     * 
     * @param soundName
     *            Name of the sound file to retrieve
     * 
     * @return a Sound object that can be used for sound effects
     */
    static Sound getSound(String soundName)
    {
        Sound ret = _sounds.get(soundName);
        if (ret == null)
            Gdx.app.log("ERROR", "Error retreiving sound " + soundName + " ... your program is probably about to crash");
        return ret;
    }

    /**
     * Internal method to retrieve a _music object by name
     * 
     * @param musicName
     *            Name of the _music file to retrieve
     * 
     * @return a Music object that can be used to play background _music
     */
    static Music getMusic(String musicName)
    {
        Music ret = _tunes.get(musicName);
        if (ret == null)
            Gdx.app.log("ERROR", "Error retreiving music " + musicName + " ... your program is probably about to crash");
        return ret;
    }

    /**
     * Internal method to retrieve an image by name
     * 
     * @param imgName
     *            Name of the image file to retrieve
     * 
     * @return a TiledTextureRegion object that can be used to create
     *         AnimatedSprites
     */
    static TextureRegion[] getImage(String imgName)
    {
        TextureRegion[] ret = _images.get(imgName);
        if (ret == null)
            Gdx.app.log("ERROR", "Error retreiving image " + imgName + " ... your program is probably about to crash");
        return ret;
    }

    /**
     * Register an image file, so that it can be used later.
     * 
     * Images should be .png files. Note that _images with internal animations
     * do not work correctly. You should use cell-based animation instead.
     * 
     * @param imgName
     *            the name of the image file (assumed to be in the "assets"
     *            folder). This should be of the form "image.png", and should be
     *            of type "png".
     */
    static public void registerImage(String imgName)
    {
        TextureRegion[] tr = new TextureRegion[1];
        tr[0] = new TextureRegion(new Texture(Gdx.files.internal(imgName)));
        _images.put(imgName, tr);
    }

    /**
     * Register an animatable image file, so that it can be used later. The
     * difference between regular _images and animatable _images is that
     * animatable _images have multiple columns, for cell-based animation.
     * 
     * Images should be .png files. Note that _images with internal animations
     * do not work correctly. You should use cell-based animation instead.
     * 
     * @param imgName
     *            the name of the image file (assumed to be in the "assets"
     *            folder). This should be of the form "image.png", and should be
     *            of type "png".
     * @param cellColumns
     *            If this image is for animation, and represents a grid of
     *            cells, then cellColumns should be the number of columns in the
     *            grid. Otherwise, it should be 1.
     */
    static public void registerAnimatableImage(String imgName, int columns, int rows)
    {
        Texture t = new Texture(Gdx.files.internal(imgName));
        int width = t.getWidth() / columns;
        int height = t.getHeight() / rows;
        TextureRegion[][] trgrid = TextureRegion.split(t, width, height);
        TextureRegion[] trs = new TextureRegion[columns * rows];
        int index = 0;
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < columns; ++j) {
                trs[index] = trgrid[i][j];
                index++;
            }
        }
        _images.put(imgName, trs);
    }

    /**
     * Register a _music file, so that it can be used later.
     * 
     * Music should be in .ogg files. You can use Audacity to convert _music as
     * needed.
     * 
     * @param musicName
     *            the name of the _music file (assumed to be in the "assets"
     *            folder). This should be of the form "song.ogg", and should be
     *            of type "ogg".
     * @param loop
     *            either true or false, to indicate whether the song should
     *            repeat when it reaches the end
     */
    static public void registerMusic(String musicName, boolean loop)
    {
        Music m = Gdx.audio.newMusic(Gdx.files.internal(musicName));
        m.setLooping(loop);
        _tunes.put(musicName, m);
    }

    /**
     * Register a sound file, so that it can be used later.
     * 
     * Sounds should be .ogg files. You can use Audacity to convert _sounds as
     * needed.
     * 
     * @param soundName
     *            the name of the sound file (assumed to be in the "assets"
     *            folder). This should be of the form "sound.ogg", and should be
     *            of type "ogg".
     */
    static public void registerSound(String soundName)
    {
        Sound s = Gdx.audio.newSound(Gdx.files.internal(soundName));
        _sounds.put(soundName, s);
    }
}