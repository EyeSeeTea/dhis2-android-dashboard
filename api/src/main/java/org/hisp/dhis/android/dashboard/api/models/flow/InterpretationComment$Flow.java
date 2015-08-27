/*
 * Copyright (c) 2015, dhis2
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.api.models.flow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.Table;

import org.hisp.dhis.android.dashboard.api.models.common.meta.DbDhis;
import org.hisp.dhis.android.dashboard.api.models.common.meta.State;
import org.hisp.dhis.android.dashboard.api.models.interpretation.InterpretationComment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Araz Abishov <araz.abishov.gsoc@gmail.com>.
 */
@Table(databaseName = DbDhis.NAME)
public final class InterpretationComment$Flow extends BaseIdentifiableObject$Flow {

    @Column(name = "text")
    String text;

    @Column
    @ForeignKey(
            references = {
                    @ForeignKeyReference(columnName = "user", columnType = long.class, foreignColumnName = "id")
            }, saveForeignKeyModel = false, onDelete = ForeignKeyAction.CASCADE
    )
    User$Flow user;

    @Column
    @ForeignKey(
            references = {
                    @ForeignKeyReference(columnName = "interpretation", columnType = long.class, foreignColumnName = "id")
            }, saveForeignKeyModel = false, onDelete = ForeignKeyAction.CASCADE
    )
    Interpretation$Flow interpretation;

    @NotNull
    @Column(name = "state")
    State state;

    public InterpretationComment$Flow() {
        state = State.SYNCED;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public User$Flow getUser() {
        return user;
    }

    public void setUser(User$Flow user) {
        this.user = user;
    }

    public Interpretation$Flow getInterpretation() {
        return interpretation;
    }

    public void setInterpretation(Interpretation$Flow interpretation) {
        this.interpretation = interpretation;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public static InterpretationComment$Flow fromModel(InterpretationComment comment) {
        InterpretationComment$Flow commentFlow = new InterpretationComment$Flow();
        commentFlow.setId(comment.getId());
        commentFlow.setUId(comment.getUId());
        commentFlow.setCreated(comment.getCreated());
        commentFlow.setLastUpdated(comment.getLastUpdated());
        commentFlow.setName(comment.getName());
        commentFlow.setDisplayName(comment.getDisplayName());
        commentFlow.setAccess(comment.getAccess());
        return commentFlow;
    }

    public static InterpretationComment toModel(InterpretationComment$Flow commentFlow) {
        InterpretationComment comment = new InterpretationComment();
        comment.setId(commentFlow.getId());
        comment.setUId(commentFlow.getUId());
        comment.setCreated(commentFlow.getCreated());
        comment.setLastUpdated(commentFlow.getLastUpdated());
        comment.setName(commentFlow.getName());
        comment.setDisplayName(commentFlow.getDisplayName());
        comment.setAccess(commentFlow.getAccess());
        return comment;
    }

    public static List<InterpretationComment> toModels(List<InterpretationComment$Flow> commentFlows) {
        List<InterpretationComment> comments = new ArrayList<>();

        if (commentFlows != null && !commentFlows.isEmpty()) {
            for (InterpretationComment$Flow commentFlow : commentFlows) {
                comments.add(toModel(commentFlow));
            }
        }

        return comments;
    }
}
