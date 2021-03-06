package proxyServer;
  
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import org.opencv.core.Core;

public class Server {

	public static final int portNumber = 5555;
	
	/**static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}**/
	 
	public static void main(String[] args){
		Server proxyServer = new Server();
		proxyServer.start();
	}
	
	public void start(){
		System.out.println("Starting the Proxy Server ...");
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber, 1);
		
		while(true){
			
			Socket clientSocket = serverSocket.accept();
			System.out.println("Connection to proxyServer is "
					+clientSocket.isConnected());
			
			//create new proxy thread and pass server socket over as a constructor parameter
			 
			
			//this will all move into the proxy thread
			InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			
			
			
			// iterate over all the lines following the GET/POST request
			String meta = null;
			while((meta = bufferedReader.readLine()) != null){
				System.out.println(meta);
			}

			bufferedReader.close();
		}
		
	} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
