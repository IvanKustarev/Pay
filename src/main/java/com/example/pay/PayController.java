package com.example.pay;

import com.example.pay.models.Subscriptions;
import com.example.pay.models.Users;
import com.example.pay.repositories.SubscriptionsRepository;
import com.example.pay.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

@Controller
@RequestMapping("/pay/")
public class PayController {

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/{id}/")
    public String showPay(@PathVariable("id") Long userId, Model model){
        Users user = usersRepository.findById(userId).get();
        model.addAttribute("balance", user.getBalance());
        return "pay";
    }

    @PostMapping("/{id}/topUpBalance")
    public String topUpBalance(@PathVariable("id") Long userId, @RequestParam("cardNumber")String cardNumber, @RequestParam("CVV")Long cvv, @RequestParam("sum") Long sum, Model model){
        Users users = usersRepository.findById(userId).get();
        users.setBalance(users.getBalance() + sum);
        usersRepository.save(users);
        model.addAttribute("message", "Средства начислены!");
        model.addAttribute("toMainURL", "/pay/" + userId + "/");
        return "message";
    }

    @PostMapping("/{id}/paySubscription")
    public String paySubscription(@PathVariable("id") Long userId, @RequestParam("monthsNumber") Integer monthsNumber, Model model){
        Users user = usersRepository.findById(userId).get();
        if(user.getBalance()/5 < monthsNumber){
            model.addAttribute("message", "Недостаточно средст!");
            model.addAttribute("toMainURL", "/pay/" + userId + "/");
            return "message";
        }else {
            user.setBalance(user.getBalance() - monthsNumber*5);
            usersRepository.save(user);
            Iterator<Subscriptions> subscriptionsIterator = subscriptionsRepository.findAll().iterator();
            Subscriptions subscriptions = null;
            while (subscriptionsIterator.hasNext()){
                subscriptions = subscriptionsIterator.next();
                if(subscriptions.getUsedBy().equals(userId)){
                    break;
                }
                subscriptions = null;
            }
            if(subscriptions == null){
                subscriptions = new Subscriptions();
                subscriptions.setStartTime(new Date().getTime());
                subscriptions.setEndTime(new Date().getTime());
            }

            Date dt = new Date(subscriptions.getEndTime());
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            c.add(Calendar.DATE, 30*monthsNumber);
            subscriptions.setEndTime(c.getTime().getTime());
            subscriptionsRepository.save(subscriptions);

            model.addAttribute("message", "Подписка продлена!");
            model.addAttribute("toMainURL", "/pay/" + userId + "/");
            return "message";
        }
    }
}
