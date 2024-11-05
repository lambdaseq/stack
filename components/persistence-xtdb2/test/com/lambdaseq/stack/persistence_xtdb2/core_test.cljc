(ns com.lambdaseq.stack.persistence-xtdb2.core-test
  (:require [clojure.test :refer :all]
            [com.lambdaseq.stack.entity-manager.api :as-alias entity]
            [com.lambdaseq.stack.entity-manager.core-test :refer [schemas]]
            [com.lambdaseq.stack.persistence-xtdb2.api :as xtdb2]
            [com.lambdaseq.stack.protocols.api.persistence :as persistence]
            [com.lambdaseq.stack.utils.test :refer :all]
            [com.lambdaseq.stack.utils.test.persistence :refer :all]
            [com.stuartsierra.component :as component]))


(use-fixtures :each (with-system-fixture
                      (component/system-map
                        :xtdb-config {}
                        :schemas schemas
                        :entity-manager (component/using
                                          (entity/make-entity-manager)
                                          [:schemas])
                        :persistence (component/using
                                       (xtdb2/make-persistence)
                                       {:entity-manager :entity-manager
                                        :config         :xtdb-config}))))

(deftest fetch--test
  #_(persistence-fetch-tests))

#_(testing "Trying to fetch using a random-id should throw(?)"
    (is (= {}
           (catch-thrown-info
             (-> persistence
                 (persistence/fetch :person (random-uuid)))))))
