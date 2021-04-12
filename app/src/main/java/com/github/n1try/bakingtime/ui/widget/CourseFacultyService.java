package com.github.n1try.bakingtime.ui.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.github.n1try.bakingtime.R;
import com.github.n1try.bakingtime.utils.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class CourseFacultyService extends IntentService {
    public CourseFacultyService() {
        super("RecipeIngredientService");
    }

    public static void startActionUpdateWidgets(Context context) {
        Intent intent = new Intent(context, CourseFacultyService.class);
        intent.setAction(Constants.ACTION_UPDATE_FACULTY_WIDGETS);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent == null) return;
        final String action = intent.getAction();
        if (action.equals(Constants.ACTION_UPDATE_FACULTY_WIDGETS)) {
            Context context = getApplicationContext();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, FacultiesWidget.class));

            int id = sharedPreferences.getInt(Constants.PREF_KEY_LAST_COURSE_ID, -1);
            String name = sharedPreferences.getString(Constants.PREF_KEY_LAST_COURSE_NAME, context.getResources().getString(R.string.unknown_course));
            Optional<Set<String>> faculties = Optional.ofNullable(sharedPreferences.getStringSet(Constants.PREF_KEY_LAST_COURSE_FACULTIES, null));
            String formattedFaculties = TextUtils.join("\n\n", faculties.orElse(new HashSet<>(Arrays.asList(context.getResources().getString(R.string.no_course_selected)))));
            FacultiesWidget.updateAppWidgets(context, appWidgetManager, id, name, formattedFaculties, appWidgetIds);
        }
    }
}
