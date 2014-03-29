using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.Xna.Framework;
using Microsoft.Xna.Framework.Media;
using FarseerPhysics.Dynamics;

namespace LibLOL
{
    public class Level : DrawableGameComponent
    {
        private Song music;

        private bool musicIsPlaying;

        // Score score = new Score();

        // Tilt tilt = new Tilt();


        World world;

        public Level(Game game) : base(game)
        {

        }
    }
}
