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

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

/**
 * A custom camera that can supports parallax... this code is based on code
 * taken from the GDX tests
 */
public class ParallaxCamera extends OrthographicCamera {
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
    public ParallaxCamera(float viewportWidth, float viewportHeight) {
        super(viewportWidth, viewportHeight);
    }

    /**
     * This is how we calculate the position of a parallax camera
     */
    public Matrix4 calculateParallaxMatrix(float parallaxX, float parallaxY) {
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
