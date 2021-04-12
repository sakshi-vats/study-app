package com.github.n1try.bakingtime.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.idling.CountingIdlingResource;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridView;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.model.Course;
import com.github.n1try.bakingtime.services.CourseApiService;
import com.github.n1try.bakingtime.utils.BasicUtils;
import com.github.n1try.bakingtime.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.offline_container)
    View mOfflineContainer;
    @BindView(R.id.course_overview_gv)
    GridView mCourseOverviewGv;

    private CourseApiService mApiService;
    private CourseItemAdapter mCourseItemAdapter;
    private List<Course> mCourses;
    private boolean isTablet;
    private CountingIdlingResource mIdlingResource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(BasicUtils.styleTitle(getResources().getString(R.string.app_name)));

        mApiService = CourseApiService.getInstance(this);
        getIdlingResource();

        isTablet = getResources().getBoolean(R.bool.is_tablet);
        mCourseItemAdapter = new CourseItemAdapter(this, new ArrayList());
        mCourseOverviewGv.setAdapter(mCourseItemAdapter);
        if (isTablet) mCourseOverviewGv.setNumColumns(getResources().getInteger(R.integer.num_cols_tablet));

        if (savedInstanceState != null) {
            mCourses = savedInstanceState.getParcelableArrayList(Constants.KEY_COURSE_LIST);
            populateAdapter();
        } else {
            if (BasicUtils.isNetworkAvailable(this)) {
                new FetchCoursesTask().execute();
                mIdlingResource.increment();
            } else {
                mCourseOverviewGv.setVisibility(View.GONE);
                mOfflineContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCourses != null) {
            outState.putParcelableArrayList(Constants.KEY_COURSE_LIST, new ArrayList<>(mCourses));
        }
    }

    @OnItemClick(R.id.course_overview_gv)
    void onItemClick(int position) {
        Intent intent = new Intent(MainActivity.this, CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE, mCourses.get(position));
        startActivity(intent);
    }

    private void populateAdapter() {
        mCourseItemAdapter.clear();
        mCourseItemAdapter.addAll(mCourses);
        mCourseItemAdapter.notifyDataSetChanged();
    }

    @VisibleForTesting
    @NonNull
    public IdlingResource getIdlingResource() {
        if (mIdlingResource == null) {
            mIdlingResource = new CountingIdlingResource(getLocalClassName());
        }
        return mIdlingResource;
    }

    class FetchCoursesTask extends AsyncTask<Void, Void, List<Course>> {
        @Override
        protected List<Course> doInBackground(Void... voids) {
            return mApiService.fetchCourses();
        }

        @Override
        protected void onPostExecute(List<Course> courses) {
            mCourses = courses;
            populateAdapter();
            mIdlingResource.decrement();
        }
    }
}
