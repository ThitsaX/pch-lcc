package com.thitsaworks.mojaloop.thitsaconnect.fspiop.outbound.command;

import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.AmountType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Party;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyComplexName;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdInfo;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyPersonalInfo;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.QuotesPostRequest;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionInitiator;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionInitiatorType;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionScenario;
import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionType;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubClient;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubConfiguration;
import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostQuotes;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ThitsaconnectToHubConfiguration.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PostQuotesUT extends EnvAwareUnitTest {

    @Autowired
    ThitsaconnectToHubClient thitsaconnectToHubClient;

    private static final Logger LOG = LoggerFactory.getLogger(PostQuotesUT.class);

    @Test
    public void postQuotesUT() throws Exception {

        PartyIdInfo payerInfo =
                new PartyIdInfo().partyIdType(PartyIdType.MSISDN).partyIdentifier("123").fspId("pinkbank")
                                 .partySubIdOrType("123");
        PartyIdInfo payeeInfo =
                new PartyIdInfo().partyIdType(PartyIdType.MSISDN).partyIdentifier("456").fspId("bluebank")
                                 .partySubIdOrType("123");

        Party payer = new Party().partyIdInfo(payerInfo).personalInfo(
                new PartyPersonalInfo().complexName(
                        new PartyComplexName().firstName("Htoo").middleName("Eain").lastName("Thin")).dateOfBirth(""));
        Party payee = new Party().partyIdInfo(payeeInfo).merchantClassificationCode("").name("").personalInfo(
                new PartyPersonalInfo().complexName(
                        new PartyComplexName().firstName("Kyaw").middleName("Myint").lastName("Lwin")).dateOfBirth(""));

        Money amount = new Money().amount("10").currency(Currency.EUR);

        TransactionType transactionType =
                new TransactionType().scenario(TransactionScenario.DEPOSIT).initiator(TransactionInitiator.PAYER)
                                     .initiatorType(TransactionInitiatorType.BUSINESS);

        QuotesPostRequest quotesPostRequest = new QuotesPostRequest();

        quotesPostRequest.quoteId(UUID.randomUUID().toString());
        quotesPostRequest.transactionId("9e9d1850-1676-45ea-8da3-0cb83574f3da");
        quotesPostRequest.transactionRequestId(UUID.randomUUID().toString());
        quotesPostRequest.payer(payer);
        quotesPostRequest.payee(payee);
        quotesPostRequest.amountType(AmountType.SEND);
        quotesPostRequest.amount(amount);
        quotesPostRequest.transactionType(transactionType);
        quotesPostRequest.expiration(LocalDateTime.now().plusDays(7).format(DateTimeFormatter.ISO_DATE_TIME));

        LOG.info("Expiration : " + quotesPostRequest.getExpiration());

        String date = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

//        String date = "2023-05-11T02:55:15.873Z";

        ZonedDateTime now = ZonedDateTime.now(java.time.ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String formatted = formatter.format(now);

        PostQuotes.Response response =
                this.thitsaconnectToHubClient.postQuote("senderLCC", "receiverLCC","RS256",
                        null,null,"/quotes", "POST",quotesPostRequest);

        LOG.info("Date : " + formatted);
        LOG.info("Response Status : " + response.getStatus());

    }

}
