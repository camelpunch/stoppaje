(ns stoppaje.core
  (:import java.io.ByteArrayInputStream)
  (:require [clojure.xml :as xml]
            [clojure.string :refer [join]]
            [clj-time.core :refer [to-time-zone default-time-zone]]
            [clj-time.coerce :refer [from-long]]))

(defn parse-str [string]
  (->> string .trim .getBytes ByteArrayInputStream. xml/parse))

; (defn route-xml [] (slurp "http://webservices.nextbus.com/service/publicXMLFeed?command=routeConfig&a=sf-muni&r=43"))

(defn query-pair [pair]
  (let [k (first pair)
        v (second pair)]
    (str (first (name k)) "=" v)))

(defn queryize [params]
  (join "&" (map query-pair params)))
(queryize {:stop-tag "1234"
           :route "43"}) ; r=43&s=1234

(defn prediction-xml [& args]
  (let [options (apply hash-map args)
        stop-tag (:stop-tag options)]
    (slurp (str "http://webservices.nextbus.com/service/publicXMLFeed?"
                "command=predictions&"
                (queryize options)))))
; (defn prediction-xml [] (slurp "prediction.xml"))
(defn route-xml [] (slurp "43.xml"))

(defn body [xml] (-> xml parse-str :content first))

(defn parse-predictions [xml]
  (let [raw-predictions (map :attrs (:content (first (:content (body xml)))))]
    (map (fn [prediction]
           {:arrival-datetime (to-time-zone (from-long (read-string (prediction :epochTime))) (default-time-zone))
            :arrival-minutes (prediction :minutes)}
           ) raw-predictions)))
(parse-predictions (prediction-xml :stop-tag "5171"
                                   :route "43"
                                   :agency "sf-muni"))

(defn pretty-prediction [& args]
  (let [all-predictions (parse-predictions (apply prediction-xml args))]
    (str "Arriving in " (:arrival-minutes (first all-predictions)) " minutes")))
(pretty-prediction :stop-tag "5171"
                   :route "43"
                   :agency "sf-muni")

(defn route-title [xml] (-> xml body :attrs :title))

(route-title (route-xml))

(defn things-tagged [tag content]
  (map :attrs (filter #(= tag (% :tag)) content)))

(defn stops [xml]
  (things-tagged :stop ((body xml) :content)))
(count (stops (route-xml)))

; (defn stops-named [stop-name xml]
;   (filter #(= stop-name (% :title))
;           (stops (body xml))))
; (stops-named "City College Bookstore" route-xml)

; (defn directions [xml]
;   (things-tagged :direction ((body xml) :content)))
; (count (directions route-xml))

