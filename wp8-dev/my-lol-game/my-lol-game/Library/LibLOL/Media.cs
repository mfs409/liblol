using System;
using System.Collections.Generic;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    public class Media
    {

        public static Dictionary<String, Texture2D[]> sImages = new Dictionary<String, Texture2D[]>();

        public static Texture2D[] GetImage(String imgName)
        {
            Texture2D[] ret;
            sImages.TryGetValue(imgName, out ret);
            return ret;
        }
    }
}
