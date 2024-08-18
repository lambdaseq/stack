(ns com.lambdaseq.stack.persistence-xtdb.api
  (:require [com.lambdaseq.stack.persistence-xtdb.core :as core]))

(defn make-persistence []
  (core/make-persistence))
