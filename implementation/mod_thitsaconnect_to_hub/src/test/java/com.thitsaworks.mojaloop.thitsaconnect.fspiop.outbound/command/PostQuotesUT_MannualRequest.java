//package com.thitsaworks.mojaloop.thitsaconnect.fspiop.outbound.command;
//
//import com.thitsaworks.mojaloop.thitsaconnect.component.test.EnvAwareUnitTest;
//import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.AmountType;
//import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Currency;
//import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.Money;
//import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdInfo;
//import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.PartyIdType;
//import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionInitiator;
//import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionInitiatorType;
//import com.thitsaworks.mojaloop.thitsaconnect.fspiop.model.TransactionScenario;
//import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubClient;
//import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.ThitsaconnectToHubConfiguration;
//import com.thitsaworks.mojaloop.thitsaconnect.thitsaconnect.to.hub.api.PostQuotes;
//import org.junit.FixMethodOrder;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.junit.runners.MethodSorters;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//
//import java.util.UUID;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = {ThitsaconnectToHubConfiguration.class})
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//public class PostQuotesUT_MannualRequest extends EnvAwareUnitTest {
//
//    @Autowired
//    ThitsaconnectToHubClient thitsaconnectToHubClient;
//
//    private static final Logger LOG = LoggerFactory.getLogger(PostQuotesUT_MannualRequest.class);
//
//    @Test
//    public void postQuotesUT() throws Exception {
//
//        Money money = new Money().currency(Currency.EUR).amount("10");
//
//        PartyIdInfo payerPartyIdInfo =
//                new PartyIdInfo().partyIdType(PartyIdType.MSISDN).fspId("pinkbank").partyIdentifier("123")
//                                 .partySubIdOrType("123");
//
//        PartyIdInfo payeePartyIdInfo =
//                new PartyIdInfo().partyIdType(PartyIdType.MSISDN).fspId("bluebank").partyIdentifier("456")
//                                 .partySubIdOrType("123");
//
//        PostQuotes.Request.TransactionType transactionType =
//                new PostQuotes.Request.TransactionType(TransactionScenario.DEPOSIT, TransactionInitiator.PAYER,
//                        TransactionInitiatorType.BUSINESS);
//
//        PostQuotes.Request request =
//                new PostQuotes.Request(UUID.randomUUID().toString(), UUID.randomUUID().toString(), AmountType.SEND,
//                        money, new PostQuotes.Request.Payer(payerPartyIdInfo),
//                        new PostQuotes.Request.Payee(payeePartyIdInfo), transactionType);
//
//        PostQuotes.Response response =
//                this.thitsaconnectToHubClient.postQuote("pinkbank", "bluebank", request);
//
//        LOG.info("Response Status : " + response.getStatus());
//
//    }
//
//}
