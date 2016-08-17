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

import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationCommentsFragmentView;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationCommentInteractor;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationComment;
import org.hisp.dhis.client.sdk.models.user.User;
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

public class InterpretationCommentsFragmentPresenterImpl implements InterpretationCommentsFragmentPresenter {
    private static final String TAG = InterpretationCommentsFragmentPresenterImpl.class.getSimpleName();
    private final InterpretationInteractor interpretationInteractor;
    private final InterpretationCommentInteractor interpretationCommentInteractor;

    private InterpretationCommentsFragmentView interpretationCommentsFragmentView;

    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    private CompositeSubscription subscription;

    public InterpretationCommentsFragmentPresenterImpl(InterpretationInteractor interpretationInteractor,
                                                       InterpretationCommentInteractor interpretationCommentInteractor,
                                                       ApiExceptionHandler apiExceptionHandler,
                                                       Logger logger) {
        this.interpretationInteractor = interpretationInteractor;
        this.interpretationCommentInteractor = interpretationCommentInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    public void attachView(View view) {
        isNull(view, "interpretationCommentsFragmentView must not be null");
        interpretationCommentsFragmentView = (InterpretationCommentsFragmentView) view;

    }

    @Override
    public void detachView() {
        interpretationCommentsFragmentView = null;
    }

    @Override
    public User getUserLocal() {
        return interpretationInteractor.getCurrentUserLocal();
    }

    @Override
    public void addInterpretationComment(final Interpretation interpretation, User user, String text){

        Observable<InterpretationComment> interpretationComment =  interpretationCommentInteractor.create(interpretation,
                user, text);
        interpretationComment.subscribeOn(Schedulers.newThread());
        interpretationComment.observeOn(AndroidSchedulers.mainThread());
        interpretationComment.subscribe(new Action1<InterpretationComment>() {
            @Override
            public void call(InterpretationComment interpretationComment) {
                logger.d(TAG ,"onAddInterpretationComment " + interpretationComment.toString());
                // save interpretationComment
                interpretationCommentInteractor.save(interpretationComment).toBlocking().first();
                interpretationCommentsFragmentView.addCommentCallback(interpretationComment);
                interpretationCommentsFragmentView.uiSync();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onAddInterpretationComment failed");
                handleError(throwable);
            }
        });
    }

    @Override
    public void getInterpretation(final String interpretaionUId) {

        Observable<Interpretation> interpretation =  interpretationInteractor.get(interpretaionUId);
        interpretation.subscribeOn(Schedulers.newThread());
        interpretation.observeOn(AndroidSchedulers.mainThread());
        interpretation.subscribe(new Action1<Interpretation>() {
            @Override
            public void call(Interpretation interpretation) {
                logger.d(TAG ,"onGetInterpretationComment " + interpretation.toString());
                interpretationCommentsFragmentView.setInterpretation(interpretation);
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
    public void getInterpretationComments(String interpretaionUId) {

//        @Override
//        public List<InterpretationComment> query(Context context) {
//            List<InterpretationComment> comments = new Select()
//                    .from(InterpretationComment.class)
//                    .where(Condition.column(InterpretationComment$Table
//                            .INTERPRETATION_INTERPRETATION).is(mInterpretationId))
//                    .and(Condition.column(InterpretationComment$Table
//                            .STATE).isNot(State.TO_DELETE.toString()))
//                    .queryList();
//            Collections.sort(comments, IdentifiableObject.CREATED_COMPARATOR);
//            return comments;
//        }

        Observable<List<InterpretationComment>> interpretationComments = interpretationCommentInteractor.list(interpretaionUId);
        interpretationComments.subscribeOn(Schedulers.newThread());
        interpretationComments.observeOn(AndroidSchedulers.mainThread());
        interpretationComments.subscribe(new Action1<List<InterpretationComment>>() {
            @Override
            public void call(List<InterpretationComment> interpretationComments) {
                logger.d(TAG ,"LoadedInterpretationComments " + interpretationComments.toString());
                Collections.sort(interpretationComments, InterpretationComment.CREATED_COMPARATOR);
                interpretationCommentsFragmentView.setInterpretationComments(interpretationComments);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "LoadedInterpretationComments failed");
                handleError(throwable);
            }
        });
    }

    @Override
    public void deleteInterpretationComment(InterpretationComment comment) {

        Observable<Boolean> interpretationComment =  interpretationCommentInteractor.remove(comment);
        interpretationComment.subscribeOn(Schedulers.newThread());
        interpretationComment.observeOn(AndroidSchedulers.mainThread());
        interpretationComment.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean deleted) {
                logger.d(TAG ,"onDeleteInterpretationComment " + deleted.toString());
                interpretationCommentsFragmentView.uiSync();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onDeleteInterpretationComment failed");
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
                        interpretationCommentsFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        interpretationCommentsFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        interpretationCommentsFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
