(ns com.lambdaseq.stack.http-server.core
  (:require [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.protocols.api.provider.http-handler :as handler]
            [com.stuartsierra.component :as component]
            [strojure.undertow.server :as server]))

(defrecord UndertowHttpServer [config handler-provider server]
  component/Lifecycle
  (start [this]
    (log/info! :starting-component {:component this})
    (-> this
        (assoc :server (-> config
                           (assoc :handler (handler/get-handler handler-provider))
                           (server/start)))))
  (stop [this]
    (log/info! :stopping-component {:component this})
    (when server
      (server/stop server))
    (-> this
        (assoc :config nil)
        (assoc :handler-provider nil)
        (assoc :server nil))))

(defn make-server []
  (map->UndertowHttpServer {}))
