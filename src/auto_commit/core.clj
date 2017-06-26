(ns auto-commit.core
  (:gen-class)
  (:require
    [taoensso.timbre :as timbre
      :refer [log  trace  debug  info  warn  error  fatal  report
              logf tracef debugf infof warnf errorf fatalf reportf
              spy get-env]])
    (:require [auto-commit.phabricator :as phb])
    (:require [aero.core :as aero])
    (:require [clojure.tools.cli :refer [parse-opts]])
)

;https://github.com/hach-que/Phabricator.Conduit/blob/master/ConduitClient.cs

(def cli-options
  ;; An option with a required argument
  [["-t" "--task TASKID" "Manifest task ID"
    :parse-fn #(Integer/parseInt %)]
   ["-h" "--help"]])

(defn args2Cmd [args opt]
  (let [cmd (parse-opts args opt)]
    (or 
      (and (cmd :errors) {:message (cmd :errors)})
      (and ((cmd :options) :help) {:message (cmd :summary)})
      (cmd :options)
    );or
  );let
);args2Cmd

(defn mainBody [cmd]
  (let [     
      conf (aero/read-config "config.edn")
      cnfPhb (conf :phabricator)
      sess (phb/session (cnfPhb :url) (cnfPhb :user-name) (cnfPhb :user-certificate))
      ]
      (info "cmd" cmd)
      (info "session started:" sess)
      (let [
          task (phb/query sess "maniphest.info" {:task_id (cmd :task)})
        ]
        (info "task" (select-keys (task :result) ["title" "status" "objectName" "statusName"]))
      );let
  );let
);mainBody

(defn -main [& args]
  (let [cmd (args2Cmd args cli-options)]
    (or 
      (and (cmd :task) (mainBody cmd)) 
      (println (cmd :message))
    );or
  );let
);(-main)

(defn -run [& args]
  (info "-run auto-commit.core")
  (use 'auto-commit.phabricator :reload)
  (use 'auto-commit.core :reload)
  (-main args)
);(-run)

;(require '[clojure.tools.namespace.repl :refer [refresh]])
;(clojure.tools.namespace.repl/refresh)