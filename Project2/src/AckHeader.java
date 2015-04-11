import java.math.BigInteger;


public class AckHeader {
	byte[] sequenceNumber = new byte[4];
	byte[] checksum = new BigInteger("0000000000000000",2).toByteArray();
	byte[] type = new BigInteger("1010101010101010",2).toByteArray();
}
