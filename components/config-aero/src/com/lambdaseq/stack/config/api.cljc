(ns com.lambdaseq.stack.config.api
  (:require [com.lambdaseq.stack.config.core :as core]))

(defn load-config! [config-path]
  (core/load-config! config-path))


