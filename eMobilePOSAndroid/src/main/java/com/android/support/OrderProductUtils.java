package com.android.support;

import android.text.TextUtils;

import com.android.emobilepos.models.orders.OrderProduct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guarionex on 07-07-16.
 */
public class OrderProductUtils {
    public static String getOrderProductQty(List<OrderProduct> orderProducts, String productId) {
        Double retVal = 0d;
        for (OrderProduct orderProduct : orderProducts) {
            if (orderProduct.getProd_id().equalsIgnoreCase(productId)) {
                if (!TextUtils.isEmpty(orderProduct.getOrdprod_qty())) {
                    if (orderProduct.isReturned())
                        retVal -= Double.parseDouble(orderProduct.getOrdprod_qty());
                    else
                        retVal += Double.parseDouble(orderProduct.getOrdprod_qty());
                }
            }
        }
        return String.valueOf(retVal);
    }

    public static HashMap<String, String> getProductQtyHashMap(List<OrderProduct> orderProducts) {
        HashMap<String, String> hashMap = new HashMap<>(orderProducts.size());
        for (OrderProduct orderProduct : orderProducts) {
            if (hashMap.containsKey(orderProduct.getProd_id())) {
                String qty = hashMap.get(orderProduct.getProd_id());
                qty = String.valueOf((Double.valueOf(qty) + Double.valueOf(orderProduct.getOrdprod_qty())));
                hashMap.put(orderProduct.getProd_id(), qty);
            } else {
                hashMap.put(orderProduct.getProd_id(), orderProduct.getOrdprod_qty());
            }

        }
        return hashMap;
    }

    public static List<OrderProduct> getOrderProducts(List<OrderProduct> orderProducts, String productId) {
        List<OrderProduct> list = new ArrayList<>();
        for (OrderProduct orderProduct : orderProducts) {
            if (orderProduct.getProd_id().equalsIgnoreCase(productId)) {
                list.add(orderProduct);
            }
        }
        return list;
    }

    public static List<OrderProduct> getOrderProductsByOrderProductId(List<OrderProduct> orderProducts, String orderProductId) {
        List<OrderProduct> list = new ArrayList<>();
        for (OrderProduct orderProduct : orderProducts) {
            if (orderProduct.getOrdprod_id().equalsIgnoreCase(orderProductId)) {
                list.add(orderProduct);
            }
        }
        return list;
    }

    public static List<OrderProduct> getOrderProductsGroupBySKU(List<OrderProduct> orderProducts) {
        HashMap<String, OrderProduct> hashMap = new HashMap<>();
        List<OrderProduct> list = new ArrayList<>();
        for (OrderProduct orderProduct : orderProducts) {
            if (hashMap.containsKey(orderProduct.getProd_id())) {
                double ordprod_qty = Double.parseDouble(hashMap.get(orderProduct.getProd_id()).getOrdprod_qty());
                ordprod_qty += Double.parseDouble(orderProduct.getOrdprod_qty());
                hashMap.get(orderProduct.getProd_id()).setOrdprod_qty(String.valueOf(ordprod_qty));
            } else {
                hashMap.put(orderProduct.getProd_id(), orderProduct);
            }
        }
        for (Map.Entry<String, OrderProduct> stringOrderProductEntry : hashMap.entrySet()) {
            OrderProduct orderProduct = stringOrderProductEntry.getValue();
            list.add(orderProduct);
        }
        return list;
    }

    public static void assignAddonsOrderProduct(List<OrderProduct> orderProducts) {
        HashMap<String, OrderProduct> parents = new HashMap<>();
        for (OrderProduct product : orderProducts) {
            if (TextUtils.isEmpty(product.getAddon_ordprod_id())) {
                parents.put(product.getOrdprod_id(), product);
            }
        }
        for (OrderProduct product : orderProducts) {
            if (!TextUtils.isEmpty(product.getAddon_ordprod_id())) {
                OrderProduct parentProd = parents.get(product.getAddon_ordprod_id());
                if (parentProd != null) {
                    parentProd.addonsProducts.add(product);
                }
            }
        }
        orderProducts.clear();
        orderProducts.addAll(parents.values());

    }
}
