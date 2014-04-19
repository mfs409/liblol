using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LOL
{
    public class ShapeRenderer
    {
        public Color Color = Color.Red;

        public void begin()
        {

        }

        public void rect(int x, int y, int w, int h)
        {
            int bw = 2; // Border width
            Texture2D t = new Texture2D(Lol.sGame.GraphicsDevice, 1, 1);
            t.SetData(new[] { Color.White });
            SpriteBatch spriteBatch = new SpriteBatch(Lol.sGame.GraphicsDevice);
            Rectangle r = new Rectangle(x, y, w, h);
            spriteBatch.Begin();
            spriteBatch.Draw(t, new Rectangle(r.Left, r.Top, bw, r.Height), Color); // Left
            spriteBatch.Draw(t, new Rectangle(r.Right, r.Top, bw, r.Height), Color); // Right
            spriteBatch.Draw(t, new Rectangle(r.Left, r.Top, r.Width, bw), Color); // Top
            spriteBatch.Draw(t, new Rectangle(r.Left, r.Bottom, r.Width, bw), Color); // Bottom
            spriteBatch.End();
            /*VertexPositionColor[] verts = new VertexPositionColor[5];
            verts[0] = new VertexPositionColor(new Vector3(x, y, 0), Color);
            verts[1] = new VertexPositionColor(new Vector3(x+w, y, 0), Color);
            verts[2] = new VertexPositionColor(new Vector3(x+w, y+h, 0), Color);
            verts[3] = new VertexPositionColor(new Vector3(x, y+h, 0), Color);
            verts[4] = new VertexPositionColor(new Vector3(x, y, 0), Color);
            VertexBuffer buf = new VertexBuffer(Lol.sGame.GraphicsDevice, typeof(VertexPositionColor), 5, BufferUsage.WriteOnly);
            buf.SetData<VertexPositionColor>(verts);
            Lol.sGame.GraphicsDevice.SetVertexBuffer(buf);
            Lol.sGame.GraphicsDevice.DrawPrimitives(PrimitiveType.LineList, 0, 1);*/
        }

        public void end()
        {

        }
    }
}
