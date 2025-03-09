package com.alejandro.sec01.publisher;

import com.alejandro.sec01.subscriber.SubscriberImpl;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublisherImpl implements Publisher<String> {

    private static final Logger log = LoggerFactory.getLogger(PublisherImpl.class);

    @Override
    public void subscribe(Subscriber<? super String> s) {
        var subscription = new SubscriptionImpl(s);
        s.onSubscribe(subscription);
    }
}
