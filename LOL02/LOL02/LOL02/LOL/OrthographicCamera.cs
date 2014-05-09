using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;

namespace LOL
{
    public class OrthographicCamera
    {
        public Vector2 Viewport;
        public Vector2 World;
        public Vector2 Center;
        public Vector2 Screen;
        public float Zoom;

        const int GDX_W = 48;
        const int GDX_H = 32;

        // Chase info
        protected float cxo,cyo;

        public OrthographicCamera(float w, float h)
        {
            Zoom = 1;
            World = new Vector2(w, h);  // LevelCoords
            Viewport = new Vector2(GDX_W, GDX_H);   // LevelCoords
            Screen = new Vector2(Lol.sGame.GraphicsDevice.DisplayMode.Height, Lol.sGame.GraphicsDevice.DisplayMode.Width);
            Center = new Vector2(World.X / 2, World.Y / 2);
        }

        protected float ZoomToScale()
        {
            return 1 / Zoom;
        }

        public void chase(PhysicsSprite o)
        {
            setChase(o.mBody.Position.X, o.mBody.Position.Y);
        }

        public void setChase(float x, float y)
        {
            Center = new Vector2(x, y);
        }
        
        public void update()
        {

        }

        // Converts touch inputs to actual screen coordinates based on viewport offsets
        public int touchX(int x)
        {
            return screenX(vpLeft())+x;
        }

        public int touchY(int y)
        {
            return invertScreenY(screenY(vpTop())+y);
        }

        // Convert to screen-based world coordinates
        public int worldX(float x)
        {
            return (int)(x / World.X * Screen.X);
        }

        public int worldY(float y)
        {
            return (int)(y / World.Y * Screen.Y);
        }

        public int screenX(float x)
        {
            return (int)(x / Viewport.X * Screen.X);
        }

        public int screenY(float y)
        {
            return (int)(y / Viewport.Y * Screen.Y);
        }

        public float levelX(float x)
        {
            return x / Screen.X * Viewport.X;
        }

        public float levelY(float y)
        {
            return y / Screen.Y * Viewport.Y;
        }

        public int invertScreenY(float y)
        {
            return (int)(Screen.Y - y);
        }

        public float invertLevelY(float y)
        {
            return World.Y-y;
        }

        // Calculate offsets for viewport
        protected float vpLeft()
        {
            return Math.Min(Math.Max(0, Center.X - (Viewport.X / ZoomToScale() / 2)), World.X-(Viewport.X/ZoomToScale()));
        }

        protected float vpTop()
        {
            return Math.Min(Math.Max(0, Center.Y - (Viewport.Y /ZoomToScale() / 2)), World.Y-(Viewport.Y/ZoomToScale()));
        }

        // Viewport + Center affect this
        public int drawX(float x)
        {
            x -= vpLeft();
            x *= ZoomToScale();
            return (int)(screenX(x));
        }

        // Viewport + Center affect this
        public int drawY(float y)
        {
            y -= vpTop();
            y *= ZoomToScale();
            return invertScreenY(screenY(y));
        }

        // Viewport affects this
        public int drawWidth(float x)
        {
            return (int)(screenX(x) * ZoomToScale());
        }

        // Viewport affects this
        public int drawHeight(float y)
        {
            return (int)(screenY(y)*ZoomToScale());
        }

        /** METHODS FOR DRAWING SPRITES ON THE SCREEN */

        /*public int dx(float x)
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
        }*/
    }
}
