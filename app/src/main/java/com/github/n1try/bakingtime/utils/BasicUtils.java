package com.github.n1try.bakingtime.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;

public class BasicUtils {
    public static SpannableStringBuilder styleTitle(String appTitle) {
        SpannableStringBuilder spannableBuilder = new SpannableStringBuilder(appTitle);
        spannableBuilder.setSpan(new TypefaceSpan("casual"), 0, appTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, appTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableBuilder.setSpan(new ForegroundColorSpan(Color.WHITE), 0, appTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableBuilder;
    }

    /* https://stackoverflow.com/a/15114434/3112139 */
    public static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    /* https://stackoverflow.com/a/4239019/3112139 */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /* Inspired by https://stackoverflow.com/a/27818661/3112139 */
    public static void justifyListViewHeightBasedOnChildren (GridView listView) {
        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getVerticalSpacing() * adapter.getCount());
        listView.setLayoutParams(par);
        listView.requestLayout();
    }
}
