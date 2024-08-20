(ns com.lambdaseq.stack.client-base.app
  (:require [com.lambdaseq.stack.client-base.app.router :as app-router]
            [com.lambdaseq.stack.frontend-router.api :as frontend-router]
            [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn App [ring-request]
  (e/client
    (let [match app-router/app-router]
      (binding [dom/node js/document.body
                frontend-router/router app-router/rf-router
                frontend-router/match match
                frontend-router/data (some-> match :data)
                frontend-router/name (some-> match :data :name)]
        (dom/h1 (dom/text "Hello, from Admin app!"))))))