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

import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationCommentEditFragmentView;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationCommentInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationComment;
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

public class InterpretationCommentEditFragmentPresenterImpl implements InterpretationCommentEditFragmentPresenter {
    private static final String TAG = InterpretationCommentEditFragmentPresenterImpl.class.getSimpleName();
    private final InterpretationCommentInteractor interpretationCommentInteractor;

    private InterpretationCommentEditFragmentView interpretationCommentEditFragmentView;

    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    private CompositeSubscription subscription;

    public InterpretationCommentEditFragmentPresenterImpl(InterpretationCommentInteractor interpretationCommentInteractor,
                                                          ApiExceptionHandler apiExceptionHandler,
                                                          Logger logger) {
        this.interpretationCommentInteractor = interpretationCommentInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    public void attachView(View view) {
        isNull(view, "interpretationCommentsFragmentView must not be null");
        interpretationCommentEditFragmentView = (InterpretationCommentEditFragmentView) view;

    }

    @Override
    public void detachView() {
        interpretationCommentEditFragmentView = null;
    }

    @Override
    public void updateInterpretationComment(final InterpretationComment interpretationComment, String text){

        interpretationComment.updateComment(text);
        Observable<Boolean> success =  interpretationCommentInteractor.save(interpretationComment);
        success.subscribeOn(Schedulers.newThread());
        success.observeOn(AndroidSchedulers.mainThread());
        success.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean success) {
                logger.d(TAG ,"onUpdateInterpretationComment " + success.toString());
                // save interpretationComment
                interpretationCommentEditFragmentView.updateCommentCallback();

            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onUpdateInterpretationComment failed");
                handleError(throwable);
            }
        });
    }

    @Override
    public void getInterpretationComment(final String interpretaionCommentUId) {

        Observable<InterpretationComment> interpretationComment = interpretationCommentInteractor.get(interpretaionCommentUId);
        interpretationComment.subscribeOn(Schedulers.newThread());
        interpretationComment.observeOn(AndroidSchedulers.mainThread());
        interpretationComment.subscribe(new Action1<InterpretationComment>() {
            @Override
            public void call(InterpretationComment interpretationComment) {
                logger.d(TAG ,"onGetInterpretationComment " + interpretationComment.toString());
                interpretationCommentEditFragmentView.setInterpretationComment(interpretationComment);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onGetInterpretationComment failed");
                handleError(throwable);
            }
        });

    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        interpretationCommentEditFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        interpretationCommentEditFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        interpretationCommentEditFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
