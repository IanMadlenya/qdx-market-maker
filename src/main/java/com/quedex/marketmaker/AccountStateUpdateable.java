package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.entities.AccountState;

public interface AccountStateUpdateable {

    void update(AccountState accountState);
}
