import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/*class SimpleClientWindow{
	DataPacket data;
	Boolean ackRcvd;
	public SimpleClientWindow(DataPacket data){
		this.data =  data;
		ackRcvd = false;
	}
	public DataPacket getData() {
		return data;
	}
	public void setData(DataPacket data) {
		this.data = data;
	}
	public Boolean getAckRcvd() {
		return ackRcvd;
	}
	public void setAckRcvd(Boolean ackRcvd) {
		this.ackRcvd = ackRcvd;
	}

}*/

class GoBackNTimerTaskNew extends TimerTask{
	@Override
	public void run() {	
		synchronized(Client.window){
			System.out.println("Timeout, sequence number = "+Client.window.firstKey());
			int num = Client.window.firstKey();
			int count = 0;
			while(count < Client.windowSize && Client.window.containsKey(num)){
				DataPacket packet = Client.window.get(num);
				byte[] dataArr = Utils.serializePacket(packet);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getByName(Client.serverHostname);
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,Client.portNum);
					Client.client.send(dataPacket);		
					num += Client.MSS;
					count++;
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			/*Iterator<Entry<Integer, SimpleClientWindow>> it = Client.window.entrySet().iterator();
			while(it.hasNext() && count < Client.windowSize){
				Entry<Integer, SimpleClientWindow> entry = it.next();
				SimpleClientWindow window = entry.getValue();
				DataPacket packet = window.getData();
				byte[] dataArr = Utils.serializePacket(packet);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getByName(Client.serverHostname);
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,Client.portNum);
					Client.client.send(dataPacket);		
					//num += Client.MSS;
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}*/
		}
	}
}

public class Client implements Runnable {
	static int portNum=7735;
	static String serverHostname;
	static DatagramSocket client;
	static Client simpleClient;
	static String filename;
	static int windowSize ;
	static int MSS;
	static TreeMap<Integer, DataPacket> window ;
	static byte[] mssData;
	static int mssCount;
	static int lastAckRcvd;
	static int lastPktSent;
	static int endAckExpected;
	static int firstPktInWindow;
	static long startTime;
	static int sequenceNum;
	static TimerTask timerTask ;
	static Timer timer;

	/**
	 * Initialize variables
	 */
	private static void init() {
		mssData = new byte[MSS];
		window = new TreeMap<Integer, DataPacket>();
		mssCount = 0;
		lastAckRcvd = -1;
		lastPktSent = -1;
		firstPktInWindow = 0;
		endAckExpected = -1;
		Thread t = new Thread(simpleClient);
		t.start();
	}

	public static void main(String[] args) {
		try{
			serverHostname = args[0];
			int portInput = Integer.parseInt(args[1]);
			if(portInput!=portNum){
				System.out.println("Invalid Server Port Number");
				System.exit(1);
			}
			filename = args[2];
			windowSize = Integer.parseInt(args[3]);
			MSS = Integer.parseInt(args[4]);
			//			serverHostname = "localhost";
			//			portNum = 7735;
			//			filename = "123.txt";
			//			windowSize = 16;
			//			MSS = 1000;
			sequenceNum =0;
			client = new DatagramSocket();
			System.out.println("Connected to server");
			simpleClient = new Client();
			init();
			startTime = System.currentTimeMillis();
			rdt_send();			
		}catch(Exception e){
			e.printStackTrace();
		}

	}



	private static void rdt_send(){
		FileInputStream input = null;
		try {
			input = new FileInputStream(filename);
			byte[] buff = new byte[1];
			int res = 0;
			while(res!=-1){
				res = input.read(buff);
				goBackN(buff, false);
			}
			goBackN(null, true);

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


	private static void goBackN(byte[] buff, boolean lastFlag) {

		if(lastFlag == true && mssCount > 0){
			//send the last packet
			mssCount = 0;
			lastPktSent++;

			DataPacket data = new DataPacket(mssData);
			data.setSequenceNumber(sequenceNum);
			window.put(sequenceNum, data);
			data.setChecksum(Utils.calcChecksum(data));
			sequenceNum = sequenceNum+MSS;
			
			
			//window.add(data);
			if(window.size() < windowSize){
				byte[] dataArr = Utils.serializePacket(data);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getByName(serverHostname);
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
					client.send(dataPacket);
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			//send Last packet with data 0
			mssData = new byte[0];
			data = new DataPacket(mssData);
			endAckExpected = sequenceNum+MSS;
			data.setSequenceNumber(sequenceNum);
			data.setChecksum(Utils.calcChecksum(data));
			window.put(sequenceNum, data);
			sequenceNum = sequenceNum+MSS;
			//element = new SimpleClientWindow(data);
			
			if(window.size() < windowSize){
				byte[] dataArr = Utils.serializePacket(data);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getByName(serverHostname);
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
					client.send(dataPacket);
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
				window.put(sequenceNum, data);
				sequenceNum = sequenceNum+MSS;
				//SimpleClientWindow element = new SimpleClientWindow(data);
				
				mssCount = 0;
				lastPktSent++;		
				mssData = new byte[MSS];
				if(window.size()<= windowSize){
					byte[] dataArr = Utils.serializePacket(data);
					InetAddress ipAddr;
					try {
						ipAddr = InetAddress.getByName(serverHostname);
						DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
						client.send(dataPacket);
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}			
				}
				if(lastPktSent==0){
					timerTask = new GoBackNTimerTaskNew();
					timer = new Timer(true);
					timer.scheduleAtFixedRate(timerTask,100, 100);
				}
			}
		}
		//return 1;


	}

	@Override
	public void run() {

		try {
			while(true){
				byte[] buffer = new byte[1000];
				DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length);
				client.receive(ackPacket);
				AckHeader ackData = (AckHeader) Utils.deserializePacket(ackPacket.getData());
				int ackRcvd = ackData.getSequenceNumber();
				//System.out.println("Recv ack : " + ackRcvd + " last ack rcvd :" + lastAckRcvd + " firstpkt :" + firstPktInWindow  + " window size :" + window.size() );
				if(ackRcvd == endAckExpected ){
					long endTime = System.currentTimeMillis();
					float time = (endTime - Client.startTime);
					System.out.println("File transfer completed \nTime to transfer : "+(time/1000)+" Sec");					
					System.exit(1);
				}
				
				if(ackRcvd > window.firstKey()){
					lastAckRcvd = ackRcvd;
					timer.cancel();
					synchronized (window) {
						while(firstPktInWindow < lastAckRcvd && window.size()>0){

							window.remove(firstPktInWindow);
							firstPktInWindow = window.firstKey();

						}
					}
					timerTask = new GoBackNTimerTaskNew();
					timer = new Timer(true);
					timer.scheduleAtFixedRate(timerTask, 100, 100);
				}
				/*if(ackRcvd > lastAckRcvd){
					lastAckRcvd = ackRcvd;
					timer.cancel();
					//slide the window
					synchronized (window) {
						while(firstPktInWindow < lastAckRcvd && window.size()>0){

							window.remove(firstPktInWindow);
							firstPktInWindow = window.firstKey();
							//firstPktInWindow+=MSS;

						}
					}
					timerTask = new GoBackNTimerTaskNew();
					timer = new Timer(true);
					timer.scheduleAtFixedRate(timerTask, 100, 100);
				}*/
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
