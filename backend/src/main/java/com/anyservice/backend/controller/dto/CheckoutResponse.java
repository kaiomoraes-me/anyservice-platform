package com.anyservice.backend.controller.dto;

public class CheckoutResponse {
    private String checkoutUrl;

    public CheckoutResponse(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }

    public String getCheckoutUrl() {
        return checkoutUrl;
    }

    public void setCheckoutUrl(String checkoutUrl) {
        this.checkoutUrl = checkoutUrl;
    }
}
