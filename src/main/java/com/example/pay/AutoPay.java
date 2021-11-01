package com.example.pay;

import com.example.pay.models.Subscriptions;
import com.example.pay.models.Users;
import com.example.pay.repositories.SubscriptionsRepository;
import com.example.pay.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class AutoPay extends Thread {

    private SubscriptionsRepository subscriptionsRepository;

    private UsersRepository usersRepository;

    public AutoPay(SubscriptionsRepository subscriptionsRepository, UsersRepository usersRepository){
        this.usersRepository = usersRepository;
        this.subscriptionsRepository = subscriptionsRepository;
    }

    @Override
    public void run() {
        while (true) {
            try {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Iterator<Subscriptions> subscriptionsIterator = subscriptionsRepository.findAll().iterator();
                while (subscriptionsIterator.hasNext()) {
                    Subscriptions subscriptions = subscriptionsIterator.next();
                    if (subscriptions.getAutoPay()) {
                        if (subscriptions.getEndTime() < new Date().getTime()) {
                            Users users = usersRepository.findById(subscriptions.getUsedBy()).get();
                            users.setBalance(users.getBalance() - 5);
                            Date dt = new Date(subscriptions.getEndTime());
                            Calendar c = Calendar.getInstance();
                            c.setTime(dt);
                            c.add(Calendar.DATE, 30);
                            subscriptions.setEndTime(c.getTime().getTime());
                            subscriptionsRepository.save(subscriptions);
                            usersRepository.save(users);
                        }
                    }
                }
            }catch (NullPointerException e){ }
        }
    }
}
