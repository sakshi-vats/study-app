package com.github.n1try.bakingtime.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.model.Course;
import com.github.n1try.bakingtime.utils.Constants;

public class TopicDetailActivity extends AppCompatActivity implements TopicDetailFragment.OnCourseTopicChangeListener {
    private Course mCourse;
    private int mTopicIndex;
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_detail);

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        mCourse = bundle.getParcelable(Constants.KEY_COURSE);
        mTopicIndex = bundle.getInt(Constants.KEY_COURSE_TOPIC_INDEX, 0);

        fragmentManager = getSupportFragmentManager();
        spawnFragment();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.KEY_COURSE, mCourse);
        outState.putInt(Constants.KEY_COURSE_TOPIC_INDEX, mTopicIndex);
    }

    private void spawnFragment() {
        String tag = String.valueOf(mCourse.getTopics().get(mTopicIndex).hashCode());
        if (fragmentManager.findFragmentByTag(tag) == null) {
            Fragment fragment = TopicDetailFragment.newInstance(mCourse, mTopicIndex);
            fragmentManager.beginTransaction().replace(R.id.detail_topic_container, fragment, tag).commit();
        }
    }

    @Override
    public void onNextTopic(int currentTopicIndex) {
        mTopicIndex++;
        spawnFragment();
    }

    @Override
    public void onPreviousTopic(int currentTopicIndex) {
        mTopicIndex--;
        spawnFragment();
    }
}
