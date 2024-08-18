(ns com.lambdaseq.stack.client-base.app.home.routes
  (:require [hyperfiddle.electric :as e]
            [com.lambdaseq.stack.client-base.app.home.views :as views]
            [com.lambdaseq.stack.client-base.app.layout :as layout]))

(e/def routes
  (e/client
    ["/home" {:name   :home
              :layout layout/MainViewLayout
              :view   views/HomeView
              :title  "Home"}]))
