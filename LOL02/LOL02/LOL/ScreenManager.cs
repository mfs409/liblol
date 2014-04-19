using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace LOL
{
    public class ScreenManager
    {
        List<GameScreen> screens;
        protected GameScreen currentScreen;
        public GameScreen Screen
        {
            get { return currentScreen; }
        }

        public ScreenManager()
        {
            screens = new List<GameScreen>();
            currentScreen = null;
        }

        public int Add(GameScreen s)
        {
            screens.Add(s);
            return screens.Count - 1;
        }

        public void Display(int id)
        {
            currentScreen = screens[id];
        }

        public void Display(GameScreen s)
        {
            currentScreen = s;
        }

        public void Update(GameTime gameTime)
        {
            if (currentScreen != null)
            {
                currentScreen.Update(gameTime);
            }
        }

        public void Draw(GameTime gameTime)
        {
            if (currentScreen != null)
            {
                currentScreen.Draw(gameTime);
            }
        }
    }
}
