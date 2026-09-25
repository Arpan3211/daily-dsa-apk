# Daily DSA

Shows LeetCode's Daily Challenge in a resizable, scrollable Android home-screen widget.

- `scripts/fetch_daily.py`: fetches the daily problem and writes `data/daily.json`.
- `.github/workflows/daily.yml`: runs the script every day at 00:10 UTC and commits the result.
- `.github/workflows/build-apk.yml`: builds the APK.
- `android/`: the app. The widget shows the title, difficulty, topics and scrollable statement. Tapping it opens the full problem, with a link to LeetCode.

## Setup

1. Create a **public** GitHub repo, then push this folder as the repo root (raw.githubusercontent.com needs a public repo).
2. In the repo go to Settings → Actions → General → Workflow permissions and choose "Read and write".
3. In the Actions tab, run "Fetch daily problem" once, then run "Build APK".
4. Download the `daily-dsa-apk` artifact and unzip it. Install `app-debug.apk` on your phone (allow "install unknown apps").
5. Long-press the home screen, choose Widgets, then Daily DSA, and resize it as you like.

The APK reads `https://raw.githubusercontent.com/<user>/<repo>/main/data/daily.json`. The URL is filled in from the repo name at build time, and the default branch must be `main`.
