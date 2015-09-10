package drivers.mpop;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class CommandDataList extends ArrayList<Byte> {

	public CommandDataList add(int... arg) {
		for (int value : arg) {
			add((byte) value);
		}
		return this;
	}

	public CommandDataList add(byte[] arg) {
		for (byte value : arg) {
			add(value);
		}
		return this;
	}

	public CommandDataList add(String arg) {
		byte[] argByte = arg.getBytes();

		for (byte value : argByte) {
			add(value);
		}
		return this;
	}

	public byte[] getByteArray() {
		ByteBuffer output;

		output = ByteBuffer.allocate(this.size());

		for (Byte value : this) {
			output.put(value);
		}

		return output.array();
	}
}
