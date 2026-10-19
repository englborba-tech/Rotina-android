package com.englborba.rotina;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.RemoteViews;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Widget "Rotina · Calendário": mês inteiro no topo e a agenda (com as pendências das obras) embaixo. */
public class RotinaCalWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context ctx, AppWidgetManager mgr, int[] ids) {
        for (int id : ids) mgr.updateAppWidget(id, RotinaWidget.montar(ctx, id, R.layout.widget_calendario));
        mgr.notifyAppWidgetViewDataChanged(ids, R.id.lista);
    }

    static void preencherMes(Context ctx, RemoteViews v, JSONObject raiz) {
        Locale br = new Locale("pt", "BR");
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar hoje = Calendar.getInstance();
        String hk = iso.format(hoje.getTime());
        String mes = new SimpleDateFormat("MMMM yyyy", br).format(hoje.getTime());
        v.setTextViewText(R.id.mes, mes.substring(0, 1).toUpperCase() + mes.substring(1));
        JSONObject marcas = raiz.optJSONObject("marcas");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        int mesAtual = c.get(Calendar.MONTH);
        c.add(Calendar.DAY_OF_MONTH, -(c.get(Calendar.DAY_OF_WEEK) - 1));
        Calendar fimMes = Calendar.getInstance();
        fimMes.set(Calendar.DAY_OF_MONTH, fimMes.getActualMaximum(Calendar.DAY_OF_MONTH));
        for (int i = 0; i < 42; i++) {
            int id = ctx.getResources().getIdentifier("c" + i, "id", ctx.getPackageName());
            if (i % 7 == 0) {
                int row = ctx.getResources().getIdentifier("sem" + (i / 7), "id", ctx.getPackageName());
                boolean passou = c.after(fimMes) && c.get(Calendar.MONTH) != mesAtual;
                v.setViewVisibility(row, passou ? View.GONE : View.VISIBLE);
            }
            String k = iso.format(c.getTime());
            boolean dentro = c.get(Calendar.MONTH) == mesAtual;
            JSONObject m = marcas != null ? marcas.optJSONObject(k) : null;
            int a = m != null ? m.optInt("a", 0) : 0, o = m != null ? m.optInt("c", 0) : 0;
            String dia = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
            String ponto = a > 0 && o > 0 ? "••" : (a > 0 || o > 0) ? "•" : " ";
            SpannableString txt = new SpannableString(dia + "\n" + ponto);
            if (a > 0 || o > 0) txt.setSpan(new StyleSpan(Typeface.BOLD), 0, dia.length(), 0);
            v.setTextViewText(id, txt);
            int cor;
            if (k.equals(hk)) cor = Color.WHITE;
            else if (!dentro) cor = Color.parseColor("#55AAB4CC");
            else if (a > 0) cor = Color.parseColor("#9DB6F5");
            else if (o > 0) cor = Color.parseColor("#F0A84A");
            else cor = Color.parseColor("#C9D0DD");
            v.setTextColor(id, cor);
            v.setInt(id, "setBackgroundResource", k.equals(hk) ? R.drawable.widget_hoje : 0);
            c.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
}
