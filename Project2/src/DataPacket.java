import java.math.BigInteger;


public class DataPacket {
	byte[] sequenceNumber = new byte[4];
	byte[] checksum = new byte[2];
	byte[] type = new BigInteger("0101010101010101",2).toByteArray();
	byte[] data;
}
