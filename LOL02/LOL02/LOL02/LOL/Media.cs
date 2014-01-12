using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Media;

namespace LOL
{
    public class Media
    {
        public static Font getFont(String name, int size)
        {
            // TODO: Incorporate size into font-name for SpriteFont assets
            return new Font(Lol.Content.Load<SpriteFont>(name), Color.White);
        }

        /**
         * Store the fonts used by this game
         */
        static private Dictionary<String, Font> sFonts = new Dictionary<String, Font>();

        /**
         * Store the sounds used by this game
         */
        static private Dictionary<String, SoundEffect> sSounds = new Dictionary<String, SoundEffect>();

        /**
         * Store the music used by this game
         */
        static private Dictionary<String, Music> sTunes = new Dictionary<String, Music>();

        /**
         * Store the images used by this game
         */
        static private Dictionary<String, Texture2D[]> sImages = new Dictionary<String, Texture2D[]>();

        /**
         * When a game is disposed of, the images are managed by libGDX. Fonts are
         * too, except that references to old fonts don't resurrect nicely. Clearing
         * the collection when the game dispose()s is satisfactory to avoid visual
         * glitches when the game comes back to the foreground.
         */
        public static void onDispose() {
            sFonts.Clear();
        }

        /**
         * Internal method to retrieve a sound by name
         * 
         * @param soundName Name of the sound file to retrieve
         * @return a Sound object that can be used for sound effects
         */
        public static SoundEffect getSound(String soundName) {
            if (!sSounds.ContainsKey(soundName))
            {
                Util.log("ERROR", "Error retreiving sound '" + soundName + "'");
            }
            return sSounds[soundName];
        }

        /**
         * Internal method to retrieve a music object by name
         * 
         * @param musicName Name of the music file to retrieve
         * @return a Music object that can be used to play background music
         */
        public static Music getMusic(String musicName) {
            if (!sTunes.ContainsKey(musicName))
            {
                Util.log("ERROR", "Error retreiving music '" + musicName + "'");
            }
            return sTunes[musicName];
        }

        /**
         * Internal method to retrieve an image by name
         * 
         * @param imgName Name of the image file to retrieve
         * @return a TiledTextureRegion object that can be used to create
         *         AnimatedSprites
         */
        public static Texture2D[] getImage(String imgName) {
            Texture2D[] ret = sImages[imgName];
            if (ret == null)
                Util.log("ERROR", "Error retreiving image '" + imgName + "'");
            return ret;
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
         *            folder). This should be of the form "image.png", and should be
         *            of type "png". "jpeg" images work too, but usually look bad in
         *            games
         */
        static public void registerImage(String imgName) {
            // Create an array with one entry
            Texture2D[] tr = new Texture2D[1];
            tr[0] = Lol.Content.Load<Texture2D>(imgName);
            sImages[imgName] = tr;
        }

        /**
         * Register an animatable image file, so that it can be used later. The
         * difference between regular images and animatable images is that
         * animatable images have multiple columns and rows, which allows cell-based
         * animation. Images should be .png files. Note that images with internal
         * animations (i.e., gifs) do not work correctly. You should use cell-based
         * animation instead.
         * 
         * @param imgName the name of the image file (assumed to be in the "assets"
         *            folder). This should be of the form "image.png", and should be
         *            of type "png"
         * @param columns The number of columns that comprise this image file
         * @param rows The number of rows that comprise this image file
         */
        static public void registerAnimatableImage(String imgName, int columns, int rows) {
            // Load the file as a texture
            Texture2D t = Lol.Content.Load<Texture2D>(imgName);
            // carve the image into cells of equal width and height
            int width = t.Width / columns;
            int height = t.Height / rows;

            // TODO: Figure out how to split textures
            Texture2D[][] trgrid = null; // TextureRegion.split(t, width, height);
            // put all entries into a 1-D array
            Texture2D[] trs = new Texture2D[columns * rows];
            int index = 0;
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < columns; ++j) {
                    trs[index] = trgrid[i][j];
                    index++;
                }
            }
            sImages[imgName]  = trs;
        }

        /**
         * Register a music file, so that it can be used later. Music should be in
         * .ogg files. You can use Audacity to convert music as needed. mp3 files
         * should work too.
         * 
         * @param musicName the name of the music file (assumed to be in the
         *            "assets" folder). This should be of the form "song.ogg", and
         *            should be of type "ogg".
         * @param loop either true or false, to indicate whether the song should
         *            repeat when it reaches the end
         */
        static public void registerMusic(String musicName, bool loop) {
            //Music m = Gdx.audio.newMusic(Gdx.files.internal(musicName));
            //m.setLooping(loop);
            Song s = Lol.Content.Load<Song>(musicName);
            Music m = new Music(s, loop);
            sTunes[musicName] = m;
        }

        /**
         * Register a sound file, so that it can be used later. Sounds should be
         * .ogg files. You can use Audacity to convert sounds as needed. mp3 files
         * should work too
         * 
         * @param soundName the name of the sound file (assumed to be in the
         *            "assets" folder). This should be of the form "sound.ogg", and
         *            should be of type "ogg".
         */
        static public void registerSound(String soundName) {
            //Sound s = Gdx.audio.newSound(Gdx.files.internal(soundName));
            sSounds[soundName] = Lol.Content.Load<SoundEffect>(soundName);
        }

    }
}
