package edu.lehigh.cse.ale;

import java.io.IOException;
import java.util.Hashtable;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeType.Bitmap;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.esotericsoftware.tablelayout.BaseTableLayout.Debug;

/**
 * The MediaFactory provides a mechanism for registering all of our _images and
 * _sounds
 */
public class Media {
	/**
	 * Making fonts can get expensive... to lighten the load, we'll cache any
	 * fonts we make
	 */
	static final Hashtable<String, BitmapFont> _fonts = new Hashtable<String, BitmapFont>();

	/**
	 * Store the sounds used by this game
	 */
	static private final Hashtable<String, Sound> _sounds = new Hashtable<String, Sound>();

	/**
	 * Store the music used by this game
	 */
	static private final Hashtable<String, Music> _tunes = new Hashtable<String, Music>();

	/**
	 * Store the images used by this game
	 */
	static private final Hashtable<String, TextureRegion> _images = new Hashtable<String, TextureRegion>();

	public static BitmapFont getFont(String fontFileName, int fontSize) 
	{
		// we store pre-made fonts as their filename appended with their size
		String key = fontFileName + "--" + fontSize;
		
		// check if we've already got this font
		BitmapFont f = _fonts.get(key);
		if (f != null)
			return f;
		
		// NB: cleaner way of doing fonts
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontFileName));
		f = generator.generateFont(fontSize, FreeTypeFontGenerator.DEFAULT_CHARS, false);
		generator.dispose();
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
	static Sound getSound(String soundName) {
		Sound ret = _sounds.get(soundName);
		if (ret == null)
			Gdx.app.log("ERROR", "Error retreiving sound " + soundName
					+ " ... your program is probably about to crash");
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
	static Music getMusic(String musicName) {
		Music ret = _tunes.get(musicName);
		if (ret == null)
			Gdx.app.log("ERROR", "Error retreiving music " + musicName
					+ " ... your program is probably about to crash");
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
	static TextureRegion getImage(String imgName) {
		TextureRegion ret = _images.get(imgName);
		if (ret == null)
			Gdx.app.log("ERROR", "Error retreiving image " + imgName
					+ " ... your program is probably about to crash");
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
	/*
	static public void registerImage(String imgName) {
		AssetManager am = ALE._self.getAssets();
		Bitmap b = null;
		try {
			b = BitmapFactory.decodeStream(am.open(imgName));
		} catch (Exception e) {
			Debug.d("Error loading image file "
					+ imgName
					+ " ... your program will probably crash when you try to use it.  Is the file in your assets?");
			return;
		}
		int width = b.getWidth();
		int height = b.getHeight();

		if (width > 2048)
			Debug.d("Image file " + imgName + " has a width of " + width
					+ "... that's probably too big!");
		if (height > 2048)
			Debug.d("Image file " + imgName + " has a height of " + height
					+ "... that's probably too big!");

		BitmapTextureAtlas bta = new BitmapTextureAtlas(
				ALE._self.getTextureManager(), width, height,
				TextureOptions.DEFAULT);
		TiledTextureRegion ttr = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(bta, ALE._self, imgName, 0, 0, 1, 1);
		_images.put(imgName, ttr);
		ALE._self.getEngine().getTextureManager().loadTexture(bta);
	}
*/
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
	/*
	static public void registerAnimatableImage(String imgName, int cellColumns) {
		AssetManager am = ALE._self.getAssets();
		Bitmap b = null;
		try {
			b = BitmapFactory.decodeStream(am.open(imgName));
		} catch (Exception e) {
			Debug.d("Error loading image file "
					+ imgName
					+ " ... your program will probably crash when you try to use it.  Is the file in your assets?");
			return;
		}
		int width = b.getWidth();
		int height = b.getHeight();

		if (width > 2048)
			Debug.d("Image file " + imgName + " has a width of " + width
					+ "... that's probably too big!");
		if (height > 2048)
			Debug.d("Image file " + imgName + " has a height of " + height
					+ "... that's probably too big!");

		BitmapTextureAtlas bta = new BitmapTextureAtlas(
				ALE._self.getTextureManager(), width, height,
				TextureOptions.DEFAULT);
		TiledTextureRegion ttr = BitmapTextureAtlasTextureRegionFactory
				.createTiledFromAsset(bta, ALE._self, imgName, 0, 0,
						cellColumns, 1);
		_images.put(imgName, ttr);
		ALE._self.getEngine().getTextureManager().loadTexture(bta);
	}
*/
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
	/*
	static public void registerMusic(String musicName, boolean loop) {
		try {
			Music m = MusicFactory.createMusicFromAsset(ALE._self.getEngine()
					.getMusicManager(), ALE._self, musicName);
			m.setLooping(loop);
			_tunes.put(musicName, m);
		} catch (final IOException e) {
			Gdx.app.log(
					"ERROR",
					"Error encountered while trying to load audio file "
							+ musicName
							+ ".  Common causes include a misspelled file name, an incorrect path, "
							+ "or an invalid file type.");
		}
	}
	*/

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
	/*
	static public void registerSound(String soundName) {
		try {
			Sound s = SoundFactory.createSoundFromAsset(ALE._self.getEngine()
					.getSoundManager(), ALE._self, soundName);
			_sounds.put(soundName, s);
		} catch (IOException e) {
			Gdx.app.log(
					"ERROR",
					"Error encountered while trying to load audio file "
							+ soundName
							+ ".  Common causes include a misspelled file name, an incorrect path, "
							+ "or an invalid file type.");
		}
	}
	*/
}