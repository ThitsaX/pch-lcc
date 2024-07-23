package com.thitsaworks.mojaloop.thitsaconnect.fspiop.outbound.command;

import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.GetPartiesById;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.support.TransferIdGenerator;
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
@ContextConfiguration(classes = {GenerateTransferIdUT.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GenerateTransferIdUT extends EnvAwareUnitTest {
    @Test
    public void generate() throws Exception {

        TransferIdGenerator idGenerator = new TransferIdGenerator();
        String transferId = idGenerator.generateTransferId();
        System.out.println(transferId);

    }


}
