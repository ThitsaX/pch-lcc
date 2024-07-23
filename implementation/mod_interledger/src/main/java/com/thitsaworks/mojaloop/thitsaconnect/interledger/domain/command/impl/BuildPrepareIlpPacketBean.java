package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.impl;

import com.google.common.primitives.UnsignedLong;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.ILPDataPacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.BuildPrepareIlpPacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.GenerateFulfillment;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerFulfillment;
import org.interledger.core.InterledgerPacketType;
import org.interledger.core.InterledgerPreparePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Component
public class BuildPrepareIlpPacketBean implements BuildPrepareIlpPacket {

    @Autowired
    private GenerateFulfillment generateFulfillment;

    private static final Logger LOG = LoggerFactory.getLogger(BuildPrepareIlpPacket.class);

    @Override
    public Output execute(Input input) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        ILPDataPacket ilpDataPacket = new ILPDataPacket();

        ilpDataPacket.amount(input.getInterledgerData().getAmount())
                     .address(input.getInterledgerData().getPayee()).data(input.getInterledgerData()); //build ILP

        byte[] encryptedIlpDataURL = ilpDataPacket.EncodeData();

        InterledgerFulfillment interledgerFulfillment =
                this.generateFulfillment.execute(new GenerateFulfillment.Input(encryptedIlpDataURL)).getFulfillment();

        InterledgerCondition condition = interledgerFulfillment.getCondition(); //get Condition from fulfillment

        Instant expiration = Instant.now().plusSeconds(180); // 3 minutes

        InterledgerPreparePacket interledgerPreparePacket =
                InterledgerPreparePacket.builder().typedData(InterledgerPacketType.PREPARE_CODE)
                                        .amount(UnsignedLong.valueOf(ilpDataPacket.getAmount()))
                                        .destination(InterledgerAddress.of(ilpDataPacket.getAccount()))
                                        .expiresAt(expiration).data(ilpDataPacket.getData())
                                        .executionCondition(condition).build();

        return new Output(interledgerPreparePacket, interledgerFulfillment);
    }

}
