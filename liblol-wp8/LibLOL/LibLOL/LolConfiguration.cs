using System;

namespace LibLOL
{
    public interface LolConfiguration
    {
        // Like Java, interface methods are automatically public
        int GetScreenWidth();

        int GetScreenHeight();

        int GetNumLevels();

        int GetNumHelpScenes();

        bool GetVibration();

        bool GetUnlockMode();

        bool ShowDebugBoxes();

        String GetStorageKey();

        String GetDefaultFontFace();

        int GetDefaultFontSize();

        int GetDefaultFontRed();

        int GetDefaultFontGreen();

        int GetDefaultFontBlue();

        String GetDefaultWinText();

        String GetDefaultLoseText();

        String GetGameTitle();
    }
}
