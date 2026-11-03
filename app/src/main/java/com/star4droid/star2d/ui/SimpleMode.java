package com.star4droid.star2d.ui;

// Added for Lo3bati: "simple mode" - a kid-friendly layer drawn on top of the existing editor (EditorShell).
// The full editor stays built underneath (hidden), so nothing was removed. "Pro mode" shows it again.

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.star4droid.star2d.evo.R;
import com.star4droid.star2d.ui.panels.ElementsPanel;

final class SimpleMode {
    private static final String FILE = "lo3bati_ui";
    private static final String KEY = "simple_mode";

    static boolean enabled(Context c) {
        return prefs(c).getBoolean(KEY, true);
    }

    static void set(Context c, boolean on) {
        prefs(c).edit().putBoolean(KEY, on).apply();
    }

    private static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    /** Small chip shown in pro mode to go back to the simple mode. */
    static TextView proChip(Context c, Runnable onClick) {
        TextView t = Ui.text(c, "الوضع البسيط", 13, 0xFFFFFFFF);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(Ui.dp(c, 14), Ui.dp(c, 8), Ui.dp(c, 14), Ui.dp(c, 8));
        t.setBackground(Ui.rounded(0xE643A047, Ui.dp(c, 16)));
        t.setOnClickListener(v -> onClick.run());
        return t;
    }

    // {name, icon type, index, isLight}
    private static final Object[][] THINGS = {
            {"مربع", "Box", 0, false},
            {"كرة", "Circle", 1, false},
            {"صندوق يجمع أشياء", "Group", 9, false},
    };
    private static final Object[][] WORDS = {
            {"كلمات", "Text", 2, false},
            {"شريط الطاقة", "Progress", 4, false},
    };
    private static final Object[][] CONTROL = {
            {"عصا التحكم", "Joystick", 3, false},
    };
    private static final Object[][] EFFECTS = {
            {"نار ودخان", "Particle", 6, false},
            {"ضوء نقطي", "Light", 0, true},
            {"ضوء الشمس", "Light", 1, true},
            {"كشاف", "Light", 2, true},
    };
    private static final Object[][] MORE = {
            {"عين اللعبة", "Camera", 7, false},
            {"شكل مخصص", "Custom", 5, false},
            {"مرحلة جاهزة", "Map", 8, false},
            {"واجهة جاهزة", "UIHolder", 10, false},
    };

    private final EditorShell shell;
    private final Activity a;
    private LinearLayout toolsBar;
    private LinearLayout hintBox;
    private TextView hint;
    private LinearLayout sheet;
    private boolean added = false;
    private boolean hintHidden = false;
    private boolean selected = false;

    SimpleMode(EditorShell shell) {
        this.shell = shell;
        this.a = shell.activity();
    }

    private int dp(float v) {
        return Ui.dp(a, v);
    }

    View view() {
        FrameLayout overlay = new FrameLayout(a);
        // Page = top part, empty gap (touches pass through to the game canvas), bottom part.
        LinearLayout page = Ui.column(a);

        // ---- top: bar + guide
        LinearLayout topCol = Ui.column(a);
        topCol.addView(topBar(), Ui.matchWrap());
        hintBox = Ui.row(a);
        hintBox.setGravity(Gravity.CENTER_VERTICAL);
        hintBox.setBackground(Ui.rounded(0xE6FFF3C4, dp(14)));
        hintBox.setPadding(dp(12), dp(8), dp(12), dp(8));
        hintBox.setClickable(true);
        hint = Ui.text(a, "", 15, 0xFF3E2F00);
        hint.setTypeface(Typeface.DEFAULT_BOLD);
        hintBox.addView(hint, Ui.weight(1));
        TextView skip = Ui.text(a, "تخطَّ", 13, 0xFF7A5C00);
        skip.setPadding(dp(10), dp(4), dp(4), dp(4));
        skip.setOnClickListener(v -> {
            hintHidden = true;
            hintBox.setVisibility(View.GONE);
        });
        hintBox.addView(skip);
        topCol.addView(hintBox, Ui.margins(Ui.matchWrap(), a, 10, 8, 10, 0));
        page.addView(topCol, Ui.matchWrap());
        page.addView(new View(a), new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // ---- bottom: object tools (when something is selected) + main buttons
        LinearLayout bottom = Ui.column(a);
        toolsBar = Ui.row(a);
        toolsBar.setBackgroundColor(0xF0181A1E);
        toolsBar.setPadding(dp(6), dp(6), dp(6), dp(6));
        toolsBar.setClickable(true);
        toolsBar.setVisibility(View.GONE);
        toolsBar.addView(toolButton(R.drawable.ic_s2_move, "حرّك", v -> shell.setTool("move")), Ui.margins(Ui.weight(1), a, 2, 0, 2, 0));
        toolsBar.addView(toolButton(R.drawable.ic_s2_scale, "حجم", v -> shell.setTool("scale")), Ui.margins(Ui.weight(1), a, 2, 0, 2, 0));
        toolsBar.addView(toolButton(R.drawable.ic_s2_rotate, "دوّر", v -> shell.setTool("rotate")), Ui.margins(Ui.weight(1), a, 2, 0, 2, 0));
        toolsBar.addView(toolButton(R.drawable.ic_s2_image, "صورة", v -> shell.togglePanel(shell.properties)), Ui.margins(Ui.weight(1), a, 2, 0, 2, 0));
        toolsBar.addView(toolButton(R.drawable.ic_s2_copy, "انسخ", v -> shell.duplicateSelected()), Ui.margins(Ui.weight(1), a, 2, 0, 2, 0));
        toolsBar.addView(toolButton(R.drawable.ic_s2_delete, "احذف", v -> shell.deleteSelected()), Ui.margins(Ui.weight(1), a, 2, 0, 2, 0));
        bottom.addView(toolsBar, Ui.matchWrap());

        LinearLayout main = Ui.row(a);
        main.setBackgroundColor(0xF0181A1E);
        main.setPadding(dp(8), dp(8), dp(8), dp(10));
        main.setGravity(Gravity.CENTER_VERTICAL);
        main.setClickable(true);
        main.addView(bigButton(R.drawable.ic_s2_add, "أضف شيئاً", 0xFFFF9800, v -> showSheet()), Ui.margins(Ui.weight(2), a, 4, 0, 4, 0));
        main.addView(bigButton(R.drawable.ic_s2_play, "العب الآن", 0xFF43A047, v -> shell.play()), Ui.margins(Ui.weight(2), a, 4, 0, 4, 0));
        TextView more = Ui.text(a, "المزيد", 14, Ui.TEXT);
        more.setGravity(Gravity.CENTER);
        more.setBackground(Ui.ripple(Ui.SURFACE_2, dp(16)));
        more.setPadding(dp(8), dp(16), dp(8), dp(16));
        more.setOnClickListener(v -> showMore());
        main.addView(more, Ui.margins(Ui.weight(1), a, 4, 0, 4, 0));
        bottom.addView(main, Ui.matchWrap());
        page.addView(bottom, Ui.matchWrap());
        overlay.addView(page, Ui.match());

        // ---- "add something" sheet
        sheet = buildSheet();
        sheet.setVisibility(View.GONE);
        int h = (int) (a.getResources().getDisplayMetrics().heightPixels * 0.6f);
        overlay.addView(sheet, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, h, Gravity.BOTTOM));

        setHint("اضغط «أضف شيئاً» لتضيف أول شيء إلى لعبتك.");
        return overlay;
    }

    private View topBar() {
        LinearLayout top = Ui.row(a);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setBackgroundColor(0xF0181A1E);
        top.setPadding(dp(8), dp(6), dp(8), dp(6));
        top.setClickable(true);
        top.addView(smallButton("رجوع", v -> shell.exitProject()), new LinearLayout.LayoutParams(-2, -2));
        top.addView(smallButton("حفظ", v -> shell.save()), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
        top.addView(smallButton("تراجع", v -> Bridge.gl(() -> Bridge.controls().actionUndo())), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
        top.addView(smallButton("إعادة", v -> Bridge.gl(() -> Bridge.controls().actionRedo())), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
        View space = new View(a);
        top.addView(space, Ui.weight(1));
        top.addView(smallButton("احترافي", v -> {
            set(a, false);
            shell.build();
        }), new LinearLayout.LayoutParams(-2, -2));
        return top;
    }

    private TextView smallButton(String label, View.OnClickListener l) {
        TextView t = Ui.text(a, label, 12.5f, Ui.TEXT);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(9), dp(7), dp(9), dp(7));
        t.setBackground(Ui.ripple(Ui.SURFACE_2, dp(12)));
        t.setOnClickListener(l);
        return t;
    }

    private LinearLayout bigButton(int icon, String label, int color, View.OnClickListener l) {
        LinearLayout b = Ui.row(a);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(10), dp(14), dp(10), dp(14));
        b.setBackground(Ui.rounded(color, dp(18)));
        b.addView(Ui.smallIcon(a, icon, 0xFFFFFFFF, 26));
        TextView t = Ui.text(a, label, 17, 0xFFFFFFFF);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(dp(8), 0, dp(4), 0);
        b.addView(t);
        b.setOnClickListener(l);
        return b;
    }

    private LinearLayout toolButton(int icon, String label, View.OnClickListener l) {
        LinearLayout b = Ui.column(a);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(4), dp(8), dp(4), dp(8));
        b.setBackground(Ui.ripple(Ui.SURFACE_2, dp(12)));
        b.addView(Ui.smallIcon(a, icon, Ui.TEXT, 24));
        TextView t = Ui.text(a, label, 11.5f, Ui.TEXT);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(4), 0, 0);
        b.addView(t);
        b.setOnClickListener(l);
        return b;
    }

    private void setHint(String s) {
        if (hint != null) hint.setText(s);
        if (hintBox != null && !hintHidden) hintBox.setVisibility(View.VISIBLE);
    }

    // =============================================================== add sheet

    private LinearLayout buildSheet() {
        LinearLayout s = Ui.column(a);
        s.setBackground(Ui.rounded(0xFA1C1F24, dp(20), Ui.BORDER, 1));
        s.setClickable(true);
        LinearLayout head = Ui.row(a);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(16), dp(12), dp(12), dp(8));
        TextView title = Ui.text(a, "أضف شيئاً", 20, Ui.TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        head.addView(title, Ui.weight(1));
        TextView close = smallButton("إغلاق", v -> hideSheet());
        head.addView(close);
        s.addView(head, Ui.matchWrap());

        LinearLayout col = Ui.column(a);
        col.setPadding(dp(10), 0, dp(10), dp(14));
        LinearLayout pics = Ui.row(a);
        pics.setGravity(Gravity.CENTER_VERTICAL);
        pics.setPadding(dp(14), dp(12), dp(14), dp(12));
        pics.setBackground(Ui.rounded(0xFF22402A, dp(14)));
        pics.addView(Ui.smallIcon(a, R.drawable.ic_s2_collections, 0xFF66BB6A, 30));
        TextView picsText = Ui.text(a, "صور ورسومات جاهزة للعبتك", 15, Ui.TEXT);
        picsText.setTypeface(Typeface.DEFAULT_BOLD);
        picsText.setPadding(dp(10), 0, 0, 0);
        pics.addView(picsText);
        pics.setOnClickListener(v -> {
            hideSheet();
            showCollections();
        });
        col.addView(pics, Ui.margins(Ui.matchWrap(), a, 4, 4, 4, 10));
        section(col, "أشياء", THINGS);
        section(col, "كلمات وأرقام", WORDS);
        section(col, "تحكم", CONTROL);
        section(col, "مؤثرات وإضاءة", EFFECTS);
        section(col, "المزيد", MORE);
        ScrollView sv = new ScrollView(a);
        sv.addView(col);
        s.addView(sv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));
        return s;
    }

    private void section(LinearLayout col, String title, Object[][] items) {
        col.addView(Ui.sectionLabel(a, title));
        GridLayout g = new GridLayout(a);
        g.setColumnCount(3);
        for (Object[] it : items) {
            final String name = (String) it[0];
            final String type = (String) it[1];
            final int pos = (Integer) it[2];
            final boolean light = (Boolean) it[3];
            LinearLayout card = Ui.column(a);
            card.setGravity(Gravity.CENTER);
            card.setPadding(dp(6), dp(12), dp(6), dp(10));
            card.setBackground(Ui.ripple(Ui.SURFACE_2, dp(14)));
            card.addView(Ui.smallIcon(a, ElementsPanel.typeIcon(type), ElementsPanel.typeColor(type), 34));
            TextView t = Ui.text(a, name, 13.5f, Ui.TEXT);
            t.setGravity(Gravity.CENTER);
            t.setPadding(0, dp(6), 0, 0);
            card.addView(t);
            card.setOnClickListener(v -> {
                hideSheet();
                Bridge.gl(() -> {
                    if (light) Bridge.controls().actionAddLight(pos);
                    else Bridge.controls().actionAddBody(pos);
                });
                added = true;
                setHint("ممتاز! أضفتَ «" + name + "» إلى لعبتك. اضغط «صورة» لتلبسه صورة، أو أضف شيئاً آخر.");
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(GridLayout.UNDEFINED), GridLayout.spec(GridLayout.UNDEFINED, 1f));
            lp.width = 0;
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            g.addView(card, lp);
        }
        col.addView(g, Ui.matchWrap());
    }

    /** Free picture packs shipped with the app (copied into the project's images folder). */
    private void showCollections() {
        Bridge.query(() -> Bridge.editor().getProject().getPath(),
                p -> CollectionsDialog.show(a, new java.io.File(p), () -> { }));
    }

    private void showSheet() {
        shell.panels.hide();
        sheet.setVisibility(View.VISIBLE);
    }

    private void hideSheet() {
        sheet.setVisibility(View.GONE);
    }

    // =============================================================== more (technical panels, still reachable)

    private void showMore() {
        final String[] names = {"إعدادات الشيء", "ماذا يحدث عندما...؟", "كل الأشياء في اللعبة", "إعدادات اللعبة"};
        new AlertDialog.Builder(a)
                .setTitle("المزيد")
                .setItems(names, (d, which) -> {
                    hideSheet();
                    if (which == 0) shell.togglePanel(shell.properties);
                    else if (which == 1) shell.togglePanel(shell.events);
                    else if (which == 2) shell.togglePanel(shell.elements);
                    else shell.togglePanel(shell.settings);
                })
                .show();
    }

    // =============================================================== selection feedback

    void onSelectionChanged() {
        Bridge.query(() -> Bridge.editor().getSelectedActor() != null, has -> {
            selected = Boolean.TRUE.equals(has);
            if (toolsBar != null) toolsBar.setVisibility(selected ? View.VISIBLE : View.GONE);
            if (selected) setHint("اختر ماذا تفعل بهذا الشيء: حرّك، حجم، دوّر، صورة، انسخ أو احذف.");
            else if (added) setHint("المس أي شيء في لعبتك لتغيّره، أو اضغط «أضف شيئاً».");
        });
    }
}
