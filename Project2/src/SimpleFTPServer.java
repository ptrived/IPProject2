import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 
 * @author Ritwikaroyc
 * 
 * command to start the Server :
 * Simple_ftp_server server-port file-name p
 * file-name : name of the file where the data will be written
 * p : packet loss probability
 * 
 * what to print: whenever a packet with sequence number X is discarded by probabilistic loss service, the server
 * should print the following line:
 * Packet loss, Sequence number = x
 * 
 */

public class SimpleFTPServer {
	static ServerSocket socket ;
	static int portNum = 7735;
	
	
	public static void main(String args[]){
		try {
			socket = new ServerSocket(portNum);
			while(true){
				Socket clientSocket = socket.accept();
				
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
