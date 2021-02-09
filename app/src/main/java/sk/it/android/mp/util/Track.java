package sk.it.android.mp.util;

import android.os.Parcel;
import android.os.Parcelable;

public class Track implements Parcelable {

    private long id;
    private String title;
    private String album;
    private String artist;
    private String duration;
    private String durationReadable;

    public Track(long id, String title, String album, String artist, String duration) {
        this.id = id;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.durationReadable = convertToReadable(duration);
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }
    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDurationReadable() {
        return durationReadable;
    }
    public void setDurationReadable(String durationReadable) {
        this.durationReadable = durationReadable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(album);
        dest.writeString(artist);
        dest.writeString(duration);
        dest.writeString(durationReadable);
    }

    public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>() {
        public Track createFromParcel(Parcel in) {
            return new Track(in);
        }

        public Track[] newArray(int size) {
            return new Track[size];
        }
    };

    public Track(Parcel in) {
        id = in.readLong();
        title = in.readString();
        album = in.readString();
        artist = in.readString();
        duration = in.readString();
        durationReadable = in.readString();
    }

    public String convertToReadable(String duration) {
        return formatMilliSecond(Long.parseLong(duration));
    }

    public static String formatMilliSecond(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    }
}
