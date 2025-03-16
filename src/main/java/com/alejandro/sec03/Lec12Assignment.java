package com.alejandro.sec03;

import com.alejandro.common.Util;
import com.alejandro.sec03.assignment.StockPriceObserver;
import com.alejandro.sec03.client.ExternalServiceClient;

public class Lec12Assignment {

    public static void main(String[] args) {
        var client = new ExternalServiceClient();
        var subscriber = new StockPriceObserver();
        client.getPriceChanges()
                .subscribe(subscriber);

        Util.sleepSeconds(20);
    }
}
