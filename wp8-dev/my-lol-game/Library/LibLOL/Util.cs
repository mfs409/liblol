using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;
using Microsoft.Xna.Framework.Input.Touch;

namespace LibLOL
{
    public static class Util
    {
        private static Random sGenerator = new Random();

        internal static Renderable MakePicture(int x, int y, int width,
            int height, string imgName)
        {
            Texture2D[] trs = Media.GetImage(imgName);
            Texture2D tr = trs != null ? trs[0] : null;
            AnonRender.DrawDelegate draw = (sb, gt) => { sb.Draw(tr, new Rectangle(x, y, width, height), Color.White); };
            AnonRender r = new AnonRender(null, draw);
            return r;
        }

        internal static Renderable MakeText(int x, int y, string message, int red, int green,
            int blue, string fontName, int size)
        {
            Font br = Media.GetFont(fontName, size);
            br.Color = new Color(red, green, blue, 1);
            AnonRender.DrawDelegate draw = (sb, gt) => { sb.DrawString(br.Face, message, new Vector2(x, y), br.Color); };
            AnonRender r = new AnonRender(null, draw);
            return r;
        }

        internal static Renderable MakeText(String message, int red, int green,
                int blue, String fontName, int size)
        {
            Font bf = Media.GetFont(fontName, size);
            float x = Lol.sGame.mConfig.GetScreenWidth() / 2
                    - bf.Face.MeasureString(message).X / 2;
            float y = Lol.sGame.mConfig.GetScreenHeight() / 2
                    + bf.Face.MeasureString(message).Y / 2;
            bf.Color = new Color(red, green, blue, 1);
            AnonRender.DrawDelegate draw = (sb, gt) => { sb.DrawString(bf.Face, message, new Vector2(x, y), bf.Color); };
            AnonRender r = new AnonRender(null, draw);
            return r;
        }

        public static bool JustTouched(out Vector2 loc)
        {
            TouchCollection tc = TouchPanel.GetState();
            if (tc.Count > 0)
            {
                bool touched = tc[0].State == TouchLocationState.Released;
                if (touched)
                {
                    loc = tc[0].Position;
                    return true;
                }
            }
            loc = Vector2.Zero;
            return false;
        }

        public static int GetRandom(int max)
        {
            return sGenerator.Next(max);
        }

        static public void DrawBoundingBox(int x0, int y0, int x1, int y1, String imgName,
                float density, float elasticity, float friction)
        {
            Obstacle bottom = Obstacle.MakeAsBox(x0 - 1, y0 - 1, Math.Abs(x0 - x1) + 2, 1, imgName);
            bottom.SetPhysics(density, elasticity, friction);

            Obstacle top = Obstacle.MakeAsBox(x0 - 1, y1, Math.Abs(x0 - x1) + 2, 1, imgName);
            top.SetPhysics(density, elasticity, friction);

            Obstacle left = Obstacle.MakeAsBox(x0 - 1, y0 - 1, 1, Math.Abs(y0 - y1) + 2, imgName);
            left.SetPhysics(density, elasticity, friction);

            Obstacle right = Obstacle.MakeAsBox(x1, y0 - 1, 1, Math.Abs(y0 - y1) + 2, imgName);
            right.SetPhysics(density, elasticity, friction);
        }

        public static void DrawPicture(int x, int y, int width, int height,
                String imgName, int zIndex)
        {
            //Level.sCurrent.AddSprite(Util.MakePicture(x, y, width, height, imgName), zIndex);
        }

        public static void DrawText(int x, int y, String text, int red,
                int green, int blue, String fontName, int size, int zIndex)
        {
            Font bf = Media.GetFont(fontName, size);
            bf.Color = new Color(red, green, blue, 1);
            AnonRender.DrawDelegate draw = delegate(SpriteBatch sb, GameTime elapsed)
            {
                sb.DrawString(bf.Face, text, new Vector2(x, y + bf.Face.MeasureString(text).Y), bf.Color); 
            };
            //Level.sCurrent.addSprite(r, zIndex);
        }
    }
}
