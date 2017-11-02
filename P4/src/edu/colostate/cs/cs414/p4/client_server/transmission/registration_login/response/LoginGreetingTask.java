package edu.colostate.cs.cs414.p4.client_server.transmission.registration_login.response;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.colostate.cs.cs414.p4.client_server.transmission.Task;
import edu.colostate.cs.cs414.p4.client_server.transmission.TaskConstents;
import edu.colostate.cs.cs414.p4.client_server.transmission.util.ReadUtils;
import edu.colostate.cs.cs414.p4.client_server.transmission.util.WriteUtils;
import edu.colostate.cs.cs414.p4.user.ActivePlayer;
import edu.colostate.cs.cs414.p4.user.Player;

public class LoginGreetingTask extends Task implements EntryResponse {

	private String greeting;
	private String playerNickname;
	
	public LoginGreetingTask(String greeting, String playerNickname) {
		this.greeting = greeting;
		this.playerNickname = playerNickname;
	}
	
	public LoginGreetingTask(DataInputStream din) throws IOException {
		this.greeting = ReadUtils.readString(din);
		this.playerNickname = ReadUtils.readString(din);
	}
	
	@Override
	public int getTaskCode() {
		return TaskConstents.LOGIN_GREETING_TASK;
	}

	@Override
	public byte[] toByteArray() throws IOException {
		ByteArrayOutputStream bs = WriteUtils.getByteOutputStream();
		DataOutputStream dout = WriteUtils.getDataOutputStream(bs);
		dout.writeInt(getTaskCode());
		WriteUtils.writeString(greeting,dout);
		WriteUtils.writeString(playerNickname, dout);
		return WriteUtils.getBytesAndCloseStreams(bs,dout);
	}

	@Override
	public void run() 
	{
		Player player;
		if((player = ActivePlayer.getInstance()) != null) {
			player.setNickName(playerNickname);
			displayMessageToPlayer(player);
		}
	}

	@Override
	public boolean wasSuccessful() {
		return true;
	}

	@Override
	public String getResponseMessage() {
		return greeting;
	}
}
