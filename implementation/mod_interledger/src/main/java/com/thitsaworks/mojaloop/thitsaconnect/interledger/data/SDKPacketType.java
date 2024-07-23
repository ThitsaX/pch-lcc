package com.thitsaworks.mojaloop.thitsaconnect.interledger.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum SDKPacketType {

    TYPE_ILP_PAYMENT(1), TYPE_ILQP_LIQUIDITY_REQUEST(2), TYPE_ILQP_LIQUIDITY_RESPONSE(3),
    TYPE_ILQP_BY_SOURCE_REQUEST(4), TYPE_ILQP_BY_SOURCE_RESPONSE(5), TYPE_ILQP_BY_DESTINATION_REQUEST(6),
    TYPE_ILQP_BY_DESTINATION_RESPONSE(7), TYPE_ILP_ERROR(8), TYPE_ILP_FULFILLMENT(9), TYPE_ILP_FORWARDED_PAYMENT(10),
    TYPE_ILP_REJECTION(11);

    private Integer code;

}

