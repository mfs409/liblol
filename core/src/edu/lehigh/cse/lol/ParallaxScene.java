package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;

/**
 * ParallaxScenes present a set of images that seem to scroll relative to the position of the
 * actor on whom the camera is centered.
 */
class ParallaxScene {
    /// The game-wide configuration object
    private final Config mConfig;
    /// All the layers to show as part of this scene
    final ArrayList<ParallaxLayer> mLayers = new ArrayList<>();
    /// An optional background color to show behind everything
    Color mColor = new Color(1, 1, 1, 1);
    /// This camera is for drawing parallax backgrounds that go in front of or behind the world
    ParallaxCamera mBgCam;

    ParallaxScene(Config config) {
        mConfig = config;
        // the background camera is like the hudcam
        mBgCam = new ParallaxCamera(mConfig.mWidth, mConfig.mHeight);
        mBgCam.position.set(mConfig.mWidth / 2, mConfig.mHeight / 2, 0);
        mBgCam.zoom = 1;
    }

    /**
     * This method, called from the render loop, is responsible for drawing all of the layers
     *
     * @param sb The SpriteBatch that is being used to do the drawing.
     */
    void renderLayers(float worldCenterX, float worldCenterY, SpriteBatch sb, float elapsed) {
        // center camera on mCamera's camera
        mBgCam.position.set(worldCenterX, worldCenterY, 0);
        mBgCam.update();

        // draw the layers
        for (ParallaxLayer pl : mLayers) {
            // each layer has a different projection, based on its speed
            sb.setProjectionMatrix(mBgCam.calculateParallaxMatrix(pl.mXSpeed
                    * mConfig.mPixelMeterRatio, pl.mYSpeed
                    * mConfig.mPixelMeterRatio));
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
                if (pl.mLastX > mConfig.mWidth)
                    pl.mLastX = 0;
                if (pl.mLastX < -mConfig.mWidth)
                    pl.mLastX = 0;
                // figure out the starting point for drawing
                float startPoint = pl.mLastX;
                while (startPoint > -mConfig.mWidth)
                    startPoint -= pl.mWidth;
                // start drawing
                while (startPoint < mConfig.mWidth) {
                    sb.draw(pl.mImage, startPoint, pl.mYOffset, pl.mWidth,
                            pl.mHeight);
                    startPoint += pl.mWidth;
                }
            }
            // Figure out what to draw for layers that repeat in the x dimension
            else if (pl.mXRepeat) {
                // getLoseScene the camera center, translate to pixels, and scale by
                // speed
                float startX = worldCenterX * mConfig.mPixelMeterRatio * pl.mXSpeed;
                // subtract one and a half screens worth of repeated pictures
                float screensBefore = 2.5f;
                // adjust by zoom... for every level of zoom, we need that much
                // more beforehand
                screensBefore += mBgCam.zoom;
                startX -= (screensBefore * mConfig.mWidth);
                // round down to nearest screen width
                startX = startX - startX % pl.mImage.getRegionWidth();
                float currX = startX;
                // draw picture repeatedly until we've drawn enough to cover the
                // screen. "enough" can be approximated as 2 screens plus twice
                // the zoom factor
                float limit = 2 + 2 * mBgCam.zoom;
                while (currX < startX + limit * mConfig.mWidth) {
                    sb.draw(pl.mImage, currX, pl.mYOffset, pl.mWidth,
                            pl.mHeight);
                    currX += pl.mImage.getRegionWidth();
                }
            }
            // Figure out what to draw for layers that repeat in the y dimension
            else if (pl.mYRepeat) {
                // getLoseScene the camera center, translate, and scale
                float startY = worldCenterY * mConfig.mPixelMeterRatio * pl.mYSpeed;
                // subtract enough screens, as above
                startY -= (1.5f + mBgCam.zoom)
                        * mConfig.mHeight;
                // round
                startY = startY - startY % pl.mImage.getRegionHeight();
                float currY = startY;
                // draw a bunch of repeated images
                float limit = 2 + 2 * mBgCam.zoom;
                while (currY < startY + limit * mConfig.mHeight) {
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

    /**
     * A custom camera that can supports parallax... this code is based on code
     * taken from the GDX tests
     */
    class ParallaxCamera extends OrthographicCamera {
        /**
         * This matrix helps us compute the view
         */
        private final Matrix4 parallaxView = new Matrix4();

        /**
         * This matrix helps us compute the camera.combined
         */
        private final Matrix4 parallaxCombined = new Matrix4();

        /**
         * A temporary vector for doing the calculations
         */
        private final Vector3 tmp = new Vector3();

        /**
         * Another temporary vector for doing the calculations
         */
        private final Vector3 tmp2 = new Vector3();

        /**
         * The constructor simply forwards to the OrthographicCamera constructor
         *
         * @param viewportWidth  Width of the camera
         * @param viewportHeight Height of the camera
         */
        ParallaxCamera(float viewportWidth, float viewportHeight) {
            super(viewportWidth, viewportHeight);
        }

        /**
         * This is how we calculate the position of a parallax camera
         */
        Matrix4 calculateParallaxMatrix(float parallaxX, float parallaxY) {
            update();
            tmp.set(position);
            tmp.x *= parallaxX;
            tmp.y *= parallaxY;

            parallaxView.setToLookAt(tmp, tmp2.set(tmp).add(direction), up);
            parallaxCombined.set(projection);
            Matrix4.mul(parallaxCombined.val, parallaxView.val);
            return parallaxCombined;
        }
    }
}
