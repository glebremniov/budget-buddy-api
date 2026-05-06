--liquibase formatted sql

--changeset g.remniov@gmail.com:013-add-category-monthly-budget
ALTER TABLE categories
  ADD COLUMN monthly_budget BIGINT NULL;

--rollback ALTER TABLE categories DROP COLUMN monthly_budget;
