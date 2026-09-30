# TennoFreunde – Handbuch

Android 7 oder neuer

TennoFreunde begleitet deine Warframe-Sammlung auf Android. Die App verbindet Weltstatus, Sammlungsfortschritt, Screenshot-Erkennung, Inventarimport, Arsenal, virtuelle Schmiede und eine optionale Cloud-Sicherung. Warframe selbst wird dabei nicht verändert. TennoFreunde benötigt kein Warframe-Passwort und speichert keine Warframe-Anmeldedaten.

## 1. Installation und Update

1. Lade die Datei `TennoFreunde-11.57.apk` ausschließlich von der offiziellen TennoFreunde-Release-Seite.
2. Öffne die heruntergeladene APK.
3. Falls Android fragt, erlaube deinem Browser oder Dateimanager einmalig, Apps aus dieser Quelle zu installieren.
4. Bestätige die Installation im Android-Systemfenster.
5. Öffne TennoFreunde.

Bei einem Update lädt TennoFreunde die neue APK automatisch herunter, prüft Paketname und Signatur und öffnet anschließend das Android-Installationsfenster. Android verlangt dort weiterhin eine Bestätigung. Nach dem erfolgreichen Update wird die zwischengespeicherte APK beim nächsten Start entfernt. Das Update-Fenster bleibt als Änderungsübersicht erhalten.

Wenn Android meldet, dass die App zum Schutz des Geräts blockiert wurde, öffne **Weitere Details** und erlaube die Installation nur dann, wenn die APK von der offiziellen TennoFreunde-Seite stammt. Eine bereits installierte Version sollte vor einem Update nicht gelöscht werden, damit lokale Daten erhalten bleiben. Scheitert ein direktes Update wegen einer anderen Signatur, zuerst in TennoFreunde unter **Exportieren** eine Sicherung erstellen.

## 2. Erste Einrichtung

- Öffne **Einstellungen** und wähle Deutsch oder Englisch.
- Wähle die Plattform, deren Live-Daten du sehen möchtest. Für PlayStation gilt PS4/PS5.
- Erlaube Benachrichtigungen, wenn du Hinweise zu Updates, Baro, Eidolon-Nacht, Schmiede oder Sicherungen erhalten möchtest.
- Erstelle unter **Konto & Cloud** ein TennoFreunde-Konto, wenn du Fortschritt zwischen Geräten sichern möchtest. Die App funktioniert auch ohne Konto.
- Erstelle nach der ersten Einrichtung über **Exportieren** eine lokale Sicherungsdatei.

## 3. Navigation und Zurück-Taste

Das Seitenmenü oben links öffnet alle Bereiche. Die Android-Zurück-Taste schließt zuerst Dialoge oder das Menü, führt aus einer Unterseite zur Hauptseite und beendet die App erst von dort aus. Die sichtbaren Zurück-Pfeile verhalten sich genauso.

### Hauptseite

Die Hauptseite zeigt den aktuellen Weltstatus für die ausgewählte Plattform: Weltenzyklen, Void-Risse, Alarme, Invasionen, Sortie, Events, tägliche Angebote, Neuigkeiten und Baro Ki’Teer. Such- und Filterfunktionen helfen bei langen Listen. Gespeicherte Daten bleiben im Offline-Modus sichtbar.

### Tenno-Zentrale

Die Tenno-Zentrale bündelt Fortschritt, Farm-Plan, Favoriten, aktive passende Risse, fast fertige Gegenstände und zusätzliche Planer. Favorisierte oder als „Jetzt farmen“ markierte Einträge werden höher gewichtet.

### Sammlung

Die Sammlung ist dein dauerhafter Fortschritt. Ein Haken bedeutet: **diesen Gegenstand oder Bestandteil habe ich mindestens einmal besessen oder abgeschlossen**. Ein bestätigter Fund aus Scanner, Profil oder vollständigem Inventarsnapshot darf einen Eintrag automatisch abhaken. Sinkt der aktuelle Bestand später auf null, bleibt der Sammlungshaken bestehen.

Du kannst suchen, nach Kategorien filtern, nur fehlende oder fast fertige Einträge anzeigen, Favoriten setzen und sichtbare Einträge gesammelt bearbeiten. Bereits vorhandene Haken werden bei Katalog- oder Cloud-Erweiterungen nicht gelöscht.

### Arsenal

Das Arsenal zeigt gebaute und besessene Warframes, Waffen, Begleiter und weitere Ausrüstung. Hier können Rang beziehungsweise Level und unterstützte Ausbauinformationen gepflegt werden. Normale Ausrüstung reicht bis Rang 30; besondere Ausrüstung wie Kuva-, Tenet-, Paracesis- oder Necramech-Gegenstände kann bis Rang 40 unterstützt werden.

### Schmiede

Die Schmiede ist vom Arsenal getrennt. Sie zeigt baubare Gegenstände, vorhandene und fehlende Bestandteile, Bauzeit, laufende Timer und fertige Abholungen. Ein virtueller Bau dient als Begleiter zum echten Bau im Spiel. Ressourcen- und Blaupauseninformationen werden berücksichtigt, soweit sie durch Katalog, Scanner oder Inventarimport bekannt sind.

## 4. Screenshot-Scanner

Der Scanner erkennt Texte aus Android-, PS4- und PS5-Screenshots. Er kann mehrere Bilder nacheinander und speicherschonend verarbeiten.

1. Öffne **Screenshot-Scanner**.
2. Wähle ein oder mehrere Bilder aus. Alternativ kannst du Bilder aus der PlayStation-App über **Teilen → TennoFreunde** senden.
3. Warte, bis die Warteschlange verarbeitet wurde. Du kannst die Seite verlassen; vorbereitete Ordner- und Hintergrundaufträge werden weitergeführt.
4. Prüfe die erkannten Treffer.
5. Übernimm bestätigte Treffer. Bereits gesetzte Haken bleiben unverändert; neue bestätigte Bestandteile werden ergänzt.

Offizielle Katalogtreffer können gesammelt übernommen werden. Unsichere oder unbekannte OCR-Treffer bleiben zur Einzelprüfung sichtbar. Neue Gegenstände werden in die passende Kategorie eingeordnet, sofern der Katalog oder die erkannten Daten eine eindeutige Zuordnung erlauben. Die letzte Übernahme kann rückgängig gemacht werden.

Für große Bildmengen empfiehlt sich ein eigener Android-Ordner. TennoFreunde merkt bereits verarbeitete Dateien und arbeitet in begrenzten Blöcken, damit der Arbeitsspeicher nicht überlastet wird.

## 5. Inventar, Profil und Windows-Helfer

### Warframe-Profil

Über **Warframe-Profil einlesen** kann eine von Warframe bereitgestellte Profil-Datei geprüft werden. Die App zeigt eine Vorschau und übernimmt nur eindeutig zugeordnete positive Besitzinformationen. Vorhandene Sammlungshaken werden nicht entfernt.

### Vollständiger Inventarsnapshot

Ein vollständiger `inventory.json`-Snapshot enthält aktuelle Mengen, beispielsweise Ressourcen, Mods, Arcanes, Relikte, Ausrüstung, Blaupausen und optional Schmiedeaufträge. Vor der Übernahme zeigt TennoFreunde eine Differenzvorschau mit neuen, geänderten, unveränderten und auf null sinkenden Einträgen.

Nur ein als vollständig validierter Snapshot darf aktuelle Inventarmengen erhöhen **und** reduzieren. Fehlen Hauptbereiche oder ist ein ungewöhnlich großer Rückgang erkennbar, wird der Import abgelehnt. Eine fehlende optionale Schmiede-Sektion bedeutet nicht automatisch, dass keine Bauaufträge vorhanden sind. Die Rohdatei wird nach erfolgreicher Normalisierung nicht dauerhaft gespeichert.

Der experimentelle Windows-Helfer ist optional. Er greift nur lesend auf einen bereits vom offiziellen Warframe-Client geladenen Zustand zu. Er verwendet keinen eigenen Warframe-Login, keine DLL-Injektion und keine Netzwerkmanipulation. Ein solcher Speicherzugriff ist von Digital Extremes nicht ausdrücklich freigegeben; deshalb muss der Nutzer dem Risiko ausdrücklich zustimmen. TennoFreunde bleibt ohne den Helfer vollständig nutzbar.

## 6. Konto und Cloud

Das TennoFreunde-Konto sichert App-Fortschritt. Es ist kein Warframe-Konto.

- **Benutzer** verwalten ihre eigenen Daten und Sicherungen.
- **Moderatoren** sind als Teamkonten gekennzeichnet.
- **Administratoren** können Benutzerrollen und eingehende Fehlerberichte verwalten.

Die Cloud-Sicherung überschreibt lokale Daten nicht ungefragt. Eine Wiederherstellung zeigt zuerst eine Vorschau. Bekannte Einträge werden zusammengeführt; unbekannte oder neuere lokale Daten sollen erhalten bleiben. Für den vollständigen Inventartransfer kann eine bereinigte Datei kurzzeitig kontogebunden übertragen und nach erfolgreicher Übernahme aus der Transferablage gelöscht werden.

Die lokale Funktion **Exportieren** bleibt die wichtigste zusätzliche Sicherung. Vor Neuinstallation, Gerätewechsel oder einer erzwungenen Deinstallation immer exportieren. Mit **Importieren** wird die Sicherung zuerst geprüft und erst nach Bestätigung übernommen.

## 7. Benachrichtigungen

In den Einstellungen lassen sich Hinweise getrennt steuern. Je nach Freigabe kann TennoFreunde melden:

- neue App-Versionen,
- Baro Ki’Teers Ankunft oder Anwesenheit,
- bevorstehende Eidolon-Nacht,
- fertige virtuelle Schmiedeaufträge,
- fällige lokale Sicherungen.

Android kann Benachrichtigungen systemweit blockieren. Prüfe bei fehlenden Hinweisen **Android-Einstellungen → Apps → TennoFreunde → Benachrichtigungen**.

## 8. Feedback und Fehlerberichte

Unter **Feedback / Fehler melden** kannst du eine Idee oder einen Fehler beschreiben und optional einen Screenshot anhängen. Die App ergänzt automatisch hilfreiche technische Angaben wie App-Version, Android-Version, Hersteller und Gerätemodell. Ein Screenshot wird verkleinert und komprimiert übertragen.

Ab Version 11.57 erfasst Firebase Crashlytics außerdem schwere Abstürze automatisch. Dadurch kann ein Fehler ausgewertet werden, selbst wenn die App direkt nach dem Start wieder geschlossen wird. Passwörter, Warframe-Tokens oder Warframe-Sitzungsdaten gehören nicht in einen Bericht und werden von TennoFreunde nicht benötigt.

## 9. Einstellungen

Zu den gespeicherten Optionen gehören Sprache, Plattform, Startseite, Farbschema, größere Schrift, kompakter Modus, Offline-Modus, letzte Seite merken, Favoriten oben anpinnen, Benachrichtigungen und Sicherungserinnerung. Diagnoseinformationen und lokale Fehlerprotokolle helfen bei Problemen.

## 10. Häufige Probleme

### Die App öffnet sich und schließt sofort

Installiere die aktuelle Version 11.57 oder neuer. Diese Version enthält automatische Absturzberichte. Wenn die App kurz offen bleibt, sende zusätzlich unter **Feedback / Fehler melden** eine Beschreibung. Hilft ein Update nicht, erst nach einer vorhandenen Export-Sicherung neu installieren.

### Arsenal oder Schmiede reagiert nicht

Version 11.57 verwendet einen vorab aufgebauten Inventarindex. Damit entfällt die frühere sehr langsame wiederholte Suche durch tausende Datensätze. Bei weiterem Hängen bitte einen Fehlerbericht mit App-Version und Gerätemodell senden.

### Scanner liest Bilder nicht

Prüfe, ob die Datei wirklich lokal geöffnet werden kann. Bei Cloudbildern zuerst vollständig herunterladen oder über **Teilen → TennoFreunde** senden. PS5-4K-Bilder werden im Original-Koordinatensystem zugeschnitten; ältere Versionen konnten dabei einen „Subset Rect“-Fehler zeigen.

### Cloud-Synchronisierung schlägt fehl

Internetverbindung und Anmeldung prüfen, danach erneut synchronisieren. Lokale Daten bleiben erhalten. Eine lokale Exportdatei kann unabhängig von der Cloud erstellt werden.

### Sammlung scheint leer

Nicht sofort neu speichern oder Cloud-Daten löschen. App neu starten, Anmeldung prüfen und die Cloud-Wiederherstellung nur nach Kontrolle der Vorschau ausführen. Eine vorhandene Exportdatei kann verlustfrei geprüft werden.

### Update lässt sich nicht installieren

Erlaube die Installation aus der verwendeten Quelle. Meldet Android eine inkompatible Signatur, stammt die installierte App wahrscheinlich aus einer alten Testsignatur. Zuerst exportieren, danach die alte App deinstallieren, die offizielle APK installieren und die Sicherung importieren.

## 11. Datenschutz und Grenzen

TennoFreunde ist ein unabhängiges Begleitprojekt. Die App verändert Warframe nicht und benötigt keine Warframe-Zugangsdaten. Persönlicher Fortschritt liegt lokal und – nur bei Nutzung der Konto-/Cloudfunktionen – im zugehörigen Firebase-Projekt. Feedback enthält die eingegebenen Texte, optionale komprimierte Screenshots und die angezeigten Gerätedaten. Live-Daten und Kataloginformationen können von externen Warframe-Datenquellen stammen und zeitlich verzögert sein.

## 12. Gute Sicherungsroutine

1. Regelmäßig **Exportieren** verwenden.
2. Exportdatei außerhalb des App-Ordners aufbewahren.
3. Bei Konto-Nutzung zusätzlich Cloud-Synchronisierung prüfen.
4. Vor Deinstallation oder Gerätewechsel einen neuen Export erstellen.
5. Nach Import oder Cloud-Wiederherstellung die Vorschau kontrollieren.

