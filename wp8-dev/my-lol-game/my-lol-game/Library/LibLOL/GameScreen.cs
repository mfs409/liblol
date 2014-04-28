using System;

using Microsoft.Xna.Framework;

namespace LibLOL
{
    internal abstract class GameScreen
    {
        internal abstract void Update(GameTime gameTime);

        internal abstract void Draw(GameTime gameTime);
    }
}
