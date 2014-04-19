using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace LOL
{
    public class Frustum
    {
	    protected static Vector3[] clipSpacePlanePoints = {new Vector3(-1, -1, -1), new Vector3(1, -1, -1),
		    new Vector3(1, 1, -1), new Vector3(-1, 1, -1), // near clip
		    new Vector3(-1, -1, 1), new Vector3(1, -1, 1), new Vector3(1, 1, 1), new Vector3(-1, 1, 1)}; // far clip
	    protected static float[] clipSpacePlanePointsArray = new float[8 * 3];

	    static Frustum() {
		    int j = 0;
		    foreach (Vector3 v in clipSpacePlanePoints) {
			    clipSpacePlanePointsArray[j++] = v.X;
			    clipSpacePlanePointsArray[j++] = v.Y;
			    clipSpacePlanePointsArray[j++] = v.Z;
		    }
	    }

	    /** the six clipping planes, near, far, left, right, top, bottm **/
	    public Plane[] planes = new Plane[6];

	    /** eight points making up the near and far clipping "rectangles". order is counter clockwise, starting at bottom left **/
	    public Vector3[] planePoints = {new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(),
		    new Vector3(), new Vector3(), new Vector3()};
	    protected float[] planePointsArray = new float[8 * 3];

	    public Frustum () {
		    for (int i = 0; i < 6; i++) {
			    planes[i] = new Plane(new Vector3(), 0);
		    }
	    }

	    /** Updates the clipping plane's based on the given inverse combined projection and view matrix, e.g. from an
	     * {@link OrthographicCamera} or {@link PerspectiveCamera}.
	     * @param inverseProjectionView the combined projection and view matrices. */
	    public void update (Matrix inverseProjectionView) {
		    System.Array.Copy(clipSpacePlanePointsArray, 0, planePointsArray, 0, clipSpacePlanePointsArray.Length);
            
		    Matrix4.prj(inverseProjectionView.val, planePointsArray, 0, 8, 3);
		    for (int i = 0, j = 0; i < 8; i++) {
			    Vector3 v = planePoints[i];
			    v.X = planePointsArray[j++];
			    v.Y = planePointsArray[j++];
			    v.Z = planePointsArray[j++];
		    }

		    planes[0] = new Plane(planePoints[1], planePoints[0], planePoints[2]);
            planes[1] = new Plane(planePoints[4], planePoints[5], planePoints[7]);
            planes[2] = new Plane(planePoints[0], planePoints[4], planePoints[3]);
            planes[3] = new Plane(planePoints[5], planePoints[1], planePoints[6]);
            planes[4] = new Plane(planePoints[2], planePoints[3], planePoints[6]);
            planes[5] = new Plane(planePoints[4], planePoints[0], planePoints[1]);
	    }
    }
}
