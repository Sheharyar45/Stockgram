DROP TABLE IF EXISTS friendrequest CASCADE;
DROP TABLE IF EXISTS friendship CASCADE;
DROP TABLE IF EXISTS historicdata CASCADE;
DROP TABLE IF EXISTS portfolioholdings CASCADE;
DROP TABLE IF EXISTS portfolios CASCADE;
DROP TABLE IF EXISTS requestreview CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS stocklistholdings CASCADE;
DROP TABLE IF EXISTS stocklist CASCADE;
DROP TABLE IF EXISTS transactions CASCADE;
DROP TABLE IF EXISTS users CASCADE;


CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

CREATE TABLE portfolios (
    portfolio_id SERIAL PRIMARY KEY,
    cash_amount REAL NOT NULL DEFAULT 0,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    name TEXT NOT NULL,
    CONSTRAINT unique_userid_name UNIQUE (user_id, name)
);

CREATE TABLE portfolioholdings (
    portfolio_id INTEGER NOT NULL REFERENCES portfolios(portfolio_id),
    stock_symbol VARCHAR(5) NOT NULL,
    shares REAL NOT NULL,
    cost REAL,
    PRIMARY KEY (portfolio_id, stock_symbol)
);


CREATE TABLE transactions (
    transaction_id SERIAL PRIMARY KEY,
    portfolio_id INTEGER NOT NULL REFERENCES portfolios(portfolio_id),
    type VARCHAR(10),
    amount REAL NOT NULL,
    stock_symbol VARCHAR(5),
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),

    CHECK (type IN ('Deposit', 'Withdraw', 'Buy', 'Sell')),

    FOREIGN KEY (stock_symbol, timestamp)
        REFERENCES historicdata(stock_symbol, timestamp)
);


CREATE TABLE stocklist (
    stocklist_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id),
    visibility VARCHAR(10),

    CHECK (visibility IN ('public', 'private'))
);


CREATE TABLE stocklistholdings (
    stocklist_id INTEGER NOT NULL REFERENCES stocklist(stocklist_id),
    stock_symbol VARCHAR(5) NOT NULL,
    shares REAL NOT NULL,

    PRIMARY KEY (stocklist_id, stock_symbol)
);

CREATE TABLE historicdata (
    stock_symbol VARCHAR(5) NOT NULL,
    timestamp DATE NOT NULL,
    open REAL,
    high REAL,
    low REAL,
    close REAL,
    volume INTEGER,

    PRIMARY KEY (stock_symbol, timestamp)
);


CREATE TABLE friendrequest (
    sender              INTEGER NOT NULL REFERENCES users(user_id),
    receiver            INTEGER NOT NULL REFERENCES users(user_id),
    status              VARCHAR(10),
    last_updated_time   TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (sender, receiver),
    CHECK (status IN ('pending', 'accepted', 'rejected'))
);


CREATE TABLE friendship (
    user1      INTEGER NOT NULL REFERENCES users(user_id),
    user2      INTEGER NOT NULL REFERENCES users(user_id),
    timestamp  TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user1, user2),
);

CREATE TABLE reviews (
    review_id     SERIAL PRIMARY KEY,
    user_id       INTEGER NOT NULL REFERENCES users(user_id),
    stocklist_id  INTEGER NOT NULL REFERENCES stocklist(stocklist_id) ON DELETE CASCADE,
    text          TEXT,
    time_created  TIMESTAMP NOT NULL DEFAULT NOW(),
    time_updated  TIMESTAMP NOT NULL DEFAULT NOW(),
    likes         INTEGER DEFAULT 0,
    dislikes      INTEGER DEFAULT 0,

    CONSTRAINT unique_user_stocklist_review UNIQUE (user_id, stocklist_id),
);


CREATE TABLE requestreview (
    sender       INTEGER NOT NULL REFERENCES users(user_id),
    receiver     INTEGER NOT NULL REFERENCES users(user_id),
    stocklist_id INTEGER NOT NULL REFERENCES stocklist(stocklist_id) ON DELETE CASCADE,

    PRIMARY KEY (sender, receiver, stocklist_id),
);

