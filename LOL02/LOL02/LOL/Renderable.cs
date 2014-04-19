using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LOL
{
    // Replace with DrawableGameComponent?
    // Must be able to draw to specific SpriteBatch
    public class Renderable
    {
        public delegate void RenderDelegate(SpriteBatch sb, GameTime delta);
        public RenderDelegate render;
    }
}
