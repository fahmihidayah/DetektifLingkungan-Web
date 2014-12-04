# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table auth (
  id_auth                   bigint auto_increment not null,
  auth_token                varchar(255),
  constraint pk_auth primary key (id_auth))
;

create table image_path (
  id_image_path             bigint auto_increment not null,
  path                      varchar(255),
  file_name                 varchar(255),
  keterangan                varchar(255),
  laporan_id_laporan        bigint,
  constraint pk_image_path primary key (id_image_path))
;

create table komentar (
  id_komentar               bigint auto_increment not null,
  data_komentar             varchar(255),
  user_id_user              bigint,
  laporan_id_laporan        bigint,
  constraint pk_komentar primary key (id_komentar))
;

create table laporan (
  id_laporan                bigint auto_increment not null,
  judul_laporan             varchar(255),
  data_laporan              varchar(255),
  tanggapan_id_tanggapan    bigint,
  user_id_user              bigint,
  jumlah_komentar           integer,
  jumlah_user_pemantau      integer,
  katagori_laporan          varchar(255),
  longitude                 double,
  latitude                  double,
  time                      datetime,
  viwer                     bigint,
  constraint pk_laporan primary key (id_laporan))
;

create table notif (
  id_notif                  bigint auto_increment not null,
  laporan_id_laporan        bigint,
  time                      datetime,
  user_id_user              bigint,
  constraint pk_notif primary key (id_notif))
;

create table private_message (
  id_private_message        bigint auto_increment not null,
  user_sender_id_user       bigint,
  user_receiver_id_user     bigint,
  message_data              varchar(255) not null,
  time                      datetime,
  status                    varchar(255),
  constraint pk_private_message primary key (id_private_message))
;

create table server_address (
  id_server_address         bigint auto_increment not null,
  address                   varchar(255),
  constraint pk_server_address primary key (id_server_address))
;

create table tanggapan (
  id_tanggapan              bigint auto_increment not null,
  data_tanggapan            varchar(255),
  user_id_user              bigint,
  image_path_id_image_path  bigint,
  constraint pk_tanggapan primary key (id_tanggapan))
;

create table user (
  id_user                   bigint auto_increment not null,
  user_name                 varchar(256) not null,
  sha_password              varbinary(64) not null,
  password                  varchar(255),
  type                      varchar(255),
  name                      varchar(255),
  email                     varchar(255),
  status                    varchar(255),
  jumlah_follower_user      integer,
  jumlah_following_user     integer,
  image_profile_path_id_image_path bigint,
  gcm_id                    varchar(255),
  constraint uq_user_user_name unique (user_name),
  constraint pk_user primary key (id_user))
;


create table user_laporan (
  user_id_user                   bigint not null,
  laporan_id_laporan             bigint not null,
  constraint pk_user_laporan primary key (user_id_user, laporan_id_laporan))
;

create table follower_user (
  follower_user_id               bigint not null,
  following_user_id              bigint not null,
  constraint pk_follower_user primary key (follower_user_id, following_user_id))
;
alter table image_path add constraint fk_image_path_laporan_1 foreign key (laporan_id_laporan) references laporan (id_laporan) on delete restrict on update restrict;
create index ix_image_path_laporan_1 on image_path (laporan_id_laporan);
alter table komentar add constraint fk_komentar_user_2 foreign key (user_id_user) references user (id_user) on delete restrict on update restrict;
create index ix_komentar_user_2 on komentar (user_id_user);
alter table komentar add constraint fk_komentar_laporan_3 foreign key (laporan_id_laporan) references laporan (id_laporan) on delete restrict on update restrict;
create index ix_komentar_laporan_3 on komentar (laporan_id_laporan);
alter table laporan add constraint fk_laporan_tanggapan_4 foreign key (tanggapan_id_tanggapan) references tanggapan (id_tanggapan) on delete restrict on update restrict;
create index ix_laporan_tanggapan_4 on laporan (tanggapan_id_tanggapan);
alter table laporan add constraint fk_laporan_user_5 foreign key (user_id_user) references user (id_user) on delete restrict on update restrict;
create index ix_laporan_user_5 on laporan (user_id_user);
alter table notif add constraint fk_notif_laporan_6 foreign key (laporan_id_laporan) references laporan (id_laporan) on delete restrict on update restrict;
create index ix_notif_laporan_6 on notif (laporan_id_laporan);
alter table notif add constraint fk_notif_user_7 foreign key (user_id_user) references user (id_user) on delete restrict on update restrict;
create index ix_notif_user_7 on notif (user_id_user);
alter table private_message add constraint fk_private_message_userSender_8 foreign key (user_sender_id_user) references user (id_user) on delete restrict on update restrict;
create index ix_private_message_userSender_8 on private_message (user_sender_id_user);
alter table private_message add constraint fk_private_message_userReceiver_9 foreign key (user_receiver_id_user) references user (id_user) on delete restrict on update restrict;
create index ix_private_message_userReceiver_9 on private_message (user_receiver_id_user);
alter table tanggapan add constraint fk_tanggapan_user_10 foreign key (user_id_user) references user (id_user) on delete restrict on update restrict;
create index ix_tanggapan_user_10 on tanggapan (user_id_user);
alter table tanggapan add constraint fk_tanggapan_imagePath_11 foreign key (image_path_id_image_path) references image_path (id_image_path) on delete restrict on update restrict;
create index ix_tanggapan_imagePath_11 on tanggapan (image_path_id_image_path);
alter table user add constraint fk_user_imageProfilePath_12 foreign key (image_profile_path_id_image_path) references image_path (id_image_path) on delete restrict on update restrict;
create index ix_user_imageProfilePath_12 on user (image_profile_path_id_image_path);



alter table user_laporan add constraint fk_user_laporan_user_01 foreign key (user_id_user) references user (id_user) on delete restrict on update restrict;

alter table user_laporan add constraint fk_user_laporan_laporan_02 foreign key (laporan_id_laporan) references laporan (id_laporan) on delete restrict on update restrict;

alter table follower_user add constraint fk_follower_user_user_01 foreign key (follower_user_id) references user (id_user) on delete restrict on update restrict;

alter table follower_user add constraint fk_follower_user_user_02 foreign key (following_user_id) references user (id_user) on delete restrict on update restrict;

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table auth;

drop table image_path;

drop table komentar;

drop table laporan;

drop table user_laporan;

drop table notif;

drop table private_message;

drop table server_address;

drop table tanggapan;

drop table user;

drop table follower_user;

SET FOREIGN_KEY_CHECKS=1;

