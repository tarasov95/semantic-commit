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

;(clojure.tools.namespace.repl/refresh)
;(-main "-t" "2007")

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
);startSess

(def lazySess (memoize startSess))

(defn getSess[]
  (lazySess (getConf))
);getSess

(def lazyTask (memoize phb/queryTask))

(defn getTask[& args]
  (apply lazyTask (cons (getSess) args))
);getTask

(def lazyProjects (memoize  phb/queryProjects))

(defn getProjects[& args]
  (apply lazyProjects (cons (getSess) args))
);getProjects

(defn getTaskProjects [task]
  (((getProjects (task "projectPHIDs")) :result) "data")
);getTaskProjects

(defn selectProjectNames [mapProj]
  (map (fn [el] (el "name")) (map (fn [el] (last el)) mapProj))
);selectProjectNames

(defn subjFromTask [taskId]
  (let [
      task ((getTask taskId) :result)
    ]
    {:p (reduce str (selectProjectNames (getTaskProjects task)))
     :t (task "objectName")
     :state (task "status")
     :s (task "title")
    }
  );let
);subjT

(defn commit-subject
  ([taskId state remarks]  (print-str (merge (subjFromTask taskId) {:state state :remarks remarks})))
  ([taskId state] (print-str (merge (subjFromTask taskId) {:state state})))
  ([taskId] (print-str (merge (subjFromTask taskId))))
)

;; (defn printTask [cmd]
;;   (let [
;;       task ((getTask (cmd :task)) :result)
;;     ]
;;     ;(def lastTask task)
;;     ;(def lastProj (getTaskProjects task))
;;     (info "task" (select-keys task ["title" "status" "objectName" "statusName" "projectPHIDs"]))
;;     ;(info "task project IDs" (task "projectPHIDs"))
;;     ;(info "task projects" (or  (getTaskProjects task) "null"))
;;     (info (reduce str (selectProjectNames (getTaskProjects task))) (task "status") (task "objectName") (task "title"))
;;   );let
;; );printTask

(defn proj
  ([]  ((getConf) :proj))
  ([name] ((proj) name))
);proj


(defn proj-list [] (map (fn [el] (el 0)) (proj)));

(defn mapProjProp [rgProj sPropName] (map (fn [el] ((el 1) sPropName)) rgProj));
(defn proj-prop
  ([sPropName] (mapProjProp (proj) sPropName))
  ([sProjName sPropName] (first (mapProjProp (filter (fn [el] (= (el 0) sProjName)) (proj)) sPropName)))
);proj-prop


(defn projInfo [sName mapProps]
  (fn
    ([] sName)
    ([sPropName] (mapProps sPropName))
  );
);

(defn def-proj [sName]
  (let [id  (clojure.string/replace sName  "/" "-")]
    (eval (read-string
            (str "(def " id " \"" sName "\")")
          )
    );eval
    (eval (read-string
            (str "(def proj-" id " (projInfo \"" sName "\" (proj \"" sName "\")))")
          )
    );eval
  );let
);def-proj

(defn injectProjects []
  (doall (map (fn [el] (def-proj (el 0))) (proj)))
);injectProjects

(def _rgProjFunc (injectProjects));


(defn proj-status-data
  ([sProjName]
     (map (fn [l] {:path (subs l 7) :status (clojure.string/trim (subs l 0 7)) :proj sProjName})
          (filter (fn [l] (not (empty? l)))
            (clojure.string/split-lines (:out (svn "status" ((proj sProjName) :path))))
          );filter
     );map
  )
  ([] (mapcat (fn [el] (proj-status-data el)) (proj-list)))
);proj-status

(defn proj-status-data-filtered [& args]
  (filter (fn [s] (< (count (:status s)) 2))
    (apply proj-status-data args)
  );filter
);proj-status

(defn proj-status [& args]
  (clojure.pprint/print-table
    (filter (fn [e] (not= (:status e) "X"))
      (apply proj-status-data-filtered args)
    )
  )
);proj-status-print


(defn proj-commit-data [sProjName & args]
  (clojure.string/split-lines (:out (svn "commit" ((proj sProjName) :path) "-m" (apply commit-subject args))))
);proj-commit

(defn proj-commit [& args]
  (clojure.pprint/pprint
    (apply proj-commit-data args)
  )
);proj-commit-print

;; (defn -main [& args]
;;   (let [cmd (args2Cmd args cli-options)]
;;     (or
;;       (and (cmd :task) (printTask cmd))
;;       (println (cmd :message))
;;     );or
;;   );let
;; );(-main)

;; (defn -run [& args]
;;   (use 'auto-commit.phabricator :reload)
;;   (use 'auto-commit.core :reload)
;;   (info "reloaded")
;;   (apply -main args)
;; );(-run)

;(require '[clojure.tools.namespace.repl :refer [refresh]])
;(clojure.tools.namespace.repl/refresh)
