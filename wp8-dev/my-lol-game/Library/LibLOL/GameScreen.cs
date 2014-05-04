using System;

using Microsoft.Xna.Framework;

namespace LibLOL
{
    internal abstract class GameScreen : DrawableGameComponent
    {
        private Game mGame;

        internal abstract void Update(GameTime gameTime);

        internal abstract void Draw(GameTime gameTime);

        protected GameScreen(Game game) : base(game)
        {
            mGame = game;
        }

        protected override void Dispose(bool disposing)
        {
            Game.Components.Remove(this);
            base.Dispose(disposing);
        }
    }
}
