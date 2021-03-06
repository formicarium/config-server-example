(defproject config-server-example "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [io.pedestal/pedestal.service "0.5.3"]
                 [io.pedestal/pedestal.jetty "0.5.3"]
                 [ch.qos.logback/logback-classic "1.2.3" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.25"]
                 [com.walmartlabs/lacinia-pedestal "0.5.0"]
                 [io.aviso/logging "0.2.0"]
                 [org.slf4j/jcl-over-slf4j "1.7.25"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.walmartlabs/lacinia "0.21.0"]
                 [org.slf4j/log4j-over-slf4j "1.7.25"]
                 [prismatic/schema "1.1.9"]
                 [clj-http "3.9.0"]
                 [http-kit "2.3.0"]
                 [beamly/beamly-core.config "0.1.1"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :test-paths ["test/" "selvage/"]
  :repositories  [["central"  {:url "https://repo1.maven.org/maven2/" :snapshots false}]
                  ["clojars"  {:url "https://clojars.org/repo/"}]]

  :profiles {:dev {:aliases {"run-dev" ["run" "-m" "config-server-example.server/run-dev"]}
                   :plugins [[lein-midje "3.2.1"]]
                   :dependencies [[io.pedestal/pedestal.service-tools "0.5.4"]
                                  [midje "1.9.1"]
                                  [clj-http-fake "1.0.3"]
                                  [http-kit.fake "0.2.1"]
                                  [nubank/selvage "0.0.1"]]}
             :uberjar {:aot [config-server-example.server]}}
  :main ^{:skip-aot true} config-server-example.server)

