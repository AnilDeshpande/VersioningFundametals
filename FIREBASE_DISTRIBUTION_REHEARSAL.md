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
