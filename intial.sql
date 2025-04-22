use thumb_db;

-- user table
create table if not exists user
(
id              bigint auto_increment primary key,
    username    varchar(255) not null
);

-- blog table
create table if not exists blog(
    id          bigint          auto_increment primary key,
    userId      bigint          not null,
    title       varchar(512)    not null,
    coverImg    varchar(1024)   not null,
    content     text            not null,
    thumbCount  int             not null,
    createTime  datetime default CURRENT_TIMESTAMP  not null,
    updateTime  datetime default CURRENT_TIMESTAMP  not null on update CURRENT_TIMESTAMP
);

-- Add Index for high frequent fields, improve query performance
create index idx_userId on blog(userId);

create table if not exists thumb
(
    id        bigint auto_increment primary key,
    userId    bigint          not null,
    blogId    bigint          not null,
    createTime datetime default CURRENT_TIMESTAMP  not null
);

create unique index idx_userId_blogId on thumb(userId, blogId);