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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import java.util.ArrayList;
import java.util.HashMap;
import android.widget.TextView;

import com.star4droid.star2d.evo.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.star4droid.star2d.Views.ProjectsHomeView;
import com.star4droid.star2d.remote.CardOps;
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
    };
    private static final Object[][] WORDS = {
            {"كلمات", "Text", 2, false},
            {"شريط الطاقة", "Progress", 4, false},
            {"عصا التحكم", "Joystick", 3, false},
            {"صندوق يجمع أشياء", "Group", 9, false},
            {"نار ودخان", "Particle", 6, false},
    };
    private static final Object[][] EFFECTS = {
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
    private TextView contextLabel;
    private LinearLayout hintBox;
    private TextView hint;
    private LinearLayout sheet;
    private LinearLayout stepsSheet;
    private final boolean[] done = new boolean[8];
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
        contextLabel = Ui.text(a, "", 12.5f, Ui.TEXT_3);
        contextLabel.setBackgroundColor(0xF0181A1E);
        contextLabel.setPadding(dp(14), dp(6), dp(14), 0);
        contextLabel.setClickable(true);
        bottom.addView(contextLabel, Ui.matchWrap());
        toolsBar = Ui.row(a);
        toolsBar.setBackgroundColor(0xF0181A1E);
        toolsBar.setPadding(dp(6), dp(6), dp(6), dp(6));
        toolsBar.setClickable(true);
        bottom.addView(toolsBar, Ui.matchWrap());
        updateContext();

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

        // ---- numbered steps popup (optional)
        stepsSheet = Ui.column(a);
        stepsSheet.setBackground(Ui.rounded(0xFA1C1F24, dp(20), Ui.BORDER, 1));
        stepsSheet.setClickable(true);
        stepsSheet.setVisibility(View.GONE);
        overlay.addView(stepsSheet, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));

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
        top.setPadding(dp(8), dp(6), dp(8), dp(6));
        top.addView(smallButton("رجوع", v -> shell.exitProject()), new LinearLayout.LayoutParams(-2, -2));
        top.addView(smallButton("حفظ", v -> shell.save()), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
        top.addView(smallButton("تراجع", v -> Bridge.gl(() -> Bridge.controls().actionUndo())), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
        top.addView(smallButton("إعادة", v -> Bridge.gl(() -> Bridge.controls().actionRedo())), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
        top.addView(new View(a), Ui.weight(1));
        TextView steps = smallButton("الخطوات", v -> showSteps());
        steps.setBackground(Ui.rounded(0xFF1E88E5, dp(12)));
        top.addView(steps, Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
        top.addView(smallButton("احترافي", v -> {
            set(a, false);
            shell.build();
        }), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
        // Scrollable so nothing is ever cut off on narrow screens
        HorizontalScrollView sc = new HorizontalScrollView(a);
        sc.setFillViewport(true);
        sc.setHorizontalScrollBarEnabled(false);
        sc.setBackgroundColor(0xF0181A1E);
        sc.setClickable(true);
        sc.addView(top, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return sc;
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
        TextView picsText = Ui.text(a, "صور شخصيات ورسومات جاهزة", 15, Ui.TEXT);
        picsText.setTypeface(Typeface.DEFAULT_BOLD);
        picsText.setPadding(dp(10), 0, 0, 0);
        pics.addView(picsText);
        pics.setOnClickListener(v -> {
            hideSheet();
            showCollections();
        });
        col.addView(pics, Ui.margins(Ui.matchWrap(), a, 4, 4, 4, 10));
        section(col, "شخصية أو عدو", THINGS);
        TextView shapeNote = Ui.text(a, "اختر شكلاً، ثم ألبسه صورة شخصية أو عدو من زر «المظهر».", 12.5f, Ui.TEXT_3);
        shapeNote.setPadding(dp(6), 0, dp(6), dp(8));
        col.addView(shapeNote, Ui.matchWrap());
        section(col, "عناصر اللعبة", WORDS);
        section(col, "إضاءة", EFFECTS);
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
                done[2] = true;
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
        hideSteps();
        sheet.setVisibility(View.VISIBLE);
    }

    private void hideSheet() {
        sheet.setVisibility(View.GONE);
    }

    // =============================================================== numbered steps (optional popup)

    private static final String[][] STEPS = {
            {"أنشئ لعبة", "ابدأ لعبة جديدة باسمك"},
            {"أضف أشياء", "اختر ما تريده من البطاقات"},
            {"حرّك ورتّب", "ضع كل شيء في مكانه"},
            {"ضع صوراً", "أحضر صوراً جاهزة والبس بها أشياءك"},
            {"اجعلها تعمل", "ماذا يحدث عندما تلمس شيئاً؟"},
            {"جرّب لعبتك", "العب الآن وانظر النتيجة"},
            {"احفظ لعبتك", "حتى لا يضيع شيء"},
    };

    private void showSteps() {
        shell.panels.hide();
        hideSheet();
        stepsSheet.removeAllViews();
        LinearLayout head = Ui.row(a);
        head.setGravity(Gravity.CENTER_VERTICAL);
        head.setPadding(dp(16), dp(12), dp(12), dp(6));
        TextView title = Ui.text(a, "خطوات صناعة لعبتك", 19, Ui.TEXT);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        head.addView(title, Ui.weight(1));
        head.addView(smallButton("إغلاق", v -> hideSteps()));
        stepsSheet.addView(head, Ui.matchWrap());
        TextView note = Ui.text(a, "الخطوات اختيارية، اضغط أي خطوة لتبدأها.", 13, Ui.TEXT_3);
        note.setPadding(dp(16), 0, dp(16), dp(6));
        stepsSheet.addView(note, Ui.matchWrap());
        for (int i = 0; i < STEPS.length; i++) {
            final int n = i + 1;
            LinearLayout row = Ui.row(a);
            row.setPadding(dp(14), dp(10), dp(14), dp(10));
            row.setBackground(Ui.ripple(0x00000000, dp(12)));
            TextView num = Ui.text(a, done[n] ? "✓" : String.valueOf(n), 17, 0xFFFFFFFF);
            num.setGravity(Gravity.CENTER);
            num.setTypeface(Typeface.DEFAULT_BOLD);
            num.setBackground(Ui.rounded(done[n] ? 0xFF43A047 : 0xFF1E88E5, dp(18)));
            row.addView(num, new LinearLayout.LayoutParams(dp(36), dp(36)));
            LinearLayout texts = Ui.column(a);
            texts.setPadding(dp(12), 0, dp(8), 0);
            TextView t1 = Ui.text(a, STEPS[i][0], 16, Ui.TEXT);
            t1.setTypeface(Typeface.DEFAULT_BOLD);
            texts.addView(t1);
            texts.addView(Ui.text(a, STEPS[i][1], 12.5f, Ui.TEXT_3));
            row.addView(texts, Ui.weight(1));
            row.setOnClickListener(v -> runStep(n));
            stepsSheet.addView(row, Ui.margins(Ui.matchWrap(), a, 6, 0, 6, 0));
        }
        View pad = new View(a);
        stepsSheet.addView(pad, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(12)));
        stepsSheet.setVisibility(View.VISIBLE);
    }

    private void hideSteps() {
        if (stepsSheet != null) stepsSheet.setVisibility(View.GONE);
    }

    private void runStep(int n) {
        hideSteps();
        done[n] = true;
        switch (n) {
            case 1:
                Dialogs.confirm(a, "لعبة جديدة", "سنحفظ لعبتك الحالية ونغلقها ثم نبدأ لعبة جديدة. هل تريد المتابعة؟", "متابعة", () -> {
                    shell.save();
                    shell.panels.hide();
                    ProjectsHomeView.openNewOnShow = true;
                    Bridge.gl(() -> Bridge.app().closeProject());
                });
                break;
            case 2:
                showSheet();
                break;
            case 3:
                if (selected) {
                    shell.setTool("move");
                    setHint("اسحب الشيء بإصبعك إلى المكان الذي تريده.");
                } else {
                    setHint("المس شيئاً في لعبتك أولاً، ثم اضغط «حرّك» واسحبه.");
                }
                break;
            case 4:
                showCollections();
                setHint("بعد إضافة الصور: المس شيئاً ثم اضغط «صورة» واختر الصورة.");
                break;
            case 5:
                shell.togglePanel(shell.events);
                break;
            case 6:
                shell.play();
                break;
            default:
                shell.save();
                setHint("تم حفظ لعبتك.");
                break;
        }
    }

    // =============================================================== more (technical panels, still reachable)

    private void showMore() {
        final String[] names = {"خطوات صناعة اللعبة", "إعدادات الشيء", "ماذا يحدث عندما...؟", "كل الأشياء في اللعبة", "إعدادات اللعبة"};
        new AlertDialog.Builder(a)
                .setTitle("المزيد")
                .setItems(names, (d, which) -> {
                    hideSheet();
                    if (which == 0) showSteps();
                    else if (which == 1) shell.togglePanel(shell.properties);
                    else if (which == 2) shell.togglePanel(shell.events);
                    else if (which == 3) shell.togglePanel(shell.elements);
                    else shell.togglePanel(shell.settings);
                })
                .show();
    }

    // =============================================================== selection feedback

    void onSelectionChanged() {
        Bridge.query(() -> Bridge.editor().getSelectedActor() != null, has -> {
            selected = Boolean.TRUE.equals(has);
            updateContext();
            if (selected) setHint("هذه أدوات الشيء المحدد فقط: حرّك، المظهر، السلوك، انسخ أو احذف.");
            else if (added) setHint("المس أي شيء لتغيّره، أو المس الخلفية لتغيّر اللعبة نفسها.");
        });
    }

    // =============================================================== context tools

    /** Shows only the tools that belong to what the user is touching: the selected thing, or the game background. */
    private void updateContext() {
        if (toolsBar == null) return;
        toolsBar.removeAllViews();
        if (selected) {
            contextLabel.setText("أدوات الشيء المحدد");
            addCtx(R.drawable.ic_s2_move, "حرّك", v -> {
                shell.setTool("move");
                setHint("اسحب الشيء بإصبعك إلى المكان الذي تريده.");
            });
            addCtx(R.drawable.ic_s2_palette, "المظهر", v -> showAppearance());
            addCtx(R.drawable.ic_s2_wand, "السلوك", v -> showBehavior());
            addCtx(R.drawable.ic_s2_copy, "انسخ", v -> shell.duplicateSelected());
            addCtx(R.drawable.ic_s2_delete, "احذف", v -> shell.deleteSelected());
        } else {
            contextLabel.setText("أدوات اللعبة");
            addCtx(R.drawable.ic_s2_image, "الخلفية", v -> showBackground());
            addCtx(R.drawable.ic_s2_sound, "الأصوات", v -> showSounds());
        }
    }

    private void addCtx(int icon, String label, View.OnClickListener l) {
        toolsBar.addView(toolButton(icon, label, l), Ui.margins(Ui.weight(1), a, 2, 0, 2, 0));
    }

    private void menu(String title, String[] names, Runnable[] actions) {
        new AlertDialog.Builder(a).setTitle(title)
                .setItems(names, (d, which) -> actions[which].run())
                .show();
    }

    private void showAppearance() {
        menu("مظهر الشيء", new String[]{"غيّر الصورة أو اللون", "كبّر أو صغّر", "دوّر", "صور شخصيات جاهزة"}, new Runnable[]{
                () -> {
                    shell.togglePanel(shell.properties);
                    setHint("في الإعدادات: اختر الصورة أو اللون.");
                },
                () -> {
                    shell.setTool("scale");
                    setHint("اسحب لتكبّر الشيء أو تصغّره.");
                },
                () -> {
                    shell.setTool("rotate");
                    setHint("اسحب لتدوير الشيء.");
                },
                this::showCollections
        });
    }

    private void showBehavior() {
        menu("سلوك الشيء", new String[]{"بطاقات جاهزة: عندما يلمس اللاعب هذا الشيء...", "شغّل حركة المشي", "قدرات جاهزة (يتحرك بالعصا، يقفز، يطلق...)", "ماذا يحدث عندما...؟"}, new Runnable[]{
                this::openCards,
                this::openWalk,
                () -> {
                    shell.togglePanel(shell.properties);
                    setHint("انزل في الإعدادات إلى قسم السلوكيات وأضف قدرة جاهزة.");
                },
                () -> {
                    shell.togglePanel(shell.events);
                    setHint("اختر متى يحدث الفعل (عند اللمس أو الاصطدام...) ثم ما يحدث.");
                }
        });
    }

    private void showBackground() {
        menu("الخلفية والبيئة", new String[]{"لون الخلفية", "الإضاءة", "اتجاه الشاشة (طولي أو عرضي)", "صور ورسومات جاهزة"}, new Runnable[]{
                shell::sceneColor,
                () -> LightSettings.show(a),
                () -> Dialogs.confirm(a, "اتجاه الشاشة", "ستُحفظ المشاهد وتُعاد فتحها.", null,
                        () -> Bridge.gl(() -> Bridge.controls().actionToggleOrientation())),
                this::showCollections
        });
    }

    private void showSounds() {
        menu("الأصوات والمؤثرات", new String[]{"أضف صوتاً من هاتفك", "اجعل شيئاً يشغّل صوتاً"}, new Runnable[]{
                () -> Bridge.query(() -> Bridge.editor().getProject().getPath(), p -> {
                    java.io.File d = new java.io.File(p, "sounds");
                    d.mkdirs();
                    shell.host().importFiles(d.getAbsolutePath(), () -> setHint("أُضيف الصوت. الآن اختر شيئاً ثم السلوك ثم «شغّل صوتاً»."));
                }),
                () -> {
                    shell.togglePanel(shell.events);
                    setHint("اختر الحدث ثم أضف الأمر «شغّل صوتاً» من القائمة.");
                }
        });
    }
    // =============================================================== action cards (ready-made, can be combined and re-ordered)
    // They write normal blocks into the scene event "onCollisionStart" (and "OnCreate"), so the pro editor and Java still see and edit them.

    private static final String[][] CARDS = {
            {"point", "أضف نقطة"},
            {"hide", "اختفِ بهذا الشيء"},
            {"sound", "شغّل صوتاً"},
            {"life", "اخسر حياة"},
            {"respawn", "أعد اللاعب إلى نقطة البداية"},
    };

    private static String cardLabel(String id) {
        for (String[] c : CARDS) if (c[0].equals(id)) return c[1];
        return id;
    }

    private static String lit(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void error(Throwable t) {
        String m = t.getMessage() == null ? String.valueOf(t) : t.getMessage();
        new AlertDialog.Builder(a).setTitle("لم يكتمل الأمر").setMessage(m).setPositiveButton("حسناً", null).show();
    }

    /** Lists the file names of a project folder (for example sounds or anims) and gives them to {@code done} on the UI thread. */
    private void listProjectFiles(String folder, java.util.function.Consumer<String[]> done) {
        Bridge.query(() -> Bridge.editor().getProject().getPath(), path -> {
            String[] names = new java.io.File(path, folder).list();
            if (names == null) names = new String[0];
            java.util.Arrays.sort(names);
            done.accept(names);
        });
    }

    private void openCards() {
        Bridge.query(() -> {
            com.badlogic.gdx.scenes.scene2d.Actor s = Bridge.editor().getSelectedActor();
            return s == null ? "" : s.getName();
        }, name -> {
            if (name == null || name.isEmpty()) {
                setHint("المس الشيء أولاً (مثل العملة أو العدو) ثم اختر البطاقات.");
                return;
            }
            shell.save();
            new Thread(() -> {
                try {
                    final ArrayList<String> names = CardOps.elementNames();
                    a.runOnUiThread(() -> showComposer(name, names));
                } catch (Throwable t) {
                    a.runOnUiThread(() -> error(t));
                }
            }).start();
        });
    }

    private void showComposer(final String target, ArrayList<String> names) {
        final ArrayList<String> players = new ArrayList<>(names);
        players.remove(target);
        final String[] player = {players.contains("player") ? "player" : (players.isEmpty() ? "" : players.get(0))};
        final ArrayList<String> ids = new ArrayList<>();
        final HashMap<String, String> param = new HashMap<>();
        final Runnable[] render = new Runnable[1];

        final LinearLayout box = Ui.column(a);
        box.setPadding(dp(14), dp(8), dp(14), dp(4));
        final TextView who = smallButton("", null);
        box.addView(who, Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 0, 0, 0, 8));
        TextView presetLabel = Ui.text(a, "ابدأ ببطاقات جاهزة:", 13, Ui.TEXT_3);
        box.addView(presetLabel);
        LinearLayout presets = Ui.row(a);
        presets.addView(smallButton("عملة", v -> {
            ids.clear();
            ids.add("point");
            ids.add("hide");
            ids.add("sound");
            render[0].run();
        }), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 0, 4, 6, 8));
        presets.addView(smallButton("عدو", v -> {
            ids.clear();
            ids.add("life");
            ids.add("respawn");
            render[0].run();
        }), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 0, 4, 6, 8));
        presets.addView(smallButton("فارغ", v -> {
            ids.clear();
            render[0].run();
        }), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 0, 4, 6, 8));
        box.addView(presets);
        final LinearLayout list = Ui.column(a);
        box.addView(list, Ui.matchWrap());
        box.addView(smallButton("+ أضف بطاقة", v -> {
            final String[] labels = new String[CARDS.length];
            for (int i = 0; i < CARDS.length; i++) labels[i] = CARDS[i][1];
            new AlertDialog.Builder(a).setTitle("اختر بطاقة").setItems(labels, (d, which) -> {
                ids.add(CARDS[which][0]);
                render[0].run();
            }).show();
        }), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 0, 8, 0, 4));

        who.setOnClickListener(v -> {
            if (players.isEmpty()) {
                setHint("لا يوجد شيء آخر في اللعبة ليكون اللاعب. أضف اللاعب أولاً.");
                return;
            }
            final String[] pn = players.toArray(new String[0]);
            new AlertDialog.Builder(a).setTitle("من هو اللاعب؟").setItems(pn, (d, which) -> {
                player[0] = pn[which];
                render[0].run();
            }).show();
        });

        render[0] = () -> {
            who.setText("اللاعب: " + (player[0].isEmpty() ? "اختر" : player[0]));
            list.removeAllViews();
            if (ids.isEmpty()) {
                list.addView(Ui.text(a, "لا توجد بطاقات بعد. اضغط «+ أضف بطاقة» أو اختر بطاقات جاهزة.", 13, Ui.TEXT_3));
            }
            for (int i = 0; i < ids.size(); i++) {
                final int idx = i;
                final String id = ids.get(i);
                LinearLayout row = Ui.row(a);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(10), dp(8), dp(6), dp(8));
                row.setBackground(Ui.rounded(0xFF2A2E35, dp(12)));
                String label = (idx + 1) + ". " + cardLabel(id);
                if (id.equals("sound")) label += param.containsKey("sound") ? " (" + param.get("sound") + ")" : " (اضغط لاختيار الصوت)";
                TextView t = Ui.text(a, label, 14.5f, Ui.TEXT);
                t.setOnClickListener(v -> {
                    if (!id.equals("sound")) return;
                    listProjectFiles("sounds", files -> {
                        if (files.length == 0) {
                            setHint("لا توجد أصوات في لعبتك. أضف صوتاً من زر «الأصوات» أولاً.");
                            return;
                        }
                        new AlertDialog.Builder(a).setTitle("اختر الصوت").setItems(files, (d, which) -> {
                            param.put("sound", files[which]);
                            render[0].run();
                        }).show();
                    });
                });
                row.addView(t, Ui.weight(1));
                row.addView(smallButton("▲", v -> {
                    if (idx > 0) {
                        ids.add(idx - 1, ids.remove(idx));
                        render[0].run();
                    }
                }));
                row.addView(smallButton("▼", v -> {
                    if (idx < ids.size() - 1) {
                        ids.add(idx + 1, ids.remove(idx));
                        render[0].run();
                    }
                }), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
                row.addView(smallButton("✕", v -> {
                    ids.remove(idx);
                    render[0].run();
                }), Ui.margins(new LinearLayout.LayoutParams(-2, -2), a, 4, 0, 0, 0));
                list.addView(row, Ui.margins(Ui.matchWrap(), a, 0, 0, 0, 6));
            }
        };
        render[0].run();

        ScrollView sv = new ScrollView(a);
        sv.addView(box);
        new AlertDialog.Builder(a)
                .setTitle("عندما يلمس اللاعب «" + target + "»")
                .setView(sv)
                .setPositiveButton("طبّق", (d, w) -> applyCards(target, player[0], ids, param))
                .setNegativeButton("إلغاء", null)
                .show();
    }

    private void applyCards(String target, String player, ArrayList<String> ids, HashMap<String, String> param) {
        if (player.isEmpty() || ids.isEmpty()) {
            setHint("اختر اللاعب وأضف بطاقة واحدة على الأقل.");
            return;
        }
        if (ids.contains("sound") && !param.containsKey("sound")) {
            setHint("اختر الصوت لبطاقة «شغّل صوتاً» أولاً (اضغط عليها).");
            return;
        }
        // Names like coin1, coin2 (copies of one thing) all match the same cards.
        String base = target.replaceAll("\\d+$", "");
        boolean many = !base.isEmpty() && !base.equals(target);
        String tm = many ? "startsWith(\"" + lit(base) + "\")" : "equals(\"" + lit(target) + "\")";
        String pm = "equals(\"" + lit(player) + "\")";
        String cond = "(body1.getName()." + pm + " && body2.getName()." + tm + ") || (body2.getName()." + pm + " && body1.getName()." + tm + ")";
        String P = lit(player);

        JsonArray inner = new JsonArray();
        ArrayList<String[]> vars = new ArrayList<>();
        boolean needStart = false;
        if (ids.contains("hide")) inner.add(code("PlayerItem hit = body1.getName()." + tm + " ? body1 : body2;"));
        for (String id : ids) {
            switch (id) {
                case "point":
                    inner.add(block("changeVariable", "variable", "score", "by", "1"));
                    vars.add(new String[]{"score", "int", "0"});
                    break;
                case "life":
                    inner.add(block("changeVariable", "variable", "lives", "by", "-1"));
                    vars.add(new String[]{"lives", "int", "3"});
                    break;
                case "sound":
                    inner.add(block("playSound", "Sound", param.get("sound")));
                    break;
                case "hide":
                    inner.add(block("hide", "body", "hit"));
                    inner.add(code("{ final PlayerItem __d = hit; Timer.schedule(new Timer.Task(){ public void run(){ if(__d.getActor().getStage() != null) __d.destroy(); } }, 0.05f); }"));
                    break;
                case "respawn":
                    inner.add(code("{ Timer.schedule(new Timer.Task(){ public void run(){ if(" + P + ".getBody() != null){ " + P + ".getBody().setTransform(startX, startY, " + P + ".getBody().getAngle()); " + P + ".getBody().setLinearVelocity(0f, 0f); } } }, 0.05f); }"));
                    vars.add(new String[]{"startX", "float", "0f"});
                    vars.add(new String[]{"startY", "float", "0f"});
                    needStart = true;
                    break;
                default:
                    break;
            }
        }
        JsonObject iff = new JsonObject();
        iff.addProperty("block", "ifThen");
        JsonObject f = new JsonObject();
        f.addProperty("boolean", cond);
        iff.add("fields", f);
        iff.add("do", inner);
        final JsonArray blocks = new JsonArray();
        blocks.add(iff);
        final String[][] varArr = vars.toArray(new String[0][]);
        final boolean start = needStart;
        final String startCode = "if (" + P + ".getBody() != null) { startX = " + P + ".getBodyX(); startY = " + P + ".getBodyY(); }";
        setHint("جارٍ إضافة البطاقات...");
        new Thread(() -> {
            try {
                CardOps.addToSceneEvent(a, "onCollisionStart", blocks.toString(), varArr);
                if (start) {
                    JsonArray sb = new JsonArray();
                    sb.add(code(startCode));
                    CardOps.addToSceneEvent(a, "OnCreate", sb.toString(), null);
                }
                a.runOnUiThread(() -> setHint("تمت إضافة البطاقات. جرّب لعبتك بزر «العب الآن»."));
            } catch (Throwable t) {
                a.runOnUiThread(() -> error(t));
            }
        }).start();
    }

    private static JsonObject block(String title, String... kv) {
        JsonObject o = new JsonObject();
        o.addProperty("block", title);
        JsonObject f = new JsonObject();
        for (int i = 0; i + 1 < kv.length; i += 2) f.addProperty(kv[i], kv[i + 1]);
        o.add("fields", f);
        return o;
    }

    private static JsonObject code(String java) {
        return block("codeLine", "code", java);
    }

    /** "Play the walking animation": starts an animation of the project (anims folder) on the selected thing when the game starts. */
    private void openWalk() {
        Bridge.query(() -> {
            com.badlogic.gdx.scenes.scene2d.Actor s = Bridge.editor().getSelectedActor();
            return s == null ? "" : s.getName();
        }, name -> {
            if (name == null || name.isEmpty()) {
                setHint("المس الشخصية أولاً ثم اختر «شغّل حركة المشي».");
                return;
            }
            listProjectFiles("anims", files -> {
                if (files.length == 0) {
                    setHint("لا توجد حركات في لعبتك. أنشئ حركة المشي أولاً (مجلد anims في الملفات) ثم ارجع.");
                    return;
                }
                new AlertDialog.Builder(a).setTitle("اختر حركة المشي").setItems(files, (d, which) -> {
                    final String anim = files[which];
                    shell.save();
                    new Thread(() -> {
                        try {
                            JsonArray b = new JsonArray();
                            b.add(block("setAnimation", "body", name, "animation", anim));
                            CardOps.addToSceneEvent(a, "OnCreate", b.toString(), null);
                            a.runOnUiThread(() -> setHint("ستبدأ حركة «" + anim + "» عند بدء اللعبة. جرّبها بزر «العب الآن»."));
                        } catch (Throwable t) {
                            a.runOnUiThread(() -> error(t));
                        }
                    }).start();
                }).show();
            });
        });
    }
}
