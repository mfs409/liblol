package com.me.mylolgame;

import edu.lehigh.cse.lol.SplashConfiguration;

// TODO: extend SplashConfiguration to handle font name/color/size, and the actual text

// TODO: then extend this so that we can query to get width and height via queries to SplashConfiguration

public class SplashConfig implements SplashConfiguration
{
    @Override
    public String getBackgroundImage()
    {
        return "splash.png";
    }

    @Override
    public String getMusic()
    {
        return "tune.ogg";
    }

    public int getPlayX()
    {
        return 82;
    }

    public int getPlayY()
    {
        return 150;
    }

    public int getPlayWidth()
    {
        return 75;
    }

    public int getPlayHeight()
    {
        return 25;
    }

    public int getHelpX()
    {
        return 284;
    }

    public int getHelpY()
    {
        return 140;
    }

    public int getHelpWidth()
    {
        return 70;
    }

    public int getHelpHeight()
    {
        return 25;
    }

    public int getQuitX()
    {
        return 163;
    }

    public int getQuitY()
    {
        return 89;
    }

    public int getQuitWidth()
    {
        return 75;
    }

    public int getQuitHeight()
    {
        return 25;
    }

}
