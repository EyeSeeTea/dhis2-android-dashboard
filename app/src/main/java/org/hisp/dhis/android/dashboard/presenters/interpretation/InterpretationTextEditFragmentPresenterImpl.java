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

import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationTextEditFragmentView;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class InterpretationTextEditFragmentPresenterImpl implements InterpretationTextEditFragmentPresenter {
    private static final String TAG = InterpretationTextEditFragmentPresenterImpl.class.getSimpleName();
    private InterpretationInteractor interpretationInteractor;
    private InterpretationTextEditFragmentView interpretationTextEditFragmentView;

    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    private CompositeSubscription subscription;

    Interpretation interpretation;

    public InterpretationTextEditFragmentPresenterImpl(InterpretationInteractor interpretationInteractor,
                                                       ApiExceptionHandler apiExceptionHandler,
                                                       Logger logger) {

        this.interpretationInteractor = interpretationInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        interpretationTextEditFragmentView = (InterpretationTextEditFragmentView) view;
    }

    @Override
    public void detachView() {
        interpretationTextEditFragmentView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void getInterpretation(final String interpretaionUId) {

        logger.e(TAG, "onGetInterpretation()");

        Observable<Interpretation> interpretations = interpretationInteractor.get(interpretaionUId);
        interpretations.subscribeOn(Schedulers.newThread());
        interpretations.observeOn(AndroidSchedulers.mainThread());
        interpretations.subscribe(new Action1<Interpretation>() {
            @Override
            public void call(Interpretation interpretation) {
                logger.d(TAG ,"onGetInterpretation " + interpretation.toString());
                interpretationTextEditFragmentView.setCurrentInterpretation(interpretation);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onGetInterpretation failed");
                handleError(throwable);
            }
        });
    }

    @Override
    public void updateInterpretation(Interpretation interpretation, String interpretationName) {

        interpretation.updateInterpretation(interpretationName);
        Observable<Boolean> success =  interpretationInteractor.save(interpretation);
        success.subscribeOn(Schedulers.newThread());
        success.observeOn(AndroidSchedulers.mainThread());
        success.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean success) {
                logger.d(TAG ,"onUpdateInterpretationComment " + success.toString());
                // save interpretationComment
                interpretationTextEditFragmentView.updateInterpretationCallback();

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onUpdateInterpretationComment failed");
                handleError(throwable);
            }
        });

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

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        interpretationTextEditFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        interpretationTextEditFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        interpretationTextEditFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
