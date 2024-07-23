package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain.command;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.interledger.core.InterledgerPreparePacket;

import java.io.IOException;
import java.io.Serializable;

public interface DecryptIlpPreparePacket {

    @Getter
    @AllArgsConstructor
    class Input implements Serializable {

        private String encryptedIlpPacket;

        private String condition;

        private String expiration;

    }

    @Getter
    @AllArgsConstructor
    class Output {

        InterledgerPreparePacket decryptedIlpPreparePacket;

    }

    Output execute(Input input) throws IOException;

}
