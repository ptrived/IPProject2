import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


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
