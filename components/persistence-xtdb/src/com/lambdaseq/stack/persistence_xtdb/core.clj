(ns com.lambdaseq.stack.persistence-xtdb.core
  (:require [com.lambdaseq.stack.datalog-query-builder.api :refer [build-query]]
            [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.protocols.api.entity-manager :as em]
            [com.lambdaseq.stack.protocols.api.persistence :as persistence]
            [com.stuartsierra.component :as component]
            [xtdb.api :as xt])
  (:import (clojure.lang ExceptionInfo)
           (java.io Closeable)))

(defrecord XtdbPersistence [config node entity-manager txops]
  component/Lifecycle
  (start [this]
    (log/info! :starting-component {:component this})
    (let [node (xt/start-node config)]
      (-> this
          (assoc :node node))))
  (stop [this]
    (log/info! :stopping-component {:component this})
    (.close ^Closeable node)
    (-> this
        (assoc :config nil)
        (assoc :node nil)
        (assoc :entity-manager nil)
        (assoc :txops nil)))

  persistence/IPersistence
  ; TODO: Should think about how to separate db id from entity id (if it should be separate)
  (db-id-key [_this _schema] :xt/id)

  (db-id [this schema entity-id]
    (let [entity (persistence/fetch this schema entity-id)]
      (get entity (persistence/db-id-key this schema))))

  (prepare [_this schema data]
    (try
      (em/validate entity-manager schema data)
      (catch ExceptionInfo e
        (let [entity-name (em/name entity-manager schema)]
          (throw (ex-info (str "Data is not a valid " entity-name)
                          {:entity-schema schema
                           :errors        (ex-data e)}
                          e)))))
    data)

  (fetch [_this _schema id]
    (xt/entity (xt/db node) id))

  (search [this schema]
    (persistence/search this schema {}))

  (search [_this schema {:keys [keys where]}]
    (let [db (xt/db node)
          {:keys [query args]} (build-query {:keys  keys
                                             :where where}
                                            (em/entity-id-key entity-manager schema))
          q-fn (partial xt/q query db)
          res (apply q-fn args)]
      (apply concat res)))

  (persist! [this schema data]
    (-> this
        (persistence/persist schema data)
        (persistence/transact!))
    (persistence/fetch this schema (em/entity-id entity-manager schema data)))

  (patch! [this schema id data]
    (-> this
        (persistence/patch schema id data)
        (persistence/transact!))
    (persistence/fetch this schema id))

  (delete! [this schema id]
    (-> this
        (persistence/delete schema id)
        (persistence/transact!))
    id)

  persistence/ITransactionalPersistence
  (persist [this schema data]
    (-> this
        (update :txops conj
                {:tx-action     :persist
                 :entity-schema schema
                 :entity-data   (persistence/prepare this schema data)})))
  (patch [this schema id data]
    (-> this
        (update :txops conj
                {:tx-action     :patch
                 :entity-schema schema
                 :entity-id     id
                 :entity-data   (persistence/prepare this (em/optional-keys entity-manager schema) data)})))

  (delete [this schema id]
    (-> this
        (update :txops conj
                {:tx-action     :delete
                 :entity-schema schema
                 :entity-id     id})))

  (clear-txs [this] (assoc this :txops []))

  (transact! [this]
    ; TODO: Implement this
    (throw (ex-info "Not Implemented" {}))))

(defn make-persistence []
  (map->XtdbPersistence {}))
