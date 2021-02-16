package sk.it.android.mp.data;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;

import sk.it.android.mp.util.Track;

public class DataViewModel extends AndroidViewModel {

    private final LiveData<ArrayList<Track>> tracks;

    public DataViewModel(@NonNull Application application) {
        super(application);
        DataRepository dataRepository = new DataRepository(application);
        tracks = dataRepository.getLiveData();
    }

    public LiveData<ArrayList<Track>> getTracks() {
        return tracks;
    }
}