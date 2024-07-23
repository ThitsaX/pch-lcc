package com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.isis.client.helper;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IsisErrorResponse {

    private String code;

    private String name;

    private String description;

}
