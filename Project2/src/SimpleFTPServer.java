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
			probabilityFactor = 0.05;
			if(probabilityFactor < 0 || probabilityFactor > 1){
				System.out.println("Probability Factor is not within the valid range[0-1]");
				System.exit(1);
			}
			socket = new DatagramSocket(portNum);
			System.out.println("Server is up");

			while(true){
				int bufferSize = 1024;			//TODO ::  will have to modify it later based on client's MSS value
				byte[] buffer = new byte[bufferSize];
				InetAddress ipAddr = InetAddress.getLocalHost();
				DatagramPacket packet = new DatagramPacket(buffer, buffer.length,ipAddr,portNum);
				socket.receive(packet);
				System.out.println("File receiving");
				System.out.println(new String(packet.getData()));
				double r = probabilisticLossService();
				if(r<=probabilityFactor){
					//TODO :: packet should be discarded
				}else{
					//TODO :: packet is accepted and processed
				}

			}
		} catch (Exception e) {

			e.printStackTrace();
		}
	}
}
