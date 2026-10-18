package com.englborba.rotina;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Fornece as linhas da agenda do widget: cabeçalhos de dia e itens. */
public class RotinaWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) { return new Fabrica(getApplicationContext()); }

    static class Linha {
        boolean cab; String texto, hora, titulo, sub; int prio; boolean obra, atrasado;
    }

    static class Fabrica implements RemoteViewsFactory {
        private final Context ctx;
        private final List<Linha> linhas = new ArrayList<>();
        Fabrica(Context c) { ctx = c; }

        @Override public void onCreate() { }
        @Override public void onDestroy() { linhas.clear(); }
        @Override public int getCount() { return linhas.size(); }
        @Override public RemoteViews getLoadingView() { return null; }
        @Override public int getViewTypeCount() { return 2; }
        @Override public long getItemId(int p) { return p; }
        @Override public boolean hasStableIds() { return false; }

        private Linha cab(String t) { Linha l = new Linha(); l.cab = true; l.texto = t; return l; }

        private Linha item(JSONObject o, boolean atrasado, String dataTxt) {
            Linha l = new Linha();
            l.hora = o.optString("hora", "");
            l.titulo = o.optString("titulo", "");
            String local = o.optString("local", "");
            String det = o.optString("detalhe", "");
            l.obra = o.optBoolean("obra", false);
            l.prio = o.optInt("prio", 0);
            l.atrasado = atrasado;
            StringBuilder s = new StringBuilder();
            if (atrasado && dataTxt != null) s.append(dataTxt);
            if (!local.isEmpty() && !l.obra) { if (s.length() > 0) s.append(" · "); s.append(local); }
            if (l.obra && !det.isEmpty()) { if (s.length() > 0) s.append(" · "); s.append(det); }
            l.sub = s.toString();
            return l;
        }

        @Override
        public void onDataSetChanged() {
            linhas.clear();
            JSONObject raiz = RotinaWidget.dados(ctx);
            Locale br = new Locale("pt", "BR");
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat fmt = new SimpleDateFormat("EEEE, d 'de' MMM", br);
            SimpleDateFormat curto = new SimpleDateFormat("dd/MM", br);
            try {
                JSONArray atr = raiz.optJSONArray("atrasados");
                if (atr != null && atr.length() > 0) {
                    linhas.add(cab("Atrasados"));
                    for (int i = 0; i < atr.length(); i++) {
                        JSONObject o = atr.getJSONObject(i);
                        String d = o.optString("data", "");
                        String dt = null;
                        try { dt = "desde " + curto.format(iso.parse(d)); } catch (Exception ignored) { }
                        linhas.add(item(o, true, dt));
                    }
                }
                JSONObject dias = raiz.optJSONObject("dias");
                Calendar c = Calendar.getInstance();
                for (int i = 0; dias != null && i < 8; i++) {
                    String k = iso.format(c.getTime());
                    JSONObject dia = dias.optJSONObject(k);
                    JSONArray itens = dia != null ? dia.optJSONArray("itens") : null;
                    if (itens != null && itens.length() > 0) {
                        String nome = i == 0 ? "Hoje" : i == 1 ? "Amanhã" : fmt.format(c.getTime());
                        if (i <= 1) nome += " · " + new SimpleDateFormat("EEE, d 'de' MMM", br).format(c.getTime());
                        linhas.add(cab(nome.substring(0, 1).toUpperCase() + nome.substring(1)));
                        for (int j = 0; j < itens.length(); j++) linhas.add(item(itens.getJSONObject(j), false, null));
                    }
                    c.add(Calendar.DAY_OF_MONTH, 1);
                }
            } catch (Exception ignored) { }
        }

        @Override
        public RemoteViews getViewAt(int p) {
            if (p < 0 || p >= linhas.size()) return null;
            Linha l = linhas.get(p);
            RemoteViews v;
            if (l.cab) {
                v = new RemoteViews(ctx.getPackageName(), R.layout.widget_dia);
                v.setTextViewText(R.id.dia, l.texto);
                v.setTextColor(R.id.dia, l.texto.equals("Atrasados") ? Color.parseColor("#F07562") : Color.parseColor("#9AA7C4"));
            } else {
                v = new RemoteViews(ctx.getPackageName(), R.layout.widget_item);
                v.setTextViewText(R.id.hora, l.hora.isEmpty() ? "•" : l.hora);
                v.setTextViewText(R.id.titulo, (l.obra ? "🛒 " : "") + l.titulo);
                if (l.sub.isEmpty()) v.setViewVisibility(R.id.sub, View.GONE);
                else { v.setViewVisibility(R.id.sub, View.VISIBLE); v.setTextViewText(R.id.sub, l.sub); }
                int cor = l.prio >= 3 ? Color.parseColor("#F07562") : l.prio == 2 ? Color.parseColor("#F0A84A") : Color.parseColor("#7D9CF0");
                v.setInt(R.id.barra, "setBackgroundColor", cor);
            }
            v.setOnClickFillInIntent(R.id.linha, new Intent());
            return v;
        }
    }
}
