package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.base.crudl.BaseMapper;
import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper
    extends BaseMapper<TransactionEntity, Transaction, TransactionCreate, TransactionUpdate> {

  default String typeEnumToString(TransactionCreate.TypeEnum type) {
    return type == null ? null : type.getValue();
  }

  default String typeEnumToString(TransactionUpdate.TypeEnum type) {
    return type == null ? null : type.getValue();
  }

  default Transaction.TypeEnum stringToTypeEnum(String value) {
    return value == null ? null : Transaction.TypeEnum.fromValue(value);
  }

  default JsonNullable<String> wrapStringNullable(String value) {
    return value != null ? JsonNullable.of(value) : JsonNullable.undefined();
  }
}
