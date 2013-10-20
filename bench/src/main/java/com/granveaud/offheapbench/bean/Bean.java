package com.granveaud.offheapbench.bean;

import com.granveaud.offheapbench.utils.StringUtils;
import com.sun.jna.Memory;
import net.openhft.lang.io.Bytes;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class Bean implements Externalizable {
    final static private Random rand = new Random();

    private int value1;
    private String value2;
    private byte[] value2Bytes;

    private int[] array1;
    private String[] array2;
    private byte[][] array2Bytes;

    private int hashValue;

    public Bean() {}

    // create a bean with random values
    public Bean(int maxStringLength, int maxArrayLength) {
        value1 = rand.nextInt();
        value2 = randomString(maxStringLength);
        value2Bytes = StringUtils.stringToUTF8Bytes(value2);

        array1 = new int[rand.nextInt(maxArrayLength) + 1];
        for (int i = 0; i < array1.length; i++) {
            array1[i] = rand.nextInt();
        }

        array2 = new String[rand.nextInt(maxArrayLength) + 1];
        for (int i = 0; i < array2.length; i++) {
            array2[i] = randomString(maxStringLength);
        }
        array2Bytes = new byte[array2.length][];
        for (int i = 0; i < array2.length; i++) {
            array2Bytes[i] = StringUtils.stringToUTF8Bytes(array2[i]);
        }
    }

    private String randomString(int maxLength) {
        int length = rand.nextInt(maxLength) + 1;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(Character.toChars(rand.nextInt(64) + 32));
        }

        return sb.toString();
    }

    public int getValue1() {
        return value1;
    }

    public void setValue1(int value1) {
        this.value1 = value1;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public int[] getArray1() {
        return array1;
    }

    public void setArray1(int[] array1) {
        this.array1 = array1;
    }

    public String[] getArray2() {
        return array2;
    }

    public void setArray2(String[] array2) {
        this.array2 = array2;
    }

    public static Random getRand() {
        return rand;
    }

    public int getHashValue() {
        return hashCode();
    }

    public void setHashValue(int hashValue) {
        this.hashValue = hashValue;
    }

    @Override
    public int hashCode() {
        int result = value1;
        result = 31 * result + (value2 != null ? value2.hashCode() : 0);
        result = 31 * result + (array1 != null ? Arrays.hashCode(array1) : 0);
        result = 31 * result + (array2 != null ? Arrays.hashCode(array2) : 0);
        return result;
    }

    /**
     * Serialization/deserialization
     */

    // Chronicle
    public int getChronicleLength() {
        int p = 0;
        p += 4; // value1
        p += 4 + value2Bytes.length; // value2
        p += 4 + 4 * array1.length; // array1
        p += 4; // array2.length;
        for (byte[] b : array2Bytes) {
            p += 4 + b.length;
        }

        return p;
    }

    public void write(Bytes out) {
        out.writeInt(value1);
        out.writeInt(value2Bytes.length);
        out.write(value2Bytes);

        out.writeInt(array1.length);
        for (int i = 0; i < array1.length; i++) {
            out.writeInt(array1[i]);
        }

        out.writeInt(array2Bytes.length);
        for (int i = 0; i < array2Bytes.length; i++) {
            out.writeInt(array2Bytes[i].length);
            out.write(array2Bytes[i]);
        }

        out.writeInt(getHashValue());
    }

    public void read(Bytes in) throws IllegalStateException {
        value1 = in.readInt();
        value2Bytes = new byte[in.readInt()];
        in.read(value2Bytes, 0, value2Bytes.length);
        value2 = StringUtils.utf8BytesToString(value2Bytes);

        array1 = new int[in.readInt()];
        for (int i = 0; i < array1.length; i++) {
            array1[i] = in.readInt();
        }

        array2Bytes = new byte[in.readInt()][];
        array2 = new String[array2Bytes.length];
        for (int i = 0; i < array2.length; i++) {
            array2Bytes[i] = new byte[in.readInt()];
            in.read(array2Bytes[i], 0, array2Bytes[i].length);
            array2[i] = StringUtils.utf8BytesToString(array2Bytes[i]);
        }

        hashValue = in.readInt();
    }

    // JNA
    public int getJNALength() {
        int p = 0;
        p += 4; // value1
        p += 4 + value2Bytes.length; // value2
        p += 4 + 4 * array1.length; // array1
        p += 4; // array2.length;
        for (byte[] b : array2Bytes) {
            p += 4 + b.length;
        }

        return p;
    }

    public void write(Memory m) {
        int p = 0;
        m.setInt(p, value1);
        p += 4;
        m.setInt(p, value2Bytes.length);
        p += 4;
        m.write(p, value2Bytes, 0, value2Bytes.length);
        p += value2Bytes.length;
        m.setInt(p, array1.length);
        p += 4;
        m.write(p, array1, 0, array1.length);
        p += array1.length * 4;
        m.setInt(p, array2Bytes.length);
        p += 4;
        for (byte[] b : array2Bytes) {
            m.setInt(p, b.length);
            p += 4;
            m.write(p, b, 0, b.length);
            p += b.length;
        }
    }

    public void read(Memory m) {
        int p = 0;
        value1 = m.getInt(p);
        p += 4;
        value2Bytes = new byte[m.getInt(p)];
        p += 4;
        m.read(p, value2Bytes, 0, value2Bytes.length);
        p += value2Bytes.length;
        value2 = StringUtils.utf8BytesToString(value2Bytes);
        array1 = new int[m.getInt(p)];
        p += 4;
        m.read(p, array1, 0, array1.length);
        p += array1.length * 4;
        array2Bytes = new byte[m.getInt(p)][];
        p += 4;
        array2 = new String[array2Bytes.length];
        for (int i = 0; i < array2Bytes.length; i++) {
            array2Bytes[i] = new byte[m.getInt(p)];
            p += 4;
            m.read(p, array2Bytes[i], 0, array2Bytes[i].length);
            p += array2Bytes[i].length;
            array2[i] = StringUtils.utf8BytesToString(array2Bytes[i]);
        }
    }

    // Externalizable

    @Override
    public void writeExternal(ObjectOutput oo) throws IOException {
        oo.writeInt(value1);
        oo.writeInt(value2Bytes.length);
        oo.write(value2Bytes);
        oo.writeInt(array1.length);
        for (int v : array1) {
            oo.writeInt(v);
        }

        oo.writeInt(array2Bytes.length);
        for (byte[] b : array2Bytes) {
            oo.writeInt(b.length);
            oo.write(b);
        }
    }

    @Override
    public void readExternal(ObjectInput oi) throws IOException, ClassNotFoundException {
        value1 = oi.readInt();
        value2Bytes = new byte[oi.readInt()];
        oi.read(value2Bytes, 0, value2Bytes.length);
        value2 = StringUtils.utf8BytesToString(value2Bytes);

        array1 = new int[oi.readInt()];
        for (int i = 0; i < array1.length; i++) {
            array1[i] = oi.readInt();
        }

        array2Bytes = new byte[oi.readInt()][];
        array2 = new String[array2Bytes.length];
        for (int i = 0; i < array2Bytes.length; i++) {
            array2Bytes[i] = new byte[oi.readInt()];
            oi.read(array2Bytes[i], 0, array2Bytes[i].length);
            array2[i] = StringUtils.utf8BytesToString(array2Bytes[i]);
        }
    }
}
