package com.github.n1try.bakingtime.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.ui.CourseDetailActivity;
import com.github.n1try.bakingtime.utils.Constants;

public class FacultiesWidget extends AppWidgetProvider {
    public static void updateAppWidgets(Context context, AppWidgetManager appWidgetManager, int courseId, String courseName, String courseFaculties, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, courseId, courseName, courseFaculties, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int courseId, String courseName, String courseFaculties, int appWidgetId) {
        Intent intent = new Intent(context, CourseDetailActivity.class);
        intent.putExtra(Constants.KEY_COURSE_ID, courseId);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.faculties_widget);
        views.setTextViewText(R.id.widget_title_tv, courseName);
        views.setTextViewText(R.id.widget_faculties_tv, courseFaculties);
        if (courseId != -1) views.setOnClickPendingIntent(R.id.widget_faculties_tv, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        CourseFacultyService.startActionUpdateWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onDisabled(Context context) {
    }
}

