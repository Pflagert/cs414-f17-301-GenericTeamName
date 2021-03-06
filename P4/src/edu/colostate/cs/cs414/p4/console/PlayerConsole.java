package edu.colostate.cs.cs414.p4.console;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import edu.colostate.cs.cs414.p4.client_server.client.AbstractClient;
import edu.colostate.cs.cs414.p4.client_server.transmission.Task;
import edu.colostate.cs.cs414.p4.client_server.transmission.game.invite.GetInvitesTask;
import edu.colostate.cs.cs414.p4.client_server.transmission.game.invite.GetSentInvitesTask;
import edu.colostate.cs.cs414.p4.client_server.transmission.profile.GetProfileTask;
import edu.colostate.cs.cs414.p4.client_server.transmission.registration_login.LoginTask;
import edu.colostate.cs.cs414.p4.client_server.transmission.registration_login.LogoutTask;
import edu.colostate.cs.cs414.p4.client_server.transmission.registration_login.RegisterTask;
import edu.colostate.cs.cs414.p4.client_server.transmission.registration_login.UnregisterTask;
import edu.colostate.cs.cs414.p4.client_server.transmission.util.ForwardTask;
import edu.colostate.cs.cs414.p4.user.ActivePlayer;
import edu.colostate.cs.cs414.p4.user.Player;

public class PlayerConsole extends AbstractConsole {
	private static final String[] noParamCommands = 
		{"exit","help","register","login",
		"logout","unregister","create-game","view-profile",
		"view-invites", "view-sent-invites"};

	private static final int noParamCommandsLength = noParamCommands.length;

	/**
	 * We will store this here to simplify {@link ForwardTask}s
	 */
	private String playerNickName;

	/**
	 * Having this instance within the console should simplify sending {@link Task}s
	 */
	private AbstractClient client;

	private static final String PROMPT_CHARACTOR_SEQUENCE = "> ";

	public PlayerConsole(Player player) {
		if(player == null) {
			throw new IllegalArgumentException("The PlayerConsole requires a true Player");
		}
		this.player = player;
		outPutBeforeConsole = System.lineSeparator()+PROMPT_CHARACTOR_SEQUENCE;
		playerNickName = player.getNickName();
		client = player.getClient();
		nullCommands();
	}

	/**
	 * Sets a few instance variables to null
	 */
	private void nullCommands() {
		noParamCommand = null;
		paramCommand = null;
		errorMessage = null;
	}

	/**
	 * Handle's commands that do NOT require additional input from the user.
	 * @param command - The String that may represent a command that does not require arguments.
	 * @return True if and only if the command does represent a "no parameter command"
	 * Otherwise returns false.
	 */
	private boolean checkCommandsWithoutParam(String command) {
		int i, length, 
		commandLength = command.length();
		for(i=0;i<noParamCommandsLength;i++) {
			if(command.startsWith(noParamCommands[i])) {
				if(commandLength >= (length = noParamCommands[i].length()) && 
						!command.substring(length, commandLength).matches(".*\\w.*")) { // white space after command is OK.
					noParamCommand = noParamCommands[i];
					return true;
				}
				else {
					errorMessage = "Expected: '" + noParamCommands[i] + "'\nReceived: '" + command + "'"; 
					return false;
				}
			}
		}
		return false;
	}


	/**
	 * Overrides {@link AbstractConsole#display}.
	 * Displays messages to a user through System.out.
	 * @param msg - The Object that represents a message that we want to be displayed to the user.
	 */
	@Override
	public synchronized void display(Object msg) {
		if(msg != null)
			output.print("\r" + msg.toString() + outPutBeforeConsole);
	}

	/**
	 * Handles the parsing and validation of the command.
	 * @param command - The command to be parsed and validated.
	 * @return true if and only if the command is a command handled by this class.
	 */
	@Override
	protected boolean acceptCommand(String command) {
		if(checkCommandsWithoutParam(command))
			return true;

		if(errorMessage == null)
			errorMessage = "Unkown Command: " + command;

		return false;
	}

	/**
	 * Implements desired functionality for commands that do NOT require parameters.
	 */
	private void handleCommandWithoutParam() {
		switch(noParamCommand) {
		case "exit":
			exit();
			break;
		case "help":
			help();
			break;
		case "register":
			register();
			break;
		case "login":
			login();
			break;
		case "view-profile":
			viewProfile();
			break;
		case "create-game":
			createGame();
			break;
		case "logout":
			logout();
			break;
		case "unregister":
			unregister();
			break;
		case "view-invites":
			viewInvites();
			break;
		case "view-sent-invites":
			viewSentInvites();
			break;
		}
	}

	/**
	 * Implements desired functionality for commands.
	 */
	@Override
	protected synchronized void handleCommand() {
		if(noParamCommand != null)
			handleCommandWithoutParam();
		nullCommands();
	}

	/**
	 * Determines how to handle incorrect input from the user.
	 */
	@Override
	protected void handleCommandError() {
		error(errorMessage);
		nullCommands();
	}

	/**
	 * Simply calls super class method {@link AbstractConsole#accept()}.
	 */
	@Override
	public void run() {
		accept();		
	}

	@Override
	public int getParam() {
		return 0;
	}

	private boolean requireLogin() {
		if(client.isLoggedIn()) {
			playerNickName = player.getNickName();
			return true;
		} else {
			error("You must be logged in before you can: " + noParamCommand);
			return false;
		}
	}

	private boolean requireLogoff() {
		if(!client.isLoggedIn()) {
			return true;
		} else {
			error("You must be logged off before you can: " + noParamCommand);
			return false;
		}
	}

	private void help() {
		String msg = "type 'help' to see this message.\n\r";
		if(client.isLoggedIn()) {
			msg += "type 'logout' to logout.\n\r" +
					"type 'unregister' to logout and remove your account.\n" +
					"type 'create-game' to create a game.\n" +
					"type 'view-profile' to view a player's profile.\n" +
					"type 'view-invites' to accept or reject pending invitations.\n" +
					"type 'view-sent-invites' to view pending invitations that you sent.\n";
		} else {
			msg += "type 'login' to login to your account.\n" +
					"type 'register' to create a new account.\n";
		}
		msg += "type 'exit' to quit this program.";
		display(msg);
	}

	private void exit() {
		if(client.isLoggedIn())
			logout();
		try {
			Thread.sleep(1000);
			client.disconnectFromServer();
		} catch(Exception e) {

		}
		System.exit(0);
	}

	private void register() {
		if(requireLogoff()) {
			try {
				String email = promptUser("Please enter in a valid Email:");
				String nickName = promptUser("Please enter your desired nickname:");
				String password = promptUser("Please enter your password:");
				ActivePlayer.setInstance(player);
				player.setEmail(email);
				player.setNickName(nickName);
				player.setPassword(password); // encrypts password
				client.sendToServer(new RegisterTask(email,nickName,player.getPassword()));
				playerNickName = nickName;
			} catch (IOException e) {
				error("Error occured while registering.");
			}
		}
	}

	private void login() {
		if(requireLogoff()) {
			try {
				String email = promptUser("Please enter in a valid Email:");
				String password = promptUser("Please enter your password:");
				ActivePlayer.setInstance(player);
				player.setEmail(email);
				player.setPassword(password); // encrypts password
				client.sendToServer(new LoginTask(email,player.getPassword()));
				playerNickName = player.getNickName();
			} catch (IOException e) {
				error("Error occured while logging in.");
			}
		}
	}

	private void createGame() {
		if(requireLogin()) {
			createGameInvites();
		}
	}

	private void createGameInvites() {
		Set<String> toInvite = new HashSet<String>();
		String next = "";
		try {
			do {
				next = promptUser("Enter the nickname name of the player you would like to invite"
						+ " or click enter to send the invititations: ");
				next = next.trim();
				if(!next.isEmpty()) {
					toInvite.add(next);
				} else {
					break;
				}
			} while(true);

			if(toInvite.isEmpty()) {
				warning("You didn't invite anyone.");
			} else {
				String message = promptUser("Type a message you would like to send with your invitation: ");
				player.sendInvitation(message, new ArrayList<String>(toInvite));
			}

		} catch(IOException e) {
			error("Error occured while sending invites.");
		}
	}

	private void viewProfile() {
		if(requireLogin()) {
			try {
				String userOther = promptUser("Enter the nickname of the player's profile you would"
						+ " like to view or click enter to view your own:");
				userOther = userOther.trim();
				if(userOther.isEmpty()) {
					userOther = playerNickName;
				}
				Task getProfile = new GetProfileTask(playerNickName, userOther);
				Task forward = new ForwardTask(playerNickName,getProfile,userOther);
				client.sendToServer(forward);
			} catch (IOException e) {
				error("Error occured while trying to receive a player's profile.");
			}
		}
	}

	private void unregister() {
		try {
			if(requireLogin()) {
				warning("Your account will be deleted if you unregister. Are you sure you want to unregister and remove your account?");
				String response = promptUser("Type 'yes' if you are sure you want to delete your account, otherwise click enter.");
				response = response.toLowerCase();
				if(response.equals("yes")) {
					client.sendToServer(new UnregisterTask(
						player.getEmail(),player.getNickName(),player.getPassword()));
					warning("Your account will be removed.");
					playerNickName = null;
				} else {
					notice("Your account will not removed.");
				}
			}
		}  catch (IOException e) {
			error("Error occured while unregistering.");
		}
	}

	private void logout() {
		if(requireLogin()) {
			try {
				client.sendToServer(new LogoutTask(player.getEmail()));
				playerNickName = null;
			}  catch (IOException e) {
				error("Error occured while logging out.");
			}
		}
	}
	
	private void viewInvites() {
		if(requireLogin()) {
			try {
				client.sendToServer(new GetInvitesTask(playerNickName));
			} catch(IOException e) {
				error("Error occured while retrieving invitations.");
			}
		}
	}
	
	private void viewSentInvites() {
		if(requireLogin()) {
			try {
				client.sendToServer(new GetSentInvitesTask(playerNickName));
			} catch(IOException e) {
				error("Error occured while retrieving invitations that you have sent.");
			}
		}
	}
}
