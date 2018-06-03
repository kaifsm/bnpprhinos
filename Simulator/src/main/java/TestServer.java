import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * A TCP server that runs on port 9090.  When a client connects, it
 * sends the client the current date and time, then closes the
 * connection with that client.  Arguably just about the simplest
 * server you can write.
 */
public class TestServer {

    /**
     * Runs the server.
     */
    public static void main(String[] args) throws IOException
    {
        ServerSocket listener = new ServerSocket(9090);
        try
        {
        	System.out.println("Starting server. . .");
            while (true)
            {
                Socket socket = listener.accept();
                System.out.println("Accepted connection");
                try
                {
                	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                	String str;
                	while( (str = in.readLine() ) != null)
                	{
                        System.out.print(str);
                        System.out.print(" ");
                        System.out.println(new Date().toString());
                	}
                }
                finally
                {
                    socket.close();
                }
            }
        }
        finally
        {
            listener.close();
        }
    }
}
