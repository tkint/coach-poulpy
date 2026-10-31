# Releasing a beta

Coach Poulpy is in beta. A release is a GitHub pre-release with the Android APK attached, made by one button and no agent.

## The version

The version people see (Settings, "Version 0.1.0") is the one line in [`VERSION`](https://github.com/tkint/coach-poulpy/blob/main/VERSION) at the repository root. Android reads it for `versionName` and iOS gets it as `MARKETING_VERSION`, so there is nowhere else to change it. It is three numbers, like `0.2.0`; while in beta it stays `0.x.y`.

## Cut a release

1. Raise `VERSION` in a pull request (`chore/release-0.2.0`) and merge it once CI is green.
2. On GitHub, open **Actions > Release > Run workflow**, keep the branch `main`, and run it.
3. A few minutes later the **Releases** page has a pre-release `v0.2.0` with `coach-poulpy-v0.2.0.apk` and notes listing the merged pull requests since the last one.
4. On a phone, download the APK from that page and install it over the old one; the phone's data is kept.

The workflow stops with a message, and publishes nothing, if `VERSION` is not three numbers, if the tag for that version already exists (raise `VERSION` first), or if the signing key secrets are missing (see the README, "The signing key").

## What a release does not do yet

- **iPhone:** no file is attached. A phone build needs an Apple Developer account and signing; until then iOS is built from Xcode (README).
- **Debug build type:** the APK is the same `assembleDebug` build CI makes, the one that has been run on phones. A smaller, minified `assembleRelease` build is a later step once it has been tried on a phone.
- **Not a store release:** the pre-release flag keeps it off the "latest" release, and nothing goes to Google Play or TestFlight.
