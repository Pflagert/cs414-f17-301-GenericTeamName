package edu.colostate.cs.cs414.p5.console;
import edu.colostate.cs.cs414.p5.client_server.client.AbstractClient;
import edu.colostate.cs.cs414.p5.console.command.CommandController;
import edu.colostate.cs.cs414.p5.user.Player;
import edu.colostate.cs.cs414.p5.user.PlayerController;

public class PlayerConsole extends AbstractConsole {

	private static final String PROMPT_CHARACTOR_SEQUENCE = "> ";

	private final CommandController controller;

	public PlayerConsole(AbstractClient client, Player player) {
		super();
		outPutBeforeConsole = System.lineSeparator()+PROMPT_CHARACTOR_SEQUENCE;
		this.controller = new CommandController(this,new PlayerController(player,client));
	}

	/**
	 * Overrides {@link AbstractConsole#display}.
	 * Displays messages to a user through System.out.
	 * @param msg - The Object that represents a message that we want to be displayed to the user.
	 */
	@Override
	public synchronized void display(Object msg) {
		synchronized(output) {
			if(msg != null)
				output.print("\r" + msg.toString() + outPutBeforeConsole);
			needsPrompt = false;
		}
	}

	@Override
	public synchronized void displayNoPrompt(Object msg) {
		synchronized(output) {
			if(msg != null)
				output.println("\r" + msg);
			needsPrompt = true;
		}
	}

	/**
	 * Implements desired functionality for commands.
	 */
	@Override
	protected synchronized void handleCommand(String command) {
		if(command != null && !command.isEmpty()) {
			controller.handleCommand(command);
		}			
	}


	public void clear() {
		synchronized(output) {
			output.print("\033[H\033[2J");
			output.flush();
			needsPrompt = true;
		}
	}


	@Override
	public void run() {
		super.accept();		
	}
}
