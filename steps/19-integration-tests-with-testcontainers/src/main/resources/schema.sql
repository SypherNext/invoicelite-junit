create table if not exists invoices (
    number       varchar(20)  primary key,
    customer     varchar(200) not null,
    country_code char(2)      not null,
    currency     char(3)      not null,
    status       varchar(10)  not null,
    issue_date   date,
    due_date     date
);

create table if not exists invoice_lines (
    invoice_number varchar(20)    not null references invoices (number) on delete cascade,
    position       int            not null,
    sku            varchar(40)    not null,
    description    varchar(200)   not null,
    unit_price     numeric(12, 2) not null,
    quantity       int            not null,
    primary key (invoice_number, position)
);
