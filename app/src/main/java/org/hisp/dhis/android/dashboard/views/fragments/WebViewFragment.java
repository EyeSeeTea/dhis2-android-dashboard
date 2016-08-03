/*
 * Copyright (c) 2016, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.views.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.R;

import org.hisp.dhis.android.dashboard.presenters.WebViewFragmentPresenter;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.inject.Inject;


import static android.text.TextUtils.isEmpty;

public class WebViewFragment extends BaseFragment implements WebViewFragmentView {
    private static final String DASHBOARD_ELEMENT_ID = "arg:dashboardElementId";

    @Inject
    WebViewFragmentPresenter webViewFragmentPresenter;

    @Inject
    Logger logger;

    WebView mWebView;
    View mProgressBarContainer;
    AlertDialog alertDialog;

    public static WebViewFragment newInstance(String id) {
        Bundle args = new Bundle();
        args.putString(DASHBOARD_ELEMENT_ID, id);

        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((DashboardApp) getActivity().getApplication())
                .getDashboardComponent().inject(this);

        webViewFragmentPresenter.attachView(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_web_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mWebView = (WebView) view.findViewById(R.id.web_view_content);
        mProgressBarContainer = view.findViewById(R.id.container_layout_progress_bar);

        mWebView.getSettings().setBuiltInZoomControls(true);
        if (getArguments() != null && !isEmpty(getArguments()
                .getString(DASHBOARD_ELEMENT_ID))) {
            webViewFragmentPresenter.getReportTableData(getArguments()
                    .getString(DASHBOARD_ELEMENT_ID));
        }
    }

    @Override
    public void onDataDownloaded(String data) {
        mProgressBarContainer.setVisibility(View.GONE);
        mWebView.loadData(data, "text/html", "UTF-8");
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    private void showErrorDialog(String title, String message) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.option_confirm, null);
            alertDialog = builder.create();
        }
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }
}