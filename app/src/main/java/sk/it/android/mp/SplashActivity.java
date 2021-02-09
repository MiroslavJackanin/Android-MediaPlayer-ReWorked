package sk.it.android.mp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class SplashActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 1;
    private static final String PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    SharedPreferences sharedPreferences;
    boolean isPermissionDenied;

    TextView textView;
    Button btnGrant;
    Button btnExit;
    Button btnSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("PermissionPrefs", MODE_PRIVATE);
        isPermissionDenied = sharedPreferences.getBoolean("isPermissionDenied", false);

        if (ActivityCompat.checkSelfPermission(this, PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            if (isPermissionDenied) {
                btnGrant = findViewById(R.id.buttonGrant);
                btnExit = findViewById(R.id.buttonExit);
                btnSettings = findViewById(R.id.buttonSettings);
                textView = findViewById(R.id.textView);

                textView.setText("Permission has been denied.\nYou can still change your permissions in settings");
                btnGrant.setVisibility(View.INVISIBLE);
                btnSettings.setVisibility(View.VISIBLE);
            } else {
                btnGrant = findViewById(R.id.buttonGrant);
                btnExit = findViewById(R.id.buttonExit);
                btnSettings = findViewById(R.id.buttonSettings);
                textView = findViewById(R.id.textView);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, PERMISSION) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void exit(View view) {
        finishAndRemoveTask();
    }

    public void requestPermission(View view) {
        ActivityCompat.requestPermissions(this, new String[]{PERMISSION}, PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                isPermissionDenied = true;
                editor.putBoolean("isPermissionDenied", true);
                editor.apply();

                textView.setText("Permission has been denied\nYou can still change your permissions in settings");
                btnGrant.setVisibility(View.INVISIBLE);
                btnSettings.setVisibility(View.VISIBLE);
            }
        }
    }

    public void goToSettings(View view) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}