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
     * Defines a standard Orthographic Camera that can be used in XNA/MonoGame since
     * cameras are a luxury in the SDK.  This basically allows for zooming and following
     * objects while providing calculated adjustments for drawing and touch input.
     */
    public class OrthographicCamera
    {
        /**
         * Viewport upon which we see the world
         * @note Measured in level coordinates
         */
        public Vector2 Viewport;

        /**
         * World observed by the camera
         * @note Measured in level coordinates
         */
        public Vector2 World;

        /**
         * Center on which the camera should focus
         * @note Measured in level coordinates
         */
        public Vector2 Center;

        /**
         * Screen dimensions
         * @note Measured in pixels
         */
        public Vector2 Screen;

        /**
         * Zoom factor
         */
        public float Zoom;

        /**
         * Coordinate conversion constants for LibGDX to XNA/MonoGame conversion
         */
        const int GDX_W = 48;
        const int GDX_H = 32;

        /**
         * Defines a new orthographic camera viewing a world of a given width and height.
         * 
         * @param w the world's width (in level coordinates)
         * @param h the world's height (in level coordinates)
         */
        public OrthographicCamera(float w, float h)
        {
            Zoom = 1;
            World = new Vector2(w, h);  // LevelCoords
            Viewport = new Vector2(GDX_W, GDX_H);   // LevelCoords
            Screen = new Vector2(Lol.sGame.GraphicsDevice.DisplayMode.Height, Lol.sGame.GraphicsDevice.DisplayMode.Width);
            Center = new Vector2(World.X / 2, World.Y / 2);
        }

        /**
         * Converts zoom factor to an actual scale that can be multiplied to some
         * dimension.
         * 
         * @return the scale factor
         */
        public float ZoomToScale()
        {
            return 1 / Zoom;
        }

        /**
         * Centers on a PhysicsSprite.
         * 
         * @param o the PhysicsSprite to chase
         */
        public void chase(PhysicsSprite o)
        {
            setChase(o.mBody.Position.X, o.mBody.Position.Y);
        }

        /**
         * Centers the camera on a specific point
         * 
         * @param x the X coordinate (level coordinates)
         * @param y the Y coordinate (level coordinates)
         */
        public void setChase(float x, float y)
        {
            Center = new Vector2(x, y);
        }
        
        /**
         * Converts touch inputs to actual screen coordinates based on viewport offsets
         * 
         * @param x the touch input received on the screen
         * @return the touch X coordinate in pixels as a unique point in the world
         */
        public int touchX(int x)
        {
            return screenX(vpLeft())+x;
        }

        /**
         * Converts touch inputs to actual screen coordinates based on viewport offsets
         * 
         * @param y the touch input received on the screen
         * @return the touch Y coordinate in pixels as a unique point in the world
         */
        public int touchY(int y)
        {
            return invertScreenY(screenY(vpTop())+y);
        }

        /**
         * Convert to screen-based world coordinates
         * 
         * @param x the X coordinate to convert
         * @return the world coordinate in pixels
         */
        public int worldX(float x)
        {
            return (int)(x / World.X * Screen.X);
        }

        /**
         * Convert to screen-based world coordinates
         * 
         * @param y the Y coordinate to convert
         * @return the world coordinate in pixels
         */
        public int worldY(float y)
        {
            return (int)(y / World.Y * Screen.Y);
        }

        /**
         * Converts viewport coordinates to screen coordinates in pixels
         * 
         * @param x the X coordinate to convert
         * @return the X coordinate in pixels
         */
        public int screenX(float x)
        {
            return (int)(x / Viewport.X * Screen.X);
        }

        /**
         * Converts viewport coordinates to screen coordinates in pixels
         * 
         * @param y the Y coordinate to convert
         * @return the Y coordinate in pixels
         */
        public int screenY(float y)
        {
            return (int)(y / Viewport.Y * Screen.Y);
        }

        /**
         * Converts pixels to level coordinates
         * 
         * @param x the X coordinate
         * @return x in pixels
         */
        public float levelX(float x)
        {
            return x / Screen.X * Viewport.X;
        }

        /**
         * Converts pixels to level coordinates
         * 
         * @param y the Y coordinate
         * @return y in pixels
         */
        public float levelY(float y)
        {
            return y / Screen.Y * Viewport.Y;
        }

        /**
         * Inverts the Y coordinate for a screen coordinate in pixels.
         * 
         * @param y the Y coordinate
         * @return inverted Y
         */
        public int invertScreenY(float y)
        {
            return (int)(Screen.Y - y);
        }

        /**
         * Inverts the Y coordinate for a level coordinate in pixels.
         * 
         * @param y the Y coordinate
         * @return inverted Y
         */
        public float invertLevelY(float y)
        {
            return World.Y-y;
        }

        /**
         * Calculate offsets for viewport
         * 
         * @return X offset
         */
        protected float vpLeft()
        {
            return Math.Min(Math.Max(0, Center.X - (Viewport.X / ZoomToScale() / 2)), World.X-(Viewport.X/ZoomToScale()));
        }

        /**
         * Calculate offsets for viewport
         * 
         * @return Y offset (inverted)
         */
        protected float vpTop()
        {
            return Math.Min(Math.Max(0, Center.Y - (Viewport.Y /ZoomToScale() / 2)), World.Y-(Viewport.Y/ZoomToScale()));
        }

        /**
         * Calculate offsets for viewport
         * 
         * @return Y offset (not inverted)
         */
        protected float vpNormalTop()
        {
            return World.Y-vpTop();
        }

        /**
         * Calculate offsets for drawing in the viewport
         * 
         * @param x the X coordinate (in level coordinates)
         * @return X offset in pixels
         */
        public int drawX(float x)
        {
            x -= vpLeft();
            x *= ZoomToScale();
            return (int)(screenX(x));
        }

        /**
         * Calculate offsets for drawing in the viewport
         * 
         * @param y the Y coordinate (in level coordinates)
         * @return Y offset in pixels
         */
        public int drawY(float y)
        {
            y -= vpTop();
            y *= ZoomToScale();
            return invertScreenY(screenY(y));
        }

        /**
         * Calculate offsets for drawing in the viewport
         * 
         * @param y the Y coordinate (in level coordinates)
         * @return Y offset in pixels (not inverted)
         */
        public int drawNormalY(float y)
        {
            y = vpNormalTop();
            y *= ZoomToScale();
            return screenY(y);
        }

        /**
         * Calculate offsets for drawing in the viewport
         * 
         * @param x the width in level coordinates
         * @return width in pixels
         */
        public int drawWidth(float x)
        {
            return (int)(screenX(x) * ZoomToScale());
        }

        /**
         * Calculate offsets for drawing in the viewport
         * 
         * @param y the height in level coordinates
         * @return height in pixels
         */
        public int drawHeight(float y)
        {
            return (int)(screenY(y)*ZoomToScale());
        }

        /**
         * Calculates drawing by adding the X-offset to an X starting coordinate
         * for binding it to a world coordinate.
         * 
         * @param xStart the X coordinate in pixels
         * @return X with an offset
         */
        public float viewX(float xStart)
        {
            return screenX(vpLeft()) + xStart;
        }

        /**
         * Calculates drawing by adding the Y-offset to an Y starting coordinate
         * for binding it to a world coordinate.
         * 
         * @param yStart the Y coordinate in pixels
         * @return Y with an offset
         */
        public float viewY(float yStart)
        {
            return screenY(vpTop()) + yStart;
        }
    }
}
