(ns com.lambdaseq.stack.persistence-xtdb2.core-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as component]
            [com.lambdaseq.stack.entity-manager.api :as-alias entity]
            [com.lambdaseq.stack.protocols.api.entity-manager :as em]
            [com.lambdaseq.stack.protocols.api.persistence :as persistence]
            [com.lambdaseq.stack.utils.test :refer :all]
            [com.lambdaseq.stack.utils.test.persistence :refer :all]))


#_(use-fixtures :each (with-system-fixture
                      (component/system-map
                        :xtdb-config {}
                        :entity-manager (doto (fresh-entity-manager)
                                    (em/merge-registry registry))
                        :persistence (component/using
                                       (fresh-xtdb-persistence)
                                       {:entity-manager :entity-manager
                                        :config   :xtdb-config}))))

(deftest fetch--test
  #_(persistence-fetch-tests))

#_(testing "Trying to fetch using a random-id should throw(?)"
    (is (= {}
           (catch-thrown-info
             (-> persistence
                 (persistence/fetch :person (random-uuid)))))))
