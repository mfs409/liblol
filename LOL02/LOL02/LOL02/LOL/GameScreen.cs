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
     * GameScreen represents a separate screen drawn and managed by the ScreenManager
     * which allows the game to switch from the PreScene, PostScene, Level, Chooser,
     * Help, and Splash screens since XNA/MonoGame does not provide a simpler way to
     * encapsulate code representing a new game screen.
     */
    public class GameScreen
    {
        /**
         * Update delegate used for dynamically overloading the Update method
         */
        public delegate void UpdateDelegate(GameTime gameTime);

        /**
         * Draw delegate used for dynamically overloading the Draw method
         */
        public delegate void DrawDelegate(GameTime gameTime);

        /**
         * Update delegate
         */
        public UpdateDelegate fUpdate;

        /**
         * Draw delegate
         */
        public DrawDelegate fDraw;

        /**
         * Update method handled by ScreenManager for XNA/MonoGame calls to Update
         */
        public virtual void Update(GameTime gameTime)
        {
            // Update
            if (fUpdate != null)
            {
                fUpdate(gameTime);
            }
        }

        /**
         * Draw method handled by ScreenManager for XNA/MonoGame calls to Draw
         */
        public virtual void Draw(GameTime gameTime)
        {
            // Draw
            if (fDraw != null)
            {
                fDraw(gameTime);
            }
        }
    }
}
