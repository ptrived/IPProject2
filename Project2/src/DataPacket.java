import java.io.Serializable;
import java.math.BigInteger;


@SuppressWarnings("serial")
public class DataPacket implements Serializable{
	int sequenceNumber ;
	byte[] checksum = new byte[2];
	byte[] type = new BigInteger("0101010101010101",2).toByteArray();
	byte[] data;
	public DataPacket(byte[] data) {
		//this.sequenceNumber = seqNum;
		this.data = data;
	}
	
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
	public byte[] gettype() {
		return type;
	}
	public void setType(byte[] type) {
		this.type = type;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] data) {
		this.data = data;
	}
}
