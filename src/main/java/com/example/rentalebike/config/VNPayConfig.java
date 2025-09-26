package com.example.rentalebike.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class VNPayConfig {

    // Getters
    @Value("${vnpay.tmn.code}")
    private String tmnCode;

    @Value("${vnpay.hash.secret}")
    private String hashSecret;

    @Value("${vnpay.pay.url}")
    private String payUrl;

    @Value("${vnpay.return.url}")
    private String returnUrl;

    @Value("${vnpay.version}")
    private String version;

    @Value("${vnpay.command}")
    private String command;

    @Value("${vnpay.order.type}")
    private String orderType;

    // VNPay constants
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    public static final String VNP_ORDER_TYPE = "other";
    public static final String VNP_CURRENCY_CODE = "VND";
    public static final String VNP_LOCALE = "vn";

    // Response codes
    public static final String VNP_RESPONSE_CODE_SUCCESS = "00";
    public static final String VNP_RESPONSE_CODE_PENDING = "01";
    public static final String VNP_RESPONSE_CODE_ERROR = "02";
    public static final String VNP_RESPONSE_CODE_INVALID_DATA = "04";
    public static final String VNP_RESPONSE_CODE_INVALID_AMOUNT = "05";
    public static final String VNP_RESPONSE_CODE_INVALID_CURRENCY = "06";
    public static final String VNP_RESPONSE_CODE_TRANSACTION_NOT_FOUND = "07";
    public static final String VNP_RESPONSE_CODE_INVALID_SIGNATURE = "97";
    public static final String VNP_RESPONSE_CODE_SYSTEM_ERROR = "99";

    // Transaction status
    public enum TransactionStatus {
        SUCCESS("00", "Giao dịch thành công"),
        PENDING("01", "Giao dịch đang chờ xử lý"),
        ERROR("02", "Giao dịch bị lỗi"),
        INVALID_DATA("04", "Dữ liệu không hợp lệ"),
        INVALID_AMOUNT("05", "Số tiền không hợp lệ"),
        INVALID_CURRENCY("06", "Loại tiền tệ không hợp lệ"),
        TRANSACTION_NOT_FOUND("07", "Không tìm thấy giao dịch"),
        INVALID_SIGNATURE("97", "Chữ ký không hợp lệ"),
        SYSTEM_ERROR("99", "Lỗi hệ thống");

        private final String code;
        private final String message;

        TransactionStatus(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public static TransactionStatus fromCode(String code) {
            for (TransactionStatus status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return SYSTEM_ERROR;
        }
    }
}