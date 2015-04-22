import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
	static String serverHostname;
	static DatagramSocket client;
	
	static String filename;
	static int windowSize ;
	static int MSS;
	
	static List<Byte> window ;
	static int lastAckRcvd;
	static int lastByteSent;
	//static int sequenceNum;
	
	/**
	 *  
	 *  Reads data byte by byte from the file
	 *  Buffers the data locally 
	 *  Sends one packet when it has MSS bytes of data
	 *  Sends Header + MSS bytes of data ; except for the last packet
	 *  Implement Timeout for lost ACKs
	 *  
	 */
	private static void rdt_send(){
		try {
			FileInputStream input = new FileInputStream(filename);
			byte[] buff = new byte[1];
			int res = 0;
			res = input.read(buff);
			
			while(res!=-1){
				if(window.size() == windowSize){
					//window is already full
					continue;
				}
				window.add(buff[0]);		// add the byte into the window
				
				//check if window has data > MSS to be sent
				if((lastByteSent % MSS)==0){
					byte[] data = new byte[MSS];
					for(int i=0; i<MSS; i++){
						int index = (lastByteSent+i) % MSS;
						data[i] = window.get(index);
					}
					DataPacket packet = new DataPacket(data);
					//send this packet to the server
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(baos);
					oos.writeObject(packet);
					
					//DatagramPacket sendPacket = new DatagramPacket(packet., length, address, port)
				}
			}
				
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * Client should be invoked like this:
	 * Simple_ftp_server server-host-name server-port# file-name N MSS 
	 * 
	 */
	public static void main (String[] args){
		try{
			serverHostname = args[1];
			int portInput = Integer.parseInt(args[2]);
			if(portInput!=portNum){
				System.out.println("Invalid Server Port Number");
				System.exit(1);
			}
			filename = args[3];
			windowSize = Integer.parseInt(args[4]);
			MSS = Integer.parseInt(args[5]);
			
			client = new DatagramSocket();
			System.out.println("Connected to server");
			
			/*
			 * InetAddress IPAddress = InetAddress.getByName("localhost");
			 * byte[] sendData = new byte[1024];
			 * byte[] receiveData = new byte[1024];
			 * String sentence = inFromUser.readLine();
			 * sendData = sentence.getBytes();
			 * DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
			 * clientSocket.send(sendPacket);
			 * DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			 * clientSocket.receive(receivePacket);
			 * String modifiedSentence = new String(receivePacket.getData());
			 * System.out.println("FROM SERVER:" + modifiedSentence);
			 * clientSocket.close();
			 */
			
			init();
			rdt_send();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private static void init() {
		window = new ArrayList<Byte>();
		lastAckRcvd = -1;
		lastByteSent = -1;
	}
}
