(defproject auto-commit "0.1.0-SNAPSHOT"
  :description "Semantic commit for phabricator-svn"
  :url "https://phs.focalscope.com/w/phabricator_workflow/semantic_commit/"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
    [org.clojure/clojure "1.8.0"]
    [http-kit "2.2.0"]
    [org.clojure/data.json "0.2.6"]
    [com.taoensso/timbre "4.10.0"]
    [pandect "0.6.1"]
    [aero "1.1.2"]
    [org.clojure/tools.cli "0.3.5"]
    [org.clojure/tools.namespace "0.2.11"]
  ]
  :main ^:skip-aot auto-commit.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
