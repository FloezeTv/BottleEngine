package tv.floeze.bottleengine.common.networking.packets;

import java.nio.charset.StandardCharsets;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * A {@link RawPacket} that carries the raw bytes of a {@link Packet}
 * 
 * @author Floeze
 *
 */
public class RawPacket {

	/**
	 * The header of the {@link Packet} this {@link RawPacket} represents
	 */
	private final int header;
	/**
	 * The raw data of the {@link Packet} this {@link RawPacket} represents
	 */
	private final ByteBuf data;

	/**
	 * Creates a new {@link RawPacket} for the given {@code header} and
	 * {@code version} and initializes an empty {@link #data}-Buffer
	 * 
	 * @param header header of the {@link Packet} this {@link RawPacket} represents
	 */
	public RawPacket(int header) {
		this.header = header;
		this.data = Unpooled.buffer();
	}

	/**
	 * Computes the hash of the data in this {@link RawPacket}.<br />
	 * Two {@link RawPacket}s having the same hash does not guarantee that the data
	 * of the two packets is the same, but having different hashes guarantees that
	 * the data is different.
	 * 
	 * @return the hash of the data in this {@link RawPacket}
	 */
	public long computeHash() {
		long hash = 0;
		for (int i = 0; i < data.readableBytes(); i++)
			hash += data.getByte(i);
		return hash;
	}

	/**
	 * Gets the length of the data of this {@link RawPacket} in bytes
	 * 
	 * @return the length of the data of this {@link RawPacket} in bytes
	 */
	public int byteLength() {
		return data.readableBytes();
	}

	/**
	 * Writes a {@link String} into the data of this {@link RawPacket}
	 * 
	 * @param string the {@link String} to write
	 * 
	 * @see #readString()
	 */
	public void writeString(CharSequence string) {
		data.writeInt(string.length());
		data.writeCharSequence(string, StandardCharsets.UTF_8);
	}

	/**
	 * Reads a {@link String} into the data of this {@link RawPacket}
	 * 
	 * @return the read {@link String}
	 * 
	 * @see #writeString(CharSequence)
	 */
	public String readString() {
		int length = data.readInt();
		return data.readCharSequence(length, StandardCharsets.UTF_8).toString();
	}

	/**
	 * Writes an array of objects into the data of this {@link RawPacket}
	 * 
	 * @param <T>         the type of the object
	 * @param array       the array to write
	 * @param valueWriter a function that writes a single object into the given
	 *                    {@link ByteBuf}
	 * 
	 * @see #readArray(Function, IntFunction)
	 */
	public <T> void writeArray(T[] array, BiConsumer<ByteBuf, T> valueWriter) {
		data.writeInt(array.length);
		for (T t : array)
			valueWriter.accept(data, t);
	}

	/**
	 * Writes an array of objects into the data of this {@link RawPacket}<br />
	 * This can be used with methods such as {@link #writeString(CharSequence)}.
	 * 
	 * @param <T>         the type of the object
	 * @param array       the array to write
	 * @param valueWriter a function that writes a single object
	 * 
	 * @see #readArray(Supplier, IntFunction)
	 */
	public <T> void writeArray(T[] array, Consumer<T> valueWriter) {
		writeArray(array, (b, v) -> valueWriter.accept(v));
	}

	/**
	 * Reads an array of objects from the data of this {@link RawPacket}
	 * 
	 * @param <T>            the type of the object
	 * @param valueReader    a function that reads a single object from the given
	 *                       {@link ByteBuf}
	 * @param arrayGenerator a function that creates an array of the objects
	 * @return the read array of objects
	 * 
	 * @see #writeArray(Object[], BiConsumer)
	 */
	public <T> T[] readArray(Function<ByteBuf, T> valueReader, IntFunction<T[]> arrayGenerator) {
		int length = data.readInt();
		T[] array = arrayGenerator.apply(length);
		for (int i = 0; i < length; i++)
			array[i] = valueReader.apply(data);
		return array;
	}

	/**
	 * Reads an array of objects from the data of this {@link RawPacket}<br />
	 * This can be used with methods such as {@link #readString()}.
	 * 
	 * @param <T>            the type of the object
	 * @param valueReader    a function that reads a single object
	 * @param arrayGenerator a function that creates an array of the objects
	 * @return the read array of objects
	 * 
	 * @see #writeArray(Object[], Consumer)
	 */
	public <T> T[] readArray(Supplier<T> valueReader, IntFunction<T[]> arrayGenerator) {
		return readArray(b -> valueReader.get(), arrayGenerator);
	}

	/**
	 * Writes a byte array into the data of this {@link RawPacket}
	 * 
	 * @param array the byte array to write
	 * 
	 * @see #readByteArray()
	 */
	public void writeByteArray(byte[] array) {
		data.writeInt(array.length);
		for (byte v : array)
			data.writeByte(v);
	}

	/**
	 * Reads a byte array from the data of this {@link RawPacket}
	 * 
	 * @return the read byte array
	 * 
	 * @see #writeByteArray(byte[])
	 */
	public byte[] readByteArray() {
		int length = data.readInt();
		byte[] array = new byte[length];
		for (int i = 0; i < length; i++)
			array[i] = data.readByte();
		return array;
	}

	/**
	 * Writes a short array into the data of this {@link RawPacket}
	 * 
	 * @param array the short array to write
	 * 
	 * @see #readShortArray()
	 */
	public void writeShortArray(short[] array) {
		data.writeInt(array.length);
		for (short v : array)
			data.writeShort(v);
	}

	/**
	 * Reads a short array from the data of this {@link RawPacket}
	 * 
	 * @return the read short array
	 * 
	 * @see #writeShortArray(byte[])
	 */
	public short[] readShortArray() {
		int length = data.readInt();
		short[] array = new short[length];
		for (int i = 0; i < length; i++)
			array[i] = data.readShort();
		return array;
	}

	/**
	 * Writes a char array into the data of this {@link RawPacket}
	 * 
	 * @param array the char array to write
	 * 
	 * @see #readCharArray()
	 */
	public void writeCharArray(char[] array) {
		data.writeInt(array.length);
		for (char v : array)
			data.writeChar(v);
	}

	/**
	 * Reads a char array from the data of this {@link RawPacket}
	 * 
	 * @return the read char array
	 * 
	 * @see #writeCharArray(byte[])
	 */
	public char[] readCharArray() {
		int length = data.readInt();
		char[] array = new char[length];
		for (int i = 0; i < length; i++)
			array[i] = data.readChar();
		return array;
	}

	/**
	 * Writes an int array into the data of this {@link RawPacket}
	 * 
	 * @param array the int array to write
	 * 
	 * @see #readIntArray()
	 */
	public void writeIntArray(int[] array) {
		data.writeInt(array.length);
		for (int v : array)
			data.writeInt(v);
	}

	/**
	 * Reads an int array from the data of this {@link RawPacket}
	 * 
	 * @return the read int array
	 * 
	 * @see #writeIntArray(byte[])
	 */
	public int[] readIntArray() {
		int length = data.readInt();
		int[] array = new int[length];
		for (int i = 0; i < length; i++)
			array[i] = data.readInt();
		return array;
	}

	/**
	 * Writes a long array into the data of this {@link RawPacket}
	 * 
	 * @param array the long array to write
	 * 
	 * @see #readLongArray()
	 */
	public void writeLongArray(long[] array) {
		data.writeInt(array.length);
		for (long v : array)
			data.writeLong(v);
	}

	/**
	 * Reads a long array from the data of this {@link RawPacket}
	 * 
	 * @return the read long array
	 * 
	 * @see #writeLongArray(byte[])
	 */
	public long[] readLongArray() {
		int length = data.readInt();
		long[] array = new long[length];
		for (int i = 0; i < length; i++)
			array[i] = data.readLong();
		return array;
	}

	/**
	 * Writes a String array into the data of this {@link RawPacket}
	 * 
	 * @param array the String array to write
	 * 
	 * @see #readStringArray()
	 */
	public void writeStringArray(String[] array) {
		writeArray(array, this::writeString);
	}

	/**
	 * Reads a string array from the data of this {@link RawPacket}
	 * 
	 * @return the read stirng array
	 * 
	 * @see #writeStringArray(byte[])
	 */
	public String[] readStringArray() {
		return readArray(this::readString, String[]::new);
	}

	/**
	 * Gets the header of the {@link Packet} this {@link RawPacket} represents
	 * 
	 * @return the header
	 */
	public int getHeader() {
		return header;
	}

	/**
	 * Gets the data of the {@link Packet} this {@link RawPacket} represents.<br />
	 * This can be written to when encoding the {@link Packet} and read form when
	 * decoding the {@link Packet}
	 * 
	 * @return the data
	 */
	public ByteBuf getData() {
		return data;
	}

	@Override
	public String toString() {
		return "RawPacket [header=" + header + ", data=" + data.readableBytes() + "B]";
	}

}
