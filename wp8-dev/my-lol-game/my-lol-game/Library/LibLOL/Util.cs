using System;

namespace LibLOL
{
    public static class Util
    {
        private static Random sGenerator = new Random();

        public static int GetRandom(int max)
        {
            return sGenerator.Next(max);
        }
    }
}
