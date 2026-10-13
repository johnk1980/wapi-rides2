#!/usr/bin/env bash
# Runs gradle with the given args, saves the log, and surfaces the key error lines as
# GitHub annotations so they can be read without downloading the full log.
set +e
gradle "$@" > build.log 2>&1
code=$?
tail -n 40 build.log
if [ $code -ne 0 ]; then
  grep -E '^e: ' build.log | sed 's#file:///home/runner/work/[^/]*/[^/]*/##' | sort -u | head -n 40 | while IFS= read -r line; do
    echo "::error title=Kotlin error::${line}"
  done
  grep -nE 'What went wrong|Could not resolve|Execution failed|Caused by|error:|FAILURE:|Unresolved|wasm|Task .* FAILED' build.log | head -n 20 | while IFS= read -r line; do
    echo "::error title=Gradle::${line}"
  done
fi
exit $code
