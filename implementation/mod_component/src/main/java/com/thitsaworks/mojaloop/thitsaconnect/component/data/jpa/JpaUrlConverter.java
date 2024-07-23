package com.thitsaworks.mojaloop.thitsaconnect.component.data.jpa;

import javax.persistence.AttributeConverter;
import java.net.MalformedURLException;
import java.net.URL;

public class JpaUrlConverter implements AttributeConverter<URL, String> {

    @Override
    public String convertToDatabaseColumn(URL attribute) {

        if (attribute == null)
            return null;

        return attribute.toString();

    }

    @Override
    public URL convertToEntityAttribute(String dbData) {

        if (dbData == null)
            return null;

        try {

            return new URL(dbData);

        } catch (MalformedURLException e) {

            throw new IllegalArgumentException();

        }

    }

}
