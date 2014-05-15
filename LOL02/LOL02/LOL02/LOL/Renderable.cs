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

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LOL
{
    /**
     * Provides a wrapper for Update and Draw as used by objects in the GameScreen
     */
    public abstract class Renderable
    {
        /**
         * Updates a renderable (called less-frequently than Draw)
         * 
         * @param gameTime the time since the game launched
         */
        public abstract void Update(GameTime gameTime);

        /**
         * Draws a renderable
         * 
         * @param spriteBatch the sprite batch for drawing the object
         * @param gameTime the time since the game launched
         */
        public abstract void Draw(SpriteBatch spriteBatch, GameTime gameTime);
    }

    /**
     * Subclasses Renderable with ability to use anonymously with delegates.
     */
    public class AnonRenderable : Renderable
    {
        /**
         * Draw delegate
         */
        public delegate void RenderDelegate(SpriteBatch sb, GameTime gt);

        /**
         * Used for handling calls to Draw
         */
        protected RenderDelegate Render;

        /**
         * Constructs a new anonymous renderable with Draw delegate
         * 
         * @param d the Draw delegate
         */
        public AnonRenderable(RenderDelegate d)
        {
            Render = d;
        }

        /**
         * Overridden for abstract class purposes
         */
        public override void Update(GameTime gameTime)
        {
            
        }

        /**
         * Draws the renderable by invoking the draw delegate
         * 
         * @param spriteBatch the sprite batch for drawing the renderable
         * @param gameTime the time since the game launched
         */
        public override void Draw(SpriteBatch spriteBatch, GameTime gameTime)
        {
            Render(spriteBatch, gameTime);
        }
    }
}
