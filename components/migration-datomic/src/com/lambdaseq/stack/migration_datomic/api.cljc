(ns com.lambdaseq.stack.migration-datomic.api
  (:require [com.lambdaseq.stack.migration-datomic.core :as core]))

(defn make-migration []
  (core/make-migration))
