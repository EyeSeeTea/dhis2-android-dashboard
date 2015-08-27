package org.hisp.dhis.android.dashboard.api.models.user;

import org.hisp.dhis.android.dashboard.api.models.common.IStore;

/**
 * Created by arazabishov on 8/27/15.
 */
public interface IUserStore extends IStore<User> {
    User query(String uid);
}
