package net.quedex.marketmaker;

import net.quedex.api.entities.Instrument;

import java.util.Collection;

public interface OrderPlacingStrategy {

    Collection<GenericOrder> getOrders(Instrument instrument);
}
