package com.nic.nerie.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {
    // TODO @Toiar: Put email length validation constraint
    public static Boolean isEmailValid(String email) {
        String emailRegex = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailRegex);

        if (email != null && !email.isBlank()) {
            Matcher matcher = pattern.matcher(email.trim());
            if (matcher.matches())
                return true;
        }

        return false;
    }
}