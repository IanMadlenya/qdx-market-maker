package com.quedex.marketmaker;

import com.quedex.marketmaker.qdxapi.entities.Instrument;

import java.util.Collection;

public interface OrderPlacingStrategy {

    Collection<GenericOrder> getOrders(Instrument instrument);
}
