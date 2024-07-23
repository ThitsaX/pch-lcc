package com.thitsaworks.mojaloop.thitsaconnect.interledger.domain;

import com.nimbusds.jose.shaded.gson.Gson;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.CurrencyDecimals;
import com.thitsaworks.mojaloop.thitsaconnect.interledger.data.InterledgerData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.interledger.core.InterledgerAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ILPDataPacket {

    private static final Logger LOG = LoggerFactory.getLogger(InterledgerAddress.class);

    protected String amount;

    protected String account;

    private byte[] data;

    public ILPDataPacket amount(Money amount) {

        Integer decimalPlace = CurrencyDecimals.valueOf(amount.getCurrency().toString()).getDecimal();

        long decimalCalculatedAmount =
                new BigDecimal(amount.getAmount()).multiply(BigDecimal.TEN.pow(decimalPlace)).longValue();

        this.amount = Long.toString(decimalCalculatedAmount);

        return this;

    }

    public ILPDataPacket address(Party payee) {

        PartyIdInfo partyIdInfo = payee.getPartyIdInfo();

        String address = "g" // ILP global address allocation scheme
                + '.' + partyIdInfo.getFspId().toLowerCase() // fspId of the party account
                + '.' + partyIdInfo.getPartyIdType().toString().toLowerCase() // identifier type
                + '.' + partyIdInfo.getPartyIdentifier().toLowerCase()// identifier value
                + ((partyIdInfo.getPartySubIdOrType() != null) ? '.' + partyIdInfo.getPartySubIdOrType().toLowerCase() :
                "");

        this.account = InterledgerAddress.of(address).getValue();

        return this;
    }

    public ILPDataPacket data(InterledgerData interledgerData) {

        String jsonString = new Gson().toJson(interledgerData);

        this.data = encodeBase64Url(jsonString).getBytes(StandardCharsets.UTF_8);

        return this;

    }

    public static String encodeBase64Url(String input) {

        byte[] base64Bytes = Base64.getUrlEncoder().encode(input.getBytes(StandardCharsets.UTF_8));
        return new String(base64Bytes, StandardCharsets.UTF_8).replace("+", "-").replace("/", "_").replace("=", "");

    }

    public byte[] EncodeData() {

        String encoded = encodeBase64Url(new Gson().toJson(this));
        return encoded.getBytes(StandardCharsets.UTF_8);

    }

}
