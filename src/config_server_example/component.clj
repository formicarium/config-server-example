(ns config-server-example.component
  (:require [com.stuartsierra.component :as component]
            [config-server-example.service :as service]
            [config-server-example.components.config.config :as config]
            [config-server-example.components.api.config-server-example-api :as config-server-example-api]))

(defn system-map
  [env]
  {:config       (config/new-config)
   :service-map  (service/create-service env)
   :config-server-example-api     (component/using (config-server-example-api/new-config-server-example-api) [:service-map :config])})

