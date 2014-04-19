using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Xna.Framework.Media;

namespace LOL
{
    public class Music
    {
        public Song Song;
        public bool Loop;

        public Music(Song s, bool loop)
        {
            Song = s;
            Loop = loop;
        }

        public void play()
        {
            MediaPlayer.IsRepeating = Loop;
            MediaPlayer.Play(Song);
        }

        public void pause()
        {
            MediaPlayer.Pause();
        }

        public void stop()
        {
            MediaPlayer.Stop();
        }
    }
}
