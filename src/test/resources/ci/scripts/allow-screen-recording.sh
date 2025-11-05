#!/bin/bash
############################################################################
# Automatically handles macOS "Allow Screen Recording" popup during UI tests
# Works in GitHub Actions macOS runners and local runs
############################################################################

echo "🔍 Checking for macOS permission popup..."

for i in {1..10}; do
  osascript <<'END'
  tell application "System Events"
      tell process "SystemUIServer"
          try
              if exists (button "Allow" of window 1) then
                  click button "Allow" of window 1
                  log "✅ Clicked 'Allow' button"
              else if exists (button "OK" of window 1) then
                  click button "OK" of window 1
                  log "✅ Clicked 'OK' button"
              end if
          on error errMsg
              log "⚠️ Error handling popup: " & errMsg
          end try
      end tell
  end tell
END
  sleep 1
done

echo "✅ Done checking for macOS popup."
