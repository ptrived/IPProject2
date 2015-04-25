import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

class GoBackNTimerTask extends TimerTask{

	@Override
	public void run() {
		// TODO :: resend the packets		
		List<DataPacket> list = new ArrayList<DataPacket>();
		list.addAll(SimpleFTPClient.window);
		System.out.println("Timeout, sequence number = "+list.get(0).getSequenceNumber());
		for(DataPacket packet : list){
			byte[] dataArr = Utils.serializePacket(packet);
			InetAddress ipAddr;
			try {
				ipAddr = InetAddress.getLocalHost();
				DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,7735);
				SimpleFTPClient.client.send(dataPacket);				
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
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

	static List<DataPacket> window ;
	static byte[] mssData;
	static int mssCount;
	static int lastAckRcvd;
	static int lastPktSent;
	static int endAckExpected;
	static int firstPktInWindow;
	//static int lastByteRcvd;
	static int sequenceNum;
	static TimerTask timerTask ;
	static Timer timer;
	/**
	 *  
	 *  Reads data byte by byte from the file
	 *  Buffers the data locally 
	 *  Sends one packet when it has MSS bytes of data
	 *  Sends Header + MSS bytes of data ; except for the last packet
	 *  Implement Timeout for lost ACKs
	 *  
	 */
	private static int goBackN(byte buff[], boolean lastFlag){
		//int result = 0;
		if(window.size()==windowSize){
			return 0;
		}

		if(lastFlag == true && mssCount > 0){
			//send the last packet
			mssCount = 0;
			lastPktSent++;

			DataPacket data = new DataPacket(mssData);
			data.setSequenceNumber(sequenceNum);
			sequenceNum = sequenceNum+MSS;
			window.add(data);
			byte[] dataArr = Utils.serializePacket(data);
			InetAddress ipAddr;
			try {
				ipAddr = InetAddress.getLocalHost();
				DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,7735);
				client.send(dataPacket);
				//System.out.println("Client done with sending the last packet to server");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//send -1 
			mssData = new byte[0];
			//lastPktSent++;
			data = new DataPacket(mssData);
			endAckExpected = sequenceNum+MSS;
			data.setSequenceNumber(sequenceNum);
			sequenceNum = sequenceNum+MSS;
			window.add(data);
			dataArr = Utils.serializePacket(data);

			try {
				ipAddr = InetAddress.getLocalHost();
				DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,7735);
				client.send(dataPacket);
				//System.out.println("Client done with sending the last packet to server");
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


		}
		else{
			mssData[mssCount++] = buff[0];
			if(mssCount==MSS){
				DataPacket data = new DataPacket(mssData);
				data.setSequenceNumber(sequenceNum);
				sequenceNum = sequenceNum+MSS;
				window.add(data);
				mssCount = 0;
				lastPktSent++;		

				byte[] dataArr = Utils.serializePacket(data);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getLocalHost();
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,7735);
					client.send(dataPacket);
					mssData = new byte[MSS];
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}				
				if(lastPktSent==0){
					timerTask = new GoBackNTimerTask();
					timer = new Timer(true);
					timer.scheduleAtFixedRate(timerTask,1000, 1000);
					//timer.schedule(timerTask, 5000);
				}
			}
		}
		return 1;
	}


	private static void rdt_send(){
		try {
			FileInputStream input = new FileInputStream(filename);
			byte[] buff = new byte[1];
			int res = 0;

			while(res!=-1){
				res = input.read(buff);
				//lastByteRcvd++;
				while(goBackN(buff, false)==0){

				}
				//goBackN(buff, false);
			}
			System.out.println("File Read");
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
			windowSize = 4;
			MSS = 2048;
			sequenceNum =0;
			client = new DatagramSocket();
			System.out.println("Connected to server");
			simpleClient = new SimpleFTPClient();
			//simpleClient.sequenceNum = 0;
			init();
			long startTime = System.currentTimeMillis();
			rdt_send();
			long endTime = System.currentTimeMillis();
			System.out.println("Time to transfer : "+(endTime - startTime));
		}catch(Exception e){
			e.printStackTrace();
		}
	}


	private static void init() {
		mssData = new byte[MSS];
		window = new ArrayList<DataPacket>();
		mssCount = 0;
		lastAckRcvd = -1;
		lastPktSent = -1;
		firstPktInWindow = 0;
		endAckExpected = -1;

		Thread t = new Thread(simpleClient);
		//t.setDaemon(true);
		t.start();
	}

	@Override
	public void run() {
		try {
			while(true){
				//System.out.println("Thread running to receive ACK in client");
				byte[] buffer = new byte[1000];
				InetAddress ipAddr = InetAddress.getLocalHost();
				DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length);
				client.receive(ackPacket);
				AckHeader ackData = (AckHeader) Utils.deserializePacket(ackPacket.getData());
				//System.out.println(ackData.getSequenceNumber());
				int ackRcvd = ackData.getSequenceNumber();
				if(ackRcvd == endAckExpected ){
					//System.out.println("File transfer complete");
					System.exit(1);
				}
				if(ackRcvd > lastAckRcvd){

					lastAckRcvd = ackRcvd;
					//System.out.println("Last Ack Rcvd = " + lastAckRcvd);
					timer.cancel();
					//TODO :: slide the window

					while(firstPktInWindow < lastAckRcvd && window.size()>0){
						window.remove(0);
						//System.out.println("Window Size :"+ window.size());
						firstPktInWindow+=MSS;
					}
					timerTask = new GoBackNTimerTask();
					timer = new Timer(true);
					timer.scheduleAtFixedRate(timerTask, 1000, 1000);
					//timer.schedule(timerTask, 5000);
					//System.out.println("Out of while after receiving ack");
				}
			}
		} catch (IOException e) {

			e.printStackTrace();
		}
	}
}
