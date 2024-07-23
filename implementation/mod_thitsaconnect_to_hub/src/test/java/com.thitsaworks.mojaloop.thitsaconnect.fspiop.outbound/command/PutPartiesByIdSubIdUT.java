package com.thitsaworks.mojaloop.thitsaconnect.fspiop.outbound.command;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartiesTypeIDPutResponse;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyComplexName;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyPersonalInfo;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubFromPayeeClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PutPartiesByIdandSubId;
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
@ContextConfiguration(classes = {ThitsaconnectToHubConfiguration.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PutPartiesByIdSubIdUT extends EnvAwareUnitTest {

    @Autowired
    ThitsaconnectToHubFromPayeeClient thitsaconnectToHubFromPayeeClient;

    private static final Logger LOG = LoggerFactory.getLogger(PutPartiesByIdSubIdUT.class);

    @Test
    public void putPartiesByIdSubIdUT() throws Exception {

        PartiesTypeIDPutResponse putPartyRequest = new PartiesTypeIDPutResponse();

        PartyIdInfo partyIdInfo = new PartyIdInfo()
                .partyIdType(PartyIdType.ACCOUNT_ID)
                .partyIdentifier("000000356")
                .partySubIdOrType("09400547259")
                .fspId("receiverLCC")
                .extensionList(null);

        PartyComplexName partyComplexName = new PartyComplexName()
                .firstName("Nyein")
                .middleName("Nyein")
                .lastName("Ei");

        PartyPersonalInfo personalInfo = new PartyPersonalInfo()
                .dateOfBirth(null)
                .complexName(partyComplexName);

        Party party = new Party()
                .partyIdInfo(partyIdInfo)
                .merchantClassificationCode("123")
                .name("sdstest")
                .personalInfo(personalInfo);

        putPartyRequest.party(party);

        LOG.info("Data : " + new Gson().toJson(putPartyRequest));

        PutPartiesByIdandSubId.Response response =
                this.thitsaconnectToHubFromPayeeClient.putParties("senderLCC", "receiverLCC", Currency.USD,
                        "Tue, 30 May 2023 09:26:33 GMT",
                        putPartyRequest, "", "");
        LOG.info("Result Status : " + response.getStatus());
    }

}
