(ns com.lambdaseq.stack.basic-http-router.api
  (:require [com.lambdaseq.stack.basic-http-router.core :as core]))

(defn make-router
  ([] (core/make-router))
  ([routes] (core/make-router routes)))
