(ns com.lambdaseq.stack.basic-http-router-reitit.api
  (:require [com.lambdaseq.stack.basic-http-router-reitit.core :as core]))

(defn make-router
  ([] (core/make-router))
  ([routes] (core/make-router routes)))
