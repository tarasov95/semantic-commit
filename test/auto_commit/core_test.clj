(ns auto-commit.core-test
  (:require [clojure.test :refer :all]
            [auto-commit.core :refer :all]
            [clojure.repl :refer :all]
            [clojure.tools.namespace.repl]
            [taoensso.timbre :as timbre
              :refer [spy info]]
            ))

;; (deftest a-test
;;   (testing "FIXME, I fail."
;;     (is (= 0 1))))

;; (source ns)
;; (doc clojure.pprint/print-table)

;(clojure.tools.namespace.repl/refresh)

;; (proj-list)


;; (proj-status-print fs-extra)

;; (proj-status-print)



;; (commit fs-aster 2053 "fix")


;; (map (fn [el] (println "!!!!!!" el)) (proj-status))
;(println (proj-status fs-core))

;; (injectProjects)

;; fs-extra
;; (proj-fs-extra :path)
;; (fs-core)
;; (fs-core :path)
;(map (fn [el] (defn-with-str (clojure.string/replace "fs/core" "/" "-") ) (proj))
;(println (map (fn [sPath] (svn "status" sPath)) (proj-prop :path)))

;(println (:out (svn "status" (proj-prop "fs/core" :path))))



