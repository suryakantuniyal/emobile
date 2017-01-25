package com.android.dao;

import com.android.emobilepos.models.realms.Device;
import com.android.emobilepos.models.realms.Dimensions;
import com.android.emobilepos.models.realms.DinningTable;
import com.android.emobilepos.models.realms.DinningTableOrder;
import com.android.emobilepos.models.realms.MixMatch;
import com.android.emobilepos.models.realms.Payment;
import com.android.emobilepos.models.realms.PaymentMethod;
import com.android.emobilepos.models.realms.Position;
import com.android.emobilepos.models.realms.ProductAttribute;
import com.android.emobilepos.models.realms.SalesAssociate;
import com.android.emobilepos.models.realms.UOM;
import com.android.emobilepos.models.realms.StoreAndForward;

/**
 * Created by guarionex on 9/28/16.
 */
@io.realm.annotations.RealmModule(classes = {Dimensions.class, Position.class,
        Device.class, SalesAssociate.class, PaymentMethod.class,
        Payment.class, DinningTable.class, DinningTableOrder.class, StoreAndForward.class,
        UOM.class, MixMatch.class, ProductAttribute.class})
public class RealmModule {
}
