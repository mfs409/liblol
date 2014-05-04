using System;

using Microsoft.Xna.Framework.Media;

namespace LibLOL
{
    internal class Music
    {
        private Song mSong;
        private bool mLoop;

        internal Song Song
        {
            get { return mSong; }
        }

        internal bool Loop
        {
            get { return mLoop; }
        }

        internal Music(Song s, bool loop)
        {
            mSong = s;
            mLoop = loop;
        }

        internal void Play()
        {
            MediaPlayer.IsRepeating = true;
            MediaPlayer.Play(mSong);
        }

        internal void Pause()
        {
            MediaPlayer.Pause();
        }

        internal void Stop()
        {
            MediaPlayer.Stop();
        }
    }
}
