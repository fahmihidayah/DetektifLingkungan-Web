# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table auth (
  id_auth                   bigint auto_increment not null,
  auth_token                varchar(255),
  constraint pk_auth primary key (id_auth))
;

create table komentar (
  id                        bigint auto_increment not null,
  data_komentar             varchar(255),
  user_id                   bigint,
  laporan_id                bigint,
  constraint pk_komentar primary key (id))
;

create table laporan (
  id                        bigint auto_increment not null,
  data_laporan              varchar(255),
  tanggapan_id              bigint,
  user_id                   bigint,
  jumlah_komentar           integer,
  jumlah_user_pemantau      integer,
  katagori_laporan          varchar(255),
  longitude                 double,
  latitude                  double,
  time                      datetime,
  constraint pk_laporan primary key (id))
;

create table notif (
  id                        bigint auto_increment not null,
  laporan_id                bigint,
  user_id                   bigint,
  constraint pk_notif primary key (id))
;

create table tanggapan (
  id                        bigint auto_increment not null,
  data_tanggapan            varchar(255),
  user_id                   bigint,
  constraint pk_tanggapan primary key (id))
;

create table user (
  id                        bigint auto_increment not null,
  user_name                 varchar(256) not null,
  sha_password              varbinary(64) not null,
  password                  varchar(255),
  type                      varchar(255),
  name                      varchar(255),
  email                     varchar(255),
  status                    varchar(255),
  jumlah_follower_user      integer,
  jumlah_following_user     integer,
  constraint uq_user_user_name unique (user_name),
  constraint pk_user primary key (id))
;


create table user_laporan (
  user_id                        bigint not null,
  laporan_id                     bigint not null,
  constraint pk_user_laporan primary key (user_id, laporan_id))
;

create table user_follow_user (
  follower_user_id               bigint not null,
  following_user_id              bigint not null,
  constraint pk_user_follow_user primary key (follower_user_id, following_user_id))
;
alter table komentar add constraint fk_komentar_user_1 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_komentar_user_1 on komentar (user_id);
alter table komentar add constraint fk_komentar_laporan_2 foreign key (laporan_id) references laporan (id) on delete restrict on update restrict;
create index ix_komentar_laporan_2 on komentar (laporan_id);
alter table laporan add constraint fk_laporan_tanggapan_3 foreign key (tanggapan_id) references tanggapan (id) on delete restrict on update restrict;
create index ix_laporan_tanggapan_3 on laporan (tanggapan_id);
alter table laporan add constraint fk_laporan_user_4 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_laporan_user_4 on laporan (user_id);
alter table notif add constraint fk_notif_laporan_5 foreign key (laporan_id) references laporan (id) on delete restrict on update restrict;
create index ix_notif_laporan_5 on notif (laporan_id);
alter table notif add constraint fk_notif_user_6 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_notif_user_6 on notif (user_id);
alter table tanggapan add constraint fk_tanggapan_user_7 foreign key (user_id) references user (id) on delete restrict on update restrict;
create index ix_tanggapan_user_7 on tanggapan (user_id);



alter table user_laporan add constraint fk_user_laporan_user_01 foreign key (user_id) references user (id) on delete restrict on update restrict;

alter table user_laporan add constraint fk_user_laporan_laporan_02 foreign key (laporan_id) references laporan (id) on delete restrict on update restrict;

alter table user_follow_user add constraint fk_user_follow_user_user_01 foreign key (follower_user_id) references user (id) on delete restrict on update restrict;

alter table user_follow_user add constraint fk_user_follow_user_user_02 foreign key (following_user_id) references user (id) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table auth;

drop table komentar;

drop table laporan;

drop table user_laporan;

drop table notif;

drop table tanggapan;

drop table user;

drop table user_follow_user;

SET FOREIGN_KEY_CHECKS=1;

