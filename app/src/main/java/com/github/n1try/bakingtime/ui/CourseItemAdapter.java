package com.github.n1try.bakingtime.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.model.Course;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CourseItemAdapter extends ArrayAdapter<Course> {
    private Context context;
    private List<Drawable> defaultImages;
    private List<Course> courses;

    public CourseItemAdapter(@NonNull Context context, @NonNull List courses) {
        super(context, 0, courses);
        this.context = context;
        this.courses = courses;

        defaultImages = Arrays.stream(context.getResources().getStringArray(R.array.default_course_images))
                .map(i -> context.getResources().getIdentifier(i, "drawable", context.getPackageName()))
                .map(id -> context.getDrawable(id))
                .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.course_card_item, parent, false);
        }

        final ImageView courseImage = convertView.findViewById(R.id.course_item_iv);
        final TextView courseText = convertView.findViewById(R.id.course_item_name_tv);

        final Course course = getItem(position);
        courseText.setText(course.getName());
        if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
            Picasso.with(getContext()).load(course.getImageUrl()).into(courseImage);
        } else {
            Drawable image = defaultImages.get(position % defaultImages.size());
            courseImage.setImageDrawable(image);
        }

        return convertView;
    }
}
