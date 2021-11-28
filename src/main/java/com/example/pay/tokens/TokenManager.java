package com.example.pay.tokens;

import io.jsonwebtoken.*;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.security.PublicKey;

@Service
public class TokenManager  {

    protected static PublicKey publicKey = readPublicKey();

    public String userIdentify(Token token){
        if(check(token)){
            return (String) Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token.toString()).getBody().get("userIdentify");
        }
        return null;
    }

    public boolean check(Token token) {
        try {
            Jwts.parser().setSigningKey(publicKey).parseClaimsJws(token.toString());
            return true;
        } catch (MalformedJwtException e) {
            return false;
        } catch (IllegalArgumentException e) {
            //БОЛЬШЕ ДЛЯ ПРОВЕРКИ, ТК ВЫСКАКИВАЕТ КОГДА ПРИШЁЛ НЕ ВАЛИДНЫЙ КЛЮЧ,
            //ЧТО ОЗНАЧАЕТ ОШУБКУ СО СТОРОНЫ РАЗРАБОТЧИКА
            return false;
        } catch (SignatureException e) {
            e.printStackTrace();
            return false;
        } catch (ExpiredJwtException e) {
            return false;
        }
    }

    private static PublicKey readPublicKey() {
        try {

            try (StringReader stringReader = new StringReader("-----BEGIN PUBLIC KEY-----\n" +
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnHqQgGeepc2EWwujCtbP\n" +
                    "j0u+MK08brVsvlMe1BU7GVFq5481dgDAHp9/38l1EXsc8IHWMtvDkZt8MRCdaW4t\n" +
                    "g/U0bwUXmMd96XlKN3nw1ptyjH5pTUx3HlFAg7oOPplJUSYG2FLR2XZw7W0KdPIO\n" +
                    "50V1u9O4MwKol4bm3m6OtkrVdKQkmksw2zxOu3MqnqbP796BLzTBwN4hpe89PkwC\n" +
                    "w9Pw46Su74Kb+Zci3GMeS3FrOGbgUGAROLbtbgHoCbInXbfsNfFUZXlArhfyvSIY\n" +
                    "JoTle6dR0vQAtbW4QrKZtTGmMdFC8gBNiGhu3K66PQ7FXcvembXddMgUYRwNdMJs\n" +
                    "0wIDAQAB\n" +
                    "-----END PUBLIC KEY-----\n")/*FileReader keyReader = new FileReader(file)*/) {
                PEMParser pemParser = new PEMParser(stringReader/*keyReader*/);
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(pemParser.readObject());
                return converter.getPublicKey(publicKeyInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
