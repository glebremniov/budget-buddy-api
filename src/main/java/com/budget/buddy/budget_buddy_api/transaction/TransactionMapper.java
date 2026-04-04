package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_contracts.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_contracts.generated.model.Transaction;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionWrite;
import com.budget.buddy.budget_buddy_contracts.generated.model.TransactionUpdate;
import java.util.Currency;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper
    extends BaseEntityMapper<TransactionEntity, Transaction, TransactionWrite, TransactionUpdate, PaginatedTransactions> {

  default Currency toCurrency(String code) {
    return code != null ? Currency.getInstance(code) : null;
  }

  default String toCurrencyCode(Currency currency) {
    return currency != null ? currency.getCurrencyCode() : null;
  }
}
