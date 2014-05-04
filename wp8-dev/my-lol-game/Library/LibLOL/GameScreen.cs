using System;

using Microsoft.Xna.Framework;

namespace LibLOL
{
    public abstract class GameScreen : DrawableGameComponent
    {
        protected Game mGame;

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
