(ns com.lambdaseq.stack.admin-base.app
  (:require [hyperfiddle.electric :as e]
            [hyperfiddle.electric-dom2 :as dom]))

(e/defn App [req]
  (e/client
    (binding [dom/node js/document.body]
      (dom/div
        (dom/h1 (dom/text "Hello, from Admin app!"))))))