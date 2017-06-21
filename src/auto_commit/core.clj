(ns auto-commit.core
  (:gen-class)
  (:require
    [taoensso.timbre :as timbre
      :refer [log  trace  debug  info  warn  error  fatal  report
              logf tracef debugf infof warnf errorf fatalf reportf
              spy get-env]])
    (:require [auto-commit.phabricator])
    (:require [aero.core :as aero])
)

;https://stackoverflow.com/questions/7658981/how-to-reload-a-clojure-file-in-repl
;(use 'your.namespace :reload)
;https://github.com/hach-que/Phabricator.Conduit/blob/master/ConduitClient.cs

(defn -main []
  
  (let [
     conf (aero/read-config "config.edn")
     phabricator (conf :phabricator)
     sess (auto-commit.phabricator/session (phabricator :url) (phabricator :user-name) (phabricator :user-certificate))
    ]
    (info sess)
    ;(info "response1's status: " (:status @response1))
    ;(debug "response1's headers: " (:headers @response1))
    ;(println @response1)
   );let
);(-main)

(defn -run []
  (info "-run auto-commit.core")
  (use 'auto-commit.phabricator :reload)
  (use 'auto-commit.core :reload)
  (-main)
);(-run)
