# Firebase Distribution Rehearsal

## Goal

Prove the same release path that will be shown in the Android CI/CD finale:

```text
GitHub Actions release workflow
-> release signing secrets
-> signed APK
-> GitHub artifact for traceability
-> Firebase App Distribution tester delivery
```

## Required GitHub Environment

Create or update the `production` environment in GitHub, then add these environment secrets:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`
- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `FIREBASE_ANDROID_APP_ID`

Do not commit keystore files, service account JSON, signed APKs, tester emails, or Firebase project identifiers.

## Rehearsal Workflow

Workflow file:

```text
.github/workflows/android-release-build.yml
```

Branch proof:

```text
codex/firebase-distribution-rehearsal
```

Manual dispatch defaults:

```text
checkout_ref=codex/firebase-distribution-rehearsal
tester_group=android-testers
```

## Local Preflight Results

- `./gradlew :app:assembleRelease` succeeds without signing secrets for normal local development.
- Without signing env vars, the local output remains `app-release-unsigned.apk`.
- With a disposable temp keystore and release signing env vars, Gradle produces `app-release.apk`.
- `apksigner verify --verbose app/build/outputs/apk/release/app-release.apk` passes for the signed rehearsal APK.
- `CI=true ./gradlew :app:assembleRelease` fails fast when release signing secrets are missing.

## What To Watch In CI

- Secret validation should fail before Gradle if any required production environment secret is missing.
- Gradle should produce `app-release.apk`, not `app-release-unsigned.apk`.
- APK path should come from `app/build/outputs/apk/release/output-metadata.json`.
- `apksigner verify` should pass in GitHub Actions.
- The GitHub artifact should preserve the same APK sent to Firebase.
- Firebase CLI should authenticate through `GOOGLE_APPLICATION_CREDENTIALS`, not `FIREBASE_TOKEN`.
- Firebase App Distribution should return release/tester links after upload.

## Branch CI Result

First pushed run:

```text
Run ID: 26965135599
Branch: codex/firebase-distribution-rehearsal
Result: Failed at Validate release and Firebase secrets
```

This is the expected first failure before the GitHub `production` environment is configured.

Missing environment secrets:

- `FIREBASE_ANDROID_APP_ID`
- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Do not copy the workflow to `main` for manual dispatch until the branch run succeeds with real environment secrets.

## First Successful Run

After merging the workflow to `main` and configuring the `production` environment with real (throwaway) secrets, the end-to-end path went green.

- Workflow source: `main` (workflow originally validated on `codex/firebase-distribution-rehearsal`).
- Run ID: _fill in from the Actions tab for the first fully green run_.
- Version delivered: `2.3.1+local.94e0bea` (versionCode `20301`).
- Firebase release URL: https://console.firebase.google.com/project/versiondemo-rehearsal/appdistribution/app/android:com.codetutor.versioningfametals/releases/68pv6h09kd800
- Tester group: `android-testers`, one tester (secondary Gmail) confirmed received and opened the invite.
- Outstanding rehearsal item: confirm SHA256 byte-identity between the GitHub Actions artifact APK and the APK Firebase delivers. This is the traceability claim chapter 11:15 of the recording outline makes.

## Failures Encountered And Resolved

Three failure modes were hit during rehearsal. Logged here so future runs and the live recording can recognize each in seconds.

### 1. Missing environment secrets (expected first failure)

- Symptom: workflow exits at the `Validate release and Firebase secrets` step with `Error: Missing required production environment secret: <name>` for each missing secret.
- Cause: `production` environment did not exist or required secrets were not populated.
- Fix: create the `production` environment in GitHub repo settings; add all six secrets as environment secrets. Repository-level secrets are visible to the workflow but defeat the environment-gate teaching point of Part 18.

### 2. PKCS12 password collapse during signing

- Symptom: Gradle fails `:app:packageRelease` with `com.android.ide.common.signing.KeytoolException: Failed to read key release from store "/home/runner/work/_temp/***.jks": Get Key failed: Given final block not properly padded.`
- Cause: the rehearsal keystore was generated with `keytool` defaults, which produces a PKCS12 file. PKCS12 collapses the store and key passwords to a single value, so `-keypass rehearsalKey123` was silently ignored and the in-file key password matched the store password. The GitHub secret `RELEASE_KEY_PASSWORD` was set to the typed `-keypass` value and did not match.
- Fix: set `RELEASE_KEY_PASSWORD` to the same value as `RELEASE_STORE_PASSWORD` for any PKCS12 keystore generated by modern `keytool`.
- Prevention: pass a single password to both `-storepass` and `-keypass` when generating the keystore. The keytool warning `Different store and key passwords not supported for PKCS12 KeyStores. Ignoring user-specified -keypass value.` is the canary; do not ignore it.

### 3. Upload succeeded but distribution to group failed

- Symptom: the `Distribute APK to Firebase App Distribution` step shows red but its log contains `✔ uploaded new release ... successfully!` and `✔ added release notes successfully`. The step exits non-zero immediately after `i distributing to testers/groups...`.
- Cause: the tester group `android-testers` did not exist under the specific Firebase Android app the workflow targets. Tester groups in App Distribution are scoped to each registered Android app, not to the Firebase project as a whole.
- Fix: create the tester group inside the specific Android app's App Distribution settings and add at least one tester. Confirm via direct URL: `https://console.firebase.google.com/project/versiondemo-rehearsal/appdistribution/app/android:com.codetutor.versioningfametals/testers`.
- Prevention: when registering a new Android app in Firebase, create its tester group and add at least one tester before triggering the first distribution. If the project hosts multiple Android apps (multi-variant demos), repeat under each.

## Lessons Captured For The Live Recording

The findings above have been folded into the canonical series docs so the live recording does not repeat them:

- Operational pre-recording checklist (Firebase project, app registration, SA + role, tester group + tester, keystore password handling, GitHub `production` environment, byte-identity check): see `Pre-Recording Setup → Operational prerequisites` in `01_Series/Mobile_Engineering/Android_CICD/Scripts/CT-YT-2026-025_recording_outline.md`.
- Extended community pain points (PKCS12 trap, per-app group scope) and pre-demo cleanup additions (SA "Create and close" trap, Firebase Add-app wizard SDK push, direct URLs): see `01_Series/Mobile_Engineering/Android_CICD/Research/CT-YT-2026-025_demo_discovery_notes.md`.
- Observed concrete error strings: see the `Observed during rehearsal` subsection of the same demo discovery notes.
