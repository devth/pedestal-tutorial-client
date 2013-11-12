(ns tutorial-client.rendering
  (:require [domina :as dom]
            [io.pedestal.app.render.events :as events]
            [io.pedestal.app.render.push :as render]
            [io.pedestal.app.render.push.templates :as templates]
            [io.pedestal.app.render.push.handlers :as h]
            [io.pedestal.app.render.push.handlers.automatic :as d])
  (:require-macros [tutorial-client.html-templates :as html-templates]))

(def templates (html-templates/tutorial-client-templates))

(defn render-page [renderer [_ path] transmitter]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:tutorial-client-page templates))]
    (dom/append! (dom/by-id parent) (html {:id id :message ""}))))


(defn add-template [renderer [_ path :as delta] input-queue]
  (let [parent (render/get-parent-id renderer path)
        id (render/new-id! renderer path)
        html (templates/add-template renderer path (:tutorial-client-page templates))]
    (dom/append! (dom/by-id parent) (html {:id id}))
    (let [g (js/BubbleGame. "game-board")]
      (render/set-data! renderer path g)
      (dotimes [_ 5] (.addBubble g)))))

(defn game [renderer]
  (render/get-data renderer [:main]))

(defn destroy-game [renderer [_ path :as delta] input-queue]
  (.destroy (game renderer))
  (render/drop-data! renderer path)
  (h/default-destroy renderer delta input-queue))

(defn set-score [renderer [_ path _ v] _]
  (let [n (last path)
        g (game renderer)]
    (.setScore g n v)
    (when (not= n "Me")
      (.removeBubble g))))

(defn add-player [renderer [_ path] _]
  (.addPlayer (game renderer) (last path)))

(defn set-stat [renderer [_ path _ v] _]
  (let [s (last path)]
    (if-let [g (game renderer)]
      (.setStat g (name s) v))))

(defn add-handler [renderer [_ path transform-name messages] input-queue]
  (.addHandler (game renderer)
               (fn [p]
                 (events/send-transforms input-queue transform-name messages))))

(defn render-config []
  [[:node-create [:main] add-template]
   [:node-destroy [:main] destroy-game]
   [:node-create [:main :counters :*] add-player]
   [:value [:main :counters :*] set-score]
   [:value [:pedestal :debug :*] set-stat]
   [:value [:main :*] set-stat]
   [:transform-enable [:main :my-counter] add-handler]])


; (defn render-message [renderer [_ path _ new-value] transmitter]
;   (templates/update-t renderer path {:message new-value}))

; (defn render-template [template-name initial-value-fn]
;   (fn [renderer [_ path :as delta] input-queue]
;     (let [parent (render/get-parent-id renderer path)
;           id (render/new-id! renderer path)
;           html (templates/add-template renderer path (template-name templates))]
;       (dom/append! (dom/by-id parent) (html (assoc (initial-value-fn delta) :id id))))))
; 
; (defn render-other-counters-element [renderer [_ path] _]
;   (render/new-id! renderer path "other-counters"))
; 
; (defn render-value [renderer [_ path _ new-value] input-queue]
;   (let [key (last path)]
;     (templates/update-t renderer [:main] {key (str new-value)})))
; 
; (defn render-other-counter-value [renderer [_ path _ new-value] input-queue]
;   (let [key (last path)]
;     (templates/update-t renderer path {:count (str new-value)})))
; 
; (defn render-config []
;   [[:node-create [:main] (render-template :tutorial-client-page
;                                           (constantly {:my-counter "0"}))]
;    [:node-destroy [:main] h/default-destroy]
;    [:transform-enable [:main :my-counter] (h/add-send-on-click "inc-button")]
;    [:transform-disable [:main :my-counter] (h/remove-send-on-click "inc-button")]
;    [:value [:main :*] render-value]
;    [:value [:pedestal :debug :*] render-value]
; 
;    [:node-create [:main :other-counters] render-other-counters-element]
;    [:node-create [:main :other-counters :*]
;     (render-template :other-counter
;                      (fn [[_ path]] {:counter-id (last path)}))]
;    [:node-destroy [:main :other-counters :*] h/default-destroy]
;    [:value [:main :other-counters :*] render-other-counter-value]])
