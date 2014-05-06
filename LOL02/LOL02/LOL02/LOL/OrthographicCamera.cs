using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace LOL
{
    public class OrthographicCamera
    {
        public float width;
        public float height;
        public float zoom = 1;
        protected int sw;
        protected int sh;

        public OrthographicCamera(float w, float h)
        {
            width = w;
            height = h;
            sw = Lol.sGame.GraphicsDevice.DisplayMode.Height;
            sh = Lol.sGame.GraphicsDevice.DisplayMode.Width;
        }

        protected float ZoomToScale()
        {
            return 1/zoom;
        }

        public void update()
        {

        }

        /** METHODS FOR DRAWING SPRITES ON THE SCREEN */

        public int dx(float x)
        {
            return (int)((x / width) * sw * ZoomToScale());
            //return (int)((x / mCamBoundX) * Lol.sGame.mConfig.getScreenWidth());
        }

        public int dy(float y)
        {
            return (int)(((height - y) / height) * sh * ZoomToScale());
            //return (int)((y / mCamBoundY) * Lol.sGame.mConfig.getScreenHeight());
        }

        public int iy(float y)
        {
            return (int)(sh - y);
        }

        public int dh(float y)
        {
            return (int)((y / height) * sh * ZoomToScale());
            //return (int)((y / mCamBoundY) * Lol.sGame.mConfig.getScreenHeight());
        }

        public float lx(float x)
        {
            return ((x / sw) * width);
        }

        public float ly(float y)
        {
            return (((Lol.sGame.GraphicsDevice.DisplayMode.Width - y) / sh) * height);
        }
    }
}
