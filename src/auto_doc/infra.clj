(ns auto-doc.infra
  (:require [clojure.data.json :as json]
            [auto-commit.core :as cc]
            [clojure.pprint :as pp]))

(defn load-data []
  (json/read-str
   (slurp
    (str (-> "fs/meta" cc/proj :path) "/doc/infra.json"))))

(def _data (atom nil))

(defn data []
  (when (nil? @_data)
    (swap! _data (fn [_] (load-data))))
  @_data)

(defn reload-data []
  (swap! _data (fn [_] nil))
  (data))

(defn hosts
  ([location] (filter
               (fn [e] (= location (-> e second (get "location"))))
               (hosts)))
  ([]
   (let [i (data)
         h (i "hosts")]
     h)))

(defn print-hosts [& args]
  (pp/pprint
   (map
    #(merge {:id (first %)} (select-keys (second %) ["dns" "ip"]))
    (apply hosts args))))
