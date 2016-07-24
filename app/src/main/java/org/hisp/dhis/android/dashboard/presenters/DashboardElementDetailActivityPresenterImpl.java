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

import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.dashboard.views.activities.DashboardElementDetailActivityView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardElementInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationElement;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class DashboardElementDetailActivityPresenterImpl implements DashboardElementDetailActivityPresenter {
    private static final String TAG = DashboardElementDetailActivityPresenterImpl.class.getSimpleName();
    private final DashboardElementInteractor dashboardElementInteractor;
    private DashboardElementDetailActivityView dashboardElementDetailActivityView;

    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    private final PreferencesModule preferencesModule;

    public DashboardElementDetailActivityPresenterImpl(DashboardElementInteractor dashboardElementInteractor,
                                                       ApiExceptionHandler apiExceptionHandler,
                                                       Logger logger, PreferencesModule preferencesModule) {
        this.dashboardElementInteractor = dashboardElementInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;
        this.preferencesModule = preferencesModule;
    }

    public void attachView(View view) {
        isNull(view, "DashboardElementDetailActivityView must not be null");
        dashboardElementDetailActivityView = (DashboardElementDetailActivityView) view;
    }

    @Override
    public void detachView() {
        dashboardElementDetailActivityView = null;
    }

    @Override
    public void loadElement(long dashboardElementId) {
        logger.d(TAG, "loadElement");

        if (dashboardElementId > 0) {

            Observable<DashboardElement> dashboardElement = dashboardElementInteractor.get(dashboardElementId);
            dashboardElement.subscribeOn(Schedulers.newThread());
            dashboardElement.observeOn(AndroidSchedulers.mainThread());
            dashboardElement.subscribe(new Action1<DashboardElement>() {
                @Override
                public void call(DashboardElement element) {
                    logger.d(TAG ,"loadedElementWithId " + element.toString());
                    dashboardElementDetailActivityView.handleDashboardElement(element);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    logger.d(TAG , "loadedElementWithId failed");
                    handleError(throwable);
                }
            });
        }

    }

    // TODO
    @Override
    public void loadInterpretation(long interpretationElementId) {
        logger.d(TAG, "loadInterpretation");
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        dashboardElementDetailActivityView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardElementDetailActivityView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardElementDetailActivityView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

    @Override
    public PreferencesModule getPreferenceModule() {
        return preferencesModule;
    }

}
