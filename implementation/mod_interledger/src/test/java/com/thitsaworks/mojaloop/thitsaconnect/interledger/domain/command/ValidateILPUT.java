package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.InterledgerConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.InterledgerData;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.ILPDataPacket;
import org.interledger.core.InterledgerAddress;
import org.interledger.core.InterledgerCondition;
import org.interledger.core.InterledgerPreparePacket;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.Instant;
import java.util.Base64;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InterledgerConfiguration.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ValidateILPUT extends EnvAwareUnitTest {

    @Autowired
    ValidateIlpPacket validateIlpPacket;

    private static final Logger LOG = LoggerFactory.getLogger(ValidateILPUT.class);

    @Test
    public void ValidateILP() throws Exception {

        String ilpDataJson =
                "{\"transactionId\":\"0749285c-1cee-4c46-877a-af0840d1e6f1\",\"quoteId\":\"0749285c-1cee-4c46-877a-af0840d1e6f1\",\"payee\":{\"partyIdInfo\":{\"partyIdType\":\"ACCOUNT_ID\",\"partyIdentifier\":\"000000356\",\"partySubIdOrType\":\"09400547259\",\"fspId\":\"receiverLCC\"},\"name\":\"Nyein Nyein Ei\",\"personalInfo\":{\"complexName\":{\"firstName\":\"Nyein\",\"middleName\":\"Nyein\",\"lastName\":\"Ei\"},\"dateOfBirth\":\"1986-06-16\"}},\"payer\":{\"partyIdInfo\":{\"partyIdType\":\"ACCOUNT_ID\",\"partyIdentifier\":\"12345\",\"fspId\":\"senderLCC\"}},\"amount\":{\"currency\":\"MMK\",\"amount\":\"100\"},\"transactionType\":{\"scenario\":\"TRANSFER\",\"initiator\":\"PAYER\",\"initiatorType\":\"BUSINESS\"}}";
        ObjectMapper mapper = new ObjectMapper();

        InterledgerData interledgerData = mapper.readValue(ilpDataJson, InterledgerData.class);

        ILPDataPacket dataPacket = new ILPDataPacket().data(interledgerData).address(interledgerData.getPayee())
                                                      .amount(interledgerData.getAmount());

        String condition = "dd8esxicFDCTmeKsR+mjz2agQlTKGIn4fMUKH2tXjCs=";
        byte[] conditionHash = Base64.getDecoder().decode(condition.getBytes());

        InterledgerPreparePacket ilpPreparePacket = InterledgerPreparePacket.builder().amount(UnsignedLong.valueOf(
                interledgerData.getAmount().getAmount())).data(ilpDataJson.getBytes()).destination(
                InterledgerAddress.of(dataPacket.getAccount())).executionCondition(
                InterledgerCondition.of(conditionHash)).expiresAt(Instant.now()).build();

        boolean validateIlp =
                this.validateIlpPacket.execute(new ValidateIlpPacket.Input("senderLCC", "receiverLCC", "100",
                        Currency.MMK, ilpPreparePacket)).isValid();
        LOG.info("Is Valid : " + validateIlp);

    }

}
