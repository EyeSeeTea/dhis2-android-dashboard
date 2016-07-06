/*
 * Copyright (c) 2015, dhis2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.Toolbar;

import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.DashboardComponent;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.presenters.DashboardEmptyFragmentPresenter;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.fragments.WrapperFragment;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.inject.Inject;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 *         This fragment is shown in case there
 *         is no any dashboards in local database.
 */

// TODO Decide where to attachView for the first time
public class DashboardEmptyFragment extends BaseFragment implements DashboardEmptyFragmentView {
    public static final String TAG = DashboardEmptyFragment.class.getSimpleName();
    private static final String STATE_IS_LOADING = "state:isLoading";

    @Inject
    DashboardEmptyFragmentPresenter dashboardEmptyFragmentPresenter;

    @Inject
    Logger logger;

    // Progress bar
    SmoothProgressBar mProgressBar;
    // events
    AlertDialog alertDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboards_empty, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ((DashboardApp) getActivity().getApplication())
//                .getDashboardComponent().inject(this);

        DashboardComponent dashboardComponent = ((DashboardApp) getActivity().getApplication()).getDashboardComponent();
        // first time fragment is created
        if (savedInstanceState == null) {
            // it means we found old component and we have to release it
            if (dashboardComponent != null) {
                // create new instance of component
                ((DashboardApp) getActivity().getApplication()).releaseDashboardComponent();
            }
            dashboardComponent = ((DashboardApp) getActivity().getApplication()).createDashboardComponent();
        } else {
            dashboardComponent = ((DashboardApp) getActivity().getApplication()).getDashboardComponent();
        }
        // inject dependencies
        dashboardComponent.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO to include super or not
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        mProgressBar = (SmoothProgressBar) view.findViewById(R.id.progress_bar);

        // Syncing is Handled here in attachView only
        // Syncing is checked here with isSyncing and hasSyncedBefore booleans
        dashboardEmptyFragmentPresenter.attachView(this);

        boolean isLoading = dashboardEmptyFragmentPresenter.isSyncing();
        ;
        if ((savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_IS_LOADING)) || isLoading) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_LOADING, mProgressBar
                .getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        logger.d(TAG, "onResume()");
        dashboardEmptyFragmentPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        logger.d(TAG, "onPause()");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        dashboardEmptyFragmentPresenter.detachView();
    }

    @Override
    public void showProgressBar() {
        logger.d(TAG, "showProgressBar()");
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        logger.d(TAG, "hideProgressBar()");
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    /**
     * This method will return Toolbar instance of DashboardContainerFragment
     */
    @Nullable
    protected Toolbar getToolbarOfContainer() {
        if (getParentFragment() != null && getParentFragment() instanceof DashboardContainerFragment) {
            return ((DashboardContainerFragment) getParentFragment()).getToolbar();
        }
        return null;
    }

    private void setupToolbar() {
        if (getToolbarOfContainer() != null) {
            logger.d(TAG, "nonNullToolbar");
            getToolbarOfContainer().inflateMenu(R.menu.menu_dashboard_empty_fragment);
            getToolbarOfContainer().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return DashboardEmptyFragment.this.onMenuItemClicked(item);
                }
            });
        }
    }

    private boolean onMenuItemClicked(MenuItem item) {
        logger.d(TAG, "onMenuItemClick()");
        switch (item.getItemId()) {
            case R.id.action_refresh: {
                dashboardEmptyFragmentPresenter.sync();
                return true;
            }
            case R.id.add_dashboard: {
                new DashboardAddFragment()
                        .show(getChildFragmentManager());
                return true;
            }
        }
        return false;
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
