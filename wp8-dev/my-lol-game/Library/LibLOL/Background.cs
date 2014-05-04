﻿using System;
using System.Collections.Generic;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    public class Background
    {
        /**
         * The color that should be shown behind everything
         */
        internal Color mColor = new Color(1, 1, 1, 1);

        /**
         * All the background layers to show for the current level
         */
        private List<ParallaxLayer> mLayers = new List<ParallaxLayer>();

        /**
         * This object holds the configuration information for a Parallax layer.
         */
        internal class ParallaxLayer
        {
            /**
             * How fast should this layer scroll in the X dimension
             */
            internal float mXSpeed;

            /**
             * How fast should it scroll in Y
             */
            internal float mYSpeed;

            /**
             * The image to display
             */
            internal Texture2D mImage;

            /**
             * How much X offset when drawing this (only useful for Y repeat)
             */
            internal float mXOffset;

            /**
             * How much Y offset when drawing this (only useful for X repeat)
             */
            internal float mYOffset;

            /**
             * Loop in X?
             */
            internal bool mXRepeat;

            /**
             * Loop in Y?
             */
            internal bool mYRepeat;

            /**
             * Simple constructor... just set the fields
             * 
             * @param xSpeed Speed that the layer moves in the X dimension
             * @param ySpeed Y speed
             * @param tr Image to use
             * @param xOffset X offset
             * @param yOffset Y offset
             */
            internal ParallaxLayer(float xSpeed, float ySpeed, Texture2D tr, float xOffset, float yOffset)
            {
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
        internal void DrawLayers(SpriteBatch sb)
        {
            // center camera on mGameCam's camera
            float x = Level.sCurrent.mGameCam.position.X;
            float y = Level.sCurrent.mGameCam.position.Y;

            Level.sCurrent.mBgCam.position = new Vector3(x, y, 0);
            Level.sCurrent.mBgCam.Update();

            // draw the layers
            foreach (ParallaxLayer pl in mLayers)
            {
                // each layer has a different projection, based on its speed
                // NOTE: Uncomment
                //sb.setProjectionMatrix(Level.sCurrent.mBgCam.calculateParallaxMatrix(pl.mXSpeed
                //        * Physics.PIXEL_METER_RATIO, pl.mYSpeed * Physics.PIXEL_METER_RATIO));
                sb.Begin();
                // Figure out what to draw for layers that repeat in the x dimension
                if (pl.mXRepeat)
                {
                    // get the camera center, translate to pixels, and scale by
                    // speed
                    float startX = x * Physics.PIXEL_METER_RATIO * pl.mXSpeed;
                    // subtract one and a half screens worth of repeated pictures
                    float screensBefore = 1.5f;
                    // adjust by zoom... for every level of zoom, we need that much
                    // more beforehand
                    screensBefore += Level.sCurrent.mBgCam.zoom;
                    startX -= (screensBefore * Lol.sGame.mConfig.GetScreenWidth());
                    // round down to nearest screen width
                    startX = startX - startX % pl.mImage.Width;  // NOTE: replacing getRegionWidth()
                    float currX = startX;
                    // draw picture repeatedly until we've drawn enough to cover the
                    // screen. "enough" can be approximated as 2 screens plus twice
                    // the zoom factor
                    float limit = 2 + 2 * Level.sCurrent.mBgCam.zoom;
                    while (currX < startX + limit * Lol.sGame.mConfig.GetScreenWidth())
                    {
                        sb.Draw(pl.mImage, new Vector2(currX, pl.mYOffset), Color.White);
                        currX += pl.mImage.Width;
                    }
                }
                // Figure out what to draw for layers that repeat in the y dimension
                else if (pl.mYRepeat)
                {
                    // get the camera center, translate, and scale
                    float startY = y * Physics.PIXEL_METER_RATIO * pl.mYSpeed;
                    // subtract enough screens, as above
                    startY -= (1.5f + Level.sCurrent.mBgCam.zoom) * Lol.sGame.mConfig.GetScreenHeight();
                    // round
                    startY = startY - startY % pl.mImage.Height;
                    float currY = startY;
                    // draw a bunch of repeated images
                    float limit = 2 + 2 * Level.sCurrent.mBgCam.zoom;
                    while (currY < startY + limit * Lol.sGame.mConfig.GetScreenHeight())
                    {
                        sb.Draw(pl.mImage, new Vector2(pl.mXOffset, currY), Color.White);
                        currY += pl.mImage.Height;
                    }
                }
                // draw a layer that never changes based on the camera's X
                // coordinate
                else if (pl.mXSpeed == 0)
                {
                    sb.Draw(pl.mImage, new Vector2(-pl.mImage.Width / 2 + pl.mXOffset, pl.mYOffset), Color.White);
                }
                // draw a layer that never changes based on the camera's Y
                // coordinate
                else if (pl.mYSpeed == 0)
                {
                    sb.Draw(pl.mImage, new Vector2(pl.mXOffset, -pl.mImage.Height / 2 + pl.mYOffset), Color.White);
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
        public static void SetColor(int red, int green, int blue)
        {
            Level.sCurrent.mBackground.mColor.R = (byte)(((float)red) / 255);
            Level.sCurrent.mBackground.mColor.G = (byte)(((float)green) / 255);
            Level.sCurrent.mBackground.mColor.B = (byte)(((float)blue) / 255);
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
        public static void AddHorizontalLayer(float xSpeed, float ySpeed, String imgName, float yOffset)
        {
            ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed, Media.GetImage(imgName)[0], 0, yOffset
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
        public static void AddVerticalLayer(float xSpeed, float ySpeed, String imgName, float xOffset)
        {
            ParallaxLayer pl = new ParallaxLayer(xSpeed, ySpeed, Media.GetImage(imgName)[0], xOffset
                    * Physics.PIXEL_METER_RATIO, 0);
            pl.mYRepeat = ySpeed != 0;
            Level.sCurrent.mBackground.mLayers.Add(pl);
        }
    }
}
