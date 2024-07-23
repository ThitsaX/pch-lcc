package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command;

import com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.ILPDataPacket;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.interledger.core.InterledgerFulfillment;

import java.io.IOException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface GenerateFulfillment {

    @Getter
    @AllArgsConstructor
    class Input implements Serializable {

        private byte[] base64EncodedIlpDataPacket;

    }

    @Getter
    @AllArgsConstructor
    class Output {

        private InterledgerFulfillment fulfillment;

    }

    Output execute(Input input) throws IOException, NoSuchAlgorithmException, InvalidKeyException;

}
