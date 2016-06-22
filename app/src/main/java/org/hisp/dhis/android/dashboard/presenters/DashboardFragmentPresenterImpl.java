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

import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.core.common.utils.CodeGenerator;
import org.hisp.dhis.client.sdk.models.common.Access;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;
import org.hisp.dhis.client.sdk.utils.Preconditions;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

// TODO Remove getFakeData() and loadData properly
public class DashboardFragmentPresenterImpl implements DashboardFragmentPresenter {
    private static final String TAG = DashboardFragmentPresenterImpl.class.getSimpleName();
    private final DashboardInteractor dashboardInteractor;
    private DashboardFragmentView dashboardFragmentView;

    private final SessionPreferences sessionPreferences;
    private final Logger logger;

    private CompositeSubscription subscription;

    public DashboardFragmentPresenterImpl(DashboardInteractor dashboardInteractor,
                                          SessionPreferences sessionPreferences,
                                          Logger logger) {

        this.dashboardInteractor = dashboardInteractor;
        this.sessionPreferences = sessionPreferences;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        dashboardFragmentView = (DashboardFragmentView) view;
    }

    @Override
    public void detachView() {
        dashboardFragmentView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    //TODO loadDashboardItems() Code using RxAndroid
    @Override
    public void loadLocalDashboardItems(long uId) {
        logger.d(TAG, "LoadDashboardItems()");
        // TODO replace this by actual loading from SDK
        dashboardFragmentView.showDashboardItems(null);
    }

    // TODO Add deleteDashboardItem() method to DashboardInteractor in SDK
    @Override
    public void deleteDashboardItem(DashboardItem dashboardItem) {
//        dashboardInteractor.deleteDashboardItem();
        // TODO syncDashboards() in parentViewPager
    }

    // TODO Add deleteDashboardElement() method to DashboardInteractor in SDK
    @Override
    public void deleteDashboardElement(DashboardElement dashboardElement) {
//        dashboardInteractor.deleteDashboardElement();
        // TODO syncDashboards() in parentViewPager
    }

    // Temporary hack for creating DashboardItems like legacy.
    // TODO Use DashboardInteractor to create new DashboardItem wherever required
    private List<DashboardItem> getFakeData(){
        /**
        List<DashboardItemContent> resources = new Select().from(DashboardItemContent.class)
                .where(generalCondition).queryList();
        Collections.sort(resources, DashboardItemContent.DISPLAY_NAME_COMPARATOR);

        List<OptionAdapterValue> adapterValues = new ArrayList<>();
        for (DashboardItemContent dashboardItemContent : resources) {
            adapterValues.add(new OptionAdapterValue(dashboardItemContent.getUId(),
                    dashboardItemContent.getDisplayName()));
        }
        List<DashboardItem> itemsData = new ArrayList<DashboardItem>() ;
        return itemsData;
         **/

        return null;
    }

}
