using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace LOL
{
    public class GameScreen
    {
        public delegate void UpdateDelegate(GameTime gameTime);
        public delegate void DrawDelegate(GameTime gameTime);
        public UpdateDelegate fUpdate;
        public DrawDelegate fDraw;

        public void Update(GameTime gameTime)
        {
            // Update
            if (fUpdate != null)
            {
                fUpdate(gameTime);
            }
        }

        public void Draw(GameTime gameTime)
        {
            // Draw
            if (fDraw != null)
            {
                fDraw(gameTime);
            }
        }
    }
}
