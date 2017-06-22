(ns auto-commit.core
  (:gen-class)
  (:require
    [taoensso.timbre :as timbre
      :refer [log  trace  debug  info  warn  error  fatal  report
              logf tracef debugf infof warnf errorf fatalf reportf
              spy get-env]])
    (:require [auto-commit.phabricator :as phb])
    (:require [aero.core :as aero])
)

;https://stackoverflow.com/questions/7658981/how-to-reload-a-clojure-file-in-repl
;(use 'your.namespace :reload)
;https://github.com/hach-que/Phabricator.Conduit/blob/master/ConduitClient.cs

(defn -main []
  (let [
     conf (aero/read-config "config.edn")
     cnfPhb (conf :phabricator)
     sess (phb/session (cnfPhb :url) (cnfPhb :user-name) (cnfPhb :user-certificate))
    ]
    (info "session started:" sess)
    (let [
        task (phb/query sess "maniphest.info" {:task_id 2006})
      ]
      (info "task" (select-keys (task :result) ["title" "status" "objectName" "statusName"]))
    );let
   );let
);(-main)

(defn -run []
  (info "-run auto-commit.core")
  (use 'auto-commit.phabricator :reload)
  (use 'auto-commit.core :reload)
  (-main)
);(-run)
