package com.arina.home;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ArinaHomeActivity extends Activity {

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView text(String value, float size, int color, int gravity) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(gravity);
        return view;
    }

    private GradientDrawable surface(int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(18));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.rgb(5, 11, 22));

        TextView brand = text("ARINA", 34, Color.rgb(234, 247, 255), Gravity.START | Gravity.CENTER_VERTICAL);
        root.addView(brand, new LinearLayout.LayoutParams(-1, dp(68)));

        TextView search = text("Search", 16, Color.rgb(169, 193, 216), Gravity.CENTER_VERTICAL);
        search.setPadding(dp(18), 0, dp(18), 0);
        search.setBackground(surface(Color.rgb(20, 35, 58), 20));
        LinearLayout.LayoutParams searchParams = new LinearLayout.LayoutParams(-1, dp(46));
        searchParams.setMargins(0, dp(4), 0, dp(24));
        root.addView(search, searchParams);

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);

        String[][] apps = {
            {"Phone", "Messages", "Camera", "Photos"},
            {"Conference", "Files", "Clock", "Settings"}
        };

        for (String[] rowNames : apps) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);

            for (String name : rowNames) {
                LinearLayout cell = new LinearLayout(this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);

                TextView icon = text(name.substring(0, 1), 22, Color.WHITE, Gravity.CENTER);
                icon.setBackground(surface(Color.rgb(74, 125, 255), 17));
                cell.addView(icon, new LinearLayout.LayoutParams(dp(58), dp(58)));

                TextView title = text(name, 12, Color.rgb(234, 247, 255), Gravity.CENTER);
                cell.addView(title, new LinearLayout.LayoutParams(-1, dp(30)));

                row.addView(cell, new LinearLayout.LayoutParams(0, dp(104), 1f));
            }

            grid.addView(row, new LinearLayout.LayoutParams(-1, dp(108)));
        }

        root.addView(grid, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(12), dp(10), dp(12), dp(10));
        dock.setBackground(surface(Color.argb(150, 36, 56, 85), 28));

        for (String name : new String[]{"Phone", "Messages", "Conference", "Camera"}) {
            TextView icon = text(name.substring(0, 1), 20, Color.WHITE, Gravity.CENTER);
            icon.setBackground(surface(Color.rgb(53, 198, 255), 16));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(56), 1f);
            lp.setMargins(dp(5), 0, dp(5), 0);
            dock.addView(icon, lp);
        }

        LinearLayout.LayoutParams dockParams = new LinearLayout.LayoutParams(-1, dp(78));
        dockParams.setMargins(0, dp(8), 0, 0);
        root.addView(dock, dockParams);

        setContentView(root);
    }
}
