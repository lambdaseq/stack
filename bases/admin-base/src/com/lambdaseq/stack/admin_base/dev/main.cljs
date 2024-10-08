(ns com.lambdaseq.stack.admin-base.dev.main
  (:require [hyperfiddle.electric :as e]
            [com.lambdaseq.stack.web-app.main :as app]
            [com.lambdaseq.stack.admin-base.app :refer [App]]))

(def client-entrypoint (e/boot-client {} App nil))

(defn ^:dev/after-load ^:export start! []
  (app/start! client-entrypoint))

(defn ^:dev/before-load stop! []
  (app/stop!))
