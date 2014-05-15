/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace LOL
{
    /**
     * Handles displaying a GameScreen object by activating Draw and Update as
     * they are called on the screen manager itself by the Lol class.
     */
    public class ScreenManager
    {
        /**
         * List of game screens (preloaded)
         */
        List<GameScreen> screens;

        /**
         * Current screen displayed
         */
        protected GameScreen currentScreen;

        /**
         * Returns current screen being displayed
         */
        public GameScreen Screen
        {
            get { return currentScreen; }
        }

        /**
         * Creates a new ScreenManager object.
         */
        public ScreenManager()
        {
            screens = new List<GameScreen>();
            currentScreen = null;
        }

        /**
         * Adds a GameScreen to the preloaded list and is returned with an index.
         * 
         * @param s the GameScreen to preload
         * @return the activation index
         */
        public int Add(GameScreen s)
        {
            screens.Add(s);
            return screens.Count - 1;
        }

        /**
         * Displays a screen from an activation index.
         * 
         * @param id the activation index
         */
        public void Display(int id)
        {
            currentScreen = screens[id];
        }

        /**
         * Displays a GameScreen
         * 
         * @param s the GameScreen to display
         */
        public void Display(GameScreen s)
        {
            currentScreen = s;
        }

        /**
         * Invokes Update on a GameScreen if one is set
         */
        public void Update(GameTime gameTime)
        {
            if (currentScreen != null)
            {
                currentScreen.Update(gameTime);
            }
        }

        /**
         * Invokes Draw on a GameScreen if one is set
         */
        public void Draw(GameTime gameTime)
        {
            if (currentScreen != null)
            {
                currentScreen.Draw(gameTime);
            }
        }
    }
}
