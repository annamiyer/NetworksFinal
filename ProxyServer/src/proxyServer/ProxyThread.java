package proxyServer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class ProxyThread extends Thread {
	
	private Socket socket = null;
	private static final int BUFFER_SIZE = 32768;

	private PrintStream out;
	private BufferedReader in;

	private InputStream is;

	private String inputLine;
	private String urlToCall;
	private String fileName;

	private URL url;
	private URLConnection conn;

	private HashMap<String, String> headerFields;

	public ProxyThread(Socket socket) {
		super("ProxyThread");
		this.socket = socket;
	}
	
	public void run() {
		// get input from user
		// send request to server
		// get response from server
		// send response to user

		try {
			setup();
			getClientRequest();
			sendRequestAndGetServerContent();
			sendClientModifiedContent();
			closeResources();

		} catch (IOException e) {}
	}

	
	//helper methods
	
	//set up printstream and bufferedreader
	private void setup() throws IOException {
		out = new PrintStream(socket.getOutputStream());
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		urlToCall = "";
		headerFields = new HashMap<String, String>();
	}
	
	//read in client's request
	private void getClientRequest() throws IOException {
		int count = 0;

		while ((inputLine = in.readLine()) != null) {
			if (count == 0) { // first line: GET URL PROTOCOL

				// parse the first line of the request to find the url
				String[] tokens = inputLine.split(" ");

				if (tokens.length < 3) {
					System.err.println("Bad request: " + inputLine);
					break;
				}

				if (!tokens[0].equals("GET")) {
					break;
				}

				urlToCall = tokens[1];

			} else { // subsequent lines

				if (inputLine.trim().equals("")) // empty line means end of
													// header
					break;

				String[] tokens = inputLine.split(":"); // non-empty line: it's
														// a header field

				if (tokens.length < 2) {
					System.out.println("Invalid line in request header: "
							+ inputLine);
					continue;
				}
				headerFields.put(tokens[0], tokens[1].trim());

			}

			count++;
		}
		
	}
	
	//send the request to server and receive the page back
	private void sendRequestAndGetServerContent() throws IOException {
		
	}

	//send back the modified webpage
	private void sendClientModifiedContent() throws IOException {
		
	}
	
	//close the socket, bufferedreader, etc
	private void closeResources() throws IOException {
		
	}
	
}
