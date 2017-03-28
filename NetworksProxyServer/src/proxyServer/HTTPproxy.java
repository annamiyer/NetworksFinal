package proxyServer;

import java.net.*;
import java.io.*;



public class HTTPproxy extends Thread {

	static public int CONNECT_RETRIES = 5;
    static public int CONNECT_PAUSE = 5;
    static public int TIMEOUT = 50;
    static public int BUFFERSIZE = 1024;
    static public boolean logging = false;
    static public OutputStream out = null;
    
    Socket socket = new Socket();
    
    //create a proxy thread for the socket
    public HTTPproxy(Socket s) { 
    	socket = s; 
    	start(); 
    }
    
    public void writeLog(int c, boolean browser) throws IOException {
        out.write(c);
    }
    
    //prevent subclasses from overriding, output to stdout
    public String processHostName(String url, String host, int port, Socket sock) {
        java.text.DateFormat cal = java.text.DateFormat.getDateTimeInstance();
        System.out.println(cal.format(new java.util.Date()) + " - " + url 
        		+ " " + sock.getInetAddress()+ "<BR>");
        return host;
    }
 
}




