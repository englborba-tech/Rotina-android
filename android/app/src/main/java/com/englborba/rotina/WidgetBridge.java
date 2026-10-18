package com.englborba.rotina;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Recebe do app o resumo do dia (JSON) e atualiza o widget da tela inicial. */
@CapacitorPlugin(name = "WidgetBridge")
public class WidgetBridge extends Plugin {
    @PluginMethod
    public void update(PluginCall call) {
        String json = call.getString("data", "{}");
        Context ctx = getContext();
        ctx.getSharedPreferences(RotinaWidget.PREFS, Context.MODE_PRIVATE).edit().putString(RotinaWidget.KEY, json).apply();
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, RotinaWidget.class));
        Intent i = new Intent(ctx, RotinaWidget.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        ctx.sendBroadcast(i);
        JSObject r = new JSObject();
        r.put("widgets", ids.length);
        call.resolve(r);
    }
}
