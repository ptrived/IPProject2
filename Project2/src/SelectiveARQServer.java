import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class SelectiveARQServer {
	static DatagramSocket socket ;
	static int portNum = 7735;
	static String filename;
	static double probabilityFactor;

	static int nextSeqNum;
	static int MSS=0;
	static int windowSize;
	static Map<Integer, DataPacket> window;

	private static double probabilisticLossService(){
		double num = 0;
		num = 0 + (double)(Math.random()*1);
		return num;
	}

	public static void main(String[] args) {

		FileOutputStream output = null;
		try {
			//int serverPort = Integer.parseInt(args[0]);	// this should always be 7735
			int serverPort = 7735;
			/*if(serverPort!=portNum){
				System.out.println("Entered port number is wrong");
				System.exit(1);
			}
			filename = args[1];
			probabilityFactor = Double.parseDouble(args[2]);
			if(probabilityFactor < 0 || probabilityFactor > 1){
				System.out.println("Probability Factor is not within the valid range[0-1]");
				System.exit(1);
			}*/
			filename = "F:\\124.txt";
			probabilityFactor = 0.5;
			output = new FileOutputStream(filename);
			nextSeqNum = 0;
			windowSize = 4;
			if(probabilityFactor < 0 || probabilityFactor > 1){
				System.out.println("Probability Factor is not within the valid range[0-1]");
				System.exit(1);
			}

			init();

			socket = new DatagramSocket(portNum);
			System.out.println("Server is up");
			while(true){
				int bufferSize = 4096;
				byte[] buffer = new byte[bufferSize];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				if(MSS==0){
					MSS = packet.getLength()-153;
				}
				DataPacket data = (DataPacket) Utils.deserializePacket(packet.getData());
				double r = probabilisticLossService();
				if(r<=probabilityFactor){
					System.out.println("Packet loss, Sequence Number = "+ data.getSequenceNumber());
				}else{	
					int rcvdSeqNum = data.getSequenceNumber();
					if(verifyCheckSum(data)==1){
						window.put(rcvdSeqNum, data);
						AckHeader ackData = new AckHeader();
						ackData.setSequenceNumber(rcvdSeqNum+MSS);
						byte[] ack = Utils.serializeAck(ackData);
						DatagramPacket ackPacket = new DatagramPacket(ack, ack.length,packet.getAddress(),packet.getPort());
						socket.send(ackPacket);
						System.out.println("Rcvd : " + rcvdSeqNum+" Ack for : " +(rcvdSeqNum+MSS));
						if(rcvdSeqNum == nextSeqNum){
							while(window.containsKey(nextSeqNum)){
								data = window.get(nextSeqNum);
								if(data.getData().length==0){
									System.out.println("File copied ");
									System.exit(1);
								}
								output.write(data.getData());
								window.remove(nextSeqNum);
								nextSeqNum+=MSS;
							}
						}
						
						
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				output.close();
			} catch (IOException e){
				e.printStackTrace();
			}
		}

	}
	private static void init() {
		window = new HashMap<Integer, DataPacket>();

	}

	private static int verifyCheckSum(DataPacket data) {
		byte[] calcChecksum = new byte[2];
		calcChecksum = Utils.calcChecksum(data);
		//System.out.println("RcvdCheckSum = "+rcvdCheckSum+" CalcCheckSum = "+calcCheckSum);
		if(Arrays.equals(data.getChecksum(),calcChecksum)){
			//System.out.println("CheckSum Equals");
			return 1;
		}
		return 0;
	}

}
