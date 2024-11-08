(ns com.lambdaseq.stack.persistence-datomic-pro.core
  (:require [com.lambdaseq.stack.datalog-query-builder.api :refer [build-query]]
            [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.protocols.api.entity-manager :as em]
            [com.lambdaseq.stack.protocols.api.migration :as migration]
            [com.lambdaseq.stack.protocols.api.persistence :as persistence]
            [com.stuartsierra.component :as component]
            [datomic.api :as d])
  (:import (clojure.lang ExceptionInfo)))


(defrecord DatomicProPersistence [config migration entity-manager conn txops]

  component/Lifecycle
  (start [this]
    (log/info! :starting-component {:component this})
    (let [{:keys [uri]} config]
      (d/create-database uri)
      (let [conn (d/connect uri)]
        (when migration
          (let [schema (migration/gen-migration migration)]
            (when-let [tx-data (seq schema)]
              @(d/transact conn tx-data))))
        (-> this
            (assoc :conn conn)
            (persistence/clear-txs)))))

  (stop [this]
    (log/info! :stopping-component {:component this})
    (let [{:keys [uri]} config]
      (d/delete-database uri)
      (-> this
          (assoc :config nil)
          (assoc :migration nil)
          (assoc :entity-manager nil)
          (assoc :conn nil)
          (assoc :txops nil))))

  persistence/IPersistence
  (db-id-key [_this _schema] :db/id)

  ; TODO: Not the most efficient, fetches from db
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

  (fetch [_this schema id]
    (let [entity-id-key (em/entity-id-key entity-manager schema)
          db (d/db conn)
          entity (-> '[:find [(pull ?e [*])]
                       :in $ ?id-attr ?id
                       :where [?e ?id-attr ?id]]
                     (d/q db entity-id-key id)
                     (first))]
      (when-not entity
        (throw (ex-info "Entity not found" {:schema  schema
                                            :id-attr entity-id-key
                                            :id      id})))
      entity))

  (search [this schema]
    (persistence/search this schema {}))

  (search [_this schema {:keys [keys where] :as _opts}]
    (let [db (d/db conn)
          {:keys [query args]} (build-query {:keys  keys
                                             :where where
                                             :datomic? true}
                                            (em/entity-id-key entity-manager schema))
          q-fn (partial d/q query db)
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

  (clear-txs [this]
    (assoc this :txops []))

  (transact! [this]
    (let [tx-data (->> txops
                       (map
                         (fn [{:keys [tx-action entity-id entity-schema entity-data]}]
                           (case tx-action
                             :persist entity-data
                             ; If :db/id exists in the map, use that, otherwise find it.
                             :patch (let [db-id (or (:db/id entity-data)
                                                    (persistence/db-id this entity-schema entity-id))]
                                      (assoc entity-data :db/id db-id))
                             :delete (let [db-id (or (:db/id entity-data)
                                                     (persistence/db-id this entity-schema entity-id))]
                                       [:db.fn/retractEntity db-id]))))
                       (into []))]
      @(d/transact conn tx-data)
      (-> this
          (persistence/clear-txs)))))


(defn make-persistence []
  (map->DatomicProPersistence {}))