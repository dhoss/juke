drop database juke;
create database juke;

drop owned by juke;
drop user juke;
create user juke with password 'juke';

grant all privileges on database juke to juke;

grant all on schema public to juke;

grant usage, create on schema public to juke;

alter database juke owner to juke;