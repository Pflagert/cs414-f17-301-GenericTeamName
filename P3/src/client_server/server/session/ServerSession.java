/**
 * 
 */
package client_server.server.session;

import java.nio.channels.SelectionKey;

import client_server.server.AbstractServer;
import client_server.transmission.Task;

/**
 * @author pflagert
 * IGNORE THIS CLASS FOR NOW
 */
public class ServerSession extends AbstractSession{

	public ServerSession(AbstractServer server, SelectionKey key, String ID) {
		super(server, key, ID);
		// TODO Auto-generated constructor stub
	}
	
	public ServerSession(AbstractSession s) {
		this(s.server,s.key,s.ID);
	}

	@Override
	public void receive() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void send(Task t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConnected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRegisteredWithServer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void registerWithServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return false;
	}

}