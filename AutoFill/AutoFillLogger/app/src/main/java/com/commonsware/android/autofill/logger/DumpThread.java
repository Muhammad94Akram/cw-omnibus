/***
 Copyright (c) 2015-2017 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _The Busy Coder's Guide to Android Development_
 https://commonsware.com/Android
 */

package com.commonsware.android.autofill.logger;

import android.app.assist.AssistStructure;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.service.autofill.FillCallback;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Set;

abstract class DumpThread extends Thread {
  abstract void onSuccess();
  abstract void onFailure(String message);

  private final File logFile;
  private final Bundle data;
  private final AssistStructure structure;
  private final Context ctxt;

  private DumpThread(Context ctxt, File logDir, Bundle data,
                     AssistStructure structure) {
    this.ctxt=ctxt.getApplicationContext();
    this.logFile=logDir;
    this.data=data;
    this.structure=structure;
  }

  @Override
  public void run() {
    if (logFile!=null) {
      JSONObject json=new JSONObject();

      if (data!=null) {
        try {
          json.put("data", dumpBundle(data, new JSONObject()));
        }
        catch (JSONException e) {
          Log.e(getClass().getSimpleName(),
            "Exception saving data", e);
        }
      }

      try {
        json.put("structure", dumpStructure(new JSONObject()));
      }
      catch (JSONException e) {
        Log.e(getClass().getSimpleName(),
          "Exception saving structure", e);
      }

      try {
        FileOutputStream fos=new FileOutputStream(logFile);
        OutputStreamWriter osw=new OutputStreamWriter(fos);
        PrintWriter pw=new PrintWriter(osw);

        pw.print(json.toString(2));
        pw.flush();
        fos.getFD().sync();
        fos.close();

        MediaScannerConnection
          .scanFile(ctxt,
            new String[]{logFile.getAbsolutePath()},
            new String[]{"application/json"}, null);

        Log.d(getClass().getSimpleName(),
          "autofill data written to: "+logFile.getAbsolutePath());

        onSuccess();
      }
      catch (Exception e) {
        Log.e(getClass().getSimpleName(),
          "Exception writing out autofill data", e);
        onFailure("Exception writing out autofill data");
      }
    }
    else {
      Log.d(getClass().getSimpleName(), "no log directory!");
    }
  }

  private JSONObject dumpBundle(Bundle b, JSONObject json)
    throws JSONException {
    Set<String> keys=b.keySet();

    for (String key : keys) {
      json.put(key, wrap(b.get(key)));
    }

    return (json);
  }

  private JSONObject dumpStructure(JSONObject json)
    throws JSONException {
    return (json.put("windows",
      dumpStructureWindows(new JSONArray())));
  }

  private JSONArray dumpStructureWindows(JSONArray windows)
    throws JSONException {
    for (int i=0; i<structure.getWindowNodeCount(); i++) {
      windows.put(
        dumpStructureWindow(structure.getWindowNodeAt(i),
          new JSONObject()));
    }

    return (windows);
  }

  private JSONObject dumpStructureWindow(
    AssistStructure.WindowNode window,
    JSONObject json)
    throws JSONException {
    json.put("displayId", wrap(window.getDisplayId()));
    json.put("height", wrap(window.getHeight()));
    json.put("left", wrap(window.getLeft()));
    json.put("title", wrap(window.getTitle()));
    json.put("top", wrap(window.getTop()));
    json.put("width", wrap(window.getWidth()));
    json.put("root",
      dumpStructureNode(window.getRootViewNode(),
        new JSONObject()));

    return (json);
  }

  private JSONObject dumpStructureNode(
    AssistStructure.ViewNode node,
    JSONObject json)
    throws JSONException {
    json.put("accessibilityFocused",
      wrap(node.isAccessibilityFocused()));
    json.put("activated", wrap(node.isActivated()));
    json.put("alpha", wrap(node.getAlpha()));
    json.put("assistBlocked", wrap(node.isAssistBlocked()));
    json.put("checkable", wrap(node.isCheckable()));
    json.put("checked", wrap(node.isChecked()));
    json.put("className", wrap(node.getClassName()));
    json.put("clickable", wrap(node.isClickable()));
    json.put("contentDescription",
      wrap(node.getContentDescription()));
    json.put("contextClickable",
      wrap(node.isContextClickable()));
    json.put("elevation", wrap(node.getElevation()));
    json.put("enabled", wrap(node.isEnabled()));

    if (node.getExtras()!=null) {
      json.put("extras", dumpBundle(node.getExtras(),
        new JSONObject()));
    }

    json.put("focusable", wrap(node.isFocusable()));
    json.put("focused", wrap(node.isFocused()));
    json.put("height", wrap(node.getHeight()));
    json.put("hint", wrap(node.getHint()));
    json.put("id", wrap(node.getId()));
    json.put("idEntry", wrap(node.getIdEntry()));
    json.put("idPackage", wrap(node.getIdPackage()));
    json.put("idType", wrap(node.getIdType()));
    json.put("left", wrap(node.getLeft()));
    json.put("longClickable", wrap(node.isLongClickable()));
    json.put("scrollX", wrap(node.getScrollX()));
    json.put("scrollY", wrap(node.getScrollY()));
    json.put("isSelected", wrap(node.isSelected()));
    json.put("text", wrap(node.getText()));
    json.put("textBackgroundColor",
      wrap(node.getTextBackgroundColor()));
    json.put("textColor", wrap(node.getTextColor()));
    json.put("textLineBaselines",
      wrap(node.getTextLineBaselines()));
    json.put("textLineCharOffsets",
      wrap(node.getTextLineCharOffsets()));
    json.put("textSelectionEnd",
      wrap(node.getTextSelectionEnd()));
    json.put("textSelectionStart",
      wrap(node.getTextSelectionStart()));
    json.put("textSize", wrap(node.getTextSize()));
    json.put("textStyle", wrap(node.getTextStyle()));
    json.put("top", wrap(node.getTop()));
    json.put("transformation",
      wrap(node.getTransformation()));
    json.put("visibility", wrap(node.getVisibility()));
    json.put("width", wrap(node.getWidth()));

    json.put("children",
      dumpStructureNodes(node, new JSONArray()));

    return (json);
  }

  private JSONArray dumpStructureNodes(
    AssistStructure.ViewNode node,
    JSONArray children) throws JSONException {
    for (int i=0; i<node.getChildCount(); i++) {
      children.put(dumpStructureNode(node.getChildAt(i),
        new JSONObject()));
    }

    return (children);
  }

  private Object wrap(Object thingy) {
    if (thingy instanceof CharSequence) {
      return (JSONObject.wrap(thingy.toString()));
    }

    return (JSONObject.wrap(thingy));
  }

  static class Save extends DumpThread {
    private final SaveCallback saveCallback;

    Save(Context ctxt, File logDir, Bundle data, AssistStructure structure,
         SaveCallback saveCallback) {
      super(ctxt, logDir, data, structure);

      this.saveCallback=saveCallback;
    }

    @Override
    void onSuccess() {
      saveCallback.onSuccess();
    }

    @Override
    void onFailure(String message) {
      saveCallback.onFailure(message);
    }
  }

  static class Fill extends DumpThread {
    private final FillCallback fillCallback;
    private final FillResponse response;

    Fill(Context ctxt, File logDir, Bundle data, AssistStructure structure,
         FillCallback fillCallback, FillResponse response) {
      super(ctxt, logDir, data, structure);

      this.fillCallback=fillCallback;
      this.response=response;
    }

    @Override
    void onSuccess() {
      fillCallback.onSuccess(response);
    }

    @Override
    void onFailure(String message) {
      fillCallback.onFailure(message);
    }
  }
}