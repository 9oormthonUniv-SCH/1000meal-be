ALTER TABLE meal_usage
  ADD COLUMN menu_group_id BIGINT NULL AFTER store_id;

CREATE INDEX idx_meal_usage_date_store_group
  ON meal_usage (used_date, store_id, menu_group_id);
