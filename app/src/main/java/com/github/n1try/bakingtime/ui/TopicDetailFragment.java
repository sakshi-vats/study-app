package com.github.n1try.bakingtime.ui;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.model.Course;
import com.github.n1try.bakingtime.model.CourseTopic;
import com.github.n1try.bakingtime.utils.BasicUtils;
import com.github.n1try.bakingtime.utils.Constants;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TopicDetailFragment extends Fragment implements Player.EventListener {
    @BindView(R.id.next_topic_fab)
    FloatingActionButton nextFab;
    @BindView(R.id.prev_topic_fab)
    FloatingActionButton prevFab;
    @BindView(R.id.topic_instructions_title_tv)
    TextView topicInstructionsTitle;
    @BindView(R.id.topic_instructions_tv)
    TextView topicInstructions;
    @BindView(R.id.topic_player)
    PlayerView topicPlayerView;
    @BindView(R.id.topic_thumbnail_iv)
    ImageView thumbnailView;

    private Course mCourse;
    private CourseTopic mTopic;
    private int mTopicIndex;
    private OnCourseTopicChangeListener mOnCourseTopicChangeListener;
    private boolean isTablet;
    private ExoPlayer mPlayer;
    private Handler mHandler;
    private long mPlayerStartPosition = 0;
    private boolean mPlayerPlayWhenReady = true;

    public static TopicDetailFragment newInstance(Course course, int topicIndex) {
        TopicDetailFragment fragment = new TopicDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.KEY_COURSE, course);
        bundle.putInt(Constants.KEY_COURSE_TOPIC_INDEX, topicIndex);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCourse = getArguments().getParcelable(Constants.KEY_COURSE);
        mTopicIndex = getArguments().getInt(Constants.KEY_COURSE_TOPIC_INDEX);
        mTopic = mCourse.getTopics().get(mTopicIndex);
        isTablet = getResources().getBoolean(R.bool.is_tablet);
        StringBuilder builder = new StringBuilder(mCourse.getName())
                .append(" - ").append(getResources().getString(R.string.topic)).append(" ")
                .append(mTopicIndex + 1);
        getActivity().setTitle(BasicUtils.styleTitle(builder.toString()));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_topic_detail, container, false);
        ButterKnife.bind(this, view);

        CourseTopic step = mCourse.getTopics().get(mTopicIndex);
        topicInstructions.setText(step.getDescription());
        topicInstructionsTitle.setText(step.getSubTopic());

        if (mTopicIndex == 0 || isTablet) prevFab.setVisibility(View.GONE);
        if (mTopicIndex == mCourse.getTopics().size() - 1 || isTablet) nextFab.setVisibility(View.GONE);

        if (savedInstanceState != null) {
            mPlayerStartPosition = savedInstanceState.getLong(Constants.KEY_PLAYER_POSITION, mPlayerStartPosition);
            mPlayerPlayWhenReady = savedInstanceState.getBoolean(Constants.KEY_PLAYER_PLAY_STATE, mPlayerPlayWhenReady);
        }

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            mOnCourseTopicChangeListener = (OnCourseTopicChangeListener) getActivity();
        } catch (ClassCastException e) {
            Log.w(getTag(), "Could not bind OnCourseTopicChangeListener to fragment");
        }
    }

    @Override
    public void onResume() {
        mPlayer = null;
        if (!TextUtils.isEmpty(mTopic.getVideoUrl())) {
            initPlayer(Uri.parse(mTopic.getVideoUrl()));
        } else {
            showThumbnail();
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        if (mPlayer != null) {
            mPlayerPlayWhenReady = mPlayer.getPlayWhenReady();
            mPlayerStartPosition = mPlayer.getCurrentPosition();
        }
        releasePlayer();
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mPlayer != null) {
            outState.putLong(Constants.KEY_PLAYER_POSITION, mPlayer.getCurrentPosition());
            outState.putBoolean(Constants.KEY_PLAYER_PLAY_STATE, mPlayer.getPlayWhenReady());
        }
    }

    @OnClick(R.id.next_topic_fab)
    void nextTopic() {
        mOnCourseTopicChangeListener.onNextTopic(mTopicIndex);
    }

    @OnClick(R.id.prev_topic_fab)
    void prevTopic() {
        mOnCourseTopicChangeListener.onPreviousTopic(mTopicIndex);
    }

    private void initPlayer(Uri mediaUri) {
        if (mPlayer == null) {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                    getContext(),
                    Util.getUserAgent(
                            getContext(),
                            BasicUtils.getApplicationName(getContext())
                    )
            );
            TrackSelector trackSelector = new DefaultTrackSelector();
            LoadControl loadControl = new DefaultLoadControl();
            RenderersFactory renderersFactory = new DefaultRenderersFactory(getContext());
            mPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
            topicPlayerView.setPlayer(mPlayer);

            mPlayer.addListener(this);

            ExtractorMediaSource.Factory mediaSourceFactory = new ExtractorMediaSource.Factory(dataSourceFactory);
            MediaSource mediaSource = mediaSourceFactory.createMediaSource(mediaUri);
            mPlayer.prepare(mediaSource);
            mPlayer.seekTo(mPlayerStartPosition);
            mPlayer.setPlayWhenReady(mPlayerPlayWhenReady);
        }
    }

    private void releasePlayer() {
        if (mPlayer == null) return;
        mPlayer.stop();
        mPlayer.release();
    }

    private void showPlayer() {
        thumbnailView.setVisibility(View.GONE);
        topicPlayerView.setVisibility(View.VISIBLE);
    }

    private boolean isPlayerShown() {
        return topicPlayerView.getVisibility() == View.VISIBLE;
    }

    private void showThumbnail() {
        topicPlayerView.setVisibility(View.GONE);
        thumbnailView.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(mTopic.getThumbnailUrl())) {
            Picasso.with(getContext()).load(mTopic.getThumbnailUrl()).into(thumbnailView);
            thumbnailView.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (mHandler == null) {
            mHandler = new Handler();
            Runnable checkBuffering = () -> {
                if (mPlayer == null) return;
                if (mPlayer.getBufferedPercentage() < 10) {
                    releasePlayer();
                    showThumbnail();
                }
            };
            mHandler.postDelayed(checkBuffering, Math.round(Constants.VIDEO_MAX_LOADING_TIME_SECS * 1000));
        }
        if (!isPlayerShown() && playWhenReady) showPlayer();
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.w(getTag(), error.getMessage());
        releasePlayer();
        showThumbnail();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
    }

    @Override
    public void onSeekProcessed() {

    }

    interface OnCourseTopicChangeListener {
        void onNextTopic(int currentTopicIndex);

        void onPreviousTopic(int currentTopicIndex);
    }
}
