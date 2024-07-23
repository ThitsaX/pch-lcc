package com.thitsaworks.mojaloop.thitsaconnect.jws;

import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.jws.domain.IValidateSignature;
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
public class ValidateSignatureUT extends EnvAwareUnitTest {

    @Autowired
    IValidateSignature validateSignature;

    private static final Logger LOG = LoggerFactory.getLogger(ValidateSignatureUT.class);

    @Test
    public void testValidateSignatureUT() throws Exception {

        String signature =
                "{\"protectedHeader\":\"eyJGU1BJT1AtRGVzdGluYXRpb24iOiJ3YWxsZXQyIiwiRlNQSU9QLVVSSSI6Ii9xdW90ZXMiLCJGU1BJT1AtSFRUUC1NZXRob2QiOiJQT1NUIiwiYWxnIjoiUlMyNTYiLCJGU1BJT1AtU291cmNlIjoid2FsbGV0MSIsIkRhdGUiOiJUdWUsIDE2IEphbnVhcnkgMjAyNDIxOjEyOjMxIEdNVCJ9\",\"payload\":\"eyJwZWVyIjoidGhpdHNhbmV0X2hhbmEiLCJpZGVudGlmaWVyIjoiMDAwMDAwNzI1IiwiY3VycmVuY3kiOiJNTUsiLCJleHRyYXMiOlt7ImtleSI6Ik1TSVNETiIsInZhbHVlIjoiMDk3OTk5OTE4NjQifV19\",\"signature\":\"feC1q3tu_nenrH7GV1M8RW2VMVWWwDewrf2N4JZaH_kXrEZtruqnqNJW52EkatSIo7cHSLMkPTx-SFSdMgGeO8Byu0IO0Uy5051jwc2YscNf-Y2lVvkn2w60-lg8FrkDCIS3BzvptE30nVaaReNgaBjFYWnxOEIKT7D10fJSio0TyOFKQkDadQ6yKBuHem152URltPDJL1GND2lj_tsDiv-7KjwLzODd7lE_r3uCglVXIucqDOFHHtM7Y6TV49JK4skiWiPm2t9G8lfYCnLmPSgqQ77-x4v5K4bqBtUsKFL_t2T6elhvux9HmkEQuhrxWeyOjZoIB9mO5de27ZMT9g\"}";

        var output = this.validateSignature.execute(new IValidateSignature.Input(
                "RS256",
                "/quotes",
                "wallet1",
                "wallet2",
                "Tue, 16 January 202421:12:31 GMT",
                RequestMethod.POST.toString(),
                signature,
                "{\"peer\":\"thitsanet_hana\",\"identifier\":\"000000726\",\"currency\":\"MMK\",\"extras\":[{\"key\":\"MSISDN\",\"value\":\"09799991864\"}]}"
        ));

        LOG.info("Output : " + (output.getIsValid() ? "Valid Signature" : "Invalid Signature"));

    }

}
