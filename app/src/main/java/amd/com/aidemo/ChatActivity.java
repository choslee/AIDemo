package amd.com.aidemo;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private EditText editTextMessage;
    private ImageButton buttonSend;
    private ChatViewModel viewModel;
    private RequestQueue requestQueue;
    private String chatId = "";
    private SharedPreferences sharedPreferences;
    private String serverIpAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        handleSSLHandshake();
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        requestQueue = Volley.newRequestQueue(this);

        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        serverIpAddress = sharedPreferences.getString("ipAddress", "");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.clChatActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        chatAdapter = new ChatAdapter(viewModel.getMessages().getValue());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        buttonSend.setOnClickListener(v -> {
            String messageText = editTextMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                Message userMessage = new Message(messageText, true);
                viewModel.addMessage(userMessage);
                editTextMessage.setText("");

                sendMessageToServer(messageText);
            }
        });

        viewModel.getMessages().observe(this, new Observer<List<Message>>() {
            @Override
            public void onChanged(List<Message> messages) {
                chatAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messages.size() - 1);
            }
        });
    }

    private void sendMessageToServer(String messageText) {
        String url = "https://" + serverIpAddress + "/v1/chat/simple";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("chat_id", chatId);
            requestBody.put("message", messageText);
            requestBody.put("topic_id", "amd");
            requestBody.put("history", "");
            requestBody.put("extra", "");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        chatId = response.getString("chat_id");
                        if (response.getInt("error") == 0) {
                            getMessageFromServer(chatId);
                        } else {
                            Toast.makeText(ChatActivity.this, "Error: " + response.getInt("error"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ChatActivity.this, "Error in response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ChatActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
    }

    private void getMessageFromServer(String chatId) {
        String url = "https://" + serverIpAddress + "/v1/chat/simple?chat_id=" + chatId;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        String aiMessage = response.getString("message");
                        boolean completed = response.getBoolean("completed");
                        Message message = new Message(aiMessage, false);
                        viewModel.addMessage(message);

                        if (!completed) {
                            // Continue polling until message is completed
                            getMessageFromServer(chatId);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(ChatActivity.this, "Error in response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ChatActivity.this, "Can't connect to the server", Toast.LENGTH_SHORT).show());

        requestQueue.add(jsonObjectRequest);
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
