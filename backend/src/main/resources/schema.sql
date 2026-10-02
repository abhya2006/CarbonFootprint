CREATE TABLE IF NOT EXISTS users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL DEFAULT 'USER',
  created_at TIMESTAMP(6) NOT NULL,
  updated_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
);

CREATE TABLE IF NOT EXISTS emission_factors (
  id BIGINT NOT NULL AUTO_INCREMENT,
  category VARCHAR(255) NOT NULL,
  activity_type VARCHAR(255) NOT NULL,
  unit VARCHAR(255) NOT NULL,
  emission_factor DOUBLE NOT NULL,
  source VARCHAR(255),
  source_year INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id),
  UNIQUE KEY uk_factor_category_type_unit (category, activity_type, unit)
);

CREATE TABLE IF NOT EXISTS activities (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  category VARCHAR(255) NOT NULL,
  activity_type VARCHAR(255) NOT NULL,
  quantity DOUBLE NOT NULL,
  unit VARCHAR(255) NOT NULL,
  co2e DOUBLE NOT NULL,
  activity_date DATE NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_activity_user_date (user_id, activity_date),
  KEY idx_activity_category_type (category, activity_type),
  CONSTRAINT fk_activity_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS sustainability_goals (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(255) NOT NULL,
  target_reduction_percentage DOUBLE NOT NULL,
  target_co2e DOUBLE NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  status VARCHAR(32) NOT NULL,
  PRIMARY KEY (id),
  KEY idx_goal_user (user_id),
  CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS organizations (
  id BIGINT NOT NULL AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS organization_members (
  id BIGINT NOT NULL AUTO_INCREMENT,
  organization_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_organization_user (organization_id, user_id),
  CONSTRAINT fk_member_organization FOREIGN KEY (organization_id) REFERENCES organizations(id),
  CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS badges (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255) NOT NULL,
  icon VARCHAR(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_badge_code (code)
);

CREATE TABLE IF NOT EXISTS user_badges (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  badge_id BIGINT NOT NULL,
  awarded_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_badge (user_id, badge_id),
  CONSTRAINT fk_user_badge_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_badge_badge FOREIGN KEY (badge_id) REFERENCES badges(id)
);

CREATE TABLE IF NOT EXISTS weekly_insights (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  week_start DATE NOT NULL,
  week_end DATE NOT NULL,
  total_co2e DOUBLE NOT NULL,
  previous_week_co2e DOUBLE NOT NULL,
  percentage_change DOUBLE NOT NULL,
  top_category VARCHAR(255) NOT NULL,
  recommendation VARCHAR(500) NOT NULL,
  generated_at TIMESTAMP(6) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_weekly_insight_user_start (user_id, week_start),
  CONSTRAINT fk_weekly_insight_user FOREIGN KEY (user_id) REFERENCES users(id)
);