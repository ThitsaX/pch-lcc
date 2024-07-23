package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.primitives.UnsignedLong;
import com.nimbusds.jose.shaded.gson.Gson;
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
public class ValidateConditionUT extends EnvAwareUnitTest {

    @Autowired
    BuildPrepareIlpPacket buildPrepareIlpPacket;

    @Autowired
    ValidateCondition validateCondition;

    private static final Logger LOG = LoggerFactory.getLogger(ValidateConditionUT.class);

    @Test
    public void generateILP() throws Exception {

        String ilpDataJson =
                "{\"transactionId\":\"53fc3fca-8dcf-40cc-b554-e4b31b127c1b\",\"quoteId\":\"53fc3fca-8dcf-40cc-b554-e4b31b127c1b\",\"payee\":{\"partyIdInfo\":{\"partyIdType\":\"MSISDN\",\"partyIdentifier\":\"09966834795\",\"fspId\":\"receiverLCC\"},\"name\":\"undefined undefined\",\"personalInfo\":{\"complexName\":{\"firstName\":\"undefined undefined\",\"middleName\":\"undefined undefined\",\"lastName\":\"undefined undefined\"}}},\"payer\":{\"partyIdInfo\":{\"partyIdType\":\"ACCOUNT_ID\",\"partyIdentifier\":\"12345\",\"partySubIdOrType\":\"12345\",\"fspId\":\"senderLCC\"}},\"amount\":{\"currency\":\"EUR\",\"amount\":\"100\"},\"transactionType\":{\"scenario\":\"TRANSFER\",\"initiator\":\"PAYER\",\"initiatorType\":\"BUSINESS\"},\"note\":\"Testing for Finance Portal\"}";
        ObjectMapper mapper = new ObjectMapper();

        InterledgerData interledgerData = mapper.readValue(ilpDataJson, InterledgerData.class);

        System.out.println(new Gson().toJson(interledgerData));

        ILPDataPacket dataPacket = new ILPDataPacket().data(interledgerData).address(interledgerData.getPayee())
                                                      .amount(interledgerData.getAmount());

        String condition = "4mvd6suyM1j0lrZ5EZHkR-x_oTNUPnlRGsJLVOpxjcY";
        byte[] conditionHash = Base64.getUrlDecoder().decode(condition.getBytes());

        String fulfillmentString = "VIT8SYEMp-Q4LbC_X-ycqABhIPa9ku3CEoL-N3dboQo";

        InterledgerPreparePacket ilpPreparePacket = InterledgerPreparePacket.builder().amount(UnsignedLong.valueOf(
                interledgerData.getAmount().getAmount())).data(ilpDataJson.getBytes()).destination(
                InterledgerAddress.of(dataPacket.getAccount())).executionCondition(
                InterledgerCondition.of(conditionHash)).expiresAt(Instant.now()).build();

        boolean validateCondition = this.validateCondition.execute(
                new ValidateCondition.Input(ilpPreparePacket, fulfillmentString, condition, Currency.EUR)).isValid();

        LOG.info("Is Valid : " + validateCondition);

    }

}
