import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * @author Ritwikaroyc
 *
 */
class SelectiveARQTimerTask extends TimerTask{
	int sequenceNum;
	public SelectiveARQTimerTask(int seqNum){
		this.sequenceNum = seqNum;
	}
	@Override
	public void run() {	
		List<DataPacket> list = new ArrayList<DataPacket>();
		list.addAll(SelectiveARQClient.window);
		System.out.println("Timeout, sequence number = "+list.get(0).getSequenceNumber());
		synchronized(SelectiveARQClient.window){
				int index = (this.sequenceNum-SelectiveARQClient.lastAckRcvd)/SelectiveARQClient.MSS - 1;
				DataPacket packet = SelectiveARQClient.window.get(index);
				byte[] dataArr = Utils.serializePacket(packet);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getByName(SelectiveARQClient.serverHostname);
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,7735);
					SelectiveARQClient.client.send(dataPacket);	
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			
		}
	}

}
public class SelectiveARQClient implements Runnable{
	static int portNum;
	static String serverHostname;
	static DatagramSocket client;
	static SelectiveARQClient selectiveClient;

	static String filename;
	static int windowSize ;
	static int MSS;

	static List<DataPacket> window ;
	static List<SelectiveARQTimerTask> timerList;
	static byte[] mssData;
	static int mssCount;
	static int lastAckRcvd;
	static int lastPktSent;
	static int endAckExpected;
	static int firstPktInWindow;
	static long startTime;
	static int sequenceNum;

	
	public static void main(String[] args){
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
			portNum = 7735;
			filename = "F:\\rfc123.txt";
			windowSize = 16;
			MSS = 256;
			sequenceNum =0;
			client = new DatagramSocket();
			System.out.println("Connected to server");
			selectiveClient = new SelectiveARQClient();
			init();
			startTime = System.currentTimeMillis();
			rdt_send();			
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void rdt_send() {
		FileInputStream input = null;
		try {
			input = new FileInputStream(filename);
			byte[] buff = new byte[1];
			int res = 0;
			while(res!=-1){
				res = input.read(buff);
				selectiveARQ(buff, false);
			}
			//System.out.println("File Read");
			selectiveARQ(null, true);

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void selectiveARQ(byte[] buff, boolean lastFlag) {
		if(lastFlag == true && mssCount > 0){
			//send the last packet
			mssCount = 0;
			lastPktSent++;

			DataPacket data = new DataPacket(mssData);
			data.setSequenceNumber(sequenceNum);
			data.setChecksum(Utils.calcChecksum(data));
			sequenceNum = sequenceNum+MSS;
			window.add(data);
			
			if(window.size()<= windowSize){
				byte[] dataArr = Utils.serializePacket(data);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getByName(serverHostname);
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
					client.send(dataPacket);
					//TODO :: start the timer
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//send -1 
			mssData = new byte[0];
			data = new DataPacket(mssData);
			endAckExpected = sequenceNum+MSS;
			data.setSequenceNumber(sequenceNum);
			data.setChecksum(Utils.calcChecksum(data));
			sequenceNum = sequenceNum+MSS;
			window.add(data);
			
			if(window.size()<= windowSize){
				byte[] dataArr = Utils.serializePacket(data);

				try {
					InetAddress ipAddr = InetAddress.getByName(serverHostname);
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
					client.send(dataPacket);
					//TODO :: start the timer
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
		else{
			mssData[mssCount++] = buff[0];
			if(mssCount==MSS){
				DataPacket data = new DataPacket(mssData);
				data.setSequenceNumber(sequenceNum);
				data.setChecksum(Utils.calcChecksum(data));
				sequenceNum = sequenceNum+MSS;
				window.add(data);
				mssCount = 0;
				lastPktSent++;		
				
				if(window.size()<= windowSize){
					byte[] dataArr = Utils.serializePacket(data);
					InetAddress ipAddr;
					try {
						ipAddr = InetAddress.getByName(serverHostname);
						DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
						client.send(dataPacket);
						//TODO :: start the timer
						mssData = new byte[MSS];
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}			
				}
				/*if(lastPktSent==0){
					timerTask = new GoBackNTimerTask();
					timer = new Timer(true);
					timer.scheduleAtFixedRate(timerTask,1000, 1000);
				}*/
			}
		}
		
		
	}

	private static void init() {
		mssData = new byte[MSS];
		window = new ArrayList<DataPacket>();
		timerList = new ArrayList<SelectiveARQTimerTask>();
		mssCount = 0;
		lastAckRcvd = -1;
		lastPktSent = -1;
		firstPktInWindow = 0;
		endAckExpected = -1;

		Thread t = new Thread(selectiveClient);
		t.start();
		
	}
	
	/**
	 * Receive ACK packets from server
	 * check their sequence number
	 * slide the window
	 * cancel the timer
	 * 
	 */
	@Override
	public void run() {
		
		//TODO :: Receive AckPackets from server
	}
}
