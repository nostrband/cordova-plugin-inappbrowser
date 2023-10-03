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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.provider.Browser;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.Color;
import android.net.http.SslError;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.HttpAuthHandler;
import android.webkit.JavascriptInterface;
import android.webkit.ServiceWorkerClient;
import android.webkit.ServiceWorkerController;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.Config;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaHttpAuthHandler;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginManager;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.StringTokenizer;

@SuppressLint("SetJavaScriptEnabled")
public class InAppBrowser extends CordovaPlugin {

    private static final String NULL = "null";
    protected static final String LOG_TAG = "InAppBrowser";
    private static final String SELF = "_self";
    private static final String SYSTEM = "_system";
    private static final String EXIT_EVENT = "exit";
    private static final String HIDE_EVENT = "hide";
    private static final String MENU_EVENT = "menu";
    private static final String CLICK_EVENT = "click";
    private static final String BLANK_EVENT = "blank";
    private static final String ICON_EVENT = "icon";
    private static final String LOCATION = "location";
    private static final String ZOOM = "zoom";
    private static final String HIDDEN = "hidden";
    private static final String LOAD_START_EVENT = "loadstart";
    private static final String LOAD_STOP_EVENT = "loadstop";
    private static final String LOAD_INIT_EVENT = "loadinit";
    private static final String LOAD_ERROR_EVENT = "loaderror";
    private static final String MESSAGE_EVENT = "message";
    private static final String CLEAR_ALL_CACHE = "clearcache";
    private static final String CLEAR_SESSION_CACHE = "clearsessioncache";
    private static final String HARDWARE_BACK_BUTTON = "hardwareback";
    private static final String MEDIA_PLAYBACK_REQUIRES_USER_ACTION = "mediaPlaybackRequiresUserAction";
    private static final String SHOULD_PAUSE = "shouldPauseOnSuspend";
    private static final Boolean DEFAULT_HARDWARE_BACK = true;
    private static final String USER_WIDE_VIEW_PORT = "useWideViewPort";
    private static final String TOOLBAR_COLOR = "toolbarcolor";
    private static final String CLOSE_BUTTON_CAPTION = "closebuttoncaption";
    private static final String CLOSE_BUTTON_COLOR = "closebuttoncolor";
    private static final String CLOSE_BUTTON_HIDE = "closebuttonhide";
    private static final String MENU_BUTTON = "menubutton";
    private static final String LEFT_TO_RIGHT = "lefttoright";
    private static final String HIDE_NAVIGATION = "hidenavigationbuttons";
    private static final String NAVIGATION_COLOR = "navigationbuttoncolor";
    private static final String HIDE_URL = "hideurlbar";
    private static final String FOOTER = "footer";
    private static final String FOOTER_COLOR = "footercolor";
    private static final String BEFORELOAD = "beforeload";
    private static final String FULLSCREEN = "fullscreen";
    private static final String TOP_OFFSET = "topoffset";
    private static final String BOTTOM_OFFSET = "bottomoffset";
    private static final String DATABASE = "database";
    private static final String BEFOREBLANK = "beforeblank";
    private static final String TRANSPARENT_LOADING = "transparentloading";

    private static final int TOOLBAR_HEIGHT = 48;

    private static final List customizableOptions = Arrays.asList(CLOSE_BUTTON_CAPTION, TOOLBAR_COLOR, NAVIGATION_COLOR, CLOSE_BUTTON_COLOR, FOOTER_COLOR, TOP_OFFSET, BOTTOM_OFFSET, DATABASE);

    final static int FILECHOOSER_REQUESTCODE = 1;

    private class Tab {
        String id = "";
        InAppBrowserDialog dialog;
        WebView inAppWebView;
        EditText edittext;
        CallbackContext callbackContext;
        boolean showLocationBar = true;
        boolean showZoomControls = true;
        boolean openWindowHidden = false;
        boolean clearAllCache = false;
        boolean clearSessionCache = false;
        boolean hardwareBackButton = true;
        boolean mediaPlaybackRequiresUserGesture = false;
        boolean shouldPauseInAppBrowser = false;
        boolean useWideViewPort = true;
        ValueCallback<Uri[]> mUploadCallback;
        String closeButtonCaption = "";
        String closeButtonColor = "";
        boolean closeButtonHide = false;
        boolean menuButton = false;
        boolean leftToRight = false;
        int toolbarColor = android.graphics.Color.LTGRAY;
        boolean hideNavigationButtons = false;
        String navigationButtonColor = "";
        boolean hideUrlBar = false;
        boolean showFooter = false;
        String footerColor = "";
        String beforeload = "";
        boolean fullscreen = true;
        String[] allowedSchemes;
        int topOffset = 0;
        int bottomOffset = 0;
        String database = "";

        boolean beforeblank = false;
        boolean transparentLoading = false;

        InAppBrowserClient currentClient;

        LinearLayout bottom;

        RelativeLayout webViewLayout;
        RelativeLayout.LayoutParams wlp;

        boolean hidden = false;
    }

    private HashMap<String, Tab> tabs = new HashMap<String, Tab>();
    private Tab tab; // current tab
    private boolean isKeyboardShowing = false;

    private boolean switchTab(final String tabId) {
        this.tab = tabs.get(tabId);
        return this.tab != null;
    }

    private void ensureTab(final String tabId) {
        Tab tab = tabs.get(tabId);
        if (tab == null) {
            tab = new Tab();
            tab.id = tabId;
            tabs.put(tabId, tab);
        }
        this.tab = tab;
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          the action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext the callbackContext used when calling back into JavaScript.
     * @return A PluginResult object with a status and message.
     */
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals("open")) {
            ensureTab(args.optString(3));
            final Tab tab = this.tab;
            tab.callbackContext = callbackContext;

            final String url = args.getString(0);
            String t = args.optString(1);
            if (t == null || t.equals("") || t.equals(NULL)) {
                t = SELF;
            }
            final String target = t;
            final HashMap<String, String> features = parseFeature(args.optString(2));

            LOG.d(LOG_TAG, "target = " + target);

            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String result = "";
                    // SELF
                    if (SELF.equals(target)) {
                        LOG.d(LOG_TAG, "in self");
                        /* This code exists for compatibility between 3.x and 4.x versions of Cordova.
                         * Previously the Config class had a static method, isUrlWhitelisted(). That
                         * responsibility has been moved to the plugins, with an aggregating method in
                         * PluginManager.
                         */
                        Boolean shouldAllowNavigation = null;
                        if (url.startsWith("javascript:")) {
                            shouldAllowNavigation = true;
                        }
                        if (shouldAllowNavigation == null) {
                            try {
                                Method iuw = Config.class.getMethod("isUrlWhiteListed", String.class);
                                shouldAllowNavigation = (Boolean) iuw.invoke(null, url);
                            } catch (NoSuchMethodException e) {
                                LOG.d(LOG_TAG, e.getLocalizedMessage());
                            } catch (IllegalAccessException e) {
                                LOG.d(LOG_TAG, e.getLocalizedMessage());
                            } catch (InvocationTargetException e) {
                                LOG.d(LOG_TAG, e.getLocalizedMessage());
                            }
                        }
                        if (shouldAllowNavigation == null) {
                            try {
                                Method gpm = webView.getClass().getMethod("getPluginManager");
                                PluginManager pm = (PluginManager) gpm.invoke(webView);
                                Method san = pm.getClass().getMethod("shouldAllowNavigation", String.class);
                                shouldAllowNavigation = (Boolean) san.invoke(pm, url);
                            } catch (NoSuchMethodException e) {
                                LOG.d(LOG_TAG, e.getLocalizedMessage());
                            } catch (IllegalAccessException e) {
                                LOG.d(LOG_TAG, e.getLocalizedMessage());
                            } catch (InvocationTargetException e) {
                                LOG.d(LOG_TAG, e.getLocalizedMessage());
                            }
                        }
                        // load in webview
                        if (Boolean.TRUE.equals(shouldAllowNavigation)) {
                            LOG.d(LOG_TAG, "loading in webview");
                            webView.loadUrl(url);
                        }
                        //Load the dialer
                        else if (url.startsWith(WebView.SCHEME_TEL)) {
                            try {
                                LOG.d(LOG_TAG, "loading in dialer");
                                Intent intent = new Intent(Intent.ACTION_DIAL);
                                intent.setData(Uri.parse(url));
                                cordova.getActivity().startActivity(intent);
                            } catch (android.content.ActivityNotFoundException e) {
                                LOG.e(LOG_TAG, "Error dialing " + url + ": " + e.toString());
                            }
                        } else if (url.startsWith("geo:")
                                || url.startsWith(WebView.SCHEME_MAILTO)
                                || url.startsWith("market:")
                                || url.startsWith("intent:")
                                || url.startsWith("lightning:")
                                || url.startsWith("nostr:")
                        ) {
                            LOG.d(LOG_TAG, "start activity for " + url);
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(url));
                                cordova.getActivity().startActivity(intent);
                            } catch (android.content.ActivityNotFoundException e) {
                                LOG.e(LOG_TAG, "Error with " + url + ": " + e.toString());
                            }
                        }
                        // load in InAppBrowser
                        else {
                            LOG.d(LOG_TAG, "loading in InAppBrowser");
                            result = showWebPage(tab, url, features);
                        }
                    }
                    // SYSTEM
                    else if (SYSTEM.equals(target)) {
                        LOG.d(LOG_TAG, "in system");
                        result = openExternal(url);
                    }
                    // BLANK - or anything else
                    else {
                        LOG.d(LOG_TAG, "in blank");
                        result = showWebPage(tab, url, features);
                    }

                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                }
            });
        } else if (action.equals("close")) {
            if (!switchTab(args.optString(0))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }
            closeDialog(this.tab);
        } else if (action.equals("screenshot")) {
            if (!switchTab(args.optString(0))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }
            screenshot(this.tab, callbackContext, args);
        } else if (action.equals("reload")) {
            if (!switchTab(args.optString(0))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }
            reload(this.tab);
        } else if (action.equals("stop")) {
            if (!switchTab(args.optString(0))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }
            stop(this.tab);
        } else if (action.equals("loadAfterBeforeload")) {
            if (!switchTab(args.optString(1))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }
            final Tab tab = this.tab;
            if (tab.beforeload == null) {
                LOG.e(LOG_TAG, "unexpected loadAfterBeforeload called without feature beforeload=yes");
            }
            final String url = args.getString(0);
	    LOG.d(LOG_TAG, "load after beforeload "+url);
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                        tab.currentClient.waitForBeforeload = false;
                        tab.inAppWebView.setWebViewClient(tab.currentClient);
                    } else {
                        ((InAppBrowserClient) tab.inAppWebView.getWebViewClient()).waitForBeforeload = false;
                    }
                    tab.inAppWebView.loadUrl(url);
                }
            });
        } else if (action.equals("injectScriptCode")) {
            if (!switchTab(args.optString(2))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }

            String jsWrapper = null;
            if (args.getBoolean(1)) {
                // jsWrapper = String.format("(function(){prompt(JSON.stringify([eval(%%s)]), 'gap-iab://%s')})()", callbackContext.getCallbackId());
                jsWrapper = String.format("(function(){prompt(JSON.stringify([(function (){ %%s;\n })()]), 'gap-iab://%s')})()", callbackContext.getCallbackId());
            }
            injectDeferredObject(args.getString(0), jsWrapper);
        } else if (action.equals("injectScriptFile")) {
            if (!switchTab(args.optString(2))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }

            String jsWrapper;
            if (args.getBoolean(1)) {
                jsWrapper = String.format("(function(d) { var c = d.createElement('script'); c.src = %%s; c.onload = function() { prompt('', 'gap-iab://%s'); }; d.body.appendChild(c); })(document)", callbackContext.getCallbackId());
            } else {
                jsWrapper = "(function(d) { var c = d.createElement('script'); c.src = %s; d.body.appendChild(c); })(document)";
            }
            injectDeferredObject(args.getString(0), jsWrapper);
        } else if (action.equals("injectStyleCode")) {
            if (!switchTab(args.optString(2))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }

            String jsWrapper;
            if (args.getBoolean(1)) {
                jsWrapper = String.format("(function(d) { var c = d.createElement('style'); c.innerHTML = %%s; d.body.appendChild(c); prompt('', 'gap-iab://%s');})(document)", callbackContext.getCallbackId());
            } else {
                jsWrapper = "(function(d) { var c = d.createElement('style'); c.innerHTML = %s; d.body.appendChild(c); })(document)";
            }
            injectDeferredObject(args.getString(0), jsWrapper);
        } else if (action.equals("injectStyleFile")) {
            if (!switchTab(args.optString(2))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }

            String jsWrapper;
            if (args.getBoolean(1)) {
                jsWrapper = String.format("(function(d) { var c = d.createElement('link'); c.rel='stylesheet'; c.type='text/css'; c.href = %%s; d.head.appendChild(c); prompt('', 'gap-iab://%s');})(document)", callbackContext.getCallbackId());
            } else {
                jsWrapper = "(function(d) { var c = d.createElement('link'); c.rel='stylesheet'; c.type='text/css'; c.href = %s; d.head.appendChild(c); })(document)";
            }
            injectDeferredObject(args.getString(0), jsWrapper);
        } else if (action.equals("show")) {
            if (!switchTab(args.optString(0))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }
            final Tab tab = this.tab;
            LOG.d(LOG_TAG, "show tab "+tab.id);
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tab.dialog != null && !cordova.getActivity().isFinishing()) {
                        if (tab.bottom != null)
                            tab.bottom.setVisibility(View.VISIBLE);
                        tab.dialog.show();
                    }
                }
            });
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            tab.callbackContext.sendPluginResult(pluginResult);
        } else if (action.equals("hide")) {
            if (!switchTab(args.optString(0))) {
                LOG.e(LOG_TAG, "unknown tab " + args.optString(1));
                return false;
            }
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tab.dialog != null && !cordova.getActivity().isFinishing()) {
                        tab.dialog.hide();
                        tab.hidden = true;
                    }
                }
            });
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
            pluginResult.setKeepCallback(true);
            tab.callbackContext.sendPluginResult(pluginResult);
        } else {
            return false;
        }
        return true;
    }

    /**
     * Called when the view navigates.
     */
    @Override
    public void onReset() {
        for (Tab tab : tabs.values()) {
            closeDialog(tab);
        }
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     */
    @Override
    public void onPause(boolean multitasking) {
        LOG.d(LOG_TAG, "onPause tabs "+tabs.size());
        for (Tab tab : tabs.values()) {
            if (tab.shouldPauseInAppBrowser) {
                tab.inAppWebView.onPause();
            }
        }
    }

    /**
     * Called when the activity will start interacting with the user.
     */
    @Override
    public void onResume(boolean multitasking) {
        LOG.d(LOG_TAG, "onResume tabs "+tabs.size());
        for (Tab tab : tabs.values()) {
            if (tab.shouldPauseInAppBrowser) {
                tab.inAppWebView.onResume();
            }
        }
    }

    /**
     * Called by AccelBroker when listener is to be shut down.
     * Stop listener.
     */
    public void onDestroy() {
        for (Tab tab : tabs.values()) {
            closeDialog(tab);
        }
    }

    /**
     * Inject an object (script or style) into the InAppBrowser WebView.
     * <p>
     * This is a helper method for the inject{Script|Style}{Code|File} API calls, which
     * provides a consistent method for injecting JavaScript code into the document.
     * <p>
     * If a wrapper string is supplied, then the source string will be JSON-encoded (adding
     * quotes) and wrapped using string formatting. (The wrapper string should have a single
     * '%s' marker)
     *
     * @param source    The source object (filename or script/style text) to inject into
     *                  the document.
     * @param jsWrapper A JavaScript string to wrap the source string in, so that the object
     *                  is properly injected, or null if the source string is JavaScript text
     *                  which should be executed directly.
     */
    private void injectDeferredObject(String source, String jsWrapper) {
        injectDeferredObject(source, jsWrapper, this.tab);
    }

    private void injectDeferredObject(String source, String jsWrapper, Tab tab) {
        if (tab.inAppWebView != null) {
            String scriptToInject;
            if (jsWrapper != null) {
                //org.json.JSONArray jsonEsc = new org.json.JSONArray();
                //jsonEsc.put(source);
                //String jsonRepr = jsonEsc.toString();
                //String jsonSourceString = jsonRepr.substring(1, jsonRepr.length()-1);
                scriptToInject = String.format(jsWrapper, source); // jsonSourceString);
            } else {
                scriptToInject = source;
            }
            final String finalScriptToInject = scriptToInject;
            this.cordova.getActivity().runOnUiThread(new Runnable() {
                @SuppressLint("NewApi")
                @Override
                public void run() {
                    tab.inAppWebView.evaluateJavascript(finalScriptToInject, null);
                }
            });
        } else {
            LOG.d(LOG_TAG, "Can't inject code into the system browser");
        }
    }

    /**
     * Put the list of features into a hash map
     *
     * @param optString
     * @return
     */
    private HashMap<String, String> parseFeature(String optString) {
        if (optString.equals(NULL)) {
            return null;
        } else {
            HashMap<String, String> map = new HashMap<String, String>();
            StringTokenizer features = new StringTokenizer(optString, ",");
            StringTokenizer option;
            while (features.hasMoreElements()) {
                option = new StringTokenizer(features.nextToken(), "=");
                if (option.hasMoreElements()) {
                    String key = option.nextToken();
                    String value = option.nextToken();
                    if (!customizableOptions.contains(key)) {
                        value = value.equals("yes") || value.equals("no") ? value : "yes";
                    }
                    map.put(key, value);
                }
            }
            return map;
        }
    }

    /**
     * Display a new browser with the specified URL.
     *
     * @param url the url to load.
     * @return "" if ok, or error message.
     */
    public String openExternal(String url) {
        try {
            Intent intent = null;
            intent = new Intent(Intent.ACTION_VIEW);
            // Omitting the MIME type for file: URLs causes "No Activity found to handle Intent".
            // Adding the MIME type to http: URLs causes them to not be handled by the downloader.
            Uri uri = Uri.parse(url);
            if ("file".equals(uri.getScheme())) {
                intent.setDataAndType(uri, webView.getResourceApi().getMimeType(uri));
            } else {
                intent.setData(uri);
            }
            intent.putExtra(Browser.EXTRA_APPLICATION_ID, cordova.getActivity().getPackageName());
            // CB-10795: Avoid circular loops by preventing it from opening in the current app
            this.openExternalExcludeCurrentApp(intent);
            return "";
            // not catching FileUriExposedException explicitly because buildtools<24 doesn't know about it
        } catch (java.lang.RuntimeException e) {
            LOG.d(LOG_TAG, "InAppBrowser: Error loading url " + url + ":" + e.toString());
            return e.toString();
        }
    }

    /**
     * Opens the intent, providing a chooser that excludes the current app to avoid
     * circular loops.
     */
    private void openExternalExcludeCurrentApp(Intent intent) {
        String currentPackage = cordova.getActivity().getPackageName();
        boolean hasCurrentPackage = false;

        PackageManager pm = cordova.getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
        ArrayList<Intent> targetIntents = new ArrayList<Intent>();

        for (ResolveInfo ri : activities) {
            if (!currentPackage.equals(ri.activityInfo.packageName)) {
                Intent targetIntent = (Intent) intent.clone();
                targetIntent.setPackage(ri.activityInfo.packageName);
                targetIntents.add(targetIntent);
            } else {
                hasCurrentPackage = true;
            }
        }

        // If the current app package isn't a target for this URL, then use
        // the normal launch behavior
        if (hasCurrentPackage == false || targetIntents.size() == 0) {
            this.cordova.getActivity().startActivity(intent);
        }
        // If there's only one possible intent, launch it directly
        else if (targetIntents.size() == 1) {
            this.cordova.getActivity().startActivity(targetIntents.get(0));
        }
        // Otherwise, show a custom chooser without the current app listed
        else if (targetIntents.size() > 0) {
            Intent chooser = Intent.createChooser(targetIntents.remove(targetIntents.size() - 1), null);
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetIntents.toArray(new Parcelable[]{}));
            this.cordova.getActivity().startActivity(chooser);
        }
    }

    public boolean isBeforeBlank(String tabId) {
        final Tab tab = this.tabs.get(tabId);
        if (tab != null)
            return tab.beforeblank;
        return false;
    }

    public void onBeforeBlank(String tabId, String url) {
        final Tab tab = this.tabs.get(tabId);
        if (tab != null) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("type", BLANK_EVENT);
                obj.put("url", url);
                sendUpdate(tab, obj, true);
            } catch (JSONException ex) {
                LOG.d(LOG_TAG, "Bad url" + url);
            }
        }
    }

    public void outsideMotion(String tabId, MotionEvent event) {
        final Tab tab = this.tabs.get(tabId);
        if (tab != null) {
            final int parentX = (int)event.getX();
            final int parentY = (int)event.getY() - getWindowOffset();
            LOG.d(LOG_TAG, "click x " + parentX + " y " + parentY);
            if (parentX >= 0 && parentY >= 0 && event.getAction() == MotionEvent.ACTION_UP
                && (parentY < tab.topOffset || parentY > (tab.topOffset + tab.webViewLayout.getHeight()))) {
                LOG.d(LOG_TAG, "sending click x " + parentX + " y " + parentY);
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", CLICK_EVENT);
                    obj.put("x", parentX);
                    obj.put("y", parentY);
                    sendUpdate(tab, obj, true);
                } catch (JSONException ex) {
                    LOG.d(LOG_TAG, "Should never happen");
                }
            }
        }
    }

    public void onIcon(String tabId, String dataUrl) {
        final Tab tab = this.tabs.get(tabId);
        if (tab != null) {
	    try {
		JSONObject obj = new JSONObject();
		obj.put("type", ICON_EVENT);
		obj.put("icon", dataUrl);
		sendUpdate(tab, obj, true);
	    } catch (JSONException ex) {
		LOG.d(LOG_TAG, "Should never happen");
	    }
	}
    }

    /**
     * Closes the dialog
     */
    public void doneDialog(String tabId) {
        final Tab tab = this.tabs.get(tabId);
        if (tab != null) {
            if (tab.closeButtonHide) {
                hideDialog(tab);
            } else {
                closeDialog(tab);
            }
        }
    }

    private void screenshot(Tab tab, CallbackContext callbackContext, CordovaArgs args) throws JSONException {

        final float scale = Float.valueOf(args.getString(1));
        final float heightAspect = Float.valueOf(args.getString(2));

        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tab == null || tab.inAppWebView == null) {
                    return;
                }

                final WebView wv = tab.inAppWebView;
                final float s = scale > 0.0f ? scale : 1.0f;
                final int width = (int)(s * Math.min(wv.getWidth(), wv.getHeight()));
                final int height = (int)(width * (heightAspect > 0 ? heightAspect : 1.0));
                LOG.d(LOG_TAG, "screenshot width "+width+" height "+height+" scale "+s);
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.scale(s, s);
                wv.draw(canvas);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String dataUrl = "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);

                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, dataUrl);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }
        });
    }

    private void closeDialog(Tab tab) {
        final InAppBrowser self = this;
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final WebView childView = tab.inAppWebView;
                // The JS protects against multiple calls, so this should happen only when
                // closeDialog() is called by other native code.
                if (tab == null || childView == null) {
                    return;
                }

                childView.setWebViewClient(new WebViewClient() {
                    // NB: wait for about:blank before dismissing
                    public void onPageFinished(WebView view, String url) {
                        // LOG.d(LOG_TAG, "done page onPageFinished " + url);
                        if (tab.dialog != null && !cordova.getActivity().isFinishing()) {
                            tab.dialog.dismiss();
                            tab.dialog = null;
                            if (self.tab == tab)
                                self.tab = null;
                            tabs.remove(tab.id);
                        }
                    }
                });
                // NB: From SDK 19: "If you call methods on WebView from any thread
                // other than your app's UI thread, it can cause unexpected results."
                // http://developer.android.com/guide/webapps/migrating.html#Threads
                childView.loadUrl("about:blank");

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("type", EXIT_EVENT);
                    sendUpdate(tab, obj, false);
                } catch (JSONException ex) {
                    LOG.d(LOG_TAG, "Should never happen");
                }
            }
        });
    }

    /**
     * Hides the dialog
     */
    public void hideDialog(final Tab tab) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tab.dialog != null && !cordova.getActivity().isFinishing()) {
                    tab.dialog.hide();
                    tab.hidden = true;

                    try {
                        JSONObject obj = new JSONObject();
                        obj.put("type", HIDE_EVENT);
                        sendUpdate(tab, obj, true);
                    } catch (JSONException ex) {
                        LOG.d(LOG_TAG, "Should never happen");
                    }
                }
            }
        });
    }

    /**
     * Checks to see if it is possible to go back one page in history, then does so.
     */
    public void goBack(String tabId) {
        final Tab tab = this.tabs.get(tabId);
        this.goBack(tab);
    }

    /**
     * Checks to see if it is possible to go back one page in history, then does so.
     */
    private void goBack(Tab tab) {
        if (tab != null && tab.inAppWebView.canGoBack()) {
            tab.inAppWebView.goBack();
        }
    }

    /**
     * Can the web browser go back?
     *
     * @return boolean
     */
    public boolean canGoBack(String tabId) {
        final Tab tab = this.tabs.get(tabId);
        if (tab != null) {
            return tab.inAppWebView.canGoBack();
        } else {
            return false;
        }
    }

    /**
     * Has the user set the hardware back button to go back
     *
     * @return boolean
     */
    public boolean hardwareBack(String tabId) {
        final Tab tab = this.tabs.get(tabId);
        if (tab != null) {
            return tab.hardwareBackButton;
        } else {
            return false;
        }
    }

    /**
     * Checks to see if it is possible to go forward one page in history, then does so.
     */
    private void goForward(String tabId) {
        final Tab tab = this.tabs.get(tabId);
        if (tab != null && tab.inAppWebView.canGoForward()) {
            tab.inAppWebView.goForward();
        }
    }

    /**
     * Navigate to the new page
     *
     * @param url to load
     */
    private void navigate(Tab tab, String url) {
        InputMethodManager imm = (InputMethodManager) this.cordova.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(tab.edittext.getWindowToken(), 0);

        if (!url.startsWith("http") && !url.startsWith("file:")) {
            tab.inAppWebView.loadUrl("http://" + url);
        } else {
            tab.inAppWebView.loadUrl(url);
        }
        tab.inAppWebView.requestFocus();
    }

    private void reload(Tab tab) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tab.inAppWebView.reload();
            }
        });
    }

    private void stop(Tab tab) {
        this.cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tab.inAppWebView.stopLoading();
            }
        });
    }

    /**
     * Should we show the location bar?
     *
     * @return boolean
     */
    //private boolean getShowLocationBar() {
    //    return this.showLocationBar;
    //}
    private InAppBrowser getInAppBrowser() {
        return this;
    }

    private int getWindowOffset() {
        int offset = 0;
        int resourceId = cordova.getActivity().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            offset = cordova.getActivity().getResources().getDimensionPixelSize(resourceId);
        }

        return offset;
    }

    private void hideBottom(Tab tab) {
        if (tab.bottom != null) {
            tab.bottom.setVisibility(View.GONE);
            LOG.d(LOG_TAG, "hide bottom");
        }
    }

    /**
     * Display a new browser with the specified URL.
     *
     * @param url the url to load.
     * @param features jsonObject
     */
    public String showWebPage(final Tab tab, final String url, HashMap<String, String> features) {
        // Determine if we should hide the location bar.
        tab.showLocationBar = true;
        tab.showZoomControls = true;
        tab.openWindowHidden = false;
        tab.mediaPlaybackRequiresUserGesture = false;

        if (features != null) {
            String show = features.get(LOCATION);
            if (show != null) {
                tab.showLocationBar = show.equals("yes") ? true : false;
            }
            if(tab.showLocationBar) {
                String hideNavigation = features.get(HIDE_NAVIGATION);
                String hideUrl = features.get(HIDE_URL);
                if(hideNavigation != null) tab.hideNavigationButtons = hideNavigation.equals("yes") ? true : false;
                if(hideUrl != null) tab.hideUrlBar = hideUrl.equals("yes") ? true : false;
            }
            String zoom = features.get(ZOOM);
            if (zoom != null) {
                tab.showZoomControls = zoom.equals("yes") ? true : false;
            }
            String hidden = features.get(HIDDEN);
            if (hidden != null) {
                tab.openWindowHidden = hidden.equals("yes") ? true : false;
            }
            String hardwareBack = features.get(HARDWARE_BACK_BUTTON);
            if (hardwareBack != null) {
                tab.hardwareBackButton = hardwareBack.equals("yes") ? true : false;
            } else {
                tab.hardwareBackButton = DEFAULT_HARDWARE_BACK;
            }
            String mediaPlayback = features.get(MEDIA_PLAYBACK_REQUIRES_USER_ACTION);
            if (mediaPlayback != null) {
                tab.mediaPlaybackRequiresUserGesture = mediaPlayback.equals("yes") ? true : false;
            }
            String cache = features.get(CLEAR_ALL_CACHE);
            if (cache != null) {
                tab.clearAllCache = cache.equals("yes") ? true : false;
            } else {
                cache = features.get(CLEAR_SESSION_CACHE);
                if (cache != null) {
                    tab.clearSessionCache = cache.equals("yes") ? true : false;
                }
            }
            String shouldPause = features.get(SHOULD_PAUSE);
            if (shouldPause != null) {
                tab.shouldPauseInAppBrowser = shouldPause.equals("yes") ? true : false;
            }
            String wideViewPort = features.get(USER_WIDE_VIEW_PORT);
            if (wideViewPort != null ) {
                tab.useWideViewPort = wideViewPort.equals("yes") ? true : false;
            }
            String closeButtonCaptionSet = features.get(CLOSE_BUTTON_CAPTION);
            if (closeButtonCaptionSet != null) {
                tab.closeButtonCaption = closeButtonCaptionSet;
            }
            String closeButtonColorSet = features.get(CLOSE_BUTTON_COLOR);
            if (closeButtonColorSet != null) {
                tab.closeButtonColor = closeButtonColorSet;
            }
            String closeButtonHideSet = features.get(CLOSE_BUTTON_HIDE);
            tab.closeButtonHide = closeButtonHideSet != null && closeButtonHideSet.equals("yes");
            String menuButtonSet = features.get(MENU_BUTTON);
            tab.menuButton = menuButtonSet != null && menuButtonSet.equals("yes");
            String leftToRightSet = features.get(LEFT_TO_RIGHT);
            tab.leftToRight = leftToRightSet != null && leftToRightSet.equals("yes");
            String toolbarColorSet = features.get(TOOLBAR_COLOR);
            if (toolbarColorSet != null) {
                tab.toolbarColor = android.graphics.Color.parseColor(toolbarColorSet);
            }
            String navigationButtonColorSet = features.get(NAVIGATION_COLOR);
            if (navigationButtonColorSet != null) {
                tab.navigationButtonColor = navigationButtonColorSet;
            }
            String showFooterSet = features.get(FOOTER);
            if (showFooterSet != null) {
                tab.showFooter = showFooterSet.equals("yes") ? true : false;
            }
            String footerColorSet = features.get(FOOTER_COLOR);
            if (footerColorSet != null) {
                tab.footerColor = footerColorSet;
            }
            if (features.get(BEFORELOAD) != null) {
                tab.beforeload = features.get(BEFORELOAD);
            }
            String fullscreenSet = features.get(FULLSCREEN);
            if (fullscreenSet != null) {
                tab.fullscreen = fullscreenSet.equals("yes") ? true : false;
            }
            String topOffsetSet = features.get(TOP_OFFSET);
            if (topOffsetSet != null) {
                try {
                    tab.topOffset = Integer.parseInt(topOffsetSet);
                    // LOG.d(LOG_TAG, "tab.topOffset " + String.valueOf(tab.topOffset));
                }
                catch (NumberFormatException ex){
                    LOG.e(LOG_TAG, "topoffset invalid " + topOffsetSet);
                }
            }
            String bottomOffsetSet = features.get(BOTTOM_OFFSET);
            if (bottomOffsetSet != null) {
                try {
                    tab.bottomOffset = Integer.parseInt(bottomOffsetSet);
                    // LOG.d(LOG_TAG, "tab.topOffset " + String.valueOf(tab.topOffset));
                }
                catch (NumberFormatException ex){
                    LOG.e(LOG_TAG, "bottomoffset invalid " + bottomOffsetSet);
                }
            }
            String databaseSet = features.get(DATABASE);
            if (databaseSet != null) {
                if (databaseSet.contains("/")
                        || databaseSet.contains(".")
                        || databaseSet.length() > 96) {
                    LOG.e(LOG_TAG, "database invalid " + databaseSet);
                }
                else {
                    tab.database = databaseSet;
                }
            }
            String beforeblankSet = features.get(BEFOREBLANK);
            if (beforeblankSet != null) {
                tab.beforeblank = beforeblankSet.equals("yes") ? true : false;
            }
            String transparentLoadingSet = features.get(TRANSPARENT_LOADING);
            if (transparentLoadingSet != null) {
                tab.transparentLoading = transparentLoadingSet.equals("yes") ? true : false;
            }
        }

        final CordovaWebView thatWebView = this.webView;

        // Create dialog in new thread
        Runnable runnable = new Runnable() {
            /**
             * Convert our DIP units to Pixels
             *
             * @return int
             */
            private int dpToPixels(int dipValue) {
                int value = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP,
                        (float) dipValue,
                        cordova.getActivity().getResources().getDisplayMetrics()
                );

                return value;
            }

            private int pxToDp(int pxValue) {
                int value = (int) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_PX,
                        (float) pxValue,
                        cordova.getActivity().getResources().getDisplayMetrics()
                );

                return value;
            }

            private View createCloseButton(int id) {
                View _close;
                Resources activityRes = cordova.getActivity().getResources();

                if (tab.closeButtonCaption != "") {
                    // Use TextView for text
                    TextView close = new TextView(cordova.getActivity());
                    close.setText(tab.closeButtonCaption);
                    close.setTextSize(20);
                    if (tab.closeButtonColor != "") close.setTextColor(android.graphics.Color.parseColor(tab.closeButtonColor));
                    close.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    close.setPadding(this.dpToPixels(10), 0, this.dpToPixels(10), 0);
                    _close = close;
                }
                else {
                    ImageButton close = new ImageButton(cordova.getActivity());
                    String icon = tab.closeButtonHide ? "ic_action_keyboard_arrow_down" : "ic_action_remove";
                    int closeResId = activityRes.getIdentifier(icon, "drawable", cordova.getActivity().getPackageName());
                    Drawable closeIcon = activityRes.getDrawable(closeResId);
                    if (tab.closeButtonColor != "") close.setColorFilter(android.graphics.Color.parseColor(tab.closeButtonColor));
                    close.setImageDrawable(closeIcon);
                    close.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    close.setPadding(this.dpToPixels(5), this.dpToPixels(10), this.dpToPixels(5), this.dpToPixels(10));
                    // close.setPadding(this.dpToPixels(0), 0, this.dpToPixels(0), 0);
                    close.getAdjustViewBounds();

                    _close = close;
                }

                RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                if (tab.leftToRight) closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                else closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                _close.setLayoutParams(closeLayoutParams);
                _close.setBackground(null);

                _close.setContentDescription("Close Button");
                _close.setId(Integer.valueOf(id));
                _close.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (tab.closeButtonHide) {
                            hideDialog(tab);
                        }
                        else {
                            closeDialog(tab);
                        }
                    }
                });

                return _close;
            }

            @SuppressLint("NewApi")
            public void run() {

                // CB-6702 InAppBrowser hangs when opening more than one instance
                // if (tab.dialog != null) {
                //     dialog.dismiss();
                // };

                // Let's create the main dialog
                tab.dialog = new InAppBrowserDialog(cordova.getActivity(), android.R.style.Theme_NoTitleBar);
                tab.dialog.getWindow().getAttributes().windowAnimations = android.R.style.Animation_Dialog;
                tab.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                if (tab.fullscreen) {
                    tab.dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                }
                else {
                    tab.dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    tab.dialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }

                if (tab.bottomOffset > 0)
                    tab.dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_PAN);

                tab.dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                tab.dialog.setCancelable(true);
                tab.dialog.setInAppBrowser(getInAppBrowser(), tab.id);

                // Main container layout
                RelativeLayout main = new RelativeLayout(cordova.getActivity());

                // Toolbar layout
                RelativeLayout toolbar = new RelativeLayout(cordova.getActivity());
                //Please, no more black!
                toolbar.setBackgroundColor(tab.toolbarColor);
                toolbar.setPadding(this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2), this.dpToPixels(2));
                if (tab.leftToRight) {
                    toolbar.setHorizontalGravity(Gravity.LEFT);
                } else {
                    toolbar.setHorizontalGravity(Gravity.RIGHT);
                }
                toolbar.setVerticalGravity(Gravity.TOP);

                // Action Button Container layout
                RelativeLayout actionButtonContainer = new RelativeLayout(cordova.getActivity());
                RelativeLayout.LayoutParams actionButtonLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                if (tab.leftToRight) actionButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                else actionButtonLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                actionButtonContainer.setLayoutParams(actionButtonLayoutParams);
                actionButtonContainer.setHorizontalGravity(Gravity.LEFT);
                actionButtonContainer.setVerticalGravity(Gravity.CENTER_VERTICAL);
                actionButtonContainer.setId(tab.leftToRight ? Integer.valueOf(5) : Integer.valueOf(1));

                // Back button
                ImageButton back = new ImageButton(cordova.getActivity());
                RelativeLayout.LayoutParams backLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                backLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
                back.setLayoutParams(backLayoutParams);
                back.setContentDescription("Back Button");
                back.setId(Integer.valueOf(2));
                Resources activityRes = cordova.getActivity().getResources();
                int backResId = activityRes.getIdentifier("ic_action_previous_item", "drawable", cordova.getActivity().getPackageName());
                Drawable backIcon = activityRes.getDrawable(backResId);
                if (tab.navigationButtonColor != "") back.setColorFilter(android.graphics.Color.parseColor(tab.navigationButtonColor));
                back.setBackground(null);
                back.setImageDrawable(backIcon);
                back.setScaleType(ImageView.ScaleType.FIT_CENTER);
                back.setPadding(0, this.dpToPixels(10), 0, this.dpToPixels(10));
                back.getAdjustViewBounds();

                back.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        goBack(tab.id);
                    }
                });

                // Forward button
                ImageButton forward = new ImageButton(cordova.getActivity());
                RelativeLayout.LayoutParams forwardLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                forwardLayoutParams.addRule(RelativeLayout.RIGHT_OF, 2);
                forward.setLayoutParams(forwardLayoutParams);
                forward.setContentDescription("Forward Button");
                forward.setId(Integer.valueOf(3));
                int fwdResId = activityRes.getIdentifier("ic_action_next_item", "drawable", cordova.getActivity().getPackageName());
                Drawable fwdIcon = activityRes.getDrawable(fwdResId);
                if (tab.navigationButtonColor != "") forward.setColorFilter(android.graphics.Color.parseColor(tab.navigationButtonColor));
                forward.setBackground(null);
                forward.setImageDrawable(fwdIcon);
                forward.setScaleType(ImageView.ScaleType.FIT_CENTER);
                forward.setPadding(0, this.dpToPixels(10), 0, this.dpToPixels(10));
                forward.getAdjustViewBounds();

                forward.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        goForward(tab.id);
                    }
                });

                int closeButtonId = tab.leftToRight ? 1 : 5;
                int menuButtonId = tab.menuButton ? 6 : 5;

                // Edit Text Box
                tab.edittext = new EditText(cordova.getActivity());
                RelativeLayout.LayoutParams textLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                textLayoutParams.addRule(RelativeLayout.RIGHT_OF, 1);
                textLayoutParams.addRule(RelativeLayout.LEFT_OF, menuButtonId);
                tab.edittext.setLayoutParams(textLayoutParams);
                tab.edittext.setId(Integer.valueOf(4));
                tab.edittext.setSingleLine(true);
                tab.edittext.setText(url);
                tab.edittext.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                tab.edittext.setImeOptions(EditorInfo.IME_ACTION_GO);
                tab.edittext.setInputType(InputType.TYPE_NULL); // Will not except input... Makes the text NON-EDITABLE
                tab.edittext.setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        // If the event is a key-down event on the "enter" button
                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            navigate(tab, tab.edittext.getText().toString());
                            return true;
                        }
                        return false;
                    }
                });

                // Header Close/Done button
                View close = createCloseButton(closeButtonId);
                toolbar.addView(close);

                // Menu button
                if (tab.menuButton) {
                    // Menu button
                    ImageButton menu = new ImageButton(cordova.getActivity());
                    RelativeLayout.LayoutParams menuLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
                    menuLayoutParams.addRule(RelativeLayout.LEFT_OF, closeButtonId);
                    menu.setLayoutParams(menuLayoutParams);
                    menu.setContentDescription("Menu Button");
                    menu.setId(Integer.valueOf(menuButtonId));
                    int menuResId = activityRes.getIdentifier("ic_action_menu", "drawable", cordova.getActivity().getPackageName());
                    Drawable menuIcon = activityRes.getDrawable(menuResId);
                    if (tab.navigationButtonColor != "") menu.setColorFilter(android.graphics.Color.parseColor(tab.navigationButtonColor));
                    menu.setBackground(null);
                    menu.setImageDrawable(menuIcon);
                    menu.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    menu.setPadding(0, this.dpToPixels(10), 0, this.dpToPixels(10));
                    menu.getAdjustViewBounds();

                    menu.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("type", MENU_EVENT);
                                sendUpdate(tab, obj, true);
                            } catch (JSONException ex) {
                                LOG.d(LOG_TAG, "Should never happen");
                            }
                        }
                    });

                    toolbar.addView(menu);
                }

                // Footer
                RelativeLayout footer = new RelativeLayout(cordova.getActivity());
                int _footerColor;
                if(tab.footerColor != "") {
                    _footerColor = Color.parseColor(tab.footerColor);
                } else {
                    _footerColor = android.graphics.Color.LTGRAY;
                }
                footer.setBackgroundColor(_footerColor);
                RelativeLayout.LayoutParams footerLayout = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, this.dpToPixels(TOOLBAR_HEIGHT));
                footerLayout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                footer.setLayoutParams(footerLayout);
                if (tab.closeButtonCaption != "") footer.setPadding(this.dpToPixels(8), this.dpToPixels(8), this.dpToPixels(8), this.dpToPixels(8));
                footer.setHorizontalGravity(Gravity.LEFT);
                footer.setVerticalGravity(Gravity.BOTTOM);

                View footerClose = createCloseButton(7);
                footer.addView(footerClose);


                // WebView
                tab.inAppWebView = new WebView(cordova.getActivity());
                if (tab.transparentLoading)
                    tab.inAppWebView.setAlpha(0f);
                tab.inAppWebView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                tab.inAppWebView.setId(Integer.valueOf(8));
                // File Chooser Implemented ChromeClient
                tab.inAppWebView.setWebChromeClient(new InAppChromeClient(thatWebView, getInAppBrowser(), tab.id) {
                    public boolean onShowFileChooser (WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams)
                    {
                        LOG.d(LOG_TAG, "File Chooser 5.0+");
                        // If callback exists, finish it.
                        if(tab.mUploadCallback != null) {
                            tab.mUploadCallback.onReceiveValue(null);
                        }
                        tab.mUploadCallback = filePathCallback;

                        // Create File Chooser Intent
                        Intent content = new Intent(Intent.ACTION_GET_CONTENT);
                        content.addCategory(Intent.CATEGORY_OPENABLE);
                        content.setType("*/*");

                        // Run cordova startActivityForResult
                        cordova.startActivityForResult(InAppBrowser.this, Intent.createChooser(content, "Select File"), FILECHOOSER_REQUESTCODE);
                        return true;
                    }
                });
                tab.currentClient = new InAppBrowserClient(tab, thatWebView, tab.edittext, tab.beforeload);
                tab.inAppWebView.setWebViewClient(tab.currentClient);
                WebSettings settings = tab.inAppWebView.getSettings();
                settings.setJavaScriptEnabled(true);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                settings.setBuiltInZoomControls(tab.showZoomControls);
                settings.setPluginState(android.webkit.WebSettings.PluginState.ON);
                settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

                ServiceWorkerController swController = ServiceWorkerController.getInstance();
                swController.setServiceWorkerClient(new ServiceWorkerClient() {
                    @Override
                    public WebResourceResponse shouldInterceptRequest(WebResourceRequest request) {
                        LOG.e(LOG_TAG, "in service worker. isMainFrame:"+request.isForMainFrame() +": " + request.getUrl());
                        return null;
                    }
                });
                swController.getServiceWorkerWebSettings().setAllowContentAccess(true);
                swController.getServiceWorkerWebSettings().setBlockNetworkLoads(false);

                // Add postMessage interface
                class JsObject {
                    @JavascriptInterface
                    public void postMessage(String data) {
                        try {
                            JSONObject obj = new JSONObject();
                            obj.put("type", MESSAGE_EVENT);
                            obj.put("data", new JSONObject(data));
                            sendUpdate(tab, obj, true);
                        } catch (JSONException ex) {
                            LOG.e(LOG_TAG, "data object passed to postMessage has caused a JSON error.");
                        }
                    }
                }

                settings.setMediaPlaybackRequiresUserGesture(tab.mediaPlaybackRequiresUserGesture);
                tab.inAppWebView.addJavascriptInterface(new JsObject(), "cordova_iab");

                // Add webln
//                class WebLNObject {
//                    @JavascriptInterface
//                    public void enable() {
//                        Log.d(LOG_TAG, "webln enable called early");
//                    }
//                }
//                tab.inAppWebView.addJavascriptInterface(new WebLNObject(), "webln");

                String overrideUserAgent = preferences.getString("OverrideUserAgent", null);
                String appendUserAgent = preferences.getString("AppendUserAgent", null);

                if (overrideUserAgent != null) {
                    settings.setUserAgentString(overrideUserAgent);
                }
                if (appendUserAgent != null) {
                    settings.setUserAgentString(settings.getUserAgentString() + " " + appendUserAgent);
                }

                //Toggle whether this is enabled or not!
                Bundle appSettings = cordova.getActivity().getIntent().getExtras();
                boolean enableDatabase = appSettings == null ? true : appSettings.getBoolean("InAppBrowserStorageEnabled", true);
                if (enableDatabase) {
                    String dir = "inAppBrowserDB";
                    if (tab.database != "") {
                        dir += "_" + tab.database;
                    }
                    LOG.d(LOG_TAG, "webview database path " + dir);
                    String databasePath = cordova.getActivity().getApplicationContext().getDir(dir, Context.MODE_PRIVATE).getPath();
                    settings.setDatabasePath(databasePath);
                    settings.setDatabaseEnabled(true);
                }
                settings.setDomStorageEnabled(true);

                if (tab.clearAllCache) {
                    CookieManager.getInstance().removeAllCookie();
                } else if (tab.clearSessionCache) {
                    CookieManager.getInstance().removeSessionCookie();
                }

                // disable thirdparty cookies
                CookieManager.getInstance().setAcceptThirdPartyCookies(tab.inAppWebView, false);
                String cookies = CookieManager.getInstance().getCookie(url);
                LOG.d(LOG_TAG, "cookies url "+url+" value "+cookies);

                tab.inAppWebView.loadUrl(url);
//                tab.inAppWebView.setId(Integer.valueOf(8));
                tab.inAppWebView.getSettings().setLoadWithOverviewMode(true);
                tab.inAppWebView.getSettings().setUseWideViewPort(tab.useWideViewPort);
                // Multiple Windows set to true to mitigate Chromium security bug.
                //  See: https://bugs.chromium.org/p/chromium/issues/detail?id=1083819
                tab.inAppWebView.getSettings().setSupportMultipleWindows(true);
                tab.inAppWebView.requestFocus();
                tab.inAppWebView.requestFocusFromTouch();

                // Add the back and forward buttons to our action button container layout
                actionButtonContainer.addView(back);
                actionButtonContainer.addView(forward);

                // Add the views to our toolbar if they haven't been disabled
                if (!tab.hideNavigationButtons) toolbar.addView(actionButtonContainer);
                if (!tab.hideUrlBar) toolbar.addView(tab.edittext);

                if (!tab.fullscreen && tab.topOffset > 0) {
                    LinearLayout top = new LinearLayout(cordova.getActivity());
                    top.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, tab.topOffset));
                    top.setId(Integer.valueOf(11));
                    top.setHorizontalGravity(Gravity.LEFT);
                    top.setVerticalGravity(Gravity.TOP);
                    main.addView(top);
                }

                // Don't add the toolbar if its been disabled
                if (tab.showLocationBar) { //getShowLocationBar()) {
                    // Add our toolbar to our main view/layout
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, this.dpToPixels(TOOLBAR_HEIGHT));
                    if (!tab.fullscreen && tab.topOffset > 0)
                        lp.addRule(RelativeLayout.BELOW, 11);
                    toolbar.setLayoutParams(lp);
                    toolbar.setId(Integer.valueOf(10));
                    main.addView(toolbar);
                }

                if (!tab.fullscreen && tab.bottomOffset > 0) {
                    tab.bottom = new LinearLayout(cordova.getActivity());
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, tab.bottomOffset);
                    lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    tab.bottom.setLayoutParams(lp);
                    tab.bottom.setId(Integer.valueOf(12));
                    main.addView(tab.bottom);

                    // ContentView is the root view of the layout of this activity/fragment
/*                    View contentView = cordova.getActivity().findViewById(android.R.id.content);
                    contentView.getViewTreeObserver().addOnGlobalLayoutListener(
                            new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    Rect r = new Rect();
                                    contentView.getWindowVisibleDisplayFrame(r);
                                    int screenHeight = contentView.getRootView().getHeight();
                                    //LOG.d(LOG_TAG, "screenHeight " + screenHeight);

                                    // r.bottom is the position above soft keypad or device button.
                                    // if keypad is shown, the r.bottom is smaller than that before.
                                    int keypadHeight = screenHeight - r.bottom;

                                    LOG.d(LOG_TAG, "keypadHeight = " + keypadHeight + " isKeyboardShowing " + isKeyboardShowing);

                                    if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                                        // keyboard is opened
                                        if (!isKeyboardShowing) {
                                            isKeyboardShowing = true;
                                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
//                                                    hideBottom(tab);
//                                                    tab.dialog.getWindow().getDecorView().requestLayout();
//                                                    tab.dialog.getWindow().getDecorView().dispatchWindowVisibilityChanged(View.VISIBLE);
                                                }
                                            }, 1000);
                                        }
                                    } else {
                                        // keyboard is closed
                                        if (isKeyboardShowing) {
                                            isKeyboardShowing = false;
                                            //tab.bottom.setVisibility(View.VISIBLE);
                                            LOG.d(LOG_TAG, "show bottom");
                                        }
                                    }
                                }
                            });
*/
                }

                // Add our webview to our main view/layout
                tab.webViewLayout = new RelativeLayout(cordova.getActivity());
                tab.wlp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                if (tab.showLocationBar)
                    tab.wlp.addRule(RelativeLayout.BELOW, 10); // toolbar
                else if (!tab.fullscreen && tab.topOffset > 0)
                    tab.wlp.addRule(RelativeLayout.BELOW, 11); // top offset
                if (!tab.fullscreen && tab.bottomOffset > 0)
                    tab.wlp.addRule(RelativeLayout.ABOVE, 12); // bottom offset
                tab.webViewLayout.setLayoutParams(tab.wlp);
                tab.webViewLayout.addView(tab.inAppWebView);
                main.addView(tab.webViewLayout);

                // Don't add the footer unless it's been enabled
                if (tab.showFooter) {
                    tab.webViewLayout.addView(footer);
                }

                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                lp.copyFrom(tab.dialog.getWindow().getAttributes());
                lp.gravity = Gravity.TOP;
                lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                if (tab.dialog != null) {
                    tab.dialog.setContentView(main);
                    tab.dialog.show();
                    tab.dialog.getWindow().setAttributes(lp);
                }
                // the goal of openhidden is to load the url and not display it
                // Show() needs to be called to cause the URL to be loaded
                if (tab.openWindowHidden && tab.dialog != null) {
                    tab.dialog.hide();
                    tab.hidden = true;
                }
            }
        };
        this.cordova.getActivity().runOnUiThread(runnable);
        return "";
    }

    /**
     * Create a new plugin success result and send it back to JavaScript
     *
     * @param obj a JSONObject contain event payload information
     */
    private void sendUpdate(Tab tab, JSONObject obj, boolean keepCallback) {
        sendUpdate(tab, obj, keepCallback, PluginResult.Status.OK);
    }

    /**
     * Create a new plugin result and send it back to JavaScript
     *
     * @param obj a JSONObject contain event payload information
     * @param status the status code to return to the JavaScript environment
     */
    private void sendUpdate(Tab tab, JSONObject obj, boolean keepCallback, PluginResult.Status status) {
        if (tab.callbackContext != null) {
            PluginResult result = new PluginResult(status, obj);
            result.setKeepCallback(keepCallback);
            tab.callbackContext.sendPluginResult(result);
            if (!keepCallback) {
                tab.callbackContext = null;
            }
        } else {
            LOG.d(LOG_TAG, "failed to sendUpdate, no callbackContext for tab", tab.id);
        }
    }

    /**
     * Receive File Data from File Chooser
     *
     * @param requestCode the requested code from chromeclient
     * @param resultCode the result code returned from android system
     * @param intent the data from android file chooser
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        LOG.d(LOG_TAG, "onActivityResult");
        // If RequestCode or Callback is Invalid
        if(requestCode != FILECHOOSER_REQUESTCODE || this.tab.mUploadCallback == null) {
            super.onActivityResult(requestCode, resultCode, intent);
            return;
        }
        this.tab.mUploadCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
        this.tab.mUploadCallback = null;
    }

    /**
     * The webview client receives notifications about appView
     */
    public class InAppBrowserClient extends WebViewClient {
        Tab tab;
        EditText edittext;
        CordovaWebView webView;
        String beforeload;
        boolean waitForBeforeload;

        /**
         * Constructor.
         *
         * @param webView
         * @param mEditText
         */
        public InAppBrowserClient(Tab tab, CordovaWebView webView, EditText mEditText, String beforeload) {
            this.tab = tab;
            this.webView = webView;
            this.edittext = mEditText;
            this.beforeload = beforeload;
            this.waitForBeforeload = beforeload != null;
        }

        /**
         * Override the URL that should be loaded
         *
         * Legacy (deprecated in API 24)
         * For Android 6 and below.
         *
         * @param webView
         * @param url
         */
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            return shouldOverrideUrlLoading(url, null);
        }

        /**
         * Override the URL that should be loaded
         *
         * New (added in API 24)
         * For Android 7 and above.
         *
         * @param webView
         * @param request
         */
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest request) {
            return shouldOverrideUrlLoading(request.getUrl().toString(), request.getMethod());
        }

        /**
         * Override the URL that should be loaded
         *
         * This handles a small subset of all the URIs that would be encountered.
         *
         * @param url
         * @param method
         */
        public boolean shouldOverrideUrlLoading(String url, String method) {
            boolean override = false;
            boolean useBeforeload = false;
            String errorMessage = null;

            if (beforeload.equals("yes") && method == null) {
                useBeforeload = true;
            } else if(beforeload.equals("yes")
                    //TODO handle POST requests then this condition can be removed:
                    && !method.equals("POST"))
            {
                useBeforeload = true;
            } else if(beforeload.equals("get") && (method == null || method.equals("GET"))) {
                useBeforeload = true;
            } else if(beforeload.equals("post") && (method == null || method.equals("POST"))) {
                //TODO handle POST requests
                errorMessage = "beforeload doesn't yet support POST requests";
            }

            // On first URL change, initiate JS callback. Only after the beforeload event, continue.
            if (useBeforeload && this.waitForBeforeload) {
                if(sendBeforeLoad(url, method)) {
                    return true;
                }
            }

            if(errorMessage != null) {
                try {
                    LOG.e(LOG_TAG, errorMessage);
                    JSONObject obj = new JSONObject();
                    obj.put("type", LOAD_ERROR_EVENT);
                    obj.put("url", url);
                    obj.put("code", -1);
                    obj.put("message", errorMessage);
                    sendUpdate(tab, obj, true, PluginResult.Status.ERROR);
                } catch(Exception e) {
                    LOG.e(LOG_TAG, "Error sending loaderror for " + url + ": " + e.toString());
                }
            }

            if (url.startsWith(WebView.SCHEME_TEL)) {
                try {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse(url));
                    cordova.getActivity().startActivity(intent);
                    override = true;
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error dialing " + url + ": " + e.toString());
                }
            } else if (url.startsWith("geo:")
		       || url.startsWith(WebView.SCHEME_MAILTO)
		       || url.startsWith("market:")
		       || url.startsWith("intent:")
//		       || url.startsWith("lightning:")
//		       || url.startsWith("nostr:")
		       ) {
                LOG.d(LOG_TAG, "start new activity for " + url);
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    cordova.getActivity().startActivity(intent);
                    override = true;
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error with " + url + ": " + e.toString());
                }
            }
            // If sms:5551212?body=This is the message
            else if (url.startsWith("sms:")) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);

                    // Get address
                    String address = null;
                    int parmIndex = url.indexOf('?');
                    if (parmIndex == -1) {
                        address = url.substring(4);
                    } else {
                        address = url.substring(4, parmIndex);

                        // If body, then set sms body
                        Uri uri = Uri.parse(url);
                        String query = uri.getQuery();
                        if (query != null) {
                            if (query.startsWith("body=")) {
                                intent.putExtra("sms_body", query.substring(5));
                            }
                        }
                    }
                    intent.setData(Uri.parse("sms:" + address));
                    intent.putExtra("address", address);
                    intent.setType("vnd.android-dir/mms-sms");
                    cordova.getActivity().startActivity(intent);
                    override = true;
                } catch (android.content.ActivityNotFoundException e) {
                    LOG.e(LOG_TAG, "Error sending sms " + url + ":" + e.toString());
                }
            }
            // Test for whitelisted custom scheme names like mycoolapp:// or twitteroauthresponse:// (Twitter Oauth Response)
            else if (!url.startsWith("http:") && !url.startsWith("https:") && url.matches("^[A-Za-z0-9+.-]*://.*?$")) {
                if (tab.allowedSchemes == null) {
                    String allowed = preferences.getString("AllowedSchemes", null);
                    if(allowed != null) {
                        tab.allowedSchemes = allowed.split(",");
                    }
                }
                if (tab.allowedSchemes != null) {
                    for (String scheme : tab.allowedSchemes) {
                        if (url.startsWith(scheme)) {
                            try {
                                JSONObject obj = new JSONObject();
                                obj.put("type", "customscheme");
                                obj.put("url", url);
                                sendUpdate(tab, obj, true);
                                override = true;
                            } catch (JSONException ex) {
                                LOG.e(LOG_TAG, "Custom Scheme URI passed in has caused a JSON error.");
                            }
                        }
                    }
                }
            }

            if (useBeforeload) {
                this.waitForBeforeload = true;
            }
            return override;
        }

        private boolean sendBeforeLoad(String url, String method) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("type", BEFORELOAD);
                obj.put("url", url);
                if(method != null) {
                    obj.put("method", method);
                }
                sendUpdate(tab, obj, true);
                return true;
            } catch (JSONException ex) {
                LOG.e(LOG_TAG, "URI passed in has caused a JSON error.");
            }
            return false;
        }

        /**
         * New (added in API 21)
         * For Android 5.0 and above.
         *
         * @param view
         * @param request
         */
        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            return shouldInterceptRequest(request.getUrl().toString(), super.shouldInterceptRequest(view, request), request.getMethod());
        }

        public WebResourceResponse shouldInterceptRequest(String url, WebResourceResponse response, String method) {
            return response;
        }

        /*
         * onPageStarted fires the LOAD_START_EVENT
         *
         * @param view
         * @param url
         * @param favicon
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            if (tab.transparentLoading)
                view.setAlpha(1.0f);
            LOG.d(LOG_TAG, "started");

            String newloc = "";
            if (url.startsWith("http:") || url.startsWith("https:") || url.startsWith("file:")) {
                newloc = url;
            }
            else
            {
                // Assume that everything is HTTP at this point, because if we don't specify,
                // it really should be.  Complain loudly about this!!!
                LOG.e(LOG_TAG, "Possible Uncaught/Unknown URI");
                newloc = "http://" + url;
            }

            // Update the UI if we haven't already
            if (!newloc.equals(edittext.getText().toString())) {
                edittext.setText(newloc);
            }

            try {
                JSONObject obj = new JSONObject();
                obj.put("type", LOAD_START_EVENT);
                obj.put("url", newloc);
                sendUpdate(tab, obj, true);
            } catch (JSONException ex) {
                LOG.e(LOG_TAG, "URI passed in has caused a JSON error.");
            }
        }

        public void onPageCommitVisible (WebView view,
                                         String url) {
            super.onPageCommitVisible(view, url);
            injectDeferredObject("window.webkit={messageHandlers:{cordova_iab:cordova_iab}}", null, tab);

            try {
                JSONObject obj = new JSONObject();
                obj.put("type", LOAD_INIT_EVENT);
                obj.put("url", url);

                sendUpdate(tab, obj, true);
            } catch (JSONException ex) {
                LOG.d(LOG_TAG, "Should never happen");
            }
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            // LOG.d(LOG_TAG, "child page onPageFinished" + url);

            // Set the namespace for postMessage()
//            injectDeferredObject("window.webkit={messageHandlers:{cordova_iab:cordova_iab}}", null, tab);

            // CB-10395 InAppBrowser's WebView not storing cookies reliable to local device storage
            CookieManager.getInstance().flush();

            // https://issues.apache.org/jira/browse/CB-11248
            view.clearFocus();
            view.requestFocus();

            try {
                JSONObject obj = new JSONObject();
                obj.put("type", LOAD_STOP_EVENT);
                obj.put("url", url);

                sendUpdate(tab, obj, true);
            } catch (JSONException ex) {
                LOG.d(LOG_TAG, "Should never happen");
            }
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            try {
                JSONObject obj = new JSONObject();
                obj.put("type", LOAD_ERROR_EVENT);
                obj.put("url", failingUrl);
                obj.put("code", errorCode);
                obj.put("message", description);

                sendUpdate(tab, obj, true, PluginResult.Status.ERROR);
            } catch (JSONException ex) {
                LOG.d(LOG_TAG, "Should never happen");
            }
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            super.onReceivedSslError(view, handler, error);
            try {
                JSONObject obj = new JSONObject();
                obj.put("type", LOAD_ERROR_EVENT);
                obj.put("url", error.getUrl());
                obj.put("code", 0);
                obj.put("sslerror", error.getPrimaryError());
                String message;
                switch (error.getPrimaryError()) {
                    case SslError.SSL_DATE_INVALID:
                        message = "The date of the certificate is invalid";
                        break;
                    case SslError.SSL_EXPIRED:
                        message = "The certificate has expired";
                        break;
                    case SslError.SSL_IDMISMATCH:
                        message = "Hostname mismatch";
                        break;
                    default:
                    case SslError.SSL_INVALID:
                        message = "A generic error occurred";
                        break;
                    case SslError.SSL_NOTYETVALID:
                        message = "The certificate is not yet valid";
                        break;
                    case SslError.SSL_UNTRUSTED:
                        message = "The certificate authority is not trusted";
                        break;
                }
                obj.put("message", message);

                sendUpdate(tab, obj, true, PluginResult.Status.ERROR);
            } catch (JSONException ex) {
                LOG.d(LOG_TAG, "Should never happen");
            }
            handler.cancel();
        }

        /**
         * On received http auth request.
         */
        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {

            // Check if there is some plugin which can resolve this auth challenge
            PluginManager pluginManager = null;
            try {
                Method gpm = webView.getClass().getMethod("getPluginManager");
                pluginManager = (PluginManager)gpm.invoke(webView);
            } catch (NoSuchMethodException e) {
                LOG.d(LOG_TAG, e.getLocalizedMessage());
            } catch (IllegalAccessException e) {
                LOG.d(LOG_TAG, e.getLocalizedMessage());
            } catch (InvocationTargetException e) {
                LOG.d(LOG_TAG, e.getLocalizedMessage());
            }

            if (pluginManager == null) {
                try {
                    Field pmf = webView.getClass().getField("pluginManager");
                    pluginManager = (PluginManager)pmf.get(webView);
                } catch (NoSuchFieldException e) {
                    LOG.d(LOG_TAG, e.getLocalizedMessage());
                } catch (IllegalAccessException e) {
                    LOG.d(LOG_TAG, e.getLocalizedMessage());
                }
            }

            if (pluginManager != null && pluginManager.onReceivedHttpAuthRequest(webView, new CordovaHttpAuthHandler(handler), host, realm)) {
                return;
            }

            // By default handle 401 like we'd normally do!
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
        }
    }
}
