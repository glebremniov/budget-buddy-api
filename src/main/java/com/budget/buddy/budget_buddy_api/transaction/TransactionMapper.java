package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.base.BaseEntityMapper;
import com.budget.buddy.budget_buddy_api.generated.model.PaginatedTransactions;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionWrite;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper
    extends BaseEntityMapper<TransactionEntity, Transaction, TransactionWrite, TransactionUpdate, PaginatedTransactions> {

}
