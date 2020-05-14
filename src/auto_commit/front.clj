(ns auto-commit.front
  (:require [auto-commit.core :as core]
            [clojure.pprint :as pp]
            [auto-doc.infra :as infra]))


(def proj-commit core/proj-commit)

(def commit-subject core/commit-subject)

(def proj-status  core/proj-status)

(def _projects (core/injectProjects))


(defn count-instances-by-iis []
  (pp/pprint (sort-by val (infra/count-instances-by "appHost"))))

(defn count-instances-by-sql []
  (pp/pprint (sort-by val (infra/count-instances-by "dbHost"))))

(defn list-instances-by-iis [hostId]
  (pp/pprint (sort (map first (infra/list-instances-by "appHost" hostId)))))

(def find-host infra/find-host)

(def count-instances-iis-by-location (partial infra/count-instances-by-location "appHost"))

(def count-instances-sql-by-location (partial infra/count-instances-by-location "dbHost"))
