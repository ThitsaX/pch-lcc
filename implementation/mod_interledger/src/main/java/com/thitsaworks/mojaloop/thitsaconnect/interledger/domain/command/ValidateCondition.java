package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command;

import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.interledger.core.InterledgerPreparePacket;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface ValidateCondition {

    @Getter
    @AllArgsConstructor
    class Input implements Serializable {

        private InterledgerPreparePacket ilpPreparePacket;

        private String fulfillment;

        private String condition;

        private Currency currency;

    }

    @Getter
    @AllArgsConstructor
    class Output {

        private boolean isValid;

    }

    Output execute(Input input) throws IOException, NoSuchAlgorithmException, InvalidKeyException;

}
