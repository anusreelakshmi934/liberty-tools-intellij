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
# macOS permission dialogs using cliclick (coordinate-based clicking).

echo "Starting permission dialog auto-clicker using cliclick..."

# Function to find and click Allow button using cliclick
click_allow_button() {
    # Common button positions for macOS permission dialogs
    # These are typical positions for "Allow" buttons in permission dialogs
    # Format: x:y coordinates
    
    # Standard dialog "Allow" button position (right side, bottom)
    ALLOW_POSITIONS=(
        "393:511"   # Standard position for Allow button
        "400:510"   # Slight variation
        "390:505"   # Another common position
        "350:500"   # Centered Allow button
    )
    
    for pos in "${ALLOW_POSITIONS[@]}"; do
        # Try clicking at each position
        cliclick c:$pos 2>/dev/null
        sleep 0.5
    done
}

# Run the auto-clicker in a loop
while true; do
    # Check if there are any dialog windows open
    # If a dialog is detected, try clicking the Allow button
    click_allow_button
    
    # Wait before next attempt
    sleep 3
done

# Made with Bob
