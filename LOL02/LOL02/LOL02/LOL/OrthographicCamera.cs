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
            chase = true;
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
