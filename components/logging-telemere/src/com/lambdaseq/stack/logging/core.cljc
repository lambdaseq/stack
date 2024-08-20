(ns com.lambdaseq.stack.logging.core
  (:require [taoensso.telemere :as t]))

(defmacro log! [message data]
  `(t/log! ~data ~message))

(defmacro info! [message data]
  `(log! ~message ~(assoc data :level :info)))

(defmacro warn! [message data]
  `(log! ~message ~(assoc data :level :warn)))

(defmacro error! [message data]
  `(log! ~message ~(assoc data :level :error)))

(defmacro debug! [message data]
  `(log! ~message ~(assoc data :level :debug)))

(defmacro with-context [ctx & body]
  (throw (ex-info "Not implemented" {})))

(defmacro trace! [id opts & body]
  (let [opts (assoc opts :id id)]
    `(t/trace! ~id ~opts ~@body)))



