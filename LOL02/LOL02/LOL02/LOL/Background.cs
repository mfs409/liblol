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
     * The Background class provides a way to declare images that go in the
     * background of the game, and which automatically pan and repeat
     */
    public class Background
    {
        /**
         * The color that should be shown behind everything
         */
        public Color mColor = Color.White;

        /**
         * All the background layers to show for the current level
         */
        private List<ParallaxLayer> mLayers = new List<ParallaxLayer>();

        /**
         * This object holds the configuration information for a Parallax layer.
         */
        public class ParallaxLayer {
            /**
             * How fast should this layer scroll in the X dimension
             */
            public float mXSpeed;

            /**
             * How fast should it scroll in Y
             */
            public float mYSpeed;

            /**
             * The image to display
             */
            public Texture2D mImage;

            /**
             * How much X offset when drawing this (only useful for Y repeat)
             */
            public float mXOffset;

            /**
             * How much Y offset when drawing this (only useful for X repeat)
             */
            public float mYOffset;

            /**
             * Loop in X?
             */
            public bool mXRepeat;

            /**
             * Loop in Y?
             */
            public bool mYRepeat;

            /**
             * Simple constructor... just set the fields
             * 
             * @param xSpeed Speed that the layer moves in the X dimension
             * @param ySpeed Y speed
             * @param tr Image to use
             * @param xOffset X offset
             * @param yOffset Y offset
             */
            public ParallaxLayer(float xSpeed, float ySpeed, Texture2D tr, float xOffset, float yOffset) {
                mXSpeed = xSpeed;
                mYSpeed = ySpeed;
                mImage = tr;
                mXOffset = xOffset;
                mYOffset = yOffset;
            }
        }

        /**
         * This method, called from the render loop, is responsible for drawing all
         * of the layers
         * 
         * @param sb The SpriteBatch that is being used to do the drawing.
         */
        public void renderLayers(SpriteBatch sb) {
            
            float zoom = Level.sCurrent.mGameCam.ZoomToScale();
            int sw = Lol.sGame.mConfig.getScreenWidth(),
                sh = Lol.sGame.mConfig.getScreenHeight();

            // Draw each layer
            foreach (ParallaxLayer pl in mLayers)
            {
                int xOff = (int)Level.sCurrent.mGameCam.levelX(pl.mXOffset), yOff = (int)Level.sCurrent.mGameCam.levelY(pl.mYOffset);
                float xSpeed = pl.mXSpeed, ySpeed = pl.mYSpeed;

                Texture2D img = pl.mImage;
                int x, y;

                // Find the biggest area to scale
                float scale = sw / (float)img.Width;
                int width = (int)(img.Width * zoom * scale),
                    height = (int)(img.Height * zoom * scale);

                // Draw segments
                sb.Begin();

                if (pl.mXRepeat)
                {
                    int c = (int)Math.Ceiling((float)sw/width)+1;
                    x = (int)(Level.sCurrent.mGameCam.drawX(xOff) * xSpeed) % width;
                    y = Level.sCurrent.mGameCam.drawY(yOff);
                    
                    for (int j = 0; j < c; j++)
                    {
                        sb.Draw(img, new Rectangle(x+(width*j), y-height, width, height), Color.White);
                    }
                }
                else if (pl.mYRepeat)
                {
                    int c = (int)Math.Ceiling((float)sh / height)+1;
                    x = Level.sCurrent.mGameCam.drawX(xOff);
                    y = -(int)(Level.sCurrent.mGameCam.drawNormalY(-yOff)*ySpeed) % height;
                    
                    for (int j = 0; j < c; j++)
                    {
                        sb.Draw(img, new Rectangle(x, y+(height*j), width, height), Color.White);
                    }
                }
                else
                {
                    x = 0;
                    y = 0;
                    sb.Draw(img, new Rectangle(x, y, width, height), Color.White);
                }

                sb.End();
            }
        }

        /*
         * PUBLIC INTERFACE
         */

        /**
         * Set the background color for the current level
         * 
         * @param red The amount of redness (0-255)
         * @param green The amount of greenness (0-255)
         * @param blue The amount of blueness (0-255)
         */
        static public void setColor(int red, int green, int blue) {
            Level.sCurrent.mBackground.mColor = new Color((float)red / 255, (float)green / 255, (float)blue / 255);
        }

        /**
         * Add a picture that may repeat in the X dimension
         * 
         * @param xSpeed Speed that the picture seems to move in the X direction.
         *            "1" is the same speed as the hero; "0" is not at all; ".5f" is
         *            at half the hero's speed
         * @param ySpeed Speed that the picture seems to move in the Y direction.
         *            "1" is the same speed as the hero; "0" is not at all; ".5f" is
         *            at half the hero's speed
         * @param imgName The name of the image file to use as the background
         * @param yOffset The default is to draw the image at y=0. This field allows
         *            the picture to be moved up or down.
         */
        static public void addHorizontalLayer(float xSpeed, float ySpeed, String imgName, float yOffset) {
            ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed, Media.getImage(imgName)[0], 0, yOffset
                    * Physics.PIXEL_METER_RATIO);
            pl.mXRepeat = xSpeed != 0;
            Level.sCurrent.mBackground.mLayers.Add(pl);
        }

        /**
         * Add a picture that may repeat in the Y dimension
         * 
         * @param xSpeed Speed that the picture seems to move in the X direction.
         *            "1" is the same speed as the hero; "0" is not at all; ".5f" is
         *            at half the hero's speed
         * @param ySpeed Speed that the picture seems to move in the Y direction.
         *            "1" is the same speed as the hero; "0" is not at all; ".5f" is
         *            at half the hero's speed
         * @param imgName The name of the image file to use as the background
         * @param xOffset The default is to draw the image at x=0. This field allows
         *            the picture to be moved left or right.
         */
        static public void addVerticalLayer(float xSpeed, float ySpeed, String imgName, float xOffset) {
            ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed, Media.getImage(imgName)[0], xOffset
                    * Physics.PIXEL_METER_RATIO, 0);
            pl.mYRepeat = ySpeed != 0;
            Level.sCurrent.mBackground.mLayers.Add(pl);
        }

    }
}
