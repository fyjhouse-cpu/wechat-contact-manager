#!/bin/sh
JAVA_HOME="${JAVA_HOME:-$(dirname $(dirname $(readlink -f $(which java))))}"
APP_HOME="$(cd "$(dirname "$0")" && pwd)"
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
exec "$JAVA_HOME/bin/java" -jar "$GRADLE_WRAPPER_JAR" "$@"