package com.thitsaworks.mojaloop.thitsaconnect.component.data.jpa;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.time.Instant;

@Converter
public class JpaInstantConverter implements AttributeConverter<Instant, Long> {

    @Override
    public Long convertToDatabaseColumn(Instant attribute) {

        if (attribute == null) {return null;}

        return attribute.getEpochSecond();

    }

    @Override
    public Instant convertToEntityAttribute(Long dbData) {

        if (dbData == null) {return null;}

        return Instant.ofEpochSecond(dbData);

    }

}