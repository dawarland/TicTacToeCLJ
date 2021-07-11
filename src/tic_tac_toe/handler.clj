(ns tic-tac-toe.handler
    (:require [tic-tac-toe.views :as views] ; add this require
              [compojure.core :refer :all]
              [compojure.route :as route]
              [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes ; replace the generated app-routes with this
           (GET "/"
                []
             (views/home-page))
           (GET "/1-vs-1"
                []
             (views/one-vs-one))
           (GET "/1-vs-AI"
                []
             (views/one-vs-ai))
           (GET "/ranking"
                []
             (views/ranking))
           (GET "/add-player"
                []
             (views/add-player-page))
           (POST "/add-player"
                 {params :params}
             (views/add-player-results-page params))
           (GET "/game"
                {params :params}
             (views/game-page params))
           (POST "/game"
                {params :params}
             (views/post-game-page params))
           (POST "/move"
                {params :params}
             (views/move params))
           (GET "/location/:loc-id"
                [loc-id]
             (views/location-page loc-id))
           (route/resources "/")
           (route/not-found "Not Found"))

(def app
     (wrap-defaults app-routes site-defaults))