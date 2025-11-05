-- allow-screen-recording.scpt
-- Handles macOS screen/audio recording permission popups.

set candidateProcesses to {"SystemUIServer", "CoreServicesUIAgent", "SecurityAgent", "WindowServer"}
set allowButtons to {"Allow", "OK", "Open System Settings", "Continue"}
set declineButtons to {"Don't Allow", "Later", "Cancel"}

tell application "System Events"
    set foundProcess to missing value
    set foundWindow to missing value

    -- Try to find a window in one of the candidate processes
    repeat 20 times
        repeat with proc in candidateProcesses
            if exists (process proc) then
                tell process proc
                    if exists (window 1) then
                        set foundProcess to proc
                        set foundWindow to window 1
                        exit repeat
                    end if
                end tell
            end if
        end repeat
        if foundProcess is not missing value then exit repeat
        delay 1
    end repeat

    if foundProcess is missing value then
        return "ℹ️ No popup window found in any candidate process"
    end if

    try
        tell process foundProcess
            set popupWindow to window 1
            repeat with b in allowButtons
                if exists (button b of popupWindow) then
                    click button b of popupWindow
                    delay 1
                    return "✅ Clicked '" & b & "' button in " & foundProcess
                end if
            end repeat

            repeat with b in declineButtons
                if exists (button b of popupWindow) then
                    return "ℹ️ Found decline option ('" & b & "') in " & foundProcess & " but did not click it"
                end if
            end repeat

            return "ℹ️ Popup found in " & foundProcess & " but no recognized buttons to click"
        end tell
    on error errMsg
        return "❌ Error in " & foundProcess & ": " & errMsg
    end try
end tell
