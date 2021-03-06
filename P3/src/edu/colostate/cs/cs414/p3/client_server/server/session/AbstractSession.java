/**
 * 
 */
package edu.colostate.cs.cs414.p3.client_server.server.session;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import edu.colostate.cs.cs414.p3.client_server.server.AbstractServer;
import edu.colostate.cs.cs414.p3.client_server.transmission.LoginTask;
import edu.colostate.cs.cs414.p3.client_server.transmission.RegisterTask;
import edu.colostate.cs.cs414.p3.client_server.transmission.Task;

/**
 * @author pflagert
 *
 */
public abstract class AbstractSession {
	protected AbstractServer server;
	
	protected SelectionKey key;
	
	protected String ID;
	
	protected Boolean isRegistered;
	
	public AbstractSession(AbstractServer server, SelectionKey key, String ID) {
		this.server = server;
		this.key = key;
		this.ID = ID;
		isRegistered = false;
	}
	
	/**
	 * @param ID - The ID to represent an instance of AbstractSession
	 */
	public void setID(String ID) {
		this.ID = ID;
	}
	
	/**
	 * @return The ID that represents an instance of AbstractSession
	 */
	public String getID() {
		return ID;
	}
	
	/**
	 * @return The SelectionKey associate with an instance of AbstractSession
	 */
	public SelectionKey getKey() {
		return key;
	}
	
	/**
	 * The receive method constructs a new Task through the TaskFactory
	 * from the bytes received from the client. 
	 * This method then calls server.handleTask() with the newly created task as the parameter.
	 * @throws IOException
	 */
	public abstract void receive() throws IOException;
	
	/**
	 * The send method constructs a byte Array by calling t.toByteArray().
	 * After the byte array is constructed, the bytes are then “sent” to the client 
	 * by writing the bytes to the SocketChannel (associated with the SelectionKey passed
	 * into the constructor).
	 * @param t
	 * @throws IOException
	 */
	public abstract void send(Task t) throws IOException;
	
	/**
	 * The disconnect method disconnects the SocketChannel by calling SocketChannel.close().
	 * Then calls server.clientDisconnected().
	 * Note that when a client is disconnected, they will not be reconnected until the
	 * send a connection request to the server.
	 * After calling disconnect on an instance of this class, 
	 * that instance will no longer be able to communicate to the client.
	 */
	public abstract void disconnect();
	
	/**
	 * The isConnected method returns true if the SocketChannel is able to be written to
	 * or read from (i.e The SocketChannel is open).
	 * Otherwise returns false.
	 */
	public abstract boolean isConnected();
	
	public abstract boolean isRegisteredWithServer();
	
	public abstract void registerWithServer(RegisterTask t);
	
	public abstract void registerWithServer(LoginTask t);
	
	public abstract String toString();
	
	public abstract int hashCode();
	
	public abstract boolean equals(Object o);
	
}
