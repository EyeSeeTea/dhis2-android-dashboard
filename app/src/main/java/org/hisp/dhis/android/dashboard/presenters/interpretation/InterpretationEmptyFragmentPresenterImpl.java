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

package org.hisp.dhis.android.dashboard.presenters.interpretation;

import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationEmptyFragmentView;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class InterpretationEmptyFragmentPresenterImpl implements InterpretationEmptyFragmentPresenter {
    private static final String TAG = InterpretationEmptyFragmentPresenterImpl.class.getSimpleName();
    private final InterpretationInteractor interpretationInteractor;
    private InterpretationEmptyFragmentView interpretationEmptyFragmentView;

    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    private CompositeSubscription subscription;
    private boolean hasSyncedBefore;
    private boolean isSyncing;

    public InterpretationEmptyFragmentPresenterImpl(InterpretationInteractor interpretationInteractor,
                                                    ApiExceptionHandler apiExceptionHandler,
                                                    Logger logger) {
        this.interpretationInteractor = interpretationInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
        this.hasSyncedBefore = false;
    }

    public void attachView(View view) {
        isNull(view, "InterpretationEmptyFragmentView must not be null");
        interpretationEmptyFragmentView = (InterpretationEmptyFragmentView) view;

        if (isSyncing) {
            interpretationEmptyFragmentView.showProgressBar();
        } else {
            interpretationEmptyFragmentView.hideProgressBar();
        }

        // check if metadata was synced,
        // if not, syncMetaData
        if (!isSyncing && !hasSyncedBefore) {
            logger.d(TAG, "!Syncing & !SyncedBefore");
            syncInterpretations();
        }
    }

    @Override
    public void detachView() {
        interpretationEmptyFragmentView.hideProgressBar();
        interpretationEmptyFragmentView = null;
    }

    // Set hasSyncedBefore boolean to True
    @Override
    public void syncInterpretations() {
        logger.d(TAG, "syncInterpretations");
        interpretationEmptyFragmentView.showProgressBar();
        isSyncing = true;
        subscription.add(interpretationInteractor.syncInterpretations()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Interpretation>>() {
                    @Override
                    public void call(List<Interpretation> interpretations) {
                        isSyncing = false;
                        hasSyncedBefore = true;

                        if (interpretationEmptyFragmentView != null) {
                            interpretationEmptyFragmentView.hideProgressBar();
                        }
                        logger.d(TAG, "Synced interpretations successfully");
                        if(interpretations!=null) {
                            logger.d(TAG + "Interpretations", interpretations.toString());
                            interpretationEmptyFragmentView.syncInterpretationsCallback();
                        }else{
                            logger.d(TAG + "Interpretations", "Empty pull");
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        if (interpretationEmptyFragmentView != null) {
                            interpretationEmptyFragmentView.hideProgressBar();
                        }
                        logger.e(TAG, "Failed to sync interpretations", throwable);
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
         getDhisService().isJobRunning(DhisService.SYNC_INTERPRETATIONS);
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
                        interpretationEmptyFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        interpretationEmptyFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        interpretationEmptyFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
