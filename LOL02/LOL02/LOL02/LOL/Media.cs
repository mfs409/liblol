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
    /**
     * The MediaFactory provides a mechanism for registering all of our images,
     * sounds, and fonts Strictly speaking, we can re-create fonts on the fly
     * whenever we need to. Caching them here is an optimization.
     */
    public class Media
    {
        /**
         * Get the font described by the file name and font size
         * 
         * @param fontFileName The filename for the font. This should be in the
         *            android project's assets, and should end in .ttf
         * @param fontSize The size to display
         * @return A font object that can be used to render text
         */
        public static Font getFont(String name, int size)
        {
            // TODO: Incorporate size into font-name for SpriteFont assets
            return new Font(Lol.sGame.Content.Load<SpriteFont>(name), Color.White);
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
            Texture2D[] ret;
            if (!sImages.TryGetValue(imgName, out ret))
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
            tr[0] = Lol.sGame.Content.Load<Texture2D>(imgName);
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
            Texture2D original = Lol.sGame.Content.Load<Texture2D>(imgName);

            int widthPerPart = original.Width / columns;
            int heightPerPart = original.Height / rows;
            int dataPerPart = widthPerPart * heightPerPart;

            Texture2D[] tiles = new Texture2D[widthPerPart * heightPerPart];

            Color[] originalData = new Color[original.Width * original.Height];
            original.GetData<Color>(originalData);

            int index = 0;
            for (int y = 0; y < heightPerPart * rows; y += heightPerPart)
            {
                for (int x = 0; x < widthPerPart * columns; x += widthPerPart)
                {
                    Texture2D part = new Texture2D(Lol.sGame.GraphicsDevice, widthPerPart, heightPerPart);
                    Color[] partData = new Color[dataPerPart];
                    for (int py = 0; py < heightPerPart; ++py)
                    {
                        for (int px = 0; px < widthPerPart; ++px)
                        {
                            int partIndex = px + py * widthPerPart;
                            partData[partIndex] = originalData[(x + px) + (y + py) * original.Width];
                        }
                    }
                    part.SetData<Color>(partData);
                    tiles[index++] = part;
                }
            }
            original.Dispose();
            sImages.Add(imgName, tiles);
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
            Song s = Lol.sGame.Content.Load<Song>(musicName);
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
            sSounds[soundName] = Lol.sGame.Content.Load<SoundEffect>(soundName);
        }

    }
}
