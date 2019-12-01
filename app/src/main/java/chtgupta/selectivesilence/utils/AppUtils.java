package chtgupta.selectivesilence.utils;

public class AppUtils {

    public static String processPhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("\\D+", "");
    }
}
