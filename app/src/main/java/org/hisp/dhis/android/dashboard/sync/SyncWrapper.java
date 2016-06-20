/*
 *  Copyright (c) 2016, University of Oslo
 *
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *  Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *  Neither the name of the HISP project nor the names of its contributors may
 *  be used to endorse or promote products derived from this software without
 *  specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.sync;

import org.hisp.dhis.client.sdk.android.api.utils.DefaultOnSubscribe;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.core.common.utils.ModelUtils;
import org.hisp.dhis.client.sdk.models.common.state.Action;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.models.organisationunit.OrganisationUnit;
import org.hisp.dhis.client.sdk.models.program.Program;
import org.hisp.dhis.client.sdk.models.program.ProgramRule;
import org.hisp.dhis.client.sdk.models.program.ProgramRuleAction;
import org.hisp.dhis.client.sdk.models.program.ProgramRuleVariable;
import org.hisp.dhis.client.sdk.models.program.ProgramStage;
import org.hisp.dhis.client.sdk.models.program.ProgramStageDataElement;
import org.hisp.dhis.client.sdk.models.program.ProgramStageSection;
import org.hisp.dhis.client.sdk.models.program.ProgramType;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

// TODO Write SyncWrapper Code

public class SyncWrapper {

    // metadata
    private final DashboardInteractor dashboardInteractor;

    // data

    public SyncWrapper(
            DashboardInteractor dashboardInteractor
    ) {
        this.dashboardInteractor = dashboardInteractor;
    }

    // TODO code for Syncing data
    public Observable<List<Dashboard>> syncMetaData() {
        /**
        return Observable.zip(
                userOrganisationUnitInteractor.pull(),
                userProgramInteractor.pull(),
                new Func2<List<OrganisationUnit>, List<Program>, List<Program>>() {
                    @Override
                    public List<Program> call(List<OrganisationUnit> units, List<Program> programs) {
                        return programs;
                    }
                })
                .map(new Func1<List<Program>, List<ProgramStageDataElement>>() {
                    @Override
                    public List<ProgramStageDataElement> call(List<Program> programs) {
                        List<Program> programsWithoutRegistration = new ArrayList<>();

                        if (programs != null && !programs.isEmpty()) {
                            for (Program program : programs) {
                                if (ProgramType.WITHOUT_REGISTRATION
                                        .equals(program.getProgramType())) {
                                    programsWithoutRegistration.add(program);
                                }
                            }
                        }

                        List<ProgramStage> programStages =
                                loadProgramStages(programsWithoutRegistration);
                        List<ProgramStageSection> programStageSections =
                                loadProgramStageSections(programStages);
                        List<ProgramRule> programRules =
                                loadProgramRules(programsWithoutRegistration);
                        List<ProgramRuleAction> programRuleActions =
                                loadProgramRuleActions(programRules);
                        List<ProgramRuleVariable> programRuleVariables =
                                loadProgramRuleVariables(programsWithoutRegistration);

                        return loadProgramStageDataElements(programStages, programStageSections);
                    }
                });
         **/

        // Return null for now
        return null;
    }

    public Observable<List<Dashboard>> syncData() {

        /**
        return dashboardInteractor.list()
                .switchMap(new Func1<List<Dashboard>, Observable<List<Dashboard>>>() {
                    @Override
                    public Observable<List<Dashboard>> call(List<Dashboard> dashboards) {
                        Set<String> uids = ModelUtils.toUidSet(dashboards);
                        if (uids != null && !uids.isEmpty()) {
                            return dashboardInteractor.sync(uids);
                        }

                        return Observable.empty();
                    }
                });

        **/

        // Return null for now
        return null;
    }

    public Observable<Boolean> checkIfSyncIsNeeded() {

        /**
         *
        EnumSet<Action> updateActions = EnumSet.of(Action.TO_POST, Action.TO_UPDATE);
        return dashboardInteractor.listByActions(updateActions)
                .switchMap(new Func1<List<Dashboard>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final List<Dashboard> dashboards) {
                        return Observable.create(new DefaultOnSubscribe<Boolean>() {
                            @Override
                            public Boolean call() {
                                return dashboards != null && !dashboards.isEmpty();
                            }
                        });
                    }
                });

        **/

        // Return null for now
        return null;
    }

    // TODO Replace for Dashboards
    public Observable<List<Dashboard>> backgroundSync() {
        /**
        return syncMetaData()
                .subscribeOn(Schedulers.io())
                .switchMap(new Func1<List<ProgramStageDataElement>, Observable<List<Event>>>() {
                    @Override
                    public Observable<List<Event>> call(List<ProgramStageDataElement> programStageDataElements) {
                        if (programStageDataElements != null) {
                            return syncData();
                        }
                        return Observable.empty();
                    }
                });
         **/
        return null;
    }

}
