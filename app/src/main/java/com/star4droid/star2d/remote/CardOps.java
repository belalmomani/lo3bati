package com.star4droid.star2d.remote;

// Added for Lo3bati: lets the simple-mode "action cards" write scene events and variables by reusing the
// same code the AI/remote tools use (BlockBuilder, BlockModel, EditTools.variables). Call from a background thread.

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.google.gson.JsonParser;
import com.star4droid.star2d.Helpers.PropertySet;
import com.star4droid.star2d.ui.Bridge;
import com.star4droid.star2d.Helpers.editor.Project;
import com.star4droid.star2d.editor.LibgdxEditor;
import com.star4droid.star2d.ui.blocks.BlockCatalog;
import com.star4droid.star2d.ui.blocks.BlockModel;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CardOps {
    private CardOps() {}

    /** Names of the elements of the open scene (read from the saved scene file). */
    public static ArrayList<String> elementNames() throws Exception {
        return Gl.run(() -> {
            LibgdxEditor ed = Proj.editor();
            Project p = ed.getProject();
            ArrayList<String> out = new ArrayList<>();
            for (LinkedHashMap<String, String> m : CoreTools.fileElements(p, ed.getScene())) {
                String n = m.get("name");
                if (n != null && !n.isEmpty()) out.add(n);
            }
            return out;
        });
    }

    /**
     * Appends the blocks (JSON array like the AI tools use) to the END of the scene event, keeping what is already
     * there, and creates the variables that do not exist yet ({name, type, value}). Returns the scene name.
     */
    public static String addToSceneEvent(Context c, String event, String blocksJson, String[][] vars) throws Exception {
        final JsonArray blocks = JsonParser.parseString(blocksJson).getAsJsonArray();
        if (vars != null && vars.length > 0) {
            JsonArray ops = new JsonArray();
            for (String[] v : vars) {
                JsonObject o = new JsonObject();
                o.addProperty("op", "add");
                o.addProperty("name", v[0]);
                o.addProperty("type", v[1]);
                o.addProperty("value", v[2]);
                ops.add(o);
            }
            JsonObject a = new JsonObject();
            a.add("ops", ops);
            EditTools.variables(a); // a variable that already exists is simply kept
        }
        return Gl.run(() -> {
            BlockCatalog.load(c);
            LibgdxEditor ed = Proj.editor();
            Project p = ed.getProject();
            String scene = ed.getScene();
            File vf = new File(p.getEventPath(scene, "", event) + ".visual");
            BlockModel add = BlockBuilder.build(blocks);
            BlockModel m = add;
            if (vf.exists() && vf.length() > 2) {
                BlockModel old = BlockModel.load(vf);
                if (!old.heads.isEmpty() && !add.heads.isEmpty()) {
                    old.heads.get(0).tail().next = add.heads.get(0).next;
                    m = old;
                }
            }
            if (vf.getParentFile() != null) vf.getParentFile().mkdirs();
            m.save(vf);
            return scene;
        });
    }
    /** {name, TYPE} of every element of the open scene (TYPE such as BOX, CIRCLE, JOYSTICK), read from the live scene. */
    public static ArrayList<String[]> elementKinds() throws Exception {
        final ArrayList<String> names = elementNames();
        return Gl.run(() -> {
            ArrayList<String[]> out = new ArrayList<>();
            for (String n : names) {
                Actor actor = Bridge.actor(n);
                if (actor == null) continue;
                Map<String, String> live = Bridge.snapshot(PropertySet.getPropertySet(actor));
                out.add(new String[]{n, String.valueOf(live.get("TYPE"))});
            }
            return out;
        });
    }

    /** Sets properties of one element, for example {"width":300,"height":300}. */
    public static void setProps(String name, String propsJson) throws Exception {
        JsonObject o = new JsonObject();
        o.addProperty("op", "set");
        o.addProperty("name", name);
        o.add("props", JsonParser.parseString(propsJson).getAsJsonObject());
        JsonArray ops = new JsonArray();
        ops.add(o);
        JsonObject a = new JsonObject();
        a.add("ops", ops);
        EditTools.elements(a);
    }
}
