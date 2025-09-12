-- V5__rbac_schema.sql
-- RBAC core tables (roles, permissions, role_permissions, user_roles, user_scope)

-- Roles
CREATE TABLE roles (
  id           CHAR(26) PRIMARY KEY,
  name         VARCHAR(50) NOT NULL UNIQUE,

  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at   TIMESTAMPTZ NULL,

  created_by   CHAR(26) NULL,
  updated_by   CHAR(26) NULL,
  deleted_by   CHAR(26) NULL
);

-- Permissions
CREATE TABLE permissions (
  id           CHAR(26) PRIMARY KEY,
  resource     VARCHAR(50) NOT NULL,   -- e.g., USER
  action       VARCHAR(50) NOT NULL,   -- e.g., VIEW, CREATE, UPDATE, DELETE
  scope        VARCHAR(50) NOT NULL,   -- e.g., GLOBAL, COUNTRY, PROVINCE, STUDIO, SELF, ASSIGNED
  effect       VARCHAR(10) NOT NULL DEFAULT 'ALLOW', -- ALLOW or DENY
  qualifiers   JSONB NULL,             -- optional qualifiers

  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  deleted_at   TIMESTAMPTZ NULL,

  created_by   CHAR(26) NULL,
  updated_by   CHAR(26) NULL,
  deleted_by   CHAR(26) NULL
);

CREATE INDEX permissions_res_act_idx ON permissions(resource, action);
CREATE INDEX permissions_scope_idx ON permissions(scope);
CREATE INDEX permissions_qualifiers_gin ON permissions USING GIN (qualifiers);

-- Role ↔ Permission
CREATE TABLE role_permissions (
  id             CHAR(26) PRIMARY KEY,
  role_id        CHAR(26) NOT NULL REFERENCES roles(id),
  permission_id  CHAR(26) NOT NULL REFERENCES permissions(id),

  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by     CHAR(26) NULL,

  UNIQUE(role_id, permission_id)
);

CREATE INDEX role_permissions_role_idx ON role_permissions(role_id);

-- User ↔ Role
CREATE TABLE user_roles (
  id          CHAR(26) PRIMARY KEY,
  user_id     CHAR(26) NOT NULL, -- logical FK to users(id)
  role_id     CHAR(26) NOT NULL REFERENCES roles(id),

  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by  CHAR(26) NULL,

  UNIQUE(user_id, role_id)
);

CREATE INDEX user_roles_user_idx ON user_roles(user_id);

-- User scope assignments
CREATE TABLE user_scope (
  id          CHAR(26) PRIMARY KEY,
  user_id     CHAR(26) NOT NULL,
  scope_type  VARCHAR(50) NOT NULL, -- COUNTRY | PROVINCE | STUDIO
  scope_id    CHAR(26) NOT NULL,

  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by  CHAR(26) NULL,

  UNIQUE(user_id, scope_type, scope_id)
);

CREATE INDEX user_scope_user_type_idx ON user_scope(user_id, scope_type);
