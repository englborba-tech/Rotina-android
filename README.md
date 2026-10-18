# Rotina (app Android)

App de agenda e rotina com resumo da manhã, lembretes, widget de tela inicial e as compras geradas pelo app de obras.
Os dados ficam no mesmo Firebase dos outros apps (coleção `agenda`).

- `www/` — o app (o mesmo da versão web, com login nativo, notificações e widget)
- `android/` — projeto Android (Capacitor 7)
- `.github/workflows/apk.yml` — a cada envio para `main`, o GitHub compila o APK e publica em **Releases**

Para alterar o app, edite `www/index.html` e envie: o APK novo sai em alguns minutos.
