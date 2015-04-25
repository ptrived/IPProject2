import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utils {
	public static byte[] calcChecksum(DataPacket packet){
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(packet.getData());
			md.update(packet.gettype());
			md.update(ByteBuffer.allocate(4).putInt(packet.getSequenceNumber()).array());
			
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Exception while calculating checksum :"+e.getMessage());
		}
		return md.digest();
	}

	public static Object deserializePacket(byte[] packetData)
	{
		try
		{
			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(packetData));
			Object obj = (Object) in.readObject();
			in.close();
			return obj;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] serializePacket(DataPacket packet)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
			ObjectOutputStream outs = new ObjectOutputStream(os);
			outs.writeObject(packet);
			outs.close();
			byte[] byte_arr= os.toByteArray();
			os.close();
			return byte_arr;
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	public static byte[] serializeAck(AckHeader ack)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream(2048);
			ObjectOutputStream outs = new ObjectOutputStream(os);
			outs.writeObject(ack);
			outs.close();
			byte[] byte_arr= os.toByteArray();
			os.close();
			return byte_arr;
		}
		catch(Exception e) {
			e.printStackTrace();
		}

		return null;

	}
}