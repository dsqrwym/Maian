-- Current sql file was generated after introspecting the database
-- If you want to run this migration please uncomment this code before executing migrations
/*
CREATE SCHEMA "auth";
--> statement-breakpoint
CREATE SCHEMA "extensions";
--> statement-breakpoint
CREATE SCHEMA "graphql";
--> statement-breakpoint
CREATE SCHEMA "graphql_public";
--> statement-breakpoint
CREATE SCHEMA "pgbouncer";
--> statement-breakpoint
CREATE SCHEMA "realtime";
--> statement-breakpoint
CREATE SCHEMA "storage";
--> statement-breakpoint
CREATE SCHEMA "vault";
--> statement-breakpoint
CREATE TYPE "auth"."factor_type" AS ENUM('totp', 'webauthn', 'phone');--> statement-breakpoint
CREATE TYPE "auth"."factor_status" AS ENUM('unverified', 'verified');--> statement-breakpoint
CREATE TYPE "auth"."aal_level" AS ENUM('aal1', 'aal2', 'aal3');--> statement-breakpoint
CREATE TYPE "auth"."code_challenge_method" AS ENUM('s256', 'plain');--> statement-breakpoint
CREATE TYPE "auth"."one_time_token_type" AS ENUM('confirmation_token', 'reauthentication_token', 'recovery_token', 'email_change_token_new', 'email_change_token_current', 'phone_change_token');--> statement-breakpoint
CREATE TYPE "auth"."oauth_registration_type" AS ENUM('dynamic', 'manual');--> statement-breakpoint
CREATE TYPE "realtime"."equality_op" AS ENUM('eq', 'neq', 'lt', 'lte', 'gt', 'gte', 'in');--> statement-breakpoint
CREATE TYPE "realtime"."action" AS ENUM('INSERT', 'UPDATE', 'DELETE', 'TRUNCATE', 'ERROR');--> statement-breakpoint
CREATE TYPE "AddressType" AS ENUM('DELIVERY', 'INVOICE', 'STORE');--> statement-breakpoint
CREATE TYPE "DeliveryStatus" AS ENUM('PENDING', 'IN_PROGRESS', 'COMPLETED');--> statement-breakpoint
CREATE TYPE "UserRole" AS ENUM('WHOLESALER', 'RETAILER', 'SUPPORT', 'DELIVERY', 'WAREHOUSE', 'ADMIN', 'SUPERADMIN');--> statement-breakpoint
CREATE TYPE "UserStatus" AS ENUM('PENDING_VERIFICATION', 'INACTIVE', 'ACTIVE', 'PENDING_REVIEW', 'APPROVED', 'BANNED');--> statement-breakpoint
CREATE TYPE "storage"."buckettype" AS ENUM('STANDARD', 'ANALYTICS');--> statement-breakpoint
CREATE TYPE "auth"."oauth_authorization_status" AS ENUM('pending', 'approved', 'denied', 'expired');--> statement-breakpoint
CREATE TYPE "auth"."oauth_response_type" AS ENUM('code');--> statement-breakpoint
CREATE TYPE "auth"."oauth_client_type" AS ENUM('public', 'confidential');--> statement-breakpoint
CREATE TYPE "SaleVariant" AS ENUM('UNIT', 'BOX', 'PACK');--> statement-breakpoint
CREATE TYPE "ProductStatus" AS ENUM('ACTIVE', 'INACTIVE');--> statement-breakpoint
CREATE SEQUENCE "graphql"."seq_schema_version" INCREMENT BY 1 MINVALUE 1 MAXVALUE 2147483647 START WITH 1 CACHE 1 CYCLE;--> statement-breakpoint
CREATE SEQUENCE "public"."seq_admin_id" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1;--> statement-breakpoint
CREATE SEQUENCE "public"."seq_deliveryman_id" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1;--> statement-breakpoint
CREATE SEQUENCE "public"."seq_retailer_id" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1;--> statement-breakpoint
CREATE SEQUENCE "public"."seq_superadmin_id" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1;--> statement-breakpoint
CREATE SEQUENCE "public"."seq_support_id" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1;--> statement-breakpoint
CREATE SEQUENCE "public"."seq_warehouse_id" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1;--> statement-breakpoint
CREATE SEQUENCE "public"."seq_wholesaler_id" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1;--> statement-breakpoint
CREATE TABLE "auth"."audit_log_entries" (
	"instance_id" uuid,
	"id" uuid PRIMARY KEY,
	"payload" json,
	"created_at" timestamp with time zone,
	"ip_address" varchar(64) DEFAULT '' NOT NULL
);
--> statement-breakpoint
ALTER TABLE "auth"."audit_log_entries" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."custom_oauth_providers" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"provider_type" text NOT NULL,
	"identifier" text NOT NULL CONSTRAINT "custom_oauth_providers_identifier_key" UNIQUE,
	"name" text NOT NULL,
	"client_id" text NOT NULL,
	"client_secret" text NOT NULL,
	"acceptable_client_ids" text[] DEFAULT '{}'::text[] NOT NULL,
	"scopes" text[] DEFAULT '{}'::text[] NOT NULL,
	"pkce_enabled" boolean DEFAULT true NOT NULL,
	"attribute_mapping" jsonb DEFAULT '{}' NOT NULL,
	"authorization_params" jsonb DEFAULT '{}' NOT NULL,
	"enabled" boolean DEFAULT true NOT NULL,
	"email_optional" boolean DEFAULT false NOT NULL,
	"issuer" text,
	"discovery_url" text,
	"skip_nonce_check" boolean DEFAULT false NOT NULL,
	"cached_discovery" jsonb,
	"discovery_cached_at" timestamp with time zone,
	"authorization_url" text,
	"token_url" text,
	"userinfo_url" text,
	"jwks_uri" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	CONSTRAINT "custom_oauth_providers_authorization_url_https" CHECK (((authorization_url IS NULL) OR (authorization_url ~~ 'https://%'::text))),
	CONSTRAINT "custom_oauth_providers_authorization_url_length" CHECK (((authorization_url IS NULL) OR (char_length(authorization_url) <= 2048))),
	CONSTRAINT "custom_oauth_providers_client_id_length" CHECK (((char_length(client_id) >= 1) AND (char_length(client_id) <= 512))),
	CONSTRAINT "custom_oauth_providers_discovery_url_length" CHECK (((discovery_url IS NULL) OR (char_length(discovery_url) <= 2048))),
	CONSTRAINT "custom_oauth_providers_identifier_format" CHECK ((identifier ~ '^[a-z0-9][a-z0-9:-]{0,48}[a-z0-9]$'::text)),
	CONSTRAINT "custom_oauth_providers_issuer_length" CHECK (((issuer IS NULL) OR ((char_length(issuer) >= 1) AND (char_length(issuer) <= 2048)))),
	CONSTRAINT "custom_oauth_providers_jwks_uri_https" CHECK (((jwks_uri IS NULL) OR (jwks_uri ~~ 'https://%'::text))),
	CONSTRAINT "custom_oauth_providers_jwks_uri_length" CHECK (((jwks_uri IS NULL) OR (char_length(jwks_uri) <= 2048))),
	CONSTRAINT "custom_oauth_providers_name_length" CHECK (((char_length(name) >= 1) AND (char_length(name) <= 100))),
	CONSTRAINT "custom_oauth_providers_oauth2_requires_endpoints" CHECK (((provider_type <> 'oauth2'::text) OR ((authorization_url IS NOT NULL) AND (token_url IS NOT NULL) AND (userinfo_url IS NOT NULL)))),
	CONSTRAINT "custom_oauth_providers_oidc_discovery_url_https" CHECK (((provider_type <> 'oidc'::text) OR (discovery_url IS NULL) OR (discovery_url ~~ 'https://%'::text))),
	CONSTRAINT "custom_oauth_providers_oidc_issuer_https" CHECK (((provider_type <> 'oidc'::text) OR (issuer IS NULL) OR (issuer ~~ 'https://%'::text))),
	CONSTRAINT "custom_oauth_providers_oidc_requires_issuer" CHECK (((provider_type <> 'oidc'::text) OR (issuer IS NOT NULL))),
	CONSTRAINT "custom_oauth_providers_provider_type_check" CHECK ((provider_type = ANY (ARRAY['oauth2'::text, 'oidc'::text]))),
	CONSTRAINT "custom_oauth_providers_token_url_https" CHECK (((token_url IS NULL) OR (token_url ~~ 'https://%'::text))),
	CONSTRAINT "custom_oauth_providers_token_url_length" CHECK (((token_url IS NULL) OR (char_length(token_url) <= 2048))),
	CONSTRAINT "custom_oauth_providers_userinfo_url_https" CHECK (((userinfo_url IS NULL) OR (userinfo_url ~~ 'https://%'::text))),
	CONSTRAINT "custom_oauth_providers_userinfo_url_length" CHECK (((userinfo_url IS NULL) OR (char_length(userinfo_url) <= 2048)))
);
--> statement-breakpoint
CREATE TABLE "auth"."flow_state" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid,
	"auth_code" text,
	"code_challenge_method" "auth"."code_challenge_method",
	"code_challenge" text,
	"provider_type" text NOT NULL,
	"provider_access_token" text,
	"provider_refresh_token" text,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	"authentication_method" text NOT NULL,
	"auth_code_issued_at" timestamp with time zone,
	"invite_token" text,
	"referrer" text,
	"oauth_client_state_id" uuid,
	"linking_target_id" uuid,
	"email_optional" boolean DEFAULT false NOT NULL
);
--> statement-breakpoint
ALTER TABLE "auth"."flow_state" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."identities" (
	"provider_id" text NOT NULL,
	"user_id" uuid NOT NULL,
	"identity_data" jsonb NOT NULL,
	"provider" text NOT NULL,
	"last_sign_in_at" timestamp with time zone,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	"email" text GENERATED ALWAYS AS (lower((identity_data ->> 'email'::text))) STORED,
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	CONSTRAINT "identities_provider_id_provider_unique" UNIQUE("provider_id","provider")
);
--> statement-breakpoint
ALTER TABLE "auth"."identities" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."instances" (
	"id" uuid PRIMARY KEY,
	"uuid" uuid,
	"raw_base_config" text,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone
);
--> statement-breakpoint
ALTER TABLE "auth"."instances" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."mfa_amr_claims" (
	"session_id" uuid NOT NULL,
	"created_at" timestamp with time zone NOT NULL,
	"updated_at" timestamp with time zone NOT NULL,
	"authentication_method" text NOT NULL,
	"id" uuid,
	CONSTRAINT "amr_id_pk" PRIMARY KEY("id"),
	CONSTRAINT "mfa_amr_claims_session_id_authentication_method_pkey" UNIQUE("session_id","authentication_method")
);
--> statement-breakpoint
ALTER TABLE "auth"."mfa_amr_claims" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."mfa_challenges" (
	"id" uuid PRIMARY KEY,
	"factor_id" uuid NOT NULL,
	"created_at" timestamp with time zone NOT NULL,
	"verified_at" timestamp with time zone,
	"ip_address" inet NOT NULL,
	"otp_code" text,
	"web_authn_session_data" jsonb
);
--> statement-breakpoint
ALTER TABLE "auth"."mfa_challenges" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."mfa_factors" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"friendly_name" text,
	"factor_type" "auth"."factor_type" NOT NULL,
	"status" "auth"."factor_status" NOT NULL,
	"created_at" timestamp with time zone NOT NULL,
	"updated_at" timestamp with time zone NOT NULL,
	"secret" text,
	"phone" text,
	"last_challenged_at" timestamp with time zone CONSTRAINT "mfa_factors_last_challenged_at_key" UNIQUE,
	"web_authn_credential" jsonb,
	"web_authn_aaguid" uuid,
	"last_webauthn_challenge_data" jsonb
);
--> statement-breakpoint
ALTER TABLE "auth"."mfa_factors" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."oauth_authorizations" (
	"id" uuid PRIMARY KEY,
	"authorization_id" text NOT NULL CONSTRAINT "oauth_authorizations_authorization_id_key" UNIQUE,
	"client_id" uuid NOT NULL,
	"user_id" uuid,
	"redirect_uri" text NOT NULL,
	"scope" text NOT NULL,
	"state" text,
	"resource" text,
	"code_challenge" text,
	"code_challenge_method" "auth"."code_challenge_method",
	"response_type" "auth"."oauth_response_type" DEFAULT 'code'::"auth"."oauth_response_type" NOT NULL,
	"status" "auth"."oauth_authorization_status" DEFAULT 'pending'::"auth"."oauth_authorization_status" NOT NULL,
	"authorization_code" text CONSTRAINT "oauth_authorizations_authorization_code_key" UNIQUE,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"expires_at" timestamp with time zone DEFAULT (now() + '00:03:00'::interval) NOT NULL,
	"approved_at" timestamp with time zone,
	"nonce" text,
	CONSTRAINT "oauth_authorizations_authorization_code_length" CHECK ((char_length(authorization_code) <= 255)),
	CONSTRAINT "oauth_authorizations_code_challenge_length" CHECK ((char_length(code_challenge) <= 128)),
	CONSTRAINT "oauth_authorizations_expires_at_future" CHECK ((expires_at > created_at)),
	CONSTRAINT "oauth_authorizations_nonce_length" CHECK ((char_length(nonce) <= 255)),
	CONSTRAINT "oauth_authorizations_redirect_uri_length" CHECK ((char_length(redirect_uri) <= 2048)),
	CONSTRAINT "oauth_authorizations_resource_length" CHECK ((char_length(resource) <= 2048)),
	CONSTRAINT "oauth_authorizations_scope_length" CHECK ((char_length(scope) <= 4096)),
	CONSTRAINT "oauth_authorizations_state_length" CHECK ((char_length(state) <= 4096))
);
--> statement-breakpoint
CREATE TABLE "auth"."oauth_client_states" (
	"id" uuid PRIMARY KEY,
	"provider_type" text NOT NULL,
	"code_verifier" text,
	"created_at" timestamp with time zone NOT NULL
);
--> statement-breakpoint
CREATE TABLE "auth"."oauth_clients" (
	"id" uuid PRIMARY KEY,
	"client_secret_hash" text,
	"registration_type" "auth"."oauth_registration_type" NOT NULL,
	"redirect_uris" text NOT NULL,
	"grant_types" text NOT NULL,
	"client_name" text,
	"client_uri" text,
	"logo_uri" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	"deleted_at" timestamp with time zone,
	"client_type" "auth"."oauth_client_type" DEFAULT 'confidential'::"auth"."oauth_client_type" NOT NULL,
	"token_endpoint_auth_method" text NOT NULL,
	CONSTRAINT "oauth_clients_client_name_length" CHECK ((char_length(client_name) <= 1024)),
	CONSTRAINT "oauth_clients_client_uri_length" CHECK ((char_length(client_uri) <= 2048)),
	CONSTRAINT "oauth_clients_logo_uri_length" CHECK ((char_length(logo_uri) <= 2048)),
	CONSTRAINT "oauth_clients_token_endpoint_auth_method_check" CHECK ((token_endpoint_auth_method = ANY (ARRAY['client_secret_basic'::text, 'client_secret_post'::text, 'none'::text])))
);
--> statement-breakpoint
CREATE TABLE "auth"."oauth_consents" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"client_id" uuid NOT NULL,
	"scopes" text NOT NULL,
	"granted_at" timestamp with time zone DEFAULT now() NOT NULL,
	"revoked_at" timestamp with time zone,
	CONSTRAINT "oauth_consents_user_client_unique" UNIQUE("user_id","client_id"),
	CONSTRAINT "oauth_consents_revoked_after_granted" CHECK (((revoked_at IS NULL) OR (revoked_at >= granted_at))),
	CONSTRAINT "oauth_consents_scopes_length" CHECK ((char_length(scopes) <= 2048)),
	CONSTRAINT "oauth_consents_scopes_not_empty" CHECK ((char_length(TRIM(BOTH FROM scopes)) > 0))
);
--> statement-breakpoint
CREATE TABLE "auth"."one_time_tokens" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"token_type" "auth"."one_time_token_type" NOT NULL,
	"token_hash" text NOT NULL,
	"relates_to" text NOT NULL,
	"created_at" timestamp DEFAULT now() NOT NULL,
	"updated_at" timestamp DEFAULT now() NOT NULL,
	CONSTRAINT "one_time_tokens_token_hash_check" CHECK ((char_length(token_hash) > 0))
);
--> statement-breakpoint
ALTER TABLE "auth"."one_time_tokens" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."refresh_tokens" (
	"instance_id" uuid,
	"id" bigserial PRIMARY KEY,
	"token" varchar(255) CONSTRAINT "refresh_tokens_token_unique" UNIQUE,
	"user_id" varchar(255),
	"revoked" boolean,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	"parent" varchar(255),
	"session_id" uuid
);
--> statement-breakpoint
ALTER TABLE "auth"."refresh_tokens" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."saml_providers" (
	"id" uuid PRIMARY KEY,
	"sso_provider_id" uuid NOT NULL,
	"entity_id" text NOT NULL CONSTRAINT "saml_providers_entity_id_key" UNIQUE,
	"metadata_xml" text NOT NULL,
	"metadata_url" text,
	"attribute_mapping" jsonb,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	"name_id_format" text,
	CONSTRAINT "entity_id not empty" CHECK ((char_length(entity_id) > 0)),
	CONSTRAINT "metadata_url not empty" CHECK (((metadata_url = NULL::text) OR (char_length(metadata_url) > 0))),
	CONSTRAINT "metadata_xml not empty" CHECK ((char_length(metadata_xml) > 0))
);
--> statement-breakpoint
ALTER TABLE "auth"."saml_providers" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."saml_relay_states" (
	"id" uuid PRIMARY KEY,
	"sso_provider_id" uuid NOT NULL,
	"request_id" text NOT NULL,
	"for_email" text,
	"redirect_to" text,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	"flow_state_id" uuid,
	CONSTRAINT "request_id not empty" CHECK ((char_length(request_id) > 0))
);
--> statement-breakpoint
ALTER TABLE "auth"."saml_relay_states" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."schema_migrations" (
	"version" varchar(255) PRIMARY KEY
);
--> statement-breakpoint
ALTER TABLE "auth"."schema_migrations" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."sessions" (
	"id" uuid PRIMARY KEY,
	"user_id" uuid NOT NULL,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	"factor_id" uuid,
	"aal" "auth"."aal_level",
	"not_after" timestamp with time zone,
	"refreshed_at" timestamp,
	"user_agent" text,
	"ip" inet,
	"tag" text,
	"oauth_client_id" uuid,
	"refresh_token_hmac_key" text,
	"refresh_token_counter" bigint,
	"scopes" text,
	CONSTRAINT "sessions_scopes_length" CHECK ((char_length(scopes) <= 4096))
);
--> statement-breakpoint
ALTER TABLE "auth"."sessions" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."sso_domains" (
	"id" uuid PRIMARY KEY,
	"sso_provider_id" uuid NOT NULL,
	"domain" text NOT NULL,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	CONSTRAINT "domain not empty" CHECK ((char_length(domain) > 0))
);
--> statement-breakpoint
ALTER TABLE "auth"."sso_domains" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."sso_providers" (
	"id" uuid PRIMARY KEY,
	"resource_id" text,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	"disabled" boolean,
	CONSTRAINT "resource_id not empty" CHECK (((resource_id = NULL::text) OR (char_length(resource_id) > 0)))
);
--> statement-breakpoint
ALTER TABLE "auth"."sso_providers" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."users" (
	"instance_id" uuid,
	"id" uuid PRIMARY KEY,
	"aud" varchar(255),
	"role" varchar(255),
	"email" varchar(255),
	"encrypted_password" varchar(255),
	"email_confirmed_at" timestamp with time zone,
	"invited_at" timestamp with time zone,
	"confirmation_token" varchar(255),
	"confirmation_sent_at" timestamp with time zone,
	"recovery_token" varchar(255),
	"recovery_sent_at" timestamp with time zone,
	"email_change_token_new" varchar(255),
	"email_change" varchar(255),
	"email_change_sent_at" timestamp with time zone,
	"last_sign_in_at" timestamp with time zone,
	"raw_app_meta_data" jsonb,
	"raw_user_meta_data" jsonb,
	"is_super_admin" boolean,
	"created_at" timestamp with time zone,
	"updated_at" timestamp with time zone,
	"phone" text DEFAULT NULL CONSTRAINT "users_phone_key" UNIQUE,
	"phone_confirmed_at" timestamp with time zone,
	"phone_change" text DEFAULT '',
	"phone_change_token" varchar(255) DEFAULT '',
	"phone_change_sent_at" timestamp with time zone,
	"confirmed_at" timestamp with time zone GENERATED ALWAYS AS (LEAST(email_confirmed_at, phone_confirmed_at)) STORED,
	"email_change_token_current" varchar(255) DEFAULT '',
	"email_change_confirm_status" smallint DEFAULT 0,
	"banned_until" timestamp with time zone,
	"reauthentication_token" varchar(255) DEFAULT '',
	"reauthentication_sent_at" timestamp with time zone,
	"is_sso_user" boolean DEFAULT false NOT NULL,
	"deleted_at" timestamp with time zone,
	"is_anonymous" boolean DEFAULT false NOT NULL,
	CONSTRAINT "users_email_change_confirm_status_check" CHECK (((email_change_confirm_status >= 0) AND (email_change_confirm_status <= 2)))
);
--> statement-breakpoint
ALTER TABLE "auth"."users" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "auth"."webauthn_challenges" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"user_id" uuid,
	"challenge_type" text NOT NULL,
	"session_data" jsonb NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"expires_at" timestamp with time zone NOT NULL,
	CONSTRAINT "webauthn_challenges_challenge_type_check" CHECK ((challenge_type = ANY (ARRAY['signup'::text, 'registration'::text, 'authentication'::text])))
);
--> statement-breakpoint
CREATE TABLE "auth"."webauthn_credentials" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"user_id" uuid NOT NULL,
	"credential_id" bytea NOT NULL,
	"public_key" bytea NOT NULL,
	"attestation_type" text DEFAULT '' NOT NULL,
	"aaguid" uuid,
	"sign_count" bigint DEFAULT 0 NOT NULL,
	"transports" jsonb DEFAULT '[]' NOT NULL,
	"backup_eligible" boolean DEFAULT false NOT NULL,
	"backed_up" boolean DEFAULT false NOT NULL,
	"friendly_name" text DEFAULT '' NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL,
	"last_used_at" timestamp with time zone
);
--> statement-breakpoint
CREATE TABLE "cart_details" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "cart_details_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"cart_id" bigint NOT NULL,
	"variant_products_id" bigint,
	"quantity" integer NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	CONSTRAINT "cart_details_quantity_check" CHECK ((quantity > 0))
);
--> statement-breakpoint
ALTER TABLE "cart_details" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "carts" (
	"id" bigint GENERATED BY DEFAULT AS IDENTITY (sequence name "cart_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"user_id" uuid NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	CONSTRAINT "cart_pkey" PRIMARY KEY("id")
);
--> statement-breakpoint
ALTER TABLE "carts" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "categories" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "categories_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"user_id" uuid,
	"name" varchar(50) NOT NULL,
	"iva" numeric(5,2),
	"parent_id" bigint,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"level" smallint NOT NULL,
	"name_unaccent" text GENERATED ALWAYS AS (immutable_unaccent((name)::text)) STORED,
	"updated_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text),
	"created_by" uuid,
	"updated_by" uuid,
	"version" bigint DEFAULT 1 NOT NULL,
	CONSTRAINT "categories_iva_check" CHECK ((iva >= (0)::numeric)),
	CONSTRAINT "categories_level_check" CHECK (((level >= 1) AND (level <= 3)))
);
--> statement-breakpoint
CREATE TABLE "category_translations" (
	"category_id" bigint,
	"lang_code" varchar(10),
	"name" varchar(50) NOT NULL,
	"name_unaccent" text GENERATED ALWAYS AS (immutable_unaccent((name)::text)) STORED,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text),
	"updated_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text),
	"updated_by" uuid,
	CONSTRAINT "pk_category_translations" PRIMARY KEY("category_id","lang_code")
);
--> statement-breakpoint
CREATE TABLE "chat_panels" (
	"id" bigint GENERATED BY DEFAULT AS IDENTITY (sequence name "chat_panel_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"name" varchar(50) NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	CONSTRAINT "chat_panel_pkey" PRIMARY KEY("id")
);
--> statement-breakpoint
CREATE TABLE "chat_participants" (
	"user_id" uuid,
	"chat_panel_id" bigint,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text),
	CONSTRAINT "chat_participants_pkey" PRIMARY KEY("chat_panel_id","user_id")
);
--> statement-breakpoint
CREATE TABLE "cities" (
	"id" serial PRIMARY KEY,
	"province_id" integer NOT NULL,
	"name" varchar(100) NOT NULL,
	"name_local" varchar(100) NOT NULL
);
--> statement-breakpoint
CREATE TABLE "configurations" (
	"user_id" uuid PRIMARY KEY,
	"language" varchar(10) DEFAULT 'en' NOT NULL,
	"timezone" varchar(32) DEFAULT 'UTC' NOT NULL
);
--> statement-breakpoint
ALTER TABLE "configurations" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "countries" (
	"iso_alpha2" char(2) NOT NULL CONSTRAINT "countries_iso_alpha2_key" UNIQUE,
	"iso_alpha3" char(3) NOT NULL CONSTRAINT "countries_iso_alpha3_key" UNIQUE,
	"iso_numeric" smallint PRIMARY KEY CONSTRAINT "countries_iso_numeric_key" UNIQUE,
	"name" varchar(100) NOT NULL,
	"name_local" varchar(100) NOT NULL,
	"currency_id" smallint
);
--> statement-breakpoint
CREATE TABLE "currencies" (
	"iso_numeric" smallint PRIMARY KEY,
	"iso_alpha3" char(3) NOT NULL CONSTRAINT "currencies_iso_alpha3_key" UNIQUE,
	"symbol" varchar(5) NOT NULL,
	"decimal_digits" smallint NOT NULL
);
--> statement-breakpoint
CREATE TABLE "deliveries" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "deliveries_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"order_id" bigint NOT NULL,
	"delivery_person" uuid,
	"status" "DeliveryStatus" DEFAULT 'PENDING'::"DeliveryStatus" NOT NULL,
	"start_time" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"end_time" timestamp,
	"notes" text NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"updated_at" timestamp,
	"latitude" double precision NOT NULL,
	"longitude" double precision NOT NULL
);
--> statement-breakpoint
ALTER TABLE "deliveries" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "delivery_timeline" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "delivery_timeline_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"delivery_id" bigint,
	"status" "DeliveryStatus" NOT NULL,
	"notes" text,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"latitude" double precision NOT NULL,
	"longitude" double precision NOT NULL
);
--> statement-breakpoint
ALTER TABLE "delivery_timeline" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "directions" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "directions_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"user_id" uuid NOT NULL,
	"type" "AddressType" DEFAULT 'STORE'::"AddressType" NOT NULL,
	"country_iso" smallint NOT NULL,
	"province_id" integer NOT NULL,
	"city_id" integer NOT NULL,
	"street" varchar(200) NOT NULL,
	"zip_code" varchar(10) NOT NULL,
	"latitude" double precision,
	"longitude" double precision,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"updated_at" timestamp
);
--> statement-breakpoint
CREATE TABLE "discounts" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "discounts_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"user_id" uuid NOT NULL,
	"name" varchar NOT NULL,
	"type_value" jsonb NOT NULL,
	"applies_to_all" boolean DEFAULT false NOT NULL,
	"start_date" date NOT NULL,
	"end_date" date NOT NULL,
	"status" smallint DEFAULT 1 NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL
);
--> statement-breakpoint
CREATE TABLE "files" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "files_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"file_name" varchar NOT NULL,
	"file_hash" varchar(64) NOT NULL CONSTRAINT "files_file_hash_key" UNIQUE,
	"mime_type" varchar(128) NOT NULL,
	"file_size" bigint NOT NULL,
	"storage_key" text NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"to_delete" boolean DEFAULT false NOT NULL,
	CONSTRAINT "files_file_name_check" CHECK (((file_name)::text ~* '^[^\\/:\*\?"<>\|]{1,255}\.[a-z0-9]+$'::text))
);
--> statement-breakpoint
CREATE TABLE "message_files" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "message_files_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"message_id" bigint NOT NULL,
	"file_id" bigint NOT NULL,
	"sort" smallint NOT NULL
);
--> statement-breakpoint
CREATE TABLE "messages" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "messages_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"chat_panel_id" bigint NOT NULL,
	"sender_id" uuid DEFAULT '00000000-0000-0000-0000-000000000000',
	"reply_to" bigint DEFAULT -1,
	"content" text,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"is_read" boolean DEFAULT false NOT NULL
);
--> statement-breakpoint
ALTER TABLE "messages" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "notifications" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "notifications_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"user_id" uuid NOT NULL,
	"title" varchar(100) NOT NULL,
	"message" text NOT NULL,
	"type" smallint NOT NULL,
	"is_read" boolean DEFAULT false NOT NULL,
	"click_action" varchar(255) NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL
);
--> statement-breakpoint
ALTER TABLE "notifications" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "order_details" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "order_details_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"order_id" bigint NOT NULL,
	"product_code" varchar(50) NOT NULL,
	"variant_product_id" bigint NOT NULL,
	"quantity" integer NOT NULL,
	"unit_price" numeric(10,2) NOT NULL,
	"unit_price_iva" numeric(10,2) NOT NULL,
	"subtotal" numeric(12,2) NOT NULL,
	"iva" numeric(10,2) NOT NULL,
	"discount_applied" numeric(10,2) NOT NULL,
	"attributes" jsonb NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL
);
--> statement-breakpoint
ALTER TABLE "order_details" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "orders" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "orders_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"retailer_id" uuid,
	"wholesaler_id" uuid,
	"status" smallint DEFAULT 1 NOT NULL,
	"payment_method" smallint NOT NULL,
	"shipping_address" bigint,
	"notes" varchar(500),
	"discount_total" numeric(12,2) NOT NULL,
	"subtotal" numeric(12,2) NOT NULL,
	"total" numeric(12,2) NOT NULL,
	"iva_total" numeric(12,2) NOT NULL,
	"discount_log" jsonb NOT NULL,
	"estimated_date" timestamp,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"updated_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text)
);
--> statement-breakpoint
ALTER TABLE "orders" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "product_categories" (
	"product_id" bigint,
	"category_id" bigint,
	"is_primary" boolean DEFAULT false NOT NULL,
	CONSTRAINT "product_categories_pkey" PRIMARY KEY("product_id","category_id")
);
--> statement-breakpoint
CREATE TABLE "product_translations" (
	"product_id" bigint,
	"lang_code" varchar(10),
	"name" varchar(50) NOT NULL,
	"title" varchar(100),
	"description" text,
	"name_unaccent" text GENERATED ALWAYS AS (immutable_unaccent((name)::text)) STORED,
	"title_unaccent" text GENERATED ALWAYS AS (immutable_unaccent((title)::text)) STORED,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"updated_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text),
	"updated_by" uuid,
	CONSTRAINT "pk_product_translations" PRIMARY KEY("product_id","lang_code")
);
--> statement-breakpoint
CREATE TABLE "products" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "products_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"user_id" uuid NOT NULL,
	"name" varchar(50) NOT NULL,
	"title" varchar(100),
	"description" text,
	"iva" numeric(5,2) NOT NULL,
	"status" "ProductStatus" DEFAULT 'ACTIVE'::"ProductStatus" NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"product_code" varchar(50) NOT NULL,
	"updated_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text),
	"created_by" uuid NOT NULL,
	"updated_by" uuid,
	"name_unaccent" text GENERATED ALWAYS AS (immutable_unaccent((name)::text)) STORED,
	"title_unaccent" text GENERATED ALWAYS AS (immutable_unaccent((title)::text)) STORED,
	"version" bigint DEFAULT 1 NOT NULL,
	CONSTRAINT "products_iva_check" CHECK ((iva >= (0)::numeric))
);
--> statement-breakpoint
CREATE TABLE "products_files" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "products_files_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"product_id" bigint NOT NULL,
	"file_id" bigint NOT NULL,
	"sort" smallint NOT NULL
);
--> statement-breakpoint
CREATE TABLE "provinces" (
	"id" serial PRIMARY KEY,
	"country_iso" smallint NOT NULL,
	"name" varchar(100) NOT NULL,
	"name_local" varchar(100) NOT NULL
);
--> statement-breakpoint
CREATE TABLE "user_sessions" (
	"session_id" uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
	"user_id" uuid NOT NULL,
	"device_name" varchar(150) NOT NULL,
	"device_finger" varchar(255) NOT NULL,
	"user_agent" text NOT NULL,
	"revoked" boolean DEFAULT false NOT NULL,
	"last_ip" varchar(50) NOT NULL,
	"refresh_token" text,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"last_active" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	CONSTRAINT "unique_device_finger_user" UNIQUE("user_id","device_finger")
);
--> statement-breakpoint
ALTER TABLE "user_sessions" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "user_uploads" (
	"user_id" uuid,
	"file_id" bigint,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	CONSTRAINT "user_uploads_pkey" PRIMARY KEY("user_id","file_id")
);
--> statement-breakpoint
CREATE TABLE "users" (
	"id" uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
	"user_id" text CONSTRAINT "users_user_id_key" UNIQUE,
	"first_name" varchar(50),
	"last_name" varchar(60),
	"username" varchar(50) CONSTRAINT "users_username_key" UNIQUE,
	"password" text NOT NULL,
	"email" varchar(255) NOT NULL CONSTRAINT "users_email_key" UNIQUE,
	"telephone" varchar(25),
	"status" "UserStatus" DEFAULT 'PENDING_VERIFICATION'::"UserStatus" NOT NULL,
	"profile" jsonb,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"updated_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text),
	"role" "UserRole" NOT NULL,
	"cif" varchar(20),
	"updated_by" uuid
);
--> statement-breakpoint
ALTER TABLE "users" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "variant_products" (
	"id" bigint PRIMARY KEY GENERATED BY DEFAULT AS IDENTITY (sequence name "variant_products_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"product_id" bigint NOT NULL,
	"type_sale" "SaleVariant" NOT NULL,
	"price" numeric(10,2) NOT NULL,
	"price_iva" numeric(10,2) NOT NULL,
	"available_stock" integer NOT NULL,
	"sort" smallint NOT NULL,
	"attributes" jsonb,
	"status" "ProductStatus" DEFAULT 'ACTIVE'::"ProductStatus" NOT NULL,
	"product_code" varchar(50) NOT NULL,
	"reserved_stock" integer DEFAULT 0 NOT NULL,
	"low_stock_threshold" integer DEFAULT 0 NOT NULL,
	"sale_unit_qty" integer DEFAULT 1 NOT NULL,
	"min_order_qty" integer DEFAULT 1 NOT NULL,
	"updated_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text),
	"created_by" uuid NOT NULL,
	"updated_by" uuid,
	CONSTRAINT "variant_available_stock_check" CHECK ((available_stock >= 0)),
	CONSTRAINT "variant_low_stock_threshold_check" CHECK ((low_stock_threshold >= 0)),
	CONSTRAINT "variant_min_order_qty_check" CHECK ((min_order_qty >= 1)),
	CONSTRAINT "variant_price_check" CHECK ((price >= (0)::numeric)),
	CONSTRAINT "variant_price_iva_check" CHECK ((price_iva >= (0)::numeric)),
	CONSTRAINT "variant_reserved_stock_check" CHECK ((reserved_stock >= 0)),
	CONSTRAINT "variant_sale_unit_qty_check" CHECK ((sale_unit_qty >= 1)),
	CONSTRAINT "variant_sort_check" CHECK ((sort >= 0))
);
--> statement-breakpoint
CREATE TABLE "verification_tokens" (
	"id" uuid DEFAULT uuid_generate_v4(),
	"user_id" uuid NOT NULL,
	"token" varchar(255) NOT NULL,
	"expires_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"is_used" boolean DEFAULT false NOT NULL,
	"created_at" timestamp DEFAULT (now() AT TIME ZONE 'utc'::text) NOT NULL,
	"attempts" smallint DEFAULT 0 NOT NULL,
	CONSTRAINT "password_reset_tokens_pkey" PRIMARY KEY("id")
);
--> statement-breakpoint
ALTER TABLE "verification_tokens" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "realtime"."messages" (
	"topic" text NOT NULL,
	"extension" text NOT NULL,
	"payload" jsonb,
	"event" text,
	"private" boolean DEFAULT false,
	"updated_at" timestamp DEFAULT now() NOT NULL,
	"inserted_at" timestamp DEFAULT now(),
	"id" uuid DEFAULT gen_random_uuid(),
	CONSTRAINT "messages_pkey" PRIMARY KEY("id","inserted_at")
);
--> statement-breakpoint
ALTER TABLE "realtime"."messages" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "realtime"."schema_migrations" (
	"version" bigint PRIMARY KEY,
	"inserted_at" timestamp(0)
);
--> statement-breakpoint
CREATE TABLE "realtime"."subscription" (
	"id" bigint GENERATED ALWAYS AS IDENTITY (sequence name "realtime"."subscription_id_seq" INCREMENT BY 1 MINVALUE 1 MAXVALUE 9223372036854775807 START WITH 1 CACHE 1),
	"subscription_id" uuid NOT NULL,
	"entity" regclass NOT NULL,
	"filters" realtime.user_defined_filter[] DEFAULT '{}'::realtime.user_defined_filter[] NOT NULL,
	"claims" jsonb NOT NULL,
	"claims_role" regrole GENERATED ALWAYS AS (realtime.to_regrole((claims ->> 'role'::text))) STORED NOT NULL,
	"created_at" timestamp DEFAULT timezone('utc'::text, now()) NOT NULL,
	CONSTRAINT "pk_subscription" PRIMARY KEY("id")
);
--> statement-breakpoint
CREATE TABLE "storage"."buckets" (
	"id" text PRIMARY KEY,
	"name" text NOT NULL,
	"owner" uuid,
	"created_at" timestamp with time zone DEFAULT now(),
	"updated_at" timestamp with time zone DEFAULT now(),
	"public" boolean DEFAULT false,
	"avif_autodetection" boolean DEFAULT false,
	"file_size_limit" bigint,
	"allowed_mime_types" text[],
	"owner_id" text,
	"type" "storage"."buckettype" DEFAULT 'STANDARD'::"storage"."buckettype" NOT NULL
);
--> statement-breakpoint
ALTER TABLE "storage"."buckets" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "storage"."buckets_analytics" (
	"id" text PRIMARY KEY,
	"type" "storage"."buckettype" DEFAULT 'ANALYTICS'::"storage"."buckettype" NOT NULL,
	"format" text DEFAULT 'ICEBERG' NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"updated_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "storage"."buckets_analytics" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "storage"."migrations" (
	"id" integer PRIMARY KEY,
	"name" varchar(100) NOT NULL CONSTRAINT "migrations_name_key" UNIQUE,
	"hash" varchar(40) NOT NULL,
	"executed_at" timestamp DEFAULT CURRENT_TIMESTAMP
);
--> statement-breakpoint
ALTER TABLE "storage"."migrations" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "storage"."objects" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"bucket_id" text,
	"name" text,
	"owner" uuid,
	"created_at" timestamp with time zone DEFAULT now(),
	"updated_at" timestamp with time zone DEFAULT now(),
	"last_accessed_at" timestamp with time zone DEFAULT now(),
	"metadata" jsonb,
	"path_tokens" text[] GENERATED ALWAYS AS (string_to_array(name, '/'::text)) STORED,
	"version" text,
	"owner_id" text,
	"user_metadata" jsonb,
	"level" integer
);
--> statement-breakpoint
ALTER TABLE "storage"."objects" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "storage"."prefixes" (
	"bucket_id" text,
	"name" text,
	"level" integer GENERATED ALWAYS AS (storage.get_level(name)) STORED,
	"created_at" timestamp with time zone DEFAULT now(),
	"updated_at" timestamp with time zone DEFAULT now(),
	CONSTRAINT "prefixes_pkey" PRIMARY KEY("bucket_id","level","name")
);
--> statement-breakpoint
ALTER TABLE "storage"."prefixes" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "storage"."s3_multipart_uploads" (
	"id" text PRIMARY KEY,
	"in_progress_size" bigint DEFAULT 0 NOT NULL,
	"upload_signature" text NOT NULL,
	"bucket_id" text NOT NULL,
	"key" text NOT NULL,
	"version" text NOT NULL,
	"owner_id" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL,
	"user_metadata" jsonb
);
--> statement-breakpoint
ALTER TABLE "storage"."s3_multipart_uploads" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "storage"."s3_multipart_uploads_parts" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"upload_id" text NOT NULL,
	"size" bigint DEFAULT 0 NOT NULL,
	"part_number" integer NOT NULL,
	"bucket_id" text NOT NULL,
	"key" text NOT NULL,
	"etag" text NOT NULL,
	"owner_id" text,
	"version" text NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "storage"."s3_multipart_uploads_parts" ENABLE ROW LEVEL SECURITY;--> statement-breakpoint
CREATE TABLE "vault"."secrets" (
	"id" uuid PRIMARY KEY DEFAULT gen_random_uuid(),
	"name" text,
	"description" text DEFAULT '' NOT NULL,
	"secret" text NOT NULL,
	"key_id" uuid,
	"nonce" bytea DEFAULT vault._crypto_aead_det_noncegen(),
	"created_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
	"updated_at" timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);
--> statement-breakpoint
CREATE INDEX "audit_logs_instance_id_idx" ON "auth"."audit_log_entries" ("instance_id");--> statement-breakpoint
CREATE UNIQUE INDEX "confirmation_token_idx" ON "auth"."users" ("confirmation_token") WHERE ((confirmation_token)::text !~ '^[0-9 ]*$'::text);--> statement-breakpoint
CREATE UNIQUE INDEX "email_change_token_current_idx" ON "auth"."users" ("email_change_token_current") WHERE ((email_change_token_current)::text !~ '^[0-9 ]*$'::text);--> statement-breakpoint
CREATE UNIQUE INDEX "email_change_token_new_idx" ON "auth"."users" ("email_change_token_new") WHERE ((email_change_token_new)::text !~ '^[0-9 ]*$'::text);--> statement-breakpoint
CREATE UNIQUE INDEX "reauthentication_token_idx" ON "auth"."users" ("reauthentication_token") WHERE ((reauthentication_token)::text !~ '^[0-9 ]*$'::text);--> statement-breakpoint
CREATE UNIQUE INDEX "recovery_token_idx" ON "auth"."users" ("recovery_token") WHERE ((recovery_token)::text !~ '^[0-9 ]*$'::text);--> statement-breakpoint
CREATE UNIQUE INDEX "users_email_partial_key" ON "auth"."users" ("email") WHERE (is_sso_user = false);--> statement-breakpoint
CREATE INDEX "users_instance_id_email_idx" ON "auth"."users" ("instance_id",lower((email)::text));--> statement-breakpoint
CREATE INDEX "users_instance_id_idx" ON "auth"."users" ("instance_id");--> statement-breakpoint
CREATE INDEX "users_is_anonymous_idx" ON "auth"."users" ("is_anonymous");--> statement-breakpoint
CREATE INDEX "custom_oauth_providers_created_at_idx" ON "auth"."custom_oauth_providers" ("created_at");--> statement-breakpoint
CREATE INDEX "custom_oauth_providers_enabled_idx" ON "auth"."custom_oauth_providers" ("enabled");--> statement-breakpoint
CREATE INDEX "custom_oauth_providers_identifier_idx" ON "auth"."custom_oauth_providers" ("identifier");--> statement-breakpoint
CREATE INDEX "custom_oauth_providers_provider_type_idx" ON "auth"."custom_oauth_providers" ("provider_type");--> statement-breakpoint
CREATE INDEX "factor_id_created_at_idx" ON "auth"."mfa_factors" ("user_id","created_at");--> statement-breakpoint
CREATE UNIQUE INDEX "mfa_factors_user_friendly_name_unique" ON "auth"."mfa_factors" ("friendly_name","user_id") WHERE (TRIM(BOTH FROM friendly_name) <> ''::text);--> statement-breakpoint
CREATE INDEX "mfa_factors_user_id_idx" ON "auth"."mfa_factors" ("user_id");--> statement-breakpoint
CREATE UNIQUE INDEX "unique_phone_factor_per_user" ON "auth"."mfa_factors" ("user_id","phone");--> statement-breakpoint
CREATE INDEX "flow_state_created_at_idx" ON "auth"."flow_state" ("created_at" DESC);--> statement-breakpoint
CREATE INDEX "idx_auth_code" ON "auth"."flow_state" ("auth_code");--> statement-breakpoint
CREATE INDEX "idx_user_id_auth_method" ON "auth"."flow_state" ("user_id","authentication_method");--> statement-breakpoint
CREATE INDEX "identities_email_idx" ON "auth"."identities" ("email" text_pattern_ops);--> statement-breakpoint
CREATE INDEX "identities_user_id_idx" ON "auth"."identities" ("user_id");--> statement-breakpoint
CREATE INDEX "idx_oauth_client_states_created_at" ON "auth"."oauth_client_states" ("created_at");--> statement-breakpoint
CREATE INDEX "mfa_challenge_created_at_idx" ON "auth"."mfa_challenges" ("created_at" DESC);--> statement-breakpoint
CREATE INDEX "oauth_auth_pending_exp_idx" ON "auth"."oauth_authorizations" ("expires_at") WHERE (status = 'pending'::auth.oauth_authorization_status);--> statement-breakpoint
CREATE INDEX "oauth_clients_deleted_at_idx" ON "auth"."oauth_clients" ("deleted_at");--> statement-breakpoint
CREATE INDEX "oauth_consents_active_client_idx" ON "auth"."oauth_consents" ("client_id") WHERE (revoked_at IS NULL);--> statement-breakpoint
CREATE INDEX "oauth_consents_active_user_client_idx" ON "auth"."oauth_consents" ("user_id","client_id") WHERE (revoked_at IS NULL);--> statement-breakpoint
CREATE INDEX "oauth_consents_user_order_idx" ON "auth"."oauth_consents" ("user_id","granted_at" DESC);--> statement-breakpoint
CREATE INDEX "one_time_tokens_relates_to_hash_idx" ON "auth"."one_time_tokens" USING hash ("relates_to");--> statement-breakpoint
CREATE INDEX "one_time_tokens_token_hash_hash_idx" ON "auth"."one_time_tokens" USING hash ("token_hash");--> statement-breakpoint
CREATE UNIQUE INDEX "one_time_tokens_user_id_token_type_key" ON "auth"."one_time_tokens" ("user_id","token_type");--> statement-breakpoint
CREATE INDEX "refresh_tokens_instance_id_idx" ON "auth"."refresh_tokens" ("instance_id");--> statement-breakpoint
CREATE INDEX "refresh_tokens_instance_id_user_id_idx" ON "auth"."refresh_tokens" ("instance_id","user_id");--> statement-breakpoint
CREATE INDEX "refresh_tokens_parent_idx" ON "auth"."refresh_tokens" ("parent");--> statement-breakpoint
CREATE INDEX "refresh_tokens_session_id_revoked_idx" ON "auth"."refresh_tokens" ("session_id","revoked");--> statement-breakpoint
CREATE INDEX "refresh_tokens_updated_at_idx" ON "auth"."refresh_tokens" ("updated_at" DESC);--> statement-breakpoint
CREATE INDEX "saml_providers_sso_provider_id_idx" ON "auth"."saml_providers" ("sso_provider_id");--> statement-breakpoint
CREATE INDEX "saml_relay_states_created_at_idx" ON "auth"."saml_relay_states" ("created_at" DESC);--> statement-breakpoint
CREATE INDEX "saml_relay_states_for_email_idx" ON "auth"."saml_relay_states" ("for_email");--> statement-breakpoint
CREATE INDEX "saml_relay_states_sso_provider_id_idx" ON "auth"."saml_relay_states" ("sso_provider_id");--> statement-breakpoint
CREATE INDEX "sessions_not_after_idx" ON "auth"."sessions" ("not_after" DESC);--> statement-breakpoint
CREATE INDEX "sessions_oauth_client_id_idx" ON "auth"."sessions" ("oauth_client_id");--> statement-breakpoint
CREATE INDEX "sessions_user_id_idx" ON "auth"."sessions" ("user_id");--> statement-breakpoint
CREATE INDEX "user_id_created_at_idx" ON "auth"."sessions" ("user_id","created_at");--> statement-breakpoint
CREATE UNIQUE INDEX "sso_domains_domain_idx" ON "auth"."sso_domains" (lower(domain));--> statement-breakpoint
CREATE INDEX "sso_domains_sso_provider_id_idx" ON "auth"."sso_domains" ("sso_provider_id");--> statement-breakpoint
CREATE UNIQUE INDEX "sso_providers_resource_id_idx" ON "auth"."sso_providers" (lower(resource_id));--> statement-breakpoint
CREATE INDEX "sso_providers_resource_id_pattern_idx" ON "auth"."sso_providers" ("resource_id" text_pattern_ops);--> statement-breakpoint
CREATE INDEX "webauthn_challenges_expires_at_idx" ON "auth"."webauthn_challenges" ("expires_at");--> statement-breakpoint
CREATE INDEX "webauthn_challenges_user_id_idx" ON "auth"."webauthn_challenges" ("user_id");--> statement-breakpoint
CREATE UNIQUE INDEX "webauthn_credentials_credential_id_key" ON "auth"."webauthn_credentials" ("credential_id");--> statement-breakpoint
CREATE INDEX "webauthn_credentials_user_id_idx" ON "auth"."webauthn_credentials" ("user_id");--> statement-breakpoint
CREATE UNIQUE INDEX "categories_name_unique_public" ON "categories" ("name") WHERE (user_id IS NULL);--> statement-breakpoint
CREATE UNIQUE INDEX "categories_user_name_unique_private" ON "categories" ("user_id","name") WHERE (user_id IS NOT NULL);--> statement-breakpoint
CREATE INDEX "idx_categories_name_unaccent" ON "categories" (lower(name_unaccent));--> statement-breakpoint
CREATE INDEX "idx_category_translations_lang_code" ON "category_translations" ("lang_code");--> statement-breakpoint
CREATE INDEX "idx_category_translations_name_unaccent" ON "category_translations" (lower(name_unaccent));--> statement-breakpoint
CREATE INDEX "idx_files_created_at" ON "files" ("created_at");--> statement-breakpoint
CREATE INDEX "idx_message_files_file_id" ON "message_files" ("file_id");--> statement-breakpoint
CREATE INDEX "idx_product_translations_name_unaccent" ON "product_translations" (lower(name_unaccent));--> statement-breakpoint
CREATE INDEX "idx_product_translations_title_unaccent" ON "product_translations" (lower(title_unaccent));--> statement-breakpoint
CREATE INDEX "idx_products_files_file_id" ON "products_files" ("file_id");--> statement-breakpoint
CREATE INDEX "idx_products_files_product_id" ON "products_files" ("product_id");--> statement-breakpoint
CREATE INDEX "idx_products_name_unaccent" ON "products" (lower(name_unaccent));--> statement-breakpoint
CREATE INDEX "idx_products_title_unaccent" ON "products" (lower(title_unaccent));--> statement-breakpoint
CREATE INDEX "password_reset_tokens_token_idx" ON "verification_tokens" ("token");--> statement-breakpoint
CREATE INDEX "password_reset_tokens_user_id_idx" ON "verification_tokens" ("user_id");--> statement-breakpoint
CREATE INDEX "verification_tokens_user_id_token_idx" ON "verification_tokens" ("user_id","token");--> statement-breakpoint
CREATE INDEX "ix_realtime_subscription_entity" ON "realtime"."subscription" ("entity");--> statement-breakpoint
CREATE UNIQUE INDEX "subscription_subscription_id_entity_filters_key" ON "realtime"."subscription" ("subscription_id","entity","filters");--> statement-breakpoint
CREATE UNIQUE INDEX "bname" ON "storage"."buckets" ("name");--> statement-breakpoint
CREATE UNIQUE INDEX "bucketid_objname" ON "storage"."objects" ("bucket_id","name");--> statement-breakpoint
CREATE UNIQUE INDEX "idx_name_bucket_level_unique" ON "storage"."objects" ("name","bucket_id","level");--> statement-breakpoint
CREATE INDEX "idx_objects_bucket_id_name" ON "storage"."objects" ("bucket_id","name");--> statement-breakpoint
CREATE INDEX "idx_objects_lower_name" ON "storage"."objects" (path_tokens[level],lower(name) text_pattern_ops,"bucket_id","level");--> statement-breakpoint
CREATE INDEX "name_prefix_search" ON "storage"."objects" ("name" text_pattern_ops);--> statement-breakpoint
CREATE UNIQUE INDEX "objects_bucket_id_level_idx" ON "storage"."objects" ("bucket_id","level","name");--> statement-breakpoint
CREATE INDEX "idx_multipart_uploads_list" ON "storage"."s3_multipart_uploads" ("bucket_id","key","created_at");--> statement-breakpoint
CREATE INDEX "idx_prefixes_lower_name" ON "storage"."prefixes" ("bucket_id","level",(string_to_array(name, '/'::text))[level],lower(name) text_pattern_ops);--> statement-breakpoint
CREATE UNIQUE INDEX "secrets_name_idx" ON "vault"."secrets" ("name") WHERE (name IS NOT NULL);--> statement-breakpoint
ALTER TABLE "auth"."refresh_tokens" ADD CONSTRAINT "refresh_tokens_session_id_fkey" FOREIGN KEY ("session_id") REFERENCES "auth"."sessions"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "storage"."objects" ADD CONSTRAINT "objects_bucketId_fkey" FOREIGN KEY ("bucket_id") REFERENCES "storage"."buckets"("id");--> statement-breakpoint
ALTER TABLE "auth"."identities" ADD CONSTRAINT "identities_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."sessions" ADD CONSTRAINT "sessions_oauth_client_id_fkey" FOREIGN KEY ("oauth_client_id") REFERENCES "auth"."oauth_clients"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."sessions" ADD CONSTRAINT "sessions_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."mfa_factors" ADD CONSTRAINT "mfa_factors_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."mfa_challenges" ADD CONSTRAINT "mfa_challenges_auth_factor_id_fkey" FOREIGN KEY ("factor_id") REFERENCES "auth"."mfa_factors"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."mfa_amr_claims" ADD CONSTRAINT "mfa_amr_claims_session_id_fkey" FOREIGN KEY ("session_id") REFERENCES "auth"."sessions"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."sso_domains" ADD CONSTRAINT "sso_domains_sso_provider_id_fkey" FOREIGN KEY ("sso_provider_id") REFERENCES "auth"."sso_providers"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."saml_providers" ADD CONSTRAINT "saml_providers_sso_provider_id_fkey" FOREIGN KEY ("sso_provider_id") REFERENCES "auth"."sso_providers"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."saml_relay_states" ADD CONSTRAINT "saml_relay_states_flow_state_id_fkey" FOREIGN KEY ("flow_state_id") REFERENCES "auth"."flow_state"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."saml_relay_states" ADD CONSTRAINT "saml_relay_states_sso_provider_id_fkey" FOREIGN KEY ("sso_provider_id") REFERENCES "auth"."sso_providers"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."one_time_tokens" ADD CONSTRAINT "one_time_tokens_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "storage"."s3_multipart_uploads" ADD CONSTRAINT "s3_multipart_uploads_bucket_id_fkey" FOREIGN KEY ("bucket_id") REFERENCES "storage"."buckets"("id");--> statement-breakpoint
ALTER TABLE "storage"."s3_multipart_uploads_parts" ADD CONSTRAINT "s3_multipart_uploads_parts_bucket_id_fkey" FOREIGN KEY ("bucket_id") REFERENCES "storage"."buckets"("id");--> statement-breakpoint
ALTER TABLE "storage"."s3_multipart_uploads_parts" ADD CONSTRAINT "s3_multipart_uploads_parts_upload_id_fkey" FOREIGN KEY ("upload_id") REFERENCES "storage"."s3_multipart_uploads"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "cart_details" ADD CONSTRAINT "cart_details_cart_id_fkey" FOREIGN KEY ("cart_id") REFERENCES "carts"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "cart_details" ADD CONSTRAINT "cart_details_variant_products_id_fkey" FOREIGN KEY ("variant_products_id") REFERENCES "variant_products"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "carts" ADD CONSTRAINT "carts_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "categories" ADD CONSTRAINT "categories_created_by_fkey" FOREIGN KEY ("created_by") REFERENCES "users"("id") ON DELETE SET NULL;--> statement-breakpoint
ALTER TABLE "categories" ADD CONSTRAINT "categories_parent_id_fkey" FOREIGN KEY ("parent_id") REFERENCES "categories"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "categories" ADD CONSTRAINT "categories_updated_by_fkey" FOREIGN KEY ("updated_by") REFERENCES "users"("id") ON DELETE SET NULL;--> statement-breakpoint
ALTER TABLE "categories" ADD CONSTRAINT "categories_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "chat_participants" ADD CONSTRAINT "chat_participants_chat_panel_id_fkey" FOREIGN KEY ("chat_panel_id") REFERENCES "chat_panels"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "chat_participants" ADD CONSTRAINT "chat_participants_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "configurations" ADD CONSTRAINT "configurations_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "deliveries" ADD CONSTRAINT "deliveries_delivery_person_fkey" FOREIGN KEY ("delivery_person") REFERENCES "users"("id") ON DELETE SET NULL;--> statement-breakpoint
ALTER TABLE "delivery_timeline" ADD CONSTRAINT "delivery_timeline_delivery_id_fkey" FOREIGN KEY ("delivery_id") REFERENCES "deliveries"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "discounts" ADD CONSTRAINT "discounts_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "message_files" ADD CONSTRAINT "message_files_file_id_fkey" FOREIGN KEY ("file_id") REFERENCES "files"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "message_files" ADD CONSTRAINT "message_files_message_id_fkey" FOREIGN KEY ("message_id") REFERENCES "messages"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "messages" ADD CONSTRAINT "messages_chat_panel_id_fkey" FOREIGN KEY ("chat_panel_id") REFERENCES "chat_panels"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "messages" ADD CONSTRAINT "messages_reply_to_fkey" FOREIGN KEY ("reply_to") REFERENCES "messages"("id") ON DELETE SET DEFAULT;--> statement-breakpoint
ALTER TABLE "messages" ADD CONSTRAINT "messages_sender_id_fkey" FOREIGN KEY ("sender_id") REFERENCES "users"("id") ON DELETE SET DEFAULT;--> statement-breakpoint
ALTER TABLE "notifications" ADD CONSTRAINT "notifications_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "order_details" ADD CONSTRAINT "order_details_order_id_fkey" FOREIGN KEY ("order_id") REFERENCES "orders"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "order_details" ADD CONSTRAINT "order_details_variant_product_id_fkey" FOREIGN KEY ("variant_product_id") REFERENCES "variant_products"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "orders" ADD CONSTRAINT "orders_retailer_id_fkey" FOREIGN KEY ("retailer_id") REFERENCES "users"("id") ON DELETE SET NULL;--> statement-breakpoint
ALTER TABLE "orders" ADD CONSTRAINT "orders_shipping_address_fkey" FOREIGN KEY ("shipping_address") REFERENCES "directions"("id") ON DELETE RESTRICT ON UPDATE RESTRICT;--> statement-breakpoint
ALTER TABLE "orders" ADD CONSTRAINT "orders_wholesaler_id_fkey" FOREIGN KEY ("wholesaler_id") REFERENCES "users"("id") ON DELETE SET NULL;--> statement-breakpoint
ALTER TABLE "products" ADD CONSTRAINT "products_created_by_fkey" FOREIGN KEY ("created_by") REFERENCES "users"("id");--> statement-breakpoint
ALTER TABLE "products" ADD CONSTRAINT "products_updated_by_fkey" FOREIGN KEY ("updated_by") REFERENCES "users"("id");--> statement-breakpoint
ALTER TABLE "products" ADD CONSTRAINT "products_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "products_files" ADD CONSTRAINT "products_files_file_id_fkey" FOREIGN KEY ("file_id") REFERENCES "files"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "products_files" ADD CONSTRAINT "products_files_product_id_fkey" FOREIGN KEY ("product_id") REFERENCES "products"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "user_sessions" ADD CONSTRAINT "fk_user_sessions_user_id" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "variant_products" ADD CONSTRAINT "variant_products_created_by_fkey" FOREIGN KEY ("created_by") REFERENCES "users"("id");--> statement-breakpoint
ALTER TABLE "variant_products" ADD CONSTRAINT "variant_products_product_id_fkey" FOREIGN KEY ("product_id") REFERENCES "products"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "variant_products" ADD CONSTRAINT "variant_products_updated_by_fkey" FOREIGN KEY ("updated_by") REFERENCES "users"("id");--> statement-breakpoint
ALTER TABLE "verification_tokens" ADD CONSTRAINT "password_reset_tokens_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "storage"."prefixes" ADD CONSTRAINT "prefixes_bucketId_fkey" FOREIGN KEY ("bucket_id") REFERENCES "storage"."buckets"("id");--> statement-breakpoint
ALTER TABLE "countries" ADD CONSTRAINT "countries_currency_id_fkey" FOREIGN KEY ("currency_id") REFERENCES "currencies"("iso_numeric") ON DELETE RESTRICT ON UPDATE CASCADE;--> statement-breakpoint
ALTER TABLE "provinces" ADD CONSTRAINT "provinces_country_iso_fkey" FOREIGN KEY ("country_iso") REFERENCES "countries"("iso_numeric") ON DELETE CASCADE ON UPDATE CASCADE;--> statement-breakpoint
ALTER TABLE "cities" ADD CONSTRAINT "cities_province_id_fkey" FOREIGN KEY ("province_id") REFERENCES "provinces"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "directions" ADD CONSTRAINT "directions_city_id_fkey" FOREIGN KEY ("city_id") REFERENCES "cities"("id") ON DELETE RESTRICT;--> statement-breakpoint
ALTER TABLE "directions" ADD CONSTRAINT "directions_country_iso_fkey" FOREIGN KEY ("country_iso") REFERENCES "countries"("iso_numeric") ON DELETE RESTRICT ON UPDATE CASCADE;--> statement-breakpoint
ALTER TABLE "directions" ADD CONSTRAINT "directions_province_id_fkey" FOREIGN KEY ("province_id") REFERENCES "provinces"("id") ON DELETE RESTRICT;--> statement-breakpoint
ALTER TABLE "directions" ADD CONSTRAINT "directions_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."oauth_authorizations" ADD CONSTRAINT "oauth_authorizations_client_id_fkey" FOREIGN KEY ("client_id") REFERENCES "auth"."oauth_clients"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."oauth_authorizations" ADD CONSTRAINT "oauth_authorizations_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."oauth_consents" ADD CONSTRAINT "oauth_consents_client_id_fkey" FOREIGN KEY ("client_id") REFERENCES "auth"."oauth_clients"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."oauth_consents" ADD CONSTRAINT "oauth_consents_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "product_categories" ADD CONSTRAINT "product_categories_category_id_fkey" FOREIGN KEY ("category_id") REFERENCES "categories"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "product_categories" ADD CONSTRAINT "product_categories_product_id_fkey" FOREIGN KEY ("product_id") REFERENCES "products"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "category_translations" ADD CONSTRAINT "category_translations_updated_by_fkey" FOREIGN KEY ("updated_by") REFERENCES "users"("id");--> statement-breakpoint
ALTER TABLE "category_translations" ADD CONSTRAINT "fk_category_translations_category_id" FOREIGN KEY ("category_id") REFERENCES "categories"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "product_translations" ADD CONSTRAINT "fk_product_translations_product_id" FOREIGN KEY ("product_id") REFERENCES "products"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "product_translations" ADD CONSTRAINT "product_translations_updated_by_fkey" FOREIGN KEY ("updated_by") REFERENCES "users"("id");--> statement-breakpoint
ALTER TABLE "user_uploads" ADD CONSTRAINT "user_uploads_file_id_fkey" FOREIGN KEY ("file_id") REFERENCES "files"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "user_uploads" ADD CONSTRAINT "user_uploads_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."webauthn_credentials" ADD CONSTRAINT "webauthn_credentials_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;--> statement-breakpoint
ALTER TABLE "auth"."webauthn_challenges" ADD CONSTRAINT "webauthn_challenges_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "auth"."users"("id") ON DELETE CASCADE;--> statement-breakpoint
CREATE VIEW "extensions"."pg_stat_statements" AS (SELECT userid, dbid, toplevel, queryid, query, plans, total_plan_time, min_plan_time, max_plan_time, mean_plan_time, stddev_plan_time, calls, total_exec_time, min_exec_time, max_exec_time, mean_exec_time, stddev_exec_time, rows, shared_blks_hit, shared_blks_read, shared_blks_dirtied, shared_blks_written, local_blks_hit, local_blks_read, local_blks_dirtied, local_blks_written, temp_blks_read, temp_blks_written, shared_blk_read_time, shared_blk_write_time, local_blk_read_time, local_blk_write_time, temp_blk_read_time, temp_blk_write_time, wal_records, wal_fpi, wal_bytes, jit_functions, jit_generation_time, jit_inlining_count, jit_inlining_time, jit_optimization_count, jit_optimization_time, jit_emission_count, jit_emission_time, jit_deform_count, jit_deform_time, stats_since, minmax_stats_since FROM pg_stat_statements(true) pg_stat_statements(userid, dbid, toplevel, queryid, query, plans, total_plan_time, min_plan_time, max_plan_time, mean_plan_time, stddev_plan_time, calls, total_exec_time, min_exec_time, max_exec_time, mean_exec_time, stddev_exec_time, rows, shared_blks_hit, shared_blks_read, shared_blks_dirtied, shared_blks_written, local_blks_hit, local_blks_read, local_blks_dirtied, local_blks_written, temp_blks_read, temp_blks_written, shared_blk_read_time, shared_blk_write_time, local_blk_read_time, local_blk_write_time, temp_blk_read_time, temp_blk_write_time, wal_records, wal_fpi, wal_bytes, jit_functions, jit_generation_time, jit_inlining_count, jit_inlining_time, jit_optimization_count, jit_optimization_time, jit_emission_count, jit_emission_time, jit_deform_count, jit_deform_time, stats_since, minmax_stats_since));--> statement-breakpoint
CREATE VIEW "extensions"."pg_stat_statements_info" AS (SELECT dealloc, stats_reset FROM pg_stat_statements_info() pg_stat_statements_info(dealloc, stats_reset));--> statement-breakpoint
CREATE VIEW "vault"."decrypted_secrets" AS (SELECT id, name, description, secret, convert_from(vault._crypto_aead_det_decrypt(message => decode(secret, 'base64'::text), additional => convert_to(id::text, 'utf8'::name), key_id => 0::bigint, context => '\x7067736f6469756d'::bytea, nonce => nonce), 'utf8'::name) AS decrypted_secret, key_id, nonce, created_at, updated_at FROM vault.secrets s);
*/