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
        public float zoom;

        public OrthographicCamera(float w, float h)
        {
            width = w;
            height = h;
        }

        public void update()
        {

        }

        /** METHODS FOR DRAWING SPRITES ON THE SCREEN */

        public int dx(float x)
        {
            return (int)((x / width) * Lol.sGame.GraphicsDevice.DisplayMode.Height);
            //return (int)((x / mCamBoundX) * Lol.sGame.mConfig.getScreenWidth());
        }

        public int dy(float y)
        {
            return (int)(((height - y) / height) * Lol.sGame.GraphicsDevice.DisplayMode.Width);
            //return (int)((y / mCamBoundY) * Lol.sGame.mConfig.getScreenHeight());
        }

        public int iy(float y)
        {
            return (int)(Lol.sGame.GraphicsDevice.DisplayMode.Width - y);
        }

        public int dh(float y)
        {
            return (int)((y / height) * Lol.sGame.GraphicsDevice.DisplayMode.Width);
            //return (int)((y / mCamBoundY) * Lol.sGame.mConfig.getScreenHeight());
        }

        public float lx(float x)
        {
            return ((x / Lol.sGame.GraphicsDevice.DisplayMode.Height) * width);
        }

        public float ly(float y)
        {
            return (((Lol.sGame.GraphicsDevice.DisplayMode.Width - y) / Lol.sGame.GraphicsDevice.DisplayMode.Width) * height);
        }
    }
}
