package com.englborba.rotina;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Widget "Rotina · Agenda": resumo no topo e lista rolável com os próximos dias. */
public class RotinaWidget extends AppWidgetProvider {
    static final String PREFS = "rotina_widget";
    static final String KEY = "dados";

    static String hoje() { return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()); }

    static JSONObject dados(Context ctx) {
        try { return new JSONObject(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "{}")); }
        catch (Exception e) { return new JSONObject(); }
    }

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, montar(ctx, id));
        mgr.notifyAppWidgetViewDataChanged(ids, R.id.lista);
    }

    static void atualizarTodos(Context ctx) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
        int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, RotinaWidget.class));
        for (int id : ids) mgr.updateAppWidget(id, montar(ctx, id));
        mgr.notifyAppWidgetViewDataChanged(ids, R.id.lista);
    }

    static RemoteViews montar(Context ctx, int widgetId) {
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_rotina);
        String titulo = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR")).format(new Date());
        v.setTextViewText(R.id.data, titulo.substring(0, 1).toUpperCase() + titulo.substring(1));

        JSONObject raiz = dados(ctx);
        int comp = 0, tar = 0, atr = 0;
        try {
            JSONObject dia = raiz.optJSONObject("dias") != null ? raiz.getJSONObject("dias").optJSONObject(hoje()) : null;
            JSONArray itens = dia != null ? dia.optJSONArray("itens") : null;
            for (int i = 0; itens != null && i < itens.length(); i++) {
                if (itens.getJSONObject(i).optString("hora", "").isEmpty()) tar++; else comp++;
            }
            JSONArray a = raiz.optJSONArray("atrasados");
            atr = a != null ? a.length() : raiz.optInt("atrasados", 0);
        } catch (Exception ignored) { }
        StringBuilder r = new StringBuilder();
        if (comp + tar == 0) r.append("Dia livre");
        else {
            if (comp > 0) r.append(comp).append(comp == 1 ? " compromisso" : " compromissos");
            if (tar > 0) { if (r.length() > 0) r.append(" · "); r.append(tar).append(tar == 1 ? " tarefa" : " tarefas"); }
        }
        if (atr > 0) r.append(" · ").append(atr).append(atr == 1 ? " atrasado" : " atrasados");
        v.setTextViewText(R.id.resumo, r.toString());

        // lista rolável
        Intent svc = new Intent(ctx, RotinaWidgetService.class);
        svc.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        svc.setData(Uri.parse(svc.toUri(Intent.URI_INTENT_SCHEME)));
        v.setRemoteAdapter(R.id.lista, svc);
        v.setEmptyView(R.id.lista, R.id.vazio);

        Intent abrir = new Intent(ctx, MainActivity.class);
        abrir.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, abrir, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.topo, pi);
        v.setOnClickPendingIntent(R.id.vazio, pi);
        PendingIntent tpl = PendingIntent.getActivity(ctx, 1, abrir, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        v.setPendingIntentTemplate(R.id.lista, tpl);
        return v;
    }
}
