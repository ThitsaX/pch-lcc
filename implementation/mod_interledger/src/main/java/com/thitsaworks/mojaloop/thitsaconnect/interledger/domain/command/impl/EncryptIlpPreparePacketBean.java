package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.impl;

import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.SDKPacketType;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.BuildPrepareIlpPacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.EncryptIlpPreparePacket;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPacketType;
import org.interledger.core.InterledgerPreparePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Component
public class EncryptIlpPreparePacketBean implements EncryptIlpPreparePacket {

    @Autowired
    private BuildPrepareIlpPacket buildPrepareIlpPacket;

    private static final Logger LOG = LoggerFactory.getLogger(BuildPrepareIlpPacket.class);

    @Override
    public Output execute(Input input) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        BuildPrepareIlpPacket.Output buildPrepareIlpPacketOutput =
                this.buildPrepareIlpPacket.execute(new BuildPrepareIlpPacket.Input(input.getInterledgerData()));

        InterledgerPreparePacket ilpPreparePacket = buildPrepareIlpPacketOutput.getInterledgerPreparePacket();
        InterledgerFulfillment fulfillment = buildPrepareIlpPacketOutput.getInterledgerFulfillment();

        //Serialize content data stream
        ByteArrayOutputStream contentOutputStream = new ByteArrayOutputStream();

        //write Amount
        byte[] byteArray = new BigInteger(ilpPreparePacket.getAmount().toString()).toByteArray();

        byte[] paddedByteArray = new byte[8];
        int paddingLength = Math.max(0, 8 - byteArray.length);
        System.arraycopy(byteArray, 0, paddedByteArray, paddingLength, byteArray.length);

        contentOutputStream.writeBytes(paddedByteArray);

        //write Account
        writeByteData(ilpPreparePacket.getDestination().getValue().getBytes(), contentOutputStream);

        //write Data
        writeByteData(ilpPreparePacket.getData(), contentOutputStream);

        //Write Condition
        writeByteData(ilpPreparePacket.getExecutionCondition().getHash(), contentOutputStream);

        //write Expiration
        DateTimeFormatter mojaloopDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        ZonedDateTime UTCDateTime = ZonedDateTime.ofInstant(ilpPreparePacket.getExpiresAt(), ZoneId.of("UTC"));
        String expireAt = mojaloopDateFormatter.format(UTCDateTime);
        writeByteData(expireAt.getBytes(StandardCharsets.UTF_8), contentOutputStream);

        byte[] contentBuffer = contentOutputStream.toByteArray();
        contentOutputStream.close();

        //serialize Ilp data stream (type + content)
        ByteArrayOutputStream ilpOutputStream = new ByteArrayOutputStream();

        //write Ilp Packet Type

//       short typeData = ilpPreparePacket.typedData().isPresent() ?
//                Short.parseShort((ilpPreparePacket.typedData().get().toString())) :
//                InterledgerPacketType.PREPARE_CODE;

        //FIXME : type:1 (ILP_PAYMENT) is used instead of type:12 (ILP_PREPARE)
        short typeData = SDKPacketType.TYPE_ILP_PAYMENT.getCode().shortValue();

        ilpOutputStream.write(typeData);

        //write content data
        writeByteData(contentBuffer, ilpOutputStream);

        byte[] serializedIlpBuffer = ilpOutputStream.toByteArray();

        ilpOutputStream.close();

        //encode ILP to base64URL format
        String encryptedIlp = encodeBase64Url(serializedIlpBuffer);

        return new Output(encryptedIlp, fulfillment);
    }

    private void writeByteData(byte[] data, ByteArrayOutputStream outputStream) throws IOException {

        int MSB = 0x80;

        ByteBuffer buffer = ByteBuffer.wrap(data);

        if (buffer.remaining() <= 127) {

            outputStream.write(buffer.remaining());

        } else {

            int lengthOfLength = (int) Math.ceil(Math.log(buffer.remaining()) / Math.log(256));

            outputStream.write(MSB | lengthOfLength);

            byte[] byteArray = new byte[lengthOfLength];

            for (int i = 0; i < lengthOfLength; i++) {
                byteArray[i] = (byte) ((buffer.remaining() >> (8 * (lengthOfLength - i - 1))) & 0xFF);
            }

            outputStream.write(byteArray);

        }

        outputStream.write(buffer.array());

    }

    private static String encodeBase64Url(byte[] byteArray) {

        byte[] base64Bytes = Base64.getUrlEncoder().encode(byteArray);
        return new String(base64Bytes, StandardCharsets.UTF_8)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "");

    }

}
