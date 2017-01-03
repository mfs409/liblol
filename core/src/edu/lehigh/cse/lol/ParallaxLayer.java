package edu.lehigh.cse.lol;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * This object holds the configuration information for a Parallax layer.
 */
class ParallaxLayer {
    /**
     * How fast should this layer scroll in the X dimension
     */
    public final float mXSpeed;

    /**
     * How fast should it scroll in Y
     */
    public final float mYSpeed;

    /**
     * The image to display
     */
    public final TextureRegion mImage;

    /**
     * How much X offset when drawing this (only useful for Y repeat)
     */
    public final float mXOffset;

    /**
     * How much Y offset when drawing this (only useful for X repeat)
     */
    public final float mYOffset;

    /**
     * Loop in X?
     */
    public boolean mXRepeat;

    /**
     * Loop in Y?
     */
    public boolean mYRepeat;

    /**
     * Width of the image
     */
    public float mWidth;

    /**
     * Height of the image
     */
    public float mHeight;

    /**
     * Does the image move in X without concern for the hero location?
     */
    public boolean mAutoX = false;

    /**
     * For tracking previous mAuto movement
     */
    public float mLastX = 0;

    /**
     * Simple constructor... just set the fields
     *
     * @param xSpeed  Speed that the layer moves in the X dimension
     * @param ySpeed  Y speed
     * @param tr      Image to use
     * @param xOffset X offset
     * @param yOffset Y offset
     * @param width   Width of the image
     * @param height  Height of the image
     */
    public ParallaxLayer(float xSpeed, float ySpeed, TextureRegion tr, float xOffset, float yOffset, float width,
                         float height) {
        mXSpeed = xSpeed;
        mYSpeed = ySpeed;
        mImage = tr;
        mXOffset = xOffset;
        mYOffset = yOffset;
        mWidth = width;
        mHeight = height;
    }
}
