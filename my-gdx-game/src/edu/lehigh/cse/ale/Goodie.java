package edu.lehigh.cse.ale;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Goodie extends PhysicsSprite
{
    /*
     * BASIC FUNCTIONALITY
     */

    // TODO: this is not valid
    Goodie(TextureRegion tr, SpriteId id, float width, float height)
    {
        super(tr, id, width, height);
        // TODO Auto-generated constructor stub
    }

    /**
     * The "score" of this goodie... it is the amount that will be added to the score when this goodie is collected.
     * 
     * This is different than strength because this actually bumps the score, which in turn lets us have
     * "super goodies" that turn on trigger obstacles.
     * 
     * Note: the right way to do this is to have an array of scores, but we're going to be sloppy for now and just have
     * four separate scores
     */
    int _score1;
    
    /**
     * The second score
     */
    int _score2;
    
    /**
     * The third score
     */
    int _score3;
    
    /**
     * The fourth score
     */
    int _score4;
    
    /**
     * Create a basic goodie. This code should never be called directly. Use addXXX methods instead
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param ttr
     *            Image to display
     */
/*    private Goodie(float x, float y, float width, float height, TiledTextureRegion ttr)
    {
        super(x, y, width, height, ttr, SpriteId.GOODIE);
        // the default is to just have a score1, and nothing else
        _score1 = 1;
        _score2 = 0;
        _score3 = 0;
        _score4 = 0;
    }

    /**
     * Add a simple Goodie who uses a box as its _fixture
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param imgName
     *            Name of image file to use
     * 
     * @return The goodie, so that we can update its properties
     */
  /*  public static Goodie makeAsBox(float x, float y, float width, float height, String imgName)
    {
        Goodie goodie = new Goodie(x, y, width, height, Media.getImage(imgName));
        goodie.setBoxPhysics(0, 0, 0, BodyType.StaticBody, false);
        goodie.setCollisionEffect(false);
        Level._current.attachChild(goodie._sprite);
        return goodie;
    }

    /**
     * Add a simple Goodie who uses a circle as its _fixture
     * 
     * @param x
     *            X coordinate of top left corner
     * @param y
     *            Y coordinate of top left corner
     * @param width
     *            Width of the image
     * @param height
     *            Height of the image
     * @param imgName
     *            Name of image file to use
     * 
     * @return The goodie, so that we can update its properties
     */
    /*public static Goodie makeAsCircle(float x, float y, float width, float height, String imgName)
    {
        Goodie goodie = new Goodie(x, y, width, height, Media.getImage(imgName));
        goodie.setCirclePhysics(0, 0, 0, BodyType.StaticBody, false);
        goodie.setCollisionEffect(false);
        Level._current.attachChild(goodie._sprite);
        return goodie;
    }

    /**
     * Set the score of this goodie. This indicates how many points the goodie is worth... can be positive or negative
     * 
     * @param value
     *            The number of points that are added to the score when the goodie is collected
     * 
     * @deprecated use setScore[1-4]() instead
     */
    @Deprecated
    public void setScore(int value)
    {
        // save this value
        _score1 = value;
    }

    /**
     * Set the score of this goodie. This indicates how many points the goodie is worth... can be positive or negative
     * 
     * @param value
     *            The number of points that are added to score #1 when the goodie is collected
     */
    public void setScore1(int value)
    {
        // save this value
        _score1 = value;
    }

    /**
     * Set the score of this goodie. This indicates how many points the goodie is worth... can be positive or negative
     * 
     * @param value
     *            The number of points that are added to score #2 when the goodie is collected
     */
    public void setScore2(int value)
    {
        // save this value
        _score2 = value;
    }

    /**
     * Set the score of this goodie. This indicates how many points the goodie is worth... can be positive or negative
     * 
     * @param value
     *            The number of points that are added to score #3 when the goodie is collected
     */
    public void setScore3(int value)
    {
        // save this value
        _score3 = value;
    }

    /**
     * Set the score of this goodie. This indicates how many points the goodie is worth... can be positive or negative
     * 
     * @param value
     *            The number of points that are added to score #4 when the goodie is collected
     */
    public void setScore4(int value)
    {
        // save this value
        _score4 = value;
    }

    /*
     * COLLISION SUPPORT
     */

    /**
     * Internal method: Goodie collision is meaningless, so we leave this method blank
     * 
     * @param other
     *            Other object involved in this collision
     */
    void onCollide(PhysicsSprite other)
    {
    }

    /*
     * STRENGTH AND INVINCIBILITY SUPPORT
     */

    /**
     * How much _strength does the hero get by collecting this goodie
     */
    int   _strengthBoost         = 0;

    /**
     * How long will the hero be invincible if it collects this goodie
     */
    float _invincibilityDuration = 0;

    /**
     * Indicate how much _strength the hero gains by collecting this goodie
     * 
     * @param boost
     *            Amount of _strength boost
     */
    public void setStrengthBoost(int boost)
    {
        _strengthBoost = boost;
    }

    /**
     * Indicate how long the hero will be invincible after collecting this goodie
     * 
     * @param duration
     *            duration for invincibility
     */
    public void setInvincibilityDuration(float duration)
    {
        _invincibilityDuration = duration;
    }

}
