package com.englborba.rotina;

import android.content.Context;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Recebe do app a agenda dos próximos dias (JSON) e atualiza o widget da tela inicial. */
@CapacitorPlugin(name = "WidgetBridge")
public class WidgetBridge extends Plugin {
    @PluginMethod
    public void update(PluginCall call) {
        String json = call.getString("data", "{}");
        Context ctx = getContext();
        ctx.getSharedPreferences(RotinaWidget.PREFS, Context.MODE_PRIVATE).edit().putString(RotinaWidget.KEY, json).commit();
        RotinaWidget.atualizarTodos(ctx);
        call.resolve(new JSObject());
    }
}
