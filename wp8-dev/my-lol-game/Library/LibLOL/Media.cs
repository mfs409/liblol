using System;
using System.Collections.Generic;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Audio;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Media;

namespace LibLOL
{
    public static class Media
    {

        private static Dictionary<string, Font> sFonts = new Dictionary<string, Font>();

        private static Dictionary<string, SoundEffect> sSounds = new Dictionary<string, SoundEffect>();

        private static Dictionary<string, Music> sTunes = new Dictionary<string, Music>();

        private static Dictionary<string, Texture2D[]> sImages = new Dictionary<string, Texture2D[]>();

        internal static void OnDispose()
        {
            sFonts.Clear();
        }

        internal static Font GetFont(string fontFileName, int fontSize)
        {
            Font f;
            if (sFonts.TryGetValue(fontFileName, out f))
            {
                return f;
            }
            f = new Font(Lol.sGame.Content.Load<SpriteFont>(fontFileName), Color.White);
            sFonts.Add(fontFileName, f);
            return f;
        }

        internal static SoundEffect GetSound(string soundName)
        {
            SoundEffect se;
            if (!sSounds.TryGetValue(soundName, out se))
            {
                //Util.log("ERROR", "Error retreiving sound '" + soundName + "'");
            }
            return se;
        }

        internal static Music GetMusic(string musicName)
        {
            Music m;
            if (!sTunes.TryGetValue(musicName, out m))
            {
                //Util.log("ERROR", "Error retreiving music '" + musicName + "'");
            }
            return m;
        }

        internal static Texture2D[] GetImage(string imgName)
        {
            Texture2D[] ret;
            if (!sImages.TryGetValue(imgName, out ret))
            {
                //Util.log("ERROR", "Error retreiving image '" + imgName + "'");
            }
            return ret;
        }

        public static void RegisterImage(string imgName)
        {
            Texture2D[] tr = new Texture2D[1];
            tr[0] = Lol.sGame.Content.Load<Texture2D>(imgName);
            sImages.Add(imgName, tr);
        }

        public static void RegisterAnimatableImage(string imgName, int columns, int rows)
        {
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
            sImages.Add(imgName, tiles);
        }

        public static void RegisterMusic(string musicName, bool loop)
        {
            Song s = Lol.sGame.Content.Load<Song>(musicName);
            Music m = new Music(s, loop);
            sTunes.Add(musicName, m);
        }

        public static void RegisterSound(string soundName)
        {
            sSounds.Add(soundName, Lol.sGame.Content.Load<SoundEffect>(soundName));
        }
    }
}
