package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;

/**
 *         This fragment is used to make decision, whether to show fragment with
 *         dashboards or fragment with message.
 */

public class DashboardContainerFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empty, container, false);
    }

}
