/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.mms.transaction;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.SystemProperties;
import android.os.UserHandle;

import com.android.mms.LogTag;

/**
 * This class exists specifically to allow us to require permissions checks on SMS_RECEIVED
 * broadcasts that are not applicable to other kinds of broadcast messages handled by the
 * SmsReceiver base class.
 */
public class PrivilegedSmsReceiver extends SmsReceiver {

    private static boolean sIsBike
            = SystemProperties.getBoolean("ro.hw.vehicle.isbike", false);

    @Override
    public void onReceive(Context context, Intent intent) {
        // Pass the message to the base class implementation, noting that it
        // was permission-checked on the way in.
        Context currentContext = getCurrentContext(context);
        if (currentContext != null && sIsBike) {
            onReceiveWithPrivilege(currentContext, intent, true);
        } else {
            // Fall back to original context if we couldn't get current user context
            onReceiveWithPrivilege(context, intent, true);
        }
    }

    /**
     * Returns the current user context if provided context is not null
     *
     * @param : context to get current context
     *
     * @return The current user context from provided context
     */
    private Context getCurrentContext(Context context) {
        // Show SMS for current user
        if (context != null) {
            try {
                return context.createPackageContextAsUser(context.getPackageName(), 0,
                        UserHandle.CURRENT);
            } catch (PackageManager.NameNotFoundException e) {
                LogTag.error("Failed to create package context for current user:" + e.getMessage());
            } catch (SecurityException e) {
                LogTag.error("Security exception creating package context: " + e.getMessage());
            } catch (RuntimeException e) {
                LogTag.error("Runtime error creating package context: " + e.getMessage());
            }
            return null;
        } else {
            return null;
        }
    }
}
