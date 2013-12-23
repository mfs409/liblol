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

package edu.lehigh.cse.lol;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * The Background class provides a way to declare images that go in the
 * background of the game, and which automatically pan and repeat
 */
public class Background {
    /**
     * The color that should be shown behind everything
     */
    Color mColor = new Color(0, 0, 0, 1);

    /**
     * All the background layers to show for the current level
     */
    private ArrayList<ParallaxLayer> mLayers = new ArrayList<ParallaxLayer>();

    /**
     * This object holds the configuration information for a Parallax layer.
     */
    static class ParallaxLayer {
        /**
         * How fast should this layer scroll in the X dimension
         */
        private float mXSpeed;

        /**
         * How fast should it scroll in Y
         */
        private float mYSpeed;

        /**
         * The image to display
         */
        private TextureRegion mImage;

        /**
         * How much X offset when drawing this (only useful for Y repeat)
         */
        private float mXOffset;

        /**
         * How much Y offset when drawing this (only useful for X repeat)
         */
        private float mYOffset;

        /**
         * Loop in X?
         */
        private boolean mXRepeat;

        /**
         * Loop in Y?
         */
        private boolean mYRepeat;

        /**
         * Simple constructor... just set the fields
         * 
         * @param xSpeed Speed that the layer moves in the X dimension
         * @param ySpeed Y speed
         * @param tr Image to use
         * @param xOffset X offset
         * @param yOffset Y offset
         */
        ParallaxLayer(float xSpeed, float ySpeed, TextureRegion tr, float xOffset, float yOffset) {
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
    void renderLayers(SpriteBatch sb) {
        // center camera on _gameCam's camera
        float x = Level._currLevel._gameCam.position.x;
        float y = Level._currLevel._gameCam.position.y;
        Level._currLevel._bgCam.position.set(x, y, 0);
        Level._currLevel._bgCam.update();

        // draw the layers
        for (ParallaxLayer pl : mLayers) {
            // each layer has a different projection, based on its speed
            sb.setProjectionMatrix(Level._currLevel._bgCam.calculateParallaxMatrix(pl.mXSpeed
                    * Physics.PIXEL_METER_RATIO, pl.mYSpeed * Physics.PIXEL_METER_RATIO));
            sb.begin();
            // Figure out what to draw for layers that repeat in the x dimension
            if (pl.mXRepeat) {
                // get the camera center, translate to pixels, and scale by
                // speed
                float startX = x * Physics.PIXEL_METER_RATIO * pl.mXSpeed;
                // subtract one and a half screens worth of repeated pictures
                float screensBefore = 1.5f;
                // adjust by zoom... for every level of zoom, we need that much
                // more beforehand
                screensBefore += Level._currLevel._bgCam.zoom;
                startX -= (screensBefore * LOL._game._config.getScreenWidth());
                // round down to nearest screen width
                startX = startX - startX % pl.mImage.getRegionWidth();
                float currX = startX;
                // draw picture repeatedly until we've drawn enough to cover the
                // screen. "enough" can be approximated as 2 screens plus twice
                // the zoom factor
                float limit = 2 + 2 * Level._currLevel._bgCam.zoom;
                while (currX < startX + limit * LOL._game._config.getScreenWidth()) {
                    sb.draw(pl.mImage, currX, pl.mYOffset);
                    currX += pl.mImage.getRegionWidth();
                }
            }
            // Figure out what to draw for layers that repeat in the y dimension
            else if (pl.mYRepeat) {
                // get the camera center, translate, and scale
                float startY = y * Physics.PIXEL_METER_RATIO * pl.mYSpeed;
                // subtract enough screens, as above
                startY -= (1.5f + Level._currLevel._bgCam.zoom)
                        * LOL._game._config.getScreenHeight();
                // round
                startY = startY - startY % pl.mImage.getRegionHeight();
                float currY = startY;
                // draw a bunch of repeated images
                float limit = 2 + 2 * Level._currLevel._bgCam.zoom;
                while (currY < startY + limit * LOL._game._config.getScreenHeight()) {
                    sb.draw(pl.mImage, pl.mXOffset, currY);
                    currY += pl.mImage.getRegionHeight();
                }
            }
            // draw a layer that never changes based on the camera's X
            // coordinate
            else if (pl.mXSpeed == 0) {
                sb.draw(pl.mImage, -pl.mImage.getRegionWidth() / 2 + pl.mXOffset, pl.mYOffset);
            }
            // draw a layer that never changes based on the camera's Y
            // coordinate
            else if (pl.mYSpeed == 0) {
                sb.draw(pl.mImage, pl.mXOffset, -pl.mImage.getRegionHeight() / 2 + pl.mYOffset);
            }
            sb.end();
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
        Level._currLevel._background.mColor.r = ((float)red) / 255;
        Level._currLevel._background.mColor.g = ((float)green) / 255;
        Level._currLevel._background.mColor.b = ((float)blue) / 255;
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
        Level._currLevel._background.mLayers.add(pl);
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
        Level._currLevel._background.mLayers.add(pl);
    }
}
