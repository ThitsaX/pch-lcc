package com.thitsaworks.mojaloop.thitsaconnect.fspiop.outbound.command;

import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.GetPartiesById;
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
public class GetPartiesByIdUT extends EnvAwareUnitTest {

    @Autowired
    ThitsaconnectToHubClient thitsaconnectToHubClient;

    private static final Logger LOG = LoggerFactory.getLogger(GetPartiesByIdUT.class);

    @Test
    public void getPartiesByIdUT() throws Exception {

        GetPartiesById.Response response =
                this.thitsaconnectToHubClient.getParties("senderLCC", "receiverLCC", null,null,null,"000000356",
                        PartyIdType.ACCOUNT_ID, "09400547259",
                        Currency.EUR);
        LOG.info("Result Status : " + response.getStatus());
    }

}
