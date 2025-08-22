#!/usr/bin/env bash
set -e
PROJECT_NAME="jetstream-demo"
ZIP_NAME="${PROJECT_NAME}.zip"

echo "Cleaning target..."
mvn -q -DskipTests package

echo "Building zip..."
TMP_DIR=".pack-tmp"
rm -rf "${TMP_DIR}"
mkdir -p "${TMP_DIR}"

cp -r pom.xml README.md src target/jetstream-demo-1.0.0.jar "${TMP_DIR}/"
(
  cd "${TMP_DIR}"
  zip -qr "../${ZIP_NAME}" .
)
rm -rf "${TMP_DIR}"
echo "OK -> ${ZIP_NAME}"
