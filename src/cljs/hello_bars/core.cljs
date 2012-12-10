(ns hello-bars.core
  (:use-macros [c2.util :only [bind! half]])
  (:use [c2.core :only [unify unify!]])
  (:require [c2.scale :as scale]
            [c2.event :as event]
            [c2.maths :as maths]
            [c2.svg :as svg]))


;; vertical bar

(def vdata (atom (map maths/cos (range 0 (* 3.6 maths/Pi)
                                       (* 3.6 (/ maths/Pi 500))))))

(defn make-bars []
  (bind! "#vbars"
         (let [x-s (range 500)
               y-s @vdata
               data (map-indexed vector y-s)
               s (scale/linear :domain [(- 0 (apply max y-s)) (apply max y-s)]
                               :range [350 100])]
           [:svg
            {:class "myfoo"}
            [:line {:x1 0 :x2 500 :y1 350 :y2 350}]
            [:g.chart
             (unify data (fn [[x y]]
                           [:line {:x1 x :x2 x :y1 350 :y2 (s y) :title (s y)}]))]
            [:circle {:cx 250 :cy 250 :r 40 :fill "red"}]
            [:rect {:x 300 :y 350 :height 10 :width 30 :rotate 10 :fill "yellow"
                    :transform "rotate(-45 300 300) scale(0.7)"}]
            [:g.rects
             (map (fn [[idx m]]
                    [:rect.marker {:height 15 :width 2
                                   :y 351 :x m}])
                  (filter #(zero? (rem (% 0) 100))  (map-indexed vector x-s)))]])))

;; dartboard
;; :transform (svg/rotate (* 360 (rand)) [xo yo])
(defn len->coord [dist]
  "Convert a scaler length to a x y coord"
  (let [x (* dist (maths/cos 0))
        y 0]
    [x y]))

(def darts (atom []))

(defn make-board []
  (bind! "#dartboard"
         (let [rings [[250 "blue"] [200 "white"] [150 "blue"]
                      [100 "white"] [50 "green"] [10 "red"]]
               origin [250 250]
               yo 250
               xo 250
               marker-len 10]
           [:svg
            [:g.chart (unify rings (fn [[r colour]]
                                     [:circle {:cx (origin 0) :cy (origin 1) :r r
                                               :stroke "black" :fill colour}]))]
            [:g.markers
             (unify @darts
                    (fn [x]
                      [:g.marker ;; example dart
                       {:transform (str (svg/rotate (* 360 (rand)) [xo yo])
                                        (svg/translate (len->coord (rand 200))))}
                       [:line.dart {:x1 (- xo marker-len) :x2 (+ xo marker-len) :y1 yo :y2 yo}]
                       [:line.dart {:x1 xo :x2 xo :y1 (- yo marker-len) :y2 (+ yo marker-len)}]
                       [:text {:x xo :y xo} "Hello"]]))]]
           ))
  (event/on-raw "#throw" :click
                (fn [_ & more] (swap! darts #(conj % 1)))))

;; util functions
(defn myfilter [data]
  (into {} (filter #(even? (% 1)) data)))

(defn rand-all-vals [data]
  (into [] (for [[key val] data] [key (rand)])))

(defn sort-by-val [d] (sort-by #(% 1) d))

(defn rotate
  "cycle a seq n steps"
  ([data] (rotate data 1))
  ([data n]
     (let [len (count data)]
       (take-last len (take (+ n len) (cycle data))))))

(defn append-val-and-slide! [val data-atom]
  "Add a value to the end of a chart and remove the first datapoint"
  (swap! data-atom #(-> (conj  % val) rest vec)))

;; events

