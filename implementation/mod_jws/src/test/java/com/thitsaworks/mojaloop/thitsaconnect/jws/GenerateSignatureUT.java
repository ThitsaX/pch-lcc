package com.thitsaworks.mojaloop.thitsaconnect.jws;

import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IGenerateSignature;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMethod;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JwsConfiguration.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GenerateSignatureUT extends EnvAwareUnitTest {

    @Autowired
    IGenerateSignature generateSignature;

    private static final Logger LOG = LoggerFactory.getLogger(GenerateSignatureUT.class);

    @Test
    public void testGenerateSignatureUT() throws Exception {

        String body = "{\"peer\":\"thitsanet_hana\",\"identifier\":\"000000725\",\"currency\":\"MMK\",\"extras\":[{\"key\":\"MSISDN\",\"value\":\"09799991864\"}]}";

        var output = this.generateSignature.execute(new IGenerateSignature.Input(
                "RS256",
                "/quotes",
                "wallet1",
                "wallet2",
                "Tue, 16 January 202421:12:31 GMT",
                body,
                RequestMethod.POST.toString()
        ));

        if (output.getSignatureObject() != null) {
            LOG.info("Output : " + output.getSignatureObject());
        }
    }

}
