# Automatische Update-Benachrichtigungen

Die App ab Version 11.8 abonniert beim ersten Öffnen automatisch das Firebase-Cloud-Messaging-Thema `app_updates`. Der Workflow `.github/workflows/firebase-update-notification.yml` sendet beim Veröffentlichen eines GitHub-Releases eine Datenbenachrichtigung an dieses Thema.

## Einmalige Einrichtung

1. In Google Cloud für das Firebase-Projekt `tennofreunde` die **Firebase Cloud Messaging API (HTTP v1)** aktivieren.
2. Das Dienstkonto `tennofreunde-update-push@tennofreunde.iam.gserviceaccount.com` besitzt ausschließlich die Rolle **Firebase Cloud Messaging API Admin** mit `cloudmessaging.messages.create`.
3. Der Workload-Identity-Pool `github-tennofreunde` verbindet den OIDC-Anbieter `github-actions` ausschließlich mit `hahawasistlos1/TennoFreunde`.
4. Das Dienstkonto erlaubt diesem Repository die kurzlebige Anmeldung als **Workload Identity User**.
5. Den Workflow zusammen mit dem App-Code zu GitHub übertragen.

Es wird kein privater Firebase-Schlüssel und kein GitHub-Repository-Secret benötigt. GitHub erhält für jeden Workflow-Lauf ein kurzlebiges Token, das nur zum Senden von FCM-Nachrichten berechtigt ist.

## Ablauf

- Ein GitHub-Release wird als **published** veröffentlicht.
- GitHub Actions tauscht sein signiertes OIDC-Token über den eingeschränkten Workload-Identity-Pool gegen ein kurzlebiges Google-Zugriffstoken.
- Der Workflow sendet über FCM HTTP v1 an `app_updates`.
- Installierte Apps vergleichen die Release-Version mit ihrer eigenen Version.
- Nur eine tatsächlich neuere Version erzeugt einmalig eine Android-Benachrichtigung.
- Ein Fingertipp öffnet die Release-Seite auf GitHub.

Auf Android 13 und neuer muss in der App unter **Einstellungen → Benachrichtigungen → Benachrichtigungen erlauben** die Systemberechtigung erteilt sein.
