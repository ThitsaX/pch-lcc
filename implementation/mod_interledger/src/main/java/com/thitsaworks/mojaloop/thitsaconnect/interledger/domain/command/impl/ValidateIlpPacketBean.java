package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.InterledgerData;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.BuildPrepareIlpPacket;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command.ValidateIlpPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ValidateIlpPacketBean implements ValidateIlpPacket {

    private static final Logger LOG = LoggerFactory.getLogger(BuildPrepareIlpPacket.class);

    @Override
    public Output execute(Input input) throws IOException, ClassNotFoundException {

        ObjectMapper mapper = new ObjectMapper();

        InterledgerData interledgerData =
                mapper.readValue(input.getIlpPreparePacket().getData(), InterledgerData.class);

        boolean isIlpValid = input.getPayerFsp().equals(interledgerData.getPayer().getPartyIdInfo().getFspId()) ||
                input.getPayeeFsp().equals(interledgerData.getPayee().getPartyIdInfo().getFspId()) ||
                input.getAmount().equals(interledgerData.getAmount().getAmount()) ||
                input.getCurrency().equals(interledgerData.getAmount().getCurrency());

        return new Output(isIlpValid);
    }

}
