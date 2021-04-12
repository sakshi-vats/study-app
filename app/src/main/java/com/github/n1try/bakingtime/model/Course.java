package com.github.n1try.bakingtime.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.github.n1try.bakingtime.utils.SerializationUtils;
import com.google.gson.annotations.SerializedName;

import java.util.List;

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
@ToString(of = "name")
public class Course implements Parcelable {
    private int id;
    private int duration;
    private String name;
    @SerializedName("image")
    private String imageUrl;
    private List<CourseFaculty> faculties;
    private List<CourseTopic> topics;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(SerializationUtils.serialize(this));
    }

    private static Course readFromParcel(Parcel parcel) {
        try {
            return SerializationUtils.deserialize(parcel.readString(), Course.class);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) {
            return readFromParcel(in);
        }

        @Override
        public Course[] newArray(int size) {
            return new Course[size];
        }
    };
}
