package com.featureflag.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserContext {

    private String userId;
    private String email;
    private String country;

    public String getEmailDomain() {
        if (email == null || !email.contains("@")) {
            return null;
        }
        return email.substring(email.indexOf("@"));
    }
}
