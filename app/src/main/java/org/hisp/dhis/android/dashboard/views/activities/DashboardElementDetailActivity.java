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

package org.hisp.dhis.android.dashboard.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.DashboardComponent;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.presenters.DashboardElementDetailActivityPresenter;
import org.hisp.dhis.android.dashboard.views.fragments.ImageViewFragment;
import org.hisp.dhis.android.dashboard.views.fragments.WebViewFragment;
import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardEmptyFragmentView;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationElement;
import org.hisp.dhis.client.sdk.ui.activities.BaseActivity;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.inject.Inject;

import okhttp3.HttpUrl;

public class DashboardElementDetailActivity extends BaseActivity implements DashboardElementDetailActivityView {
    public static final String TAG = DashboardElementDetailActivity.class.getSimpleName();
    private static final String DASHBOARD_ELEMENT_ID = "arg:dashboardElementId";
    private static final String INTERPRETATION_ELEMENT_ID = "arg:interpretationElementId";

    @Inject
    DashboardElementDetailActivityPresenter dashboardElementDetailActivityPresenter;

    @Inject
    Logger logger;

    Toolbar mToolbar;

    AlertDialog alertDialog;

    public static Intent newIntentForDashboardElement(Activity activity, long dashboardElementId) {
        Intent intent = new Intent(activity, DashboardElementDetailActivity.class);
        intent.putExtra(DASHBOARD_ELEMENT_ID, dashboardElementId);
        return intent;
    }

    public static Intent newIntentForInterpretationElement(Activity activity, long interpretationElementId) {
        Intent intent = new Intent(activity, DashboardElementDetailActivity.class);
        intent.putExtra(INTERPRETATION_ELEMENT_ID, interpretationElementId);
        return intent;
    }

    private String buildImageUrl(String resource, String id) {
        PreferencesModule preferencesModule =  dashboardElementDetailActivityPresenter.getPreferenceModule();
        HttpUrl url = HttpUrl.parse(preferencesModule.getConfigurationPreferences().get().getServerUrl());
        return url.newBuilder()
                .addPathSegment("api").addPathSegment(resource).addPathSegment(id).addPathSegment("data.png")
                .addQueryParameter("width", "480").addQueryParameter("height", "320").build()
                .toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_element_detail);

        mToolbar= (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ((DashboardApp) getApplication())
                .getDashboardComponent().inject(this);

        dashboardElementDetailActivityPresenter.attachView(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        long dashboardElementId = getDashboardElementId();
        long interpretationElementId = getInterpretationElementId();

        dashboardElementDetailActivityPresenter.loadElement(dashboardElementId);

        dashboardElementDetailActivityPresenter.loadInterpretation(interpretationElementId);

    }


    private long getDashboardElementId() {
        return getIntent().getLongExtra(DASHBOARD_ELEMENT_ID, -1);
    }

    private long getInterpretationElementId() {
        return getIntent().getLongExtra(INTERPRETATION_ELEMENT_ID, -1);
    }

    @Override
    public void onPause() {
        super.onPause();

        logger.d(TAG, "onPause()");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        dashboardElementDetailActivityPresenter.detachView();
    }

    @Override
    public void handleDashboardElement(DashboardElement element) {

        if (element == null || element.getDashboardItem() == null) {
            return;
        }

        mToolbar.setTitle(element.getDisplayName());
        switch (element.getDashboardItem().getType()) {
            case DashboardContent.TYPE_CHART: {
                String request = buildImageUrl("charts", element.getUId());
                attachFragment(ImageViewFragment.newInstance(request));
                break;
            }
            case DashboardContent.TYPE_EVENT_CHART: {
                String request = buildImageUrl("eventCharts", element.getUId());
                attachFragment(ImageViewFragment.newInstance(request));
                break;
            }
            case DashboardContent.TYPE_MAP: {
                String request = buildImageUrl("maps", element.getUId());
                attachFragment(ImageViewFragment.newInstance(request));
                break;
            }
            case DashboardContent.TYPE_REPORT_TABLE: {
                String elementId = element.getUId();
                attachFragment(WebViewFragment.newInstance(elementId));
                break;
            }
        }
    }

    @Override
    public void handleInterpretationElement(InterpretationElement element) {
        if (element == null || element.getInterpretation() == null) {
            return;
        }

        mToolbar.setTitle(element.getDisplayName());
        switch (element.getInterpretation().getType()) {
            case InterpretationElement.TYPE_CHART: {
                String request = buildImageUrl("charts", element.getUId());
                attachFragment(ImageViewFragment.newInstance(request));
                break;
            }
            case InterpretationElement.TYPE_MAP: {
                String request = buildImageUrl("maps", element.getUId());
                attachFragment(ImageViewFragment.newInstance(request));
                break;
            }
            case InterpretationElement.TYPE_REPORT_TABLE: {
                String elementId = element.getUId();
                attachFragment(WebViewFragment.newInstance(elementId));
                break;
            }
            case InterpretationElement.TYPE_DATA_SET: {
                break;
            }
        }
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    private void attachFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showErrorDialog(String title, String message) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setPositiveButton(R.string.option_confirm, null);
            alertDialog = builder.create();
        }
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }
}