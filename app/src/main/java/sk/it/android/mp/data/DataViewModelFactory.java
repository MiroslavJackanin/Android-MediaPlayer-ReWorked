package sk.it.android.mp.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class DataViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final OnTracksLoadListener onTracksLoadListener;

    public DataViewModelFactory(Application application, OnTracksLoadListener onTracksLoadListener) {
        this.application = application;
        this.onTracksLoadListener = onTracksLoadListener;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DataViewModel(application, onTracksLoadListener);
    }
}
