create table if not exists users
(
    username varchar(50)  not null primary key,
    password varchar(500) not null,
    enabled  boolean      not null
);
create unique index if not exists udx_auth_username on users (username);

create table if not exists authorities
(
    username  varchar(50) not null,
    authority varchar(50) not null,
    constraint delete_user_cascade foreign key (username) references users (username) on delete cascade
);
create unique index if not exists udx_auth_username on authorities (username, authority);