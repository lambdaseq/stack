(ns com.lambdaseq.stack.email-client-mailgun.api
  (:require [com.lambdaseq.stack.email-client-mailgun.core :as core]))

(defn make-email-client []
  (core/make-email-client))
