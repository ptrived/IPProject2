import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;


public class Utils {
	public static long calcChecksum(byte[] buff){
	    byte[] buf = { (byte) 0xed, 0x2A, 0x44, 0x10, 0x03, 0x30};
	    int length = buf.length;
	    int i = 0;

	    long sum = 0;
	    long data = 0;
	    while (length > 1) {
	        data = 0;
	        data = (((buf[i]) << 8) | ((buf[i + 1]) & 0xFF));

	        sum += data;
	        if ((sum & 0xFFFF0000) > 0) {
	            sum = sum & 0xFFFF;
	            sum += 1;
	        }

	        i += 2;
	        length -= 2;
	    }

	    if (length > 0) {
	        sum += (buf[i] << 8);
	        // sum += buffer[i];
	        if ((sum & 0xFFFF0000) > 0) {
	            sum = sum & 0xFFFF;
	            sum += 1;
	        }
	    }
	    sum = ~sum;
	    sum = sum & 0xFFFF;
	    return sum;
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