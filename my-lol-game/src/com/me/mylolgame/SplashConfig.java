package com.me.mylolgame;

import edu.lehigh.cse.ale.SplashConfiguration;

// TODO: extend SplashConfiguration to handle font name/color/size, and the actual text

// TODO: then extend this so that we can query to get width and height via queries to SplashConfiguration

public class SplashConfig extends SplashConfiguration
{

    public String getTitle()
    {
        return "ALE Demo Game";
    }

    public String getPlayButtonText()
    {
        return "Play";
    }

    public String getHelpButtonText()
    {
        return "Help";
    }

    public String getQuitButtonText()
    {
        return "Quit";
    }

    public int getPlayX()
    {
        return 213;
    }

    public int getPlayY()
    {
        return 241;
    }

    public int getPlayWidth()
    {
        return 56;
    }

    public int getPlayHeight()
    {
        return 22;
    }

    public int getHelpX()
    {
        return 211;
    }

    public int getHelpY()
    {
        return 189;
    }

    public int getHelpWidth()
    {
        return 58;
    }

    public int getHelpHeight()
    {
        return 22;
    }

    public int getQuitX()
    {
        return 213;
    }

    public int getQuitY()
    {
        return 137;
    }

    public int getQuitWidth()
    {
        return 45;
    }

    public int getQuitHeight()
    {
        return 22;
    }

    public int getTitleX()
    {
        return 157;
    }

    public int getTitleY()
    {
        return 293;
    }

    public int getTitleWidth()
    {
        return 166;
    }

    public int getTitleHeight()
    {
        return 22;
    }
}
