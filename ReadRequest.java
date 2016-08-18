import java.io.*; 
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * The main() program in this class is designed to read requests from
 * a Web browser and display the requests on standard output.  The
 * program sets up a listener on port 50505.  It can be contacted
 * by a Web browser running on the same machine using a URL of the
 * form  http://localhost:505050/path/to/resource.html  This method
 * does not return any data to the web browser.  It simply reads the
 * request, writes it to standard output, and then closes the connection.
 * The program continues to run, and the server continues to listen
 * for new connections, until the program is terminated (by clicking the
 * red "stop" square in Eclipse or by Control-C on the command line).
 */
public class ReadRequest {
	
	/**
	 * The server listens on this port.  Note that the port number must
	 * be greater than 1024 and lest than 65535.
	 */
	private final static int LISTENING_PORT = 50505;
	
	/**
	 * Main program opens a server socket and listens for connection
	 * requests.  It calls the handleConnection() method to respond
	 * to connection requests.  The program runs in an infinite loop,
	 * unless an error occurs.
	 * @param args ignored
	 */
	public static void main(String[] args) {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(LISTENING_PORT);
		}
		catch (Exception e) {
			System.out.println("Failed to create listening socket.");
			return;
		}
		System.out.println("Listening on port " + LISTENING_PORT);
		try {
			while (true) {
				Socket connection = serverSocket.accept();
				System.out.println("\nConnection from " 
						+ connection.getRemoteSocketAddress());
				ConnectionThread thread = new ConnectionThread(connection);
				thread.start();
			}
		}
		catch (Exception e) {
			System.out.println("Server socket shut down unexpectedly!");
			System.out.println("Error: " + e);
			System.out.println("Exiting.");
		}
	}

	/**
	 * Handle commuincation with one client connection.  This method reads
	 * lines of text from the client and prints them to standard output.
	 * It continues to read until the client closes the connection or
	 * until an error occurs or until a blank line is read.  In a connection
	 * from a Web browser, the first blank line marks the end of the request.
	 * This method can run indefinitely,  waiting for the client to send a
	 * blank line.
	 * NOTE:  This method does not throw any exceptions.  Exceptions are
	 * caught and handled in the method, so that they will not shut down
	 * the server.
	 * @param connection the connected socket that will be used to
	 *    communicate with the client.
	 * @throws IOException 
	 */
	private static void handleConnection(Socket connection) throws IOException {
		String rootDirectory = "//C:/Users/Murphy/Desktop/server";
		try {
			Scanner in = new Scanner(connection.getInputStream());
			String lineGet = in.next();			
			String compareWithGet = "GET";
 			//Tests is the first token is equal to get, if it isn't then it runs the
			//sendErrorResponse method. else it continues
			if (lineGet.equals("GET") == false){
				sendErrorResponse(501, connection.getOutputStream() );
				return;
			}
			System.out.println(lineGet);
			String pathToFile = in.next();
			System.out.println(pathToFile);
			File file = new File(rootDirectory + pathToFile);
			System.out.println(rootDirectory + pathToFile);
				//Checks to se if file exists, if it doesn't then it calls the
				//sendErrorResponse() method sending the user to a custom 404 error page
			if(file.exists() != true){
				sendErrorResponse(404, connection.getOutputStream() );
				return;
				
			}
			//Sends headers to the client socket then runs sendFile() which
			// send the selected file usiing the selected output
			PrintWriter pw = new PrintWriter(connection.getOutputStream(), true );
			pw.print("HTTP/1.1 200 OK"  + "\r\n");
			pw.print("Connection: close"  + "\r\n");
			pw.print("Content-Length: " + file.length()  + "\r\n");			
			pw.print("Content-Type: " + getMimeType(pathToFile) + "\r\n");
			pw.print("\r\n");
			pw.flush();
			sendFile(file, connection.getOutputStream());
								
			
		}
		
		catch (Exception e) {
			System.out.println("Error while communicating with client: " + e);
		}
		finally {  // make SURE connection is closed before returning!
			try {
				
				connection.close();
			}
			catch (Exception e) {
			}
			System.out.println("Connection closed.");
		}
	}
	private static void sendFile(File file, OutputStream socketOut) throws
	  IOException {
	    InputStream in = new BufferedInputStream(new FileInputStream(file));
	    OutputStream out = new BufferedOutputStream(socketOut);
	    while (true) {
	      int x = in.read(); // read one byte from file
	      if (x < 0)
	         break; // end of file reached
	      out.write(x);  // write the byte to the socket
	   }
	   out.flush();
	}

	private static String getMimeType(String fileName) {
        int pos = fileName.lastIndexOf('.');
        if (pos < 0)  // no file extension in name
            return "x-application/x-unknown";
        String ext = fileName.substring(pos+1).toLowerCase();
        if (ext.equals("txt")) return "text/plain";
        else if (ext.equals("html")) return "text/html";
        else if (ext.equals("htm")) return "text/html";
        else if (ext.equals("css")) return "text/css";
        else if (ext.equals("js")) return "text/javascript";
        else if (ext.equals("java")) return "text/x-java";
        else if (ext.equals("jpeg")) return "image/jpeg";
        else if (ext.equals("jpg")) return "image/jpeg";
        else if (ext.equals("png")) return "image/png";
        else if (ext.equals("gif")) return "image/gif"; 
        else if (ext.equals("ico")) return "image/x-icon";
        else if (ext.equals("class")) return "application/java-vm";
        else if (ext.equals("jar")) return "application/java-archive";
        else if (ext.equals("zip")) return "application/zip";
        else if (ext.equals("xml")) return "application/xml";
        else if (ext.equals("xhtml")) return"application/xhtml+xml";
        else return "x-application/x-unknown";
           // Note:  x-application/x-unknown  is something made up;
           // it will probably make the browser offer to save the file.
     }
    private static class ConnectionThread extends Thread {
        Socket connection;
        ConnectionThread(Socket connection) {
           this.connection = connection;
        }
        public void run() {
           try {
			handleConnection(connection);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        }
     }
    // This method sends one of two possible errors pages depending on the errorCodesent
    // you need a file for the 404 error, the 501 is in text so no file is needed
    static void sendErrorResponse(int errorCode, OutputStream socketOut){
		try{
    	
		if (errorCode == 404){
		PrintWriter pw2 = new PrintWriter(socketOut);
		pw2.print("HTTP/1.1 404 Not Found"  + "\r\n");			
		pw2.print("Connection: close"  + "\r\n");
		pw2.print("Content-Type: text/html"  + "\r\n");
		pw2.print("\r\n");
		pw2.flush();
		
		File file404 = new File("//C:/Users/Murphy/Desktop/server/path/404.html");
		System.out.println("This file exists? " + file404.exists());
		sendFile(file404, socketOut);
		}
    	else{
    		PrintWriter pw2 = new PrintWriter(socketOut);
    		pw2.print("HTTP/1.1 501 Not Implemented"  + "\r\n");			
    		pw2.print("Connection: close"  + "\r\n");
    		pw2.print("Content-Type: text/plain"  + "\r\n");
    		pw2.print("\r\n");
    		pw2.print("Error: 501 Not Implemented");
    		pw2.flush();
    	}
		}
		catch(IOException e){}
		
    }
}
