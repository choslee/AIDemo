package amd.com.aidemo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<String> ipAddress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isButtonEnabled = new MutableLiveData<>(false);

    public void setIpAddress(String ip) {
        ipAddress.setValue(ip);
    }

    public LiveData<String> getIpAddress() {
        return ipAddress;
    }

    public void setButtonEnabled(boolean enabled) {
        isButtonEnabled.setValue(enabled);
    }

    public LiveData<Boolean> isButtonEnabled() {
        return isButtonEnabled;
    }
}
