package com.github.n1try.bakingtime.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.model.Course;
import com.github.n1try.bakingtime.services.CourseApiService;
import com.github.n1try.bakingtime.utils.Constants;

public class CourseDetailActivity extends AppCompatActivity implements CourseDetailFragment.OnCourseTopicSelectedListener, TopicDetailFragment.OnCourseTopicChangeListener {
    private FragmentManager mFragmentManager;
    private CourseApiService mApiService;
    private boolean isTablet;
    private Course mCourse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mApiService = CourseApiService.getInstance(getApplicationContext());

        Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();
        if (bundle.containsKey(Constants.KEY_COURSE)) {
            mCourse = bundle.getParcelable(Constants.KEY_COURSE);
        } else if (bundle.containsKey(Constants.KEY_COURSE_ID)) {
            mCourse = mApiService.getOrFetchById(bundle.getInt(Constants.KEY_COURSE_ID)).get(); // handle null case
        }
        mFragmentManager = getSupportFragmentManager();
        isTablet = getResources().getBoolean(R.bool.is_tablet);

        if (mFragmentManager.findFragmentByTag(Constants.TAG_DETAIL_FRAGMENT) == null) {
            Fragment fragment = CourseDetailFragment.newInstance(mCourse);
            mFragmentManager.beginTransaction().replace(R.id.detail_overview_container, fragment, Constants.TAG_DETAIL_FRAGMENT).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCourse != null) {
            outState.putParcelable(Constants.KEY_COURSE, mCourse);
        }
    }

    @Override
    public void onTopicSelected(Course course, int index) {
        if (isTablet) {
            spawnTopicFragment(index);
        } else {
            Intent intent = new Intent(CourseDetailActivity.this, TopicDetailActivity.class);
            intent.putExtra(Constants.KEY_COURSE, course);
            intent.putExtra(Constants.KEY_COURSE_TOPIC_INDEX, index);
            startActivity(intent);
        }
    }

    private void spawnTopicFragment(int topicIndex) {
        String tag = String.valueOf(mCourse.getTopics().get(topicIndex).hashCode());
        if (mFragmentManager.findFragmentByTag(tag) == null) {
            Fragment fragment = TopicDetailFragment.newInstance(mCourse, topicIndex);
            mFragmentManager.beginTransaction().replace(R.id.detail_topic_container, fragment, tag).commit();
        }
    }

    @Override
    public void onNextTopic(int currentTopicIndex) {
        if (!isTablet) return;
        spawnTopicFragment(++currentTopicIndex);
    }

    @Override
    public void onPreviousTopic(int currentTopicIndex) {
        if (!isTablet) return;
        spawnTopicFragment(--currentTopicIndex);
    }
}
