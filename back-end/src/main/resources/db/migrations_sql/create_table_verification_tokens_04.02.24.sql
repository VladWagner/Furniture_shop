create table furniture_shop.verification_tokens
(
    id          bigint auto_increment
        primary key,
    user_id     int         null,
    token       varchar(40) null,
    expiry_date datetime    null,
    constraint token_UNIQUE
        unique (token),
    constraint fk_verification_tokens_users
        foreign key (user_id) references furniture_shop.users (id)
);

create index fk_users_idx
    on furniture_shop.verification_tokens (user_id);

