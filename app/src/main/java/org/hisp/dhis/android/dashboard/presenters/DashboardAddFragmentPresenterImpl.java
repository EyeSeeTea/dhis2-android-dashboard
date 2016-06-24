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

package org.hisp.dhis.android.dashboard.presenters;

import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardAddFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;

import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class DashboardAddFragmentPresenterImpl implements DashboardAddFragmentPresenter {
    private static final String TAG = DashboardAddFragmentPresenterImpl.class.getSimpleName();
    private final DashboardInteractor dashboardInteractor;
    private DashboardAddFragmentView dashboardAddFragmentView;

    private final Logger logger;

    private CompositeSubscription subscription;

    public DashboardAddFragmentPresenterImpl(DashboardInteractor dashboardInteractor,
                                             Logger logger) {

        this.dashboardInteractor = dashboardInteractor;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        dashboardAddFragmentView = (DashboardAddFragmentView) view;
    }

    @Override
    public void detachView() {
        dashboardAddFragmentView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    // TODO write code according to SDK
    // TODO Handle dismissDialogFragment in callback
    @Override
    public void createDashboard(String dashboardName) {
        /**
        Dashboard newDashboard = Dashboard
                .createDashboard(mDashboardName.getText().toString());
        //newDashboard.save();
        dashboardInteractor.save(newDashboard);
        UiEventSync();
        dashboardAddFragmentView.dismissDialogFragment();
         **/
    }

    // TODO handle UiEventSync
    @Override
    public void UiEventSync() {
        /**
        if (isDhisServiceBound()) {
            getDhisService().syncDashboards();
            EventBusProvider.post(new UiEvent(UiEvent.UiEventType.SYNC_DASHBOARDS));
        }
         **/
    }
}