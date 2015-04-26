import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;


/**
 * 
 * Main class for Go Back N Server
 * 
 * command to start the Server :
 * Simple_ftp_server server-port file-name p
 *  
 */

public class SimpleFTPServer {
	static DatagramSocket socket ;
	static int portNum = 7735;
	static String filename;
	static double probabilityFactor;

	static int nextSeqNum;
	static int MSS=0;

	private static double probabilisticLossService(){
		double num = 0;
		num = 0 + (double)(Math.random()*1);
		return num;
	}

	/**
	 * Command to run:
	 * SimpleFTPServer server-port file-name p
	 */
	public static void main(String args[]){
		FileOutputStream output = null;
		try {
			int serverPort = Integer.parseInt(args[0]);	// this should always be 7735
			if(serverPort!=portNum){
				System.out.println("Entered port number is wrong");
				System.exit(1);
			}
			filename = args[1];
			probabilityFactor = Double.parseDouble(args[2]);
			if(probabilityFactor < 0 || probabilityFactor > 1){
				System.out.println("Probability Factor is not within the valid range[0-1]");
				System.exit(1);
			}
			//			int serverPort = 7735;
			//			filename = "124.txt";
			//			probabilityFactor = 0.25;
			output = new FileOutputStream(new File(filename),true);
			nextSeqNum = 0;
			if(probabilityFactor < 0 || probabilityFactor > 1){
				System.out.println("Probability Factor is not within the valid range[0-1]");
				System.exit(1);
			}
			socket = new DatagramSocket(portNum);
			System.out.println("Server is up");
			int bufferSize = 65536;
			while(true){				
				output = new FileOutputStream(new File(filename),true);
				byte[] buffer = new byte[bufferSize];
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);
				if(MSS==0){
					MSS = packet.getLength()-153;
					//bufferSize = packet.getLength();
				}
				DataPacket data = (DataPacket) Utils.deserializePacket(packet.getData());
				double r = probabilisticLossService();
				if(r<=probabilityFactor){
					System.out.println("Packet loss, Sequence Number = "+ data.getSequenceNumber());
				}else{	
					int rcvdSeqNum = data.getSequenceNumber();
					if(rcvdSeqNum == nextSeqNum){									
						if(data.getData().length==0){
							nextSeqNum=nextSeqNum+MSS;
							AckHeader ackData = new AckHeader();
							ackData.setSequenceNumber(nextSeqNum);
							byte[] ack = Utils.serializeAck(ackData);
							DatagramPacket ackPacket = new DatagramPacket(ack, ack.length,packet.getAddress(),packet.getPort());
							socket.send(ackPacket);
							//System.out.println("Rcvd : " + rcvdSeqNum+" Ack for : " +nextSeqNum);
							System.out.println("File copied to disk");	
							System.exit(1);
						}
						if(verifyCheckSum(data)==1){
							output.write(data.getData());
							nextSeqNum=nextSeqNum+MSS;
						}
					}
					AckHeader ackData = new AckHeader();
					ackData.setSequenceNumber(nextSeqNum);
					byte[] ack = Utils.serializeAck(ackData);
					DatagramPacket ackPacket = new DatagramPacket(ack, ack.length,packet.getAddress(),packet.getPort());
					socket.send(ackPacket);
					//System.out.println("Rcvd : " + rcvdSeqNum+" Ack for : " +nextSeqNum);

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
	
	/*
	 * Verify the checksum of incoming packets
	 */
	private static int verifyCheckSum(DataPacket data) {
		byte[] calcChecksum = new byte[2];
		calcChecksum = Utils.calcChecksum(data);
		if(Arrays.equals(data.getChecksum(),calcChecksum)){
			return 1;
		}
		return 0;
	}
}