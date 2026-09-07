-- =============================================================================
-- DuoQuest — схема базы данных
-- Выполните этот файл целиком в Supabase → SQL Editor.
-- =============================================================================

create extension if not exists "pgcrypto";

-- ------------------------------------------------------------------ типы ----
do $$ begin
  create type task_kind as enum ('personal', 'assigned', 'shared');
exception when duplicate_object then null; end $$;

do $$ begin
  create type task_status as enum ('open', 'in_progress', 'submitted', 'completed', 'failed', 'cancelled');
exception when duplicate_object then null; end $$;

do $$ begin
  create type task_difficulty as enum ('trivial', 'easy', 'normal', 'hard', 'epic');
exception when duplicate_object then null; end $$;

do $$ begin
  create type hero_class as enum ('warrior', 'mage', 'rogue', 'bard', 'healer');
exception when duplicate_object then null; end $$;

-- --------------------------------------------------------------- таблицы ----

create table if not exists public.profiles (
  id           uuid primary key references auth.users on delete cascade,
  display_name text not null default 'Игрок',
  avatar_emoji text not null default '🙂',
  push_token   text,
  created_at   timestamptz not null default now()
);

create table if not exists public.couples (
  id          uuid primary key default gen_random_uuid(),
  name        text not null default 'Наша пара',
  invite_code text not null unique,
  created_by  uuid references public.profiles(id) on delete set null,
  created_at  timestamptz not null default now()
);

create table if not exists public.couple_members (
  couple_id uuid not null references public.couples on delete cascade,
  user_id   uuid not null references public.profiles on delete cascade,
  joined_at timestamptz not null default now(),
  primary key (couple_id, user_id)
);

-- Пользователь состоит максимум в одной паре — это упрощает всю модель прав.
create unique index if not exists couple_members_one_couple_per_user on public.couple_members (user_id);

create table if not exists public.characters (
  id           uuid primary key default gen_random_uuid(),
  user_id      uuid not null unique references public.profiles on delete cascade,
  couple_id    uuid references public.couples on delete set null,
  name         text not null default 'Герой',
  hero_class   hero_class not null default 'warrior',
  level        int not null default 1,
  xp           int not null default 0,
  hp           int not null default 100,
  max_hp       int not null default 100,
  gold         int not null default 100,
  strength     int not null default 5,
  agility      int not null default 5,
  wisdom       int not null default 5,
  charisma     int not null default 5,
  streak       int not null default 0,
  avatar_emoji text not null default '🧝',
  bio          text,
  updated_at   timestamptz not null default now()
);

create table if not exists public.tasks (
  id                uuid primary key default gen_random_uuid(),
  couple_id         uuid not null references public.couples on delete cascade,
  created_by        uuid not null references public.profiles on delete cascade,
  title             text not null,
  description       text,
  kind              task_kind not null default 'assigned',
  difficulty        task_difficulty not null default 'normal',
  status            task_status not null default 'open',
  due_at            timestamptz,
  reward_text       text,
  reward_gold       int not null default 50,
  reward_xp         int not null default 100,
  penalty_text      text,
  penalty_hp        int not null default 10,
  penalty_gold      int not null default 25,
  requires_approval boolean not null default true,
  completed_at      timestamptz,
  quest_title       text,
  quest_intro       text,
  created_at        timestamptz not null default now(),
  updated_at        timestamptz not null default now()
);

create index if not exists tasks_couple_idx on public.tasks (couple_id, created_at desc);

create table if not exists public.task_assignees (
  task_id uuid not null references public.tasks on delete cascade,
  user_id uuid not null references public.profiles on delete cascade,
  done_at timestamptz,
  primary key (task_id, user_id)
);

create table if not exists public.checkpoints (
  id         uuid primary key default gen_random_uuid(),
  task_id    uuid not null references public.tasks on delete cascade,
  title      text not null,
  remind_at  timestamptz not null,
  done_at    timestamptz,
  position   int not null default 0,
  created_at timestamptz not null default now()
);

create index if not exists checkpoints_task_idx on public.checkpoints (task_id, remind_at);

create table if not exists public.story_chapters (
  id           uuid primary key default gen_random_uuid(),
  couple_id    uuid not null references public.couples on delete cascade,
  chapter_no   int not null,
  title        text not null,
  body         text not null,
  cliffhanger  text,
  triggered_by uuid references public.tasks on delete set null,
  created_at   timestamptz not null default now(),
  unique (couple_id, chapter_no)
);

create table if not exists public.ledger (
  id          uuid primary key default gen_random_uuid(),
  couple_id   uuid not null references public.couples on delete cascade,
  task_id     uuid references public.tasks on delete set null,
  user_id     uuid not null references public.profiles on delete cascade,
  kind        text not null check (kind in ('reward', 'penalty')),
  text        text not null,
  redeemed_at timestamptz,
  created_at  timestamptz not null default now()
);

create index if not exists ledger_couple_idx on public.ledger (couple_id, created_at desc);

-- -------------------------------------------------------------- триггеры ----

-- Профиль создаётся автоматически при регистрации пользователя.
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
  insert into public.profiles (id, display_name)
  values (
    new.id,
    coalesce(nullif(new.raw_user_meta_data ->> 'display_name', ''), split_part(new.email, '@', 1), 'Игрок')
  )
  on conflict (id) do nothing;
  return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
  after insert on auth.users
  for each row execute function public.handle_new_user();

create or replace function public.touch_updated_at()
returns trigger language plpgsql as $$
begin
  new.updated_at = now();
  return new;
end;
$$;

drop trigger if exists tasks_touch on public.tasks;
create trigger tasks_touch before update on public.tasks
  for each row execute function public.touch_updated_at();

drop trigger if exists characters_touch on public.characters;
create trigger characters_touch before update on public.characters
  for each row execute function public.touch_updated_at();

-- ------------------------------------------------------- функции доступа ----

-- SECURITY DEFINER, чтобы политики на couple_members не ссылались сами на себя.
create or replace function public.is_couple_member(p_couple_id uuid)
returns boolean
language sql
security definer
stable
set search_path = public
as $$
  select exists (
    select 1 from public.couple_members
    where couple_id = p_couple_id and user_id = auth.uid()
  );
$$;

create or replace function public.my_couple_id()
returns uuid
language sql
security definer
stable
set search_path = public
as $$
  select couple_id from public.couple_members where user_id = auth.uid() limit 1;
$$;

-- Вступление в пару по коду: обычным SELECT чужую пару не найти, поэтому SECURITY DEFINER.
create or replace function public.join_couple(p_invite_code text)
returns public.couples
language plpgsql
security definer
set search_path = public
as $$
declare
  v_couple public.couples;
  v_members int;
begin
  if auth.uid() is null then
    raise exception 'Нужно войти в аккаунт';
  end if;

  select * into v_couple from public.couples where invite_code = upper(trim(p_invite_code));
  if v_couple.id is null then
    raise exception 'Пара с таким кодом не найдена';
  end if;

  select count(*) into v_members from public.couple_members where couple_id = v_couple.id;
  if v_members >= 2 and not exists (
    select 1 from public.couple_members where couple_id = v_couple.id and user_id = auth.uid()
  ) then
    raise exception 'В этой паре уже двое';
  end if;

  delete from public.couple_members where user_id = auth.uid();
  insert into public.couple_members (couple_id, user_id)
  values (v_couple.id, auth.uid())
  on conflict do nothing;

  update public.characters set couple_id = v_couple.id where user_id = auth.uid();

  return v_couple;
end;
$$;

grant execute on function public.join_couple(text) to authenticated;

-- ------------------------------------------------------------------ RLS ----

alter table public.profiles       enable row level security;
alter table public.couples        enable row level security;
alter table public.couple_members enable row level security;
alter table public.characters     enable row level security;
alter table public.tasks          enable row level security;
alter table public.task_assignees enable row level security;
alter table public.checkpoints    enable row level security;
alter table public.story_chapters enable row level security;
alter table public.ledger         enable row level security;

-- profiles: свой профиль + профиль партнёра.
drop policy if exists profiles_select on public.profiles;
create policy profiles_select on public.profiles for select using (
  id = auth.uid()
  or exists (
    select 1 from public.couple_members m
    where m.user_id = profiles.id and m.couple_id = public.my_couple_id()
  )
);

drop policy if exists profiles_update on public.profiles;
create policy profiles_update on public.profiles for update using (id = auth.uid()) with check (id = auth.uid());

drop policy if exists profiles_insert on public.profiles;
create policy profiles_insert on public.profiles for insert with check (id = auth.uid());

-- couples: только своя пара; создать может любой авторизованный.
drop policy if exists couples_select on public.couples;
-- created_by нужен отдельно: сразу после INSERT строки в couple_members ещё нет,
-- а клиент делает .select() на созданную пару.
create policy couples_select on public.couples for select using (
  public.is_couple_member(id) or created_by = auth.uid()
);

drop policy if exists couples_insert on public.couples;
create policy couples_insert on public.couples for insert with check (created_by = auth.uid());

drop policy if exists couples_update on public.couples;
create policy couples_update on public.couples for update using (public.is_couple_member(id));

-- couple_members: видно состав своей пары; добавить/удалить можно только себя.
drop policy if exists couple_members_select on public.couple_members;
create policy couple_members_select on public.couple_members for select using (
  user_id = auth.uid() or public.is_couple_member(couple_id)
);

drop policy if exists couple_members_insert on public.couple_members;
create policy couple_members_insert on public.couple_members for insert with check (user_id = auth.uid());

drop policy if exists couple_members_delete on public.couple_members;
create policy couple_members_delete on public.couple_members for delete using (user_id = auth.uid());

-- characters: свой герой и герой партнёра. Партнёру разрешено начислять награду.
drop policy if exists characters_select on public.characters;
create policy characters_select on public.characters for select using (
  user_id = auth.uid()
  or (couple_id is not null and public.is_couple_member(couple_id))
);

drop policy if exists characters_insert on public.characters;
create policy characters_insert on public.characters for insert with check (user_id = auth.uid());

drop policy if exists characters_update on public.characters;
create policy characters_update on public.characters for update using (
  user_id = auth.uid()
  or (couple_id is not null and public.is_couple_member(couple_id))
);

-- tasks и всё, что к ним привязано: доступ по членству в паре.
drop policy if exists tasks_all on public.tasks;
create policy tasks_all on public.tasks for all
  using (public.is_couple_member(couple_id))
  with check (public.is_couple_member(couple_id));

drop policy if exists task_assignees_all on public.task_assignees;
create policy task_assignees_all on public.task_assignees for all
  using (exists (select 1 from public.tasks t where t.id = task_id and public.is_couple_member(t.couple_id)))
  with check (exists (select 1 from public.tasks t where t.id = task_id and public.is_couple_member(t.couple_id)));

drop policy if exists checkpoints_all on public.checkpoints;
create policy checkpoints_all on public.checkpoints for all
  using (exists (select 1 from public.tasks t where t.id = task_id and public.is_couple_member(t.couple_id)))
  with check (exists (select 1 from public.tasks t where t.id = task_id and public.is_couple_member(t.couple_id)));

drop policy if exists story_all on public.story_chapters;
create policy story_all on public.story_chapters for all
  using (public.is_couple_member(couple_id))
  with check (public.is_couple_member(couple_id));

drop policy if exists ledger_all on public.ledger;
create policy ledger_all on public.ledger for all
  using (public.is_couple_member(couple_id))
  with check (public.is_couple_member(couple_id));

-- -------------------------------------------------------------- realtime ----
-- Чтобы изменения у партнёра прилетали на второй телефон мгновенно.
do $$
begin
  execute 'alter publication supabase_realtime add table public.tasks';
exception when duplicate_object then null; end $$;

do $$
begin
  execute 'alter publication supabase_realtime add table public.checkpoints';
exception when duplicate_object then null; end $$;

do $$
begin
  execute 'alter publication supabase_realtime add table public.story_chapters';
exception when duplicate_object then null; end $$;

do $$
begin
  execute 'alter publication supabase_realtime add table public.ledger';
exception when duplicate_object then null; end $$;

do $$
begin
  execute 'alter publication supabase_realtime add table public.characters';
exception when duplicate_object then null; end $$;
