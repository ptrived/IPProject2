import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * 
 * @author Ritwikaroyc
 * 
 * command to start the Server :
 * Simple_ftp_server server-port file-name p
 * file-name : name of the file where the data will be written
 * p : packet loss probability
 * 
 * what to print: whenever a packet with sequence number X is discarded by probabilistic loss service, the server
 * should print the following line:
 * Packet loss, Sequence number = x
 * 
 * server has to implement a probabilistic loss service 
 * after receiving a packet, server will generate a random number r
 * if r<=p then this packet is discarded
 * else packet is accepted and processed
 *  
 */

public class SimpleFTPServer {
	static DatagramSocket socket ;
	static int portNum = 7735;
	static String filename;
	static double probabilityFactor;

	static int nextSeqNum;

	private static double probabilisticLossService(){
		double num = 0;
		num = 0 + (double)(Math.random()*1);
		return num;
	}
	public static void main(String args[]){
		try {
			//int serverPort = Integer.parseInt(args[1]);	// this should always be 7735
			int serverPort = 7735;
			if(serverPort!=portNum){
				System.out.println("Entered port number is wrong");
				System.exit(1);
			}
			//			filename = args[2];
			//			probabilityFactor = Double.parseDouble(args[3]);
			//			if(probabilityFactor < 0 || probabilityFactor > 1){
			//				System.out.println("Probability Factor is not within the valid range[0-1]");
			//				System.exit(1);
			//			}
			filename = "F:\\124.txt";
			FileOutputStream output = new FileOutputStream(filename);
			
			probabilityFactor = 0.15;
			
			nextSeqNum = 0;
			if(probabilityFactor < 0 || probabilityFactor > 1){
				System.out.println("Probability Factor is not within the valid range[0-1]");
				System.exit(1);
			}
			socket = new DatagramSocket(portNum);
			System.out.println("Server is up");
			System.out.println(Utils.calcChecksum(null));
			while(true){
				int bufferSize = 1024;			//TODO ::  will have to modify it later based on client's MSS value
				byte[] buffer = new byte[bufferSize];
				InetAddress ipAddr = InetAddress.getLocalHost();
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
				socket.receive(packet);

				DataPacket data = (DataPacket) Utils.deserializePacket(packet.getData());
				double r = probabilisticLossService();
				System.out.println("loss factor = "+ r);
				if(r<=probabilityFactor){
					System.out.println("Packet loss, Sequence Number = "+ data.getSequenceNumber());
				}else{
					long checksum = Utils.calcChecksum(data.getData());					
					//TODO :: calculate checksum and compare

					int rcvdSeqNum = data.getSequenceNumber();
					System.out.println("Received Seq Num : "+rcvdSeqNum);
					if(rcvdSeqNum == nextSeqNum){
						output.write(data.getData());
						AckHeader ackData = new AckHeader();
						ackData.setSequenceNumber(++nextSeqNum);
						byte[] ack = Utils.serializeAck(ackData);

						DatagramPacket ackPacket = new DatagramPacket(ack, ack.length,packet.getAddress(),packet.getPort());
						socket.send(ackPacket);
						System.out.println("Server sent ack for " +nextSeqNum);
						if(data.getData().length==0){
							System.out.println("File copied to disk");
							System.exit(1);
						}
					}
					else{
						System.out.println("Next Sequence Number = " + nextSeqNum);
					}

				}

			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
