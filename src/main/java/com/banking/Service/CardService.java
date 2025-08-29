package com.banking.Service;


import com.banking.Entity.Card;
import com.banking.Repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CardService {

    @Autowired
    private CardRepository cardRepository;

    public Long getAccountIdByCardNumber(String cardSend){
        Card cardSendEntity = cardRepository.findByCardNumber(cardSend)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thẻ gửi"));
        return cardSendEntity.getAccount().getAccountId();
    }
}
