/*
 * Copyright (c) 2015, University of Oslo
 * All rights reserved.
 *
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

package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.DashboardComponent;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.adapters.DashboardAdapter;
import org.hisp.dhis.android.dashboard.presenters.DashboardViewPagerFragmentPresenter;
import org.hisp.dhis.client.sdk.models.common.Access;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;

import javax.inject.Inject;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public class DashboardViewPagerFragment extends BaseFragment
        implements DashboardViewPagerFragmentView, ViewPager.OnPageChangeListener{
    static final String TAG = DashboardViewPagerFragment.class.getSimpleName();
    static final String STATE_IS_LOADING = "state:isLoading";

    @Inject
    DashboardViewPagerFragmentPresenter dashboardViewPagerFragmentPresenter;

    @Inject
    Logger logger;

    TabLayout mTabs;
    ViewPager mViewPager;
    SmoothProgressBar mProgressBar;
    DashboardAdapter mDashboardAdapter;
    AlertDialog alertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboards_view_pager, parent, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((DashboardApp) getActivity().getApplication())
                .getDashboardComponent().inject(this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        setupToolbar();
        mTabs = (TabLayout) view.findViewById(R.id.dashboard_tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.dashboard_view_pager);
        mProgressBar = (SmoothProgressBar) view.findViewById(R.id.progress_bar);

        mDashboardAdapter = new DashboardAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mDashboardAdapter);
        mViewPager.addOnPageChangeListener(this);

        // Syncing is Handled here in attachView only
        // Syncing is checked here with isSyncing and hasSyncedBefore booleans
        dashboardViewPagerFragmentPresenter.attachView(this);

        boolean isLoading = dashboardViewPagerFragmentPresenter.isSyncing();
        ;
        if ((savedInstanceState != null &&
                savedInstanceState.getBoolean(STATE_IS_LOADING)) || isLoading) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dashboardViewPagerFragmentPresenter.loadLocalDashboards();
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
        // Have to add attachView here , because Container Fragment's onResume()
        // and ViewPagerFragment's onResume() is called after setDashboards()
        // Syncing is checked here with isSyncing and hasSyncedBefore booleans
        dashboardViewPagerFragmentPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        logger.d(TAG, "onPause()");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        dashboardViewPagerFragmentPresenter.detachView();
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
    public void onPageSelected(int position) {
        Dashboard dashboard = mDashboardAdapter.getDashboard(position);
        Access dashboardAccess = dashboard.getAccess();

        Menu menu = getToolbarOfContainer().getMenu();
        menu.findItem(R.id.add_dashboard_item)
                .setVisible(dashboardAccess.isUpdate());
        menu.findItem(R.id.manage_dashboard)
                .setVisible(dashboardAccess.isUpdate());
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                               int positionOffsetPixels) {
        // stub implementation
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // stub implementation
    }

    @Override
    public void setDashboards(List<Dashboard> dashboards) {
        logger.e(TAG, "onSetDashboards()");
        mDashboardAdapter.swapData(dashboards);
        mTabs.removeAllTabs();

        if (dashboards != null && !dashboards.isEmpty()) {
            mTabs.setupWithViewPager(mViewPager);
        }
    }

    private void setupToolbar() {
        if (getToolbarOfContainer() != null) {
            logger.d(TAG, "nonNullToolbar");
            getToolbarOfContainer().inflateMenu(R.menu.menu_dashboard_view_pager_fragment);
            getToolbarOfContainer().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return DashboardViewPagerFragment.this.onMenuItemClicked(item);
                }
            });
        }
    }

    @Nullable
    protected Toolbar getToolbarOfContainer() {
        if (getParentFragment() != null && getParentFragment() instanceof DashboardContainerFragment) {
            return ((DashboardContainerFragment) getParentFragment()).getToolbar();
        }
        return null;
    }

    private boolean onMenuItemClicked(MenuItem item) {
        logger.d(TAG, "onMenuItemClick()");
        switch (item.getItemId()) {
            case R.id.add_dashboard_item: {
                long dashboardId = mDashboardAdapter
                        .getDashboard(mViewPager.getCurrentItem()).getId();
                 DashboardItemAddFragment
                 .newInstance(dashboardId)
                 .show(getChildFragmentManager());
                return true;
            }
            case R.id.refresh: {
                dashboardViewPagerFragmentPresenter.sync();
                return true;
            }
            case R.id.add_dashboard: {
                new DashboardAddFragment()
                        .show(getChildFragmentManager());
                return true;
            }
            case R.id.manage_dashboard: {
                Dashboard dashboard = mDashboardAdapter
                        .getDashboard(mViewPager.getCurrentItem());
                // TODO Write code for DashboardManageFragment and add it
                /**
                DashboardManageFragment
                        .newInstance(dashboard.getId())
                        .show(getChildFragmentManager());
                 **/
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
