# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table pdfsession (
  session_id                varchar(255) not null,
  start_date                bigint,
  end_date                  bigint,
  number_of_pages           integer,
  is_complete               boolean,
  review_threshold          integer,
  auto_color_threshold      integer,
  filter_background_color   boolean,
  constraint pk_pdfsession primary key (session_id))
;

create table parsed_pdfpage (
  id                        varchar(255) not null,
  date                      bigint,
  session_id                varchar(255),
  page_number               integer,
  percent_color             integer,
  to_be_reviewed            boolean,
  print_color               boolean,
  print_black_and_white     boolean,
  image_blob                blob,
  thumbnail_blob            blob,
  constraint pk_parsed_pdfpage primary key (id))
;

create sequence pdfsession_seq;

create sequence parsed_pdfpage_seq;




# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists pdfsession;

drop table if exists parsed_pdfpage;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists pdfsession_seq;

drop sequence if exists parsed_pdfpage_seq;

