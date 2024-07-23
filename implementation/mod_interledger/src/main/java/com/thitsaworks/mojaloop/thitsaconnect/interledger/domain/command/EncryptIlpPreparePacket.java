package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command;

import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.InterledgerData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.interledger.core.InterledgerFulfillment;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface EncryptIlpPreparePacket {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    class Input implements Serializable {

        private InterledgerData interledgerData;

    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    class Output {

        private String encryptedIlpPreparePacket;

        private InterledgerFulfillment interledgerFulfillment;

    }

    Output execute(Input input) throws IOException, NoSuchAlgorithmException, InvalidKeyException;

}
