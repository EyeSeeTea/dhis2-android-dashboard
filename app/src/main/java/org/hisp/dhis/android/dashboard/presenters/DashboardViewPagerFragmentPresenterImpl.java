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

import org.hisp.dhis.android.dashboard.models.SyncWrapper;
import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardViewPagerFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

;

public class DashboardViewPagerFragmentPresenterImpl implements DashboardViewPagerFragmentPresenter {
    private static final String TAG = DashboardViewPagerFragmentPresenterImpl.class.getSimpleName();
    private final DashboardInteractor dashboardInteractor;
    private DashboardViewPagerFragmentView dashboardViewPagerFragmentView;

    private final SessionPreferences sessionPreferences;
    private final SyncDateWrapper syncDateWrapper;
    private final SyncWrapper syncWrapper;
    private final ApiExceptionHandler apiExceptionHandler;
//  TODO          private final SyncWrapper syncWrapper , add to constructor as well
    private final Logger logger;

    private CompositeSubscription subscription;
    private boolean hasSyncedBefore;
    private boolean isSyncing;

    public DashboardViewPagerFragmentPresenterImpl(DashboardInteractor dashboardInteractor,
                                               SessionPreferences sessionPreferences,
                                               SyncDateWrapper syncDateWrapper,
                                               SyncWrapper syncWrapper,
                                               ApiExceptionHandler apiExceptionHandler,
                                               Logger logger) {

        this.dashboardInteractor = dashboardInteractor;
        this.sessionPreferences = sessionPreferences;
        this.syncDateWrapper = syncDateWrapper;
        this.syncWrapper = syncWrapper;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
        this.hasSyncedBefore = false;
    }

    public void attachView(View view) {
        isNull(view, "DashboardViewPagerFragmentView must not be null");
        dashboardViewPagerFragmentView = (DashboardViewPagerFragmentView) view;

        // TODO handle isSyncing properly
        isSyncing = false;
        if (isSyncing) {
            dashboardViewPagerFragmentView.showProgressBar();
        } else {
            dashboardViewPagerFragmentView.hideProgressBar();
        }
        // check if metadata was synced,
        // if not, syncMetaData it

        // TODO don't do (check) sync right now
        /**
         if (!isSyncing && !hasSyncedBefore) {
         sync();
         }
         **/

        // TODO  Some loading method might be called here; listDashboards()
    }

    @Override
    public void detachView() {
        dashboardViewPagerFragmentView.hideProgressBar();
        dashboardViewPagerFragmentView = null;
    }

    @Override
    public void sync() {
        dashboardViewPagerFragmentView.showProgressBar();
        isSyncing = true;
       // TODO Syncing code
        subscription.add(syncWrapper.syncMetaData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Dashboard>>() {
                    @Override
                    public void call(List<Dashboard> dashboards) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        syncDateWrapper.setLastSyncedNow();

                        if (dashboardViewPagerFragmentView != null) {
                            dashboardViewPagerFragmentView.hideProgressBar();
                        }
                        logger.d(TAG, "Synced dashboards successfully");

                        // TODO  Some loading method might be called here; listDashboards()
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        if (dashboardViewPagerFragmentView != null) {
                            dashboardViewPagerFragmentView.hideProgressBar();
                        }
                        logger.e(TAG, "Failed to sync dashboards", throwable);
                        handleError(throwable);
                    }
                }));
    }


    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        dashboardViewPagerFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardViewPagerFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardViewPagerFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

    // TODO to include listDashboards() here or sync is enough ?

    /**
    @Override
    public void listDashboards() {
        logger.d(TAG, "listDashboards()");
        // TODO uncomment when interactor is addded to SDK
        subscription.add(dashboardInteractor.list()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Dashboard>() {
                    @Override
                    public void call(Dashboard dashboard) {
                        if (dashboardEmptyFragmentView != null) {
                            dashboardEmptyFragmentView.showDashboards(dashboard);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed listing dashboards.", throwable);
                    }
                }));
    }

    **/
}
