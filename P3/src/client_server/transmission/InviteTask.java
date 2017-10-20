package client_server.transmission;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import client_server.transmission.util.ReadUtils;
import client_server.transmission.util.WriteUtils;
import user.Invitation;

public class InviteTask extends Task {
	private String message;
	private String playerFrom;
	private ArrayList<String> playersToInvite;
	
	public InviteTask(Invitation invite)
	{
		super();
		this.message = invite.getMessage();
		this.playerFrom = invite.getPlayerFrom();
		this.playersToInvite = invite.getPlayersToInvite();
	}
	
	public InviteTask(DataInputStream din) throws IOException
	{
		this.message = ReadUtils.readString(din);
		this.playerFrom = ReadUtils.readString(din);
		this.playersToInvite = (ArrayList<String>) ReadUtils.readStringList(din);
	}
	
	public int getTaskCode() 
	{
		return TaskConstents.INVITE_TASK;
	}
	
	public byte[] toByteArray() throws IOException 
	{
		ByteArrayOutputStream bs = WriteUtils.getByteOutputStream();
		DataOutputStream dout = WriteUtils.getDataOutputStream(bs);
		dout.writeInt(getTaskCode());
		WriteUtils.writeString(message, dout);
		WriteUtils.writeString(playerFrom, dout);
		WriteUtils.writeStringList(playersToInvite, dout);
		return WriteUtils.getBytesAndCloseStreams(bs,dout);
	}
	
	public void run()
	{
		System.out.println(this.playerFrom + " has invited you to play a game of Banqi.");
		System.out.println('"' + this.message + '"');
		for(String name: this.playersToInvite)
		{
			System.out.println(name);
		}
	}
}