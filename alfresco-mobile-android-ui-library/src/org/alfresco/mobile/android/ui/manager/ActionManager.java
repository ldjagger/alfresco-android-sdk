/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.ui.manager;

import java.io.File;

import org.alfresco.mobile.android.ui.R;

import android.app.Activity;
import android.app.Fragment;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class ActionManager
{

    /**
     * Allow to open a file with an associated application installed in the
     * device.
     * 
     * @param context
     * @param myFile
     * @param mimeType
     */
    public static void actionView(Context context, File myFile, String mimeType)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(myFile);
        intent.setDataAndType(data, mimeType.toLowerCase());

        try
        {
            context.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(context, R.string.error_unable_open_file);
        }
    }

    /**
     * Allow to open a file with an associated application installed in the
     * device and saved it backed via a requestcode...
     * 
     * @param context
     * @param myFile
     * @param mimeType
     */
    public static void openIn(Fragment fr, File myFile, String mimeType, int requestCode)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(myFile);
        intent.setDataAndType(data, mimeType.toLowerCase());

        try
        {
            fr.startActivityForResult(intent, requestCode);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_open_file);
        }
    }
    
    public static void openIn(Fragment fr, File myFile, String mimeType)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(myFile);
        intent.setDataAndType(data, mimeType.toLowerCase());

        try
        {
            fr.startActivity(intent);
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_open_file);
        }
    }

    /**
     * Allow to send a link to other application installed in the device.
     * 
     * @param fr
     * @param url
     */
    public static void actionShareLink(Fragment fr, String url)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, R.string.share_url_long);
            i.putExtra(Intent.EXTRA_TEXT, url);
            fr.startActivity(Intent.createChooser(i, fr.getActivity().getText(R.string.share_url)));
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_share_link);
        }
    }

    /**
     * Allow user to share a file with other applications.
     * 
     * @param fr
     * @param contentFile
     */
    public static void actionShareContent(Fragment fr, File contentFile)
    {
        try
        {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.putExtra(Intent.EXTRA_SUBJECT, contentFile.getName());
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
            i.setType(MimeTypeManager.getMIMEType(contentFile.getName()));
            fr.startActivity(Intent.createChooser(i, fr.getActivity().getText(R.string.share_content)));
        }
        catch (ActivityNotFoundException e)
        {
            MessengerManager.showToast(fr.getActivity(), R.string.error_unable_share_content);
        }
    }

    /**
     * Allow to show map
     */
    public static void actionShowMap(Fragment f, String name, String lattitude, String longitude)
    {
        final String uri = "geo:0,0?q=" + lattitude + "," + longitude + " (" + name + ")";
        f.startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
    }

    /**
     * Allow to pick file with other apps.
     * 
     * @return Activity for Result.
     */
    public static void actionPickFile(Fragment f, int requestCode)
    {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        f.startActivityForResult(Intent.createChooser(i, f.getText(R.string.pick_file_title)), requestCode);
    }

    /**
     * Utils to get File path
     * 
     * @param activity
     * @param uri
     * @return
     */
    public static String getPath(Activity activity, Uri uri)
    {
        String scheme = uri.getScheme();
        String s = null;
        if (scheme.equals("content"))
        {
            String[] projection = { MediaStore.Files.FileColumns.DATA };
            Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
            activity.startManagingCursor(cursor);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            cursor.moveToFirst();
            s = cursor.getString(columnIndex);
        }
        else if (scheme.equals("file"))
        {
            s = uri.getPath();
        }
        Log.d("ActionManager", "URI:" + uri + " - S:" + s);
        return s;
    }

}
