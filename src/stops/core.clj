(ns stops.core
  (:import java.io.ByteArrayInputStream)
  (:require [clojure.xml :as xml]
            [clj-time.core :refer [to-time-zone default-time-zone]]
            [clj-time.coerce :refer [from-long]]))

(defn parse-str [string]
  (->> string .trim .getBytes ByteArrayInputStream. xml/parse))

; (defn route-xml [] (slurp "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni&r=43"))
(defn prediction-xml [stop-tag] (slurp (str "http://webservices.nextbus.com/service/publicXMLFeed?command=predictions&a=sf-muni&r=43&s=" stop-tag)))
; (defn prediction-xml [] (slurp "prediction.xml"))
(defn route-xml [] (slurp "43.xml"))

(defn first-content [xml]
  (-> xml parse-str :content first))

(defn predictions [xml]
  (let [raw-predictions (map :attrs (:content (first (:content (first-content xml)))))]
    (map (fn [prediction]
           {:arrival-datetime (to-time-zone (from-long (read-string (prediction :epochTime))) (default-time-zone))
            :arrival-minutes (prediction :minutes)}
           ) raw-predictions)))
(predictions (prediction-xml "5171"))

; (defn title [route]
;   (-> route :attrs :title))

; (title (first-content route-xml))

; (defn things-tagged [tag content]
;   (map :attrs (filter #(= tag (% :tag)) content)))

; (defn stops [xml]
;   (things-tagged :stop ((first-content xml) :content)))
; (count (stops (first-content route-xml)))

; (defn stops-named [stop-name xml]
;   (filter #(= stop-name (% :title))
;           (stops (first-content xml))))
; (stops-named "City College Bookstore" route-xml)

; (defn directions [xml]
;   (things-tagged :direction ((first-content xml) :content)))
; (count (directions route-xml))




