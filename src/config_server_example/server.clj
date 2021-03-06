(ns config-server-example.server
  (:gen-class)
  (:require [io.pedestal.http :as server]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor :as int]
            [com.stuartsierra.component :as component]
            [config-server-example.component :as config-server-example]
            [com.walmartlabs.lacinia.pedestal :as lp]
            [config-server-example.service :as service]))



(defn run-dev
  "The entry-point for 'lein run-dev'"
  [& args]
  (println "Running system map")
  (component/start-system (config-server-example/system-map :dev)))

(defn -main
  "The entry-point for 'lein run'"
  [& args]
  (println "\nCreating your server...")
  (component/start-system (config-server-example/system-map :prod)))

