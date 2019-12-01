package chtgupta.selectivesilence.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import chtgupta.selectivesilence.R;

public class MainActivity extends AppCompatActivity {

    private static final int RC_PERMISSIONS = 69;
    public static final String[] PERMISSIONS = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS};

    private View permissionsLayout;
    private View overlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionsLayout = findViewById(R.id.permissionsLayout);
        overlay = findViewById(R.id.overlay);
        Button grant = findViewById(R.id.grant);

        grant.setOnClickListener(v -> requestPermissions());
        overlay.setOnClickListener(v -> {});

        setup();
    }

    private void setup() {
        permissionsLayout.setVisibility(permissionsGranted() ? View.GONE : View.VISIBLE);
        overlay.setVisibility(permissionsGranted() ? View.GONE : View.VISIBLE);
    }

    private boolean permissionsGranted() {
        return ActivityCompat.checkSelfPermission(this, PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, PERMISSIONS, RC_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == RC_PERMISSIONS) {
            setup();
        }
    }
}
