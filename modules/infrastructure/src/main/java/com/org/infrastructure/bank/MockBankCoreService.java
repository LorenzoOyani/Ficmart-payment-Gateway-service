package com.org.infrastructure.bank;

import com.org.domain.dto.BankAuthorizationCmd;
import com.org.domain.dto.BankCaptureResult;
import com.org.domain.dto.BankRefundResult;
import com.org.domain.enums.PaymentStatus;
import com.org.domain.model.Account;
import com.org.domain.model.BankAuthorization;
import com.org.domain.model.BankAuthorizeCmd;
import com.org.domain.dto.BankAuthorizeResult;
import com.org.domain.model.Transaction;
import com.org.persistence.entities.AccountEntity;
import com.org.persistence.entities.TransactionEntity;
import com.org.persistence.mapper.AccountMapper;
import com.org.persistence.mapper.BankAuthorizationMapper;
import com.org.persistence.mapper.TransactionMapper;
import com.org.persistence.repository.AccountRepository;
import com.org.persistence.repository.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MockBankCoreService {

    private static final Logger logger = Logger.getLogger(MockBankCoreService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final BankAuthorizationRepository bankAuthorizationRepository;
    private final AccountMapper accountMapper;
    private final BankAuthorizationMapper bankAuthorizationMapper;
    private final TransactionMapper transactionMapper;


    private final BankAuthorizationCmd cmd;


    @Transactional
    public BankAuthorizeResult authorize(BankAuthorizeCmd cmd) {
        AccountEntity account = accountRepository.findByCustomerId(cmd.customerId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));


        final String AUTH_ID = "auth_" + UUID.randomUUID();

        Account accountModel = accountMapper.AccountEntityMapper(account);


        try {

            if (accountModel.getBalanceCents() < cmd.amount()) {
                new BankAuthorizeResult(false, null, "INSUFFICIENT_FUNDS");
            }
            BankAuthorization bankAuthorization = BankAuthorization.authorize(
                    AUTH_ID,
                    cmd.orderId(),
                    cmd.customerId(),
                    cmd.amount()
            );

            com.org.persistence.entities.BankAuthorization bankAuthorization1 = bankAuthorizationMapper.bankAuthMapper(bankAuthorization);

            bankAuthorizationRepository.save(bankAuthorization1);
            return new BankAuthorizeResult(true, AUTH_ID, "AUTHORIZED");

        } catch (DataIntegrityViolationException e) {
            com.org.persistence.entities.BankAuthorization bankAuthorization = bankAuthorizationRepository.findByOrderIdAndCustomerId(
                    cmd.orderId(), cmd.customerId()
            ).orElseThrow(() -> new DataIntegrityViolationException(e.getMessage()));

            return new BankAuthorizeResult(true, bankAuthorization.getAuthId(), null);
        }
    }

    @Transactional
    public BankCaptureResult capture(String authId){
        com.org.persistence.entities.BankAuthorization auth = bankAuthorizationRepository
                .findByAuthId(authId).orElseThrow();


        if(auth.getStatus() != auth.status()){
            return new BankCaptureResult(false, null, "BANK_UNAUTHORIZED");
        }


        Optional<AccountEntity> account = Optional.ofNullable(accountRepository.findByCustomerId(auth.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Account with customer id %s\n does not exist: " + auth.getCustomerId())));


        if(account.isEmpty()){
           return new BankCaptureResult(false, null, "ACCOUNT_NOT_FOUND");
        }

        Account account1 = accountMapper.AccountEntityMapper(account.get());

        account1.debit(auth.getAmountCents());

        logger.info(account1.getBalanceCents());

        auth.markCaptured();

        String transactionId = "tx_" + UUID.randomUUID();
        Optional<TransactionEntity> transaction = Optional.of(transactionRepository.findByTransactionId(transactionId)
                .orElseThrow());

        TransactionEntity transaction1 = transaction.orElse(null);

        Transaction tnx = transactionMapper.transactionMapper(transaction1);
        tnx = tnx.capture(authId, cmd);

        TransactionEntity tx = transactionMapper.transactionMappers(tnx);

        transactionRepository.save(tx);


        return new BankCaptureResult(true, "", null);
    }


    @Transactional
    public BankRefundResult refund(String authId) {
        com.org.persistence.entities.BankAuthorization auth = bankAuthorizationRepository.findByAuthId(authId)
                .orElseThrow();

        if (auth.getStatus() != PaymentStatus.AUTHORIZED) {
            return new BankRefundResult(false, null, "BAD_STATE");
        }

        AccountEntity acc = accountRepository.findByCustomerId(auth.getCustomerId())
                .orElseThrow();

        acc.credit(auth.getAmountCents()); /// refunded.

        logger.info(acc.getBalanceCents());

        String txnId = "ref_" + UUID.randomUUID();

        Optional<TransactionEntity> transaction = Optional.of(transactionRepository.findByTransactionId(txnId)
                .orElseThrow());

        TransactionEntity transaction1 = transaction.orElse(null);

        Transaction tnx = transactionMapper.transactionMapper(transaction1);
        tnx = tnx.refund(authId, cmd);

        TransactionEntity tx = transactionMapper.transactionMappers(tnx);

        transactionRepository.save(tx);

        return new BankRefundResult(true, txnId, null);
    }




}
