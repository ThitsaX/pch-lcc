package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.impl;

import com.google.common.primitives.UnsignedLong;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.BuildPrepareIlpPacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.DecryptIlpPreparePacket;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerPreparePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Base64;

@Component
public class DecryptIlpPreparePacketBean implements DecryptIlpPreparePacket {

    private static final Logger LOG = LoggerFactory.getLogger(BuildPrepareIlpPacket.class);

    @Override
    public Output execute(Input input) throws IOException {

        byte[] decodedBase64Ilp = Base64.getUrlDecoder().decode(input.getEncryptedIlpPacket().getBytes());

        //read form ilp Stream
        ByteArrayInputStream ilpInputStream = new ByteArrayInputStream(decodedBase64Ilp);

        //read ilp type
        int interledgerType = ilpInputStream.read();

        //read contents
        byte[] contents = readByteData(ilpInputStream);

        ilpInputStream.close();

        //read form content Stream
        ByteArrayInputStream contentInputStream = new ByteArrayInputStream(contents);

        //read amount
        BigInteger amountInteger = new BigInteger(contentInputStream.readNBytes(8));
        UnsignedLong amount = UnsignedLong.fromLongBits(amountInteger.longValue());

        //read address
        byte[] addressBuffer = readByteData(contentInputStream);
        InterledgerAddress destination = InterledgerAddress.of(new String(addressBuffer));

        //read data
        byte[] dataBuffer = readByteData(contentInputStream);
        byte[] decodedIlpData = Base64.getUrlDecoder().decode(dataBuffer);

        //read condition
        byte[] conditionBuffer = readByteData(contentInputStream);

        byte[] conditionHash = new byte[32];

        if (conditionBuffer.length != 0 || input.getCondition() != null) {

            conditionHash = (conditionBuffer.length == 0) ? Base64.getUrlDecoder().decode(input.getCondition()) :
                    conditionBuffer;
        }

        InterledgerCondition condition = InterledgerCondition.of(conditionHash);

        //read expiration
        byte[] expirationBuffer = readByteData(contentInputStream);
        Instant expiration = (expirationBuffer.length == 0) ? Instant.now().plusSeconds(60) :
                Instant.parse(new String(expirationBuffer));

        InterledgerPreparePacket interledgerPreparePacket =
                InterledgerPreparePacket.builder().typedData(interledgerType).amount(amount).destination(destination)
                                        .data(decodedIlpData).executionCondition(condition).expiresAt(expiration)
                                        .build();

        return new Output(interledgerPreparePacket);

    }

    private byte[] readByteData(ByteArrayInputStream inputStream) throws IOException {

        int length = inputStream.read();
        if (length <= 0) {
            return new byte[0];
        }

        if (((length >> 7) & 1) != 0) {
            int lengthPrefixLength = length & 0x7F;
            length = new BigInteger(inputStream.readNBytes(lengthPrefixLength)).intValue();
        }

        byte[] dataBuffer = new byte[length];
        inputStream.read(dataBuffer, 0, length);

        return dataBuffer;
    }

}
