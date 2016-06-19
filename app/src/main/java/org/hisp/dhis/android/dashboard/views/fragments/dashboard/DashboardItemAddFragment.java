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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.adapters.DashboardItemSearchDialogAdapter;
import org.hisp.dhis.android.dashboard.adapters.DashboardItemSearchDialogAdapter.OptionAdapterValue;

import org.hisp.dhis.android.dashboard.presenters.DashboardItemAddFragmentPresenter;
import org.hisp.dhis.android.dashboard.views.fragments.BaseDialogFragment;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;

// TODO Remove ButterKnife (if need be)
// TODO see if ARG_DASHBOARD_ID is correct
public class DashboardItemAddFragment extends BaseDialogFragment
        implements DashboardItemAddFragmentView, PopupMenu.OnMenuItemClickListener
{
    private static final String TAG = DashboardItemAddFragment.class.getSimpleName();
    private static final String ARG_DASHBOARD_ID = "arg:dashboardId";
    private static final String ARG_PROGRAM_STAGE_ID = "arg:programStageId";

    @Inject
    DashboardItemAddFragmentPresenter dashboardItemAddFragmentPresenter;

    @Inject
    Logger logger;

    EditText mFilter;
    TextView mDialogLabel;
    ListView mListView;
    ImageView mFilterResources;

    PopupMenu mResourcesMenu;
    DashboardItemSearchDialogAdapter mAdapter;

    Dashboard mDashboard;

    public static DashboardItemAddFragment newInstance(long dashboardId) {
        Bundle args = new Bundle();
        args.putLong(ARG_DASHBOARD_ID, dashboardId);

        DashboardItemAddFragment fragment = new DashboardItemAddFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);

        ((DashboardApp) getActivity().getApplication())
                .getDashboardComponent().inject(this);

        dashboardItemAddFragmentPresenter.attachView(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_dashboard_item_add, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        logger.d(TAG, "onActivityCreated()");
        queryApiResources();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.d(TAG, "onDestroy()");
        dashboardItemAddFragmentPresenter.detachView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        // Associate the Dashboard
        long dashboardId = getArguments().getLong(ARG_DASHBOARD_ID);
        dashboardItemAddFragmentPresenter.getDashboardFromId(dashboardId);

        mFilter = (EditText) view.findViewById(R.id.filter_options);
        mDialogLabel = (TextView)view.findViewById(R.id.dialog_label);
        mListView = (ListView)view.findViewById(R.id.simple_listview);
        mFilterResources = (ImageView)view.findViewById(R.id.filter_resources);

        InputMethodManager imm = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mFilter.getWindowToken(), 0);

        mAdapter = new DashboardItemSearchDialogAdapter(
                LayoutInflater.from(getActivity()));
        mListView.setAdapter(mAdapter);
        mDialogLabel.setText(getString(R.string.add_dashboard_item));

        setupResourceMenu();
    }

    @OnTextChanged(value = R.id.filter_options,
            callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    @SuppressWarnings("unused")
    public void afterTextChanged(Editable s) {
        mAdapter.getFilter().filter(s.toString());
    }

    @OnClick({R.id.close_dialog_button, R.id.filter_resources})
    @SuppressWarnings("unused")
    public void onButtonClick(View v) {
        if (R.id.close_dialog_button == v.getId()) {
            dismissDialogFragment();
        } else if (R.id.filter_resources == v.getId()) {
            mResourcesMenu.show();
        }
    }

    @SuppressWarnings("unused")
    @OnItemClick(R.id.simple_listview)
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OptionAdapterValue adapterValue = mAdapter.getItem(position);
        dashboardItemAddFragmentPresenter.getDashboardContentFromId(adapterValue);
    }

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, TAG);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        item.setChecked(!item.isChecked());
        queryApiResources();
        return false;
    }

    @Override
    public void showOptionAdapterValues(List<OptionAdapterValue> data) {
        mAdapter.swapData(data);
    }

    // TODO handle with DashboardInteractor or mDashboard ?
    @Override
    public void addItemContent(DashboardContent resource) {
        //mDashboard.addItemContent(resource);
    }

    @Override
    public void setDashboard(Dashboard dashboard) {
        mDashboard = dashboard;
    }

    @Override
    public void dismissDialogFragment() {
        dismiss();
    }

    /** TODO when to set null
    @Override
    public void onLoaderReset(Loader<List<OptionAdapterValue>> loader) {
        if (loader != null && loader.getId() == LOADER_ID) {
            mAdapter.swapData(null);
        }
    }
    **/

    private void setupResourceMenu(){
        mResourcesMenu = new PopupMenu(getActivity(), mFilterResources);
        mResourcesMenu.inflate(R.menu.menu_filter_resources);
        mResourcesMenu.setOnMenuItemClickListener(this);
    }

    // TODO handle restartLoader appropriately(if need be)
    private void queryApiResources() {
        //getLoaderManager().restartLoader(LOADER_ID, getArguments(), this);
        dashboardItemAddFragmentPresenter.loadOptionAdapterValues(getTypesToInclude());
    }

    private List<String> getTypesToInclude() {
        List<String> typesToInclude = new ArrayList<>();
        if (isItemChecked(R.id.type_charts)) {
            typesToInclude.add(DashboardContent.TYPE_CHART);
        }
        if (isItemChecked(R.id.type_event_charts)) {
            typesToInclude.add(DashboardContent.TYPE_EVENT_CHART);
        }
        if (isItemChecked(R.id.type_maps)) {
            typesToInclude.add(DashboardContent.TYPE_MAP);
        }
        if (isItemChecked(R.id.type_report_tables)) {
            typesToInclude.add(DashboardContent.TYPE_REPORT_TABLE);
        }
        if (isItemChecked(R.id.type_event_reports)) {
            typesToInclude.add(DashboardContent.TYPE_EVENT_REPORT);
        }
        if (isItemChecked(R.id.type_users)) {
            typesToInclude.add(DashboardContent.TYPE_USERS);
        }
        if (isItemChecked(R.id.type_reports)) {
            typesToInclude.add(DashboardContent.TYPE_REPORTS);
        }
        if (isItemChecked(R.id.type_resources)) {
            typesToInclude.add(DashboardContent.TYPE_RESOURCES);
        }

        return typesToInclude;
    }

    private boolean isItemChecked(int id) {
        return mResourcesMenu.getMenu().findItem(id).isChecked();
    }

}
