package net.quedex.marketmaker;

import net.quedex.api.entities.AccountState;

public interface AccountStateUpdateable {

    void update(AccountState accountState);
}
