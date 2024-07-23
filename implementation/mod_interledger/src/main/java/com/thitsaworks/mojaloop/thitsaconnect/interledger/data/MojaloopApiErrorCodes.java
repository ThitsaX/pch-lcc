package com.thitsaworks.mojaloop.thitsaconnect.interledger.data;

public enum MojaloopApiErrorCodes {

    // Generic communication errors
    COMMUNICATION_ERROR("1000", "Communication error"),
    DESTINATION_COMMUNICATION_ERROR("1001", "Destination communication error"),

    // Generic server errors
    SERVER_ERROR("2000", "Generic server error"),
    INTERNAL_SERVER_ERROR("2001", "Internal server error"),
    NOT_IMPLEMENTED("2002", "Not implemented", 501),
    SERVICE_CURRENTLY_UNAVAILABLE("2003", "Service currently unavailable", 503),
    SERVER_TIMED_OUT("2004", "Server timed out"),
    SERVER_BUSY("2005", "Server busy"),

    // Generic client errors
    METHOD_NOT_ALLOWED("3000", "Generic client error - Method Not Allowed", 405),
    CLIENT_ERROR("3000", "Generic client error", 400),
    UNACCEPTABLE_VERSION("3001", "Unacceptable version requested", 406),
    UNKNOWN_URI("3002", "Unknown URI", 404),
    ADD_PARTY_INFO_ERROR("3003", "Add Party information error"),
    DELETE_PARTY_INFO_ERROR("3040", "Delete Party information error"),

    // Client validation errors
    VALIDATION_ERROR("3100", "Generic validation error", 400),
    MALFORMED_SYNTAX("3101", "Malformed syntax", 400),
    MISSING_ELEMENT("3102", "Missing mandatory element", 400),
    TOO_MANY_ELEMENTS("3103", "Too many elements", 400),
    TOO_LARGE_PAYLOAD("3104", "Too large payload", 400),
    INVALID_SIGNATURE("3105", "Invalid signature", 400),
    MODIFIED_REQUEST("3106", "Modified request", 400),
    MISSING_MANDATORY_EXTENSION("3107", "Missing mandatory extension parameter", 400),

    // Identifier errors
    ID_NOT_FOUND("3200", "Generic ID not found"),
    DESTINATION_FSP_ERROR("3201", "Destination FSP Error"),
    PAYER_FSP_ID_NOT_FOUND("3202", "Payer FSP ID not found"),
    PAYEE_FSP_ID_NOT_FOUND("3203", "Payee FSP ID not found"),
    PARTY_NOT_FOUND("3204", "Party not found"),
    QUOTE_ID_NOT_FOUND("3205", "Quote ID not found"),
    TXN_REQUEST_ID_NOT_FOUND("3206", "Transaction request ID not found"),
    TXN_ID_NOT_FOUND("3207", "Transaction ID not found"),
    TRANSFER_ID_NOT_FOUND("3208", "Transfer ID not found"),
    BULK_QUOTE_ID_NOT_FOUND("3209", "Bulk quote ID not found"),
    BULK_TRANSFER_ID_NOT_FOUND("3210", "Bulk transfer ID not found"),

    // Expired errors
    EXPIRED_ERROR("3300", "Generic expired error"),
    TXN_REQUEST_EXPIRED("3301", "Transaction request expired"),
    QUOTE_EXPIRED("3302", "Quote expired"),
    TRANSFER_EXPIRED("3303", "Transfer expired"),

    // Payer errors
    PAYER_ERROR("4000", "Generic Payer error"),
    PAYER_FSP_INSUFFICIENT_LIQUIDITY("4001", "Payer FSP insufficient liquidity"),
    PAYER_REJECTION("4100", "Generic Payer rejection"),
    PAYER_REJECTED_TXN_REQUEST("4101", "Payer rejected transaction request"),
    PAYER_FSP_UNSUPPORTED_TXN_TYPE("4102", "Payer FSP unsupported transaction type"),
    PAYER_UNSUPPORTED_CURRENCY("4103", "Payer unsupported currency"),
    PAYER_LIMIT_ERROR("4200", "Payer limit error"),
    PAYER_PERMISSION_ERROR("4300", "Payer permission error"),
    PAYER_BLOCKED_ERROR("4400", "Generic Payer blocked error"),

    // Payee errors
    PAYEE_ERROR("5000", "Generic Payee error"),
    PAYEE_FSP_INSUFFICIENT_LIQUIDITY("5001", "Payee FSP insufficient liquidity"),
    PAYEE_REJECTION("5100", "Generic Payee rejection"),
    PAYEE_REJECTED_QUOTE("5101", "Payee rejected quote"),
    PAYEE_FSP_UNSUPPORTED_TXN_TYPE("5102", "Payee FSP unsupported transaction type"),
    PAYEE_FSP_REJECTED_QUOTE("5103", "Payee FSP rejected quote"),
    PAYEE_REJECTED_TXN("5104", "Payee rejected transaction"),
    PAYEE_FSP_REJECTED_TXN("5105", "Payee FSP rejected transaction"),
    PAYEE_UNSUPPORTED_CURRENCY("5106", "Payee unsupported currency"),
    PAYEE_LIMIT_ERROR("5200", "Payee limit error"),
    PAYEE_PERMISSION_ERROR("5300", "Payee permission error"),
    GENERIC_PAYEE_BLOCKED_ERROR("5400", "Generic Payee blocked error"),

    // Thirdparty errors
    TP_ERROR("7000", "Generic Thirdparty error"),
    TP_TRANSACTION_ERROR("7100", "Generic Thirdparty transaction error"),
    TP_FSP_TRANSACTION_REQUEST_NOT_VALID("7101", "Transaction request failed DFSP validation"),
    TP_FSP_TRANSACTION_UPDATE_FAILED("7102", "Transaction request PUT update send to PISP failed"),
    TP_FSP_TRANSACTION_REQUEST_QUOTE_FAILED("7103", "Transaction request quote send to payee DFSP failed"),
    TP_FSP_TRANSACTION_REQUEST_AUTHORIZATION_FAILED("7104",
            "Transaction request for user authorization send to PISP failed"),
    TP_FSP_TRANSACTION_AUTHORIZATION_NOT_VALID("7105", "Authorization received from PISP failed DFSP validation"),
    TP_FSP_TRANSACTION_AUTHORIZATION_REJECTED_BY_USER("7106",
            "DFSP received reject by user authorization, so transaction cancelled"),
    TP_FSP_TRANSACTION_AUTHORIZATION_UNEXPECTED("7107", "DFSP received unexpected authorization responseType"),
    TP_FSP_TRANSACTION_TRANSFER_FAILED("7108", "Transaction request transfer failed"),
    TP_FSP_TRANSACTION_NOTIFICATION_FAILED("7109", "Transaction request notification failed"),

    TP_ACCOUNT_LINKING_ERROR("7200", "Generic Thirdparty account linking error"),
    TP_NO_SERVICE_PROVIDERS_FOUND("7201", "No thirdparty enabled FSP found"),
    TP_NO_ACCOUNTS_FOUND("7202", "No accounts found for generic ID"),
    TP_NO_SUPPORTED_AUTH_CHANNELS("7203", "FSP does not support any requested authentication channels"),
    TP_NO_SUPPORTED_SCOPE_ACTIONS("7204", "FSP does not support any requested scope actions"),
    TP_OTP_VALIDATION_ERROR("7205", "OTP failed validation"),
    TP_FSP_OTP_VALIDATION_ERROR("7206", "FSP failed to validate OTP"),
    TP_FSP_CONSENT_SCOPES_ERROR("7207", "FSP failed retrieve scopes for consent request"),
    TP_CONSENT_REQ_VALIDATION_ERROR("7208", "FSP failed to validate consent request"),
    TP_FSP_CONSENT_REQ_NO_SCOPES("7209", "FSP does not find scopes suitable"),
    TP_NO_TRUSTED_CALLBACK_URI("7210", "FSP does not trust PISP callback URI"),
    TP_CONSENT_REQ_USER_NOT_ALLOWED("7211", "FSP does not allow consent requests for specified username"),
    TP_SIGNED_CHALLENGE_MISMATCH("7212", "Signed challenge does not match derived challenge"),
    TP_CONSENT_INVALID("7213", "Consent is invalid"),
    TP_FAILED_REG_ACCOUNT_LINKS("7214", "Failed to register account links with oracle"),
    TP_AUTH_SERVICE_ERROR("7300", "Generic Thirdparty auth service error");

    private final String code;

    private final String message;

    private final int httpStatusCode;

    MojaloopApiErrorCodes(String code, String message) {

        this.code = code;
        this.message = message;
        this.httpStatusCode = 500; // Default HTTP status code for server errors
    }

    MojaloopApiErrorCodes(String code, String message, int httpStatusCode) {

        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    public String getCode() {

        return code;
    }

    public String getMessage() {

        return message;
    }

    public int getHttpStatusCode() {

        return httpStatusCode;
    }
}

