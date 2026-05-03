package com.budget.buddy.budget_buddy_api.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
class TransactionPagingRepositoryImpl implements TransactionPagingRepository {

  private static final String FROM_CLAUSE = """
      FROM transactions t
      LEFT JOIN categories c ON c.id = t.category_id
      """;

  private static final RowMapper<TransactionEntity> ROW_MAPPER = (rs, rowNum) -> {
    var entity = new TransactionEntity();
    entity.setId(rs.getObject("id", UUID.class));
    entity.setCategoryId(rs.getObject("category_id", UUID.class));
    entity.setAmount(rs.getLong("amount"));
    entity.setType(TransactionType.valueOf(rs.getString("type")));
    entity.setCurrency(Currency.getInstance(rs.getString("currency")));
    entity.setDate(rs.getObject("date", LocalDate.class));
    entity.setDescription(rs.getString("description"));
    entity.setOwnerId(rs.getObject("owner_id", UUID.class));
    entity.setVersion(rs.getObject("version", Integer.class));
    entity.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
    entity.setUpdatedAt(rs.getObject("updated_at", OffsetDateTime.class));
    return entity;
  };

  private final JdbcClient jdbc;

  @Override
  public Page<TransactionEntity> findAllByFilter(TransactionFilter filter, Pageable pageable) {
    var dirSql = resolveDirection(pageable) == Direction.ASC ? "ASC" : "DESC";

    var params = new LinkedHashMap<String, Object>();
    var where = buildWhere(filter, params);

    var listSql = "SELECT t.* " + FROM_CLAUSE + "WHERE " + where
        + " ORDER BY t.\"date\" " + dirSql + ", t.created_at " + dirSql
        + " LIMIT :limit OFFSET :offset";

    var entities = jdbc.sql(listSql)
        .params(params)
        .param("limit", pageable.getPageSize())
        .param("offset", pageable.getOffset())
        .query(ROW_MAPPER)
        .list();

    return PageableExecutionUtils.getPage(entities, pageable, () -> jdbc
        .sql("SELECT COUNT(*) " + FROM_CLAUSE + "WHERE " + where)
        .params(params)
        .query(Long.class)
        .single());
  }

  private static Direction resolveDirection(Pageable pageable) {
    return pageable.getSort()
        .stream()
        .filter(o -> TransactionEntity.DATE.equals(o.getProperty()))
        .findFirst()
        .map(Order::getDirection)
        .orElse(Direction.DESC);
  }

  private static String buildWhere(TransactionFilter filter, Map<String, Object> params) {
    var where = new StringBuilder("t.owner_id = :ownerId");
    params.put("ownerId", filter.ownerId());

    if (filter.start() != null) {
      where.append(" AND t.\"date\" >= :start");
      params.put("start", filter.start());
    }
    if (filter.end() != null) {
      where.append(" AND t.\"date\" <= :end");
      params.put("end", filter.end());
    }
    if (filter.categoryId() != null) {
      where.append(" AND t.category_id = :categoryId");
      params.put("categoryId", filter.categoryId());
    }
    if (filter.type() != null) {
      where.append(" AND t.type = :type");
      params.put("type", filter.type().name());
    }
    if (filter.amountMin() != null) {
      where.append(" AND t.amount >= :amountMin");
      params.put("amountMin", filter.amountMin());
    }
    if (filter.amountMax() != null) {
      where.append(" AND t.amount <= :amountMax");
      params.put("amountMax", filter.amountMax());
    }
    if (StringUtils.hasText(filter.query())) {
      where.append(" AND (t.description ILIKE :q ESCAPE '\\' OR c.name ILIKE :q ESCAPE '\\')");
      params.put("q", "%" + escapeLike(filter.query()) + "%");
    }
    return where.toString();
  }

  private static String escapeLike(String value) {
    return value
        .replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_");
  }
}
