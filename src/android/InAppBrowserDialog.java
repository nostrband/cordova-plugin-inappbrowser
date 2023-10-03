/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package org.apache.cordova.inappbrowser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.graphics.Rect;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import org.apache.cordova.LOG;

/**
 * Created by Oliver on 22/11/2013.
 */
public class InAppBrowserDialog extends Dialog {
    Context context;
    InAppBrowser inAppBrowser = null;
    String tabId = "";

    public InAppBrowserDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;
    }

    public void setInAppBrowser(InAppBrowser browser, String tabId) {
        this.inAppBrowser = browser;
        this.tabId = tabId;
    }

    public void onBackPressed () {
        if (this.inAppBrowser == null) {
            this.dismiss();
        } else {
            // better to go through the in inAppBrowser
            // because it does a clean up
            if (this.inAppBrowser.hardwareBack(tabId) && this.inAppBrowser.canGoBack(tabId)) {
                this.inAppBrowser.goBack(tabId);
            }  else {
                this.inAppBrowser.doneDialog(tabId);
            }
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        this.inAppBrowser.outsideMotion(tabId, event);
        return false;
//        return super.onTouchEvent(event);
    }
}
