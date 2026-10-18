package com.englborba.rotina;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Widget "Hoje": mostra os itens do dia gravados pelo app.
 * O app grava um JSON por dia ({"dias":{"2026-10-18":{"itens":[...],"atrasados":n}}}),
 * então o widget continua certo na virada do dia mesmo sem abrir o app.
 */
public class RotinaWidget extends AppWidgetProvider {
    static final String PREFS = "rotina_widget";
    static final String KEY = "dados";
    private static final int[] LINHAS = {R.id.l1, R.id.l2, R.id.l3, R.id.l4, R.id.l5};

    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, montar(ctx));
    }

    static RemoteViews montar(Context ctx) {
        RemoteViews v = new RemoteViews(ctx.getPackageName(), R.layout.widget_rotina);
        String hoje = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        String titulo = new SimpleDateFormat("EEEE, d 'de' MMMM", new Locale("pt", "BR")).format(new Date());
        v.setTextViewText(R.id.data, titulo.substring(0, 1).toUpperCase() + titulo.substring(1));
        int n = 0, atrasados = 0, total = 0;
        try {
            JSONObject raiz = new JSONObject(ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "{}"));
            JSONObject dias = raiz.optJSONObject("dias");
            JSONObject dia = dias != null ? dias.optJSONObject(hoje) : null;
            atrasados = raiz.optInt("atrasados", 0);
            if (dia != null) {
                JSONArray itens = dia.optJSONArray("itens");
                total = itens != null ? itens.length() : 0;
                for (int i = 0; itens != null && i < itens.length() && n < LINHAS.length; i++) {
                    JSONObject it = itens.getJSONObject(i);
                    String hora = it.optString("hora", "");
                    String linha = (hora.isEmpty() ? "•  " : hora + "  ") + it.optString("titulo", "");
                    v.setTextViewText(LINHAS[n], linha);
                    v.setViewVisibility(LINHAS[n], View.VISIBLE);
                    n++;
                }
            }
        } catch (Exception ignored) { }
        for (int i = n; i < LINHAS.length; i++) v.setViewVisibility(LINHAS[i], View.GONE);
        String resumo = total == 0 ? "Nada pendente para hoje" : total + (total == 1 ? " item para hoje" : " itens para hoje");
        if (total > LINHAS.length) resumo += " · +" + (total - LINHAS.length);
        if (atrasados > 0) resumo += " · " + atrasados + (atrasados == 1 ? " atrasado" : " atrasados");
        v.setTextViewText(R.id.resumo, resumo);
        Intent abrir = new Intent(ctx, MainActivity.class);
        abrir.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, abrir, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        v.setOnClickPendingIntent(R.id.raiz, pi);
        return v;
    }
}
