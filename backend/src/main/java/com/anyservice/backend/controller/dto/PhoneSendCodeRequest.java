package com.anyservice.backend.controller.dto;

public class PhoneSendCodeRequest {
    private String countryCode;
    private String phone;

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
