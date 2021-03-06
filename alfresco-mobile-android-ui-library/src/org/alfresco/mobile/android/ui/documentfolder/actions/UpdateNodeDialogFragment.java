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
package org.alfresco.mobile.android.ui.documentfolder.actions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.constants.ContentModel;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Tag;
import org.alfresco.mobile.android.api.model.impl.RepositoryVersionHelper;
import org.alfresco.mobile.android.ui.R;
import org.alfresco.mobile.android.ui.documentfolder.listener.OnNodeUpdateListener;
import org.alfresco.mobile.android.ui.fragments.BaseFragment;
import org.alfresco.mobile.android.ui.manager.MimeTypeManager;
import org.alfresco.mobile.android.ui.tag.TagLoaderCallback;
import org.alfresco.mobile.android.ui.tag.TagLoaderCallback.OnLoaderListener;
import org.alfresco.mobile.android.ui.tag.actions.TagPickerDialogFragment;
import org.alfresco.mobile.android.ui.tag.actions.TagPickerDialogFragment.onTagPickerListener;
import org.alfresco.mobile.android.ui.utils.Formatter;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public abstract class UpdateNodeDialogFragment extends BaseFragment
{
    public static final String TAG = "UpdateNodeDialogFragment";

    public static final String ARGUMENT_NODE = "node";

    public static final String ARGUMENT_CONTENT_NAME = "contentName";

    public static final String ARGUMENT_CONTENT_DESCRIPTION = "contentDescription";

    public static final String ARGUMENT_CONTENT_TAGS = "contentTags";

    protected OnNodeUpdateListener onUpdateListener;

    protected EditText editTags;

    protected List<Tag> selectedTags;

    private Node node;

    public UpdateNodeDialogFragment()
    {
    }

    public static Bundle createBundle(Node node)
    {
        Bundle args = new Bundle();
        args.putSerializable(ARGUMENT_NODE, node);
        return args;
    }

    @Override
    public void onStart()
    {
        if (node != null)
        {
            getDialog().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, MimeTypeManager.getIcon(node.getName()));
        }
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        getDialog().setTitle(R.string.metadata_prop_edit_metadata);
        getDialog().requestWindowFeature(Window.FEATURE_LEFT_ICON);

        node = (Node) getArguments().getSerializable(ARGUMENT_NODE);

        View v = inflater.inflate(R.layout.sdk_create_content_props, container, false);
        final EditText tv = (EditText) v.findViewById(R.id.content_name);
        final EditText desc = (EditText) v.findViewById(R.id.content_description);
        TextView tsize = (TextView) v.findViewById(R.id.content_size);

        editTags = (EditText) v.findViewById(R.id.content_tags);

        TagLoaderCallback tags = new TagLoaderCallback(alfSession, getActivity(), node);
        tags.setOnLoaderListener(new OnLoaderListener()
        {
            @Override
            public void afterLoading(PagingResult<Tag> tags)
            {
                displayTags(tags.getList());
                Log.d(TAG, tags.toString());
            }
        });
        tags.start();

        ImageButton ib = (ImageButton) v.findViewById(R.id.pick_tag);
        ib.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                createPickTag();
            }
        });

        Button button = (Button) v.findViewById(R.id.cancel);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                UpdateNodeDialogFragment.this.dismiss();
            }
        });

        final Button bcreate = (Button) v.findViewById(R.id.create_content);
        bcreate.setText(R.string.update);
        bcreate.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                Map<String, Serializable> props = new HashMap<String, Serializable>(2);
                props.put(ContentModel.PROP_NAME, tv.getText().toString());
                if (desc.getText() != null && desc.getText().length() > 0)
                {
                    props.put(ContentModel.PROP_DESCRIPTION, desc.getText().toString());
                }
                if (selectedTags != null && !selectedTags.isEmpty())
                {
                    props.put(ContentModel.PROP_TAGS, (ArrayList<Tag>) selectedTags);
                }
                UpdateLoaderCallback up = new UpdateLoaderCallback(alfSession, getActivity(), node, props);
                up.setOnUpdateListener(onUpdateListener);
                up.start();
                bcreate.setEnabled(false);
            }
        });

        if (node != null)
        {
            tv.setText(node.getName());
            if (node.isDocument())
            {
                tsize.setText(Formatter.formatFileSize(getActivity(), ((Document) node).getContentStreamLength()));
                tsize.setVisibility(View.VISIBLE);
            }

            if (RepositoryVersionHelper.isAlfrescoProduct(alfSession)
                    && node.getProperty(ContentModel.PROP_DESCRIPTION) != null
                    && node.getProperty(ContentModel.PROP_DESCRIPTION).getValue() != null)
            {
                desc.setText(node.getProperty(ContentModel.PROP_DESCRIPTION).getValue().toString());
            }

            bcreate.setEnabled(true);

        }
        else
        {
            tsize.setVisibility(View.GONE);
        }

        tv.addTextChangedListener(new TextWatcher()
        {
            public void afterTextChanged(Editable s)
            {
                if (tv.getText().length() == 0)
                {
                    bcreate.setEnabled(false);
                }
                else
                {
                    bcreate.setEnabled(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        });

        return v;
    }

    @TargetApi(13)
    public void createPickTag()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag(TagPickerDialogFragment.TAG);
        if (prev != null)
        {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        Log.d(TAG, "selectedTags : " + selectedTags);
        TagPickerDialogFragment newFragment = TagPickerDialogFragment.newInstance(alfSession, selectedTags);
        newFragment.setOnTagPickerListener(new onTagPickerListener()
        {
            @Override
            public void onValidateTags(List<Tag> tags)
            {
                displayTags(tags);
            }
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2
                && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        }
        else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left,
                    R.anim.slide_out_right);
        }

        newFragment.show(ft, TagPickerDialogFragment.TAG);
    }

    private void displayTags(List<Tag> tags)
    {
        selectedTags = tags;
        String s = "";
        for (int i = 0; i < tags.size(); i++)
        {
            if (i == 0)
            {
                s = tags.get(i).getValue();
            }
            else
            {
                s += "," + tags.get(i).getValue();
            }
        }
        editTags.setText(s);
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        if (selectedTags != null)
        {
            selectedTags.clear();
        }
        super.onDismiss(dialog);
    }
}
