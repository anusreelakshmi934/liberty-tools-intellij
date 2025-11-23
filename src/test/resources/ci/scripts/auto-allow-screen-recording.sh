#!/bin/bash

############################################################################
# Copyright (c) 2024 IBM Corporation and others.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
#
# Script to automatically click "Allow" on macOS screen recording permission popup
# This runs in the background and monitors for the popup dialog
############################################################################

set -e

echo "Starting auto-allow screen recording monitor..."

# Create AppleScript to click Allow button
cat > /tmp/auto_allow_screen_recording.scpt << 'APPLESCRIPT'
on run
    repeat
        try
            tell application "System Events"
                -- Look for the permission dialog
                if exists (window 1 of process "UserNotificationCenter") then
                    tell process "UserNotificationCenter"
                        -- Try to click the "Allow" button
                        if exists button "Allow" of window 1 then
                            click button "Allow" of window 1
                            log "Clicked Allow button"
                            delay 2
                        end if
                    end tell
                end if
                
                -- Also check for System Settings dialogs
                if exists (window 1 of process "System Settings") then
                    tell process "System Settings"
                        if exists button "Allow" of window 1 then
                            click button "Allow" of window 1
                            log "Clicked Allow button in System Settings"
                            delay 2
                        end if
                    end tell
                end if
            end tell
        on error errMsg
            -- Silently continue on errors
        end try
        delay 1
    end repeat
end run
APPLESCRIPT

# Run the AppleScript in the background
osascript /tmp/auto_allow_screen_recording.scpt > /tmp/auto_allow_screen_recording.log 2>&1 &
APPLESCRIPT_PID=$!

echo "Auto-allow monitor started with PID: $APPLESCRIPT_PID"
echo $APPLESCRIPT_PID > /tmp/auto_allow_screen_recording.pid

# Save the PID so it can be killed later
echo "To stop the monitor, run: kill $APPLESCRIPT_PID"

# Made with Bob
