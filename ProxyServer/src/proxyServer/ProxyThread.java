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
	private URLConnection connect;

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
			if (count == 0) { // get url protocol

				// parse the first line of the request to find the url
				String[] tokens = inputLine.split(" ");
				if (tokens.length < 3) {
					System.err.println("Invalid request: " + inputLine);
					break;
				}
				if (!tokens[0].equals("GET")) {
					break;
				}

				urlToCall = tokens[1];
			} else { // next lines

				if (inputLine.trim().equals("")) { // empty line means end of header
					break;
				}
				String[] tokens = inputLine.split(":"); // non-empty line: it's a header field

				if (tokens.length < 2) {
					System.out.println("Invalid line in request header: " + inputLine);
					continue;
				}
				headerFields.put(tokens[0], tokens[1].trim());

			}
			count++;
		}
	}
	
	
	
	
	//send the request to server and receive the page back
	private void sendRequestAndGetServerContent() throws IOException {
		url = new URL(urlToCall);
		connect = url.openConnection();

		// add select header fields that we received to the clients to
		// the http connection ????
		

		// disable gzipping content
		connect.setRequestProperty("Accept-Encoding", "deflate");

		connect.setDoInput(true);
		

		// Get the response
		is = connect.getInputStream();
	}

	//send back the modified webpage
	private void sendClientModifiedContent() throws IOException {
		// send status to client since getHeaderField(0) returns the status line
		out.println(connect.getHeaderField(0));
				
		// add select header fields that we received to the clients ???
		
		// send empty line that separates response header from the resource
		out.println();
		
		// send the body, reading from the server and writing to the client
		// if the Content-Type field is "text/html", it's a HTML page
		// if the Content-Type field is "image/jpeg", it's a JPG file etc.
		PrintStream ps = null;
		fileName = null;
		
		// if requesting an image create a new PrintStream
		if (connect.getContentType() != null && connect.getContentType().equals("image/jpeg")) {
			// create a new file of the type specified by getting file extension from
			// Content-Type field
			fileName = connect.getContent().hashCode() + ".jpeg";
			ps = new PrintStream(new FileOutputStream(fileName));
		}

		byte b[] = new byte[BUFFER_SIZE];
		int index = is.read(b, 0, BUFFER_SIZE);
		
		while (index != -1) {
			// if content is an image, write the bytes to the file
			if (ps != null) {
				ps.write(b, 0, index);
			} else {
				out.write(b, 0, index);
			}
			index = is.read(b, 0, BUFFER_SIZE);
		}
		
		if (ps != null) {
			modifyImage(fileName); //call the OpenCV method to modify
			//write in new file name
			FileInputStream stream = new FileInputStream("edited_" + fileName);

			index = stream.read(b, 0, BUFFER_SIZE);
			while (index != -1) {
				out.write(b, 0, index);
				index = stream.read(b, 0, BUFFER_SIZE);
			}
			stream.close();
		}

		out.flush(); // flush output stream and force any buffered output bytes to be written out
		if (ps == null) { // or do i check if not null?
			ps.close();
		}

	}
	
	//close the socket, bufferedreader, etc
	private void closeResources() throws IOException {
		if (out != null) {
			out.close();
		}
		if (in != null) {
			in.close();
		}
		if (socket != null) {
			socket.close();
		}
	}
	
	
	private void modifyImage(String fileName) {
		// Create a face detector from the cascade file
		CascadeClassifier faceDetector = new CascadeClassifier("haarcascade_frontalface_alt.xml");
		Mat image = Imgcodecs.imread(fileName);

		// Detect faces in the image.
		// MatOfRect is a special container class for Rect.
		MatOfRect faceDetections = new MatOfRect();
		faceDetector.detectMultiScale(image, faceDetections);

		// Blur each face
		for (Rect rect : faceDetections.toArray()) {
			Mat faceArea = image.submat(rect);
			Imgproc.blur(faceArea, faceArea, new Size(30, 30));
		}
		
		// Save the modified image
		Imgcodecs.imwrite("edited_" + fileName, image);
	}
	
	
}
