#!/bin/bash

############################################################################
# Copyright (c) 2025 IBM Corporation and others.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0 which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# SPDX-License-Identifier: EPL-2.0
############################################################################

# This script runs in the background and automatically clicks "Allow" on
# macOS permission dialogs for screen recording and accessibility.

echo "Starting permission dialog auto-clicker..."

# Function to click Allow button using AppleScript
click_allow_button() {
    osascript <<EOF
tell application "System Events"
    repeat
        try
            -- Look for permission dialog windows
            set dialogWindows to (every window whose name contains "requesting" or name contains "access" or name contains "permission")
            
            repeat with dialogWindow in dialogWindows
                -- Try to find and click the "Allow" button
                tell dialogWindow
                    if exists button "Allow" then
                        click button "Allow"
                        log "Clicked Allow button"
                        delay 1
                    end if
                    
                    -- Also try "OK" button
                    if exists button "OK" then
                        click button "OK"
                        log "Clicked OK button"
                        delay 1
                    end if
                end tell
            end repeat
        on error errMsg
            -- Silently continue on errors
        end try
        
        delay 2
    end repeat
end tell
EOF
}

# Run the auto-clicker in a loop
while true; do
    click_allow_button &
    sleep 5
done

# Made with Bob
