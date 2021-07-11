(ns tic-tac-toe.db
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec {:dbtype "h2" :dbname "./tic-tac-toe"})

(defn add-player-to-db
  [surname]
  (let [results (jdbc/insert! db-spec :players {:surname surname :nbwin "0"})]
    (assert (= (count results) 1))
    (first (vals (first results)))))

(defn add-game-to-db
  [p1 p2]
  (let [results (jdbc/insert! db-spec :games {:p1_id p1 :p2_id p2})]
    (assert (= (count results) 1))
    (first (vals (first results)))))

(defn get-xy
  [loc-id]
  (let [results (jdbc/query db-spec
                            ["select x, y from locations where id = ?" loc-id])]
    (assert (= (count results) 1))
    (first results)))

(defn get-player
  [p-id]
  (let [results (jdbc/query db-spec
                            ["select id, surname, nbwin from players where id = ?" p-id])]
    (assert (= (count results) 1))
    (first results)))

(defn get-all-players
  []
  (jdbc/query db-spec "select id, surname, nbwin from players order by nbwin desc"))

(defn get-all-games
  []
  (jdbc/query db-spec "SELECT games.id, date, p1.surname, p2.surname, winner FROM games INNER JOIN players p1 ON p1.id = games.p1_id INNER JOIN players p2 ON p2.id = games.p2_id order by date desc"))

(defn player-win
  [p-id game-id]
  (do
    (let [results  (jdbc/execute! db-spec ["update players set nbwin = (nbwin + 1) where id = ?" p-id])
            ]
        (assert (= (count results) 1)))
      (let [results  (jdbc/execute! db-spec ["update games set winner = ? where id = ?" p-id game-id])
            ]
        (assert (= (count results) 1)))
      )

  )