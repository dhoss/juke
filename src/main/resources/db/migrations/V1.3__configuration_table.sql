create table configuration(
  id integer not null generated always as identity primary key,
  layout_id integer not null references layouts(id),
  created_on timestamptz not null default now(),
  updated_on timestamptz
);