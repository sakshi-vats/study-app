package com.github.n1try.bakingtime.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.n1try.bakingtime.utils.SerializationUtils;
import com.google.gson.annotations.SerializedName;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(of = "shortDescription")
public class CourseTopic implements Parcelable {
    private int id;
    private String subTopic;
    private String description;
    @SerializedName("videoURL")
    private String videoUrl;
    @SerializedName("thumbnailURL")
    private String thumbnailUrl;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(SerializationUtils.serialize(this));
    }

    private static CourseTopic readFromParcel(Parcel parcel) {
        try {
            return SerializationUtils.deserialize(parcel.readString(), CourseTopic.class);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static final Creator<CourseTopic> CREATOR = new Creator<CourseTopic>() {
        @Override
        public CourseTopic createFromParcel(Parcel in) {
            return readFromParcel(in);
        }

        @Override
        public CourseTopic[] newArray(int size) {
            return new CourseTopic[size];
        }
    };
}
