package com.github.n1try.bakingtime.services;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.github.n1try.bakingtime.model.Course;
import com.github.n1try.bakingtime.model.CourseTopic;
import com.github.n1try.bakingtime.utils.SerializationUtils;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CourseApiService {
    private static CourseApiService ourInstance;
    private static final String COURSE_ENDPOINT = "https://iamoffended.github.io/json_sample.json";
    private static final String[] SUPPORTED_VIDEO_EXTENSIONS = new String[]{"mp4"};
    private static final String[] SUPPORTED_IMAGE_EXTENSIONS = new String[]{"jpg", "jpeg", "png"};
    private static Type courseListType = new TypeToken<ArrayList<Course>>() {
    }.getType();

    private OkHttpClient mHttpClient;
    private ArrayList<Course> coursesCache;

    public static CourseApiService getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new CourseApiService(context);
        }
        return ourInstance;
    }

    private CourseApiService(Context context) {
        mHttpClient = new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir(), 1024 * 1014 * 10))
                .build();
    }

    public List<Course> fetchCourses() {
        Uri fetchUri = Uri.parse(COURSE_ENDPOINT);
        Request request = new Request.Builder().url(fetchUri.toString()).build();

        try {
            Response response = mHttpClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException(response.message());
            ResponseBody body = response.body();
            List<Course> courses = SerializationUtils.deserializeList(body.string(), courseListType)
                    .stream()
                    .map(r -> tryHealConfusedLinks((Course) r))
                    .collect(Collectors.toList());
            body.close();
            coursesCache = new ArrayList<>(courses);
            return courses;
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(), "Could not fetch courses.\n" + e.getMessage());
        }
        coursesCache = new ArrayList<>();
        return new ArrayList<>();
    }

    private Course tryHealConfusedLinks(Course course) {
        for (CourseTopic topic : course.getTopics()) {
            String videoUrl = topic.getVideoUrl();
            String thumbnailUrl = topic.getThumbnailUrl();
            String newVideoUrl = videoUrl;
            String newThumbnailUrl = thumbnailUrl;

            if (!TextUtils.isEmpty(videoUrl)) {
                if (!checkExtension(videoUrl, SUPPORTED_VIDEO_EXTENSIONS)) {
                    if (checkExtension(videoUrl, SUPPORTED_IMAGE_EXTENSIONS)) {
                        newThumbnailUrl = videoUrl;
                    }
                    newVideoUrl = null;
                }
            }

            if (!TextUtils.isEmpty(thumbnailUrl)) {
                if (!checkExtension(thumbnailUrl, SUPPORTED_IMAGE_EXTENSIONS)) {
                    if (checkExtension(thumbnailUrl, SUPPORTED_VIDEO_EXTENSIONS)) {
                        newVideoUrl = thumbnailUrl;
                    }
                    newThumbnailUrl = null;
                }
            }

            topic.setVideoUrl(newVideoUrl);
            topic.setThumbnailUrl(newThumbnailUrl);
        }
        return course;
    }

    private static boolean checkExtension(String url, String[] extensions) {
        return Arrays.stream(extensions).anyMatch(e -> url.toLowerCase().endsWith(e));
    }

    public Optional<Course> getOrFetchById(int id) {
        if (coursesCache == null || coursesCache.isEmpty()) {
            fetchCourses();
        }
        return coursesCache.stream().filter(r -> r.getId() == id).findFirst();
    }

    public List<Course> getCoursesCache() {
        return coursesCache;
    }

    public void invalidateCoursesCache() {
        coursesCache = null;
    }
}
