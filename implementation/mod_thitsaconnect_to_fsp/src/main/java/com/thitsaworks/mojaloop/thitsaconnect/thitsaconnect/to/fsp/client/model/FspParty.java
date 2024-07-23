package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.fsp.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.ExtensionList;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyComplexName;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FspParty {

    @JsonProperty("type")
    private String type;

    @JsonProperty("idType")
    private PartyIdType idType;

    @JsonProperty("idValue")
    private String idValue;

    @JsonProperty("idSubValue")
    private String idSubValue;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("middleName")
    private String middleName;

    @JsonProperty("lastName")
    private String lastName;

    @JsonProperty("dateOfBirth")
    private String dateOfBirth;

    @JsonProperty("merchantClassificationCode")
    private String merchantClassificationCode;

    @JsonProperty("fspId")
    private String fspId;

    @JsonProperty("extensionList")
    private ExtensionList extensionList;

    public FspParty assignQuoteParty(Party hubParty) {

        if (hubParty.getPartyIdInfo() != null) {

            PartyIdInfo partyIdInfo = hubParty.getPartyIdInfo();

            this.setIdType(partyIdInfo.getPartyIdType());
            this.setIdValue(partyIdInfo.getPartyIdentifier());
            this.setIdSubValue(partyIdInfo.getPartySubIdOrType());
            this.setFspId(partyIdInfo.getFspId());
            this.setExtensionList(partyIdInfo.getExtensionList());

        }

        this.setDisplayName(hubParty.getName());

        if (hubParty.getPersonalInfo() != null) {

            if (hubParty.getPersonalInfo().getComplexName() != null) {
                PartyComplexName partyComplexName = hubParty.getPersonalInfo().getComplexName();

                this.setFirstName(partyComplexName.getFirstName());
                this.setMiddleName(partyComplexName.getMiddleName());
                this.setLastName(partyComplexName.getLastName());

            }

            this.setDateOfBirth(hubParty.getPersonalInfo().getDateOfBirth());

        }
        this.setMerchantClassificationCode(hubParty.getMerchantClassificationCode());

        return this;

    }

}


