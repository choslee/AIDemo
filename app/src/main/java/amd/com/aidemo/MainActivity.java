package amd.com.aidemo;

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

public class MainActivity extends AppCompatActivity {

    private EditText editTextIpAddress;
    private Button buttonConnect;
    private SharedPreferences sharedPreferences;
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextIpAddress = findViewById(R.id.editTextIpAddress);
        buttonConnect = findViewById(R.id.buttonConnect);
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clConnectActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
            connectToServer(ipAddress);
            openChatActivity();
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


//                buttonConnect.setBackgroundResource(enabled ? R.drawable.button_background : R.drawable.button_background_disabled);
            }
        });
    }

    private void connectToServer(String ipAddress) {
        String url = "http://" + ipAddress + "/api/connect"; // Adjust the URL as needed
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String serverName = response.getString("nazivservera");
                        savePreferences(ipAddress, serverName);
                        openChatActivity();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Error in response", Toast.LENGTH_SHORT).show();
                    }
                }, error -> Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show());

        queue.add(jsonObjectRequest);
    }

    private void savePreferences(String ipAddress, String serverName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ipAddress", ipAddress);
        editor.putString("serverName", serverName);
        editor.apply();
    }

    private void openChatActivity() {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        startActivity(intent);
        finish();
    }
}
