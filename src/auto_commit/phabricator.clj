(ns auto-commit.phabricator
  (:gen-class)
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

(defn session [sUrl sUser sCeritficate]
  (let [
     token (quot (System/currentTimeMillis) 1000)
     param (json/write-str {
        :client "phabricator.clj"
        :clientVersion "0.1"
        :user sUser
        :authToken token
        :authSignature (signature token sCeritficate)
     })
     body {
        :params param
        :output "json"
        :__conduit__ "true"
     }
     query (http/request {
        :url (str sUrl "conduit.connect")
        :method :post
        :form-params body
        :insecure? true
     })
    ]
    (let [
        status (@query :status)
        result (json/read-str (@query :body))
      ]
      (and (= status 200) (result "result"))
    );let
   );let
);(-main)
