package com.thitsaworks.mojaloop.thitsaconnect.fspiop.outbound.command;

import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransfersPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostTransfers;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ThitsaconnectToHubConfiguration.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PostTransfersUT extends EnvAwareUnitTest {

    @Autowired
    ThitsaconnectToHubClient thitsaconnectToHubClient;

    private static final Logger LOG = LoggerFactory.getLogger(PostTransfersUT.class);

    @Test
    public void postTransfersUT() throws Exception {

        Money money = new Money().currency(Currency.EUR).amount("10");

        String expiration = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

        PostTransfers.Request request =
                new PostTransfers.Request(UUID.randomUUID().toString(), "pinkbank",
                        "bluebank", money, "Fv7DNRbxXtms9rU", "tw7o1hJm-5Ir7gJVTBrsp9FpxrEc_cYVJM_zu6St5Y-", expiration,
                        new ExtensionList());

        TransfersPostRequest transfersPostRequest = new TransfersPostRequest();

        PostTransfers.Response response =
                this.thitsaconnectToHubClient.postTransfers(null,null,null,null,null,"pinkbank", "bluebank", transfersPostRequest);

        LOG.info("Response Status : " + response.getStatus());

    }

}
