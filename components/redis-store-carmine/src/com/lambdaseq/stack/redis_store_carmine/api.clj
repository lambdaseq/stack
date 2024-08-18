(ns com.lambdaseq.stack.redis-store-carmine.api
  (:require [com.lambdaseq.stack.redis-store-carmine.core :as core]))

(defn make-redis-store []
  (core/make-redis-store))
