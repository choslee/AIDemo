package amd.com.aidemo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());

    public void addMessage(Message message) {
        List<Message> currentMessages = messages.getValue();
        if (currentMessages != null) {
            currentMessages.add(message);
            messages.setValue(currentMessages);
        }
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }
}
