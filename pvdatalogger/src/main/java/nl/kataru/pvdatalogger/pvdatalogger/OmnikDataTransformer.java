package nl.kataru.pvdatalogger.pvdatalogger;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author morten
 */
public class OmnikDataTransformer {
    private final DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");

    public Map<String, String> transform(byte[] rawData) {
        validateFrameStart(rawData[0]);
        final int messageVersion = rawData[1] & 0xFF;
        validateMessageVersion(messageVersion);
        validateFrameSize(messageVersion, rawData.length);
        validateDataLoggerSerialNumber(rawData, messageVersion);

        final int offset = determineOffset(messageVersion);

        final Map<String, String> result = new HashMap<>();
        final ByteBuffer byteBuffer = ByteBuffer.wrap(rawData);    // Default byte order = big endian
        final Date now = new Date();

        result.put("timestamp", isoDateFormat.format(now));
        result.put("datalogger_sn", "" + transformIntegerLittleEndian(byteBuffer, 4, 1));
        result.put("inverter_sn", transformToString(byteBuffer, 15, 16));
        result.put("temp", "" + transformShort(byteBuffer, 31, 10));
        result.put("vpv1", "" + transformShort(byteBuffer, 33, 10));
        result.put("vpv2", "" + transformShort(byteBuffer, 35, 10));
        result.put("vpv3", "" + transformShort(byteBuffer, 37, 10));
        result.put("ipv1", "" + transformShort(byteBuffer, 39, 10));
        result.put("ipv2", "" + transformShort(byteBuffer, 41, 10));
        result.put("ipv3", "" + transformShort(byteBuffer, 43, 10));
        result.put("iac1", "" + transformShort(byteBuffer, 45, 10));
        result.put("iac2", "" + transformShort(byteBuffer, 47, 10));
        result.put("iac3", "" + transformShort(byteBuffer, 49, 10));
        result.put("vac1", "" + transformShort(byteBuffer, 51, 10));
        result.put("vac2", "" + transformShort(byteBuffer, 53, 10));
        result.put("vac3", "" + transformShort(byteBuffer, 55, 10));
        result.put("fac1", "" + transformShort(byteBuffer, 57, 100));
        result.put("pac1", "" + transformShort(byteBuffer, 59, 1));    // Do not divide pac value
        result.put("fac2", "" + transformShort(byteBuffer, 61, 100));
        result.put("pac2", "" + transformShort(byteBuffer, 63, 1));    // Do not divide pac value
        result.put("fac3", "" + transformShort(byteBuffer, 65, 100));
        result.put("pac3", "" + transformShort(byteBuffer, 67, 1));    // Do not divide pac value
        result.put("yield_today", "" + transformShort(byteBuffer, 69, 100));
        result.put("yield_total", "" + transformInteger(byteBuffer, 71, 10));
        result.put("hours_total", "" + transformInteger(byteBuffer, 75, 1));

        result.put("firmware_version_main", transformToString(byteBuffer, 97 + offset, 15));
        result.put("firmware_version_slave", transformToString(byteBuffer, 117 + offset, 9));
        result.put("message", transformToString(byteBuffer, 151 + offset, 17));
        result.put("rawdata", javax.xml.bind.DatatypeConverter.printHexBinary(rawData));

        return result;
    }

    private void validateFrameStart(byte frameStart) {
        if (frameStart != 0x68) {
            throw new IllegalArgumentException("Input data does not start with 0x68");
        }
    }

    private void validateMessageVersion(int messageVersion) {
        if (messageVersion != 0x7D && messageVersion != 0x81) {
            throw new IllegalArgumentException("Unknown message version received: 0x" + Integer.toHexString(messageVersion));
        }
    }

    private void validateFrameSize(int messageVersion, int frameLength) {
        int expectedFrameSize = -1;
        if (messageVersion == 0x7D) {
            expectedFrameSize = 170;
        }
        if (messageVersion == 0x81) {
            expectedFrameSize = 174;
        }

        if (frameLength != expectedFrameSize) {
            throw new IllegalArgumentException("Input data does not contain " + expectedFrameSize + " bytes, actual size is " + frameLength);
        }
    }

    private void validateDataLoggerSerialNumber(byte[] rawData, int messageVersion)
    {
        int offset = determineOffset(messageVersion);
        ByteBuffer byteBuffer = ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN);
        int sn1 = byteBuffer.getInt(4);
        int sn2 = byteBuffer.getInt(8);
        int sn3 = byteBuffer.getInt(143 + offset);
        int sn4 = byteBuffer.getInt(147 + offset);
        if (!allEqual(sn1, sn2, sn3, sn4))
        {
            throw new IllegalArgumentException("Input data does not contain correct data logger serial number information.");
        }
    }

    private boolean allEqual(int sn1, int sn2, int sn3, int sn4)
    {
        return sn1 == sn2 && sn2 == sn3 && sn3 == sn4;
    }

    private int determineOffset(int messageVersion) {
        int offset = 0;
        if (messageVersion == 0x81) {
            offset = 4;
        }
        return offset;
    }

    private BigDecimal transformShort(ByteBuffer byteBuffer, int offset, int divisor) {
        final short value = byteBuffer.getShort(offset);
        if (value == -1) {
            return new BigDecimal(value);
        } else {
            return new BigDecimal(value).divide(new BigDecimal(divisor));
        }
    }

    private BigDecimal transformInteger(ByteBuffer byteBuffer, int offset, int divisor) {
        final int value = byteBuffer.getInt(offset);

        if (value == -1) {
            return new BigDecimal(value);
        } else {
            return new BigDecimal(value).divide(new BigDecimal(divisor));
        }
    }

    private BigDecimal transformIntegerLittleEndian(ByteBuffer byteBuffer, int offset, int divisor)
    {
        final int value = byteBuffer.getInt(offset);
        final int littleEndianValue = Integer.reverseBytes(value);

        if (value == -1)
        {
            return new BigDecimal(littleEndianValue);
        } else
        {
            return new BigDecimal(littleEndianValue).divide(new BigDecimal(divisor));
        }
    }

    private String transformToString(ByteBuffer byteBuffer, int offset, int size) {
        final byte[] byteData = new byte[size];

        for (int index = 0; index < size; index++) {
            byteData[index] = byteBuffer.get(offset + index);
        }

        return new String(byteData);
    }

}
