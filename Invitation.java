import java.util.ArrayList;

public class Invitation {
	/* GLOBAL VARIABLES */
	private String message;
	private ArrayList<Player> playersToInvite;
	
	
	/* Constructor */
	public Invitation(String message, ArrayList<Player> playersToInvite)
	{
		this.message = message;
		this.playersToInvite = playersToInvite;
	}
	
	public void sendInvite()
	{
		for(Player player : this.playersToInvite)
		{
			/* Use server to send invite to players */
			System.out.println(player);
		}
	}
}