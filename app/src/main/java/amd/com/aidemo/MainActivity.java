package amd.com.aidemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    private EditText editTextIpAddress;
    private Button buttonConnect;
    private SharedPreferences sharedPreferences;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handleSSLHandshake();
        editTextIpAddress = findViewById(R.id.editTextIpAddress);
        buttonConnect = findViewById(R.id.buttonConnect);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clConnectActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set default IP address and enable the connect button by default
        buttonConnect.setEnabled(true);
        buttonConnect.setBackground(ContextCompat.getDrawable(this, R.drawable.button_background));
        editTextIpAddress.setText("89.216.103.191:3000");

        editTextIpAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ipAddress = s.toString().trim();
                viewModel.setIpAddress(ipAddress);
                viewModel.setButtonEnabled(ipAddress.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        buttonConnect.setOnClickListener(v -> {
            String ipAddress = editTextIpAddress.getText().toString().trim();
            checkServerHealth(ipAddress);
        });

        viewModel.getIpAddress().observe(this, ipAddress -> {
            if (ipAddress != null && !ipAddress.equals(editTextIpAddress.getText().toString().trim())) {
                editTextIpAddress.setText(ipAddress);
            }
        });

        viewModel.isButtonEnabled().observe(this, enabled -> {
            if (enabled != null) {
                buttonConnect.setEnabled(enabled);
                Drawable background = ContextCompat.getDrawable(this, enabled ? R.drawable.button_background : R.drawable.button_background_disabled);
                buttonConnect.setBackground(background);
            }
        });

        // Check if the field is empty initially and set the button state
        String initialIpAddress = editTextIpAddress.getText().toString().trim();
        viewModel.setButtonEnabled(!initialIpAddress.isEmpty());
    }

    private void checkServerHealth(String ipAddress) {
        String url = "https://" + ipAddress + "/health";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if ("ok".equals(response.getString("status"))) {
                            savePreferences(ipAddress);
                            Toast.makeText(MainActivity.this, "Connected to Adrenalin AI server", Toast.LENGTH_SHORT).show();
                            openChatActivity();
                        } else {
                            Toast.makeText(MainActivity.this, "Error on checking server health", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error in response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(MainActivity.this, "Can't connect to the server, there is no response.", Toast.LENGTH_SHORT).show());

        queue.add(jsonObjectRequest);
    }

    private void savePreferences(String ipAddress) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ipAddress", ipAddress);
        editor.apply();
    }

    private void openChatActivity() {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
        finish();
    }
    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

}
