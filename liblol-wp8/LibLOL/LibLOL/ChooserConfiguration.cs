using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace LibLOL
{
    public interface ChooserConfiguration
    {
        int GetRows();

        int GetColumns();

        int GetTopMargin();

        int GetLeftMargin();

        int GetHPadding();

        int GetBPadding();

        String GetLevelButtonName();

        int GetLevelButtonWidth();

        int GetLevelButtonHeight();

        String GetLevelFont();

        int GetLevelFontSize();

        int GetLevelFontRed();

        int GetLevelFontGreen();

        int GetLevelFontBlue();

        String GetLevelLockFont();

        String GetMusicName();

        String GetBackgroundName();

        String GetBackButtonName();

        int GetBackButtonX();

        int GetBackButtonY();

        int GetBackButtonWidth();

        int GetBackButtonHeight();

        String GetPrevButtonName();

        int GetPrevButtonX();

        int GetPrevButtonY();

        int GetPrevButtonWidth();

        int GetPrevButtonHeight();

        String GetNextButtonName();

        int GetNextButtonX();

        int GetNextButtonY();

        int GetNextButtonWidth();

        int GetNextButtonHeight();
    }
}
