CREATE TABLE notification_task (
    id serial not null primary key,
    chat_id int8 not null,
    notification text not null,
    date_time timestamp not null
)