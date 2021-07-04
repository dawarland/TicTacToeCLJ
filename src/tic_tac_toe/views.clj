(ns tic-tac-toe.views
  (:require [tic-tac-toe.db :as db]
            [tic-tac-toe.gamecontroller :as controller]
            [clojure.string :as str]
            [hiccup.page :as page]

            [ring.util.anti-forgery :as util]))

(defn gen-page-head
  [title]
  [:head
   [:title (str "Tic Tac Toe: " title)]
   (page/include-css "/css/styles.css")])

(def header-links
  [:div#header-links
   "[ "
   [:a {:href "/"} "Home"]
   " | "
   [:a {:href "/add-location"} "Add a Location"]
   " | "
   [:a {:href "/all-locations"} "View All Locations"]
   " ]"])

(defn block-player [num]
   (let [all-players (db/get-all-players)]
         [:div.player-block
          [:h2 (str "Player " num " :")]
          [:select {:name (str "p" num "_id") :onchange "
            /* on recupere les valeurs selectionnees */
            val1 = document.querySelector(\"select[name=\\\"p1_id\\\"]\")?.value;
            val2 = document.querySelector(\"select[name=\\\"p2_id\\\"]\")?.value || 0; /* 0 correspond a l'ID de l'ordinateur */
          "}
           (for [p all-players]
             [:option {:value (:id p)} (:surname p)])
           ]
          [:form {:action "/add-player" :method "POST"}
           (util/anti-forgery-field) ; prevents cross-site scripting attacks
           [:div.player-block__form-group
            [:input {:type "text" :name "surname" :placeholder "Surname"}]
            [:input {:type "submit" :value "+"}]]
           ]
          ])
  )


(defn home-page
  []
  (page/html5
    (gen-page-head "Home")
    [:div.home
      [:h1 "Tic Tac Toe"]
      [:p "Choose your mode : "
       [:a.home-menu__btn {:href (str "/1-vs-AI")} "1 VS AI"]
       [:a.home-menu__btn {:href (str "/1-vs-1")} "1 VS 1"]
       ]
     ]))
(defn one-vs-one
  []
  (page/html5
    (gen-page-head "1 VS 1")
    [:h1 "Choose your players :"]
    [:div.player
     (block-player 1)
     (block-player 2)
     ]

    [:a.player-btn__play {:href "#" :onclick "
    if(val1!=val2)
      window.location = \"/game?p1=\"+val1+\"&p2=\"+val2;
    else
      alert(\"Merci de choisir des joueurs differents\");
    "} "PLAY" ]
    ))

(defn one-vs-ai
  []
  (page/html5
    (gen-page-head "1 VS AI")
    [:h1 "Choose your player :"]
    [:div.player
     (block-player 1)
     ]
    [:a.player-btn__play {:href "#" :onclick "
    window.location = \"/game?p1=\"+val1+\"&p2=\"+val2;
    "} "PLAY" ]
    )
  )


(defn add-player-page
  []
  (page/html5
    (gen-page-head "Add a player")
    [:h1 "Add a Player"]
    [:form {:action "/add-player" :method "POST"}
     (util/anti-forgery-field) ; prevents cross-site scripting attacks
     [:div.player-block__form-group
      [:input {:type "text" :name "surname" :placeholder "Surname"}]
      [:input {:type "submit" :value "+"}]]
     ]))

(defn add-player-results-page
  [{:keys [surname]}]
  (let [id (db/add-player-to-db surname)]
    (page/html5
      (gen-page-head "Added a player")
      [:h1 "Added a player"]
      [:p "Added [" surname "] (id: " id ") to the db."])))

(defn location-page
  [loc-id]
  (let [{x :x y :y} (db/get-xy loc-id)]
    (page/html5
      (gen-page-head (str "Location " loc-id))
      header-links
      [:h1 "A Single Location"]
      [:p "id: " loc-id]
      [:p "x: " x]
      [:p "y: " y])))

(defn game-page
  [{:keys [p1 p2]}]
  (let [{surname1 :surname nbWin1 :nbWin} (db/get-player p1) {surname2 :surname nbWin2 :nbWin} (db/get-player p2)]
    (page/html5
      (gen-page-head (str "Game " p1))
      ; (let [game-id (db/add-game-to-db p1 p2)]
      (let [game-id 1]
          [:h1 (str "Game " game-id)])
        [:div.game
         [:div.game-block
          [:p "id1: " p1]
          [:p "surname1: " surname1]
          [:p "nbWin1: " nbWin1]

          [:p "id2: " p2]
          [:p "surname2: " surname2]
          [:p "nbWin2: " nbWin2]
          ]
         [:div.game-gameboard
          [:div#11.game-gameboard__case [:p ]]
          [:div#12.game-gameboard__case [:p ]]
          [:div#13.game-gameboard__case [:p ]]
          [:div#21.game-gameboard__case [:p ]]
          [:div#22.game-gameboard__case [:p ]]
          [:div#23.game-gameboard__case [:p ]]
          [:div#31.game-gameboard__case [:p ]]
          [:div#32.game-gameboard__case [:p ]]
          [:div#33.game-gameboard__case [:p ]]
          ]
         ]

        [:script {:type "text/javascript"} "
          var player = 1;
          function init() {
            /* Pour chaque case, on initialise une fonction qui sera déclancher au clique */
            for( x of document.getElementsByClassName(\"game-gameboard__case\") ){
              x.addEventListener(\"click\", function(){
                console.log(this.id)
                let [x,y] = this.id.split('')
                /* Si la case est deja rempli, je ne fais rien */
                if(this.textContent){
                  return;
                }
                /* Sinon, en fonction tu joueur, j'ajoute un X ou un O */
                this.textContent = (player==1)? \"X\" : \"O\";
                console.log({player,x,y});
                player = (player == 1) ? 2 : 1;
                /* TODO : envoyer les info au backend (clojure) */
              })
            }
          };
          document.addEventListener(\"DOMContentLoaded\", function(event) {
              console.log(\"ready\");
              init();
          });
        "]
        ; (controller/play-morpion )
        )))
