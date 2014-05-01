using System;

using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Graphics;

namespace LibLOL
{
    internal class AnonRender : Renderable
    {
        internal delegate void UpdateDelegate(GameTime gt);

        internal delegate void DrawDelegate(SpriteBatch sb, GameTime gt);

        private UpdateDelegate ud;

        private DrawDelegate dd;

        internal override void Update(GameTime gameTime)
        {
            ud(gameTime);
        }

        internal override void Draw(SpriteBatch spriteBatch, GameTime gameTime)
        {
            dd(spriteBatch, gameTime);
        }

        internal AnonRender(UpdateDelegate udel, DrawDelegate ddel)
        {
            ud = (udel != null ? udel : (gt) => { });
            dd = (ddel != null ? ddel : (sb, gt) => { });
        }
    }
}
