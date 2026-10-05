-- =============================================================================
-- DuoQuest — цели в календаре и настройки нейросети
-- Выполните этот файл в Supabase → SQL Editor после 0001_init.sql.
-- =============================================================================

-- ------------------------------------------------- настройки нейросети ------

-- Ключ вводится прямо в приложении и хранится у пары: оба партнёра им пользуются
-- и оба его видят. Это ключ самого пользователя от его же аккаунта DeepSeek —
-- доступ к строке ограничен политиками пары (см. 0001_init.sql).
alter table public.couples add column if not exists llm_provider text not null default 'deepseek';
alter table public.couples add column if not exists llm_api_key text;
alter table public.couples add column if not exists llm_model text;

-- ------------------------------------------------------------- цели --------

-- Цель — это намерение на конкретный день: легче квеста, без наград,
-- наказаний и проверки партнёром.
create table if not exists public.goals (
  id         uuid primary key default gen_random_uuid(),
  couple_id  uuid not null references public.couples on delete cascade,
  created_by uuid not null references public.profiles on delete cascade,
  -- Чья цель. NULL — общая на двоих.
  user_id    uuid references public.profiles on delete cascade,
  title      text not null,
  note       text,
  goal_date  date not null,
  done_at    timestamptz,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create index if not exists goals_couple_date_idx on public.goals (couple_id, goal_date);

drop trigger if exists goals_touch on public.goals;
create trigger goals_touch before update on public.goals
  for each row execute function public.touch_updated_at();

alter table public.goals enable row level security;

drop policy if exists goals_all on public.goals;
create policy goals_all on public.goals for all
  using (public.is_couple_member(couple_id))
  with check (public.is_couple_member(couple_id));

do $$
begin
  execute 'alter publication supabase_realtime add table public.goals';
exception when duplicate_object then null; end $$;
