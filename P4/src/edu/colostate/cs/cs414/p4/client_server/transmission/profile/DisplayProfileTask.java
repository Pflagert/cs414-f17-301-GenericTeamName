package edu.colostate.cs.cs414.p4.client_server.transmission.profile;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import edu.colostate.cs.cs414.p4.client_server.transmission.Task;
import edu.colostate.cs.cs414.p4.client_server.transmission.TaskConstents;
import edu.colostate.cs.cs414.p4.client_server.transmission.util.ReadUtils;
import edu.colostate.cs.cs414.p4.client_server.transmission.util.WriteUtils;
import edu.colostate.cs.cs414.p4.console.AbstractConsole;
import edu.colostate.cs.cs414.p4.user.ActivePlayer;
import edu.colostate.cs.cs414.p4.user.Player;

public class DisplayProfileTask extends Task {

	private String profile;
	
	public DisplayProfileTask(String profile) {
		this.profile = profile;
	}
	
	public DisplayProfileTask(DataInputStream din) throws IOException {
		profile = ReadUtils.readString(din);
	}
	
	@Override
	public int getTaskCode() {
		return TaskConstents.DISPLAY_PROFILE_TASK;
	}
	
	@Override
	public void writeBytes(DataOutputStream dout) throws IOException {
		WriteUtils.writeString(profile, dout);		
	}
	
	public String toString() {
		return "[DisplayProfileTask, Taskcode: " + getTaskCode() + "]";
	}

	@Override
	public void run() {
		Player player = ActivePlayer.getInstance();
		if(player != null) {
			AbstractConsole console = player.getConsole();
			if(console != null) {
				console.notice(profile);
			} else {
				System.out.println(profile);
			}
		}

	}

}
