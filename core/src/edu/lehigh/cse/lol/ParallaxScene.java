package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

/**
 * Created by spear on 12/28/2016.
 */

class ParallaxScene {
    /// All the layers to show as part of this scene
    final ArrayList<ParallaxLayer> mLayers = new ArrayList<>();

    /// An optional background color to show behind everything
    Color mColor = new Color(1, 1, 1, 1);

    /// This camera is for drawing parallax backgrounds that go in front of or behind the world
    ///
    /// TODO: make private
    ParallaxCamera mBgCam;

    ParallaxScene(Config config) {
        int camWidth = config.mWidth;
        int camHeight = config.mHeight;

        // the background camera is like the hudcam
        mBgCam = new ParallaxCamera(camWidth, camHeight);
        mBgCam.position.set(camWidth / 2, camHeight / 2, 0);
        mBgCam.zoom = 1;
    }

    /**
     * This method, called from the render loop, is responsible for drawing all
     * of the layers
     *
     * @param sb
     *            The SpriteBatch that is being used to do the drawing.
     */
    void renderLayers(MainScene level, SpriteBatch sb, float elapsed) {
        // center camera on mCamera's camera
        float x = level.mCamera.position.x;
        float y = level.mCamera.position.y;
        mBgCam.position.set(x, y, 0);
        mBgCam.update();

        // draw the layers
        for (ParallaxLayer pl : mLayers) {
            // each layer has a different projection, based on its speed
            sb.setProjectionMatrix(mBgCam.calculateParallaxMatrix(pl.mXSpeed
                            * level.mConfig.mPixelMeterRatio, pl.mYSpeed
                            * level.mConfig.mPixelMeterRatio));
            sb.begin();
            // go auto layers
            if (pl.mAutoX) {
                // hack for changing the projection matrix
                sb.end();
                sb.setProjectionMatrix(mBgCam
                        .calculateParallaxMatrix(0, 0));
                sb.begin();
                // update position, based on elapsed time
                pl.mLastX += pl.mXSpeed * elapsed;
                if (pl.mLastX > level.mConfig.mWidth)
                    pl.mLastX = 0;
                if (pl.mLastX < -level.mConfig.mWidth)
                    pl.mLastX = 0;
                // figure out the starting point for drawing
                float startPoint = pl.mLastX;
                while (startPoint > -level.mConfig.mWidth)
                    startPoint -= pl.mWidth;
                // start drawing
                while (startPoint < level.mConfig.mWidth) {
                    sb.draw(pl.mImage, startPoint, pl.mYOffset, pl.mWidth,
                            pl.mHeight);
                    startPoint += pl.mWidth;
                }
            }
            // Figure out what to draw for layers that repeat in the x dimension
            else if (pl.mXRepeat) {
                // getLoseScene the camera center, translate to pixels, and scale by
                // speed
                float startX = x * level.mConfig.mPixelMeterRatio * pl.mXSpeed;
                // subtract one and a half screens worth of repeated pictures
                float screensBefore = 2.5f;
                // adjust by zoom... for every level of zoom, we need that much
                // more beforehand
                screensBefore += mBgCam.zoom;
                startX -= (screensBefore * level.mConfig.mWidth);
                // round down to nearest screen width
                startX = startX - startX % pl.mImage.getRegionWidth();
                float currX = startX;
                // draw picture repeatedly until we've drawn enough to cover the
                // screen. "enough" can be approximated as 2 screens plus twice
                // the zoom factor
                float limit = 2 + 2 * mBgCam.zoom;
                while (currX < startX + limit * level.mConfig.mWidth) {
                    sb.draw(pl.mImage, currX, pl.mYOffset, pl.mWidth,
                            pl.mHeight);
                    currX += pl.mImage.getRegionWidth();
                }
            }
            // Figure out what to draw for layers that repeat in the y dimension
            else if (pl.mYRepeat) {
                // getLoseScene the camera center, translate, and scale
                float startY = y * level.mConfig.mPixelMeterRatio * pl.mYSpeed;
                // subtract enough screens, as above
                startY -= (1.5f + mBgCam.zoom)
                        * level.mConfig.mHeight;
                // round
                startY = startY - startY % pl.mImage.getRegionHeight();
                float currY = startY;
                // draw a bunch of repeated images
                float limit = 2 + 2 * mBgCam.zoom;
                while (currY < startY + limit * level.mConfig.mHeight) {
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
