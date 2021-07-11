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
(def home-button
  [:header
   [:a {:href "/"}
    [:img.home-button {:src "https://www.nicepng.com/png/detail/89-898495_house-logo-png-home-address-logo-png.png" :alt "Return to home"}]]
   ])

(defn bloc-player [num]
  (let [all-players (db/get-all-players)]
    [:div.player-bloc
     [:h2 (str "Player " num " :")]
     [:select {:name (str "p" num "_id") :onchange "
            /* on recupere les valeurs selectionnees */
            val1 = document.querySelector(\"select[name=\\\"p1_id\\\"]\")?.value;
            val2 = document.querySelector(\"select[name=\\\"p2_id\\\"]\")?.value || 0; /* 0 correspond a l'ID de l'ordinateur */
          "}
      (for [p all-players]
        (cond (= (:id p) 0) ""
              :else [:option {:value (:id p)} (:surname p)])

        )
      ]
     [:form {:action "/add-player" :method "POST"}
      (util/anti-forgery-field)                             ; prevents cross-site scripting attacks
      [:div.player-bloc__form-group
       [:input {:type "text" :name "surname" :placeholder "Surname"}]
       [:input {:type "submit" :value "+"}]]
      ]
     ])
  )


(defn home-page
  []
  (page/html5
    (gen-page-head "Home")
    home-button
    [:div.home
     [:h1 "Tic Tac Toe"]
     [:p "Choose your mode : "
      [:a.home-menu__btn {:href (str "/1-vs-AI")} "1 VS AI"]
      [:a.home-menu__btn {:href (str "/1-vs-1")} "1 VS 1"]
      [:a.home-menu__btn {:href (str "/ranking")} "Ranking"]
      ]
     ]))
(defn one-vs-one
  []
  (page/html5
    (gen-page-head "1 VS 1")
    home-button
    [:h1 "Choose your players :"]
    [:div.player
     (bloc-player 1)
     (bloc-player 2)
     ]

    [:a.player-btn__play {:href "#" :onclick "
    if(val1!=val2)
      window.location = \"/game?p1=\"+val1+\"&p2=\"+val2+\"&player=1\";
    else
      alert(\"Merci de choisir des joueurs differents\");
    "} "PLAY"]
    ))

(defn one-vs-ai
  []
  (page/html5
    (gen-page-head "1 VS AI")
    home-button
    [:h1 "Choose your player :"]
    [:div.player
     (bloc-player 1)
     ]
    [:a.player-btn__play {:href "#" :onclick "
    window.location = \"/game?p1=\"+val1+\"&p2=\"+val2+\"&player=1\";
    "} "PLAY"]
    )
  )

(defn ranking
  []
  (let [all-player (db/get-all-players)]
    (page/html5
      (gen-page-head "Ranking")
      home-button
      [:h1 "All Players stats"]

      [:h2 "Ranking"]
      [:table.rank
       [:tr [:th "Surname"] [:th "Number of wins"]]
       (for [player all-player]
         (cond (= (:id player) 0) ""
               :else [:tr [:td (:surname player)] [:td (:nbwin player)]])
         )]

      [:h2 "History"]
      (let [all-game (db/get-all-games)]
        [:table.rank
         [:tr [:th "id"] [:th "Date"] [:th "p1"] [:th "p2"] [:th "winner"]]
         (for [game all-game]
           [:tr [:td (:id game)] [:td (:date game)] [:td (:surname game)] [:td (:surname_2 game)] (cond (= (:winner game) 1) [:td (:surname game)]
                                                                                                        (= (:winner game) 2) [:td (:surname_2 game)]
                                                                                                        :else [:td "draw"]) ])])
      ))

  )


(defn add-player-page
  []
  (page/html5
    (gen-page-head "Add a player")
    home-button
    [:h1 "Add a Player"]
    [:form {:action "/add-player" :method "POST"}
     (util/anti-forgery-field)
     [:div.player-bloc__form-group
      [:input {:type "text" :name "surname" :placeholder "Surname"}]
      [:input {:type "submit" :value "+"}]]
     ]))

(defn add-player-results-page
  [{:keys [surname]}]
  (let [id (db/add-player-to-db surname)]
    (page/html5
      (gen-page-head "Added a player")
      home-button
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


(defn game-case
  [id x y plateau player]
  [:form {:action "/move" :method "POST"}
   (util/anti-forgery-field)
   [:input {:type "hidden" :name "x" :value x}]
   [:input {:type "hidden" :name "y" :value y}]
   [:input {:type "hidden" :name "plateau" :value (str plateau) }]
   [:input {:type "hidden" :name "player" :value player}]
   [:input {:type "hidden" :name "id" :value id}]
   [:input {:type "hidden" :name "p1"}]
   [:input {:type "hidden" :name "p2"}]
   [:button {:id (str x y) :class "game-gameboard__case" :name "submit"} [:p]]
   ]
  )
(defn parse-int [s]
  (Integer. (re-find  #"\d+" s )))

(defn move
  [{:keys [id x y player plateau p1 p2]}]
  (page/html5
    [:p "id: " id]
    [:p "x: " x]
    [:p "y: " y]
    [:p "player: " player]
    [:form {:action "/game" :method "POST"}
     (util/anti-forgery-field)
     [:input {:type "hidden" :name "id" :value id}]
     [:input {:type "hidden" :name "x" :value x}]
     [:input {:type "hidden" :name "y" :value y}]
     [:input {:type "hidden" :name "p1" :value p1}]
     [:input {:type "hidden" :name "p2" :value p2}]
     [:input {:type "hidden" :name "player" :value (cond (= player "1") 2
                                                         (= player "2") 1
                                                         :else "3")}]
     [:p (map #(str % ) (vec plateau))  ]
     [:input {:type "hidden" :name "plateau" :value (str/join (controller/play-morpion2 (vec plateau) player (parse-int x)  (parse-int y)) ) }  ]
      [:button {:name "submit"} "submit"]
     ]
    [:script {:type "text/javascript"} "
          document.addEventListener(\"DOMContentLoaded\", function(event) {
              document.querySelector(\"body > form > button\").click() ;
          });
        "]
    ))

(defn game-page
  [{:keys [p1 p2 player]}]
  (let [{surname1 :surname nbWin1 :nbWin} (db/get-player p1) {surname2 :surname nbWin2 :nbWin} (db/get-player p2)]
    (page/html5
      (gen-page-head "Game")
      home-button
      (let [game-id (db/add-game-to-db p1 p2)]
        [:h1 (str "Game " game-id)]
      [:div.game
       [:div.game-bloc
        [:p "Id P1: " p1]
        [:p "Surname P1: " surname1]
        [:p "Nb win P1: " nbWin1]

        [:p "Id P2: " p2]
        [:p "Surname P2: " surname2]
        [:p "Nb win P2: " nbWin2]
        ]
       [:div.game-gameboard
        (game-case game-id 0 0 "000000000" player) ;[:div#11.game-gameboard__case [:p]]
        (game-case game-id 1 0 "000000000" player);[:div#12.game-gameboard__case [:p]]
        (game-case game-id 2 0 "000000000" player);[:div#13.game-gameboard__case [:p]]
        (game-case game-id 0 1 "000000000" player);[:div#21.game-gameboard__case [:p]]
        (game-case game-id 1 1 "000000000" player);[:div#22.game-gameboard__case [:p]]
        (game-case game-id 2 1 "000000000" player);[:div#23.game-gameboard__case [:p]]
        (game-case game-id 0 2 "000000000" player);[:div#31.game-gameboard__case [:p]]
        (game-case game-id 1 2 "000000000" player);[:div#32.game-gameboard__case [:p]]
        (game-case game-id 2 2 "000000000" player);[:div#33.game-gameboard__case [:p]]
        ]
       ]
        )

      [:script {:type "text/javascript"} "
          var player = 1;
          function init() {
            // Pour chaque case, on initialise une fonction qui sera déclancher au clique
            for( x of document.getElementsByClassName(\"game-gameboard__case\") ){
              x.addEventListener(\"click\", function(){
                console.log(this.id)
                let [x,y] = this.id.split('')
                /* Si la case est deja rempli, je ne fais rien */
                if(this.textContent){
                  return;
                }
                /* Sinon, en fonction du joueur, j'ajoute un X ou un O */
                this.textContent = (player==1)? \"X\" : \"O\";
                console.log({player,x,y});
                player = (player == 1) ? 2 : 1;

              })
            }
          };
          document.addEventListener(\"DOMContentLoaded\", function(event) {
              console.log(\"ready\");
              /*init();*/
              var p1Id = document.querySelector(\"body > div > div.game-bloc > p:nth-child(1)\").textContent.split(':')[1].trim()
              var p2Id = document.querySelector(\"body > div > div.game-bloc > p:nth-child(4)\").textContent.split(':')[1].trim()
              document.querySelectorAll(\"input[name=\\\"p1\\\"]\").forEach(x=>x.value = p1Id);
              document.querySelectorAll(\"input[name=\\\"p2\\\"]\").forEach(x=>x.value = p2Id);
              });
        "]
      )))

(defn post-game-page
  [{:keys [p1 p2 id plateau player]}]
  (let [{surname1 :surname nbWin1 :nbwin} (db/get-player p1) {surname2 :surname nbWin2 :nbwin} (db/get-player p2)]
    (page/html5
      (gen-page-head (str "Game " id))
      home-button
      [:h1 (str "Game " id)]
      [:div.game
       [:div.game-bloc
        [:p "Id P1: " p1]
        [:p "Surname P1: " surname1]
        [:p "Nb win P1: " nbWin1]

        [:p "Id P2: " p2]
        [:p "Surname P2: " surname2]
        [:p "Nb win P2: " nbWin2]
        ]
       [:div.game-gameboard
        (game-case id 0 0 plateau player);[:div#11.game-gameboard__case [:p]]
        (game-case id 1 0 plateau player);[:div#12.game-gameboard__case [:p]]
        (game-case id 2 0 plateau player);[:div#13.game-gameboard__case [:p]]
        (game-case id 0 1 plateau player);[:div#21.game-gameboard__case [:p]]
        (game-case id 1 1 plateau player);[:div#22.game-gameboard__case [:p]]
        (game-case id 2 1 plateau player);[:div#23.game-gameboard__case [:p]]
        (game-case id 0 2 plateau player);[:div#31.game-gameboard__case [:p]]
        (game-case id 1 2 plateau player);[:div#32.game-gameboard__case [:p]]
        (game-case id 2 2 plateau player);[:div#33.game-gameboard__case [:p]]
        ]
       ]
      [:input {:type "hidden" :id "plateau" :value plateau}]
      [:p#result (cond (controller/winning-morpion? (vec plateau)) (str (cond (= "1" player) (do (db/player-win p2 id) (str surname2 " wins !") )
                                                                       :else (do (db/player-win p1 id) (str surname1 " wins !") ) )  )
                       (controller/end-morpion? (vec plateau))  (str "draw")
                :else "")]

      [:script {:type "text/javascript"} "
          var player = 1;
          function init() {
            var plateau = document.getElementById(\"plateau\").value.split('');
            var cases = document.getElementsByClassName(\"game-gameboard__case\");
            console.log(plateau);
            /* Pour chaque case, on initialise une fonction qui sera déclancher au clique */
            for( let i = 0; i < cases.length ; i++){
              cases[i].textContent = (plateau[i] == \"1\") ? \"X\" : (plateau[i] == \"2\") ? \"O\" : '';
              cases[i].onclick = () => {
                if(cases[i].textContent) return false ;
                if( !!document.querySelector(\"#result\").textContent ) {
                  if( confirm(\"Game finished! go to home page ?\")) {
                    window.location= \"/\";
                  }
                  return false;
                }

                return true;
              }
            }
          };

          document.addEventListener(\"DOMContentLoaded\", function(event) {
              console.log(\"ready\");
              init();
              var p1Id = document.querySelector(\"body > div > div.game-bloc > p:nth-child(1)\").textContent.split(':')[1].trim()
              var p2Id = document.querySelector(\"body > div > div.game-bloc > p:nth-child(4)\").textContent.split(':')[1].trim()

              document.querySelectorAll(\"input[name=\\\"p1\\\"]\").forEach(x=>x.value = p1Id);
              document.querySelectorAll(\"input[name=\\\"p2\\\"]\").forEach(x=>x.value = p2Id);
          });
        "]
      ))
  )