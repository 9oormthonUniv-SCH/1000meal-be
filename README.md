# 1000meal

1. Local seeding: copy `.env.local.example` to `.env.local` and fill DB_* values.
2. Run `./scripts/seed_dump.sh base` to regenerate `src/main/resources/R__seed_local.sql` from local DB.
3. Run `./scripts/seed_dump.sh user --email you@example.com` to append one account (and admin profile if exists).
4. Start the app with `SPRING_PROFILES_ACTIVE=local` (or `--spring.profiles.active=local`).
5. Only the local profile enables `spring.sql.init.mode=always`, so shared/prod profiles never seed.
