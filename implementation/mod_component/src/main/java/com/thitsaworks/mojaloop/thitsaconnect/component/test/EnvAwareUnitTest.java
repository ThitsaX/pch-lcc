package com.thitsaworks.mojaloop.thitsaconnect.component.test;

public class EnvAwareUnitTest {

    static {

        System.setProperty("JASYPT_PASSWORD", "password");

    }

}