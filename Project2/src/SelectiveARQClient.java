import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * @author Ritwikaroyc
 *
 */
class SelectiveARQWindow{
	Timer timer;
	DataPacket packet;
	Boolean ackRcvd;

	public SelectiveARQWindow(){
		this.ackRcvd = false;
		this.timer = new Timer();
	}
	public Timer getTimer() {
		return timer;
	}
	public void setTimer(Timer timer) {
		this.timer = timer;
	}
	public DataPacket getPacket() {
		return packet;
	}
	public void setPacket(DataPacket packet) {
		this.packet = packet;
	}
	public Boolean getAckRcvd() {
		return ackRcvd;
	}
	public void setAckRcvd(Boolean ackRcvd) {
		this.ackRcvd = ackRcvd;
	}

}

class SelectiveARQTimerTask extends TimerTask{
	int seqNum;
	public SelectiveARQTimerTask(int seqNum){
		this.seqNum = seqNum;
	}

	@Override
	public void run() {	
		System.out.println("Timeout, sequence number = "+ this.seqNum);
		if(SelectiveARQClient.window.containsKey(this.seqNum)){
			DataPacket packet = SelectiveARQClient.window.get(this.seqNum).getPacket();
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

	static Map<Integer, SelectiveARQWindow> window ;
	static byte[] mssData;
	static int mssCount;
	static int lastAckRcvd;
	//static int lastPktSent;
	static int endAckExpected;
	static int firstPktInWindow;
	static int lastPktInWindow;
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
			filename = "F:\\123.txt";
			windowSize = 256;
			MSS = 1000;
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
			System.out.println("File Read");
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
		if(lastFlag == true){
			System.out.println("last flag = true");
			if(mssCount>0){
				mssCount = 0;
				//lastPktSent++;
				DataPacket data = new DataPacket(mssData);
				data.setSequenceNumber(sequenceNum);
				data.setChecksum(Utils.calcChecksum(data));

				SelectiveARQWindow windowElement = new SelectiveARQWindow();
				windowElement.setPacket(data);


				sequenceNum = sequenceNum+MSS;
				window.put(data.getSequenceNumber(), windowElement);
				System.out.println("last pkt = " + data.getSequenceNumber() + " length = " + data.getData().length);
				if(window.size() < windowSize){
					byte[] dataArr = Utils.serializePacket(data);
					InetAddress ipAddr;
					try {
						ipAddr = InetAddress.getByName(serverHostname);
						DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
						client.send(dataPacket);
						SelectiveARQTimerTask timerTask = new SelectiveARQTimerTask(data.getSequenceNumber());
						windowElement.getTimer().scheduleAtFixedRate(timerTask, 50, 50);
						lastPktInWindow = data.getSequenceNumber();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			//send the last packet

			//send -1 
			mssData = new byte[0];
			DataPacket data = new DataPacket(mssData);
			endAckExpected = sequenceNum+MSS;
			data.setSequenceNumber(sequenceNum);
			data.setChecksum(Utils.calcChecksum(data));

			SelectiveARQWindow windowElement = new SelectiveARQWindow();
			windowElement.setPacket(data);

			sequenceNum = sequenceNum+MSS;
			window.put(data.getSequenceNumber(), windowElement);
			System.out.println("last empty pkt = " + data.getSequenceNumber()+ " length = " + data.getData().length);
			if(window.size() < windowSize){
				byte[] dataArr = Utils.serializePacket(data);
				InetAddress ipAddr;
				try {
					ipAddr = InetAddress.getByName(serverHostname);
					DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
					client.send(dataPacket);
					SelectiveARQTimerTask timerTask = new SelectiveARQTimerTask(data.getSequenceNumber());
					windowElement.getTimer().scheduleAtFixedRate(timerTask, 50, 50);
					lastPktInWindow = data.getSequenceNumber();
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
				Timer timer = new Timer();
				SelectiveARQWindow windowElement = new SelectiveARQWindow();
				windowElement.setPacket(data);
				windowElement.setTimer(timer);

				sequenceNum = sequenceNum+MSS;
				window.put(data.getSequenceNumber(), windowElement);
				mssCount = 0;
				//lastPktSent++;		
				mssData = new byte[MSS];

				if(window.size()<= windowSize){
					byte[] dataArr = Utils.serializePacket(data);
					InetAddress ipAddr;
					try {
						ipAddr = InetAddress.getByName(serverHostname);
						DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
						client.send(dataPacket);
						SelectiveARQTimerTask timerTask = new SelectiveARQTimerTask(data.getSequenceNumber());
						timer.scheduleAtFixedRate(timerTask, 50, 50);
						lastPktInWindow = data.getSequenceNumber();
					} catch (UnknownHostException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}			
				}

			}
		}


	}

	private static void init() {
		mssData = new byte[MSS];
		window = new HashMap<Integer, SelectiveARQWindow>();
		mssCount = 0;
		lastAckRcvd = -1;
		//lastPktSent = -1;
		firstPktInWindow = 0;
		endAckExpected = -1;
		lastPktInWindow = -1;

		Thread t = new Thread(selectiveClient);
		t.start();

	}

	@Override
	public void run() {
		try {
			int maxAckRcvd = 0;
			while(true){
				byte[] buffer = new byte[1000];
				DatagramPacket ackPacket = new DatagramPacket(buffer, buffer.length);
				client.receive(ackPacket);
				AckHeader ackData = (AckHeader) Utils.deserializePacket(ackPacket.getData());
				int ackRcvd = ackData.getSequenceNumber();
				if(ackRcvd > maxAckRcvd){
					maxAckRcvd = ackRcvd;
				}
				//				if(ackRcvd == endAckExpected){
				//					long endTime = System.currentTimeMillis();
				//					float time = (endTime - SimpleFTPClient.startTime);
				//					System.out.println("File transfer completed \nTime to transfer : "+(time/1000)+" Sec");					
				//					System.exit(1);
				//				}
				int ackPkt = ackRcvd-MSS;
				if(window.containsKey(ackPkt)){

					SelectiveARQWindow windowElement = window.get(ackPkt);
					if(!windowElement.getAckRcvd()){
						windowElement.setAckRcvd(true);
						windowElement.getTimer().cancel();
					}
					while(windowElement.getPacket().getSequenceNumber()==firstPktInWindow && windowElement.getAckRcvd()){
						window.remove(firstPktInWindow);
						firstPktInWindow+=MSS;

						if(window.containsKey(lastPktInWindow+MSS)){
							SelectiveARQWindow element = window.get(lastPktInWindow+MSS);
							DataPacket data = element.getPacket();
							byte[] dataArr = Utils.serializePacket(data);
							InetAddress ipAddr;
							try {
								ipAddr = InetAddress.getByName(serverHostname);
								DatagramPacket dataPacket = new DatagramPacket(dataArr, dataArr.length,ipAddr,portNum);
								client.send(dataPacket);
								SelectiveARQTimerTask timerTask = new SelectiveARQTimerTask(data.getSequenceNumber());
								element.getTimer().scheduleAtFixedRate(timerTask, 50, 50);
								lastPktInWindow = data.getSequenceNumber();
							} catch (UnknownHostException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}	
						}
						if(window.containsKey(firstPktInWindow)){
							windowElement = window.get(firstPktInWindow);
						}else{
							break;
						}
					}
				}
				if(window.size()==0 && maxAckRcvd==endAckExpected){
					long endTime = System.currentTimeMillis();
					float time = (endTime - SelectiveARQClient.startTime);
					System.out.println("File transfer completed \nTime to transfer : "+(time/1000)+" Sec");					
					System.exit(1);
				}


			}
		} catch (IOException e) {

			e.printStackTrace();
		}

	}
}
