(ns com.lambdaseq.stack.persistence-datomic-pro.api
  (:require [com.lambdaseq.stack.persistence-datomic-pro.core :as core]))

(defn make-persistence []
  (core/make-persistence))
