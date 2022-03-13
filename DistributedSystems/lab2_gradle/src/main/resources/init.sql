drop table if exists Tags;

drop table if exists Nodes;

create table Nodes (
    id bigserial primary key,
    "user" text not null,
    latitude numeric not null,
    longitude numeric not null
);

create table Tags (
    id bigserial primary key,
    key text not null,
    value text not null,
    nodeId bigint references Nodes(id)
);