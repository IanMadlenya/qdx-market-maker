package com.quedex.marketmaker;

import com.quedex.qdxapi.entities.AccountState;

public interface AccountStateUpdateable {

    void update(AccountState accountState);
}
