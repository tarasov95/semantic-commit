(ns auto-doc.infa-test
  (:require [clojure.test :refer :all]
            [auto-doc.infra :refer :all :as i]))

(i/count-instances-by-location "appHost" "DE")
(i/count-instances-by-location "dbHost" "DE")
