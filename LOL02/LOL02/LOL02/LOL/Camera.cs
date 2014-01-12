using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LOL
{
    public abstract class Camera
    {
	    /** the position of the camera **/
	    public Vector3 position = new Vector3();
	    /** the unit length direction vector of the camera **/
	    public Vector3 direction = new Vector3(0, 0, -1);
	    /** the unit length up vector of the camera **/
	    public Vector3 up = new Vector3(0, 1, 0);

	    /** the projection matrix **/
	    public Matrix projection = new Matrix();
	    /** the view matrix **/
	    public Matrix view = new Matrix();
	    /** the combined projection and view matrix **/
	    public Matrix combined = new Matrix();
	    /** the inverse combined projection and view matrix **/
	    public Matrix invProjectionView = new Matrix();

	    /** the near clipping plane distance, has to be positive **/
	    public float near = 1;
	    /** the far clipping plane distance, has to be positive **/
	    public float far = 100;

	    /** the viewport width **/
	    public float viewportWidth = 0;
	    /** the viewport height **/
	    public float viewportHeight = 0;

	    /** the frustum **/
	    public Frustum frustum = new Frustum();

	    private Vector3 tmpVec = new Vector3();

	    /** Recalculates the projection and view matrix of this camera and the {@link Frustum} planes. Use this after you've manipulated
	     * any of the attributes of the camera. */
	    public abstract void update ();

	    /** Recalculates the projection and view matrix of this camera and the {@link Frustum} planes if <code>updateFrustum</code> is
	     * true. Use this after you've manipulated any of the attributes of the camera. */
	    public abstract void update (bool updateFrustum);

	    /** Recalculates the direction of the camera to look at the point (x, y, z).
	     * @param x the x-coordinate of the point to look at
	     * @param y the x-coordinate of the point to look at
	     * @param z the x-coordinate of the point to look at */
	    public void lookAt (float x, float y, float z) {
		    direction = new Vector3(x, y, z);
            direction -= position;
            direction.Normalize();
		    normalizeUp();
	    }

	    /** Recalculates the direction of the camera to look at the point (x, y, z).
	     * @param target the point to look at */
	    public void lookAt (Vector3 target) {
		    direction = target;
            direction -= position;
            direction.Normalize();
		    normalizeUp();
	    }

	    /** Normalizes the up vector by first calculating the right vector via a cross product between direction and up, and then
	     * recalculating the up vector via a cross product between right and direction. */
	    public void normalizeUp () {
		    tmpVec = direction;
            tmpVec = Microsoft.Xna.Framework.Vector3.Cross(tmpVec, up);
            tmpVec.Normalize();
		    up = tmpVec;
            up = Microsoft.Xna.Framework.Vector3.Cross(up, direction);
            up.Normalize();
	    }


	    /** Moves the camera by the given vector.
	     * @param vec the displacement vector */
	    public void translate (Vector3 vec) {
		    position += vec;
	    }

	    /** Function to translate a point given in window (or window) coordinates to world space. It's the same as GLU gluUnProject, but
	     * does not rely on OpenGL. The x- and y-coordinate of vec are assumed to be in window coordinates (origin is the top left
	     * corner, y pointing down, x pointing to the right) as reported by the touch methods in {@link Input}. A z-coordinate of 0
	     * will return a point on the near plane, a z-coordinate of 1 will return a point on the far plane. This method allows you to
	     * specify the viewport position and dimensions in the coordinate system expected by
	     * {@link GLCommon#glViewport(int, int, int, int)}, with the origin in the bottom left corner of the screen.
	     * 
	     * @param vec the point in window coordinates (origin top left)
	     * @param viewportX the coordinate of the top left corner of the viewport in glViewport coordinates (origin bottom left)
	     * @param viewportY the coordinate of the top left corner of the viewport in glViewport coordinates (origin bottom left)
	     * @param viewportWidth the width of the viewport in pixels
	     * @param viewportHeight the height of the viewport in pixels */
	    public void unproject (Vector3 vec, float viewportX, float viewportY, float viewportWidth, float viewportHeight) {
            float x = vec.X, y = vec.Y;
            x = x - viewportX;
            y = Lol.GD.DisplayMode.Height - y - 1;
            y = y - viewportY;
            vec.X = (2 * x) / viewportWidth - 1;
            vec.Y = (2 * y) / viewportHeight - 1;
            vec.Z = 2 * vec.Z - 1;
            float l_w = 1f / (vec.X * invProjectionView.M41 + vec.Y * invProjectionView.M42 + vec.Z * invProjectionView.M43 + invProjectionView.M44);
            float vX = vec.X, vY = vec.Y, vZ = vec.Z;
            vec.X = (vX * invProjectionView.M11 + vY * invProjectionView.M12 + vZ * invProjectionView.M13 + invProjectionView.M14) * l_w;
            vec.Y = (vX * invProjectionView.M21 + vY * invProjectionView.M22 + vZ * invProjectionView.M23 + invProjectionView.M24) * l_w;
            vec.Z = (vX * invProjectionView.M31 + vY * invProjectionView.M32 + vZ * invProjectionView.M33 + invProjectionView.M34) * l_w;
	    }

	    /** Function to translate a point given in window (or window) coordinates to world space. It's the same as GLU gluUnProject but
	     * does not rely on OpenGL. The viewport is assumed to span the whole screen and is fetched from {@link Graphics#getWidth()}
	     * and {@link Graphics#getHeight()}. The x- and y-coordinate of vec are assumed to be in window coordinates (origin is the top
	     * left corner, y pointing down, x pointing to the right) as reported by the touch methods in {@link Input}. A z-coordinate of
	     * 0 will return a point on the near plane, a z-coordinate of 1 will return a point on the far plane.
	     * 
	     * @param vec the point in window coordinates */
	    public void unproject (ref Vector3 vec) {
		    unproject(vec, 0f, 0f, (float) Lol.GD.DisplayMode.Width, (float) Lol.GD.DisplayMode.Height);
	    }
    }
}
