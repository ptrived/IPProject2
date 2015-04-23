import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
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
public class SimpleFTPClient implements Runnable{
	static final int portNum = 7735;
	static String serverHostname;
	static DatagramSocket client;
	static SimpleFTPClient simpleClient;

	static String filename;
	static int windowSize ;
	static int MSS;

	static List<byte[]> window ;
	static byte[] mssData;
	static int mssCount;
	static int lastAckRcvd;
	static int lastMSSSent;
	static int lastByteRcvd;
	static int sequenceNum = 0;
	
	/**
	 *  
	 *  Reads data byte by byte from the file
	 *  Buffers the data locally 
	 *  Sends one packet when it has MSS bytes of data
	 *  Sends Header + MSS bytes of data ; except for the last packet
	 *  Implement Timeout for lost ACKs
	 *  
	 */
	private static void goBackN(byte buff[], boolean lastFlag){
		while(window.size()>=windowSize){
			//do nothing
		}
		if(lastFlag == true && mssCount > 0){
			//send the last packet
		}
		else{
			mssData[mssCount++] = buff[0];
			if(mssCount==MSS){
				window.add(mssData);
				mssCount = 0;
				lastMSSSent++;
				
				DataPacket data = new DataPacket(mssData);
				data.setSequenceNumber(++sequenceNum);
				byte[] dataArr = Utils.serializePacket(data);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getLocalHost();
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,7735);
					client.send(dataPacket);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	

	private static void rdt_send(){
		try {
			FileInputStream input = new FileInputStream(filename);
			byte[] buff = new byte[1];
			int res = 0;

			while(res!=-1){
				res = input.read(buff);
				lastByteRcvd++;
				goBackN(buff, false);
			}
			goBackN(null, true);

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
			//			serverHostname = args[1];
			//			int portInput = Integer.parseInt(args[2]);
			//			if(portInput!=portNum){
			//				System.out.println("Invalid Server Port Number");
			//				System.exit(1);
			//			}
			//			filename = args[3];
			//			windowSize = Integer.parseInt(args[4]);
			//			MSS = Integer.parseInt(args[5]);
			serverHostname = "localhost";
			int portInput = 7735;
			filename = "F:\\123.txt";
			windowSize = 128;
			MSS = 16;
			client = new DatagramSocket();
			System.out.println("Connected to server");
			simpleClient = new SimpleFTPClient();
			
			init();
			rdt_send();
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private static void init() {
		mssData = new byte[MSS];
		window = new ArrayList<byte[]>();
		mssCount = 0;
		lastAckRcvd = -1;
		lastMSSSent = -1;
		//lastByteRcvd = -1;

		Thread t = new Thread(simpleClient);
		t.start();
	}

	@Override
	public void run() {
		try {
			while(true){
				System.out.println("Thread running to receive ACK in client");
				byte[] buffer = new byte[1000];
				InetAddress ipAddr = InetAddress.getLocalHost();
				DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length);
				client.receive(ackPacket);
				AckHeader ackData = (AckHeader) Utils.deserializePacket(ackPacket.getData());
				System.out.println(ackData.getSequenceNumber());
				lastAckRcvd = ackData.getSequenceNumber();
				System.out.println("Last Ack Rcvd = " + lastAckRcvd);
				//TODO :: slide the window
			}
			
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
