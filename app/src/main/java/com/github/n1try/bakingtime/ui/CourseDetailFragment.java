package com.github.n1try.bakingtime.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.model.Course;
import com.github.n1try.bakingtime.model.CourseFaculty;
import com.github.n1try.bakingtime.utils.BasicUtils;
import com.github.n1try.bakingtime.utils.Constants;

import java.util.Set;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class CourseDetailFragment extends Fragment {
    @BindView(R.id.faculties_tv)
    TextView facultiesText;
    @BindView(R.id.topics_gv)
    GridView topicsList;

    private Course mCourse;
    private CourseTopicsAdapter mTopicsAdapter;
    private OnCourseTopicSelectedListener onTopicSelectedListener;
    private boolean isTablet;

    public static CourseDetailFragment newInstance(Course course) {
        CourseDetailFragment fragment = new CourseDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_COURSE, course);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            onTopicSelectedListener = (OnCourseTopicSelectedListener) getActivity();
        } catch (ClassCastException e) {
            Log.w(getTag(), "Could not bind OnCourseTopicSelectedListener to fragment");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourse = getArguments().getParcelable(Constants.KEY_COURSE);
        mTopicsAdapter = new CourseTopicsAdapter(getContext(), mCourse.getTopics());
        getActivity().setTitle(BasicUtils.styleTitle(mCourse.getName()));
        isTablet = getResources().getBoolean(R.bool.is_tablet);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Set<String> formattedFaculties = mCourse.getFaculties().stream()
                .map(CourseFaculty::toString)
                .collect(Collectors.toSet());
        sharedPreferences.edit().putInt(Constants.PREF_KEY_LAST_COURSE_ID, mCourse.getId()).apply();
        sharedPreferences.edit().putString(Constants.PREF_KEY_LAST_COURSE_NAME, mCourse.getName()).apply();
        sharedPreferences.edit().putStringSet(Constants.PREF_KEY_LAST_COURSE_FACULTIES, formattedFaculties).apply();
    }

    @OnItemClick(R.id.topics_gv)
    void onItemClick(int position) {
        if (onTopicSelectedListener == null) return;
        onTopicSelectedListener.onTopicSelected(mCourse, position);
        if (isTablet) mTopicsAdapter.setActiveIndex(position);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail_overview, container, false);
        ButterKnife.bind(this, view);

        facultiesText.setText(TextUtils.concat((CharSequence[]) mCourse.getFaculties().stream().map(CourseFaculty::format).toArray(i -> new CharSequence[i])));
        topicsList.setAdapter(mTopicsAdapter);
        BasicUtils.justifyListViewHeightBasedOnChildren(topicsList);
        if (isTablet) onItemClick(0);
        return view;
    }

    interface OnCourseTopicSelectedListener {
        void onTopicSelected(Course course, int index);
    }
}
