#!/usr/bin/env bash
# Makes the Android signing key that every Coach Poulpy build is signed with, and prints the two
# GitHub secrets CI needs. Run it once, on your own computer (it needs a JDK for keytool).
#   tools/make-signing-key.sh [output.jks]
# Keep the .jks file and the password somewhere safe: an APK signed with another key cannot
# install over the app already on a phone.
set -euo pipefail

out="${1:-poulpy-signing.jks}"
if [ -e "$out" ]; then
  echo "$out already exists. Pick another name or move it away first." >&2
  exit 1
fi

password="$(od -An -N24 -tx1 /dev/urandom | tr -d ' \n')"
keytool -genkeypair -v \
  -keystore "$out" -storetype PKCS12 \
  -alias poulpy -keyalg RSA -keysize 4096 -validity 10000 \
  -storepass "$password" -keypass "$password" \
  -dname "CN=Coach Poulpy" >/dev/null

base64_file="$out.base64.txt"
base64 < "$out" | tr -d '\n' > "$base64_file"

cat <<INFO

Made $out.

Add two repository secrets on GitHub (Settings > Secrets and variables > Actions > New repository secret):

  ANDROID_KEYSTORE_PASSWORD  $password
  ANDROID_KEYSTORE_BASE64    the contents of $base64_file

Then delete $base64_file. Keep $out and the password in your password manager.

To sign local builds with the same key, create app-android/keystore.properties (it is not committed):

  storeFile=$(cd "$(dirname "$out")" && pwd)/$(basename "$out")
  storePassword=$password
INFO
