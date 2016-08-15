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

import org.hisp.dhis.android.dashboard.adapters.DashboardItemSearchDialogAdapter;
import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardItemAddFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardContentInteractor;
import org.hisp.dhis.android.dashboard.adapters.DashboardItemSearchDialogAdapter.OptionAdapterValue;

import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class DashboardItemAddFragmentPresenterImpl implements DashboardItemAddFragmentPresenter {
    private static final String TAG = DashboardItemAddFragmentPresenterImpl.class.getSimpleName();
    private final DashboardContentInteractor dashboardContentInteractor;
    private final DashboardInteractor dashboardInteractor;
    private DashboardItemAddFragmentView dashboardItemAddFragmentView;
    private ApiExceptionHandler apiExceptionHandler;

    private final Logger logger;

    private CompositeSubscription subscription;

    public DashboardItemAddFragmentPresenterImpl(DashboardContentInteractor dashboardContentInteractor,
                                                 DashboardInteractor dashboardInteractor,
                                                 ApiExceptionHandler apiExceptionHandler,
                                                 Logger logger) {

        this.dashboardContentInteractor = dashboardContentInteractor;
        this.dashboardInteractor = dashboardInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        dashboardItemAddFragmentView = (DashboardItemAddFragmentView) view;
    }

    @Override
    public void detachView() {
        dashboardItemAddFragmentView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void loadOptionAdapterValues(List<String> typesToInclude) {

        Set<String> types = new HashSet<>(typesToInclude);
        Observable<List<DashboardContent>> dashboardContents =
                dashboardContentInteractor.list(types);
        dashboardContents.subscribeOn(Schedulers.newThread());
        dashboardContents.observeOn(AndroidSchedulers.mainThread());
        dashboardContents.subscribe(new Action1<List<DashboardContent>>() {
            @Override
            public void call(List<DashboardContent> dashboardContents) {
                logger.d(TAG ,"loadDashboardContentItemAddF " + dashboardContents);

                Collections.sort(dashboardContents, DashboardContent.DISPLAY_NAME_COMPARATOR);

                List<DashboardItemSearchDialogAdapter.OptionAdapterValue> adapterValues = new ArrayList<>();
                for (DashboardContent dashboardItemContent : dashboardContents) {
                    adapterValues.add(new DashboardItemSearchDialogAdapter.OptionAdapterValue(dashboardItemContent.getUId(),
                            dashboardItemContent.getDisplayName()));
                }
                dashboardItemAddFragmentView.showOptionAdapterValues(adapterValues);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "loadDashboardContentItemAddF failed");
            }
        });
    }

    @Override
    public void getDashboardFromUId(String dashboardUId) {

        logger.e(TAG, "onGetDashboards()");

        Observable<Dashboard> dashboards = dashboardInteractor.get(dashboardUId);
        dashboards.subscribeOn(Schedulers.newThread());
        dashboards.observeOn(AndroidSchedulers.mainThread());
        dashboards.subscribe(new Action1<Dashboard>() {
            @Override
            public void call(Dashboard dashboard) {
                logger.d(TAG ,"onGetDashboards " + dashboard.toString());
                dashboardItemAddFragmentView.setDashboard(dashboard);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onGetDashboards failed");
                handleError(throwable);
            }
        });
    }

    // TODO Get DashboardContent
    @Override
    public void getDashboardContentFromId(OptionAdapterValue optionAdapterValue) {
        /**
        DashboardContent resource = new Select()
                .from(DashboardContent.class)
                .where(Condition.column(DashboardItemContent$Table
                        .UID).is(adapterValue.id))
                .querySingle();
         **/

        // Replace null with DashboardContent
        dashboardItemAddFragmentView.addItemContent(null);
        dashboardItemAddFragmentView.uiSync();
        dashboardItemAddFragmentView.dismissDialogFragment();
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        dashboardItemAddFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardItemAddFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardItemAddFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
