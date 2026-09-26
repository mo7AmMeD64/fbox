# Fkbox

Native Android app (Kotlin, Jetpack Compose, Material 3 Expressive, dark only) that browses and plays
MovieBox content. The catalogue client is a direct port of the CloudStream `com.MovieBox` extension
(request signing, token, lists, search, details, seasons, dubs, subtitles); playback uses the
prebuilt aniyomi **mpv** library.

## Build the signed APK (GitHub Actions)
1. Push this folder to a GitHub repository (branch `main`).
2. Open **Actions → Build Fkbox APK** (it also runs on every push, or press *Run workflow*).
3. Download the `Fkbox-release-apk` artifact and install `app-release.apk`.

The workflow downloads the two player libraries from their GitHub releases, then builds and signs the
release APK. It signs with `keystore/fkbox.jks` (password `fkbox-release-key`, alias `fkbox`).
For your own key, add the secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`;
keep the repository private if you keep the bundled key.

## Structure
- `data/moviebox/` – signing, HTTP client, models (pure Kotlin, no Android dependency)
- `data/` – OkHttp transport, settings + saved titles + token (SharedPreferences)
- `ui/` – theme, Home, Search, Saved, Settings, Details, navigation
- `player/` – mpv view, controller, Compose player screen (sources, subtitles, audio, quality, speed)

## Notes
- Adult content is filtered by default (Settings → Adult content).
- Stream links carry a CloudFront cookie that expires after about a week, so links are always fetched
  when you press play and never stored.
