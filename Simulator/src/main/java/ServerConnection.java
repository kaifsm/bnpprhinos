import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.SocketFactory;
import org.apache.log4j.Logger;

public class ServerConnection
{
	private String address;
	private int port;
	private PrintWriter out = null;
	private Socket socket = null;
	LinkedBlockingDeque<String> replyQueue = new LinkedBlockingDeque<>();
	private AtomicBoolean connectionLost = new AtomicBoolean(true);
	ScheduledExecutorService reconnectExecutor = Executors.newSingleThreadScheduledExecutor();

	public ServerConnection(String address_, int port_) throws IOException
	{
		this.address = address_;
		this.port = port_;
	}

	private void deleteSocket()
	{
		if (socket != null)
		{
			try
			{
				socket.close();
			}
			catch (IOException ex)
			{

			}
			socket = null;
		}
	}

	public void killConnection()
	{
		deleteSocket();
	}

	public boolean isConnected()
	{
		return (socket != null && !connectionLost.get());
	}

	public void connect() throws IOException
	{
		if (socket == null || connectionLost.get())
		{
			killConnection();

			SocketFactory factory = (SocketFactory) SocketFactory.getDefault();
			socket = factory.createSocket(address, port);

			out = new PrintWriter(socket.getOutputStream(), true);
			connectionLost.set(false);
		}
	}

	public void writeToOutput(String message) throws IOException
	{
		out.println(message);
	}
}