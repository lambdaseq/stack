(ns com.lambdaseq.stack.domain.core
  (:require [com.lambdaseq.stack.protocols.api.entity-manager :as-alias em]))

(def schemas
  {:user [:map {::em/name "User"}
          [:first-name :string]
          [:last-name :string]]})
