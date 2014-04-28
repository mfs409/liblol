using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    internal class Font
    {
        private SpriteFont mFace;
        private Color mColor;

        internal SpriteFont Face
        {
            get { return mFace; }
        }

        internal Color Color
        {
            get { return mColor; }
        }

        internal Font(SpriteFont sf, Color c)
        {
            mFace = sf;
            mColor = c;
        }
    }
}
