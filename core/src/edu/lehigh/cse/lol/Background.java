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

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

import edu.lehigh.cse.lol.internals.ParallaxLayer;

/**
 * The Background class provides a way to declare images that go in the
 * background of the game, and which automatically pan and repeat. Note that if
 * you wish to have a background image that does not pan and repeat, you should
 * use Util.drawPicture.
 */
public class Background {
    /**
     * All the background layers to show for the current level
     */
    final ArrayList<ParallaxLayer> mLayers = new ArrayList<>();

    /**
     * The color that should be shown behind everything
     */
    Color mColor = new Color(1, 1, 1, 1);

    /**
     * This method, called from the render loop, is responsible for drawing all
     * of the layers
     *
     * @param sb
     *            The SpriteBatch that is being used to do the drawing.
     */
    void renderLayers(Level level, SpriteBatch sb, float elapsed) {
        // center camera on mGameCam's camera
        float x = level.mGameCam.position.x;
        float y = level.mGameCam.position.y;
        level.mBgCam.position.set(x, y, 0);
        level.mBgCam.update();

        // draw the layers
        for (ParallaxLayer pl : mLayers) {
            // each layer has a different projection, based on its speed
            sb.setProjectionMatrix(level.mBgCam
                    .calculateParallaxMatrix(pl.mXSpeed
                            * Lol.PIXEL_METER_RATIO, pl.mYSpeed
                            * Lol.PIXEL_METER_RATIO));
            sb.begin();
            // handle auto layers
            if (pl.mAutoX) {
                // hack for changing the projection matrix
                sb.end();
                sb.setProjectionMatrix(level.mBgCam
                        .calculateParallaxMatrix(0, 0));
                sb.begin();
                // update position, based on elapsed time
                pl.mLastX += pl.mXSpeed * elapsed;
                if (pl.mLastX > Lol.sGame.mWidth)
                    pl.mLastX = 0;
                if (pl.mLastX < -Lol.sGame.mWidth)
                    pl.mLastX = 0;
                // figure out the starting point for drawing
                float startPoint = pl.mLastX;
                while (startPoint > -Lol.sGame.mWidth)
                    startPoint -= pl.mWidth;
                // start drawing
                while (startPoint < Lol.sGame.mWidth) {
                    sb.draw(pl.mImage, startPoint, pl.mYOffset, pl.mWidth,
                            pl.mHeight);
                    startPoint += pl.mWidth;
                }
            }
            // Figure out what to draw for layers that repeat in the x dimension
            else if (pl.mXRepeat) {
                // getLoseScene the camera center, translate to pixels, and scale by
                // speed
                float startX = x * Lol.PIXEL_METER_RATIO * pl.mXSpeed;
                // subtract one and a half screens worth of repeated pictures
                float screensBefore = 2.5f;
                // adjust by zoom... for every level of zoom, we need that much
                // more beforehand
                screensBefore += level.mBgCam.zoom;
                startX -= (screensBefore * Lol.sGame.mWidth);
                // round down to nearest screen width
                startX = startX - startX % pl.mImage.getRegionWidth();
                float currX = startX;
                // draw picture repeatedly until we've drawn enough to cover the
                // screen. "enough" can be approximated as 2 screens plus twice
                // the zoom factor
                float limit = 2 + 2 * level.mBgCam.zoom;
                while (currX < startX + limit * Lol.sGame.mWidth) {
                    sb.draw(pl.mImage, currX, pl.mYOffset, pl.mWidth,
                            pl.mHeight);
                    currX += pl.mImage.getRegionWidth();
                }
            }
            // Figure out what to draw for layers that repeat in the y dimension
            else if (pl.mYRepeat) {
                // getLoseScene the camera center, translate, and scale
                float startY = y * Lol.PIXEL_METER_RATIO * pl.mYSpeed;
                // subtract enough screens, as above
                startY -= (1.5f + level.mBgCam.zoom)
                        * Lol.sGame.mHeight;
                // round
                startY = startY - startY % pl.mImage.getRegionHeight();
                float currY = startY;
                // draw a bunch of repeated images
                float limit = 2 + 2 * level.mBgCam.zoom;
                while (currY < startY + limit * Lol.sGame.mHeight) {
                    sb.draw(pl.mImage, pl.mXOffset, currY);
                    currY += pl.mImage.getRegionHeight();
                }
            }
            // draw a layer that never changes based on the camera's X
            // coordinate
            else if (pl.mXSpeed == 0) {
                sb.draw(pl.mImage, -pl.mImage.getRegionWidth() / 2
                        + pl.mXOffset, pl.mYOffset);
            }
            // draw a layer that never changes based on the camera's Y
            // coordinate
            else if (pl.mYSpeed == 0) {
                sb.draw(pl.mImage, pl.mXOffset, -pl.mImage.getRegionHeight()
                        / 2 + pl.mYOffset);
            }
            sb.end();
        }
    }
}
