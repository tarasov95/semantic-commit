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
    (:require [clojure.tools.namespace.repl :refer [refresh]])
    (:require [clojure.java.shell :only [sh]])
)

;https://github.com/hach-que/Phabricator.Conduit/blob/master/ConduitClient.cs

(def cli-options
  [
   ["-h" "--help"]
   ["-t" "--task TASKID" "Manifest task ID" :parse-fn #(Integer/parseInt %)]
  ]
);--cli-options

(defn args2Cmd [args opt]
  (let [cmd (parse-opts args opt)]
    (or 
      (and (cmd :errors) {:message (cmd :errors)})
      (and ((cmd :options) :help) {:message (cmd :summary)})
      (cmd :options)
    );or
  );let
);args2Cmd

(defn svn [& args]
  (apply clojure.java.shell/sh (cons "svn" args))
)

(defn loadConf []
  (aero/read-config "config.edn")
);loadConf

(def getConf (memoize loadConf));

(defn startSess[conf] 
  (let [cnfPhb (conf :phabricator)] 
    (phb/session (cnfPhb :url) (cnfPhb :user-name) (cnfPhb :user-certificate))
  );let
);getSess

(def getSess (memoize 
  (fn [] (startSess (getConf)))
));getSess

(def getTask (memoize
  (partial phb/queryTask (getSess))
));getTask

(defn printTask [cmd]
  (let [
      task (getTask (cmd :task))
    ]
    (info "task" (select-keys (task :result) ["title" "status" "objectName" "statusName"]))
  );let
);printTask

(defn -main [& args]
  (let [cmd (args2Cmd args cli-options)]
    (or 
      (and (cmd :task) (printTask cmd)) 
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