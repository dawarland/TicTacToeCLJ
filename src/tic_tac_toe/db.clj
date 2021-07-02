(ns tic-tac-toe.db
  (:require [clojure.java.jdbc :as jdbc]))

(def db-spec {:dbtype "h2" :dbname "./tic-tac-toe"})

(defn add-player-to-db
  [surname]
  (let [results (jdbc/insert! db-spec :players {:surname surname :nbWin 0})]
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
                            ["select id, surname, nbWin from players where id = ?" p-id])]
    (assert (= (count results) 1))
    (first results)))

(defn get-all-players
  []
  (jdbc/query db-spec "select id, surname, nbWin from players"))

(defn get-all-games
  []
  (jdbc/query db-spec "select id, date, p1_id, p2_id , winner  nbWin from games"))