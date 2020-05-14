(ns auto-doc.infra
  (:require [clojure.data.json :as json]
            [auto-commit.core :as cc]
            [clojure.string :as s]
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

(defn with-host-id [& args]
  (map
   #(merge {:id (first %)} (select-keys (second %) ["dns" "ip" "alias"]))
   (apply hosts args)))

(defn print-hosts [& args]
  (pp/pprint
   (apply with-host-id args)))

(defn active
  ([prop] (active (data) prop))
  ([coll prop]
   (filter #(not (get "decommissioned" %)) (get coll prop))))

(defn active-instances []
  (active "instances"))

(defn contains-val? [val h]
  (let [lcv (s/lower-case (or val ""))]
    (letfn [(eq-str [s] (= lcv (s/lower-case (or s ""))))
            (is-match [v] (if (vector? v)
                            (not-empty (filter eq-str v))
                            (eq-str v)))]
      (not-empty
       (filter is-match
               (map (partial get h) (keys h)))))))

(defn find-host [name]
  (let [r (with-host-id)]
    (first (filter (partial contains-val? name) r))))

(defn find-host-id [name]
  (or
   (:id (find-host name))
   name))

(defn list-instances-by
  ([prop propVal] (list-instances-by (active-instances) prop propVal))
  ([inst prop propVal]
   (filter #(= propVal (find-host-id (get (second %) prop))) inst)))

(defn count-instances-by
  ([prop] (count-instances-by (active-instances) prop))
  ([inst prop]
   (reduce
    (fn [z e] (assoc z e
                     (inc (or (get z e) 0))))
    {}
    (map #(find-host-id (get (second %) prop)) inst))))

(defn count-instances-by-location [location]
  (let [cnt-data (count-instances-by "appHost")
        id first
        cnt-val (comp first vals)]
    (sort-by #(-> % cnt-val -)
             (filter #(not= nil (cnt-val %))
                     (map
                      (fn [host] {(id host) (-> host id cnt-data)})
                      (hosts location))))))

(let []
  ;; (pp/pprint inst)
  ;; (group-by (map #(select-keys (second %) ["appHost"]) inst ))
  ;; (group-by #(get (select-keys % ["appHost"]) "appHost") inst)
  ;; (pp/pprint (group-by #(get % "appHost") (map #(select-keys (second %) ["appHost"]) inst)))
  ;; (find-host "sqlde")
  ;; (pp/pprint (sort-by val (count-instances-by "appHost")))
  ;; (pp/pprint (sort-by val (count-instances-by "dbHost")))
  ;; [(count-instances-by "appHost") (count-instances-by "dbHost")]
)

