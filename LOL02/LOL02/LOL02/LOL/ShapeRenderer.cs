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
using Microsoft.Xna.Framework.Graphics;

namespace LOL
{
    /**
     * Draws vector shapes in a specific color.
     */
    public class ShapeRenderer
    {
        /**
         * Color of the lines
         */
        public Color Color = Color.Red;

        /**
         * Draws a rectangle.
         * 
         * @param x the X coordinate
         * @param y the Y coordinate
         * @param w the width
         * @param h the height
         */
        public void rect(int x, int y, int w, int h)
        {
            int bw = 2; // Border width
            Texture2D t = new Texture2D(Lol.sGame.GraphicsDevice, 1, 1);
            t.SetData(new[] { Color.White });
            SpriteBatch spriteBatch = new SpriteBatch(Lol.sGame.GraphicsDevice);
            Rectangle r = new Rectangle(x, y, w, h);
            spriteBatch.Begin();
            spriteBatch.Draw(t, new Rectangle(r.Left, r.Top, bw, r.Height), Color); // Left
            spriteBatch.Draw(t, new Rectangle(r.Right, r.Top, bw, r.Height), Color); // Right
            spriteBatch.Draw(t, new Rectangle(r.Left, r.Top, r.Width, bw), Color); // Top
            spriteBatch.Draw(t, new Rectangle(r.Left, r.Bottom, r.Width, bw), Color); // Bottom
            spriteBatch.End();
        }
    }
}
