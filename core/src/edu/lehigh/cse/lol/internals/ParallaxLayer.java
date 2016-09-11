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

package edu.lehigh.cse.lol.internals;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * This object holds the configuration information for a Parallax layer.
 */
public class ParallaxLayer {
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