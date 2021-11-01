package com.example.pay;

import com.example.pay.models.Subscriptions;
import com.example.pay.models.Users;
import com.example.pay.repositories.SubscriptionsRepository;
import com.example.pay.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

@Controller
@RequestMapping("/pay")
public class PayController {

    @PostConstruct
    private void startAutoPayer(){
        AutoPay autoPay = new AutoPay(subscriptionsRepository, usersRepository);
        autoPay.start();
    }

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping("/{id}/")
    public String showPay(@PathVariable("id") Long userId, Model model){
        Users user = usersRepository.findById(userId).get();
        model.addAttribute("balance", user.getBalance());
//        Iterator<Subscriptions> subscriptionsIterator = subscriptionsRepository.findAll().iterator();
//        Subscriptions subscriptions = null;
//        while (subscriptionsIterator.hasNext()){
//            subscriptions = subscriptionsIterator.next();
//            if(subscriptions.getUsedBy().equals(userId)){
//                break;
//            }
//            subscriptions = null;
//        }
//        if(subscriptions != null) {
//            String aboutSub = "Автопродление подписки: " + subscriptions.getAutoPay();
//            model.addAttribute("aboutSub", aboutSub);
//        }else {
//
//        }

        addModelAttributes(model, userId);

        return "pay";
    }

    @PostMapping("/{id}/topUpBalance")
    public String topUpBalance(@PathVariable("id") Long userId, @RequestParam("cardNumber")String cardNumber, @RequestParam("CVV")Long cvv, @RequestParam("sum") Long sum, Model model){
        Users users = usersRepository.findById(userId).get();
        users.setBalance(users.getBalance() + sum);
        usersRepository.save(users);
        model.addAttribute("message", "Средства начислены!");
        model.addAttribute("toMainURL", "/pay/" + userId + "/");

        addModelAttributes(model, userId);

        return "message";
    }

    @PostMapping("/{id}/paySubscription")
    public String paySubscription(@PathVariable("id") Long userId, @RequestParam("monthsNumber") Integer monthsNumber, Model model){
        Users user = usersRepository.findById(userId).get();
        if(user.getBalance()/5 < monthsNumber){
            model.addAttribute("message", "Недостаточно средст!");
            model.addAttribute("toMainURL", "/pay/" + userId + "/");

            addModelAttributes(model, userId);

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
                subscriptions.setUsedBy(userId);
                subscriptions.setStartTime(new Date().getTime());
                subscriptions.setEndTime(new Date().getTime());
                subscriptions.setAutoPay(true);
            }
            Date dt = new Date(subscriptions.getEndTime());
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            c.add(Calendar.DATE, 30*monthsNumber);
            subscriptions.setEndTime(c.getTime().getTime());
            subscriptionsRepository.save(subscriptions);

            model.addAttribute("message", "Подписка продлена!");
            model.addAttribute("toMainURL", "/pay/" + userId + "/");

            addModelAttributes(model, userId);

            return "message";
        }
    }

    private Model addModelAttributes(Model model, Long userId){
        model.addAttribute("toAuthorisation", "http://localhost:8081/authorisation/");
        model.addAttribute("toProfile", "http://localhost:8082/profile/" + userId + "/");
        model.addAttribute("toCatalog", "http://localhost:8083/catalog/" + userId + "/");
        model.addAttribute("toSubscription", "http://localhost:8084/subscription/" + userId + "/");
        model.addAttribute("toPay", "http://localhost:8085/pay/" + userId + "/");
        model.addAttribute("toPenalties", "http://localhost:8086/penalties/" + userId + "/");
        return model;
    }
}
