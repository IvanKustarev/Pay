package com.example.pay;

import com.example.pay.models.Subscriptions;
import com.example.pay.models.Users;
import com.example.pay.repositories.SubscriptionsRepository;
import com.example.pay.repositories.UsersRepository;
import com.example.pay.tokens.Token;
import com.example.pay.tokens.TokenManager;
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

    @Autowired
    private TokenManager tokenManager;

    @GetMapping("/{token}/")
    public String showPay(@PathVariable("token") String token, Model model){

        if(tokenManager.userIdentify(new Token(token)) == null){
            return "redirect:http://localhost:8080/authorization/";
        }
        long id = new Long(tokenManager.userIdentify(new Token(token)));

        Users user = usersRepository.findById(id).get();
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

        addModelAttributes(model, token);

        return "pay";
    }

    @PostMapping("/{token}/topUpBalance")
    public String topUpBalance(@PathVariable("token") String token, @RequestParam("cardNumber")String cardNumber, @RequestParam("CVV")Long cvv, @RequestParam("sum") Long sum, Model model){

        if(tokenManager.userIdentify(new Token(token)) == null){
            return "redirect:http://localhost:8080/authorization/";
        }
        long id = new Long(tokenManager.userIdentify(new Token(token)));
        Users users = usersRepository.findById(id).get();
        users.setBalance(users.getBalance() + sum);
        usersRepository.save(users);
        model.addAttribute("message", "Средства начислены!");
        model.addAttribute("toMainURL", "/pay/" + token + "/");

        addModelAttributes(model, token);

        return "message";
    }

    @PostMapping("/{token}/paySubscription")
    public String paySubscription(@PathVariable("token") String token, @RequestParam("monthsNumber") Integer monthsNumber, Model model){

        if(tokenManager.userIdentify(new Token(token)) == null){
            return "redirect:http://localhost:8080/authorization/";
        }
        long id = new Long(tokenManager.userIdentify(new Token(token)));

        Users user = usersRepository.findById(id).get();
        if(user.getBalance()/5 < monthsNumber){
            model.addAttribute("message", "Недостаточно средст!");
            model.addAttribute("toMainURL", "/pay/" + token + "/");

            addModelAttributes(model, token);

            return "message";
        }else {
            user.setBalance(user.getBalance() - monthsNumber*5);
            usersRepository.save(user);
            Iterator<Subscriptions> subscriptionsIterator = subscriptionsRepository.findAll().iterator();
            Subscriptions subscriptions = null;
            while (subscriptionsIterator.hasNext()){
                subscriptions = subscriptionsIterator.next();
                if(subscriptions.getUsedBy().equals(id)){
                    break;
                }
                subscriptions = null;
            }
            if(subscriptions == null){
                subscriptions = new Subscriptions();
                subscriptions.setUsedBy(id);
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
            model.addAttribute("toMainURL", "/pay/" + token + "/");

            addModelAttributes(model, token);

            return "message";
        }
    }

    private Model addModelAttributes(Model model, String token){
        model.addAttribute("toAuthorisation", "http://localhost:8080/authorization/");
        model.addAttribute("toProfile", "http://localhost:8080/profile/" + token + "/");
        model.addAttribute("toCatalog", "http://localhost:8080/catalog/" + token + "/");
        model.addAttribute("toSubscription", "http://localhost:8080/subscription/" + token + "/");
        model.addAttribute("toPay", "http://localhost:8080/pay/" + token + "/");
        model.addAttribute("toPenalties", "http://localhost:8080/penalties/" + token + "/");
        return model;
    }
}
