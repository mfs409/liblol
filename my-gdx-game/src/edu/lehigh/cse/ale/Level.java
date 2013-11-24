package edu.lehigh.cse.ale;

public class Level {
	
    /**
     * these are the ways you can complete a level: you can reach the destination, you can collect enough stuff, or you
     * can get the number of enemies down to 0
     */
    enum VictoryType {
        DESTINATION, GOODIECOUNT, ENEMYCOUNT
    };

    
	static GameLevel _current;
	
	/**
	 * Create a new empty level, and set its camera
	 * 
	 * @param width width of the camera
	 * @param height height of the camera
	 */
	public static void configure(int width, int height)
	{
		_current = new GameLevel(width, height);
	}
	
    /**
     * Describes how a level is won
     */
    static VictoryType _victoryType;

    /**
     * Supporting data for VictoryType
     * 
     * This is the number of heroes who must reach destinations
     */
    static int         _victoryHeroCount;

    /**
     * Indicate that the level is won by having a certain number of _heroes reach destinations
     * 
     * @param howMany
     *            Number of _heroes that must reach destinations
     */
    static public void setVictoryDestination(int howMany)
    {
        _victoryType = VictoryType.DESTINATION;
        _victoryHeroCount = howMany;
    }

}
