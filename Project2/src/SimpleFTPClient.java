import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/*
 * 
 * command to start the client: simple_ftp_server server_host_name server-port file-name N MSS
 * file-name : name of the file to be transferred
 * N : window size
 * MSS : maximum segment size
 * 
 * What to print on CLient: 
 * whenever a timeout occurs for a packet with sequence number Y, the client should print the following:
 * Timeout, sequence number = Y
 * 
 */
public class SimpleFTPClient {
	static final int portNum = 7735;
	static final String serverHostname = "localhost";
	static Socket client;
	
	static String filename;
	static int windowSize ;
	static int MSS;
	
	static List<Byte> window ;
	
	public static void main (String[] args){
		try{
			client = new Socket(serverHostname, portNum);
			filename = args[3];
			windowSize = Integer.parseInt(args[4]);
			MSS = Integer.parseInt(args[4]);
			
			init();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}


	private static void init() {
		window = new ArrayList<Byte>();
		
	}
}