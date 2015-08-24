/*
 * Copyright (c) 2015, University of Oslo
 * All rights reserved.
 *
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

package org.hisp.dhis.android.dashboard.api.models.entities.flow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.Table;

import org.hisp.dhis.android.dashboard.api.models.entities.dashboard.Dashboard;
import org.hisp.dhis.android.dashboard.api.models.meta.DbDhis;
import org.hisp.dhis.android.dashboard.api.models.meta.State;

import java.util.ArrayList;
import java.util.List;

@Table(databaseName = DbDhis.NAME)
public final class DashboardFlow extends BaseIdentifiableObjectFlow {

    @Column(name = "state")
    @NotNull
    State state;

    public DashboardFlow() {
        state = State.SYNCED;
    }

    /////////////////////////////////////////////////////////////////////////
    // Getters and setters
    /////////////////////////////////////////////////////////////////////////

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public static DashboardFlow fromModel(Dashboard dashboard) {
        DashboardFlow dashboardFlow = new DashboardFlow();
        dashboardFlow.setId(dashboard.getId());
        dashboardFlow.setCreated(dashboard.getCreated());
        dashboardFlow.setLastUpdated(dashboard.getLastUpdated());
        dashboardFlow.setAccess(dashboard.getAccess());
        dashboardFlow.setName(dashboard.getName());
        dashboardFlow.setDisplayName(dashboard.getDisplayName());
        dashboardFlow.setState(dashboard.getState());
        return dashboardFlow;
    }

    public static Dashboard toModel(DashboardFlow dashboardFlow) {
        Dashboard dashboard = new Dashboard();
        dashboard.setId(dashboardFlow.getId());
        dashboard.setCreated(dashboardFlow.getCreated());
        dashboard.setLastUpdated(dashboardFlow.getLastUpdated());
        dashboard.setAccess(dashboardFlow.getAccess());
        dashboard.setName(dashboardFlow.getName());
        dashboard.setDisplayName(dashboardFlow.getDisplayName());
        dashboard.setState(dashboardFlow.getState());
        return dashboard;
    }

    public static List<Dashboard> toModels(List<DashboardFlow> dashboardFlows) {
        List<Dashboard> dashboards = new ArrayList<>();

        if (dashboardFlows != null && !dashboardFlows.isEmpty()) {
            for (DashboardFlow dashboardFlow : dashboardFlows) {
                dashboards.add(toModel(dashboardFlow));
            }
        }

        return dashboards;
    }
}