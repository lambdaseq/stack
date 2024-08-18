(ns com.lambdaseq.stack.http-handler.core
  (:require [com.stuartsierra.component :as component]
            [reitit.ring :as ring]
            [com.lambdaseq.stack.protocols.api.provider.http-handler :as handler]
            [com.lambdaseq.stack.protocols.api.provider.http-router :as router]
            [com.lambdaseq.stack.logging.api :as log]))

(defrecord ReititHandlerProvider
  [router-provider handler]

  component/Lifecycle
  (start [this]
    (log/info! :starting-component {:component this})
    (->> router-provider
         (router/get-router)
         (ring/ring-handler)
         (assoc this :handler)))

  (stop [this]
    (log/info! :stopping-component {:component this})
    (assoc this :handler nil))

  handler/IHttpHandlerProvider
  (get-handler [_]
    handler))

(defn make-handler []
  (map->ReititHandlerProvider {}))