package com.banking.repositories.specifications;

import com.banking.models.entities.Transaction;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.UUID;

public class TransactionSpecs {
    public static Specification<Transaction> accountIdEquals(UUID accountId) {
        return (root, query, builder) ->
            accountId == null ?
            builder.conjunction() :
            builder.or(builder.equal(root.get("toAccount").get("accountId"), accountId), builder.equal(root.get("fromAccount").get("accountId"), accountId));
    }

    public static Specification<Transaction> toAccountIbanEquals(String iban) {
        return (root, query, builder) ->
            iban == null ?
            builder.conjunction() :
            builder.equal(root.get("toAccount").get("iban"), iban);
    }

    public static Specification<Transaction> fromAccountIbanEquals(String iban) {
        return (root, query, builder) ->
            iban == null ?
            builder.conjunction() :
            builder.equal(root.get("fromAccount").get("iban"), iban);
    }

    public static Specification<Transaction> amountEquals(BigDecimal amount) {
        return (root, query, builder) ->
            amount == null ?
            builder.conjunction() :
            builder.equal(root.get("amount"), amount);
    }

    public static Specification<Transaction> amountLessThan(BigDecimal amount) {
        return (root, query, builder) ->
            amount == null ?
            builder.conjunction() :
            builder.lessThan(root.get("amount"), amount);
    }

    public static Specification<Transaction> amountGreaterThan(BigDecimal amount) {
        return (root, query, builder) ->
                amount == null ?
                builder.conjunction() :
                builder.greaterThan(root.get("amount"), amount);
    }
}
