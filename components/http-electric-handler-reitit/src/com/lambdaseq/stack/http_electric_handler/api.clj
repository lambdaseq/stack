(ns com.lambdaseq.stack.http-electric-handler.api
  (:require [com.lambdaseq.stack.http-electric-handler.core :as core]))

; TODO: maybe there should be a electric handler provided that's a component
(defn electric-handler [entrypoint config]
  (core/electric-handler entrypoint config))
