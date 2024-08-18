(ns com.lambdaseq.stack.migration-datomic.core
  (:require [com.stuartsierra.component :as component]
            [com.lambdaseq.stack.entity-manager.api :as-alias entity]
            [com.lambdaseq.stack.logging.api :as log]
            [com.lambdaseq.stack.protocols.api.entity-manager :as em]
            [com.lambdaseq.stack.protocols.api.migration :as migration]
            [com.lambdaseq.stack.protocols.api.persistence :as-alias persistence]
            [com.lambdaseq.stack.protocols.api.transformer.schema :as ts]))

(defrecord DatomicMigration [entity-manager persistence-transformer]

  component/Lifecycle

  (start [this]
    (log/info! :starting-component {:component this})
    this)

  (stop [this]
    (log/info! :stopping-component {:component this})
    (-> this
        (assoc :entity-manager nil)))

  migration/IMigration
  (gen-migration [_]
    (let [schemas (em/get-entities entity-manager)]
      (transduce (comp (filter #(em/entity? entity-manager %))
                       (mapcat #(ts/transform persistence-transformer %)))
                 conj
                 (vals schemas)))))

(defn make-migration []
  (map->DatomicMigration {}))
