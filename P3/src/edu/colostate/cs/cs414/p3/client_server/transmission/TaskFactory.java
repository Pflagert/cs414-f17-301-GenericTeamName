/**
 * 
 */
package edu.colostate.cs.cs414.p3.client_server.transmission;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import edu.colostate.cs.cs414.p3.client_server.transmission.util.ReadUtils;

/**
 * @author pflagert
 *
 */
public final class TaskFactory {
	
	private static final TaskFactory instance = new TaskFactory();
	
	private TaskFactory() {}
	
	public static TaskFactory getInstance() {
		return instance;
	}
	
	public Task createTask(int taskCode) {
		Task t = null;
		switch(taskCode) {
		case TaskConstents.TASK: 
			
			break;
		case TaskConstents.FORWARD_TASK:
			
			break;
			
		case TaskConstents.REGISTER_TASK:
			
			break;
		}
		return t;
	}
	
	public Task createTaskFromBytes(byte[] bytes) throws IOException {
		ByteArrayInputStream ba = ReadUtils.getByteInputStream(bytes);
		DataInputStream din = ReadUtils.getDataInputStream(ba);
		Task t = createTaskFromDataInputStream(din);
		ReadUtils.closeInputStreams(ba, din);
		return t;
	}
	
	public Task createTaskFromDataInputStream(DataInputStream din) throws IOException {
		Task t = null;
		
		int taskCode = din.readInt();
		switch(taskCode) {
		case TaskConstents.TASK: 
			break;
		case TaskConstents.FORWARD_TASK:
			t = new ForwardTask(din);
			break;
		case TaskConstents.MULTI_FORWARD_TASK:
			t = new MultiForwardTask(din);
			break;
		case TaskConstents.LOGIN_TASK:
			t = new LoginTask(din);
			break;
		case TaskConstents.REGISTER_TASK:
			t = new RegisterTask(din);
			break;
		case TaskConstents.MESSAGE_TASK:
			t = new MessageTask(din);
			break;
			
		case TaskConstents.INVITE_TASK:
			t = new InviteTask(din);
			break;
			
		case TaskConstents.CREATGAME_TASK:
			t = new CreateGameTask(din);
			break;
		case TaskConstents.ACCEPT_INVITE_TASK:
			t = new AcceptInviteTask(din);
			break;			
		case TaskConstents.REJECTINVITE_TASK:
			t = new RejectInviteTask(din);
			break;		
		case TaskConstents.MOVE_TASK:
			t = new MoveTask(din);
			break;
		case TaskConstents.FLIP_PIECE_TASK:
			t = new FlipPieceTask(din);
			break;
		case TaskConstents.FORFEIT_TASK:
			t = new ForfeitTask(din);
			break;
		case TaskConstents.UPDATERECORD_TASK:
			t = new UpdateRecordTask(din);
			break;
		case TaskConstents.LOGOUT_TASK:
			t = new LogoutTask(din);
			break;
		case TaskConstents.UNREGISTER_TASK:
			t = new UnregisterTask(din);
			break;
		case TaskConstents.GET_PROFILE_TASK:
			t = new GetProfileTask(din);
			break;
		case TaskConstents.DISPLAY_PROFILE_TASK:
			t = new DisplayProfileTask(din);
			break;
		case TaskConstents.LOGIN_GREETING_TASK:
			t = new LoginGreetingTask(din);
			break;
		case TaskConstents.REGISTER_GREETING_TASK:
			t = new RegisterGreetingTask(din);
			break;
    }
		
		return t;
	}
}
