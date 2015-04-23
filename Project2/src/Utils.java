import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Utils {
	public static int calcChecksum(byte[] data){
		int result = 0;

		return result;
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