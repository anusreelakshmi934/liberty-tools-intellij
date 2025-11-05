-- allow-screen-recording.scpt
-- Handles macOS permission popups for Screen Recording / Accessibility.
-- Supports buttons: “Allow”, “OK”, “Open System Settings”, “Later”, “Don't Allow”.

tell application "System Events"
    -- Give the popup time to appear
    repeat 15 times
        if exists (window 1 of process "SystemUIServer") then exit repeat
        delay 1
    end repeat

    try
        tell process "SystemUIServer"
            if exists (window 1) then
                set popupWindow to window 1

                -- Define possible button labels
                set allowButtons to {"Allow", "OK", "Open System Settings", "Continue"}
                set declineButtons to {"Don't Allow", "Later", "Cancel"}

                repeat with b in allowButtons
                    if exists (button b of popupWindow) then
                        click button b of popupWindow
                        delay 1
                        return "✅ Clicked '" & b & "' button"
                    end if
                end repeat

                repeat with b in declineButtons
                    if exists (button b of popupWindow) then
                        return "ℹ️ Found decline option ('" & b & "') but did not click it"
                    end if
                end repeat

                return "ℹ️ Popup found but no recognized buttons to click"
            else
                return "ℹ️ No popup window found in SystemUIServer"
            end if
        end tell
    on error errMsg
        return "❌ Error: " & errMsg
    end try
end tell
