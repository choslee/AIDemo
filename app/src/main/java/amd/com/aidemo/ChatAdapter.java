package amd.com.aidemo;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewMessage;
        private FrameLayout messageContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            messageContainer = itemView.findViewById(R.id.messageContainer);
        }

        public void bind(Message message) {
            textViewMessage.setText(message.getText());
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textViewMessage.getLayoutParams();
            if (message.isUser()) {
                textViewMessage.setBackgroundResource(R.drawable.user_message_background);
                layoutParams.gravity = Gravity.END;
            } else {
                textViewMessage.setBackgroundResource(R.drawable.ai_message_background);
                layoutParams.gravity = Gravity.START;
            }
            textViewMessage.setLayoutParams(layoutParams);
        }
    }
}
