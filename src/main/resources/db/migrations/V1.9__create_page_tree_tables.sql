create table page_trees
(
    id           integer      not null generated always as identity primary key,
    author       integer      not null references authors (id),
    title        varchar(200) not null unique,
    slug         varchar(30)  not null unique,
    body         varchar      not null,
    parent       integer references page_trees (id),
    approved     boolean default false,
    created_on   timestamptz  not null,
    published_on timestamptz,
    updated_on   timestamptz
);