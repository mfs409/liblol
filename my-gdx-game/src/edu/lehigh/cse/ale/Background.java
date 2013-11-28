package edu.lehigh.cse.ale;

public class Background
{
    /**
     * Background image for this level. It is "parallax", which means it can scroll slower than the motion of the game.
     */
    //static ParallaxBackground         _background;

    /**
     * If we are doing a vertical scrolling _background, we will use this instead of the other _background field
     */
    //static VerticalParallaxBackground _vertBackground;

    /**
     * Scrolling rate of the backgrounds
     */
    static float                              _backgroundScrollFactor = 1;

    /**
     * Set the background color for this level
     * 
     * @param red
     *            Red portion of _background color
     * @param green
     *            Green portion of _background color
     * @param blue
     *            Blue portion of _background color
     */
    /*
    static public void setColor(float red, float green, float blue)
    {
        if (_background == null) {
            // configure the _background based on the colors provided
            _background = new ParallaxBackground(red / 255, green / 255, blue / 255);
            Level._current.setBackground(_background);
            _background.setParallaxValue(0);
        }
    }
    */

    /**
     * Attach a background layer to this scene
     * 
     * @param imgName
     *            Name of the image file to display
     * @param factor
     *            scrolling factor for this layer. 0 means "dont' move". Negative value matches left-to-right scrolling,
     *            with larger values moving faster.
     * @param x
     *            Starting x coordinate of top left corner
     * @param y
     *            Starting y coordinate of top left corner
     */
    /*
    public static void addLayer(String imgName, float factor, int x, int y)
    {
        TiledTextureRegion ttr = Media.getImage(imgName);
        if (_background == null) {
            // we'll configure the _background as black
            _background = new ParallaxBackground(0, 0, 0);
            Level._current.setBackground(_background);
        }
        _background.attachParallaxEntity(new ParallaxEntity(factor, new AnimatedSprite(x, y, ttr, ALE._self
                .getVertexBufferObjectManager())));
    }
    */

    /**
     * Set the background color for this level if it scrolls vertically
     * 
     * Note: mixing vertical and horizontal backgrounds is a recipe for madness, but it's not something we're going to
     * worry about preventing...
     * 
     * @param red
     *            Red portion of _background color
     * @param green
     *            Green portion of _background color
     * @param blue
     *            Blue portion of _background color
     */
    /*
    {
        if (_vertBackground == null) {
            // configure the _background based on the colors provided
            _vertBackground = new VerticalParallaxBackground(red / 255, green / 255, blue / 255);
            Level._current.setBackground(_vertBackground);
            _vertBackground.setParallaxValue(0);
        }
    }
    */
    
    /**
     * Attach a vertical background layer to this scene
     * 
     * @param imgName
     *            Name of the image file to display
     * @param factor
     *            scrolling factor for this layer. 0 means "dont' move". Negative value matches left-to-right scrolling,
     *            with larger values moving faster.
     * @param x
     *            Starting x coordinate of top left corner
     * @param y
     *            Starting y coordinate of top left corner
     */
    /*
    public static void addLayerVertical(String imgName, float factor, int x, int y)
    {
        TiledTextureRegion ttr = Media.getImage(imgName);
        if (_vertBackground == null) {
            // we'll configure the _background as black
            _vertBackground = new VerticalParallaxBackground(0, 0, 0);
            Level._current.setBackground(_vertBackground);
        }
        _vertBackground.attachVerticalParallaxEntity(new VerticalParallaxBackground.VerticalParallaxEntity(factor,
                new AnimatedSprite(x, y, ttr, ALE._self.getVertexBufferObjectManager())));
    }
    */
    
    /**
     * Set the rate at which the background scrolls
     * 
     * @param rate
     *            The new value to use. When in doubt, 20 is pretty good
     */
    static public void setScrollFactor(float rate)
    {
        _backgroundScrollFactor = rate;
    }
}
