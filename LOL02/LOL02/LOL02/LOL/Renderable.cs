using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LOL
{
    public abstract class Renderable
    {
        public abstract void Update(GameTime gameTime);

        public abstract void Draw(SpriteBatch spriteBatch, GameTime gameTime);
    }

    public class AnonRenderable : Renderable
    {
        public delegate void RenderDelegate(SpriteBatch sb, GameTime gt);
        protected RenderDelegate Render;

        public AnonRenderable(RenderDelegate d)
        {
            Render = d;
        }

        public override void Update(GameTime gameTime)
        {
            
        }

        public override void Draw(SpriteBatch spriteBatch, GameTime gameTime)
        {
            Render(spriteBatch, gameTime);
        }
    }
}
