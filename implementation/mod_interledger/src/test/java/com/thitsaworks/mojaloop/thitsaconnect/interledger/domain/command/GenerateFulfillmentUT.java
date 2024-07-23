package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.InterledgerConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.InterledgerData;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {InterledgerConfiguration.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GenerateFulfillmentUT extends EnvAwareUnitTest {

    @Autowired
    BuildPrepareIlpPacket buildPrepareIlpPacket;

    private static final Logger LOG = LoggerFactory.getLogger(GenerateFulfillmentUT.class);

    @Test
    public void generateFulfillment() throws Exception {

        String ilpDataJson =
                "{\"quoteId\":\"53fc3fca-8dcf-40cc-b554-e4b31b127c1b\",\"transactionId\":\"53fc3fca-8dcf-40cc-b554-e4b31b127c1b\",\"payee\":{\"partyIdInfo\":{\"partyIdType\":\"MSISDN\",\"partyIdentifier\":\"09966834795\",\"fspId\":\"receiverLCC\"},\"name\":\"undefined undefined\",\"personalInfo\":{\"complexName\":{\"firstName\":\"undefined undefined\",\"middleName\":\"undefined undefined\",\"lastName\":\"undefined undefined\"}}},\"payer\":{\"partyIdInfo\":{\"partyIdType\":\"ACCOUNT_ID\",\"partyIdentifier\":\"12345\",\"partySubIdOrType\":\"12345\",\"fspId\":\"senderLCC\"}},\"amountType\":\"SEND\",\"amount\":{\"currency\":\"EUR\",\"amount\":\"123.0001\"},\"transactionType\":{\"scenario\":\"TRANSFER\",\"initiator\":\"PAYER\",\"initiatorType\":\"BUSINESS\"},\"note\":\"Testing for Finance Portal\"}";
        ObjectMapper mapper = new ObjectMapper();
        InterledgerData interledgerData = mapper.readValue(ilpDataJson, InterledgerData.class);


        BuildPrepareIlpPacket.Output output =
                this.buildPrepareIlpPacket.execute(new BuildPrepareIlpPacket.Input(interledgerData));

        LOG.info("ILP packet : " + output.getInterledgerPreparePacket());
        LOG.info("Fulfillment : " + output.getInterledgerFulfillment());

    }

}
