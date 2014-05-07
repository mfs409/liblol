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

        const int GDX_W = 48;
        const int GDX_H = 32;

        // Chase info
        protected float cxo,cyo;
        protected bool chase = false;

        public OrthographicCamera(float w, float h)
        {
            width = w;
            height = h;
            sw = Lol.sGame.GraphicsDevice.DisplayMode.Height;
            sh = Lol.sGame.GraphicsDevice.DisplayMode.Width;
        }

        public void center(PhysicsSprite o)
        {
            chase = false;
            float center_x = o.mBody.Position.X + (o.mSize.X / 2);
            float center_y = o.mBody.Position.Y + (o.mSize.Y / 2);
            
            // Note lx and ly use cxo and cyo but since they're zeroed out, it returns a neutral value
            cxo=0;
            cyo=0;
            // Convert screen center to level coordinates
            float scx = lx((sw / ZoomToScale()) / 2);
            float scy = ly((sh/ZoomToScale()) / 2);

            // Check if we need to move the camera
            cxo = (center_x - scx) * -1;
            cyo = (center_y - scy) * -1;
        }

        protected float ZoomToScale()
        {
            return 1/zoom;
        }

        public void update()
        {

        }

        public int screenX(float x)
        {
            return (int)(x / width * sw);
        }

        public int screenY(float y)
        {
            return (int)(y / height * sh);
        }

        public float levelX(float x)
        {
            return x / sw * width;
        }

        public float levelY(float y)
        {
            return y / sh * height;
        }

        public float invertLevelY(float y)
        {
            return height-y;
        }

        /** METHODS FOR DRAWING SPRITES ON THE SCREEN */

        public int dx(float x)
        {
            if (chase)
            {
                return (int)(((x+cxo) / width) * sw * ZoomToScale());
            }
            return (int)((x / width) * sw * ZoomToScale());
        }

        public int dy(float y)
        {
            if (chase)
            {
                return (int)(((height - (y+cyo)) / height) * sh * ZoomToScale());
            }
            return (int)(((height - y) / height) * sh * ZoomToScale());
        }

        public int tx(float x)
        {
            return (int)(x -cxo);
        }

        public int ty(float y)
        {
            return (int)(y - cyo);
        }

        public float tlx(float x)
        {
            return lx(tx(x));
        }

        public float tly(float y)
        {
            return ly(ty(y));
        }

        // Invert Y
        public int iy(float y)
        {
            return (int)(sh - y);
        }

        public int dw(float x)
        {
            return (int)((x / width) * sw * ZoomToScale());
        }

        public int dh(float y)
        {
            return (int)((y / height) * sh * ZoomToScale());
        }

        public float lx(float x)
        {
            if (chase)
            {
                return ((x / sw) * width)+cxo;
            }
            return ((x / sw) * width);
        }

        public float ly(float y)
        {
            if (chase)
            {
                return (((sh - y) / sh) * height)+cyo;
            }
            return (((sh - y) / sh) * height);
        }
    }
}
