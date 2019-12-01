package chtgupta.selectivesilence.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import chtgupta.selectivesilence.BuildConfig;
import chtgupta.selectivesilence.R;
import chtgupta.selectivesilence.activity.AboutActivity;
import chtgupta.selectivesilence.activity.ContactsActivity;
import chtgupta.selectivesilence.activity.MainActivity;
import chtgupta.selectivesilence.data.Constants;

import static android.app.Activity.RESULT_OK;
import static chtgupta.selectivesilence.activity.MainActivity.PERMISSIONS;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends PreferenceFragmentCompat {

    private Context context;

    private Preference contactsPreference;

    private static final int RC_ACTIVITY_WHITELIST = 420;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SwitchPreference statusPreference = findPreference(getString(R.string.preference_key_status));
        contactsPreference = findPreference(getString(R.string.preference_key_contacts));
        Preference sharePreference = findPreference(getString(R.string.preference_key_share));
        Preference aboutPreference = findPreference(getString(R.string.preference_key_about));

        if (statusPreference != null) {

            if (!permissionsGranted() && statusPreference.isChecked()) {
                statusPreference.setChecked(false);
            }

        }

        if (contactsPreference != null) {

            SharedPreferences contacts = context.getSharedPreferences(Constants.DB_NAME_CONTACTS, Context.MODE_PRIVATE);

            int whitelistSize = contacts.getAll().size();
            showWhitelistSize(whitelistSize);

            contactsPreference.setOnPreferenceClickListener(v -> {
                startActivityForResult(new Intent(context, ContactsActivity.class), RC_ACTIVITY_WHITELIST);
                return true;
            });
        }

        if (sharePreference != null) {
            sharePreference.setOnPreferenceClickListener(v -> {

                ShareCompat.IntentBuilder.from((MainActivity) context)
                        .setType("text/plain")
                        .setChooserTitle("Share via...")
                        .setText("I use " + getString(R.string.app_name) + " to avoid missing important calls even in silent mode. Get it for free at http://play.google.com/store/apps/details?id=" + context.getPackageName())
                        .startChooser();

                return true;
            });
        }

        if (aboutPreference != null) {
            aboutPreference.setSummary("Version " + BuildConfig.VERSION_NAME);
            aboutPreference.setOnPreferenceClickListener(v -> {
                startActivity(new Intent(context, AboutActivity.class));
                return true;
            });
        }

    }

    private void showWhitelistSize(int size) {

        if (size == 0) {
            contactsPreference.setSummary(R.string.preference_summary_contacts);
        } else if (size == 1) {
            contactsPreference.setSummary(size + " contact added");
        } else {
            contactsPreference.setSummary(size + " contacts added");
        }

    }

    private boolean permissionsGranted() {
        return ContextCompat.checkSelfPermission(context, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_ACTIVITY_WHITELIST && resultCode == RESULT_OK && data != null) {

            int whitelistSize = data.getIntExtra(Constants.INTENT_KEY_WHITELIST_SIZE, 0);
            if (contactsPreference != null) {
                showWhitelistSize(whitelistSize);
            }
        }

    }
}
