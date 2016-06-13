package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;


/**
 *         This fragment is used to make decision, whether to show fragment with
 *         dashboards or fragment with message.
 */

    //TODO  Code for fetching data

public class DashboardContainerFragment extends BaseFragment implements DashboardContainerFragmentView{

    private static final String TAG = DashboardContainerFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empty, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //TODO  Some initiation for loading data
    }

    private void attachFragment(Fragment fragment, String tag) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_content_frame, fragment, tag)
                .commitAllowingStateLoss();
    }

    private boolean isFragmentAttached(String tag) {
        return getChildFragmentManager().findFragmentByTag(tag) != null;
    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void hideProgressBar() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public void showUnexpectedError(String message) {

    }

    @Override
    public void navigationAfterLoadingData(Event event, Boolean hasData) {
        if (hasData) {
            // we don't want to attach the same fragment
            if (!isFragmentAttached(DashboardViewPagerFragment.TAG)) {
                attachFragment(new DashboardViewPagerFragment(),
                        DashboardViewPagerFragment.TAG);
            }
        } else {
            if (!isFragmentAttached(DashboardEmptyFragment.TAG)) {
                attachFragment(new DashboardEmptyFragment(),
                        DashboardEmptyFragment.TAG);
            }
        }
    }

    // TODO Implement Query of Dashboards

    /**
    private static class DashboardsQuery implements Query<Boolean> {

        @Override
        public Boolean query(Context context) {
            List<Dashboard> dashboards = new Select()
                    .from(Dashboard.class)
                    .where(Condition.column(Dashboard$Table
                            .STATE).isNot(State.TO_DELETE.toString()))
                    .queryList();

            return dashboards != null && dashboards.size() > 0;
        }
    }
     **/
}
