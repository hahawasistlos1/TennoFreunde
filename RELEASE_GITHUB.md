# GitHub-Release fuer TennoFreunde

TennoFreunde verteilt Updates ueber GitHub-Releases, nicht ueber den Play Store.

## Signierter Release-Build

Lege die Signaturdaten lokal als Gradle-Properties oder Umgebungsvariablen ab:

```properties
TENNO_RELEASE_STORE_FILE=C:\\Pfad\\zu\\tennofreunde-release.jks
TENNO_RELEASE_STORE_PASSWORD=...
TENNO_RELEASE_KEY_ALIAS=...
TENNO_RELEASE_KEY_PASSWORD=...
```

Danach bauen:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat :app:assembleRelease --console=plain --no-daemon --max-workers=1 --no-watch-fs
```

Die APK liegt danach unter:

```text
app\build\outputs\apk\release\app-release.apk
```

## GitHub-Release

1. Version in `app/build.gradle.kts` erhoehen: `versionCode` und `versionName`.
2. Release-APK bauen.
3. APK als Asset in einem neuen GitHub-Release hochladen.
4. Release-Tag passend zur App-Version setzen, aktuell `v10.1`.
5. In der App pruefen, ob der Update-Dialog die neue APK erkennt.

Die App ist auf GitHub-Updates vorbereitet. Der Release-Build wird nur signiert, wenn die Keystore-Daten gesetzt sind; Debug-Builds bleiben davon unabhaengig.
