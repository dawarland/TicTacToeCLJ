# tic-tac-toe

FIXME

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Set up your database

    lein repl

Then, execute code bellow
```
    (require '[clojure.java.jdbc :as jdbc])
    (jdbc/with-db-connection [conn {:dbtype "h2" :dbname "./tic-tac-toe"}]

        (jdbc/db-do-commands conn
            (jdbc/create-table-ddl :players
                [[:id "bigint primary key auto_increment"]
                [:surname "varchar(32)"]
                [:nbwin "integer"]]))

        (jdbc/db-do-commands conn
            (jdbc/create-table-ddl :games
                [[:id "bigint primary key auto_increment"]
                [:date "timestamp NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]
                [:p1_id :integer]
                [:p2_id :integer]
                ["FOREIGN KEY(p1_id) REFERENCES players(id)"]
                ["FOREIGN KEY(p2_id) REFERENCES players(id)"]
                [:winner "integer"]]))


        (jdbc/insert! conn :players {:id 0 :surname "AI" :nbwin 0}))
```

## Running

To start a web server for the application, run:

    lein ring server

## License

Copyright © 2021 FIXME
