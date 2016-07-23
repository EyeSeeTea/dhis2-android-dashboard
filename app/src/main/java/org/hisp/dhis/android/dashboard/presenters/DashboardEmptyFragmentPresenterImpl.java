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

import org.hisp.dhis.android.dashboard.sync.SyncWrapper;
import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardEmptyFragmentView;
;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardContentInteractor;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardContent;
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

public class DashboardEmptyFragmentPresenterImpl implements DashboardEmptyFragmentPresenter {
    private static final String TAG = DashboardEmptyFragmentPresenterImpl.class.getSimpleName();
    private final DashboardInteractor dashboardInteractor;
    private final DashboardContentInteractor dashboardContentInteractor;
    private DashboardEmptyFragmentView dashboardEmptyFragmentView;

    private final SessionPreferences sessionPreferences;
    private final SyncDateWrapper syncDateWrapper;
    private final SyncWrapper syncWrapper;
    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    private CompositeSubscription subscription;
    private boolean hasSyncedBefore;
    private boolean isSyncing;

    public DashboardEmptyFragmentPresenterImpl(DashboardInteractor dashboardInteractor,
                                               DashboardContentInteractor dashboardContentInteractor,
                                               SessionPreferences sessionPreferences,
                                               SyncDateWrapper syncDateWrapper,
                                               SyncWrapper syncWrapper,
                                               ApiExceptionHandler apiExceptionHandler,
                                               Logger logger) {
        this.dashboardInteractor = dashboardInteractor;
        this.dashboardContentInteractor = dashboardContentInteractor;
        this.sessionPreferences = sessionPreferences;
        this.syncDateWrapper = syncDateWrapper;
        this.syncWrapper = syncWrapper;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
        this.hasSyncedBefore = false;
    }

    public void attachView(View view) {
        isNull(view, "DashboardEmptyFragmentView must not be null");
        dashboardEmptyFragmentView = (DashboardEmptyFragmentView) view;

        // TODO conditions to check if Syncing has to be done
        /**
         if (isDhisServiceBound() &&
         !getDhisService().isJobRunning(DhisService.SYNC_DASHBOARDS) &&
         !SessionManager.getInstance().isResourceTypeSynced(ResourceType.DASHBOARDS)) {
         syncDashboards();
         }
         **/

        if (isSyncing) {
            dashboardEmptyFragmentView.showProgressBar();
        } else {
            dashboardEmptyFragmentView.hideProgressBar();
        }

        // check if metadata was synced,
        // if not, syncMetaData
        if (!isSyncing && !hasSyncedBefore) {
            logger.d(TAG, "!Syncing & !SyncedBefore");
            syncDashboardContent();
        }
    }

    @Override
    public void detachView() {
        dashboardEmptyFragmentView.hideProgressBar();
        dashboardEmptyFragmentView = null;
    }

    @Override
    public void syncDashboardContent() {
        logger.d(TAG, "syncDashboardContent");
        dashboardEmptyFragmentView.showProgressBar();
        // TODO Write code for syncing
        isSyncing = true;
        subscription.add(dashboardContentInteractor.syncDashboardContent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<DashboardContent>>() {
                    @Override
                    public void call(List<DashboardContent> dashboardContents) {
                        isSyncing = false;
                        hasSyncedBefore = true;

                        if (dashboardEmptyFragmentView != null) {
                            dashboardEmptyFragmentView.hideProgressBar();
                        }
                        logger.d(TAG, "Synced dashboardContents successfully");
                        if(dashboardContents!=null) {
                            logger.d(TAG + "DashboardContents", dashboardContents.toString());
                        }else{
                            logger.d(TAG + "DashboardContents", "Empty pull");
                        }
                        //do something
                        syncDashboard();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        if (dashboardEmptyFragmentView != null) {
                            dashboardEmptyFragmentView.hideProgressBar();
                        }
                        logger.e(TAG, "Failed to sync dashboardContents", throwable);
                        handleError(throwable);
                    }
                })
        );
    }

    // Set hasSyncedBefore boolean to True
    @Override
    public void syncDashboard() {
        logger.d(TAG, "syncDashboard");
        dashboardEmptyFragmentView.showProgressBar();
        // TODO Write code for syncing
        isSyncing = true;
        subscription.add(dashboardInteractor.syncDashboards()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Dashboard>>() {
                    @Override
                    public void call(List<Dashboard> dashboards) {
                        isSyncing = false;
                        hasSyncedBefore = true;

                        if (dashboardEmptyFragmentView != null) {
                            dashboardEmptyFragmentView.hideProgressBar();
                        }
                        logger.d(TAG, "Synced dashboards successfully");
                        if(dashboards!=null) {
                            logger.d(TAG + "Dashboards", dashboards.toString());
                        }else{
                            logger.d(TAG + "Dashboards", "Empty pull");
                        }
                        //do something
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        if (dashboardEmptyFragmentView != null) {
                            dashboardEmptyFragmentView.hideProgressBar();
                        }
                        logger.e(TAG, "Failed to sync dashboards", throwable);
                        handleError(throwable);
                    }
                })
        );
    }

    @Override
    public boolean isSyncing() {
        return isSyncing;
        /**
         boolean isLoading = isDhisServiceBound() &&
         getDhisService().isJobRunning(DhisService.SYNC_DASHBOARDS);
         **/
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        dashboardEmptyFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardEmptyFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardEmptyFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
