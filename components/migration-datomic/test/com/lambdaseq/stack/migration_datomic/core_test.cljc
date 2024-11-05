(ns com.lambdaseq.stack.migration-datomic.core-test
  (:require [clojure.test :refer :all]
            [com.lambdaseq.stack.entity-manager.api :as-alias entity]
            [com.lambdaseq.stack.entity-manager.core-test :refer [schemas]]
            [com.lambdaseq.stack.migration-datomic.api :as migration-datomic]
            [com.lambdaseq.stack.persistence-schema-transformer-malli-datomic.api :as dpst]
            [com.lambdaseq.stack.protocols.api.migration :as migration]
            [com.lambdaseq.stack.utils.test :refer :all]
            [com.stuartsierra.component :as component]))

(use-fixtures :each (with-system-fixture
                      (component/system-map
                        :schemas schemas
                        :entity-manager (component/using
                                          (entity/make-entity-manager)
                                          [:schemas])
                        :persistence-transformer
                        (component/using
                          (dpst/make-persistence-schema-transformer)
                          [:entity-manager])
                        :migration (component/using
                                     (migration-datomic/make-migration)
                                     [:entity-manager :persistence-transformer]))))

(deftest gen-migration--test
  (testing "Generating migration"
    (let [{:keys [migration]} *system*]
      (is (= [{:db/ident       :person/id
               :db/valueType   :db.type/uuid
               :db/cardinality :db.cardinality/one}
              {:db/ident       :person/first-name
               :db/cardinality :db.cardinality/one
               :db/valueType   :db.type/string}
              {:db/ident       :person/last-name
               :db/cardinality :db.cardinality/one
               :db/valueType   :db.type/string}
              {:db/ident       :person/age
               :db/cardinality :db.cardinality/one
               :db/valueType   :db.type/long}]
             (migration/gen-migration migration))))))
