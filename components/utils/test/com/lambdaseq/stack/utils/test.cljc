(ns com.lambdaseq.stack.utils.test
  (:require [com.stuartsierra.component :as component])
  #?(:clj (:import (clojure.lang ExceptionInfo))))

; System

(def ^:dynamic *system* nil)

(defmacro with-system [system & body]
  `(binding [*system* (component/start-system ~system)]
     ~@body
     (component/stop-system *system*)))


(defn with-system-fixture [system]
  (fn [f]
    (with-system system
                 (f))))

(defmacro catch-thrown-info [f]
  `(try
     ~f
     (catch ExceptionInfo e#
       {:message (ex-message e#)
        :data    (ex-data e#)})))