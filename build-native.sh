#!/bin/sh

cd $(dirname $1)

native-image -jar $(basename $1) -H:Name=minirandr -H:+ReportExceptionStackTraces -J-Dclojure.spec.skip.macros=true -J-Dclojure.compiler.direct-linking=true -J-Xmx3G --initialize-at-build-time --enable-http --enable-https --verbose --no-fallback --no-server --report-unsupported-elements-at-runtime --native-image-info -H:+StaticExecutableWithDynamicLibC -H:CCompilerOption=-pipe --allow-incomplete-classpath --enable-url-protocols=http,https --enable-all-security-services
