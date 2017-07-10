(ns auto-commit.phabricator
  ;(:gen-class)
  (:require [org.httpkit.client :as http])
  (:require [pandect.algo.sha1 :refer :all])
  (:require
    [taoensso.timbre :as timbre
      :refer [log  trace  debug  info  warn  error  fatal  report
              logf tracef debugf infof warnf errorf fatalf reportf
              spy get-env]])
  (:require [clojure.data.json :as json])
)

(defn signature [dtToken sCeritficate]
  (sha1 (str dtToken sCeritficate))
);signature

(def clientInfo {
  :client "phabricator.clj"
  :clientVersion "0.1"
});clientInfo

(defn queryBodyFromMap [param]
  {
    :params (json/write-str param)
    :output "json"
    :__conduit__ "true"
  }
);queryBodyFromMap

(defn response [query]
  (let [
      status (@query :status)
    ]
    (and (not (= status 200)) (error @query))
    (or (and (= status 200) {:result ((json/read-str (@query :body)) "result")}) {:status status})
  );let
)

(defn query [sess sFunc mapParam]
 (let [
    param (merge {:__conduit__ (select-keys sess ["connectionID" "sessionKey"])} mapParam)
    query (http/request {
        :url (str (sess :url) sFunc)
        :method :post
        :form-params (queryBodyFromMap param)
        :insecure? true
     })
  ]
  (response query)
 );let
);query

(defn queryTask [sess nIdTask]
  (query sess "maniphest.info" {:task_id nIdTask})
)

(defn queryProjects [sess sPhids]
  (query sess "project.query" {:phids sPhids})
)

(defn session [sUrl sUser sCeritficate]
  (let [
     token (quot (System/currentTimeMillis) 1000)
     param {
        :user sUser
        :authToken token
        :authSignature (signature token sCeritficate)
     }
     query (http/request {
        :url (str sUrl "conduit.connect")
        :method :post
        :form-params (queryBodyFromMap (merge param  clientInfo))
        :insecure? true
     })
    ]
    (debug "session started" sUrl sUser)
    (merge {:url sUrl} ((response query) :result))
   );let
);(-session)
