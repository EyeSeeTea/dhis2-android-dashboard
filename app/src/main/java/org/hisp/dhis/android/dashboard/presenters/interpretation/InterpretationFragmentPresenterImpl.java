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

import android.util.Log;

import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationFragmentView;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationCommentInteractor;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationElementInteractor;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationComment;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationElement;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class InterpretationFragmentPresenterImpl implements InterpretationFragmentPresenter {
    private static final String TAG = InterpretationFragmentPresenterImpl.class.getSimpleName();
    private final InterpretationInteractor interpretationInteractor;
    private final InterpretationElementInteractor interpretationElementInteractor;
    private final InterpretationCommentInteractor interpretationCommentInteractor;
    private InterpretationFragmentView interpretationFragmentView;
    private final ApiExceptionHandler apiExceptionHandler;
    private final PreferencesModule preferencesModule;
    private final Logger logger;

    private CompositeSubscription subscription;

    private boolean hasSyncedBefore;
    private boolean isSyncing;

    public InterpretationFragmentPresenterImpl(InterpretationInteractor interpretationInteractor,
                                               InterpretationElementInteractor interpretationElementInteractor,
                                               InterpretationCommentInteractor interpretationCommentInteractor,
                                               ApiExceptionHandler apiExceptionHandler,
                                               PreferencesModule preferencesModule,
                                               Logger logger) {

        this.interpretationInteractor = interpretationInteractor;
        this.interpretationElementInteractor = interpretationElementInteractor;
        this.interpretationCommentInteractor = interpretationCommentInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.preferencesModule = preferencesModule;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
        this.hasSyncedBefore = false;
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        interpretationFragmentView = (InterpretationFragmentView) view;

       initSyncing();
    }

    @Override
    public void initSyncing(){
        if (isSyncing) {
            interpretationFragmentView.showProgressBar();
        } else {
            interpretationFragmentView.hideProgressBar();
        }

        if (!isSyncing && !hasSyncedBefore) {
            logger.d(TAG, "!Syncing & !SyncedBefore");
            syncInterpretations();
        }
    }

    @Override
    public void detachView() {
        interpretationFragmentView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void loadLocalInterpretations() {
        logger.d(TAG, "LoadInterpretationItems()");

        final Observable<List<Interpretation>> interpretation = interpretationInteractor.list();
        interpretation.subscribeOn(Schedulers.newThread());
        interpretation.observeOn(AndroidSchedulers.mainThread());
        interpretation.subscribe(new Action1<List<Interpretation>>() {
            @Override
            public void call(List<Interpretation> interpretationList) {
                if (interpretationList != null && !interpretationList.isEmpty()) {
                    logger.d(TAG ,"LoadInterpretationItems " + interpretationList.toString());
                    for (Interpretation interpretation : interpretationList) {

                        List<InterpretationElement> interpretationElements =
                                interpretationElementInteractor.list(interpretation).toBlocking().first();
                        Log.d(TAG +" IElements", interpretationElements.toString());
                        interpretation.setInterpretationElements(interpretationElements);

                        List<InterpretationComment> interpretationComments =
                                interpretationCommentInteractor.list(interpretation.getUId()).toBlocking().first();
                        Log.d(TAG +" IComments", interpretationComments.toString());
                        interpretation.setComments(interpretationComments);
                    }
                }
                // sort interpretations by created field in reverse order.
                Collections.sort(interpretationList,
                        Collections.reverseOrder(Interpretation.CREATED_COMPARATOR));
                interpretationFragmentView.showInterpretations(interpretationList);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "LoadInterpretation failed");
                handleError(throwable);
            }
        });
    }


    // Set hasSyncedBefore boolean to True
    @Override
    public void syncInterpretations() {
        logger.d(TAG, "syncInterpretations");
        interpretationFragmentView.showProgressBar();
        isSyncing = true;
        subscription.add(interpretationInteractor.syncInterpretations()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Interpretation>>() {
                    @Override
                    public void call(List<Interpretation> interpretations) {
                        isSyncing = false;
                        hasSyncedBefore = true;

                        if (interpretationFragmentView != null) {
                            interpretationFragmentView.hideProgressBar();
                        }
                        logger.d(TAG, "Synced interpretations successfully");
                        if(interpretations!=null) {
                            loadLocalInterpretations();
                            logger.d(TAG + "Interpretations", interpretations.toString());
                        }else{
                            logger.d(TAG + "Interpretations", "Empty pull");
                        }
                        //do something
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        if (interpretationFragmentView != null) {
                            interpretationFragmentView.hideProgressBar();
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
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        interpretationFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        interpretationFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        interpretationFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

    @Override
    public PreferencesModule getPreferenceModule(){
        return preferencesModule;
    }

    @Override
    public void deleteInterpretation(Interpretation interpretation) {
        interpretationInteractor.remove(interpretation);
    }
}
