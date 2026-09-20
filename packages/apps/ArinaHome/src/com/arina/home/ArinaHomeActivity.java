package com.arina.home;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ArinaHomeActivity extends Activity {

    private static final int ICE = Color.rgb(234, 247, 255);
    private static final int STEEL = Color.rgb(169, 193, 216);
    private static final int BLUE = Color.rgb(74, 125, 255);
    private static final int CYAN = Color.rgb(53, 198, 255);
    private static final int GLASS = Color.argb(150, 20, 35, 58);

    private final List<AppEntry> apps = new ArrayList<>();
    private GridLayout appGrid;

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView label(String value, float sizeSp, int color, int gravity) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sizeSp);
        view.setTextColor(color);
        view.setGravity(gravity);
        view.setFontFeatureSettings("kern");
        view.setIncludeFontPadding(false);
        return view;
    }

    private GradientDrawable rounded(int fill, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), Color.argb(42, 216, 240, 255));
        return drawable;
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        Window window = getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        window.setNavigationBarColor(Color.TRANSPARENT);
        window.setDecorFitsSystemWindows(false);

        FrameLayout root = new FrameLayout(this);
        root.addView(new ArinaWallpaperView(this),
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(16), dp(18), dp(14));
        root.addView(content, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout smartHeader = buildSmartHeader();
        LinearLayout.LayoutParams headerParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(88));
        headerParams.setMargins(0, dp(4), 0, dp(14));
        content.addView(smartHeader, headerParams);

        EditText search = buildSearch();
        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        searchParams.setMargins(0, 0, 0, dp(14));
        content.addView(search, searchParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

        appGrid = new GridLayout(this);
        appGrid.setColumnCount(4);
        appGrid.setUseDefaultMargins(false);
        appGrid.setPadding(0, dp(2), 0, dp(8));
        scroll.addView(appGrid, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(scroll,
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView pageIndicator = label("●  •  •", 10, Color.argb(190, 234, 247, 255), Gravity.CENTER);
        LinearLayout.LayoutParams indicatorParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(22));
        indicatorParams.setMargins(0, dp(4), 0, dp(4));
        content.addView(pageIndicator, indicatorParams);

        LinearLayout dock = buildDock();
        LinearLayout.LayoutParams dockParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(82));
        dockParams.setMargins(0, 0, 0, dp(4));
        content.addView(dock, dockParams);

        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int top = insets.getInsets(WindowInsets.Type.statusBars()).top;
            int bottom = insets.getInsets(WindowInsets.Type.navigationBars()).bottom;
            content.setPadding(dp(18), top + dp(8), dp(18), Math.max(dp(10), bottom + dp(4)));
            return insets;
        });

        setContentView(root);

        loadApps();
        renderApps("");

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                renderApps(s == null ? "" : s.toString());
            }
            @Override public void afterTextChanged(Editable editable) {}
        });
    }

    private LinearLayout buildSmartHeader() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(18), dp(12), dp(18), dp(12));
        card.setBackground(rounded(Color.argb(118, 11, 26, 48), 26));
        card.setElevation(dp(8));

        LinearLayout textBlock = new LinearLayout(this);
        textBlock.setOrientation(LinearLayout.VERTICAL);
        textBlock.setGravity(Gravity.CENTER_VERTICAL);

        TextView brand = label("ARINA", 22, ICE, Gravity.START);
        brand.setLetterSpacing(0.08f);
        TextView sub = label("Home · calm, fast, private", 12, STEEL, Gravity.START);
        textBlock.addView(brand, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(32)));
        textBlock.addView(sub, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(22)));

        card.addView(textBlock, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        TextView settings = label("◎", 28, ICE, Gravity.CENTER);
        settings.setBackground(rounded(Color.argb(135, 74, 125, 255), 18));
        settings.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
            } catch (Exception ignored) {
                Toast.makeText(this, "Settings unavailable", Toast.LENGTH_SHORT).show();
            }
        });
        card.addView(settings, new LinearLayout.LayoutParams(dp(52), dp(52)));

        return card;
    }

    private EditText buildSearch() {
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("Search");
        search.setHintTextColor(Color.argb(185, 169, 193, 216));
        search.setTextColor(ICE);
        search.setTextSize(16);
        search.setPadding(dp(18), 0, dp(18), 0);
        search.setBackground(rounded(GLASS, 22));
        search.setSelectAllOnFocus(false);
        return search;
    }

    private void loadApps() {
        apps.clear();
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolved = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
        for (ResolveInfo info : resolved) {
            if (info.activityInfo == null) continue;
            if (getPackageName().equals(info.activityInfo.packageName)) continue;

            String name = String.valueOf(info.loadLabel(pm));
            Drawable icon = info.loadIcon(pm);
            Intent launch = new Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setClassName(info.activityInfo.packageName, info.activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            apps.add(new AppEntry(name, icon, launch));
        }

        Collator collator = Collator.getInstance(Locale.getDefault());
        Collections.sort(apps, (a, b) -> collator.compare(a.name, b.name));
    }

    private void renderApps(String query) {
        if (appGrid == null) return;
        appGrid.removeAllViews();

        String q = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());
        for (AppEntry app : apps) {
            if (!q.isEmpty() && !app.name.toLowerCase(Locale.getDefault()).contains(q)) {
                continue;
            }
            appGrid.addView(buildAppCell(app), gridParams());
        }

        if (appGrid.getChildCount() == 0) {
            TextView empty = label("No matching apps", 14, STEEL, Gravity.CENTER);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.columnSpec = GridLayout.spec(0, 4);
            params.width = GridLayout.LayoutParams.MATCH_PARENT;
            params.height = dp(110);
            appGrid.addView(empty, params);
        }
    }

    private GridLayout.LayoutParams gridParams() {
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.width = 0;
        params.height = dp(106);
        params.setMargins(dp(2), dp(2), dp(2), dp(2));
        return params;
    }

    private View buildAppCell(AppEntry app) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        cell.setPadding(dp(3), dp(4), dp(3), 0);

        ImageView icon = new ImageView(this);
        icon.setImageDrawable(app.icon);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        icon.setBackground(rounded(Color.argb(34, 234, 247, 255), 18));
        icon.setPadding(dp(5), dp(5), dp(5), dp(5));
        icon.setElevation(dp(6));
        cell.addView(icon, new LinearLayout.LayoutParams(dp(62), dp(62)));

        TextView title = label(app.name, 11.5f, ICE, Gravity.CENTER);
        title.setMaxLines(2);
        title.setShadowLayer(4f, 0f, 1f, Color.argb(180, 0, 0, 0));
        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(36));
        titleParams.setMargins(0, dp(5), 0, 0);
        cell.addView(title, titleParams);

        cell.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            animatePress(v);
            try {
                startActivity(app.intent);
            } catch (Exception error) {
                Toast.makeText(this, "Unable to open " + app.name, Toast.LENGTH_SHORT).show();
            }
        });

        cell.setOnLongClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            Toast.makeText(this, app.name + " · app actions foundation", Toast.LENGTH_SHORT).show();
            return true;
        });

        return cell;
    }

    private LinearLayout buildDock() {
        LinearLayout dock = new LinearLayout(this);
        dock.setGravity(Gravity.CENTER);
        dock.setPadding(dp(12), dp(11), dp(12), dp(11));
        dock.setBackground(rounded(Color.argb(156, 23, 48, 82), 30));
        dock.setElevation(dp(10));

        List<AppEntry> pinned = chooseDockApps();
        for (AppEntry app : pinned) {
            ImageView icon = new ImageView(this);
            icon.setImageDrawable(app.icon);
            icon.setPadding(dp(4), dp(4), dp(4), dp(4));
            icon.setBackground(rounded(Color.argb(35, 234, 247, 255), 18));
            icon.setOnClickListener(v -> {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                animatePress(v);
                try {
                    startActivity(app.intent);
                } catch (Exception ignored) {}
            });
            LinearLayout.LayoutParams iconParams =
                    new LinearLayout.LayoutParams(0, dp(58), 1f);
            iconParams.setMargins(dp(6), 0, dp(6), 0);
            dock.addView(icon, iconParams);
        }

        return dock;
    }

    private List<AppEntry> chooseDockApps() {
        List<AppEntry> result = new ArrayList<>();
        String[] preferred = {"phone", "message", "conference", "camera"};

        for (String keyword : preferred) {
            for (AppEntry app : apps) {
                if (app.name.toLowerCase(Locale.US).contains(keyword) && !result.contains(app)) {
                    result.add(app);
                    break;
                }
            }
        }

        for (AppEntry app : apps) {
            if (result.size() >= 4) break;
            if (!result.contains(app)) result.add(app);
        }
        return result;
    }

    private void animatePress(View view) {
        view.animate()
                .scaleX(0.975f)
                .scaleY(0.975f)
                .setDuration(80)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(150)
                        .start())
                .start();
    }

    private static final class AppEntry {
        final String name;
        final Drawable icon;
        final Intent intent;

        AppEntry(String name, Drawable icon, Intent intent) {
            this.name = name;
            this.icon = icon;
            this.intent = intent;
        }
    }
}
