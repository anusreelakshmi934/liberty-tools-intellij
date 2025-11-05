tell application "System Events"
    tell process "SecurityAgent"
        if exists (button "Allow" of window 1) then
            click button "Allow" of window 1
        end if
    end tell
end tell