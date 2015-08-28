package org.hisp.dhis.android.dashboard.api.models.user;

import org.hisp.dhis.android.dashboard.api.models.common.IModelsStore;
import org.hisp.dhis.android.dashboard.api.network.SessionManager;
import org.hisp.dhis.android.dashboard.api.persistence.preferences.DateTimeManager;
import org.hisp.dhis.android.dashboard.api.persistence.preferences.LastUpdatedManager;

import java.util.List;

/**
 * Created by arazabishov on 8/27/15.
 */
public class UserAccountService implements IUserAccountService {
    private final IUserAccountStore userAccountStore;
    private final IModelsStore modelsStore;

    public UserAccountService(IUserAccountStore userAccountStore, IModelsStore modelsStore) {
        this.userAccountStore = userAccountStore;
        this.modelsStore = modelsStore;
    }

    @Override
    public UserAccount getCurrentUserAccount() {
        List<UserAccount> userAccounts = userAccountStore.query();
        return userAccounts != null && !userAccounts.isEmpty() ? userAccounts.get(0) : null;
    }

    @Override
    public User toUser(UserAccount userAccount) {
        User user = new User();
        user.setUId(userAccount.getUId());
        user.setAccess(userAccount.getAccess());
        user.setCreated(user.getCreated());
        user.setLastUpdated(userAccount.getLastUpdated());
        user.setName(userAccount.getName());
        user.setDisplayName(userAccount.getDisplayName());
        return user;
    }

    @Override
    public void logOut() {
        LastUpdatedManager.getInstance().delete();
        DateTimeManager.getInstance().delete();
        SessionManager.getInstance().delete();

        // removing all existing data
        modelsStore.deleteAllTables();
    }
}
