(ns config-server-example.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [cheshire.core :as cheshire]
            [io.pedestal.interceptor.helpers :as int-helpers]))


(def externalize-json (int-helpers/on-response ::json-response
                                               (fn [response]
                                                 (println response)
                                                 (-> response
                                                     (update-in [:body] (fn [body] (if body
                                                                                     (cheshire/generate-string body)
                                                                                     body)))
                                                     (update-in [:headers] (fn [headers] (-> (or headers {})
                                                                                             (assoc "Content-Type" "application/json"))))))))


(defn get-health
  [request]
  {:status  200
   :headers {}
   :body    {:healthy "foda-se man5!!!"}})

(defn svc->nu-env-config [{:keys [service-name transactor-host transactor-port]}]
  (cheshire.core/generate-string {:environment "local"
                                  :zk_primary "zookeeper"
                                  :router "linear-search"
                                  :kafka_topics {:absolved-delinquent {:topic "ABSOLVED-DELINQUENT"
                                                                       :direction "producer"}}
                                  :datomic_uri (str "datomic:dev://" transactor-host ":" transactor-port "/" service-name)}))

(defn on-deploy-service
  [request]
  {:status  200
   :headers {}
   :body
   (let [{:keys [name] :as params} (:json-params request)
         aws-secret-key (System/getenv "AWS_SECRET_KEY"); some account with read access to the keysets to boot the svc
         aws-access-key-id (System/getenv "AWS_ACCESS_KEY_ID")]
     {:name       name
      :containers [{:name      "transactor"
                    :image     "quay.io/nubank/nudev-transactor:7edadc4"
                    :syncable? false
                    :env       {:TRANSACTOR_HOST     "transactor"
                                :TRANSACTOR_PORT     "4334"
                                :TRANSACTOR_ALT_HOST "transactor"}}
                   {:name      "zookeeper"
                    :image     "quay.io/nubank/nudev-exhibitor"
                    :syncable? false
                    :env       {}}
                   {:name      "kafka"
                    :image     "quay.io/nubank/nudev-kafka"
                    :syncable? false
                    :env       {}}
                   {:name      "dynamodb"
                    :image     "quay.io/nubank/nudev-dynamodb"
                    :syncable? false
                    :env       {}}
                   {:name      name
                    :image     (str "quay.io/nubank/nu-" name)
                    :syncable? true
                    :env       {:NU_ENV_CONFIG  (svc->nu-env-config {:service-name name
                                                                     :transactor-host "transactor"
                                                                     :transactor-port "4334"})
                                :AWS_ACCESS_KEY_ID aws-access-key-id
                                :AWS_SECRET_KEY aws-secret-key}}]
      :interfaces [{:name      "transactor-1"
                    :port      "4334"
                    :container "transactor"
                    :type      :tcp}
                   {:name      "transactor-2"
                    :port      "4335"
                    :container "transactor"
                    :type      :tcp}
                   {:name      "transactor-3"
                    :port      "4336"
                    :container "transactor"
                    :type      :tcp}
                   {:name      "zookeeper-client"
                    :port      "2181"
                    :container "zookeeper"
                    :type      :tcp}
                   {:name      "zookeeper-follower"
                    :port      "2888"
                    :container "zookeeper"
                    :type      :tcp}
                   {:name      "zookeeper-leader"
                    :port      "3888"
                    :container "zookeeper"
                    :type      :tcp}
                   {:name      "zookeeper-exhibitor"
                    :port      "3888"
                    :container "zookeeper"
                    :type      :tcp}
                   {:name      "kafka-client"
                    :port      "9092"
                    :container "kafka"
                    :type      :tcp}
                   {:name      "dynamodb-client"
                    :port      "8000"
                    :container "dynamodb"
                    :type      :tcp}]})})

(def routes
  `[[["/" ^:interceptors [(body-params/body-params) externalize-json] {:get [:get-health get-health]}
      ["/ondeployservice" {:post [:on-deploy-service on-deploy-service]}]]]])


;; Consumed by config-server-example.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env                     :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes            routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path     "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type              :jetty
              ;;::http/host "localhost"
              ::http/port              8081
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2?  false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})

(defn create-service
  [env]
  (case env
    :prod (merge service {:env :prod})
    :dev (merge service {:env :dev})
    :test (merge service {:env :test})))

