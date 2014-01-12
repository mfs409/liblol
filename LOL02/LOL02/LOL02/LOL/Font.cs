using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LOL
{
    public class Font
    {
        public SpriteFont Face;
        public Color Color;

        public Font(SpriteFont f, Color c)
        {
            Face = f;
            Color = c;
        }
    }
}
