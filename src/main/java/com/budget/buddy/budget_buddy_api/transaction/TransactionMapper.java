package com.budget.buddy.budget_buddy_api.transaction;

import com.budget.buddy.budget_buddy_api.generated.model.Transaction;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionCreate;
import com.budget.buddy.budget_buddy_api.generated.model.TransactionUpdate;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.openapitools.jackson.nullable.JsonNullable;

/**
 * Mapper for Transaction entities to DTO models.
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING
)
public interface TransactionMapper {

  @Mapping(target = "type", source = "type", qualifiedByName = "stringToTypeEnum")
  @Mapping(target = "categoryId", source = "categoryId", qualifiedByName = "wrapStringNullable")
  Transaction toTransaction(TransactionEntity entity);

  List<Transaction> toTransactions(List<TransactionEntity> entities);

  @Named("stringToTypeEnum")
  default Transaction.TypeEnum stringToTypeEnum(String value) {
    return value == null ? null : Transaction.TypeEnum.fromValue(value);
  }

  @Named("wrapStringNullable")
  default JsonNullable<String> wrapStringNullable(String value) {
    return value != null ? JsonNullable.of(value) : JsonNullable.undefined();
  }

  TransactionEntity toEntity(TransactionCreate request);

  TransactionEntity toEntity(TransactionUpdate request);
}
