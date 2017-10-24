/**
 * 
 */
package client_server.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import client_server.transmission.ForwardTask;
import client_server.transmission.LoginTask;
import client_server.transmission.LogoutTask;
import client_server.transmission.MessageTask;
import client_server.transmission.RegisterTask;
import client_server.transmission.Task;
import client_server.transmission.TaskConstents;
import client_server.transmission.TaskFactory;

/**
 * @author pflagert
 * The Client class extends AbstractClient and provides a default implementation of 
 * methods inherited from AbstractClient.
 */
public class Client extends AbstractClient {

	/*
	 * Time out variables for "select" operations Milliseconds
	 */
	public static final int SELECT_TIMEOUT = 500;

	/* Thread Pool Initialization variables */
	public static final int CORE_THREAD_POOL_SIZE = 4;
	public static final int MAX_THREAD_POOL_SIZE = 8;
	public static final int THREAD_KEEP_ALIVE_TIME = 10;
	public static final TimeUnit ALIVE_TIME_UNIT = TimeUnit.MINUTES;
	
	/**
	 * A thread pool to manage tasks
	 */
	private ThreadPoolExecutor threadPool;
	
	private SocketChannel serverChannel;

	private Boolean isReceiving;
	private Object writeLock;
	private Object readLock;
	private Thread receivingThread;
	private Boolean isLoggedIn;

	/**
	 * Creates a new instance of Client.
	 * @throws IOException 
	 */
	public Client() throws IOException {
		super();
		selector = Selector.open();
		channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.register(selector, SelectionKey.OP_CONNECT);
		isReceiving = false;
		serverChannel = null;
		writeLock = new Object();
		readLock = new Object();
		receivingThread = null;
		isLoggedIn = false;
		initThreadPool();
	}

	/**
	 * Creates a new instance of client and connects the Client to a Server with
	 * the associated InetSocketAddress.
	 * Note that this is equivalent to:
	 * Client c = new Client();
	 * c.connectToServer(address);
	 * @param address - The {@link IndetSocketAddress} that represents the server's address
	 * @throws IOException 
	 */
	public Client(InetSocketAddress address) throws IOException {
		this();
		channel.connect(address);
		connect();
	}

	/**
	 * Creates a new instance of client and connects the Client to a Server
	 * with the associated address and port number.
	 * Note that this is equivalent to:
	 * Client c = new Client();
	 * c.connectToServer(address, port);
	 * @param address - The address of the server connecting too.
	 * @param port - The port number the server is listening on.
	 * @throws IOException 
	 */
	public Client(String address, int port) throws IOException {
		this(new InetSocketAddress(address,port));
	}
	
	private void initThreadPool() {
		threadPool = new ThreadPoolExecutor(CORE_THREAD_POOL_SIZE,
				MAX_THREAD_POOL_SIZE,
				THREAD_KEEP_ALIVE_TIME,
				ALIVE_TIME_UNIT,
				new LinkedBlockingQueue<Runnable>());
		threadPool.prestartCoreThread();
	}

	@Override
	public void connectToServer(InetSocketAddress serverAddress) throws IOException {
		synchronized(writeLock) {
			synchronized(readLock) {

				if(isConnected()) {
					disconnectFromServer();
				}
				unsetLoggedIn();
				channel = SocketChannel.open();
				channel.configureBlocking(false);
				channel.connect(serverAddress);
				channel.register(selector, SelectionKey.OP_CONNECT);
				connect();
			}
		}
	}

	@Override
	public void connectToServer(String address, int port) throws IOException {
		connectToServer(new InetSocketAddress(address,port));		
	}
	
	public boolean isLoggedIn() {
		synchronized(isLoggedIn) {
			return isLoggedIn;
		}
	}
	
	public void setLoggedIn() {
		synchronized(isLoggedIn) {
			isLoggedIn = true;
		}
	}
	
	public void unsetLoggedIn() {
		synchronized(isLoggedIn) {
			isLoggedIn = false;
		}
	}

	@Override
	public boolean isReceiving() {
		synchronized(isReceiving) {
			return isReceiving && isConnected();
		}
	}

	@Override
	public boolean isConnected() {
		synchronized(writeLock) {
			synchronized(readLock) {
				return isChannelConnected(channel) && isChannelConnected(serverChannel);
			}
		}
	}

	private boolean isChannelConnected(SocketChannel c) {
		return c != null && c.isConnected() && c.isOpen();
	}
	
	@Override
	public void stopReceiving() {
		synchronized(isReceiving) {
			isReceiving = false;
		}		
	}

	@Override
	public void startReceiving() {
		synchronized(isReceiving) {
			if(!isConnected()) {
				throw new IllegalStateException("You must be connected to a server "
						+ "before receiving from a server");
			} else if(isReceiving() && receivingThread != null && receivingThread.isAlive()) {
				return;
			} else {
				isReceiving = true;
				constructReceivingThread();
//				System.out.println("Starting Receiver Thread");
				receivingThread.start();
			}
		}
	}

	private void constructReceivingThread() {
		receivingThread = new Thread(){
			public void run() {
				try{
					while(isReceiving()) {
						selector.select(SELECT_TIMEOUT);
						Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
						while(selectedKeys.hasNext()) {
							SelectionKey key = (SelectionKey) selectedKeys.next();
							selectedKeys.remove();
							if(!key.isValid())
								continue;
							else if(key.isReadable()) {
								receive();
							}
						}
					}
				} catch (IOException e) {
					
				} finally {
					stopReceiving();
				}
			}
		};
	}

	private void connect() {
		try {
			while(true) {
				selector.select(SELECT_TIMEOUT);
				Iterator<SelectionKey> selectedKeys = this.selector.selectedKeys().iterator();
				while(selectedKeys.hasNext()) {
					SelectionKey key = (SelectionKey) selectedKeys.next();
					selectedKeys.remove();
					if(!key.isValid())
						continue;
					else if(key.isConnectable()) {
						connect(key);
						return;
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void connect(SelectionKey key) {
		try {
			System.out.println("Attempting to connect...");
			while(!((SocketChannel) key.channel()).finishConnect());

			if(((SocketChannel) key.channel()).isConnected()) {
				System.out.println("Connected!");
				key.interestOps(SelectionKey.OP_READ);
				serverChannel = ((SocketChannel)key.channel());
			}
		} catch (IOException e) {
			System.out.println("Could not connect to server");
		}
	}

	@Override
	public void disconnectFromServer() {
		synchronized(readLock) {
			synchronized(writeLock) {
				stopReceiving();
				try {
					if(channel != null)
						channel.close();
					if(serverChannel != null)
						serverChannel.close();
				} catch (IOException e) {

				} finally {
					channel = null;
					serverChannel = null;
				}
			}
		}
	}

	public void send(Task t) throws IOException {
		if(!isConnected()) {
			System.out.println("no longer connected");
			return;
		} else {
			synchronized(writeLock) {
				byte[] data = t.toByteArray();
				int dataLength = data.length;
//				System.out.println("Sending: " + dataLength + " bytes for TaskCode: " + t.getTaskCode());
				ByteBuffer writeBuffer = ByteBuffer.allocate(dataLength+4);
				writeBuffer.putInt(dataLength);
				writeBuffer.put(data);
				writeBuffer.flip();
				int written = 0;
				while(writeBuffer.hasRemaining()) {
					written += serverChannel.write(writeBuffer);
				}
//				System.out.println("Done Sending: " + written + " bytes");
			}
		}
	}

	@Override
	public void sendToServer(Task t) throws IOException {
		if(!isConnected()) {
			throw new IllegalStateException("You must be connected to a server "
					+ "before receiving from a server");
		}
		else if(!isReceiving()) {
			System.out.println("You must be receiving from a server "
					+ "before sending to a server");
			startReceiving();
		}
		try {
			send(t);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void receive() throws IOException {
		ByteBuffer localWrite = null;

		ByteBuffer readBuffer = ByteBuffer.allocate(7000);
		int temp = 0, read = 0, total = 0;
		int size = -1;
		synchronized(readLock) {
			while( (temp = channel.read(readBuffer)) > 0 ) {
				read+=temp;
				if(read >= 4) {
					readBuffer.flip();
					size = readBuffer.getInt();
//					System.out.println("READ " + size + " For the amount of required bytes");
					if(size >= 1) {
						localWrite = ByteBuffer.allocate(size);
						if(read > 4) {
							fillLocal(localWrite,readBuffer);
						}
						total += receiveTask(localWrite,size, read);
						read -= size;
						//readBuffer.clear();
					} else {
//						System.out.println("SIZE: " + size + " is an invalid ammount of bytes.");
//						System.out.println("returning");
						return;
					}
				} else {
					continue;
				}
			}
			if(read > 0 && readBuffer.remaining() >= 4) {
				handleOverread(readBuffer);
			}
			if(temp == -1 || !isConnected()) {
				disconnectFromServer();
			} else {
//				System.out.println("DONE READING BYTES: READ " + total + " TOTAL BYTES");
			}
		}
	}
	
	private void handleOverread(ByteBuffer readBuffer) throws IOException {
		while(readBuffer.remaining() >= 4) {
			int size = readBuffer.getInt();
			if(size >= 1) {
				ByteBuffer localWrite = ByteBuffer.allocate(size);
				fillLocal(localWrite,readBuffer);
				receiveTask(localWrite,size, 0);
			} else {
				return;
			}
			
		}
	}
	
	private void fillLocal(ByteBuffer local, ByteBuffer readBuffer) {
		while(local.hasRemaining() && readBuffer.hasRemaining()) {
			local.put(readBuffer.get());
		}
	}

	private int receiveTask(ByteBuffer local, int size, int currentRead) throws IOException {
		int temp = 0,read = currentRead;

		if(read < size) {
			while(local.hasRemaining() && (temp = channel.read(local)) > -1) {
				read += temp;
				if(read >= size) {
					break;
				}
			}
		}

//		System.out.println("Done reading the required bytes: " + (read - 4));

		if(temp == -1) {
			disconnectFromServer();
//			System.out.println("Server disconnected");
		} else {
			handleTask(createTask(local));
		}

		return read;
	}

	private Task createTask(ByteBuffer local) throws IOException {
		return TaskFactory.getInstance().createTaskFromBytes(local.array());
	}

	@Override
	public void handleTask(Task t) {
		int taskcode = t.getTaskCode();
		if(taskcode == TaskConstents.LOGIN_TASK || taskcode == TaskConstents.LOGOUT_TASK) {
			this.setLoggedIn();
		} else if(taskcode == TaskConstents.LOGOUT_TASK || taskcode == TaskConstents.UNREGISTER_TASK) {
			this.unsetLoggedIn();
		} else {
			threadPool.execute(t);
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {
		if(args.length != 5) {
			System.out.println(
					"Expected 5 arguments: <server-host> <server-port> <email> <nickname> <password>"
					);
			return;
		}
		else {
			String host = args[0];
			String email, nickname, password;
			int port;

			try {
				port = Integer.parseInt(args[1]);
				if(port <=0 ) 
					throw new NumberFormatException();
			} catch (NumberFormatException ex) { 
				System.out.println("Invalid port: " + args[1]);
				return;
			}
			
			email = args[2];
			nickname = args[3];
			password = args[4];

			InetSocketAddress address = new InetSocketAddress(host,port);

			Client c1 = new Client(address);
			c1.sendToServer(new RegisterTask(email,nickname,password));
			Thread.sleep(2000);
			
			//c1.sendToServer(new MessageTask("Hello tanner"));
			c1.sendToServer(new ForwardTask(nickname,
					new MessageTask("Hello tanner"),
					"tanner"));
			while(true) {
				Thread.sleep(1000);
			}
			

		}
	}

}
