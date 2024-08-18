(ns com.lambdaseq.stack.api-router.api
  (:require [com.lambdaseq.stack.api-router.core :as core]))

(defn make-router
  [routes]
  (core/make-router routes))
