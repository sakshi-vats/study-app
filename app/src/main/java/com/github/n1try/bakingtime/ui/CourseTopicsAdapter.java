package com.github.n1try.bakingtime.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.model.CourseTopic;

import java.util.List;

public class CourseTopicsAdapter extends ArrayAdapter<CourseTopic> {
    private Context context;
    private List<CourseTopic> topics;
    private int activeIndex = -1;

    public CourseTopicsAdapter(@NonNull Context context, @NonNull List<CourseTopic> topics) {
        super(context, 0, topics);
        this.context = context;
        this.topics = topics;
    }

    public void setActiveIndex(int index) {
        activeIndex = index;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.course_topic_item, parent, false);
        }

        CourseTopic topic = getItem(position);
        View innerTopicContainer = convertView.findViewById(R.id.inner_topic_container);
        TextView stepTitle = convertView.findViewById(R.id.topic_title_tv);
        TextView stepIndex = convertView.findViewById(R.id.topic_index_tv);
        stepIndex.setText(String.valueOf(position + 1));
        stepTitle.setText(topic.getSubTopic());

        if (position == activeIndex) {
            innerTopicContainer.setBackground(context.getDrawable(R.drawable.rounded_box_filled));
        } else {
            innerTopicContainer.setBackground(context.getDrawable(R.drawable.rounded_box));
        }

        return convertView;
    }
}
