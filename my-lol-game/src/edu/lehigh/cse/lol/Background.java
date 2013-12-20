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
    static class ParallaxLayer {
        float _xSpeed;

        float _ySpeed;

        TextureRegion _tr;

        float _xOffset;

        float _yOffset;

        boolean _xRepeat;

        boolean _yRepeat;

        ParallaxLayer(float xSpeed, float ySpeed, TextureRegion tr, float xOffset, float yOffset) {
            _xSpeed = xSpeed;
            _ySpeed = ySpeed;
            _tr = tr;
            _xOffset = xOffset;
            _yOffset = yOffset;
        }
    }

    /*
     * INTERNAL INTERFACE
     */

    /**
     * The color that should be shown behind everything
     */
    Color _c = new Color(0, 0, 0, 1);

    /**
     * All the background layers get put into here
     */
    ArrayList<ParallaxLayer> _layers = new ArrayList<ParallaxLayer>();

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
        for (ParallaxLayer pl : _layers) {
            // each layer has a different projection, based on its speed
            sb.setProjectionMatrix(Level._currLevel._bgCam.calculateParallaxMatrix(pl._xSpeed
                    * Physics.PIXEL_METER_RATIO, pl._ySpeed * Physics.PIXEL_METER_RATIO));
            sb.begin();
            // Figure out what to draw for layers that repeat in the x dimension
            if (pl._xRepeat) {
                // NB: this while loop is not the most efficient way to draw the background
                int i = -(int)pl._tr.getRegionWidth() / 2;
                while (i / Physics.PIXEL_METER_RATIO < x + Level._currLevel._camBoundX) {
                    sb.draw(pl._tr, i, pl._yOffset);
                    i += pl._tr.getRegionWidth();
                }
            }
            // Figure out what to draw for layers that repeat in the y dimension
            else if (pl._yRepeat) {
                // NB: this while loop is not the most efficient way to draw the background
                int i = 0;
                while (i / Physics.PIXEL_METER_RATIO < y + Level._currLevel._camBoundY) {
                    sb.draw(pl._tr, pl._xOffset, i);
                    i += pl._tr.getRegionHeight();
                }
            }
            // draw a layer that never changes based on the camera's X
            // coordinate
            else if (pl._xSpeed == 0) {
                sb.draw(pl._tr, -pl._tr.getRegionWidth() / 2 + pl._xOffset, pl._yOffset);
            }
            // draw a layer that never changes based on the camera's Y
            // coordinate
            else if (pl._ySpeed == 0) {
                sb.draw(pl._tr, pl._xOffset, -pl._tr.getRegionHeight() / 2 + pl._yOffset);
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
        Level._currLevel._background._c.r = ((float)red) / 255;
        Level._currLevel._background._c.g = ((float)green) / 255;
        Level._currLevel._background._c.b = ((float)blue) / 255;
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
        pl._xRepeat = xSpeed != 0;
        Level._currLevel._background._layers.add(pl);
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
        pl._yRepeat = ySpeed != 0;
        Level._currLevel._background._layers.add(pl);
    }
}
