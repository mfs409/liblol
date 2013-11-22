package edu.lehigh.cse.ale;

public class Level {
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
}
