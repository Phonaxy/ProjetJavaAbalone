#!/bin/sh
# Maven Wrapper script
# Downloads Maven if not present and runs it

set -e

MAVEN_HOME="${HOME}/.m2/wrapper/dists/apache-maven-3.9.6"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.tar.gz"

if [ ! -f "${MAVEN_HOME}/bin/mvn" ]; then
    echo "Downloading Maven 3.9.6..."
    mkdir -p "${MAVEN_HOME}"
    TMP_FILE=$(mktemp)
    curl -fsSL "${MAVEN_URL}" -o "${TMP_FILE}"
    tar -xzf "${TMP_FILE}" -C "${MAVEN_HOME}" --strip-components=1
    rm -f "${TMP_FILE}"
    echo "Maven 3.9.6 installed."
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
