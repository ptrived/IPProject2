import java.io.Serializable;
import java.math.BigInteger;


public class AckHeader implements Serializable{
	int sequenceNumber;
	byte[] checksum = new BigInteger("0000000000000000",2).toByteArray();
	byte[] type = new BigInteger("1010101010101010",2).toByteArray();
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public byte[] getChecksum() {
		return checksum;
	}
	public void setChecksum(byte[] checksum) {
		this.checksum = checksum;
	}
	public byte[] getType() {
		return type;
	}
	
}
