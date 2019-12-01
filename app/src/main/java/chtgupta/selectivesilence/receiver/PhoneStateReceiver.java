package chtgupta.selectivesilence.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.telephony.TelephonyManager;

import androidx.preference.PreferenceManager;

import java.util.Set;

import chtgupta.selectivesilence.R;
import chtgupta.selectivesilence.data.Constants;
import chtgupta.selectivesilence.utils.AppUtils;

public class PhoneStateReceiver extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = preferences.getBoolean(context.getString(R.string.preference_key_status), false);
        if (!enabled) {
            // Selective Silence not enabled, should return.
            return;
        }

        try {

            SharedPreferences contacts = context.getSharedPreferences(Constants.DB_NAME_CONTACTS, Context.MODE_PRIVATE);

            Set<String> whitelist = contacts.getAll().keySet();
            if (whitelist.size() == 0) return;

            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

            if (state == null || incomingNumber == null) {
                // just making a few checks, I don't like NPEs
                return;
            }

            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                // fired when device rings

                if (anyOfSetInString(AppUtils.processPhoneNumber(incomingNumber), whitelist)) {
                    if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE) {
                        am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                        scheduleRollbackForNumber(context, AppUtils.processPhoneNumber(incomingNumber));
                    }
                }
            } else if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                // fired when phone hangs up

                if (anyOfSetInString(AppUtils.processPhoneNumber(incomingNumber), whitelist)) {

                    if (am != null && am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL && shouldRollBack(context, AppUtils.processPhoneNumber(incomingNumber))) {
                        am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                        rollbackForNumber(context, AppUtils.processPhoneNumber(incomingNumber));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean anyOfSetInString(String inputStr, Set<String> items) {
        // Expectations: Arrays.stream(), Reality: loop

        for (String s : items) {
            if (inputStr.contains(s)) {
                // Didn't use String.equals() as user can have the number saved with or without country code

                return true;
            }
        }

        return false;
    }

    private void scheduleRollbackForNumber(Context context, String incomingNumber) {
        SharedPreferences temp = context.getSharedPreferences(Constants.DB_NAME_TEMP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = temp.edit();
        editor.putBoolean(incomingNumber, true);
        editor.apply();
    }

    private boolean shouldRollBack(Context context, String incomingNumber) {
        SharedPreferences temp = context.getSharedPreferences(Constants.DB_NAME_TEMP, Context.MODE_PRIVATE);
        return temp.getBoolean(incomingNumber, false);
    }

    private void rollbackForNumber(Context context, String incomingNumber) {
        SharedPreferences temp = context.getSharedPreferences(Constants.DB_NAME_TEMP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = temp.edit();
        editor.remove(incomingNumber);
        editor.apply();
    }

}
